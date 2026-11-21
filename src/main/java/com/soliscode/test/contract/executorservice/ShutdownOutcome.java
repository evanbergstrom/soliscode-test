/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

/// What became of a task's submission in a round of [ThreadSafeShutdownContract], as recorded in the round's
/// outcomes.
final class ShutdownOutcome {
    /// Not submitted.
    static final int NONE = 0;
    /// Accepted after the shutdowner set its flag, or while it was setting it.
    static final int ACCEPTED = 1;
    /// Accepted, with `execute` returned before the shutdowner set its flag.
    static final int ACCEPTED_BEFORE_SHUTDOWN = 2;
    /// Rejected.
    static final int REJECTED = 3;

    private ShutdownOutcome() { }
}
