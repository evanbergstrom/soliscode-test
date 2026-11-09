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
package com.soliscode.test.contract.blockingqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `drainTo(Collection, int)` method of a `BlockingQueue`**
///
/// [BlockingQueue#drainTo(java.util.Collection, int)] "Removes at most the given number of available elements from
/// this queue and adds them to the given collection", returning "the number of elements transferred"; it throws
/// [IllegalArgumentException] "if the specified collection is this queue" and [NullPointerException] "if the
/// specified collection is null". The contract checks each, for a maximum below, at and above the queue's size; the
/// elements moved and those left are compared as multisets, since a `BlockingQueue` need not be FIFO. A maximum of
/// zero or less is not specified, and not tested.
///
/// *Stricter reading:* with no other thread, every element is "available", so it moves `min(maxElements, size)`.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#drainTo(java.util.Collection, int)
/// @since 0.1.2
public interface DrainToMaxElementsContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// Tests that `drainTo(c, max)` moves `min(max, size)` elements and returns how many, leaving the rest, for a
    /// maximum below, at and above the size; or, where it is not supported, throws [UnsupportedOperationException].
    @DisplayName("drainTo(Collection, int) moves at most the given number of elements and returns how many")
    @Test
    default void drainToMaxElementsMovesAtMostMaxAndReturnsCount() {
        for (final int max : new int[] {1, DEFAULT_SIZE / 2, DEFAULT_SIZE, DEFAULT_SIZE + 5}) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Q queue = provider().createInstance(values);
            List<E> target = new ArrayList<>();
            if (supportsMethod(BlockingQueueMethods.DRAIN_TO_MAX_ELEMENTS)) {
                int expected = Math.min(max, values.size());
                assertEquals(expected, queue.drainTo(target, max), "drainTo(Collection, " + max + ")");
                assertEquals(expected, target.size(), "the elements drainTo(Collection, " + max + ") moved");
                assertEquals(values.size() - expected, queue.size(), "the elements drainTo(Collection, " + max
                        + ") left");
                List<E> all = new ArrayList<>(target);
                all.addAll(queue);
                assertSameElements(values, all, "the elements moved and left by drainTo(Collection, " + max + ")");
            } else {
                assertThrows(UnsupportedOperationException.class, () -> queue.drainTo(target, max));
            }
        }
    }

    /// Tests that `drainTo(c, max)` into the queue itself throws [IllegalArgumentException] and leaves the queue
    /// unchanged.
    @DisplayName("drainTo(Collection, int) into the queue itself throws IllegalArgumentException")
    @Test
    default void drainToMaxElementsItselfThrowsIllegalArgumentException() {
        if (supportsMethod(BlockingQueueMethods.DRAIN_TO_MAX_ELEMENTS)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Q queue = provider().createInstance(values);
            assertThrows(IllegalArgumentException.class, () -> queue.drainTo(queue, DEFAULT_SIZE));
            assertSameElements(values, queue, "the queue after drainTo(Collection, int) into itself");
        }
    }

    /// Tests that `drainTo(null, max)` throws [NullPointerException].
    @DisplayName("drainTo(null, int) throws NullPointerException")
    @Test
    // NullAway: the test passes null on purpose, to check that drainTo refuses it.
    @SuppressWarnings("NullAway")
    default void drainToMaxElementsNullThrowsNullPointerException() {
        if (supportsMethod(BlockingQueueMethods.DRAIN_TO_MAX_ELEMENTS)) {
            Q queue = provider().createInstance(elementProvider().createUniqueInstances(DEFAULT_SIZE));
            assertThrows(NullPointerException.class, () -> queue.drainTo(null, DEFAULT_SIZE));
        }
    }
}
