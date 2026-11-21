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
import com.soliscode.test.methods.CollectionMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/// Contract for the [pollLast()][NavigableSet#pollLast()] method of [NavigableSet].
///
/// The tests run only where [CollectionMethods#REMOVE] is supported. `NavigableSet` does not make `pollLast` an
/// optional operation, but a set that cannot be changed cannot remove its last element
/// ([java.util.Collections#unmodifiableNavigableSet(NavigableSet)]'s throws [UnsupportedOperationException], even
/// when empty); the contract does not say what it does there.
///
/// @param <S> the type of navigable set
/// @param <E> the type of elements
/// @see java.util.NavigableSet#pollLast()
/// @since 0.1.2
public interface PollLastContract<S extends NavigableSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [pollLast()][NavigableSet#pollLast()] returns `null` for an empty set.
    @Test
    @DisplayName("pollLast() returns null for an empty set")
    default void pollLastWhenEmptyReturnsNull() {
        if (supportsMethod(CollectionMethods.REMOVE)) {
            S set = provider().emptyInstance();
            assertNull(set.pollLast());
        }
    }

    /// Tests that [pollLast()][NavigableSet#pollLast()] returns the last element in the set's order, as a
    /// [TreeSet][java.util.TreeSet] with the same elements and comparator does, and removes it.
    @Test
    @DisplayName("pollLast() removes and returns the last element")
    default void pollLastWhenNotEmptyRemovesAndReturnsLastElement() {
        if (supportsMethod(CollectionMethods.REMOVE)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            S set = provider().createInstance(values);
            E expected = sortedReference(set, values).last();
            assertEquals(expected, set.pollLast());
            assertFalse(set.contains(expected), "pollLast() did not remove the element it returned");
            assertEquals(values.size() - 1, set.size(), "pollLast() did not remove exactly one element");
        }
    }
}
