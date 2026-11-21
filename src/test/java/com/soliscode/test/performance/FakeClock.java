/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

/// A [PacingClock] the test moves: parking moves it by the time asked, spinning by a microsecond, and the test by
/// [#advance(long)]. It starts at an arbitrary time, so that nothing depends on its origin.
final class FakeClock implements PacingClock {

    /// How far one spin moves the clock.
    static final long SPIN_STEP = 1_000;

    private long now = 7_000_000_000L;
    private long parked;

    @Override
    public long nanoTime() {
        return now;
    }

    @Override
    public void park(final long nanos) {
        now += nanos;
        parked += nanos;
    }

    @Override
    public void spin() {
        now += SPIN_STEP;
    }

    /// Moves the clock on, as an operation that takes that long does.
    void advance(final long nanos) {
        now += nanos;
    }

    /// Returns the time parked in all.
    long parked() {
        return parked;
    }
}
