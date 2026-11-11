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
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.IteratorContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [previous()][ListIterator#previous()] method of a [ListIterator]: it returns the element before
/// the cursor and moves the cursor back, so that alternating calls to `next` and `previous` return the same element,
/// and it throws [NoSuchElementException] at the start.
///
/// @param <I> the type of the list iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see ListIterator#previous()
public interface PreviousContract<I extends ListIterator<E>, E> extends IteratorContractSupport<I, E> {

    /// Tests that walking back from the end returns the elements in reverse order.
    @Test
    @DisplayName("previous() from the end returns the elements in reverse order")
    default void previousFromTheEndReturnsTheElementsReversed() {
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        ListIterator<E> iterator = sourceOf(elements).iterator();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            iterator.next();
        }
        List<E> returned = new ArrayList<>();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            returned.add(iterator.previous());
        }
        assertEquals(elements.reversed(), returned);
    }

    /// Tests that `previous()` after `next()` returns the same element, and `next()` after it returns it again.
    @Test
    @DisplayName("alternating next() and previous() return the same element")
    default void previousAfterNextReturnsTheSameElement() {
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        ListIterator<E> iterator = sourceOf(elements).iterator();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            E e = iterator.next();
            assertEquals(elements.get(i), e, "next() for element " + i);
            assertEquals(e, iterator.previous(), "previous() after next() for element " + i);
            assertEquals(e, iterator.next(), "next() after previous() for element " + i);
        }
    }

    /// Tests that `previous()` at the start throws [NoSuchElementException]: before any `next()`, and after walking
    /// back.
    @Test
    @DisplayName("previous() at the start throws NoSuchElementException")
    default void previousAtStartThrowsNoSuchElementException() {
        ListIterator<E> fresh = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        assertThrows(NoSuchElementException.class, fresh::previous, "before any next()");
        ListIterator<E> walked = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        walked.next();
        walked.previous();
        assertThrows(NoSuchElementException.class, walked::previous, "after walking back to the start");
    }
}
