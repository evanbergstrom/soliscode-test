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
