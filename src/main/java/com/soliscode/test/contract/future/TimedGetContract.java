/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.future;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.FutureContractSupport;
import com.soliscode.test.provider.FutureSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [get(long, TimeUnit)][Future#get(long, TimeUnit)] method of a [Future].
///
/// It follows the Javadoc of [Future#get(long, TimeUnit)]: it "waits if necessary for at most the given time for
/// the computation to complete, and then retrieves its result", and throws [TimeoutException] "if the wait timed
/// out", and [CancellationException], [ExecutionException] and [InterruptedException] as `get()` does. It reads the
/// Javadoc more strictly in two places:
/// - A zero timeout on a done future gives its outcome at once: there is no need to wait.
/// - A pending future with the interrupt status already set throws [InterruptedException], as
///   [com.soliscode.test.contract.blockingqueue.TakeContract] reads the timed `poll`.
///
/// A null `TimeUnit` is not tested: the Javadoc does not say what it does.
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future#get(long, TimeUnit)
public interface TimedGetContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// Tests that the timed `get`, with a zero timeout, gives the outcome of a done future: its value once
    /// succeeded, [ExecutionException] with the failure as its cause once failed, and [CancellationException] once
    /// cancelled.
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("get(0, unit) gives the outcome of a done future")
    default void timedGetWithZeroTimeoutGivesTheOutcomeOnceDone() throws Exception {
        FutureSource<F, V> succeeded = succeeded();
        assertEquals(succeeded.value(), get(succeeded.future(), 0, TimeUnit.MILLISECONDS),
                "get(0, MILLISECONDS) once succeeded");
        FutureSource<F, V> failed = failed();
        ExecutionException thrown = assertThrows(ExecutionException.class,
                () -> get(failed.future(), 0, TimeUnit.MILLISECONDS), "get(0, MILLISECONDS) once failed");
        assertSame(failed.exception(), thrown.getCause(), "the cause of get(0, MILLISECONDS)'s ExecutionException");
        F cancelled = cancelled().future();
        assertThrows(CancellationException.class, () -> get(cancelled, 0, TimeUnit.MILLISECONDS),
                "get(0, MILLISECONDS) once cancelled");
    }

    /// Tests that the timed `get` of a pending future throws [TimeoutException] when its timeout passes, and leaves
    /// the future pending.
    @Test
    @DisplayName("get(timeout, unit) of a pending future times out and leaves it pending")
    default void timedGetTimesOutWhilePending() {
        F future = pending().future();
        assertThrows(TimeoutException.class, () -> get(future, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS),
                () -> "get(" + TIMED_WAIT_MILLIS + ", MILLISECONDS) of a pending future");
        assertFalse(future.isDone(), "isDone() after a timed get() timed out");
    }

    /// Tests that the timed `get` of a pending future waits, within its timeout, until another thread succeeds it.
    @Test
    @DisplayName("get(timeout, unit) waits until another thread succeeds the future")
    default void timedGetWaitsForCompletion() {
        FutureSource<F, V> source = pending();
        assertWaitsFor(source.future(), () -> assertEquals(source.value(),
                source.future().get(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "get(timeout, unit) after another thread succeeded the future"), source::succeed);
    }

    /// Tests that the timed `get` of a pending future throws [InterruptedException] when the thread is
    /// interrupted. Its timeout is short, so that a `get` that ignores the interrupt ends long before the bound.
    @Test
    @DisplayName("get(timeout, unit) throws InterruptedException when interrupted while pending")
    default void timedGetThrowsInterruptedExceptionWhenInterrupted() {
        F future = pending().future();
        assertThrows(InterruptedException.class,
                () -> boundedInterrupted(() -> future.get(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)),
                "get(timeout, unit) of a pending future with the thread interrupted");
    }
}
