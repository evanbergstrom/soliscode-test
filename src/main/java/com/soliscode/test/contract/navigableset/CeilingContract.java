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

/// Contract for the [ceiling(E)][NavigableSet#ceiling(Object)] method of [NavigableSet].
///
/// It reads the Javadoc more loosely in two ways, neither of which it tests: the [NullPointerException] for a `null`
/// element and the [ClassCastException] for one that cannot be compared.
///
/// For an element the set holds, `floor` and `ceiling` both return it, so a `floor` that answers as `ceiling` does (or
/// the other way round) shows only for an element the set does not hold: the test relies on the two absent elements
/// [SortedSetContractSupport#probes(List)] adds.
///
/// @param <S> the type of navigable set
/// @param <E> the type of elements
/// @see java.util.NavigableSet#ceiling(Object)
/// @since 0.1.2
public interface CeilingContract<S extends NavigableSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [ceiling(E)][NavigableSet#ceiling(Object)] returns the least element equal to or after the element,
    /// or `null` if there is none, as a [TreeSet][java.util.TreeSet] with the same elements and comparator does, for
    /// each element and two the set does not hold.
    @Test
    @DisplayName("ceiling(E) returns the least element equal to or after the element")
    default void ceilingWhenCalledReturnsLeastElementAtOrAbove() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        NavigableSet<E> expected = sortedReference(set, values);
        for (final E element : probes(values)) {
            assertEquals(expected.ceiling(element), set.ceiling(element), () -> "ceiling(" + element + ")");
        }
    }
}
