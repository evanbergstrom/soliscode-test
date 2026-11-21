/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.soliscode.performance.MetricKind;
import com.soliscode.performance.ScenarioBound;
import com.soliscode.performance.ScenarioMetric;
import com.soliscode.performance.ScenarioResult;

/// Samples a process's live heap at an interval over a long run, and fits its slope.
///
/// Each sample is one `jcmd` attach (`<java.home>/bin/jcmd <pid> -f`), which runs `GC.run`, so that what is left
/// is live, and then `GC.heap_info`, whose heap lines' `used` it sums; each `jcmd` has a time limit, 30 s by
/// default. The samples are taken at the start and at each interval after it, up to the duration; a sample that
/// ends after the next one's time makes the next start at once. The slope is the least-squares slope of the bytes
/// against the time, in MB (2²⁰ bytes) per minute.
///
/// What is read is the occupancy just after `GC.run`: what the process allocates between the collection's end and
/// the heap's reading, within the one attach, is counted with it. So the slope is an estimate of the live heap's,
/// valid for a workload at a steady load, which is what a soak runs, and where that gap is about the same in every
/// sample. A steady allocation rate adds about the same bytes to every sample, which the slope does not see; an
/// allocation rate that rises or falls during the soak, or a gap that widens, moves it.
///
/// ## Usage Examples
/// ```java
/// HeapSoak.Result soak = HeapSoak.of(topology.process("server").pid()).run(Duration.ofMinutes(30));
/// return soak.scenarioResult(0.5); // heapSlope, bounded below 0.5 MB/min
/// ```
///
/// ## Thread Safety
/// Immutable. [#run(Duration)] runs on the calling thread.
///
/// @since 0.1.6
public final class HeapSoak {

    /// The metric's name.
    public static final String METRIC = "heapSlope";

    /// The metric's unit.
    public static final String UNIT = "MB/min";

    private static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(60);
    private static final Duration DEFAULT_JCMD_LIMIT = Duration.ofSeconds(30);
    private static final long SPIN_NANOS = 1_000_000;
    private static final double BYTES_PER_MB = 1 << 20;
    private static final double NANOS_PER_MINUTE = 60e9;
    private static final int MINIMUM_INTERVALS = 2;

    private final long pid;
    private final Duration interval;
    private final Duration jcmdLimit;
    private final PacingClock clock;
    private final HeapReader reader;

    private HeapSoak(final long pidArg, final Duration intervalArg, final Duration jcmdLimitArg,
        final PacingClock clockArg, final HeapReader readerArg) {
        this.pid = pidArg;
        this.interval = intervalArg;
        this.jcmdLimit = jcmdLimitArg;
        this.clock = clockArg;
        this.reader = readerArg;
    }

    /// Returns a soak of a process, sampled every 60 s, each `jcmd` limited to 30 s.
    ///
    /// @param pid the process's pid, 1 or more (`jcmd` reads 0 as every Java process)
    /// @return the soak
    /// @throws IllegalArgumentException if the pid is below 1
    public static HeapSoak of(final long pid) {
        if (pid < 1) {
            throw new IllegalArgumentException("the pid " + pid + " is below 1; jcmd reads 0 as every Java process");
        }
        return new HeapSoak(pid, DEFAULT_INTERVAL, DEFAULT_JCMD_LIMIT, PacingClock.SYSTEM,
            new JcmdHeapReader(DEFAULT_JCMD_LIMIT));
    }

    /// Returns this soak sampled at another interval.
    ///
    /// @param every the interval, above 0
    /// @return the new soak
    /// @throws IllegalArgumentException if it is not above 0
    public HeapSoak interval(final Duration every) {
        return new HeapSoak(pid, positive(every, "interval"), jcmdLimit, clock, reader);
    }

    /// Returns this soak with another time limit on each `jcmd`; one past it is killed, and fails the soak.
    ///
    /// @param limit the limit, above 0
    /// @return the new soak
    /// @throws IllegalArgumentException if it is not above 0
    public HeapSoak jcmdLimit(final Duration limit) {
        Duration checked = positive(limit, "jcmd limit");
        return new HeapSoak(pid, interval, checked, clock,
            reader instanceof JcmdHeapReader ? new JcmdHeapReader(checked) : reader);
    }

