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

import com.soliscode.test.contract.iterable.IterableMethods;
import com.soliscode.test.contract.support.IteratorContractSupport;
import com.soliscode.test.provider.IteratorSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the optional [set(Object)][ListIterator#set(Object)] method of a [ListIterator]: it replaces the last
/// element `next` or `previous` returned, and throws [IllegalStateException] if neither has been called since the
/// last `remove` or `add`, or [UnsupportedOperationException] where it is not supported
/// ([ListIteratorMethods#LIST_ITERATOR_SET]).
///
/// It does not test [ClassCastException] or [IllegalArgumentException]: no element a generic test could pick is sure
/// to be refused.
///
/// @param <I> the type of the list iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see ListIterator#set(Object)
public interface ListIteratorSetContract<I extends ListIterator<E>, E> extends IteratorContractSupport<I, E> {

    /// The number of elements ({@value}) the set tests move past first.
    int SET_INDEX = 3;

    /// Tests that `set` after `next()` replaces the element `next()` returned, and the iteration carries on.
    @Test
    @DisplayName("ListIterator.set() after next() replaces the element next() returned")
    default void setAfterNextReplacesTheElement() {
        if (!supportsMethod(ListIteratorMethods.LIST_ITERATOR_SET)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        ListIterator<E> iterator = source.iterator();
        for (int i = 0; i <= SET_INDEX; i++) {
            iterator.next();
        }
        E replacement = elementProvider().createInstanceNotIn(elements);
        iterator.set(replacement);
        assertEquals(elements.get(SET_INDEX + 1), iterator.next(), "next() after set()");
        List<E> expected = new ArrayList<>(elements);
        expected.set(SET_INDEX, replacement);
        assertSourceHolds(expected, source);
    }

    /// Tests that `set` after `previous()` replaces the element `previous()` returned.
    @Test
    @DisplayName("ListIterator.set() after previous() replaces the element previous() returned")
    default void setAfterPreviousReplacesTheElement() {
        if (!supportsMethod(ListIteratorMethods.LIST_ITERATOR_SET)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        ListIterator<E> iterator = source.iterator();
        for (int i = 0; i <= SET_INDEX; i++) {
            iterator.next();
        }
        iterator.previous();
        E replacement = elementProvider().createInstanceNotIn(elements);
        iterator.set(replacement);
        List<E> expected = new ArrayList<>(elements);
        expected.set(SET_INDEX, replacement);
        assertSourceHolds(expected, source);
    }

    /// Tests that `set` before any `next()` or `previous()` throws [IllegalStateException].
    @Test
    @DisplayName("ListIterator.set() before next() or previous() throws IllegalStateException")
    default void setBeforeNextThrowsIllegalStateException() {
        if (!supportsMethod(ListIteratorMethods.LIST_ITERATOR_SET)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        ListIterator<E> iterator = source.iterator();
        E replacement = elementProvider().createInstanceNotIn(elements);
        assertThrows(IllegalStateException.class, () -> iterator.set(replacement));
        assertSourceHolds(elements, source);
    }

    /// Tests that `set` after `remove()` throws [IllegalStateException].
    @Test
    @DisplayName("ListIterator.set() after remove() throws IllegalStateException")
    default void setAfterRemoveThrowsIllegalStateException() {
        if (!supportsMethod(ListIteratorMethods.LIST_ITERATOR_SET)
                || !supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        ListIterator<E> iterator = source.iterator();
        iterator.next();
        iterator.remove();
        E replacement = elementProvider().createInstanceNotIn(elements);
        assertThrows(IllegalStateException.class, () -> iterator.set(replacement));
        assertSourceHolds(elements.subList(1, DEFAULT_SIZE), source);
    }

    /// Tests that `set` after `add` throws [IllegalStateException].
    @Test
    @DisplayName("ListIterator.set() after add() throws IllegalStateException")
    default void setAfterAddThrowsIllegalStateException() {
        if (!supportsMethod(ListIteratorMethods.LIST_ITERATOR_SET)
                || !supportsMethod(ListIteratorMethods.LIST_ITERATOR_ADD)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        ListIterator<E> iterator = sourceOf(elements).iterator();
        iterator.next();
        List<E> excluded = new ArrayList<>(elements);
        E added = elementProvider().createInstanceNotIn(excluded);
        excluded.add(added);
        iterator.add(added);
        E replacement = elementProvider().createInstanceNotIn(excluded);
        assertThrows(IllegalStateException.class, () -> iterator.set(replacement));
    }

    /// Tests that an unsupported `set`, after `next()`, throws [UnsupportedOperationException].
    @Test
    @DisplayName("ListIterator.set() throws UnsupportedOperationException where it is not supported")
    default void setWhenNotSupportedThrowsUnsupportedOperationException() {
        if (supportsMethod(ListIteratorMethods.LIST_ITERATOR_SET)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        ListIterator<E> iterator = sourceOf(elements).iterator();
        iterator.next();
        E replacement = elementProvider().createInstanceNotIn(elements);
        assertThrows(UnsupportedOperationException.class, () -> iterator.set(replacement));
    }
}
