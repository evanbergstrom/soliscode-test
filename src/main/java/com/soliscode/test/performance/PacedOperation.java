/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

/// An operation an [OpenLoopPacer] sends at its rate.
///
/// @since 0.1.6
@FunctionalInterface
public interface PacedOperation {

    /// Runs the operation once; the pacer times it from its scheduled send to its return.
    ///
    /// @param sequence the operation's number, counted from 0 with the warmup's, which a [StageTaps] can use as
    ///     its id
    /// @throws Exception if it fails, which ends the run
    void run(long sequence) throws Exception;
}
