/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.listiterator;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.IteratorContractSupport;
import com.soliscode.test.methods.IterableMethods;
import com.soliscode.test.methods.ListIteratorMethods;
import com.soliscode.test.provider.IteratorSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the rules of [remove()][ListIterator#remove()] that are a [ListIterator]'s own: it removes the last
/// element `previous` returned, as it does `next`'s, once, and throws [IllegalStateException] after `add`.
/// [com.soliscode.test.contract.iterator.RemoveContract] tests the rest.
///
/// @param <I> the type of the list iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see ListIterator#remove()
public interface ListIteratorRemoveContract<I extends ListIterator<E>, E> extends IteratorContractSupport<I, E> {

    /// The number of elements ({@value}) the remove tests move past before going back one.
    int REMOVE_CURSOR = 5;

    /// Tests that `remove()` after `previous()` removes the element `previous()` returned, at an index above 0, and
    /// that the cursor is then where that element was.
    @Test
    @DisplayName("ListIterator.remove() after previous() removes the element previous() returned")
    default void removeAfterPreviousRemovesThatElement() {
        if (!supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        ListIterator<E> iterator = source.iterator();
        for (int i = 0; i < REMOVE_CURSOR; i++) {
            iterator.next();
        }
        int removed = REMOVE_CURSOR - 1;
        assertEquals(elements.get(removed), iterator.previous(), "previous()");
        iterator.remove();
        assertEquals(removed, iterator.nextIndex(), "nextIndex() after remove()");
        assertEquals(elements.get(REMOVE_CURSOR), iterator.next(), "next() after remove()");
        List<E> expected = new ArrayList<>(elements);
        expected.remove(removed);
        assertSourceHolds(expected, source);
    }

    /// Tests that a second `remove()` after one `previous()` throws [IllegalStateException].
    @Test
    @DisplayName("ListIterator.remove() twice after one previous() throws IllegalStateException")
    default void removeTwiceAfterPreviousThrowsIllegalStateException() {
        if (!supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        ListIterator<E> iterator = source.iterator();
        for (int i = 0; i < REMOVE_CURSOR; i++) {
            iterator.next();
        }
        iterator.previous();
        iterator.remove();
        assertThrows(IllegalStateException.class, iterator::remove);
        List<E> expected = new ArrayList<>(elements);
        expected.remove(REMOVE_CURSOR - 1);
        assertSourceHolds(expected, source);
    }

    /// Tests that `remove()` after `add` throws [IllegalStateException], and removes nothing.
    @Test
    @DisplayName("ListIterator.remove() after add() throws IllegalStateException")
    default void removeAfterAddThrowsIllegalStateException() {
        if (!supportsMethod(IterableMethods.ITERATOR_REMOVE)
                || !supportsMethod(ListIteratorMethods.LIST_ITERATOR_ADD)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        ListIterator<E> iterator = source.iterator();
        iterator.next();
        E added = elementProvider().createInstanceNotIn(elements);
        iterator.add(added);
        assertThrows(IllegalStateException.class, iterator::remove);
        List<E> expected = new ArrayList<>(elements);
        expected.add(1, added);
        assertSourceHolds(expected, source);
    }
}
