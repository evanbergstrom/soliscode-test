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

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [previousIndex()][ListIterator#previousIndex()] method of a [ListIterator]: the index of the
/// element `previous` would return, or -1 at the start.
///
/// @param <I> the type of the list iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see ListIterator#previousIndex()
public interface PreviousIndexContract<I extends ListIterator<E>, E> extends IteratorContractSupport<I, E> {

    /// Tests that `previousIndex()` is -1 at the start.
    @Test
    @DisplayName("previousIndex() returns -1 at the start")
    default void previousIndexAtStartReturnsMinusOne() {
        assertEquals(-1, sourceOf(List.of()).iterator().previousIndex(), "over an empty source");
        assertEquals(-1, sourceOf(uniqueElements(DEFAULT_SIZE)).iterator().previousIndex(),
                "over a source with elements");
    }

    /// Tests that `previousIndex()` is one less than the cursor's position, forward to the end and back.
    @Test
    @DisplayName("previousIndex() returns one less than the cursor's position, forward and back")
    default void previousIndexFollowsTheCursor() {
        ListIterator<E> iterator = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            iterator.next();
            assertEquals(i, iterator.previousIndex(), "previousIndex() after next() returned element " + i);
        }
        for (int i = DEFAULT_SIZE - 1; i >= 0; i--) {
            assertEquals(i, iterator.previousIndex(), "previousIndex() before previous() for element " + i);
            iterator.previous();
        }
        assertEquals(-1, iterator.previousIndex(), "previousIndex() back at the start");
    }
}
