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
package com.soliscode.test.contract.executorservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.support.ExecutorServiceContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/// **Contract for `shutdown` of an `ExecutorService` under contention with `execute`**
///
/// [ExecutorService#shutdown()]'s Javadoc: "previously submitted tasks are executed, but no new tasks will be
/// accepted". When other threads submit while one shuts the executor down, each submission is either rejected or
/// accepted, and an accepted task runs, once.
///
/// Over [#ROUNDS] new executors, [#SUBMITTERS] submitters and a shutdowner go through the rounds in order, meeting
/// at a barrier before each. Each submitter calls `execute` with tasks that count their runs, at most
/// [#MAX_SUBMISSIONS] times, until it is rejected; the shutdowner waits until [#ACCEPTED_BEFORE_SHUTDOWN] have been
/// accepted, sets a flag, and calls `shutdown`. Once each round's executor has terminated, every accepted task has
/// run exactly once, and no rejected one has run.
///
/// Where [#rejectsTasksAfterShutdown()] is `false`, a submission after `shutdown` may be discarded without being
/// rejected; each submitter stops once it sees `isShutdown()`, and the test checks only that no task ran more than
/// once, and that each task whose `execute` returned before the shutdowner set its flag has run.
///
/// The run is bounded by [#DEADLINE_MILLIS] and the coordinator's timeouts: each wait at the barrier is timed, and a
/// broken or timed-out barrier, or the deadline, fails the run and stops every thread; each round's executor is
/// disposed of at the end, whatever happened.
///
/// A `ThreadPoolExecutor` whose core threads are not yet started can fail this test, rightly, through a race of its
/// own (probably JDK-8186445; seen on JDK 25.0.5, in about 1 fresh JVM of 10 that ran the test 5 times, #54): an
/// `execute` that has reserved a core slot, and is making its thread, gives the slot up and rejects its own task if
/// `shutdown` comes first, while the tasks other threads queued meanwhile, accepted before `shutdown`, are left with no
/// worker, and the executor never terminates. Starting its core threads first
/// ([java.util.concurrent.ThreadPoolExecutor#prestartAllCoreThreads()]) avoids it.
///
/// @param <E> the type of the executor under test
/// @see ExecutorService#shutdown()
/// @since 0.1.5
public interface ThreadSafeShutdownContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// The number of rounds ({@value}), each on a new executor.
    int ROUNDS = 200;

    /// The number of threads ({@value}) that submit while another shuts the executor down.
    int SUBMITTERS = 3;

    /// The most submissions ({@value}) each submitter makes in a round.
    int MAX_SUBMISSIONS = 200;

    /// The number of tasks ({@value}) accepted in a round before the shutdowner calls `shutdown`.
    int ACCEPTED_BEFORE_SHUTDOWN = 50;

    /// How long ({@value} ms) the run is given, from when the threads start.
    long DEADLINE_MILLIS = 3_000;

    /// The longest ({@value} ms) a thread waits at the barrier for the others.
    long BARRIER_TIMEOUT_MILLIS = 1_000;

    /// Tests that when threads submit while another shuts the executor down, every accepted task runs once, and no
    /// rejected one runs.
    @DisplayName("shutdown() called while other threads submit runs each accepted task once")
    @VerySlow
    @ConcurrencyTest
    default void shutdownWhenCalledUnderContentionRunsEachAcceptedTask() {
        int perRound = SUBMITTERS * MAX_SUBMISSIONS;
        List<E> executors = new ArrayList<>(ROUNDS);
        List<AtomicIntegerArray> runs = new ArrayList<>(ROUNDS);
        List<AtomicIntegerArray> outcomes = new ArrayList<>(ROUNDS);
        AtomicIntegerArray accepted = new AtomicIntegerArray(ROUNDS);
        AtomicIntegerArray shutdownCalled = new AtomicIntegerArray(ROUNDS);
        AtomicReference<@Nullable String> failure = new AtomicReference<>();
        AtomicInteger nextSubmitter = new AtomicInteger();
        boolean rejects = rejectsTasksAfterShutdown();
        try {
            for (int r = 0; r < ROUNDS; r++) {
                executors.add(newExecutor());
                runs.add(new AtomicIntegerArray(perRound));
                outcomes.add(new AtomicIntegerArray(perRound));
            }
            CyclicBarrier barrier = new CyclicBarrier(SUBMITTERS + 1);
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(DEADLINE_MILLIS);
            ContentionCoordinator<List<E>> coordinator = new ContentionCoordinator<>(
                    ThreadSafeSubmitContract.START_TIMEOUT_SECONDS, ThreadSafeSubmitContract.SHUTDOWN_TIMEOUT_SECONDS,
                    ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
            coordinator.add(SUBMITTERS, 1, list -> {
                int base = nextSubmitter.getAndIncrement() * MAX_SUBMISSIONS;
                for (int r = 0; r < ROUNDS; r++) {
                    if (!meet(barrier, deadline, failure, r)) {
                        return;
                    }
                    submitRound(list.get(r), base, runs.get(r), outcomes.get(r), accepted, shutdownCalled, r,
                            rejects);
                }
            });
            coordinator.add(1, 1, list -> {
                for (int r = 0; r < ROUNDS; r++) {
                    if (!meet(barrier, deadline, failure, r)) {
                        return;
                    }
                    while (accepted.get(r) < ACCEPTED_BEFORE_SHUTDOWN && System.nanoTime() - deadline < 0) {
                        Thread.onSpinWait();
                    }
                    shutdownCalled.set(r, 1);
                    list.get(r).shutdown();
                }
            });
            coordinator.execute(executors);
            assertNull(failure.get(), failure::get);
            for (int r = 0; r < ROUNDS; r++) {
                checkRound(executors.get(r), r, runs.get(r), outcomes.get(r), rejects, deadline);
            }
        } finally {
            for (final E executor : executors) {
                dispose(executor);
            }
        }
    }

    /// One submitter's tasks in one round: it submits until one is rejected, or, for a class that does not reject
    /// tasks after shutdown, until the executor is shut down, and records each task's outcome.
    ///
    /// @param executor       the round's executor
    /// @param base           the submitter's first task id
    /// @param runs           the times each task of the round ran
    /// @param outcomes       each task's [ShutdownOutcome]
    /// @param accepted       the tasks accepted in each round
    /// @param shutdownCalled whether `shutdown` has been called in each round, 1 once it has
    /// @param round          the round
    /// @param rejects        whether the class rejects tasks after shutdown ([#rejectsTasksAfterShutdown()])
    private static void submitRound(final ExecutorService executor, final int base, final AtomicIntegerArray runs,
            final AtomicIntegerArray outcomes, final AtomicIntegerArray accepted,
            final AtomicIntegerArray shutdownCalled, final int round, final boolean rejects) {
        for (int k = 0; k < MAX_SUBMISSIONS; k++) {
            int id = base + k;
            try {
                executor.execute(() -> runs.incrementAndGet(id));
            } catch (final RejectedExecutionException e) {
                outcomes.set(id, ShutdownOutcome.REJECTED);
                return;
            }
            outcomes.set(id, shutdownCalled.get(round) == 0 ? ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN
                    : ShutdownOutcome.ACCEPTED);
            accepted.incrementAndGet(round);
            if (!rejects && executor.isShutdown()) {
                return;
            }
        }
    }

    /// Checks one round: its executor terminates within the time left, and each task ran as its outcome says.
    ///
    /// @param executor the round's executor
    /// @param round    the round
    /// @param runs     the times each task ran
    /// @param outcomes each task's [ShutdownOutcome]
    /// @param rejects  whether the class rejects tasks after shutdown ([#rejectsTasksAfterShutdown()])
    /// @param deadline the run's deadline, in [System#nanoTime()]'s terms
    private static void checkRound(final ExecutorService executor, final int round, final AtomicIntegerArray runs,
            final AtomicIntegerArray outcomes, final boolean rejects, final long deadline) {
        try {
            if (!executor.awaitTermination(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS)) {
                fail("the executor of round " + round + " did not terminate within " + DEADLINE_MILLIS + " ms");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("interrupted while waiting for round " + round + " to terminate", e);
        }
        for (int id = 0; id < runs.length(); id++) {
            int ran = runs.get(id);
            int outcome = outcomes.get(id);
            boolean mustRun = outcome == ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN
                    || (rejects && outcome == ShutdownOutcome.ACCEPTED);
            if (ran > 1) {
                fail("task " + id + " of round " + round + " ran " + ran + " times");
            }
            if (mustRun && ran == 0) {
                fail("task " + id + " of round " + round + " was accepted"
                        + (outcome == ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN ? " before shutdown() was called" : "")
                        + " and never ran");
            }
            boolean accepted = outcome == ShutdownOutcome.ACCEPTED
                    || outcome == ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN;
            if (!accepted && ran != 0) {
                fail("task " + id + " of round " + round + " was rejected, or never submitted, and ran");
            }
        }
    }

    /// Waits at the barrier for the others, bounded by the deadline; records a failure and returns `false` if the
    /// barrier breaks, times out or the deadline passes, or another thread has recorded one.
    ///
    /// @param barrier  the barrier
    /// @param deadline the run's deadline, in [System#nanoTime()]'s terms
    /// @param failure  the first failure
    /// @param round    the round, for the message
    /// @return `true` if every thread met
    private static boolean meet(final CyclicBarrier barrier, final long deadline,
            final AtomicReference<@Nullable String> failure, final int round) {
        long left = deadline - System.nanoTime();
        if (failure.get() != null || left <= 0) {
            failure.compareAndSet(null, "the threads did not finish within " + DEADLINE_MILLIS + " ms");
            barrier.reset();
            return false;
        }
        try {
            barrier.await(Math.min(left, TimeUnit.MILLISECONDS.toNanos(BARRIER_TIMEOUT_MILLIS)),
                    TimeUnit.NANOSECONDS);
            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (final BrokenBarrierException | TimeoutException e) {
            failure.compareAndSet(null, "the threads did not meet at round " + round + ": " + e);
            return false;
        }
    }
}
