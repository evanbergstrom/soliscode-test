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
package com.soliscode.test.contract.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

/// Support for the contracts of sorted sets.
///
/// A test builds the set from known elements, then compares what the set returns with what a [TreeSet] holding the
/// same elements and the set's comparator returns. The expected results so come from the JDK's implementation, not
/// the one under test; only the order comes from the set, through its `comparator()`, which
/// [com.soliscode.test.contract.sortedset.ComparatorContract] checks against the set's actual order.
///
/// @param <S> the type of sorted set
/// @param <E> the type of elements
/// @since 0.1.2
public interface SortedSetContractSupport<S extends SortedSet<E>, E> extends CollectionContractSupport<S, E> {

    /// The index, in the set's order, of the element [#viewFixture()] leaves out of the set.
    int MISSING_INDEX = 4;

    /// Returns a [TreeSet] with the elements and the set's comparator: the expected results for a sorted set.
    ///
    /// @param set the sorted set under test
    /// @param elements the elements it holds
    /// @return the reference set
    default NavigableSet<E> sortedReference(final SortedSet<E> set, final Collection<E> elements) {
        TreeSet<E> reference = new TreeSet<>(set.comparator());
        reference.addAll(elements);
        return reference;
    }

    /// Returns the elements to look up: every element of `elements`, then two that are not in it.
    ///
    /// @param elements the elements the set holds
    /// @return the elements to look up
    default List<E> probes(final List<E> elements) {
        List<E> probes = new ArrayList<>(elements);
        for (int i = 0; i < 2; i++) {
            probes.add(elementProvider().createInstanceNotIn(probes));
        }
        return probes;
    }

    /// Returns the order of a sorted set as a comparator: its `comparator()`, or natural order if that is `null`.
    ///
    /// @param set the sorted set
    /// @return the comparator of its order
    default Comparator<? super E> ordering(final SortedSet<E> set) {
        // reverseOrder(null) is natural reverse order, and reversing a reversed comparator gives the original back,
        // so this is natural order for a null comparator without an unchecked cast.
        return Collections.reverseOrder(Collections.reverseOrder(set.comparator()));
    }

    /// Returns a set for the view tests: [#DEFAULT_SIZE] unique elements less one, the one at [#MISSING_INDEX] in
    /// the set's order. That element is absent and inside the range of any view around it.
    ///
    /// @return the set and its elements
    default ViewFixture<S, E> viewFixture() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S probe = provider().createInstance(values);
        List<E> sorted = new ArrayList<>(sortedReference(probe, values));
        E missing = sorted.get(MISSING_INDEX);
        List<E> held = new ArrayList<>(values);
        held.remove(missing);
        return new ViewFixture<>(provider().createInstance(held), sorted, missing);
    }

    /// A sorted set for the view tests, with every element in its order, the one left out included.
    ///
    /// @param set the set, holding every element of `sorted` but `missing`
    /// @param sorted every element, in the set's order
    /// @param missing the element left out of the set, at [#MISSING_INDEX] in `sorted`
    /// @param <S> the type of sorted set
    /// @param <E> the type of elements
    record ViewFixture<S, E>(S set, List<E> sorted, E missing) { }
}
