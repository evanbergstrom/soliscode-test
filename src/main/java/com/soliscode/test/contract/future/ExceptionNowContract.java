/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.future;

import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.FutureContractSupport;
import com.soliscode.test.provider.FutureSource;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [exceptionNow()][Future#exceptionNow()] method of a [Future].
///
/// It follows the Javadoc: it "returns the exception thrown by the task, without waiting", and throws
/// [IllegalStateException] "if the task has not completed, the task completed normally, or the task was cancelled".
/// The exception returned must be the one the future failed with, the same object.
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future#exceptionNow()
public interface ExceptionNowContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// Tests that `exceptionNow()` returns the exception the future failed with, once failed.
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("exceptionNow() returns the failure once failed")
    default void exceptionNowReturnsTheExceptionOnceFailed() throws Exception {
        FutureSource<F, V> source = failed();
        assertSame(source.exception(), exceptionNow(source.future()), "exceptionNow() once failed");
    }

    /// Tests that `exceptionNow()` throws [IllegalStateException] while pending, once succeeded and once cancelled.
    @Test
    @DisplayName("exceptionNow() throws IllegalStateException unless failed")
    default void exceptionNowThrowsUnlessFailed() {
        F pending = pending().future();
        assertThrows(IllegalStateException.class, () -> exceptionNow(pending), "exceptionNow() while pending");
        F succeeded = succeeded().future();
        assertThrows(IllegalStateException.class, () -> exceptionNow(succeeded), "exceptionNow() once succeeded");
        F cancelled = cancelled().future();
        assertThrows(IllegalStateException.class, () -> exceptionNow(cancelled), "exceptionNow() once cancelled");
    }
}
