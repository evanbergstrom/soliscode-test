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
package com.soliscode.test.contract.sortedset;

import java.util.List;
import java.util.SortedSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.sequencedcollection.AddLastContract;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [addLast(E)][SortedSet#addLast(Object)] method of [SortedSet]: it throws
/// [UnsupportedOperationException], since a sorted set's order is its comparator's.
///
/// It overrides each test of [AddLastContract], so a test class need not mark `addLast` unsupported.
///
/// @param <S> the type of sorted set
/// @param <E> the type of elements
/// @see SortedSet#addLast(Object)
/// @since 0.1.2
public interface SortedAddLastContract<S extends SortedSet<E>, E> extends AddLastContract<S, E> {

    /// Tests that [addLast(E)][SortedSet#addLast(Object)] throws [UnsupportedOperationException] for an element.
    // DoNotCall: the test calls addLast on a SortedSet on purpose, to check that it throws, as SortedSet requires
    @SuppressWarnings("DoNotCall")
    @Override
    @Test
    @DisplayName("addLast(E) throws UnsupportedOperationException for an element")
    default void addLastSingleElementAddsToBackAndUpdatesSize() {
        S set = provider().emptyInstance();
        E element = elementProvider().createInstance();
        assertThrows(UnsupportedOperationException.class, () -> set.addLast(element));
    }

    /// Tests that [addLast(E)][SortedSet#addLast(Object)] throws [UnsupportedOperationException] for null.
    // DoNotCall: the test calls addLast on a SortedSet on purpose, to check that it throws, as SortedSet requires
    @SuppressWarnings("DoNotCall")
    @Override
    @Test
    @DisplayName("addLast(E) throws UnsupportedOperationException for null")
    default void addLastWithNullValueHandlesCorrectly() {
        S set = provider().emptyInstance();
        assertThrows(UnsupportedOperationException.class, () -> set.addLast(null));
    }

    /// Tests that [addLast(E)][SortedSet#addLast(Object)] throws [UnsupportedOperationException] for an element already
    /// in the set.
    // DoNotCall: the test calls addLast on a SortedSet on purpose, to check that it throws, as SortedSet requires
    @SuppressWarnings("DoNotCall")
    @Override
    @Test
    @DisplayName("addLast(E) throws UnsupportedOperationException for an element already in the set")
    default void addLastWithDuplicateValueHandlesCorrectly() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        S set = provider().createInstance(values);
        E element = values.getFirst();
        assertThrows(UnsupportedOperationException.class, () -> set.addLast(element));
    }
}
