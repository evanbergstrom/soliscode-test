/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [awaitTermination(long, TimeUnit)][ExecutorService#awaitTermination(long, TimeUnit)] method of
/// an [ExecutorService].
///
/// It follows the Javadoc: it "blocks until all tasks have completed execution after a shutdown request, or the
/// timeout occurs, or the current thread is interrupted, whichever happens first", returns `true` "if this executor
/// terminated and `false` if the timeout elapsed before termination", and throws [InterruptedException] "if
/// interrupted while waiting". It reads it strictly in two places: a timeout of zero once terminated returns `true`,
/// since nothing is left to wait for; and with the interrupt status already set while not terminated, it throws, as
/// [com.soliscode.test.contract.blockingqueue.TakeContract] reads `take`.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#awaitTermination(long, TimeUnit)
public interface AwaitTerminationContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that `awaitTermination` returns `false` once a short timeout elapses after `shutdown` while a task runs.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("awaitTermination returns false when it times out while a task runs")
    default void awaitTerminationTimesOutWhileTasksRun() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                executor.execute(gate.task());
                gate.awaitStarted(1);
                executor.shutdown();
                assertFalse(bounded(() -> executor.awaitTermination(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)),
                        "awaitTermination(" + TIMED_WAIT_MILLIS + ", MILLISECONDS) while a task runs");
            } finally {
                gate.open();
            }
            assertTerminates(executor, "after shutdown()");
        });
    }

    /// Tests that `awaitTermination` with a timeout of zero returns `true` once the executor has terminated.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("awaitTermination with a zero timeout returns true once terminated")
    default void awaitTerminationWithZeroTimeoutIsTrueOnceTerminated() throws Exception {
        using(executor -> {
            executor.shutdown();
            assertTerminates(executor, "after shutdown()");
            assertTrue(bounded(() -> executor.awaitTermination(0, TimeUnit.MILLISECONDS)),
                    "awaitTermination(0, MILLISECONDS) once terminated");
        });
    }

    /// Tests that `awaitTermination` waits after `shutdown` until another thread lets the running task end, and then
    /// returns `true`.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("awaitTermination waits until the tasks have completed")
    default void awaitTerminationWaitsForTheTasks() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                executor.execute(gate.task());
                gate.awaitStarted(1);
                executor.shutdown();
                assertWaitsFor(() -> assertTrue(executor.awaitTermination(BLOCKING_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS), "awaitTermination after another thread let the task end"), gate::open);
            } finally {
                gate.open();
            }
        });
    }

    /// Tests that `awaitTermination` throws [InterruptedException] when the thread is interrupted while the executor
    /// has not terminated.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("awaitTermination throws InterruptedException when interrupted")
    default void awaitTerminationThrowsInterruptedExceptionWhenInterrupted() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                executor.execute(gate.task());
                gate.awaitStarted(1);
                executor.shutdown();
                assertThrows(InterruptedException.class, () -> boundedInterrupted(
                        () -> executor.awaitTermination(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS)),
                        "awaitTermination with the thread interrupted while a task runs");
            } finally {
                gate.open();
            }
        });
    }
}