    /// Samples the heap for a duration, on the calling thread, and fits its slope.
    ///
    /// @param duration how long, at least two intervals, so that there are at least three samples
    /// @return the samples and the slope
    /// @throws IllegalArgumentException if the duration is shorter than two intervals
    /// @throws IllegalStateException if `jcmd` fails, passes its limit, or prints no heap line it can read
    /// @throws IOException if `jcmd` cannot be started, or its files written or read
    /// @throws InterruptedException if interrupted while it waits
    public Result run(final Duration duration) throws IOException, InterruptedException {
        Objects.requireNonNull(duration, "duration");
        if (duration.compareTo(interval.multipliedBy(MINIMUM_INTERVALS)) < 0) {
            throw new IllegalArgumentException("the soak's duration " + duration + " is shorter than two intervals "
                + "of " + interval + ", too short for three samples");
        }
        long count = duration.toNanos() / interval.toNanos() + 1;
        List<Sample> samples = new ArrayList<>();
        long start = clock.nanoTime();
        for (long k = 0; k < count; k++) {
            clock.waitUntil(start + k * interval.toNanos(), SPIN_NANOS);
            long began = clock.nanoTime();
            long bytes = reader.liveBytes(pid);
            samples.add(new Sample(Duration.ofNanos(began - start), bytes,
                Duration.ofNanos(clock.nanoTime() - began)));
        }
        return new Result(samples, slope(samples));
    }

    /// Returns this soak on another clock and heap reader, for the tests.
    HeapSoak with(final PacingClock otherClock, final HeapReader otherReader) {
        return new HeapSoak(pid, interval, jcmdLimit, otherClock, otherReader);
    }

    /// The least-squares slope of the bytes against the time, in MB per minute.
    static double slope(final List<Sample> samples) {
        double meanTime = 0;
        double meanBytes = 0;
        for (final Sample sample : samples) {
            meanTime += sample.time().toNanos() / NANOS_PER_MINUTE;
            meanBytes += sample.bytes() / BYTES_PER_MB;
        }
        meanTime /= samples.size();
        meanBytes /= samples.size();
        double covariance = 0;
        double variance = 0;
        for (final Sample sample : samples) {
            double time = sample.time().toNanos() / NANOS_PER_MINUTE - meanTime;
            covariance += time * (sample.bytes() / BYTES_PER_MB - meanBytes);
            variance += time * time;
        }
        return covariance / variance;
    }

    private static Duration positive(final Duration duration, final String what) {
        Objects.requireNonNull(duration, what);
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("the " + what + " " + duration + " is not above 0");
        }
        return duration;
    }

    /// Reads a process's live heap; `jcmd`, or, in the tests, readings the test gives.
    interface HeapReader {

        /// Returns the process's live heap.
        ///
        /// @param pid the process's pid
        /// @return its bytes
        /// @throws IOException if the reading cannot be made
        /// @throws InterruptedException if interrupted while it is made
        long liveBytes(long pid) throws IOException, InterruptedException;
    }

    /// One sample of a soak.
    ///
    /// @param time when it began, from the soak's start
    /// @param bytes the heap's occupancy just after the collection
    /// @param invocation how long its whole `jcmd` invocation took, the collection included; no measure of the gap
    ///     between the collection and the reading
    /// @since 0.1.6
    public record Sample(Duration time, long bytes, Duration invocation) {

        /// Checks the sample.
        ///
        /// @param time when it began
        /// @param bytes the occupancy
        /// @param invocation the invocation's time
        public Sample {
            Objects.requireNonNull(time, "time");
            Objects.requireNonNull(invocation, "invocation");
        }
    }

    /// What a soak measured: its samples and their slope.
    ///
    /// @param samples the samples, in order, at least three
    /// @param slopeMegabytesPerMinute the least-squares slope, in MB (2²⁰ bytes) per minute
    /// @since 0.1.6
    public record Result(List<Sample> samples, double slopeMegabytesPerMinute) {

        /// Copies the samples.
        ///
        /// @param samples the samples
        /// @param slopeMegabytesPerMinute the slope
        public Result {
            samples = List.copyOf(samples);
        }

        /// Returns the slope as `heapSlope`, a `count` in `MB/min`, which the gate never compares with a baseline.
        ///
        /// @return the metric
        public ScenarioMetric metric() {
            return new ScenarioMetric(METRIC, MetricKind.COUNT, UNIT, slopeMegabytesPerMinute);
        }

        /// Returns a scenario result of the metric with a bound at the limit, which the gate checks wherever
        /// timings are judged.
        ///
        /// @param limitMegabytesPerMinute the bound, in MB per minute; the slope must be below it
        /// @return the result
        public ScenarioResult scenarioResult(final double limitMegabytesPerMinute) {
            return new ScenarioResult(List.of(metric()), List.of(new ScenarioBound(METRIC, limitMegabytesPerMinute)),
                List.of());
        }
    }
}
