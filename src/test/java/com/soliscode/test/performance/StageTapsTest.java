/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.performance.MetricKind;
import com.soliscode.performance.ScenarioMetric;
import com.soliscode.test.AbstractTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [StageTaps].
@DisplayName("StageTaps")
class StageTapsTest extends AbstractTest {

    @Test
    @DisplayName("metrics() reports each stage's latency from the stage before it, and its samples")
    void metricsReportsEachStageFromTheOneBefore() {
        StageTaps taps = StageTaps.of(4, "send", "match", "publish");
        for (int id = 0; id < 4; id++) {
            taps.tap(0, id, 1_000L * id);
            taps.tap(1, id, 1_000L * id + 100);
            taps.tap(2, id, 1_000L * id + 400);
        }

        Map<String, ScenarioMetric> metrics = byName(taps.metrics(1.0));

        assertEquals(16, metrics.size());
        assertEquals(100.0, metrics.get("match.max").value());
        assertEquals(300.0, metrics.get("publish.p50").value());
        assertEquals(MetricKind.TIME, metrics.get("publish.p50").kind());
        assertEquals(4.0, metrics.get("match.samples").value());
        assertEquals(MetricKind.COUNT, metrics.get("match.samples").kind());
        assertEquals("samples", metrics.get("match.samples").unit());
        assertEquals(0.0, metrics.get("publish.reversed").value());
    }

    @Test
    @DisplayName("a stage with fewer samples than the share of origin taps fails with IllegalStateException")
    void aStageBelowTheShareFails() {
        StageTaps taps = StageTaps.of(10, "send", "match");
        for (int id = 0; id < 10; id++) {
            taps.tap(0, id, id);
        }
        for (int id = 0; id < 8; id++) {
            taps.tap(1, id, id + 5L);
        }

        assertEquals(8.0, byName(taps.metrics(0.8)).get("match.samples").value());
        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> taps.metrics(0.9));
        assertEquals("the stage match has 8 samples of the 10 ids tapped at the origin, fewer than the share of 0.9",
            failure.getMessage());
    }

    @Test
    @DisplayName("ids the origin never saw are not samples, so they cannot make up another's share")
    void idsTheOriginNeverSawAreNotSamples() {
        StageTaps taps = StageTaps.of(3, "send", "match", "publish");
        taps.tap(0, 0, 0);
        for (int id = 1; id < 3; id++) {
            taps.tap(1, id, 10);
            taps.tap(2, id, 20);
        }

        assertThrows(IllegalStateException.class, () -> taps.metrics(1.0));
        assertThrows(IllegalStateException.class, () -> taps.metrics(0.0));
    }

    @Test
    @DisplayName("an id whose time at a stage is before the stage before it is counted as reversed, not a sample")
    void aReversedIdIsCountedApart() {
        StageTaps taps = StageTaps.of(2, "send", "match");
        taps.tap(0, 0, 100);
        taps.tap(1, 0, 150);
        taps.tap(0, 1, 100);
        taps.tap(1, 1, 50);

        Map<String, ScenarioMetric> metrics = byName(taps.metrics(0.5));

        assertEquals(1.0, metrics.get("match.samples").value());
        assertEquals(1.0, metrics.get("match.reversed").value());
        assertEquals(50.0, metrics.get("match.max").value());
    }

    @Test
    @DisplayName("a second tap of an id at a stage replaces the first")
    void aSecondTapReplacesTheFirst() {
        StageTaps taps = StageTaps.of(1, "send", "match");
        taps.tap(0, 0, 0);
        taps.tap(1, 0, 10);
        taps.tap(1, 0, 30);

        assertEquals(30.0, byName(taps.metrics(1.0)).get("match.max").value());
    }

    @Test
    @DisplayName("a latency above the highest fails with IllegalStateException")
    void aLatencyAboveTheHighestFails() {
        StageTaps taps = StageTaps.of(1, Duration.ofNanos(100), "send", "match");
        taps.tap(0, 0, 0);
        taps.tap(1, 0, 101);

        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> taps.metrics(0));
        assertTrue(failure.getMessage().startsWith("the id 0 took 101 ns to reach the stage match"),
            failure.getMessage());
    }

    @Test
    @DisplayName("a difference too large for a long is above the highest")
    void aDifferenceThatOverflowsIsAboveTheHighest() {
        StageTaps taps = StageTaps.of(1, "send", "match");
        taps.tap(0, 0, -2);
        taps.tap(1, 0, Long.MAX_VALUE);

        assertThrows(IllegalStateException.class, () -> taps.metrics(0));
    }

    @Test
    @DisplayName("tap() refuses a stage or id out of range and the time Long.MIN_VALUE")
    void tapRefusesItsArgumentsOutOfRange() {
        StageTaps taps = StageTaps.of(2, "send", "match");

        assertThrows(IllegalArgumentException.class, () -> taps.tap(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> taps.tap(2, 0));
        assertThrows(IllegalArgumentException.class, () -> taps.tap(0, -1));
        assertThrows(IllegalArgumentException.class, () -> taps.tap(0, 2));
        assertThrows(IllegalArgumentException.class, () -> taps.tap(0, 0, Long.MIN_VALUE));
        taps.tap(0, 1);
    }

    @Test
    @DisplayName("the taps refuse a capacity below 1, a highest below 2 ns, and a name blank or given twice")
    void refusesItsSettingsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> StageTaps.of(0, "a", "b"));
        assertThrows(IllegalArgumentException.class, () -> StageTaps.of(1, Duration.ofNanos(1), "a", "b"));
        assertThrows(IllegalArgumentException.class, () -> StageTaps.of(1, "a", "a"));
        assertThrows(IllegalArgumentException.class, () -> StageTaps.of(1, "a", "b", " "));
        assertThrows(IllegalArgumentException.class, () -> StageTaps.of(1, "a", "b").metrics(1.1));
        assertThrows(IllegalArgumentException.class, () -> StageTaps.of(1, "a", "b").metrics(-0.1));
    }

    @Test
    @DisplayName("stages() and stage() name the stages and find their indexes")
    void stagesAreNamedAndFound() {
        StageTaps taps = StageTaps.of(1, "send", "match", "publish");

        assertEquals(List.of("send", "match", "publish"), taps.stages());
        assertEquals(2, taps.stage("publish"));
        assertThrows(IllegalArgumentException.class, () -> taps.stage("settle"));
    }

    @Test
    @DisplayName("tap() is thread safe: ids tapped from several threads are all samples")
    void tapIsThreadSafe() throws InterruptedException {
        int ids = 4_000;
        StageTaps taps = StageTaps.of(ids, "send", "match");
        Thread[] threads = new Thread[4];
        for (int t = 0; t < threads.length; t++) {
            int first = t * ids / threads.length;
            threads[t] = new Thread(() -> {
                for (int id = first; id < first + ids / threads.length; id++) {
                    taps.tap(0, id);
                    taps.tap(1, id);
                }
            });
            threads[t].start();
        }
        for (final Thread thread : threads) {
            thread.join();
        }

        assertEquals((double) ids, byName(taps.metrics(1.0)).get("match.samples").value());
    }

    @Test
    @DisplayName("tap() allocates nothing")
    void tapAllocatesNothing() throws Exception {
        StageTaps taps = StageTaps.of(16, "send", "match");

        AllocationAssertion.assertAllocatesAtMost(0, 100_000, () -> taps.tap(1, 7, 42));
    }

    private static Map<String, ScenarioMetric> byName(final List<ScenarioMetric> metrics) {
        return metrics.stream().collect(Collectors.toMap(ScenarioMetric::name, Function.identity()));
    }
}
