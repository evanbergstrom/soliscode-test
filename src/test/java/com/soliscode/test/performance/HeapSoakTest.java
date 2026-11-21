/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.performance.MetricKind;
import com.soliscode.performance.ScenarioBound;
import com.soliscode.performance.ScenarioMetric;
import com.soliscode.performance.ScenarioResult;
import com.soliscode.test.AbstractTest;
import com.soliscode.test.annotations.Slow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [HeapSoak] and its `jcmd` reader, on a [FakeClock] but for one short soak of this JVM.
@DisplayName("HeapSoak")
class HeapSoakTest extends AbstractTest {

    private static final long MB = 1 << 20;
    private static final long MINUTE = 60_000_000_000L;

    @Test
    @DisplayName("run() samples at the start and each interval, and fits the slope in MB/min")
    void runSamplesAtEachIntervalAndFitsTheSlope() throws Exception {
        FakeClock clock = new FakeClock();
        long start = clock.nanoTime();
        List<Long> times = new ArrayList<>();
        HeapSoak soak = HeapSoak.of(42).with(clock, pid -> {
            times.add(clock.nanoTime() - start);
            clock.advance(1_000_000);
            return (100 + 2 * (long) times.size()) * MB;
        });

        HeapSoak.Result result = soak.run(Duration.ofMinutes(4));

        assertEquals(List.of(0L, MINUTE, 2 * MINUTE, 3 * MINUTE, 4 * MINUTE), times);
        assertEquals(5, result.samples().size());
        assertEquals(Duration.ofMinutes(2), result.samples().get(2).time());
        assertEquals(106 * MB, result.samples().get(2).bytes());
        assertEquals(Duration.ofMillis(1), result.samples().get(2).invocation());
        assertEquals(2.0, result.slopeMegabytesPerMinute(), 1e-9);
    }

    @Test
    @DisplayName("a sample that ends after the next one's time makes the next start at once")
    void aLateSampleMakesTheNextStartAtOnce() throws Exception {
        FakeClock clock = new FakeClock();
        long start = clock.nanoTime();
        List<Long> times = new ArrayList<>();
        HeapSoak soak = HeapSoak.of(42).interval(Duration.ofSeconds(1)).with(clock, pid -> {
            times.add(clock.nanoTime() - start);
            clock.advance(times.size() == 1 ? 1_500_000_000L : 1_000);
            return MB;
        });

        HeapSoak.Result result = soak.run(Duration.ofSeconds(2));

        assertEquals(List.of(0L, 1_500_000_000L, 2_000_000_000L), times);
        assertEquals(0.0, result.slopeMegabytesPerMinute(), 1e-9);
    }

    @Test
    @DisplayName("a duration shorter than two intervals is refused, and so is an interval or limit not above 0")
    void refusesTooShortADurationAndIntervalsNotAboveZero() {
        HeapSoak soak = HeapSoak.of(42);

        assertThrows(IllegalArgumentException.class, () -> soak.run(Duration.ofSeconds(119)));
        assertThrows(IllegalArgumentException.class, () -> soak.interval(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> soak.jcmdLimit(Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> HeapSoak.of(0));
        assertThrows(IllegalArgumentException.class, () -> HeapSoak.of(-1));
    }

    @Test
    @DisplayName("metric() is heapSlope, a count in MB/min, and scenarioResult() bounds it")
    void theResultIsACountWithABound() {
        HeapSoak.Result result = new HeapSoak.Result(List.of(), 0.25);

        ScenarioMetric metric = result.metric();
        assertEquals(new ScenarioMetric("heapSlope", MetricKind.COUNT, "MB/min", 0.25), metric);
        ScenarioResult scenario = result.scenarioResult(0.5);
        assertEquals(List.of(metric), scenario.metrics());
        assertEquals(List.of(new ScenarioBound("heapSlope", 0.5)), scenario.bounds());
    }

    @Test
    @DisplayName("jcmd's heap lines are read for G1, ZGC, Parallel and Serial, in K, M and G")
    void heapLinesAreReadForEachCollector() {
        assertEquals(29_671L << 10, JcmdHeapReader.usedBytes("""
            34691:
            Command executed successfully
            garbage-first heap   total reserved 16777216K, committed 1056768K, used 29671K [0x0, 0x1)
             region size 8192K, 4 young (32768K), 0 survivors (0K)
            """));
        assertEquals(60L << 20, JcmdHeapReader.usedBytes("""
            49579:
            Command executed successfully
            ZHeap            used 60M, capacity 1024M, max capacity 16384M
             Cache           964M (1)
            """));
        assertEquals((57_707L + 1_150) << 10, JcmdHeapReader.usedBytes("""
            1:
            PSYoungGen      total 305664K, used 57707K [0x0, 0x1, 0x2)
             eden space 262144K, 22% used [0x0,0x1,0x2)
            ParOldGen       total 699392K, used 1150K [0x0, 0x1, 0x2)
             object space 699392K, 0% used [0x0,0x1,0x2)
            """));
        assertEquals((61_519L + 1_150) << 10, JcmdHeapReader.usedBytes("""
            1:
            DefNew     total 314560K, used 61519K [0x0, 0x1, 0x2)
             eden space 279616K,  22% used [0x0, 0x1, 0x2)
            Tenured    total 699072K, used 1150K [0x0, 0x1, 0x2)
            """));
        assertEquals(3L << 30, JcmdHeapReader.usedBytes("1:\nZHeap used 3G, capacity 4G\n"));
    }

    @Test
    @DisplayName("output with no heap line fails with IllegalStateException, quoting it")
    void outputWithNoHeapLineFails() {
        IllegalStateException failure = assertThrows(IllegalStateException.class,
            () -> JcmdHeapReader.usedBytes("1:\nCommand executed successfully\n eden used 5K\n"));

        assertTrue(failure.getMessage().endsWith("Command executed successfully\n eden used 5K\n"),
            failure.getMessage());
    }

    @Test
    @DisplayName("a jcmd that fails, and one past its limit, fail the soak")
    @Slow
    void aFailingOrSlowJcmdFails() {
        // no process has this pid, so jcmd cannot attach and exits non-zero
        assertThrows(IllegalStateException.class,
            () -> new JcmdHeapReader(Duration.ofSeconds(30)).liveBytes(999_999_999));
        assertThrows(IllegalStateException.class,
            () -> new JcmdHeapReader(Duration.ofNanos(1)).liveBytes(ProcessHandle.current().pid()));
    }

    @Test
    @DisplayName("an interrupt while jcmd runs ends the reading with InterruptedException")
    @Slow
    void anInterruptEndsTheReading() throws Exception {
        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedException.class,
                () -> new JcmdHeapReader(Duration.ofSeconds(30)).liveBytes(ProcessHandle.current().pid()));
        } finally {
            Thread.interrupted();
        }
        for (final ProcessHandle child : ProcessHandle.current().children().toList()) {
            child.onExit().get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("a short soak of this JVM takes three samples it can read")
    @Slow
    void aShortSoakOfThisJvmReadsThreeSamples() throws Exception {
        HeapSoak.Result result = HeapSoak.of(ProcessHandle.current().pid()).interval(Duration.ofSeconds(1))
            .jcmdLimit(Duration.ofSeconds(20)).run(Duration.ofSeconds(2));

        assertEquals(3, result.samples().size());
        assertTrue(result.samples().stream().allMatch(s -> s.bytes() > 0), result.samples().toString());
        assertTrue(Double.isFinite(result.slopeMegabytesPerMinute()));
    }
}
