/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.soliscode.performance.PerformanceScenario;
import com.soliscode.performance.ScenarioContext;
import com.soliscode.performance.ScenarioResult;
import com.soliscode.test.performance.OpenLoopPacer;
import com.soliscode.test.performance.PacedRun;
import com.soliscode.test.performance.Sweep;

/// How to write a performance test: a scenario for the parent's performance gate, which counts hits on a
/// [HitCounter] at each of two rates and reports the latencies' percentiles and the rate achieved.
public final class CounterScenario implements PerformanceScenario {

    private static final Duration WARMUP = Duration.ofSeconds(2);
    private static final Duration MEASURED = Duration.ofSeconds(10);

    @Override
    public String name() {
        return "counter";
    }

    @Override
    public List<Map<String, String>> points() {
        return Sweep.of("rate", 1_000, 10_000).points();
    }

    @Override
    public ScenarioResult run(final ScenarioContext context, final Map<String, String> point) throws Exception {
        HitCounter counter = new HitCounter();
        PacedRun run = OpenLoopPacer.at(Sweep.intParameter(point, "rate"))
                .warmup(WARMUP)
                .duration(MEASURED)
                .run(sequence -> counter.hit());
        return new ScenarioResult(run.metrics(), List.of(), List.of());
    }

    @Override
    public Duration estimate(final Map<String, String> point) {
        return WARMUP.plus(MEASURED);
    }
}
