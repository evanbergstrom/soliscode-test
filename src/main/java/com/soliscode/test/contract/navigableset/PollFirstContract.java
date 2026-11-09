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

import com.soliscode.test.contract.collection.CollectionMethods;
import com.soliscode.test.contract.support.SortedSetContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/// Contract for the [pollFirst()][NavigableSet#pollFirst()] method of [NavigableSet].
///
/// The tests run only where [CollectionMethods#REMOVE] is supported. `NavigableSet` does not make `pollFirst` an
/// optional operation, but a set that cannot be changed cannot remove its first element
/// ([java.util.Collections#unmodifiableNavigableSet(NavigableSet)]'s throws [UnsupportedOperationException], even
/// when empty); the contract does not say what it does there.
///
/// @param <S> the type of navigable set
/// @param <E> the type of elements
/// @see java.util.NavigableSet#pollFirst()
/// @since 0.1.2
public interface PollFirstContract<S extends NavigableSet<E>, E> extends SortedSetContractSupport<S, E> {

    /// Tests that [pollFirst()][NavigableSet#pollFirst()] returns `null` for an empty set.
    @Test
    @DisplayName("pollFirst() returns null for an empty set")
    default void pollFirstWhenEmptyReturnsNull() {
        if (supportsMethod(CollectionMethods.REMOVE)) {
            S set = provider().emptyInstance();
            assertNull(set.pollFirst());
        }
    }

    /// Tests that [pollFirst()][NavigableSet#pollFirst()] returns the first element in the set's order, as a
    /// [TreeSet][java.util.TreeSet] with the same elements and comparator does, and removes it.
    @Test
    @DisplayName("pollFirst() removes and returns the first element")
    default void pollFirstWhenNotEmptyRemovesAndReturnsFirstElement() {
        if (supportsMethod(CollectionMethods.REMOVE)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            S set = provider().createInstance(values);
            E expected = sortedReference(set, values).first();
            assertEquals(expected, set.pollFirst());
            assertFalse(set.contains(expected), "pollFirst() did not remove the element it returned");
            assertEquals(values.size() - 1, set.size(), "pollFirst() did not remove exactly one element");
        }
    }
}
