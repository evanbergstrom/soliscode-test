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
package com.soliscode.test.contract.listiterator;

import java.util.List;
import java.util.ListIterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.IteratorContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [nextIndex()][ListIterator#nextIndex()] method of a [ListIterator]: the index of the element
/// `next` would return, or the list's size at the end.
///
/// @param <I> the type of the list iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see ListIterator#nextIndex()
public interface NextIndexContract<I extends ListIterator<E>, E> extends IteratorContractSupport<I, E> {

    /// Tests that `nextIndex()` is 0 at the start, and so the size over an empty source.
    @Test
    @DisplayName("nextIndex() returns 0 at the start")
    default void nextIndexAtStartReturnsZero() {
        assertEquals(0, sourceOf(List.of()).iterator().nextIndex(), "over an empty source");
        assertEquals(0, sourceOf(uniqueElements(DEFAULT_SIZE)).iterator().nextIndex(), "over a source with elements");
    }

    /// Tests that `nextIndex()` follows the cursor forward to the size, and back to 0.
    @Test
    @DisplayName("nextIndex() returns the cursor's position, forward to the size and back")
    default void nextIndexFollowsTheCursor() {
        ListIterator<E> iterator = sourceOf(uniqueElements(DEFAULT_SIZE)).iterator();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            assertEquals(i, iterator.nextIndex(), "nextIndex() before next() for element " + i);
            iterator.next();
        }
        assertEquals(DEFAULT_SIZE, iterator.nextIndex(), "nextIndex() at the end");
        for (int i = DEFAULT_SIZE - 1; i >= 0; i--) {
            iterator.previous();
            assertEquals(i, iterator.nextIndex(), "nextIndex() after previous() returned element " + i);
        }
    }
}
