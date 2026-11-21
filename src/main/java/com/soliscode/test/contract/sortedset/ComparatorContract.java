/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
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
