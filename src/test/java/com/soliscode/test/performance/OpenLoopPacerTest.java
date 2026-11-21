/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.performance.MetricKind;
import com.soliscode.performance.ScenarioMetric;
import com.soliscode.test.AbstractTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [OpenLoopPacer], on a [FakeClock] but for one short run on the system's clock.
@DisplayName("OpenLoopPacer")
class OpenLoopPacerTest extends AbstractTest {

    private static final long MS = 1_000_000;

    @Test
    @DisplayName("operations are sent on schedule, the warmup's run and not measured, from sequence 0")
    void operationsAreSentOnScheduleAfterAWarmup() throws Exception {
        FakeClock clock = new FakeClock();
        long start = clock.nanoTime();
        List<Long> sent = new ArrayList<>();
        List<Long> sequences = new ArrayList<>();

        PacedRun run = OpenLoopPacer.at(1_000).warmup(Duration.ofMillis(2)).duration(Duration.ofMillis(5)).on(clock)
            .run(sequence -> {
                sequences.add(sequence);
                sent.add(clock.nanoTime() - start);
            });

        assertEquals(List.of(0L, 1L, 2L, 3L, 4L, 5L, 6L), sequences);
        assertEquals(List.of(0L, MS, 2 * MS, 3 * MS, 4 * MS, 5 * MS, 6 * MS), sent);
        assertEquals(5, run.operations());
        assertEquals(5, run.latencies().count());
        assertEquals(0, run.latencies().valueAtPercentile(100));
        assertEquals(1_000.0, run.achievedRate(), 1e-9);
    }

    @Test
    @DisplayName("a slow operation delays the next, whose latency counts the wait (no coordinated omission)")
    void aSlowOperationsWaitCountsInTheNextLatency() throws Exception {
        FakeClock clock = new FakeClock();

        PacedRun run = OpenLoopPacer.at(1_000).duration(Duration.ofMillis(3)).on(clock)
            .run(sequence -> clock.advance(MS * 3 / 2));

        assertEquals(3, run.operations());
        assertEquals(MS * 3 / 2, run.latencies().valueAtPercentile(0), MS / 1000.0);
        assertEquals(MS * 5 / 2, run.latencies().valueAtPercentile(100), MS * 5 / 2 / 1000.0);
        assertEquals(2 * MS, run.latencies().valueAtPercentile(50), 2 * MS / 1000.0);
        assertEquals(3 / 0.0045, run.achievedRate(), 1e-6);
    }

    @Test
    @DisplayName("the pacer parks until 50 µs before a send, then spins")
    void thePacerParksThenSpins() throws Exception {
        FakeClock clock = new FakeClock();

        OpenLoopPacer.at(1_000).duration(Duration.ofMillis(2)).on(clock).run(sequence -> { });

        assertEquals(MS - OpenLoopPacer.SPIN_NANOS, clock.parked());
    }

    @Test
    @DisplayName("metrics() is the latencies and the achieved rate, in ops/s")
    void metricsAreTheLatenciesAndTheRate() throws Exception {
        PacedRun run = OpenLoopPacer.at(1_000).duration(Duration.ofMillis(4)).on(new FakeClock())
            .run(sequence -> { });

        List<ScenarioMetric> metrics = run.metrics();

        assertEquals(List.of("p50", "p90", "p95", "p99", "p99.9", "max", "rate"),
            metrics.stream().map(ScenarioMetric::name).toList());
        ScenarioMetric rate = metrics.get(6);
        assertEquals(MetricKind.RATE, rate.kind());
        assertEquals("ops/s", rate.unit());
        assertEquals(1_000.0, rate.value(), 1e-9);
    }

    @Test
    @DisplayName("an exception from the operation ends the run and propagates")
    void anExceptionEndsTheRun() {
        IllegalStateException failure = new IllegalStateException("down");
        List<Long> sequences = new ArrayList<>();

        assertSame(failure, assertThrows(IllegalStateException.class,
            () -> OpenLoopPacer.at(1_000).duration(Duration.ofMillis(5)).on(new FakeClock()).run(sequence -> {
                sequences.add(sequence);
                if (sequence == 1) {
                    throw failure;
                }
            })));
        assertEquals(List.of(0L, 1L), sequences);
    }

