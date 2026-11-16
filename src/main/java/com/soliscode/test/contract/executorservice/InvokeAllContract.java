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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;
import com.soliscode.test.provider.DeliberateException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [invokeAll(Collection)][ExecutorService#invokeAll(Collection)] method of an [ExecutorService].
///
/// It follows the Javadoc: it "executes the given tasks, returning a list of Futures holding their status and
/// results when all complete. `Future.isDone` is `true` for each element of the returned list", "in the same
/// sequential order as produced by the iterator for the given task list"; it throws [InterruptedException] "if
/// interrupted while waiting" and [NullPointerException] "if tasks or any of its elements are null". A failed task's
/// cause is read as [SubmitCallableContract] reads it. An interrupt status already set while a task is held is read
/// as being interrupted while waiting. That the tasks not done are cancelled once interrupted is not tested, nor the
/// results when the collection changes during the call, which the Javadoc leaves undefined.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#invokeAll(Collection)
public interface InvokeAllContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that `invokeAll` returns one future per task, in order, each done, with each task's result, or an
    /// [ExecutionException] for the task that threw; and that it returns an empty list for no tasks.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAll gives the tasks' outcomes, in order")
    default void invokeAllGivesTheOutcomesInOrder() throws Exception {
        using(executor -> {
            Exception failure = new DeliberateException("invokeAllGivesTheOutcomesInOrder");
            List<Callable<Integer>> tasks = List.of(() -> 1, () -> {
                throw failure;
            }, () -> 3);
            List<Future<Integer>> futures = bounded(() -> executor.invokeAll(tasks));
            assertEquals(3, futures.size(), "the futures invokeAll returned for 3 tasks");
            for (int i = 0; i < futures.size(); i++) {
                int index = i;
                assertTrue(futures.get(i).isDone(), () -> "isDone() of future " + index + " invokeAll returned");
            }
            assertEquals(1, futures.get(0).get(), "the result of the first task");
            ExecutionException thrown = assertThrows(ExecutionException.class, futures.get(1)::get,
                    "get() of the second task, which threw");
            assertCauseIs(failure, thrown, "the second future's get()");
            assertEquals(3, futures.get(2).get(), "the result of the third task");
            assertEquals(List.of(), bounded(() -> executor.invokeAll(List.<Callable<Integer>>of())),
                    "invokeAll of no tasks");
        });
    }

    /// Tests that `invokeAll` waits until another thread lets the held tasks end, and then returns their futures,
    /// done, with their results.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAll waits until every task has completed")
    default void invokeAllWaitsForTheTasks() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                List<Callable<Integer>> tasks = List.of(gate.callable(1), gate.callable(2));
                assertWaitsFor(() -> {
                    List<Future<Integer>> futures = executor.invokeAll(tasks);
                    assertTrue(futures.get(0).isDone() && futures.get(1).isDone(),
                            "isDone() of each future invokeAll returned");
                    assertEquals(1, futures.get(0).get(), "the result of the first task");
                    assertEquals(2, futures.get(1).get(), "the result of the second task");
                }, gate::open);
            } finally {
                gate.open();
            }
        });
    }

    /// Tests that `invokeAll` throws [NullPointerException] for a collection with a null task, and for a null
    /// collection.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAll with a null task throws NullPointerException")
    // NullAway: passes a null task and a null collection on purpose, to test that invokeAll throws.
    @SuppressWarnings("NullAway")
    default void invokeAllWithANullTaskThrowsNullPointerException() throws Exception {
        using(executor -> {
            Callable<Integer> task = () -> 1;
            Collection<Callable<Integer>> withNull = Arrays.asList(task, null);
            assertThrows(NullPointerException.class, () -> bounded(() -> executor.invokeAll(withNull)),
                    "invokeAll with a null task");
            assertThrows(NullPointerException.class, () -> bounded(() -> executor.invokeAll(null)),
                    "invokeAll(null)");
        });
    }

    /// Tests that `invokeAll` throws [InterruptedException] when the thread is interrupted while a task is held.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAll throws InterruptedException when interrupted")
    default void invokeAllThrowsInterruptedExceptionWhenInterrupted() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                List<Callable<Integer>> tasks = List.of(gate.callable(1));
                assertThrows(InterruptedException.class, () -> boundedInterrupted(() -> executor.invokeAll(tasks)),
                        "invokeAll with the thread interrupted while a task is held");
            } finally {
                gate.open();
            }
        });
    }
}
