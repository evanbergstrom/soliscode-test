/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.util;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [IterableTestUtils#equals(Iterable, Iterable)], each way it can end (#68).
@DisplayName("IterableTestUtils.equals")
class IterableTestUtilsEqualsTest {

    /// Default constructor.
    IterableTestUtilsEqualsTest() {
        super();
    }

    @Test
    @DisplayName("iterables with equal elements in the same order are equal")
    void equalElementsInOrderAreEqual() {
        assertTrue(IterableTestUtils.equals(List.of(1, 2, 3), IterableTestUtils.asIterable(new Integer[] {1, 2, 3})));
    }

    @Test
    @DisplayName("two empty iterables are equal")
    void emptyIterablesAreEqual() {
        assertTrue(IterableTestUtils.equals(List.of(), IterableTestUtils.empty()));
    }

    @Test
    @DisplayName("iterables that differ at a position are not equal")
    void elementsThatDifferAreNotEqual() {
        assertFalse(IterableTestUtils.equals(List.of(1, 2, 3), List.of(1, 4, 3)));
        assertFalse(IterableTestUtils.equals(List.of(1, 2), List.of(2, 1)));
    }

    @Test
    @DisplayName("an iterable that ends first is not equal to a longer one, either way round")
    void shorterIterableIsNotEqual() {
        assertFalse(IterableTestUtils.equals(List.of(1, 2), List.of(1, 2, 3)));
        assertFalse(IterableTestUtils.equals(List.of(1, 2, 3), List.of(1, 2)));
        assertFalse(IterableTestUtils.equals(List.of(), List.of(1)));
    }
}
