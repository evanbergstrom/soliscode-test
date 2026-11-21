/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.listiterator;

import java.util.List;
import java.util.ListIterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.IteratorContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [hasPrevious()][ListIterator#hasPrevious()] method of a [ListIterator]: it is true where there is
/// an element before the cursor, false at the start.
///
/// @param <I> the type of the list iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see ListIterator#hasPrevious()
public interface HasPreviousContract<I extends ListIterator<E>, E> extends IteratorContractSupport<I, E> {

    /// Tests that `hasPrevious()` is false at the start, over an empty source and over one with elements.
    @Test
    @DisplayName("hasPrevious() returns false at the start")
    default void hasPreviousAtStartReturnsFalse() {
        assertFalse(sourceOf(List.of()).iterator().hasPrevious(), "over an empty source");
        assertFalse(sourceOf(uniqueElements(DEFAULT_SIZE)).iterator().hasPrevious(), "over a source with elements");
    }

    /// Tests that `hasPrevious()` is true after each `next()`.
    @Test
    @DisplayName("hasPrevious() returns true after next()")
    default void hasPreviousAfterNextReturnsTrue() {
        ListIterator<E> iterator = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            iterator.next();
            assertTrue(iterator.hasPrevious(), "hasPrevious() after element " + i);
        }
    }

    /// Tests that, walking back from the end, `hasPrevious()` is true until the start.
    @Test
    @DisplayName("hasPrevious() returns true walking back, until the start")
    default void hasPreviousWalkingBackReturnsTrueUntilTheStart() {
        ListIterator<E> iterator = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            iterator.next();
        }
        for (int i = DEFAULT_SIZE - 1; i >= 0; i--) {
            assertTrue(iterator.hasPrevious(), "hasPrevious() before going back to element " + i);
            iterator.previous();
        }
        assertFalse(iterator.hasPrevious(), "hasPrevious() back at the start");
    }
}
