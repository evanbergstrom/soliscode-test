/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.performance.AllocationAssertion;
import com.soliscode.test.performance.LatencyRecorder;

/// How to write a performance test: an allocation bound, checked in an ordinary test.
class RecorderAllocationTest {

    /// Less than the smallest object, 16 bytes, and more than the measurement's noise, a fraction of a byte a run:
    /// a bound of 0 can fail on that noise alone.
    private static final double LESS_THAN_ONE_OBJECT = 1;

    @Test
    @DisplayName("Recording a latency allocates nothing")
    void recordingALatencyAllocatesNothing() throws Exception {
        LatencyRecorder recorder = LatencyRecorder.upTo(Duration.ofSeconds(1));
        AllocationAssertion.assertAllocatesAtMost(LESS_THAN_ONE_OBJECT, 10_000, () -> recorder.record(1_000));
    }
}
