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
package com.soliscode.test.contract.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.iterable.IterableMethods;
import com.soliscode.test.contract.support.IteratorContractSupport;
import com.soliscode.test.provider.IteratorSource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the optional [remove()][Iterator#remove()] method of an [Iterator]: it removes from the source the
/// last element `next()` returned, once per call to `next()`, and throws [IllegalStateException] otherwise, or
/// [UnsupportedOperationException] where it is not supported ([IterableMethods#ITERATOR_REMOVE]).
///
/// It does not check that an unsupported `remove()` leaves the source as it was: "performs no other action" is the
/// default implementation's specification, not the interface's. Nor does it call `remove()` after
/// `forEachRemaining`, which [Iterator#remove()] leaves unspecified.
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Iterator#remove()
public interface RemoveContract<I extends Iterator<E>, E> extends IteratorContractSupport<I, E> {

    /// Tests that `remove()` removes the element `next()` returned last, and that carrying on returns the elements not
    /// yet returned: it removes every other element.
    @Test
    @DisplayName("remove() removes the last element next() returned, and the iteration carries on")
    default void removeRemovesLastElementReturned() {
        if (!supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        Iterator<E> iterator = source.iterator();
        List<E> returned = new ArrayList<>();
        List<E> kept = new ArrayList<>();
        while (iterator.hasNext()) {
            assertTrueWithinLimit(returned.size());
            E e = iterator.next();
            if (returned.size() % 2 == 0) {
                iterator.remove();
            } else {
                kept.add(e);
            }
            returned.add(e);
        }
        assertSameElements(elements, returned, source, "the elements returned");
        assertSourceHolds(kept, source);
    }

    /// Tests that removing each element as it is returned leaves the source empty.
    @Test
    @DisplayName("remove() after each next() empties the source")
    default void removeAfterEachNextEmptiesTheSource() {
        if (!supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
            return;
        }
        IteratorSource<I, E> source = sourceOf(uniqueElements(DEFAULT_SIZE));
        Iterator<E> iterator = source.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            assertTrueWithinLimit(count++);
            iterator.next();
            iterator.remove();
        }
        assertSourceHolds(List.of(), source);
    }

    /// Tests that `remove()` before any `next()` throws [IllegalStateException].
    @Test
    @DisplayName("remove() before next() throws IllegalStateException")
    default void removeBeforeNextThrowsIllegalStateException() {
        if (!supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        Iterator<E> iterator = source.iterator();
        assertThrows(IllegalStateException.class, iterator::remove);
        assertSourceHolds(elements, source);
    }

    /// Tests that a second `remove()` after one `next()` throws [IllegalStateException], and removes nothing more.
    @Test
    @DisplayName("remove() twice after one next() throws IllegalStateException")
    default void removeTwiceThrowsIllegalStateException() {
        if (!supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
            return;
        }
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        Iterator<E> iterator = source.iterator();
        E first = iterator.next();
        iterator.remove();
        assertThrows(IllegalStateException.class, iterator::remove);
        List<E> rest = new ArrayList<>(elements);
        rest.remove(first);
        assertSourceHolds(rest, source);
    }

    /// Tests that an unsupported `remove()`, after `next()`, throws [UnsupportedOperationException].
    @Test
    @DisplayName("remove() throws UnsupportedOperationException where it is not supported")
    default void removeWhenNotSupportedThrowsUnsupportedOperationException() {
        if (supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
            return;
        }
        Iterator<E> iterator = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    private static void assertTrueWithinLimit(final int count) {
        assertTrue(count < TRAVERSAL_LIMIT, "the iterator did not end");
    }
}