    @Test
    @DisplayName("a latency above the highest fails the run with IllegalStateException")
    void aLatencyAboveTheHighestFailsTheRun() {
        FakeClock clock = new FakeClock();
        OpenLoopPacer pacer = OpenLoopPacer.at(1_000).duration(Duration.ofMillis(2))
            .highestLatency(Duration.ofMillis(1)).on(clock);

        IllegalStateException failure = assertThrows(IllegalStateException.class,
            () -> pacer.run(sequence -> clock.advance(2 * MS)));

        assertTrue(failure.getMessage().startsWith("operation 0 took 2000000 ns"), failure.getMessage());
    }

    @Test
    @DisplayName("a rate, warmup, duration or highest latency out of range is refused")
    void refusesItsSettingsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> OpenLoopPacer.at(0));
        assertThrows(IllegalArgumentException.class, () -> OpenLoopPacer.at(-1));
        assertThrows(IllegalArgumentException.class, () -> OpenLoopPacer.at(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> OpenLoopPacer.at(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> OpenLoopPacer.at(Double.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> OpenLoopPacer.at(1e9 + 1));
        OpenLoopPacer pacer = OpenLoopPacer.at(1_000);
        assertThrows(IllegalArgumentException.class, () -> pacer.warmup(Duration.ofNanos(-1)));
        assertThrows(IllegalArgumentException.class, () -> pacer.duration(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> pacer.duration(Duration.ofMillis(-1)));
        assertThrows(IllegalArgumentException.class, () -> pacer.duration(Duration.ofMillis(1)));
        assertThrows(IllegalArgumentException.class, () -> pacer.highestLatency(Duration.ofNanos(1)));
        assertThrows(IllegalArgumentException.class, () -> OpenLoopPacer.at(0.1).run(sequence -> { }));
    }

    @Test
    @DisplayName("an interrupt while waiting for a send ends the run with InterruptedException")
    void anInterruptEndsTheRun() {
        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedException.class,
                () -> OpenLoopPacer.at(1_000).duration(Duration.ofMillis(5)).on(new FakeClock()).run(s -> { }));
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    @DisplayName("the warmup's end and the window's end are judged by the rounded times operations are sent at")
    void theBoundariesUseTheRoundedSchedule() throws Exception {
        // a period of 2.6 ns: sends at 0, 3, 5, 8, 10, 13 ns; a 3 ns warmup holds only the send at 0
        OpenLoopPacer pacer = OpenLoopPacer.at(1e9 / 2.6).warmup(Duration.ofNanos(3)).duration(Duration.ofNanos(10));
        List<Long> sent = new ArrayList<>();

        PacedRun run = pacer.on(new FakeClock()).run(sent::add);

        assertEquals(4, pacer.measuredOperations());
        assertEquals(4, run.operations());
        assertEquals(List.of(0L, 1L, 2L, 3L, 4L), sent);
    }

    @Test
    @DisplayName("measuredOperations() counts the operations scheduled in the duration")
    void measuredOperationsCountsTheDuration() {
        assertEquals(10_000, OpenLoopPacer.at(1_000).measuredOperations());
        assertEquals(30, OpenLoopPacer.at(10).warmup(Duration.ofSeconds(5)).duration(Duration.ofSeconds(3))
            .measuredOperations());
    }

    @Test
    @DisplayName("a short run on the system's clock finishes, measuring every operation it schedules")
    void aShortRunOnTheSystemClockFinishes() throws Exception {
        PacedRun run = OpenLoopPacer.at(1_000).warmup(Duration.ofMillis(10)).duration(Duration.ofMillis(100))
            .run(sequence -> { });

        assertEquals(100, run.operations());
        assertEquals(100, run.latencies().count());
        assertTrue(run.metrics().stream().allMatch(m -> Double.isFinite(m.value()) && m.value() >= 0));
    }
}
