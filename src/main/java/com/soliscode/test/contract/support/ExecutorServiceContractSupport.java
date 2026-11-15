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

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// The base interface of the executor contracts: it gives them the provider's executors, checked and shut down after
/// each test, gates that hold tasks running or queued, calls that may block run with a time limit, and a test that
/// one thread's call waits until another thread releases it.
///
/// Every call the contracts make that may block is bounded in time: through [#bounded], by the
/// [ContentionCoordinator] or the deadline that runs it ([#assertWaitsFor], the thread-safety contracts), or by the
/// gate's own limit on how long a task waits, so that a faulty executor fails a test rather than hanging it.
///
/// The bounded calls are copies of [FutureContractSupport]'s, as those are of [BlockingQueueContractSupport]'s:
/// moving them into an interface both extend would rename `BlockingQueueContractSupport.BlockingAction` and
/// `FutureContractSupport.BlockingCall`, which have shipped, and break compiled callers (#44, #45).
///
/// @param <E> the type of the executor under test
/// @see com.soliscode.test.contract.executorservice.ExecutorServiceContract
/// @since 0.1.5
public interface ExecutorServiceContractSupport<E extends ExecutorService> extends ExecutorServiceProviderSupport<E> {

    /// How long ({@value} s) a call that may block is given before the test fails: a call that should return at once
    /// and does not, fails the test rather than hanging it. A gate's tasks wait no longer than this either.
    int BLOCKING_TIMEOUT_SECONDS = 5;

    /// The timeout ({@value} ms) given to a timed call that should time out.
    long TIMED_WAIT_MILLIS = 50;

    /// How long ({@value} ms), at most, the releasing thread of [#assertWaitsFor] waits to see the other thread
    /// waiting, before it releases it anyway.
    long SPIN_LIMIT_MILLIS = 1_000;

    /// The longest ({@value} ms) [#dispose] waits for an executor to terminate after each test.
    long DISPOSE_TIMEOUT_MILLIS = 3_000;

    /// The number of tasks ({@value}) a test holds at a gate, so that some are queued on a small pool.
    int HELD_TASKS = 8;

    /// A call that may block or throw a checked exception.
    @FunctionalInterface
    interface BlockingCall {
        /// Makes the call.
        ///
        /// @throws Exception what the call throws
        void run() throws Exception;
    }

    /// A test on an executor.
    ///
    /// @param <E> the type of the executor
    @FunctionalInterface
    interface ExecutorCall<E> {
        /// Runs the test.
        ///
        /// @param executor the executor
        /// @throws Exception what the test throws
        void run(E executor) throws Exception;
    }

    /// A gate that tasks wait at, so that a test can hold them running or queued, and then release them. It counts
    /// the tasks that started and the tasks that ended.
    ///
    /// A task waits, interruptibly, until the gate opens, and no longer than [#BLOCKING_TIMEOUT_SECONDS], so that a
    /// gate left closed does not hang the test; an interrupted task ends at once, counted as ended. Each test opens
    /// its gates in a `finally`.
    final class Gate {
        private final CountDownLatch open = new CountDownLatch(1);
        private final Semaphore startPermits = new Semaphore(0);
        private final AtomicInteger started = new AtomicInteger();
        private final AtomicInteger ended = new AtomicInteger();
        private final Set<Integer> startedIds = ConcurrentHashMap.newKeySet();

        /// Makes a closed gate.
        public Gate() {
            // Closed until open() is called.
        }

        /// Returns a task that waits at the gate.
        ///
        /// @return the task
        public Runnable task() {
            return this::pass;
        }

        /// Returns a task that waits at the gate, and records, by `id`, that it started.
        ///
        /// @param id the task's id
        /// @return the task
        public Runnable task(final int id) {
            return () -> {
                startedIds.add(id);
                pass();
            };
        }

        /// Returns whether the task of an id ([#task(int)]) has started.
        ///
        /// @param id the task's id
        /// @return `true` if it started
        public boolean hasStarted(final int id) {
            return startedIds.contains(id);
        }

