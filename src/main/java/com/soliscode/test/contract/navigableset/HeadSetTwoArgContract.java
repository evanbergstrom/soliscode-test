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
package com.soliscode.test.contract.navigableset;

import java.util.List;
import java.util.NavigableSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.collection.CollectionMethods;
import com.soliscode.test.contract.support.SortedSetContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [headSet(E, boolean)][NavigableSet#headSet(Object, boolean)] method of [NavigableSet].
///
/// The tests that change the set or the view respect the optional operations: an addition through the view runs
/// only where [CollectionMethods#ADD] is supported, a removal from the set only where [CollectionMethods#REMOVE] is.
/// It reads the Javadoc more loosely in three ways, none of which it tests: the [IllegalArgumentException] a view's
/// own range views throw for an element outside its range, the [NullPointerException] for a `null` endpoint, and
/// the [ClassCastException] for an endpoint that cannot be compared.
///
/// @param <S> the type of navigable set
/// @param <E> the type of elements
/// @see java.util.NavigableSet#headSet(Object, boolean)
/// @since 0.1.2
public interface HeadSetTwoArgContract<S extends NavigableSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [headSet(E, boolean)][NavigableSet#headSet(Object, boolean)] returns the elements before the element,
    /// and the element itself where `inclusive` is `true`, in the set's order, as a [TreeSet][java.util.TreeSet] with
    /// the same elements and comparator does, for each element, two the set does not hold, and both values of
    /// `inclusive`.
    @Test
    @DisplayName("headSet(E, boolean) returns the elements before the element, and it if inclusive")
    default void headSetTwoArgWhenCalledReturnsElementsBefore() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        NavigableSet<E> expected = sortedReference(set, values);
        for (final E element : probes(values)) {
            for (final boolean inclusive : new boolean[] {true, false}) {
                assertIterableEquals(expected.headSet(element, inclusive), set.headSet(element, inclusive),
                        () -> "headSet(" + element + ", " + inclusive + ")");
            }
        }
    }

    /// Tests that the inclusive view from [headSet(E, boolean)][NavigableSet#headSet(Object, boolean)] is backed by the
    /// set: its bound, added to the view, is in the set, and an element removed from the set leaves the view.
    @Test
    @DisplayName("headSet(E, true) returns a view that holds its bound and writes through to the set")
    default void headSetTwoArgWhenModifiedWritesThrough() {
        ViewFixture<S, E> fixture = viewFixture();
        S set = fixture.set();
        List<E> sorted = fixture.sorted();
        NavigableSet<E> view = set.headSet(fixture.missing(), true);
        if (supportsMethod(CollectionMethods.ADD)) {
            assertTrue(view.add(fixture.missing()), "the inclusive view did not add its bound");
            assertTrue(set.contains(fixture.missing()), "an element added to the view is not in the set");
        }
        if (supportsMethod(CollectionMethods.REMOVE)) {
            E removed = sorted.get(0);
            assertTrue(view.contains(removed), "the view does not hold an element in its range");
            assertTrue(set.remove(removed));
            assertFalse(view.contains(removed), "an element removed from the set is still in the view");
        }
    }

    /// Tests that the exclusive view from [headSet(E, boolean)][NavigableSet#headSet(Object, boolean)] throws
    /// [IllegalArgumentException] when its bound, which is outside its range, is added.
    @Test
    @DisplayName("headSet(E, false)'s view refuses its bound")
    default void headSetTwoArgWhenAddingOutOfRangeThrowsIllegalArgumentException() {
        if (supportsMethod(CollectionMethods.ADD)) {
            ViewFixture<S, E> fixture = viewFixture();
            NavigableSet<E> view = fixture.set().headSet(fixture.missing(), false);
            assertThrows(IllegalArgumentException.class, () -> view.add(fixture.missing()));
        }
    }
}
