/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.performance.MetricKind;
import com.soliscode.performance.ScenarioMetric;
import com.soliscode.test.AbstractTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [LatencyRecorder].
@DisplayName("LatencyRecorder")
class LatencyRecorderTest extends AbstractTest {

    @Test
    @DisplayName("metrics() reports p50 to max as time metrics in ns")
    void metricsReportsThePercentilesAsTimeInNanoseconds() {
        LatencyRecorder recorder = LatencyRecorder.upTo(Duration.ofSeconds(1));
        for (int i = 1; i <= 1000; i++) {
            recorder.record(i);
        }

        List<ScenarioMetric> metrics = recorder.metrics();

        assertEquals(List.of("p50", "p90", "p95", "p99", "p99.9", "max"),
            metrics.stream().map(ScenarioMetric::name).toList());
        assertTrue(metrics.stream().allMatch(m -> m.kind() == MetricKind.TIME && m.unit().equals("ns")));
        assertEquals(List.of(500.0, 900.0, 950.0, 990.0, 999.0, 1000.0),
            metrics.stream().map(ScenarioMetric::value).toList());
    }

    @Test
    @DisplayName("metrics(prefix) puts the prefix before each name")
    void metricsWithPrefixNamesEachMetricAfterIt() {
        LatencyRecorder recorder = new LatencyRecorder(1_000, 3);
        recorder.record(7);

        assertEquals(List.of("send.p50", "send.p90", "send.p95", "send.p99", "send.p99.9", "send.max"),
            recorder.metrics("send").stream().map(ScenarioMetric::name).toList());
        assertThrows(IllegalArgumentException.class, () -> recorder.metrics(" "));
    }

    @Test
    @DisplayName("metrics() of an empty recorder throws IllegalStateException")
    void metricsOfAnEmptyRecorderThrows() {
        LatencyRecorder recorder = new LatencyRecorder(1_000, 3);

        assertThrows(IllegalStateException.class, recorder::metrics);
        assertThrows(IllegalStateException.class, () -> recorder.metrics("x"));
    }

    @Test
    @DisplayName("record() refuses a latency below 0 or above the highest, and takes both ends")
    void recordRefusesALatencyOutOfRange() {
        LatencyRecorder recorder = new LatencyRecorder(1_000, 2);

        recorder.record(0);
        recorder.record(1_000);
        assertThrows(IllegalArgumentException.class, () -> recorder.record(-1));
        assertThrows(IllegalArgumentException.class, () -> recorder.record(1_001));
        assertEquals(2, recorder.count());
        assertTrue(recorder.accepts(1_000));
        assertFalse(recorder.accepts(1_001));
        assertFalse(recorder.accepts(-1));
        assertEquals(1_000, recorder.highestTrackable());
    }

    @Test
    @DisplayName("the constructor refuses a highest below 2 ns and significant digits outside 1 to 5")
    void constructorRefusesItsArgumentsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new LatencyRecorder(1, 3));
        assertThrows(IllegalArgumentException.class, () -> new LatencyRecorder(1_000, 0));
        assertThrows(IllegalArgumentException.class, () -> new LatencyRecorder(1_000, 6));
        assertThrows(NullPointerException.class, () -> LatencyRecorder.upTo(null));
    }

    @Test
    @DisplayName("valueAtPercentile() reads the histogram, and refuses a percentile outside 0 to 100")
    void valueAtPercentileReadsTheHistogram() {
        LatencyRecorder recorder = new LatencyRecorder(1_000_000, 3);
        recorder.record(10);
        recorder.record(20);

        assertEquals(10, recorder.valueAtPercentile(50));
        assertEquals(20, recorder.valueAtPercentile(100));
        assertThrows(IllegalArgumentException.class, () -> recorder.valueAtPercentile(-0.1));
        assertThrows(IllegalArgumentException.class, () -> recorder.valueAtPercentile(100.1));
        assertThrows(IllegalArgumentException.class, () -> recorder.valueAtPercentile(Double.NaN));
    }

    @Test
    @DisplayName("reset() forgets every latency")
    void resetForgetsEveryLatency() {
        LatencyRecorder recorder = new LatencyRecorder(1_000, 3);
        recorder.record(5);

        recorder.reset();

        assertEquals(0, recorder.count());
        assertThrows(IllegalStateException.class, recorder::metrics);
    }

    @Test
    @DisplayName("record() allocates nothing")
    void recordAllocatesNothing() throws Exception {
        LatencyRecorder recorder = LatencyRecorder.upTo(Duration.ofSeconds(10));

        AllocationAssertion.assertAllocatesAtMost(0, 100_000, () -> recorder.record(12_345));
    }
}
