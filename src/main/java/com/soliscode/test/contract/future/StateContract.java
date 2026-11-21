/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.future;

import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.FutureContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [state()][Future#state()] method of a [Future].
///
/// It follows the Javadoc of [Future.State]: `RUNNING` "the task has not completed", `SUCCESS` "completed with a
/// result", `FAILED` "completed with an exception", and `CANCELLED` "the task was cancelled".
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future#state()
public interface StateContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// Tests that `state()` is `RUNNING` while pending.
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("state() is RUNNING while pending")
    default void stateIsRunningWhilePending() throws Exception {
        assertEquals(Future.State.RUNNING, state(pending().future()), "state() while pending");
    }

    /// Tests that `state()` is `SUCCESS`, `FAILED` or `CANCELLED` once the future has succeeded, failed or been
    /// cancelled.
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("state() is SUCCESS, FAILED or CANCELLED once done")
    default void stateIsTheOutcomeOnceDone() throws Exception {
        assertEquals(Future.State.SUCCESS, state(succeeded().future()), "state() once succeeded");
        assertEquals(Future.State.FAILED, state(failed().future()), "state() once failed");
        assertEquals(Future.State.CANCELLED, state(cancelled().future()), "state() once cancelled");
    }
}
