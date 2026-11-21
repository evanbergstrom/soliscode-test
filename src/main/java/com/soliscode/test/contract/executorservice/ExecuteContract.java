/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [execute(Runnable)][java.util.concurrent.Executor#execute(Runnable)] method of an
/// [ExecutorService].
///
/// It follows the Javadoc of `Executor.execute`: it "executes the given command at some time in the future", and
/// throws [NullPointerException] "if command is null". It reads "executes" as once: the command has run
/// exactly once by the time the executor has terminated. `Executor`'s memory-consistency effects ("actions in a
/// thread prior to submitting a `Runnable` object to an `Executor` happen-before its execution begins") are tested
/// under contention by [ThreadSafeSubmitContract].
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see java.util.concurrent.Executor#execute(Runnable)
public interface ExecuteContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that `execute` runs the command, once: after `shutdown`, once the executor has terminated, it has run
    /// exactly once.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("execute(Runnable) runs the command once")
    default void executeRunsTheCommandOnce() throws Exception {
        using(executor -> {
            AtomicInteger runs = new AtomicInteger();
            executor.execute(runs::incrementAndGet);
            executor.shutdown();
            assertTerminates(executor, "after shutdown()");
            assertEquals(1, runs.get(), "the times the command ran");
        });
    }

    /// Tests that `execute(null)` throws [NullPointerException].
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("execute(null) throws NullPointerException")
    // NullAway: passes null to execute on purpose, to test that it throws.
    @SuppressWarnings("NullAway")
    default void executeOfNullThrowsNullPointerException() throws Exception {
        using(executor -> assertThrows(NullPointerException.class, () -> executor.execute(null),
                "execute(null)"));
    }
}
