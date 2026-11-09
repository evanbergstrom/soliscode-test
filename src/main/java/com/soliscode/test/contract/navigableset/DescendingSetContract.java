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
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [descendingSet()][NavigableSet#descendingSet()] method of [NavigableSet].
///
/// The test that changes the set or the view respects the optional operations: an addition through the view runs
/// only where [CollectionMethods#ADD] is supported, a removal from the set only where [CollectionMethods#REMOVE] is.
/// It reads the Javadoc more loosely in two ways, neither of which it tests: that the view's ordering is
/// `Collections.reverseOrder(comparator())`, and that `s.descendingSet().descendingSet()` is a view essentially
/// equivalent to `s`. It checks the view's iteration order, not its `comparator()`.
///
/// @param <S> the type of navigable set
/// @param <E> the type of elements
/// @see java.util.NavigableSet#descendingSet()
/// @since 0.1.2
public interface DescendingSetContract<S extends NavigableSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [descendingSet()][NavigableSet#descendingSet()] returns the elements in the reverse of the set's
    /// order, as a [TreeSet][java.util.TreeSet] with the same elements and comparator does.
    @Test
    @DisplayName("descendingSet() returns the elements in reverse order")
    default void descendingSetWhenCalledReturnsElementsInReverseOrder() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        assertIterableEquals(sortedReference(set, values).descendingSet(), set.descendingSet());
    }

    /// Tests that the view from [descendingSet()][NavigableSet#descendingSet()] is backed by the set ("The descending
    /// set is backed by this set"): an element added to the view is in the set, and one removed from the set leaves
    /// the view.
    @Test
    @DisplayName("descendingSet() returns a view that writes through to the set")
    default void descendingSetWhenModifiedWritesThrough() {
        ViewFixture<S, E> fixture = viewFixture();
        S set = fixture.set();
        NavigableSet<E> view = set.descendingSet();
        if (supportsMethod(CollectionMethods.ADD)) {
            assertTrue(view.add(fixture.missing()), "the view did not add an element the set does not hold");
            assertTrue(set.contains(fixture.missing()), "an element added to the view is not in the set");
        }
        if (supportsMethod(CollectionMethods.REMOVE)) {
            E removed = fixture.sorted().get(0);
            assertTrue(view.contains(removed), "the view does not hold an element of the set");
            assertTrue(set.remove(removed));
            assertFalse(view.contains(removed), "an element removed from the set is still in the view");
        }
    }
}
