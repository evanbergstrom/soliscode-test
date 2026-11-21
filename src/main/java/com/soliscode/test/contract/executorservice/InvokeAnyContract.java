/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;
import com.soliscode.test.provider.DeliberateException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [invokeAny(Collection)][ExecutorService#invokeAny(Collection)] method of an [ExecutorService].
///
/// It follows the Javadoc: it "executes the given tasks, returning the result of one that has completed
/// successfully (i.e., without throwing an exception), if any do", and throws [ExecutionException] "if no task
/// successfully completes", [IllegalArgumentException] "if tasks is empty", [NullPointerException] "if tasks or any
/// element task subject to execution is null", and [InterruptedException] "if interrupted while waiting". The null
/// task is put first, since an executor may return before it reaches a later one ("subject to execution"). That the
/// tasks not done are cancelled on return is not tested: it cannot be seen from outside without depending on
/// interruption.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#invokeAny(Collection)
public interface InvokeAnyContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that `invokeAny` returns the result of the task that succeeded, though another failed.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAny gives the result of a task that succeeded")
    default void invokeAnyGivesTheResultOfATaskThatSucceeded() throws Exception {
        using(executor -> {
            Exception failure = new DeliberateException("invokeAnyGivesTheResultOfATaskThatSucceeded");
            List<Callable<Integer>> tasks = List.of(() -> {
                throw failure;
            }, () -> 7);
            assertEquals(7, bounded(() -> executor.invokeAny(tasks)), "invokeAny of a failing and a succeeding task");
        });
    }

    /// Tests that `invokeAny` throws [ExecutionException] when every task fails.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAny throws ExecutionException when every task fails")
    default void invokeAnyThrowsExecutionExceptionWhenEveryTaskFails() throws Exception {
        using(executor -> {
            List<Callable<Integer>> tasks = List.of(() -> {
                throw new DeliberateException("the first task");
            }, () -> {
                throw new DeliberateException("the second task");
            });
            assertThrows(ExecutionException.class, () -> bounded(() -> executor.invokeAny(tasks)),
                    "invokeAny of tasks that all fail");
        });
    }

    /// Tests that `invokeAny` of no tasks throws [IllegalArgumentException].
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAny of no tasks throws IllegalArgumentException")
    default void invokeAnyOfNoTasksThrowsIllegalArgumentException() throws Exception {
        using(executor -> assertThrows(IllegalArgumentException.class,
                () -> bounded(() -> executor.invokeAny(List.<Callable<Integer>>of())), "invokeAny of no tasks"));
    }

    /// Tests that `invokeAny` throws [NullPointerException] for a collection whose first task is null, and for a
    /// null collection.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAny with a null task throws NullPointerException")
    // NullAway: passes a null task and a null collection on purpose, to test that invokeAny throws.
    @SuppressWarnings("NullAway")
    default void invokeAnyWithANullTaskThrowsNullPointerException() throws Exception {
        using(executor -> {
            Callable<Integer> task = () -> 1;
            Collection<Callable<Integer>> withNull = Arrays.asList(null, task);
            assertThrows(NullPointerException.class, () -> bounded(() -> executor.invokeAny(withNull)),
                    "invokeAny with a null first task");
            assertThrows(NullPointerException.class, () -> bounded(() -> executor.invokeAny(null)),
                    "invokeAny(null)");
        });
    }

    /// Tests that `invokeAny` throws [InterruptedException] when the thread is interrupted while a task is held.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAny throws InterruptedException when interrupted")
    default void invokeAnyThrowsInterruptedExceptionWhenInterrupted() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                List<Callable<Integer>> tasks = List.of(gate.callable(1));
                assertThrows(InterruptedException.class, () -> boundedInterrupted(() -> executor.invokeAny(tasks)),
                        "invokeAny with the thread interrupted while a task is held");
            } finally {
                gate.open();
            }
        });
    }
}
