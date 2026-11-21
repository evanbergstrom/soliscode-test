/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.future;

import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.FutureContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [isCancelled()][Future#isCancelled()] method of a [Future].
///
/// It follows the Javadoc: `true` "if this task was cancelled before it completed normally".
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future#isCancelled()
public interface IsCancelledContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// Tests that `isCancelled()` is `false` while pending, once succeeded and once failed.
    @Test
    @DisplayName("isCancelled() is false while pending, once succeeded and once failed")
    default void isCancelledIsFalseUnlessCancelled() {
        assertFalse(pending().future().isCancelled(), "isCancelled() while pending");
        assertFalse(succeeded().future().isCancelled(), "isCancelled() once succeeded");
        assertFalse(failed().future().isCancelled(), "isCancelled() once failed");
    }

    /// Tests that `isCancelled()` is `true` once cancelled.
    @Test
    @DisplayName("isCancelled() is true once cancelled")
    default void isCancelledIsTrueOnceCancelled() {
        assertTrue(cancelled().future().isCancelled(), "isCancelled() once cancelled");
    }
}
