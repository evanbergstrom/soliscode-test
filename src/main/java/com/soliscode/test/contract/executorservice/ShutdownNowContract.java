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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [shutdownNow()][ExecutorService#shutdownNow()] method of an [ExecutorService].
///
/// It follows the Javadoc: `shutdownNow` "attempts to stop all actively executing tasks, halts the processing of
/// waiting tasks, and returns a list of the tasks that were awaiting execution." It reads that loosely: a task it
/// returns has not run and does not run, and each task that had not started is either returned or cancelled, as
/// `ForkJoinPool` documents that its `shutdownNow` cancels its waiting tasks and "always returns an empty list". That
/// it stops the tasks running is not tested: the Javadoc calls it "best-effort". The new tasks it rejects are read as
/// [ShutdownContract] reads `shutdown`'s.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#shutdownNow()
public interface ShutdownNowContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that after `shutdownNow`, `execute` and each `submit` throw [RejectedExecutionException], no task
    /// submitted after it runs, and the executor terminates.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("shutdownNow() rejects new tasks, and the executor terminates")
    default void shutdownNowRejectsNewTasksAndTerminates() throws Exception {
        using(executor -> {
            executor.shutdownNow();
            AtomicInteger runs = new AtomicInteger();
            Callable<Integer> callable = runs::incrementAndGet;
            Runnable runnable = runs::incrementAndGet;
            if (rejectsTasksAfterShutdown()) {
                assertThrows(RejectedExecutionException.class, () -> executor.execute(runnable),
                        "execute after shutdownNow()");
                assertThrows(RejectedExecutionException.class, () -> executor.submit(callable),
                        "submit(Callable) after shutdownNow()");
                assertThrows(RejectedExecutionException.class, () -> executor.submit(runnable),
                        "submit(Runnable) after shutdownNow()");
                assertThrows(RejectedExecutionException.class, () -> executor.submit(runnable, "result"),
                        "submit(Runnable, Object) after shutdownNow()");
            } else {
                try {
                    executor.execute(runnable);
                } catch (final RejectedExecutionException e) {
                    // A class that discards may also reject: either keeps the task from running.
                }
            }
            assertTerminates(executor, "after shutdownNow()");
            assertEquals(0, runs.get(), "the tasks submitted after shutdownNow() that ran");
        });
    }

    /// Tests that `shutdownNow` returns or cancels the tasks that had not started, and that none it returns runs.
    /// [#HELD_TASKS] tasks, each marking that it started, wait at a gate; after `shutdownNow`, the gate opens and the
    /// executor terminates. Then the tasks started and the tasks returned are at most [#HELD_TASKS], and with the
    /// tasks cancelled and not started, at least [#HELD_TASKS].
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("shutdownNow() returns or cancels the tasks not started, and none it returns runs")
    default void shutdownNowReturnsOrCancelsTheTasksNotStarted() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            List<Future<?>> futures = new ArrayList<>();
            List<Runnable> returned;
            try {
                for (int i = 0; i < HELD_TASKS; i++) {
                    futures.add(executor.submit(gate.task(i)));
                }
                returned = executor.shutdownNow();
            } finally {
                gate.open();
            }
            assertTerminates(executor, "after shutdownNow()");
            int started = 0;
            int cancelled = 0;
            for (int i = 0; i < HELD_TASKS; i++) {
                if (gate.hasStarted(i)) {
                    started++;
                } else if (futures.get(i).isCancelled()) {
                    cancelled++;
                }
            }
            String counts = started + " started, " + returned.size() + " returned by shutdownNow() and "
                    + cancelled + " cancelled and not started, of " + HELD_TASKS;
            assertTrue(started + returned.size() <= HELD_TASKS, () -> counts + ": a task it returned ran");
            assertTrue(started + returned.size() + cancelled >= HELD_TASKS,
                    () -> counts + ": a task that had not started was neither returned nor cancelled");
        });
    }
}
