/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.HdrHistogram.Histogram;

import com.soliscode.performance.MetricKind;
import com.soliscode.performance.ScenarioMetric;

/// Records latencies in nanoseconds into an HdrHistogram [Histogram] sized when the recorder is made, and reports
/// their percentiles as `time` metrics.
///
/// Recording allocates nothing: the histogram's counts are allocated once, for every value from 1 ns to the
/// highest trackable value, at the significant digits given (3 by default, so each reported value is within 0.1%
/// of the value recorded).
///
/// ## Usage Examples
/// ```java
/// LatencyRecorder latencies = LatencyRecorder.upTo(Duration.ofSeconds(10));
/// long start = System.nanoTime();
/// call();
/// latencies.record(System.nanoTime() - start);
/// return new ScenarioResult(latencies.metrics(), List.of(), List.of());
/// ```
///
/// ## Thread Safety
/// Not thread safe: one thread records, or the caller orders the threads that do (a lock, or a join before
/// [#metrics()]).
///
/// @since 0.1.6
public final class LatencyRecorder {

    /// The percentiles [#metrics()] reports, in order, with `max` after them.
    private static final double[] PERCENTILES = {50.0, 90.0, 95.0, 99.0, 99.9};

    /// The metric names of [#PERCENTILES], in the same order.
    private static final String[] PERCENTILE_NAMES = {"p50", "p90", "p95", "p99", "p99.9"};

    /// The significant digits [#upTo(Duration)] keeps.
    private static final int DEFAULT_SIGNIFICANT_DIGITS = 3;

    /// The most significant digits HdrHistogram keeps.
    private static final int MAX_SIGNIFICANT_DIGITS = 5;

    private final Histogram histogram;
    private final long highest;

    /// Makes a recorder of values from 0 to `highestNanos`, at the significant digits given.
    ///
    /// @param highestNanos the highest value that can be recorded, in nanoseconds, at least 2
    /// @param significantDigits the significant digits kept, 1 to 5
    /// @throws IllegalArgumentException if either is out of range
    public LatencyRecorder(final long highestNanos, final int significantDigits) {
        if (highestNanos < 2) {
            throw new IllegalArgumentException("the highest latency is " + highestNanos + " ns, below 2 ns");
        }
        if (significantDigits < 1 || significantDigits > MAX_SIGNIFICANT_DIGITS) {
            throw new IllegalArgumentException("the significant digits are " + significantDigits
                + ", not 1 to " + MAX_SIGNIFICANT_DIGITS);
        }
        this.highest = highestNanos;
        this.histogram = new Histogram(1, highestNanos, significantDigits);
    }

    /// Makes a recorder of values from 0 to `highest`, at 3 significant digits.
    ///
    /// @param highest the highest latency that can be recorded, at least 2 ns
    /// @return the recorder
    /// @throws IllegalArgumentException if `highest` is below 2 ns
    /// @throws ArithmeticException if `highest` is more nanoseconds than a `long` holds
    public static LatencyRecorder upTo(final Duration highest) {
        Objects.requireNonNull(highest, "highest");
        return new LatencyRecorder(highest.toNanos(), DEFAULT_SIGNIFICANT_DIGITS);
    }

    /// Records one latency. Allocates nothing, unless it is refused.
    ///
    /// @param nanos the latency, from 0 to the highest trackable value
    /// @throws IllegalArgumentException if it is out of range
    public void record(final long nanos) {
        if (nanos < 0 || nanos > highest) {
            throw new IllegalArgumentException("the latency " + nanos + " ns is not from 0 to " + highest + " ns");
        }
        histogram.recordValue(nanos);
    }

    /// Returns whether a latency can be recorded: whether it is from 0 to the highest trackable value.
    ///
    /// @param nanos the latency
    /// @return `true` if [#record(long)] takes it
    public boolean accepts(final long nanos) {
        return nanos >= 0 && nanos <= highest;
    }

    /// Returns the highest latency that can be recorded.
    ///
    /// @return the highest, in nanoseconds
    public long highestTrackable() {
        return highest;
    }

    /// Returns how many latencies were recorded.
    ///
    /// @return the count
    public long count() {
        return histogram.getTotalCount();
    }

    /// Returns the latency at a percentile of those recorded, within the significant digits.
    ///
    /// @param percentile the percentile, 0 to 100
    /// @return the latency, in nanoseconds, or 0 if none was recorded
    /// @throws IllegalArgumentException if the percentile is not from 0 to 100
    public long valueAtPercentile(final double percentile) {
        if (!(percentile >= 0.0 && percentile <= 100.0)) {
            throw new IllegalArgumentException("the percentile " + percentile + " is not from 0 to 100");
        }
        return histogram.getValueAtPercentile(percentile);
    }

    /// Forgets every latency recorded.
    public void reset() {
        histogram.reset();
    }

    /// Returns the latencies' `p50`, `p90`, `p95`, `p99`, `p99.9` and `max`, as `time` metrics in `ns`.
    ///
    /// @return the metrics
    /// @throws IllegalStateException if no latency was recorded
    public List<ScenarioMetric> metrics() {
        return metricsNamed("");
    }

    /// Returns the latencies' percentiles as [#metrics()] does, each name after `<prefix>.`: `send.p99`.
    ///
    /// @param prefix the names' prefix, not blank
    /// @return the metrics
    /// @throws IllegalArgumentException if the prefix is blank
    /// @throws IllegalStateException if no latency was recorded
    public List<ScenarioMetric> metrics(final String prefix) {
        Objects.requireNonNull(prefix, "prefix");
        if (prefix.isBlank()) {
            throw new IllegalArgumentException("the metrics' prefix is blank");
        }
        return metricsNamed(prefix + ".");
    }

    private List<ScenarioMetric> metricsNamed(final String prefix) {
        if (histogram.getTotalCount() == 0) {
            throw new IllegalStateException("no latency was recorded, so there is none to report");
        }
        List<ScenarioMetric> metrics = new ArrayList<>(PERCENTILES.length + 1);
        for (int i = 0; i < PERCENTILES.length; i++) {
            metrics.add(time(prefix + PERCENTILE_NAMES[i], histogram.getValueAtPercentile(PERCENTILES[i])));
        }
        metrics.add(time(prefix + "max", histogram.getMaxValue()));
        return List.copyOf(metrics);
    }

    private static ScenarioMetric time(final String name, final long nanos) {
        return new ScenarioMetric(name, MetricKind.TIME, "ns", nanos);
    }
}
