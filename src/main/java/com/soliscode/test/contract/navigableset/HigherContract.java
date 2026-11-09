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

import com.soliscode.test.contract.support.SortedSetContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [higher(E)][NavigableSet#higher(Object)] method of [NavigableSet].
///
/// It reads the Javadoc more loosely in two ways, neither of which it tests: the [NullPointerException] for a `null`
/// element and the [ClassCastException] for one that cannot be compared.
///
/// @param <S> the type of navigable set
/// @param <E> the type of elements
/// @see java.util.NavigableSet#higher(Object)
/// @since 0.1.2
public interface HigherContract<S extends NavigableSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [higher(E)][NavigableSet#higher(Object)] returns the least element strictly after the element, or
    /// `null` if there is none, as a [TreeSet][java.util.TreeSet] with the same elements and comparator does, for each
    /// element and two the set does not hold.
    @Test
    @DisplayName("higher(E) returns the least element after the element")
    default void higherWhenCalledReturnsLeastElementAbove() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        NavigableSet<E> expected = sortedReference(set, values);
        for (final E element : probes(values)) {
            assertEquals(expected.higher(element), set.higher(element), () -> "higher(" + element + ")");
        }
    }
}
