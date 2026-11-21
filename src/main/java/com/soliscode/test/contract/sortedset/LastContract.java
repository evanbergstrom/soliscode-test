/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.sortedset;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SortedSetContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [last()][SortedSet#last()] method of [SortedSet].
///
/// @param <S> the type of sorted set
/// @param <E> the type of elements
/// @see java.util.SortedSet#last()
/// @since 0.1.2
public interface LastContract<S extends SortedSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [last()][SortedSet#last()] throws [NoSuchElementException] for an empty set.
    @Test
    @DisplayName("last() throws NoSuchElementException for an empty set")
    default void lastWhenEmptyThrowsException() {
        S set = provider().emptyInstance();
        assertThrows(NoSuchElementException.class, set::last);
    }

    /// Tests that [last()][SortedSet#last()] returns the last element in the set's order, as a [java.util.TreeSet]
    /// with the same elements and comparator does.
    @Test
    @DisplayName("last() returns the last element in the set's order")
    default void lastWhenNotEmptyReturnsLastElement() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        assertEquals(sortedReference(set, values).last(), set.last());
    }
}
