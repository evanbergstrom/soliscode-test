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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [invokeAll(Collection, long, TimeUnit)][ExecutorService#invokeAll(Collection, long, TimeUnit)]
/// method of an [ExecutorService].
///
/// It follows the Javadoc: it "executes the given tasks, returning a list of Futures holding their status and
/// results when all complete or the timeout expires, whichever happens first. `Future.isDone` is `true` for each
/// element of the returned list. Upon return, tasks that have not completed are cancelled." It reads it loosely in
/// one place: a quick task may come back cancelled once the timeout has expired, since `ThreadPoolExecutor` stops
/// submitting at the deadline and `ForkJoinPool` cancels what it has not joined. The order, the null tasks and the
/// interrupt are as [InvokeAllContract] tests them for `invokeAll(Collection)`, and are not tested again; nor is a
/// null `TimeUnit`.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#invokeAll(Collection, long, TimeUnit)
public interface TimedInvokeAllContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that the timed `invokeAll` waits, within its timeout, until another thread lets the held tasks end,
    /// and then returns their futures in order, done and not cancelled, with their results.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAll with a timeout waits for the tasks that complete within it")
    default void timedInvokeAllWaitsForTheTasks() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                List<Callable<Integer>> tasks = List.of(gate.callable(1), gate.callable(2));
                assertWaitsFor(() -> {
                    List<Future<Integer>> futures = executor.invokeAll(tasks, BLOCKING_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS);
                    for (final Future<Integer> future : futures) {
                        assertTrue(future.isDone(), "isDone() of each future the timed invokeAll returned");
                        assertFalse(future.isCancelled(), "isCancelled() of a task that completed in time");
                    }
                    assertEquals(1, futures.get(0).get(), "the result of the first task");
                    assertEquals(2, futures.get(1).get(), "the result of the second task");
                }, gate::open);
            } finally {
                gate.open();
            }
        });
    }

    /// Tests that the timed `invokeAll` cancels, on return, a task that did not complete within its timeout: every
    /// future is done, the held task's is cancelled, and the quick task's either has its result or is cancelled.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAll with a timeout cancels the tasks not done when it expires")
    default void timedInvokeAllCancelsTheTasksNotDone() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                List<Callable<Integer>> tasks = List.of(() -> 1, gate.callable(2));
                List<Future<Integer>> futures = bounded(() -> executor.invokeAll(tasks, TIMED_WAIT_MILLIS,
                        TimeUnit.MILLISECONDS));
                assertEquals(2, futures.size(), "the futures the timed invokeAll returned for 2 tasks");
                assertTrue(futures.get(0).isDone(), "isDone() of the quick task's future");
                assertTrue(futures.get(1).isDone(), "isDone() of the held task's future");
                assertTrue(futures.get(1).isCancelled(), "isCancelled() of the held task's future");
                assertTrue(futures.get(0).isCancelled() || futures.get(0).get() == 1,
                        "the quick task's future has its result, or is cancelled");
            } finally {
                gate.open();
            }
        });
    }
}
