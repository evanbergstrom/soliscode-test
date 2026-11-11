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
package com.soliscode.test.contract.spliterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SpliteratorContractSupport;
import com.soliscode.test.provider.SpliteratorSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [forEachRemaining(Consumer)][Spliterator#forEachRemaining(java.util.function.Consumer)] method
/// of a [Spliterator]: it performs the action on each remaining element, in encounter order where the spliterator is
/// `ORDERED`, and relays an exception the action throws.
///
/// Order is checked where the source documents `ORDERED`. It does not test that the action runs "sequentially in
/// the current thread", or what the spliterator does after the action throws, which [Spliterator] leaves
/// unspecified.
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Spliterator#forEachRemaining(java.util.function.Consumer)
public interface ForEachRemainingContract<S extends Spliterator<E>, E> extends SpliteratorContractSupport<S, E> {

    /// Tests that `forEachRemaining` at the start visits every element, and leaves none.
    @Test
    @DisplayName("forEachRemaining() at the start visits every element")
    default void spliteratorForEachRemainingAtStartVisitsEveryElement() {
        SpliteratorSource<S, E> source = defaultSource();
        Spliterator<E> spliterator = source.spliterator();
        List<E> visited = new ArrayList<>();
        spliterator.forEachRemaining(visited::add);
        assertSameElements(source.elements(), visited, documentsOrder(source), "the elements visited");
        assertFalse(spliterator.tryAdvance(e -> { }), "tryAdvance() after forEachRemaining()");
    }

    /// Tests that `forEachRemaining` after some `tryAdvance` calls visits exactly the elements not yet passed.
    @Test
    @DisplayName("forEachRemaining() after tryAdvance() visits only the remaining elements")
    default void spliteratorForEachRemainingAfterTryAdvanceVisitsTheRest() {
        SpliteratorSource<S, E> source = defaultSource();
        Spliterator<E> spliterator = source.spliterator();
        List<E> all = new ArrayList<>();
        for (int i = 0; i < DEFAULT_SIZE / 2; i++) {
            spliterator.tryAdvance(all::add);
        }
        spliterator.forEachRemaining(all::add);
        assertSameElements(source.elements(), all, documentsOrder(source), "the elements passed, then visited");
        assertFalse(spliterator.tryAdvance(e -> { }), "tryAdvance() after forEachRemaining()");
    }

    /// Tests that `forEachRemaining` at the end, and on an empty source, visits nothing.
    @Test
    @DisplayName("forEachRemaining() at the end, and on an empty source, visits nothing")
    default void spliteratorForEachRemainingAtEndVisitsNothing() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        traverse(spliterator);
        List<E> visited = new ArrayList<>();
        spliterator.forEachRemaining(visited::add);
        assertEquals(List.of(), visited, "the elements visited at the end");
        sourceOf(List.of()).spliterator().forEachRemaining(visited::add);
        assertEquals(List.of(), visited, "the elements visited on an empty source");
    }

    /// Tests that `forEachRemaining(null)` throws [NullPointerException]: with elements left, over an empty source,
    /// and at the end of a traversal.
    @Test
    @DisplayName("forEachRemaining(null) throws NullPointerException, with elements left, when empty and at the end")
    default void spliteratorForEachRemainingOfNullThrowsNullPointerException() {
        Spliterator<E> withElements = defaultSource().spliterator();
        assertThrows(NullPointerException.class, () -> withElements.forEachRemaining(null), "with elements left");
        Spliterator<E> empty = sourceOf(List.of()).spliterator();
        assertThrows(NullPointerException.class, () -> empty.forEachRemaining(null), "over an empty source");
        Spliterator<E> atEnd = defaultSource().spliterator();
        traverse(atEnd);
        assertThrows(NullPointerException.class, () -> atEnd.forEachRemaining(null), "at the end of a traversal");
    }

    /// Tests that an exception the action throws is relayed to the caller.
    @Test
    @DisplayName("forEachRemaining() relays an exception the action throws")
    default void spliteratorForEachRemainingWithThrowingActionRelaysTheException() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        RuntimeException thrown = new IllegalStateException("thrown by the action");
        RuntimeException caught = assertThrows(RuntimeException.class, () -> spliterator.forEachRemaining(e -> {
            throw thrown;
        }));
        assertSame(thrown, caught);
    }
}
