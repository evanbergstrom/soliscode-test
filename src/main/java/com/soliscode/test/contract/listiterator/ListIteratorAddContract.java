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
package com.soliscode.test.contract.listiterator;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.IteratorContractSupport;
import com.soliscode.test.provider.IteratorSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the optional [add(Object)][ListIterator#add(Object)] method of a [ListIterator]: it inserts the
/// element before the cursor, so that `next` is unaffected, `previous` returns the new element, and `nextIndex` and
/// `previousIndex` rise by one; it throws [UnsupportedOperationException] where it is not supported
/// ([ListIteratorMethods#LIST_ITERATOR_ADD]).
///
/// It does not test [ClassCastException] or [IllegalArgumentException]: no element a generic test could pick is sure
/// to be refused.
///
/// @param <I> the type of the list iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see ListIterator#add(Object)
public interface ListIteratorAddContract<I extends ListIterator<E>, E> extends IteratorContractSupport<I, E> {

    /// The number of elements ({@value}) the add tests move past first.
    int ADD_INDEX = 4;

    /// Tests that `add` to an empty source makes the new element its sole element, before the cursor.
    @Test
    @DisplayName("ListIterator.add() to an empty source makes the sole element")
    default void addToEmptySourceMakesTheSoleElement() {
        if (!supportsMethod(ListIteratorMethods.LIST_ITERATOR_ADD)) {
            return;
        }
        IteratorSource<I, E> source = sourceOf(List.of());
        ListIterator<E> iterator = source.iterator();
        E added = elementProvider().createInstance();
        iterator.add(added);
        assertFalse(iterator.hasNext(), "hasNext() after add()");
        assertEquals(added, iterator.previous(), "previous() after add()");
        assertSourceHolds(List.of(added), source);
    }

    /// Tests that `add` inserts before the cursor: `next()` returns what it would have, the indexes rise by one, and
    /// the source holds the new element at the cursor's position.
    @Test
    @DisplayName("ListIterator.add() inserts before the cursor, and next() is unaffected")
    default void addInsertsBeforeTheCursor() {
        if (!supportsMethod(ListIteratorMethods.LIST_ITERATOR_ADD)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        ListIterator<E> iterator = source.iterator();
        for (int i = 0; i < ADD_INDEX; i++) {
            iterator.next();
        }
        E added = elementProvider().createInstanceNotIn(elements);
        iterator.add(added);
        assertEquals(ADD_INDEX + 1, iterator.nextIndex(), "nextIndex() after add()");
        assertEquals(ADD_INDEX, iterator.previousIndex(), "previousIndex() after add()");
        assertEquals(elements.get(ADD_INDEX), iterator.next(), "next() after add()");
        List<E> expected = new ArrayList<>(elements);
        expected.add(ADD_INDEX, added);
        assertSourceHolds(expected, source);
    }

    /// Tests that `previous()` after `add` returns the new element.
    @Test
    @DisplayName("ListIterator.add() then previous() returns the new element")
    default void addThenPreviousReturnsTheNewElement() {
        if (!supportsMethod(ListIteratorMethods.LIST_ITERATOR_ADD)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        ListIterator<E> iterator = sourceOf(elements).iterator();
        for (int i = 0; i < ADD_INDEX; i++) {
            iterator.next();
        }
        E added = elementProvider().createInstanceNotIn(elements);
        iterator.add(added);
        assertEquals(added, iterator.previous());
    }

    /// Tests that an unsupported `add` throws [UnsupportedOperationException].
    @Test
    @DisplayName("ListIterator.add() throws UnsupportedOperationException where it is not supported")
    default void listIteratorAddWhenNotSupportedThrowsUnsupportedOperationException() {
        if (supportsMethod(ListIteratorMethods.LIST_ITERATOR_ADD)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        ListIterator<E> iterator = sourceOf(elements).iterator();
        iterator.next();
        E added = elementProvider().createInstanceNotIn(elements);
        assertThrows(UnsupportedOperationException.class, () -> iterator.add(added));
    }
}
