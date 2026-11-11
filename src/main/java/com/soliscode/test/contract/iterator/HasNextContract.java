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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [hasNext()][Iterator#hasNext()] method of an [Iterator]: it is true while elements remain, false
/// at the end, and does not advance the iterator.
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Iterator#hasNext()
public interface HasNextContract<I extends Iterator<E>, E> extends IteratorContractSupport<I, E> {

    /// Tests that `hasNext()` is false for an iterator over an empty source.
    @Test
    @DisplayName("hasNext() returns false over an empty source")
    default void hasNextWhenEmptyReturnsFalse() {
        assertFalse(sourceOf(List.of()).iterator().hasNext());
    }

    /// Tests that `hasNext()` is true before each element, and false after the last.
    @Test
    @DisplayName("hasNext() returns true while elements remain, and false at the end")
    default void hasNextWhileElementsRemainReturnsTrue() {
        Iterator<E> iterator = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            assertTrue(iterator.hasNext(), "hasNext() before element " + i);
            iterator.next();
        }
        assertFalse(iterator.hasNext(), "hasNext() after the last element");
    }

    /// Tests that `hasNext()` does not advance the iterator: called twice before each `next()`, the iteration still
    /// returns every element.
    @Test
    @DisplayName("hasNext() called twice does not advance the iterator")
    default void hasNextCalledTwiceDoesNotAdvance() {
        List<E> elements = uniqueElements(DEFAULT_SIZE);
        IteratorSource<I, E> source = sourceOf(elements);
        Iterator<E> iterator = source.iterator();
        List<E> returned = new ArrayList<>();
        while (iterator.hasNext()) {
            assertTrue(returned.size() < TRAVERSAL_LIMIT, "the iterator did not end");
            assertTrue(iterator.hasNext(), "hasNext() called again, before next()");
            returned.add(iterator.next());
        }
        assertSameElements(elements, returned, source, "the elements returned");
    }
}
