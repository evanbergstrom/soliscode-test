/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.soliscode.performance.MetricKind;
import com.soliscode.performance.ScenarioMetric;

/// What one run of an [OpenLoopPacer] measured.
///
/// @param latencies the measured operations' latencies, each from its scheduled send to its return
/// @param operations how many operations were measured (the warmup's are not)
/// @param achievedRate the measured operations per second, from the measured window's scheduled start to its
///     scheduled end or the last measured operation's return, whichever is later
/// @since 0.1.6
public record PacedRun(LatencyRecorder latencies, long operations, double achievedRate) {

    /// Checks the run.
    ///
    /// @param latencies the latencies
    /// @param operations the operations measured
    /// @param achievedRate the achieved rate
    public PacedRun {
        Objects.requireNonNull(latencies, "latencies");
    }

    /// Returns the latencies' percentiles ([LatencyRecorder#metrics()]) and `rate`, the achieved rate, a `rate`
    /// metric in `ops/s`.
    ///
    /// @return the metrics
    public List<ScenarioMetric> metrics() {
        List<ScenarioMetric> metrics = new ArrayList<>(latencies.metrics());
        metrics.add(new ScenarioMetric("rate", MetricKind.RATE, "ops/s", achievedRate));
        return List.copyOf(metrics);
    }
}
