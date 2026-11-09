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
