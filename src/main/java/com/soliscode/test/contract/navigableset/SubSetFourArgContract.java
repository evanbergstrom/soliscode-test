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

import java.util.ArrayList;
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

/// Contract for the [subSet(E, boolean, E, boolean)][NavigableSet#subSet(Object, boolean, Object, boolean)] method
/// of [NavigableSet].
///
/// The tests that change the set or the view respect the optional operations: an addition through the view runs
/// only where [CollectionMethods#ADD] is supported, a removal from the set only where [CollectionMethods#REMOVE] is.
/// It reads the Javadoc more loosely in three ways, none of which it tests: the [IllegalArgumentException] a view's
/// own range views throw for an element outside its range, the [NullPointerException] for a `null` endpoint, and
/// the [ClassCastException] for an endpoint that cannot be compared.
///
/// @param <S> the type of navigable set
/// @param <E> the type of elements
/// @see java.util.NavigableSet#subSet(Object, boolean, Object, boolean)
/// @since 0.1.2
public interface SubSetFourArgContract<S extends NavigableSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [subSet(E, boolean, E, boolean)][NavigableSet#subSet(Object, boolean, Object, boolean)] returns the
    /// elements between the two, each bound included where its flag is `true`, in the set's order, as a
    /// [TreeSet][java.util.TreeSet] with the same elements and comparator does, for every pair of elements in order
    /// and each of the four combinations of flags.
    @Test
    @DisplayName("subSet(E, boolean, E, boolean) returns the elements between the elements")
    default void subSetFourArgWhenCalledReturnsElementsBetween() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        NavigableSet<E> expected = sortedReference(set, values);
        List<E> sorted = new ArrayList<>(expected);
        boolean[] flags = {true, false};
        for (int i = 0; i < sorted.size(); i++) {
            for (int j = i; j < sorted.size(); j++) {
                E from = sorted.get(i);
                E to = sorted.get(j);
                for (final boolean fromInclusive : flags) {
                    for (final boolean toInclusive : flags) {
                        assertIterableEquals(expected.subSet(from, fromInclusive, to, toInclusive),
                                set.subSet(from, fromInclusive, to, toInclusive),
                                () -> "subSet(" + from + ", " + fromInclusive + ", " + to + ", " + toInclusive + ")");
                    }
                }
            }
        }
    }

    /// Tests that [subSet(E, boolean, E, boolean)][NavigableSet#subSet(Object, boolean, Object, boolean)] throws
    /// [IllegalArgumentException] when the first element comes after the second in the set's order.
    @Test
    @DisplayName("subSet(E, boolean, E, boolean) throws IllegalArgumentException when the first element is after the "
            + "second")
    default void subSetFourArgWhenFromAfterToThrowsIllegalArgumentException() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        List<E> sorted = new ArrayList<>(sortedReference(set, values));
        E from = sorted.get(MISSING_INDEX + 1);
        E to = sorted.get(MISSING_INDEX - 1);
        assertThrows(IllegalArgumentException.class, () -> set.subSet(from, true, to, true));
    }

    /// Tests that a view from [subSet(E, boolean, E, boolean)][NavigableSet#subSet(Object, boolean, Object, boolean)]
    /// with an inclusive upper bound is backed by the set: its bound, added to the view, is in the set, and an
    /// element removed from the set leaves the view.
    @Test
    @DisplayName("subSet(E, false, E, true) returns a view that holds its upper bound and writes through to the set")
    default void subSetFourArgWhenModifiedWritesThrough() {
        ViewFixture<S, E> fixture = viewFixture();
        S set = fixture.set();
        List<E> sorted = fixture.sorted();
        NavigableSet<E> view = set.subSet(sorted.get(MISSING_INDEX - 2), false, fixture.missing(), true);
        if (supportsMethod(CollectionMethods.ADD)) {
            assertTrue(view.add(fixture.missing()), "the view did not add its inclusive bound");
            assertTrue(set.contains(fixture.missing()), "an element added to the view is not in the set");
        }
        if (supportsMethod(CollectionMethods.REMOVE)) {
            E removed = sorted.get(MISSING_INDEX - 1);
            assertTrue(view.contains(removed), "the view does not hold an element in its range");
            assertTrue(set.remove(removed));
            assertFalse(view.contains(removed), "an element removed from the set is still in the view");
        }
    }

    /// Tests that a view from [subSet(E, boolean, E, boolean)][NavigableSet#subSet(Object, boolean, Object, boolean)]
    /// with an exclusive lower bound throws [IllegalArgumentException] when that bound is added.
    @Test
    @DisplayName("subSet(E, false, E, true)'s view refuses its exclusive lower bound")
    default void subSetFourArgWhenAddingOutOfRangeThrowsIllegalArgumentException() {
        if (supportsMethod(CollectionMethods.ADD)) {
            ViewFixture<S, E> fixture = viewFixture();
            List<E> sorted = fixture.sorted();
            NavigableSet<E> view = fixture.set().subSet(fixture.missing(), false, sorted.get(MISSING_INDEX + 2), true);
            assertThrows(IllegalArgumentException.class, () -> view.add(fixture.missing()));
        }
    }
}
