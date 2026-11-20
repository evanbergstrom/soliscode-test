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
