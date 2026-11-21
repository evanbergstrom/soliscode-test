/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [submit(Runnable)][ExecutorService#submit(Runnable)] method of an [ExecutorService].
///
/// It follows the Javadoc: it returns "a Future representing pending completion of the task", whose `get` "will
/// return `null` upon successful completion", and throws [NullPointerException] "if the task is null". It reads
/// "completion of the task" as the task having run by the time `get` returns.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#submit(Runnable)
public interface SubmitRunnableContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that `submit(Runnable)` runs the runnable, and that its future's `get` returns `null` once it has.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("submit(Runnable) runs the runnable and gives null")
    default void submitRunnableRunsItAndGivesNull() throws Exception {
        using(executor -> {
            AtomicBoolean ran = new AtomicBoolean();
            Future<?> future = executor.submit(() -> ran.set(true));
            assertNull(bounded(future::get), "get() of the future submit(Runnable) returned");
            assertTrue(ran.get(), "the runnable ran by the time get() returned");
        });
    }

    /// Tests that `submit((Runnable) null)` throws [NullPointerException].
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("submit((Runnable) null) throws NullPointerException")
    // NullAway: passes null to submit on purpose, to test that it throws.
    @SuppressWarnings("NullAway")
    default void submitRunnableOfNullThrowsNullPointerException() throws Exception {
        using(executor -> assertThrows(NullPointerException.class, () -> executor.submit((Runnable) null),
                "submit((Runnable) null)"));
    }
}
