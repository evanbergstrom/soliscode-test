/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

/// The test side of the SolisCode performance framework: what a scenario of soliscode-parent's performance gate
/// (`com.soliscode.performance.PerformanceScenario`) is measured with.
///
/// - [LatencyRecorder]: latencies in a pre-sized histogram, reported as percentiles.
/// - [OpenLoopPacer]: operations at a fixed rate, each timed from its scheduled send.
/// - [Sweep]: the points of a parameter matrix, one case each.
/// - [AllocationAssertion]: the bytes an operation allocates, for a test.
/// - [ProcessTopology]: processes started from jars, in order, each ready before the next.
/// - [StageTaps]: the latency of each stage of a pipeline, correlated by an id.
/// - [HeapSoak]: the slope of a process's live heap over a long run.
///
/// Each gives what it measured as `com.soliscode.performance.ScenarioMetric`s, which a scenario returns in its
/// `ScenarioResult`. Inside a scenario a failure is an exception, which the gate's scenario JVM reports; only
/// [AllocationAssertion], which is for tests, fails with an assertion error.
///
/// @since 0.1.6
@NullMarked
package com.soliscode.test.performance;

import org.jspecify.annotations.NullMarked;
