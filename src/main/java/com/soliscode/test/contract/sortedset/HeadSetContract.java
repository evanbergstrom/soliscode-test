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
package com.soliscode.test.contract.sortedset;

import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.collection.CollectionMethods;
import com.soliscode.test.contract.support.SortedSetContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [headSet(E)][SortedSet#headSet(Object)] method of [SortedSet].
///
/// The tests that change the set or the view respect the optional operations: an addition through the view runs
/// only where [CollectionMethods#ADD] is supported, a removal from the set only where [CollectionMethods#REMOVE] is.
/// It reads the Javadoc more loosely in three ways, none of which it tests: the [IllegalArgumentException] a view's
/// own range views throw for an element outside its range, the [NullPointerException] for a `null` endpoint, and
/// the [ClassCastException] for an endpoint that cannot be compared.
///
/// @param <S> the type of sorted set
/// @param <E> the type of elements
/// @see java.util.SortedSet#headSet(Object)
/// @since 0.1.2
public interface HeadSetContract<S extends SortedSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [headSet(E)][SortedSet#headSet(Object)] returns the elements before the element, in the set's order,
    /// as a [TreeSet][java.util.TreeSet] with the same elements and comparator does.
    @Test
    @DisplayName("headSet(E) returns the elements before the element")
    default void headSetWhenCalledReturnsElementsBefore() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        NavigableSet<E> expected = sortedReference(set, values);
        for (final E element : probes(values)) {
            assertIterableEquals(expected.headSet(element), set.headSet(element), () -> "headSet(" + element + ")");
        }
    }

    /// Tests that the view from [headSet(E)][SortedSet#headSet(Object)] is backed by the set: an element added to the
    /// view is in the set, and one removed from the set leaves the view.
    @Test
    @DisplayName("headSet(E) returns a view that writes through to the set")
    default void headSetWhenModifiedWritesThrough() {
        ViewFixture<S, E> fixture = viewFixture();
        S set = fixture.set();
        List<E> sorted = fixture.sorted();
        SortedSet<E> view = set.headSet(sorted.get(MISSING_INDEX + 2));
        if (supportsMethod(CollectionMethods.ADD)) {
            assertTrue(view.add(fixture.missing()), "the view did not add an element in its range");
            assertTrue(set.contains(fixture.missing()), "an element added to the view is not in the set");
        }
        if (supportsMethod(CollectionMethods.REMOVE)) {
            E removed = sorted.get(0);
            assertTrue(view.contains(removed), "the view does not hold an element in its range");
            assertTrue(set.remove(removed));
            assertFalse(view.contains(removed), "an element removed from the set is still in the view");
        }
    }

    /// Tests that the view from [headSet(E)][SortedSet#headSet(Object)] throws [IllegalArgumentException] for an
    /// element added outside its range.
    @Test
    @DisplayName("headSet(E)'s view refuses an element outside its range")
    default void headSetWhenAddingOutOfRangeThrowsIllegalArgumentException() {
        if (supportsMethod(CollectionMethods.ADD)) {
            ViewFixture<S, E> fixture = viewFixture();
            List<E> sorted = fixture.sorted();
            SortedSet<E> view = fixture.set().headSet(sorted.get(MISSING_INDEX + 2));
            E outside = sorted.get(MISSING_INDEX + 2);
            assertThrows(IllegalArgumentException.class, () -> view.add(outside));
        }
    }
}
