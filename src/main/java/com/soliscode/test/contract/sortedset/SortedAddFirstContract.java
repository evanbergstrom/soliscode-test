/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.sortedset;

import java.util.List;
import java.util.SortedSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.sequencedcollection.AddFirstContract;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [addFirst(E)][SortedSet#addFirst(Object)] method of [SortedSet]: it throws
/// [UnsupportedOperationException], since a sorted set's order is its comparator's.
///
/// It overrides each test of [AddFirstContract], so a test class need not mark `addFirst` unsupported.
///
/// @param <S> the type of sorted set
/// @param <E> the type of elements
/// @see SortedSet#addFirst(Object)
/// @since 0.1.2
public interface SortedAddFirstContract<S extends SortedSet<E>, E> extends AddFirstContract<S, E> {

    /// Tests that [addFirst(E)][SortedSet#addFirst(Object)] throws [UnsupportedOperationException] for an element.
    // DoNotCall: the test calls addFirst on a SortedSet on purpose, to check that it throws, as SortedSet requires
    @SuppressWarnings("DoNotCall")
    @Override
    @Test
    @DisplayName("addFirst(E) throws UnsupportedOperationException for an element")
    default void addFirstSingleElementAddsToFrontAndUpdatesSize() {
        S set = provider().emptyInstance();
        E element = elementProvider().createInstance();
        assertThrows(UnsupportedOperationException.class, () -> set.addFirst(element));
    }

    /// Tests that [addFirst(E)][SortedSet#addFirst(Object)] throws [UnsupportedOperationException] for null.
    // DoNotCall: the test calls addFirst on a SortedSet on purpose, to check that it throws, as SortedSet requires
    @SuppressWarnings("DoNotCall")
    @Override
    @Test
    @DisplayName("addFirst(E) throws UnsupportedOperationException for null")
    default void addFirstWithNullValueHandlesCorrectly() {
        S set = provider().emptyInstance();
        assertThrows(UnsupportedOperationException.class, () -> set.addFirst(null));
    }

    /// Tests that [addFirst(E)][SortedSet#addFirst(Object)] throws [UnsupportedOperationException] for an element
    /// already in the set.
    // DoNotCall: the test calls addFirst on a SortedSet on purpose, to check that it throws, as SortedSet requires
    @SuppressWarnings("DoNotCall")
    @Override
    @Test
    @DisplayName("addFirst(E) throws UnsupportedOperationException for an element already in the set")
    default void addFirstWithDuplicateValueHandlesCorrectly() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        E element = values.getFirst();
        assertThrows(UnsupportedOperationException.class, () -> set.addFirst(element));
    }
}
