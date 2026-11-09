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

/// **Contract for the `drainTo(Collection)` method of a `BlockingQueue`**
///
/// [BlockingQueue#drainTo(java.util.Collection)] "Removes all available elements from this queue and adds them to
/// the given collection", returning "the number of elements transferred"; it throws [IllegalArgumentException] "if
/// the specified collection is this queue" and [NullPointerException] "if the specified collection is null". The
/// contract checks each, for an empty and a non-empty queue, comparing the elements moved as a multiset: a
/// `BlockingQueue` need not be FIFO.
///
/// *Stricter reading:* with no other thread, every element is "available", so all of them are moved.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#drainTo(java.util.Collection)
/// @since 0.1.2
public interface DrainToContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// Tests that `drainTo` moves every element to the collection, returns how many, and leaves the queue empty,
    /// for an empty and a non-empty queue; or, where it is not supported, throws [UnsupportedOperationException].
    @DisplayName("drainTo(Collection) moves every element and returns how many")
    @Test
    default void drainToMovesEveryElementAndReturnsCount() {
        for (final int size : new int[] {0, DEFAULT_SIZE}) {
            List<E> values = elementProvider().createUniqueInstances(size);
            Q queue = provider().createInstance(values);
            List<E> target = new ArrayList<>();
            if (supportsMethod(BlockingQueueMethods.DRAIN_TO)) {
                assertEquals(size, queue.drainTo(target), "drainTo(Collection) of a queue of " + size);
                assertSameElements(values, target, "the elements drainTo(Collection) moved");
                assertEquals(0, queue.size(), "drainTo(Collection) left elements in the queue");
            } else {
                assertThrows(UnsupportedOperationException.class, () -> queue.drainTo(target));
            }
        }
    }

    /// Tests that `drainTo` into the queue itself throws [IllegalArgumentException] and leaves the queue unchanged.
    @DisplayName("drainTo(Collection) into the queue itself throws IllegalArgumentException")
    @Test
    default void drainToItselfThrowsIllegalArgumentException() {
        if (supportsMethod(BlockingQueueMethods.DRAIN_TO)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Q queue = provider().createInstance(values);
            assertThrows(IllegalArgumentException.class, () -> queue.drainTo(queue));
            assertSameElements(values, queue, "the queue after drainTo(Collection) into itself");
        }
    }

    /// Tests that `drainTo(null)` throws [NullPointerException].
    @DisplayName("drainTo(null) throws NullPointerException")
    @Test
    // NullAway: the test passes null on purpose, to check that drainTo refuses it.
    @SuppressWarnings("NullAway")
    default void drainToNullThrowsNullPointerException() {
        if (supportsMethod(BlockingQueueMethods.DRAIN_TO)) {
            Q queue = provider().createInstance(elementProvider().createUniqueInstances(DEFAULT_SIZE));
            assertThrows(NullPointerException.class, () -> queue.drainTo(null));
        }
    }
}
