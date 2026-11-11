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
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.IteratorContractSupport;
import com.soliscode.test.provider.IteratorSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [next()][Iterator#next()] method of an [Iterator]: it returns each element of the source, in its
/// order where it has one, and then throws [NoSuchElementException].
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Iterator#next()
public interface NextContract<I extends Iterator<E>, E> extends IteratorContractSupport<I, E> {

    /// Tests that `next()` returns each element of the source, once.
    @Test
    @DisplayName("next() returns each element of the source")
    default void nextReturnsEachElementOfTheSource() {
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        assertSameElements(elements, drain(source.iterator()), source, "the elements returned");
    }

    /// Tests that `next()` returns each element when it is called without `hasNext()`.
    @Test
    @DisplayName("next() without hasNext() returns each element of the source")
    default void nextWithoutHasNextReturnsEachElement() {
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        Iterator<E> iterator = source.iterator();
        List<E> returned = new ArrayList<>();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            returned.add(iterator.next());
        }
        assertSameElements(elements, returned, source, "the elements returned");
    }

    /// Tests that `next()` throws [NoSuchElementException] over an empty source.
    @Test
    @DisplayName("next() throws NoSuchElementException over an empty source")
    default void nextWhenEmptyThrowsNoSuchElementException() {
        Iterator<E> iterator = sourceOf(List.of()).iterator();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    /// Tests that `next()` throws [NoSuchElementException] once every element has been returned.
    @Test
    @DisplayName("next() throws NoSuchElementException after the last element")
    default void nextAtEndThrowsNoSuchElementException() {
        Iterator<E> iterator = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            iterator.next();
        }
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}
