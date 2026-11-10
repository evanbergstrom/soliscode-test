/*
 * Copyright 2026 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soliscode.test.contract.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Support for the contracts of [BlockingQueue]: a full queue to test capacity on, calls that may block run with a
/// time limit, a test that one thread's call waits until another thread's call, and a comparison of elements that
/// counts repeats.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see com.soliscode.test.contract.blockingqueue.BlockingQueueContract
/// @since 0.1.2
public interface BlockingQueueContractSupport<Q extends BlockingQueue<E>, E> extends CollectionContractSupport<Q, E> {

    /// The most places ({@value}) a queue may have left for the capacity tests to fill it. A queue with more,
    /// `LinkedBlockingQueue()` among them, is not tested when full.
    int FILL_LIMIT = 1_000;

    /// How long ({@value} s) a call that may block is given before the test fails: a call that should return at once
    /// and does not, fails the test rather than hanging it.
    int BLOCKING_TIMEOUT_SECONDS = 5;

    /// The timeout ({@value} ms) given to the timed `offer` and `poll`.
    long TIMED_WAIT_MILLIS = 50;

    /// How long ({@value} ms), at most, the releasing thread of [#assertWaitsFor] waits to see the other thread
    /// waiting, before it releases it anyway.
    long SPIN_LIMIT_MILLIS = 1_000;

    /// A call on a queue that may block or throw a checked exception.
    ///
    /// @param <Q> the queue type
    @FunctionalInterface
    interface QueueAction<Q> {
        /// Calls the queue.
        ///
        /// @param queue the queue
        /// @throws Exception what the call throws
        void accept(Q queue) throws Exception;
    }

    /// A call that may block or throw a checked exception.
    @FunctionalInterface
    interface BlockingAction {
        /// Makes the call.
        ///
        /// @throws Exception what the call throws
        void run() throws Exception;
    }

    /// Returns a full queue, or empty if the queue has more than [#FILL_LIMIT] places (an unbounded queue among
    /// them) or does not support `remainingCapacity()`.
    ///
    /// It reads the remaining capacity of an empty instance, and makes an instance holding that many unique elements
    /// through the provider, so that no method under test fills it. It asserts that the queue holds them and reports
    /// no room left.
    ///
    /// @return the full queue, or empty
    default Optional<Q> fullInstance() {
        if (!supportsMethod(com.soliscode.test.contract.blockingqueue.BlockingQueueMethods.REMAINING_CAPACITY)) {
            return Optional.empty();
        }
        int room = provider().emptyInstance().remainingCapacity();
        if (room > FILL_LIMIT) {
            return Optional.empty();
        }
        Q queue = provider().createInstance(elementProvider().createUniqueInstances(room));
        assertEquals(room, queue.size(), "a queue made with as many elements as its remaining capacity");
        assertEquals(0, queue.remainingCapacity(), "remainingCapacity() of a queue made full");
        return Optional.of(queue);
    }

    /// Makes a call that may block, on a daemon thread of its own, and returns its result; fails the test if it does
    /// not return within [#BLOCKING_TIMEOUT_SECONDS], after interrupting the thread.
    ///
    /// An exception the call throws is thrown again, as itself, so that a test can expect it. If the calling thread
    /// is interrupted while it waits, the call's thread is interrupted too, the caller's interrupt status is set
    /// again, and the test fails: that interrupt is not the call's.
    ///
    /// @param call the call
    /// @param <T> the result type
    /// @return the call's result
    /// @throws Exception what the call throws
    default <T extends @Nullable Object> T bounded(final Callable<T> call) throws Exception {
        FutureTask<T> task = new FutureTask<>(call);
        Thread thread = new Thread(task, "blocking-queue-contract");
        thread.setDaemon(true);
        thread.start();
        try {
            return task.get(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            thread.interrupt();
            throw new AssertionFailedError("the call did not return within " + BLOCKING_TIMEOUT_SECONDS
                    + " s: it blocked", e);
        } catch (final InterruptedException e) {
            thread.interrupt();
            Thread.currentThread().interrupt();
            throw new AssertionFailedError("the test thread was interrupted while it waited for the call", e);
        } catch (final ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }

    /// Makes a call that may block, as [#bounded(Callable)] does, for a call with no result.
    ///
    /// @param action the call
    /// @throws Exception what the call throws
    default void boundedRun(final BlockingAction action) throws Exception {
        bounded(() -> {
            action.run();
            return null;
        });
    }

    /// Makes a call that may block with the thread's interrupt status set, as [#boundedRun] does, and clears the
    /// status afterwards. The status is set on the thread that makes the call.
    ///
    /// @param action the call
    /// @throws Exception what the call throws, [InterruptedException] for a call that waits
    default void boundedInterrupted(final BlockingAction action) throws Exception {
        boundedRun(() -> {
            Thread.currentThread().interrupt();
            try {
                action.run();
            } finally {
                boolean ignored = Thread.interrupted();
            }
        });
    }

    /// Asserts that one call waits until another thread's call releases it: `waiting` must not return before
    /// `release` has been called.
    ///
    /// Two threads of a [ContentionCoordinator] run the calls, once each. The releasing thread waits (with
    /// [Thread#onSpinWait], at most [#SPIN_LIMIT_MILLIS]) until the other is waiting, then calls `release`. The
    /// result does not depend on timing: if the releasing thread stops waiting early, a correct queue still passes.
    ///
    /// @param queue the queue
    /// @param waiting the call that should wait (a `put` on a full queue, a `take` on an empty one)
    /// @param release the call that should release it
    default void assertWaitsFor(final Q queue, final QueueAction<Q> waiting, final QueueAction<Q> release) {
        AtomicReference<@Nullable Thread> waiter = new AtomicReference<>();
        AtomicBoolean released = new AtomicBoolean();
        ContentionCoordinator<Q> coordinator = new ContentionCoordinator<>(BLOCKING_TIMEOUT_SECONDS,
                BLOCKING_TIMEOUT_SECONDS, ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
        coordinator.add(1, 1, unchecked(q -> {
            waiter.set(Thread.currentThread());
            waiting.accept(q);
            assertTrue(released.get(), "the call returned before another thread made it possible");
        }));
        coordinator.add(1, 1, unchecked(q -> {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SPIN_LIMIT_MILLIS);
            while (!isWaiting(waiter.get()) && System.nanoTime() - deadline < 0) {
                Thread.onSpinWait();
            }
            released.set(true);
            release.accept(q);
        }));
        coordinator.execute(queue);
    }

    /// Turns a call that throws checked exceptions into a [Consumer] for a [ContentionCoordinator]; a checked
    /// exception becomes an [AssertionError], and an [InterruptedException] sets the interrupt status again.
    ///
    /// @param action the call
    /// @return the consumer
    default Consumer<Q> unchecked(final QueueAction<Q> action) {
        return q -> {
            try {
                action.accept(q);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("interrupted", e);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new AssertionError(e);
            }
        };
    }

    /// Asserts that two collections hold the same elements, each as many times.
    ///
    /// @param expected the elements expected
    /// @param actual the elements found
    /// @param description what is compared, for the failure message
    default void assertSameElements(final Iterable<? extends @Nullable Object> expected,
                                    final Iterable<? extends @Nullable Object> actual, final String description) {
        assertEquals(counts(expected), counts(actual), description);
    }

    /// Returns whether a thread is waiting: in the state [Thread.State#WAITING] or [Thread.State#TIMED_WAITING].
    /// A thread that is waiting may be waiting for the queue, or for something else, such as a lock.
    ///
    /// @param thread the thread, or `null` for none yet
    /// @return `true` if the thread is waiting
    default boolean isWaiting(final @Nullable Thread thread) {
        if (thread == null) {
            return false;
        }
        Thread.State state = thread.getState();
        return state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING;
    }

    private static Map<@Nullable Object, Integer> counts(final Iterable<? extends @Nullable Object> elements) {
        Map<@Nullable Object, Integer> counts = new HashMap<>();
        for (final Object e : elements) {
            counts.merge(e, 1, Integer::sum);
        }
        return counts;
    }
}
