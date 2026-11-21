/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests of [Sweep].
@DisplayName("Sweep")
class SweepTest extends AbstractTest {

    @Test
    @DisplayName("points() is the cross product, the first parameter changing slowest")
    void pointsAreTheCrossProductInOrder() {
        List<Map<String, String>> points = Sweep.of("threads", 1, 4, 8).and("rate", "1000", "10000").points();

        assertEquals(List.of(
            Map.of("threads", "1", "rate", "1000"), Map.of("threads", "1", "rate", "10000"),
            Map.of("threads", "4", "rate", "1000"), Map.of("threads", "4", "rate", "10000"),
            Map.of("threads", "8", "rate", "1000"), Map.of("threads", "8", "rate", "10000")), points);
        assertEquals(List.of("threads", "rate"), List.copyOf(points.get(0).keySet()));
    }

    @Test
    @DisplayName("each point is unmodifiable")
    void eachPointIsUnmodifiable() {
        Map<String, String> point = Sweep.of("size", 1).points().get(0);

        assertThrows(UnsupportedOperationException.class, () -> point.put("size", "2"));
    }

    @Test
    @DisplayName("where() drops the points its predicate refuses, and points() refuses none left")
    void whereDropsPointsAndRefusesDroppingAll() {
        Sweep sweep = Sweep.of("subscribers", 1, 10).and("rate", 100, 1_000)
            .where(p -> !(Sweep.intParameter(p, "subscribers") == 10 && Sweep.intParameter(p, "rate") == 1_000));

        assertEquals(3, sweep.points().size());
        assertThrows(IllegalStateException.class, () -> sweep.where(p -> false).points());
    }

    @Test
    @DisplayName("a parameter named twice, a blank name and a value given twice are refused")
    void refusesDuplicateNamesBlankNamesAndDuplicateValues() {
        assertThrows(IllegalArgumentException.class, () -> Sweep.of("threads", 1).and("threads", 2));
        assertThrows(IllegalArgumentException.class, () -> Sweep.of(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> Sweep.of("threads", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> Sweep.of("mode", "a").and("x", "b", "b"));
    }

    @Test
    @DisplayName("parameter() and intParameter() read a point, and refuse a missing or non-numeric parameter")
    void parametersAreReadBack() {
        Map<String, String> point = Sweep.of("mode", "fast").and("threads", 4).points().get(0);

        assertEquals("fast", Sweep.parameter(point, "mode"));
        assertEquals(4, Sweep.intParameter(point, "threads"));
        assertThrows(IllegalArgumentException.class, () -> Sweep.parameter(point, "rate"));
        assertThrows(IllegalArgumentException.class, () -> Sweep.intParameter(point, "mode"));
    }
}
