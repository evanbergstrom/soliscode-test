/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.support.ExecutorServiceContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for `execute` and `submit` of an `ExecutorService` under contention**
///
/// [ExecutorService]'s Javadoc: "Actions in a thread prior to the submission of a `Runnable` or `Callable` task to
/// an `ExecutorService` happen-before any actions taken by that task, which in turn happen-before the result is
/// retrieved via `Future.get()`." And each task submitted, from whichever thread, runs once.
///
/// [#SUBMITTERS] threads each submit [#TASKS_PER_SUBMITTER] tasks at once, alternating `execute` and
/// `submit(Callable)`. Before submitting a task, its submitter writes a plain mark for it; the task checks the mark,
/// counts its run, and writes a plain result. Once the executor has terminated after `shutdown`, every task has run
/// exactly once, and for each submitted one, `get` returns its value and its plain result is seen after `get`.
///
/// The run is bounded by [#DEADLINE_MILLIS] and the coordinator's timeouts ([#START_TIMEOUT_SECONDS],
/// [#SHUTDOWN_TIMEOUT_SECONDS]); every count is checked before any `get`, and each `get` is timed by the time left,
/// so a lost task fails the test rather than hanging it.
///
/// @param <E> the type of the executor under test
/// @see ExecutorService
/// @since 0.1.5
public interface ThreadSafeSubmitContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// The number of threads ({@value}) that submit at once.
    int SUBMITTERS = 4;

    /// The number of tasks ({@value}) each submitter submits.
    int TASKS_PER_SUBMITTER = 2_000;

    /// How long ({@value} ms) the executor is given to terminate, and the futures to give their results, once the
    /// submitters are done.
    long DEADLINE_MILLIS = 3_000;

    /// How long ({@value} s) the coordinator waits for its workers to start.
    int START_TIMEOUT_SECONDS = 1;

    /// How long ({@value} s) the coordinator waits for its workers to finish.
    int SHUTDOWN_TIMEOUT_SECONDS = 4;

    /// Tests that tasks submitted by several threads at once each run once, and see what their submitter wrote
    /// before submitting them; and that what a task wrote is seen after `get`.
    ///
    /// @throws Exception if a call on the executor throws
    @DisplayName("execute and submit called by several threads at once run each task once")
    @VerySlow
    @ConcurrencyTest
    default void submitWhenCalledUnderContentionRunsEachTaskOnce() throws Exception {
        int total = SUBMITTERS * TASKS_PER_SUBMITTER;
        int[] marks = new int[total];
        int[] results = new int[total];
        AtomicIntegerArray runs = new AtomicIntegerArray(total);
        AtomicReferenceArray<@Nullable Future<Integer>> futures = new AtomicReferenceArray<>(total);
        AtomicReference<@Nullable String> failure = new AtomicReference<>();
        AtomicInteger nextSubmitter = new AtomicInteger();
        using(executor -> {
            ContentionCoordinator<E> coordinator = new ContentionCoordinator<>(START_TIMEOUT_SECONDS,
                    SHUTDOWN_TIMEOUT_SECONDS, ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
            coordinator.add(SUBMITTERS, 1, subject -> {
                int base = nextSubmitter.getAndIncrement() * TASKS_PER_SUBMITTER;
                for (int k = 0; k < TASKS_PER_SUBMITTER; k++) {
                    int id = base + k;
                    marks[id] = id + 1;
                    Runnable body = () -> {
                        if (marks[id] != id + 1) {
                            failure.compareAndSet(null, "task " + id + " did not see what its submitter wrote "
                                    + "before submitting it");
                        }
                        runs.incrementAndGet(id);
                        results[id] = id + 1;
                    };
                    if (k % 2 == 0) {
                        subject.execute(body);
                    } else {
                        futures.set(id, subject.submit(() -> {
                            body.run();
                            return id;
                        }));
                    }
                }
            });
            coordinator.execute(executor);
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(DEADLINE_MILLIS);
            executor.shutdown();
            assertTrue(executor.awaitTermination(deadline - System.nanoTime(), TimeUnit.NANOSECONDS),
                    () -> "the executor did not terminate within " + DEADLINE_MILLIS + " ms of shutdown()");
            assertNull(failure.get(), failure::get);
            for (int id = 0; id < total; id++) {
                int task = id;
                assertEquals(1, runs.get(id), () -> "the times task " + task + " of " + total + " ran");
            }
            for (int id = 0; id < total; id++) {
                Future<Integer> future = futures.get(id);
                if (future == null) {
                    continue;
                }
                int task = id;
                assertEquals(id, future.get(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS),
                        () -> "get() of task " + task);
                assertEquals(id + 1, results[id], () -> "what task " + task + " wrote, read after get()");
            }
        });
    }
}
