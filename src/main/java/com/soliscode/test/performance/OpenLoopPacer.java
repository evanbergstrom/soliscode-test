/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.time.Duration;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/// Sends an operation at a fixed rate on the caller's thread, and times each from its scheduled send.
///
/// Operation *i*, counted from 0, is scheduled at `start + i × (1 s / rate)`, waited for (parking until 50 µs
/// before it, then spinning), and timed from that scheduled time to its return. An operation that starts late,
/// because the one before it was slow, counts the wait in its latency: the load is open-loop, and the latencies
/// are free of coordinated omission. Every scheduled operation is run, so a slow operation makes the run longer
/// rather than dropping sends.
///
/// The operations scheduled in the first `warmup` are run and not measured; those scheduled in the `duration`
/// after it are measured. The achieved rate is the measured operations over the time from the measured window's
/// scheduled start to its scheduled end or the last measured return, whichever is later.
///
/// ## Usage Examples
/// ```java
/// PacedRun run = OpenLoopPacer.at(10_000)
///     .warmup(Duration.ofSeconds(5))
///     .duration(Duration.ofSeconds(30))
///     .run(sequence -> client.send(sequence));
/// return new ScenarioResult(run.metrics(), List.of(), List.of());
/// ```
///
/// ## Thread Safety
/// Immutable. [#run(PacedOperation)] runs on the calling thread, and may be called from several at once.
///
/// @since 0.1.6
public final class OpenLoopPacer {

    /// How long before a scheduled send the pacer stops parking and spins.
    static final long SPIN_NANOS = 50_000;

    private static final double NANOS_PER_SECOND = 1e9;
    private static final Duration DEFAULT_DURATION = Duration.ofSeconds(10);
    private static final Duration HIGHEST_LATENCY_MARGIN = Duration.ofMinutes(1);
    private static final int SIGNIFICANT_DIGITS = 3;

    private final double rate;
    private final Duration warmup;
    private final Duration duration;
    private final @Nullable Duration highestLatency;
    private final PacingClock clock;

    private OpenLoopPacer(final double rateArg, final Duration warmupArg, final Duration durationArg,
        final @Nullable Duration highestLatencyArg, final PacingClock clockArg) {
        this.rate = rateArg;
        this.warmup = warmupArg;
        this.duration = durationArg;
        this.highestLatency = highestLatencyArg;
        this.clock = clockArg;
    }

    /// Returns a pacer at a rate, with no warmup, a duration of 10 s, and a highest latency of the warmup and the
    /// duration together and a minute.
    ///
    /// @param operationsPerSecond the rate, above 0 and at most 10⁹, one send a nanosecond
    /// @return the pacer
    /// @throws IllegalArgumentException if the rate is not above 0 and at most 10⁹
    public static OpenLoopPacer at(final double operationsPerSecond) {
        if (!(operationsPerSecond > 0.0 && operationsPerSecond <= NANOS_PER_SECOND)) {
            throw new IllegalArgumentException("the rate " + operationsPerSecond + " is not above 0 and at most "
                + "1e9 operations a second, one a nanosecond");
        }
        return new OpenLoopPacer(operationsPerSecond, Duration.ZERO, DEFAULT_DURATION, null, PacingClock.SYSTEM);
    }

    /// Returns this pacer with a warmup: the operations scheduled in it are run and not measured.
    ///
    /// @param warmupDuration the warmup, not negative
    /// @return the new pacer
    /// @throws IllegalArgumentException if the warmup is negative
    public OpenLoopPacer warmup(final Duration warmupDuration) {
        Objects.requireNonNull(warmupDuration, "warmupDuration");
        if (warmupDuration.isNegative()) {
            throw new IllegalArgumentException("the warmup " + warmupDuration + " is negative");
        }
        return new OpenLoopPacer(rate, warmupDuration, duration, highestLatency, clock);
    }

