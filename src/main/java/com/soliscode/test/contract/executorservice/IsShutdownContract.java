/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [isShutdown()][ExecutorService#isShutdown()] method of an [ExecutorService].
///
/// It follows the Javadoc: it "returns `true` if this executor has been shut down", by `shutdown` or
/// `shutdownNow`.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#isShutdown()
public interface IsShutdownContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that `isShutdown()` is `false` before `shutdown`, idle and while a task runs.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("isShutdown() is false before shutdown")
    default void isShutdownIsFalseBeforeShutdown() throws Exception {
        using(executor -> {
            assertFalse(executor.isShutdown(), "isShutdown() of a new executor");
            Gate gate = new Gate();
            try {
                executor.execute(gate.task());
                gate.awaitStarted(1);
                assertFalse(executor.isShutdown(), "isShutdown() while a task runs");
            } finally {
                gate.open();
            }
        });
    }

    /// Tests that `isShutdown()` is `true` after `shutdown`, and once terminated.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("isShutdown() is true after shutdown()")
    default void isShutdownIsTrueAfterShutdown() throws Exception {
        using(executor -> {
            executor.shutdown();
            assertTrue(executor.isShutdown(), "isShutdown() after shutdown()");
            assertTerminates(executor, "after shutdown()");
            assertTrue(executor.isShutdown(), "isShutdown() once terminated after shutdown()");
        });
    }

    /// Tests that `isShutdown()` is `true` after `shutdownNow`, and once terminated.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("isShutdown() is true after shutdownNow()")
    default void isShutdownIsTrueAfterShutdownNow() throws Exception {
        using(executor -> {
            executor.shutdownNow();
            assertTrue(executor.isShutdown(), "isShutdown() after shutdownNow()");
            assertTerminates(executor, "after shutdownNow()");
            assertTrue(executor.isShutdown(), "isShutdown() once terminated after shutdownNow()");
        });
    }
}
