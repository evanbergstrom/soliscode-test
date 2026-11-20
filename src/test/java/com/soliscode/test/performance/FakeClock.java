/*
 * Copyright 2026 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
