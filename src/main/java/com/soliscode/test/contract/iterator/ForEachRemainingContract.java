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
package com.soliscode.test.contract.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.IteratorContractSupport;
import com.soliscode.test.provider.IteratorSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [forEachRemaining(Consumer)][Iterator#forEachRemaining(java.util.function.Consumer)] method of an
/// [Iterator]: it performs the action on each remaining element, in order where there is one, and relays an exception
/// the action throws.
///
/// It reads [Iterator] more strictly than `IteratorMethodContract` (the `Iterable` contract's) does: the method is not
/// optional, so it must not throw [UnsupportedOperationException]. It does not test what the iterator does after the
/// action throws, or an action that changes the source, both of which [Iterator] leaves unspecified.
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Iterator#forEachRemaining(java.util.function.Consumer)
public interface ForEachRemainingContract<I extends Iterator<E>, E> extends IteratorContractSupport<I, E> {

    /// Tests that `forEachRemaining` at the start visits every element, and leaves none.
    @Test
    @DisplayName("forEachRemaining() at the start visits every element")
    default void forEachRemainingAtStartVisitsEveryElement() {
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        Iterator<E> iterator = source.iterator();
        List<E> visited = new ArrayList<>();
        iterator.forEachRemaining(visited::add);
        assertSameElements(elements, visited, source, "the elements visited");
        assertFalse(iterator.hasNext(), "hasNext() after forEachRemaining()");
    }

    /// Tests that `forEachRemaining` in the middle visits exactly the elements `next()` has not returned.
    @Test
    @DisplayName("forEachRemaining() in the middle visits only the remaining elements")
    default void forEachRemainingInMiddleVisitsTheRest() {
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        Iterator<E> iterator = source.iterator();
        List<E> all = new ArrayList<>();
        for (int i = 0; i < DEFAULT_SIZE / 2; i++) {
            all.add(iterator.next());
        }
        iterator.forEachRemaining(all::add);
        assertSameElements(elements, all, source, "the elements returned, then visited");
        assertFalse(iterator.hasNext(), "hasNext() after forEachRemaining()");
    }

    /// Tests that `forEachRemaining` at the end visits nothing.
    @Test
    @DisplayName("forEachRemaining() at the end visits nothing")
    default void forEachRemainingAtEndVisitsNothing() {
        Iterator<E> iterator = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        drain(iterator);
        List<E> visited = new ArrayList<>();
        iterator.forEachRemaining(visited::add);
        assertEquals(List.of(), visited);
    }

    /// Tests that `forEachRemaining(null)` throws [NullPointerException].
    @Test
    @DisplayName("forEachRemaining(null) throws NullPointerException")
    default void forEachRemainingOfNullThrowsNullPointerException() {
        Iterator<E> iterator = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        assertThrows(NullPointerException.class, () -> iterator.forEachRemaining(null));
    }

    /// Tests that an exception the action throws is relayed to the caller.
    @Test
    @DisplayName("forEachRemaining() relays an exception the action throws")
    default void forEachRemainingWithThrowingActionRelaysTheException() {
        Iterator<E> iterator = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        RuntimeException thrown = new IllegalStateException("thrown by the action");
        RuntimeException caught = assertThrows(RuntimeException.class, () -> iterator.forEachRemaining(e -> {
            throw thrown;
        }));
        assertSame(thrown, caught);
    }
}
