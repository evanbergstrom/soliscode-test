/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.util.concurrent.locks.LockSupport;

/// The time a pacer or a soak reads and waits on: the system's, or, in the tests, a clock the test moves.
interface PacingClock {

    /// The system's clock: `System.nanoTime`, `LockSupport.parkNanos` and `Thread.onSpinWait`.
    PacingClock SYSTEM = new PacingClock() {
        @Override
        public long nanoTime() {
            return System.nanoTime();
        }

        @Override
        public void park(final long nanos) {
            LockSupport.parkNanos(nanos);
        }

        @Override
        public void spin() {
            Thread.onSpinWait();
        }
    };

    /// Returns the time, in nanoseconds from an arbitrary origin.
    ///
    /// @return the time
    long nanoTime();

    /// Waits about `nanos`, perhaps less.
    ///
    /// @param nanos the time to wait
    void park(long nanos);

    /// Waits a moment, without giving up the processor.
    void spin();

    /// Waits until the time is `deadline` or later: parks until `spinNanos` before it, then spins.
    ///
    /// @param deadline the time to wait until
    /// @param spinNanos how long before the deadline to stop parking and spin
    /// @throws InterruptedException if the thread is interrupted while it waits, which would otherwise turn the
    ///     wait into a spin, since a park returns at once on an interrupted thread
    default void waitUntil(final long deadline, final long spinNanos) throws InterruptedException {
        long remaining = deadline - nanoTime();
        while (remaining > 0) {
            if (Thread.interrupted()) {
                throw new InterruptedException("interrupted while waiting for the next scheduled time");
            }
            if (remaining > spinNanos) {
                park(remaining - spinNanos);
            } else {
                spin();
            }
            remaining = deadline - nanoTime();
        }
    }
}
