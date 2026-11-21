/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [invokeAny(Collection, long, TimeUnit)][ExecutorService#invokeAny(Collection, long, TimeUnit)]
/// method of an [ExecutorService].
///
/// It follows the Javadoc: it "executes the given tasks, returning the result of one that has completed
/// successfully (i.e., without throwing an exception), if any do before the given timeout elapses", and throws
/// [TimeoutException] "if the given timeout elapses before any task successfully completes". The failures, the
/// empty and null collections and the interrupt are as [InvokeAnyContract] tests them for
/// `invokeAny(Collection)`, and are not tested again; nor is a null `TimeUnit`.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#invokeAny(Collection, long, TimeUnit)
public interface TimedInvokeAnyContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that the timed `invokeAny` waits, within its timeout, until another thread lets the held task end, and
    /// then returns its result.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAny with a timeout waits for a task that succeeds within it")
    default void timedInvokeAnyWaitsForATask() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                List<Callable<Integer>> tasks = List.of(gate.callable(5));
                assertWaitsFor(() -> assertEquals(5, executor.invokeAny(tasks, BLOCKING_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS), "the timed invokeAny after another thread let the task end"), gate::open);
            } finally {
                gate.open();
            }
        });
    }

    /// Tests that the timed `invokeAny` throws [TimeoutException] when no task completes within its timeout.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("invokeAny with a timeout throws TimeoutException when it expires")
    default void timedInvokeAnyTimesOut() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                List<Callable<Integer>> tasks = List.of(gate.callable(5));
                assertThrows(TimeoutException.class, () -> bounded(() -> executor.invokeAny(tasks,
                        TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)), "invokeAny(tasks, " + TIMED_WAIT_MILLIS
                        + ", MILLISECONDS) with the task held");
            } finally {
                gate.open();
            }
        });
    }
}
