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
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SortedSetContractSupport;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

/// Contract for the [descendingIterator()][NavigableSet#descendingIterator()] method of [NavigableSet].
///
/// @param <S> the type of navigable set
/// @param <E> the type of elements
/// @see java.util.NavigableSet#descendingIterator()
/// @since 0.1.2
public interface DescendingIteratorContract<S extends NavigableSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [descendingIterator()][NavigableSet#descendingIterator()] returns the elements in the reverse of the
    /// set's order, as a [TreeSet][java.util.TreeSet] with the same elements and comparator does.
    @Test
    @DisplayName("descendingIterator() returns the elements in reverse order")
    default void descendingIteratorWhenCalledReturnsElementsInReverseOrder() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        List<E> iterated = new ArrayList<>();
        Iterator<E> iterator = set.descendingIterator();
        while (iterator.hasNext() && iterated.size() <= TRAVERSAL_LIMIT) {
            iterated.add(iterator.next());
        }
        assertIterableEquals(sortedReference(set, values).descendingSet(), iterated,
                "descendingIterator() is not in the reverse of the set's order");
    }
}
