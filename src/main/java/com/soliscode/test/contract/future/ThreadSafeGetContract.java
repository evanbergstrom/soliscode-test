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
package com.soliscode.test.contract.future;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.support.FutureContractSupport;
import com.soliscode.test.provider.FutureSource;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertNull;

/// **Contract for `get` of a `Future` under contention**
///
/// [Future]'s Javadoc: "Actions taken by the asynchronous computation happen-before actions following the
/// corresponding `Future.get()` in another thread." And each `get` must see a completion that another thread makes
/// while it waits.
///
/// Over [#ROUNDS] pending futures, [#GETTERS] getters each call `get` on round 0, 1, 2 and so on, having first
/// published that they are on it. One completer goes through the rounds in order: it spins until a getter has
/// published a round, writes a plain mark for it, and succeeds its future, so that completions land while getters are
/// arriving and waiting. Each getter checks the value and the mark. No break shows the check of the mark failing, and
/// on most hardware none could: it is there for the class under test.
///
/// The run is bounded in time by a deadline of [#DEADLINE_MILLIS], counted from when the completer starts. If no
/// getter reaches a round by then, or the getters are not finished by then after the last round, the completer fails
/// the run, since the getters left are blocked in `get` on a completed future, and interrupts them. Every loop checks
/// for a failure and for an interrupt each round, and a getter that fails the run stops the completer, which then
/// interrupts the others. The [ContentionCoordinator] that runs it is given a start timeout of
/// [#START_TIMEOUT_SECONDS] and a shutdown timeout of [#SHUTDOWN_TIMEOUT_SECONDS], which it may wait twice: at most
/// 9 s, under the 10 s that [ConcurrencyTest] gives the test. A future whose `get` ignores an interrupt fails the run,
/// and leaves its getter behind as a daemon thread.
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @see Future#get()
/// @see Future#get(long, TimeUnit)
/// @since 0.1.4
public interface ThreadSafeGetContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// The number of futures ({@value}) each run goes through.
    int ROUNDS = 1_000;

    /// The number of threads ({@value}) that call `get`.
    int GETTERS = 4;

    /// How long ({@value} ms) the run is given, from when the completer starts.
    long DEADLINE_MILLIS = 3_000;

    /// How long ({@value} s) the coordinator waits for its workers to start.
    int START_TIMEOUT_SECONDS = 1;

    /// How long ({@value} s) the coordinator waits for its workers to finish, and again for them to end once it
    /// interrupts them: longer than [#DEADLINE_MILLIS], so that the run's own deadline ends it first.
    int SHUTDOWN_TIMEOUT_SECONDS = 4;

    /// The timeout ({@value} s) of the timed `get` under contention: longer than the run, so that the run's deadline,
    /// not the timeout, is what a missed completion meets.
    long TIMED_GET_TIMEOUT_SECONDS = 30;

    /// A `get` of a future, timed or not.
    ///
    /// @param <F> the type of the future
    /// @param <V> the type of its result
    @FunctionalInterface
    interface Get<F, V> {
        /// Calls `get` on the future.
        ///
        /// @param future the future
        /// @return its result
        /// @throws Exception what `get` throws
        @Nullable V get(F future) throws Exception;
    }

    /// Tests that threads blocked in `get()` see each completion that another thread makes, with its value and what
    /// the completing thread wrote before it.
    @DisplayName("get() sees completions made by another thread under contention")
    @VerySlow
    @ConcurrencyTest
    default void getWhenCompletedUnderContentionIsThreadSafe() {
        assertGettersSeeCompletions(Future::get, "get()");
    }

    /// Tests that threads blocked in the timed `get` see each completion that another thread makes, with its value
    /// and what the completing thread wrote before it.
    @DisplayName("get(timeout, unit) sees completions made by another thread under contention")
    @VerySlow
    @ConcurrencyTest
    default void timedGetWhenCompletedUnderContentionIsThreadSafe() {
        assertGettersSeeCompletions(f -> f.get(TIMED_GET_TIMEOUT_SECONDS, TimeUnit.SECONDS), "get(timeout, unit)");
    }

    /// Runs the getters and the completer over [#ROUNDS] pending futures, and fails the test if a getter got the
    /// wrong value or mark, threw, or did not see a completion by the deadline.
    ///
    /// The completer yields while it waits for a getter to reach a round, so that on a single core it does not keep
    /// the getter from running until its time slice ends.
    ///
    /// @param get  the `get` under test
    /// @param name its name, for the failure message
    default void assertGettersSeeCompletions(final Get<F, V> get, final String name) {
        List<FutureSource<F, V>> rounds = new ArrayList<>(ROUNDS);
        for (int r = 0; r < ROUNDS; r++) {
            rounds.add(pending());
        }
        AtomicIntegerArray arrived = new AtomicIntegerArray(ROUNDS);
        int[] marks = new int[ROUNDS];
        AtomicReference<@Nullable String> failure = new AtomicReference<>();
        Set<Thread> getters = ConcurrentHashMap.newKeySet();
        CountDownLatch gettersDone = new CountDownLatch(GETTERS);
        ContentionCoordinator<List<FutureSource<F, V>>> coordinator = new ContentionCoordinator<>(
                START_TIMEOUT_SECONDS, SHUTDOWN_TIMEOUT_SECONDS, ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
        coordinator.add(GETTERS, 1, futures -> {
            getters.add(Thread.currentThread());
            try {
                for (int r = 0; r < ROUNDS; r++) {
                    if (failure.get() != null || Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    arrived.incrementAndGet(r);
                    @Nullable String wrong = getOnce(get, name, futures.get(r), r, marks);
                    if (wrong != null) {
                        failure.compareAndSet(null, wrong);
                        return;
                    }
                }
            } finally {
                gettersDone.countDown();
            }
        });
        coordinator.add(1, 1, futures -> {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(DEADLINE_MILLIS);
            for (int r = 0; r < ROUNDS; r++) {
                if (!awaitArrival(arrived, r, deadline, failure, name)) {
                    getters.forEach(Thread::interrupt);
                    return;
                }
                marks[r] = r + 1;
                futures.get(r).succeed();
            }
            try {
                if (!gettersDone.await(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS)) {
                    failure.compareAndSet(null, gettersDone.getCount() + " of " + GETTERS + " getters were still "
                            + "blocked in " + name + " on a completed future " + DEADLINE_MILLIS + " ms after the "
                            + "start");
                    getters.forEach(Thread::interrupt);
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                getters.forEach(Thread::interrupt);
            }
        });
        coordinator.execute(rounds);
        assertNull(failure.get(), failure::get);
    }

    /// One getter's call of `get` on a round's future.
    ///
    /// @param get    the `get` under test
    /// @param name   its name, for the failure message
    /// @param source the round's future and the value it completes with
    /// @param r      the round
    /// @param marks  the completer's writes, one per round, made before it completes the round's future
    /// @return what the call got wrong, or `null` if it returned the value after the completer's write
    private @Nullable String getOnce(final Get<F, V> get, final String name, final FutureSource<F, V> source,
            final int r, final int[] marks) {
        @Nullable V got;
        try {
            got = get.get(source.future());
        } catch (final InterruptedException e) {
            // The completer records a failure before it interrupts a getter: an interrupt with none recorded came
            // from the future, not from this run.
            return name + " of round " + r + " threw " + e + ", though nothing interrupted its thread";
        } catch (final Exception e) {
            return name + " of round " + r + " threw " + e;
        }
        if (!Objects.equals(source.value(), got)) {
            return name + " of round " + r + " returned " + got + ", not " + source.value();
        }
        if (marks[r] != r + 1) {
            return name + " of round " + r + " returned before the completing thread's earlier write was visible: "
                    + "the completion does not happen-before it";
        }
        return null;
    }

    /// The completer's wait for a getter to reach a round, yielding meanwhile.
    ///
    /// @param arrived  the getters that have reached each round
    /// @param r        the round
    /// @param deadline the [System#nanoTime()] by which a getter must reach it
    /// @param failure  the run's first failure, which the wait records if no getter reaches the round in time
    /// @param name     the name of the `get` under test, for the failure message
    /// @return whether a getter reached it, rather than the run failing first
    // ThreadPriorityCheck: the completer's yield lets the getter it waits for run, on purpose.
    @SuppressWarnings("ThreadPriorityCheck")
    private boolean awaitArrival(final AtomicIntegerArray arrived, final int r, final long deadline,
            final AtomicReference<@Nullable String> failure, final String name) {
        while (arrived.get(r) == 0) {
            if (failure.get() != null) {
                return false;
            }
            if (System.nanoTime() - deadline >= 0) {
                failure.compareAndSet(null, "no getter reached round " + r + " within " + DEADLINE_MILLIS
                        + " ms: the others are blocked in " + name + " on a completed future");
                return false;
            }
            Thread.yield();
        }
        return true;
    }
}
