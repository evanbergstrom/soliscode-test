/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// How to write a thread-safety test: [HitCounter] under contention, through a [ContentionCoordinator].
class HitCounterThreadSafetyTest {

    private static final int THREADS = 4;
    private static final int ITERATIONS = 10_000;

    @ConcurrencyTest
    @DisplayName("Hits counted on several threads at once are all counted")
    void hitsCountedConcurrentlyAreAllCounted() {
        ContentionCoordinator<HitCounter> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, ITERATIONS, HitCounter::hit);
        coordinator.add(THREADS, ITERATIONS, HitCounter::hits);

        HitCounter counter = new HitCounter();
        coordinator.execute(counter);

        assertEquals((long) THREADS * ITERATIONS, counter.hits());
    }
}