        /// Returns a task that waits at the gate and then returns a value.
        ///
        /// @param value the value
        /// @param <T>   its type
        /// @return the task
        public <T> Callable<T> callable(final T value) {
            return () -> {
                pass();
                return value;
            };
        }

        /// Opens the gate: every task waiting at it goes on, and every later one passes at once.
        public void open() {
            open.countDown();
        }

        /// Returns the number of tasks that have started.
        ///
        /// @return the number
        public int started() {
            return started.get();
        }

        /// Returns the number of tasks that have ended.
        ///
        /// @return the number
        public int ended() {
            return ended.get();
        }

        /// Waits until at least `count` tasks have started, for at most [#BLOCKING_TIMEOUT_SECONDS].
        ///
        /// @param count the number of tasks
        /// @throws InterruptedException if interrupted while waiting
        /// @throws AssertionFailedError if fewer started in time
        public void awaitStarted(final int count) throws InterruptedException {
            if (!startPermits.tryAcquire(count, BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new AssertionFailedError(started.get() + " of " + count + " tasks started within "
                        + BLOCKING_TIMEOUT_SECONDS + " s");
            }
            startPermits.release(count);
        }

        private void pass() {
            started.incrementAndGet();
            startPermits.release();
            try {
                boolean ignored = open.await(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                ended.incrementAndGet();
            }
        }
    }

    /// Whether the class under test throws [java.util.concurrent.RejectedExecutionException] for a task submitted
    /// after shutdown. The Javadoc of `shutdown` says "no new tasks will be accepted", and that of `execute` and
    /// `submit` that they throw it "if this task cannot be scheduled for execution", so it is tested unless a class
    /// that discards such tasks instead (a `ThreadPoolExecutor` with a `DiscardPolicy`) overrides this to return
    /// `false`; the tests then check only that the task never runs.
    ///
    /// @return `true` unless overridden
    default boolean rejectsTasksAfterShutdown() {
        return true;
    }

    /// Returns a new executor from the provider, checked: a null one fails the test with a message that says the
    /// provider is at fault, and one already shut down with a message that says the provider or `isShutdown()` is.
    ///
    /// @return the executor
    default E newExecutor() {
        E executor = executorServiceProvider().newExecutor();
        if (executor == null) {
            throw new AssertionFailedError("the executor provider is at fault: it gave a null executor");
        }
        if (executor.isShutdown()) {
            executor.shutdownNow();
            throw new AssertionFailedError("the executor provider gave one already shut down, or isShutdown() is "
                    + "wrong for a new one");
        }
        return executor;
    }

    /// Runs a test on a new executor ([#newExecutor()]), then shuts it down with `shutdownNow()` and waits, bounded,
    /// for it to terminate, whatever the test did. The shutdown is not checked: a test that cares checks it itself.
    ///
    /// @param test the test
    /// @throws Exception what the test throws
    default void using(final ExecutorCall<? super E> test) throws Exception {
        E executor = newExecutor();
        try {
            test.run(executor);
        } finally {
            dispose(executor);
        }
    }

    /// Shuts an executor down with `shutdownNow()` and waits for it to terminate, on a daemon thread joined for at most
    /// [#DISPOSE_TIMEOUT_MILLIS]; never throws, and ignores what either call does wrong, so that cleanup never
    /// replaces the test's own failure, and a test's bounded call plus its cleanup stays within the time a contract
    /// run gives a test method.
    ///
    /// @param executor the executor
    default void dispose(final ExecutorService executor) {
        Thread thread = new Thread(() -> {
            try {
                executor.shutdownNow();
                boolean ignored = executor.awaitTermination(DISPOSE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (final RuntimeException | Error e) {
                // Cleanup only: the test has passed or failed already.
            }
        }, "executor-service-contract-dispose");
        thread.setDaemon(true);
        thread.start();
        try {
            thread.join(DISPOSE_TIMEOUT_MILLIS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /// Asserts that an executor, already shut down, terminates: `awaitTermination` with a timeout of
    /// [#BLOCKING_TIMEOUT_SECONDS] returns `true`.
    ///
    /// @param executor the executor
    /// @param when     when it is checked, for the failure message
    /// @throws Exception what `awaitTermination` throws
    default void assertTerminates(final ExecutorService executor, final String when) throws Exception {
        Boolean terminated = bounded(() -> executor.awaitTermination(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertTrue(terminated, () -> "awaitTermination(" + BLOCKING_TIMEOUT_SECONDS + ", SECONDS) " + when);
    }

    /// Asserts that the cause of an [ExecutionException] is the exception a task threw: that exception itself, or,
    /// as `ForkJoinTask` documents ("to provide accurate stack traces, ... a new exception of the same type"), a copy
    /// of the same class whose cause is that exception.
    ///
    /// @param expected the exception the task threw
    /// @param thrown   the exception `get`, `invokeAny` or the like threw
    /// @param what     what threw it, for the failure message
    // ReferenceEquality: the cause must be the task's exception itself, or a copy whose cause is it, by identity.
    @SuppressWarnings("ReferenceEquality")
    default void assertCauseIs(final Throwable expected, final ExecutionException thrown, final String what) {
        Throwable cause = thrown.getCause();
        boolean same = cause == expected
                || (cause != null && cause.getClass() == expected.getClass() && cause.getCause() == expected);
        assertTrue(same, () -> "the cause of " + what + "'s ExecutionException is " + cause + ", not the task's "
                + expected + " or a copy of it");
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
        Thread thread = new Thread(task, "executor-service-contract");
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

    /// Makes a call that may block, as [#bounded(Callable)] does, with the thread's interrupt status set, and clears
    /// the status afterwards. The status is set on the thread that makes the call.
    ///
    /// @param call the call
    /// @throws Exception what the call throws, [InterruptedException] for a call that waits
    default void boundedInterrupted(final BlockingCall call) throws Exception {
        bounded(() -> {
            Thread.currentThread().interrupt();
            try {
                call.run();
            } finally {
                boolean ignored = Thread.interrupted();
            }
            return null;
        });
    }

    /// Asserts that one call waits until another thread's call releases it: `waiting` must not return before
    /// `release` has been called.
    ///
    /// Two threads of a [ContentionCoordinator] run the calls, once each, and it bounds them in time. The releasing
    /// thread waits (with [Thread#onSpinWait], at most [#SPIN_LIMIT_MILLIS]) until the other is waiting, then calls
    /// `release`. The result does not depend on timing: if the releasing thread stops waiting early, a correct
    /// executor still passes.
    ///
    /// @param waiting the call that should wait, with its own checks of what it gets
    /// @param release the call that should release it
    default void assertWaitsFor(final BlockingCall waiting, final Runnable release) {
        AtomicReference<@Nullable Thread> waiter = new AtomicReference<>();
        AtomicBoolean released = new AtomicBoolean();
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(BLOCKING_TIMEOUT_SECONDS,
                BLOCKING_TIMEOUT_SECONDS, ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
        coordinator.add(1, 1, unchecked(() -> {
            waiter.set(Thread.currentThread());
            waiting.run();
            assertTrue(released.get(), "the call returned before another thread released it");
        }));
        coordinator.add(1, 1, o -> {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SPIN_LIMIT_MILLIS);
            while (!isWaiting(waiter.get()) && System.nanoTime() - deadline < 0) {
                Thread.onSpinWait();
            }
            released.set(true);
            release.run();
        });
        coordinator.execute(new Object());
    }

    /// Turns a call that throws checked exceptions into a [Consumer] for a [ContentionCoordinator], which ignores its
    /// subject; a checked exception becomes an [AssertionError], and an [InterruptedException] sets the interrupt
    /// status again.
    ///
    /// @param call the call
    /// @param <S>  the type of the coordinator's subject
    /// @return the consumer
    default <S> Consumer<S> unchecked(final BlockingCall call) {
        return subject -> {
            try {
                call.run();
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

    /// Returns whether a thread is waiting: in the state [Thread.State#WAITING] or [Thread.State#TIMED_WAITING].
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
}
