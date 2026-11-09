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
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SortedSetContractSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [comparator()][SortedSet#comparator()] method of [SortedSet].
///
/// It reads [SortedSet#comparator()] more loosely in one way: a test sees only the order, so it cannot tell a
/// `null` comparator from one that orders the elements as natural order does.
///
/// @param <S> the type of sorted set
/// @param <E> the type of elements
/// @see java.util.SortedSet#comparator()
/// @since 0.1.2
public interface ComparatorContract<S extends SortedSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [comparator()][SortedSet#comparator()] describes the set's order: each element, in iteration
    /// order, comes before the next by that comparator (by natural order, for `null`).
    @Test
    @DisplayName("comparator() describes the order of the elements")
    default void comparatorWhenCalledDescribesElementOrder() {
        S set = provider().createInstance(elementProvider().createUniqueInstances(DEFAULT_SIZE));
        Comparator<? super E> order = ordering(set);
        List<E> elements = new ArrayList<>();
        Iterator<E> iterator = set.iterator();
        while (iterator.hasNext() && elements.size() <= TRAVERSAL_LIMIT) {
            elements.add(iterator.next());
        }
        for (int i = 1; i < elements.size(); i++) {
            E before = elements.get(i - 1);
            E after = elements.get(i);
            assertTrue(order.compare(before, after) < 0,
                    () -> "comparator() does not put " + before + " before " + after + ", as the set does");
        }
    }
}
