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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [resultNow()][Future#resultNow()] method of a [Future].
///
/// It follows the Javadoc: it "returns the computed result, without waiting", and throws [IllegalStateException]
/// "if the task has not completed or the task did not complete with a result".
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future#resultNow()
public interface ResultNowContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// Tests that `resultNow()` returns the value once succeeded.
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("resultNow() returns the result once succeeded")
    default void resultNowReturnsTheResultOnceSucceeded() throws Exception {
        FutureSource<F, V> source = succeeded();
        assertEquals(source.value(), resultNow(source.future()), "resultNow() once succeeded");
    }

    /// Tests that `resultNow()` throws [IllegalStateException] while pending, once failed and once cancelled.
    @Test
    @DisplayName("resultNow() throws IllegalStateException unless succeeded")
    default void resultNowThrowsUnlessSucceeded() {
        F pending = pending().future();
        assertThrows(IllegalStateException.class, () -> resultNow(pending), "resultNow() while pending");
        F failed = failed().future();
        assertThrows(IllegalStateException.class, () -> resultNow(failed), "resultNow() once failed");
        F cancelled = cancelled().future();
        assertThrows(IllegalStateException.class, () -> resultNow(cancelled), "resultNow() once cancelled");
    }
}
