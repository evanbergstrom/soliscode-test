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