    /// Returns this pacer with a measured duration.
    ///
    /// @param measuredDuration the duration, above 0, with at least 2 operations scheduled in it
    /// @return the new pacer
    /// @throws IllegalArgumentException if the duration is not above 0, or has fewer than 2 operations in it
    public OpenLoopPacer duration(final Duration measuredDuration) {
        Objects.requireNonNull(measuredDuration, "measuredDuration");
        if (measuredDuration.isNegative() || measuredDuration.isZero()) {
            throw new IllegalArgumentException("the duration " + measuredDuration + " is not above 0");
        }
        return new OpenLoopPacer(rate, warmup, measuredDuration, highestLatency, clock).checked();
    }

    /// Returns this pacer with the highest latency its recorder takes; a latency above it fails the run.
    ///
    /// @param highest the highest latency, at least 2 ns
    /// @return the new pacer
    /// @throws IllegalArgumentException if it is below 2 ns
    public OpenLoopPacer highestLatency(final Duration highest) {
        Objects.requireNonNull(highest, "highest");
        if (highest.compareTo(Duration.ofNanos(2)) < 0) {
            throw new IllegalArgumentException("the highest latency " + highest + " is below 2 ns");
        }
        return new OpenLoopPacer(rate, warmup, duration, highest, clock);
    }

    /// Returns the operations a run measures.
    ///
    /// @return the operations scheduled in the measured duration
    public long measuredOperations() {
        return operationsBefore(warmup.plus(duration)) - operationsBefore(warmup);
    }

    /// Runs the operations, on the calling thread, and returns what was measured.
    ///
    /// @param operation the operation
    /// @return the run's latencies, its measured operations and its achieved rate
    /// @throws IllegalArgumentException if fewer than 2 operations are scheduled in the measured duration
    /// @throws IllegalStateException if a measured latency is above the highest
    /// @throws Exception if the operation throws, which ends the run
    public PacedRun run(final PacedOperation operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        checked();
        LatencyRecorder latencies = new LatencyRecorder(highest().toNanos(), SIGNIFICANT_DIGITS);
        long firstMeasured = operationsBefore(warmup);
        long total = operationsBefore(warmup.plus(duration));
        long start = clock.nanoTime();
        long lastReturn = start;
        for (long sequence = 0; sequence < total; sequence++) {
            long scheduled = start + offset(sequence);
            clock.waitUntil(scheduled, SPIN_NANOS);
            operation.run(sequence);
            lastReturn = clock.nanoTime();
            if (sequence >= firstMeasured) {
                record(latencies, sequence, lastReturn - scheduled);
            }
        }
        long windowStart = start + warmup.toNanos();
        long window = Math.max(lastReturn, windowStart + duration.toNanos()) - windowStart;
        return new PacedRun(latencies, total - firstMeasured, (total - firstMeasured) * NANOS_PER_SECOND / window);
    }

    /// Returns this pacer on another clock, for the tests.
    OpenLoopPacer on(final PacingClock other) {
        return new OpenLoopPacer(rate, warmup, duration, highestLatency, Objects.requireNonNull(other, "other"));
    }

    private OpenLoopPacer checked() {
        if (measuredOperations() < 2) {
            throw new IllegalArgumentException("a rate of " + rate + " ops/s schedules " + measuredOperations()
                + " operations in " + duration + ", fewer than 2");
        }
        return this;
    }

    private Duration highest() {
        return highestLatency != null ? highestLatency : warmup.plus(duration).plus(HIGHEST_LATENCY_MARGIN);
    }

    private static void record(final LatencyRecorder latencies, final long sequence, final long latency) {
        if (!latencies.accepts(latency)) {
            throw new IllegalStateException("operation " + sequence + " took " + latency + " ns from its scheduled "
                + "send, above the highest latency of " + latencies.highestTrackable() + " ns");
        }
        latencies.record(latency);
    }

    /// The operations scheduled before a time from the start: those whose rounded offset, the one they are sent at,
    /// is below it.
    private long operationsBefore(final Duration time) {
        long nanos = time.toNanos();
        long count = (long) Math.ceil(nanos * rate / NANOS_PER_SECOND);
        while (count > 0 && offset(count - 1) >= nanos) {
            count--;
        }
        while (offset(count) < nanos) {
            count++;
        }
        return count;
    }

    private long offset(final long sequence) {
        return Math.round(sequence * NANOS_PER_SECOND / rate);
    }
}
