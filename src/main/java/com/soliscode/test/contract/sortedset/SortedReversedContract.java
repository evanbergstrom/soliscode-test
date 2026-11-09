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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.sequencedcollection.ReversedContract;
import com.soliscode.test.contract.sequencedcollection.SequencedCollectionMethods;
import com.soliscode.test.contract.support.SortedSetContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [reversed()][SortedSet#reversed()] method of [SortedSet]: a reverse-ordered view that is a
/// [SortedSet] itself.
///
/// It does not test that later changes to the set show in the view: [java.util.SequencedCollection#reversed()] lets
/// them show or not.
///
/// @param <S> the type of sorted set
/// @param <E> the type of elements
/// @see SortedSet#reversed()
/// @since 0.1.2
public interface SortedReversedContract<S extends SortedSet<E>, E>
        extends ReversedContract<S, E>, SortedSetContractSupport<S, E> {

    /// Tests that [reversed()][SortedSet#reversed()] returns the elements in reverse order, as a sorted set whose
    /// first element is the set's last, whose last is the set's first and whose comparator orders the other way, and
    /// that the set keeps its own order.
    @Override
    @Test
    @DisplayName("reversed() returns a reverse-ordered sorted view of the set")
    default void reversedWhenCalledReturnsReversedView() {
        if (supportsMethod(SequencedCollectionMethods.REVERSED)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            S set = provider().createInstance(values);
            List<E> ascending = new ArrayList<>(sortedReference(set, values));
            List<E> descending = new ArrayList<>(ascending.reversed());

            SortedSet<E> reversed = set.reversed();
            assertIterableEquals(descending, reversed, "reversed() does not iterate in reverse order");
            assertEquals(descending.getFirst(), reversed.first());
            assertEquals(descending.getLast(), reversed.last());
            Comparator<? super E> order = ordering(reversed);
            for (int i = 1; i < descending.size(); i++) {
                E before = descending.get(i - 1);
                E after = descending.get(i);
                assertTrue(order.compare(before, after) < 0,
                        () -> "the reversed view's comparator does not put " + before + " before " + after);
            }
            assertIterableEquals(ascending, set, "reversed() changed the set's order");
        }
    }
}
