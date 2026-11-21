/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
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
