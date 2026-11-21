/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.navigableset;

import java.util.List;
import java.util.NavigableSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SortedSetContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [lower(E)][NavigableSet#lower(Object)] method of [NavigableSet].
///
/// It reads the Javadoc more loosely in two ways, neither of which it tests: the [NullPointerException] for a `null`
/// element and the [ClassCastException] for one that cannot be compared.
///
/// @param <S> the type of navigable set
/// @param <E> the type of elements
/// @see java.util.NavigableSet#lower(Object)
/// @since 0.1.2
public interface LowerContract<S extends NavigableSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [lower(E)][NavigableSet#lower(Object)] returns the greatest element strictly before the element, or
    /// `null` if there is none, as a [TreeSet][java.util.TreeSet] with the same elements and comparator does, for each
    /// element and two the set does not hold.
    @Test
    @DisplayName("lower(E) returns the greatest element before the element")
    default void lowerWhenCalledReturnsGreatestElementBelow() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        NavigableSet<E> expected = sortedReference(set, values);
        for (final E element : probes(values)) {
            assertEquals(expected.lower(element), set.lower(element), () -> "lower(" + element + ")");
        }
    }
}
