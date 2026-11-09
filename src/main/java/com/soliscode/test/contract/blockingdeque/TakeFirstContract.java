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
package com.soliscode.test.contract.blockingdeque;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.deque.DequeMethods;
import com.soliscode.test.contract.queue.QueueMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `takeFirst` method of a `BlockingDeque`**
///
/// [BlockingDeque#takeFirst()] "Retrieves and removes the first element of this deque, waiting if necessary until an
/// element becomes available". The contract checks that it returns the first element (what `peekFirst()` returned just
/// before) and removes it, waits on an empty deque until another thread adds an element, and throws
/// [InterruptedException] when interrupted on an empty deque.
///
/// *Stricter reading:* the interrupt status is set before the call, as
/// [com.soliscode.test.contract.blockingqueue.PutContract] does.
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @see BlockingDeque#takeFirst()
/// @since 0.1.2
public interface TakeFirstContract<D extends BlockingDeque<E>, E> extends BlockingQueueContractSupport<D, E> {

    /// Tests that `takeFirst` returns the first element and removes it, once for each element, leaving the deque empty;
    /// or, where it is not supported, throws [UnsupportedOperationException].
    ///
    /// @throws Exception if the deque throws one
    @DisplayName("takeFirst() returns and removes the first element of the deque")
    @Test
    default void takeFirstReturnsAndRemovesFirstElement() throws Exception {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        D deque = provider().createInstance(values);
        if (supportsMethod(BlockingDequeMethods.TAKE_FIRST)) {
            List<E> taken = new ArrayList<>();
            boundedRun(() -> {
                for (int i = 0; i < values.size(); i++) {
                    @Nullable E expected = supportsMethod(DequeMethods.PEEK_FIRST) ? deque.peekFirst() : null;
                    E e = deque.takeFirst();
                    if (expected != null) {
                        assertEquals(expected, e, "takeFirst() did not return the first element");
                    }
                    taken.add(e);
                    assertEquals(values.size() - i - 1, deque.size(), "takeFirst() did not remove the element");
                }
            });
            assertEquals(0, deque.size(), "takeFirst() left elements in the deque");
            assertSameElements(values, taken, "the elements takeFirst() returned");
        } else {
            assertThrows(UnsupportedOperationException.class, () -> boundedRun(deque::takeFirst));
        }
    }

    /// Tests that `takeFirst` on an empty deque waits until another thread adds an element (with `offer`), then
    /// returns it.
    @DisplayName("takeFirst() on an empty deque waits until another thread adds an element")
    @Test
    default void takeFirstWhenEmptyWaitsForElement() {
        if (supportsMethod(BlockingDequeMethods.TAKE_FIRST) && supportsMethod(QueueMethods.OFFER)) {
            D deque = provider().emptyInstance();
            E element = elementProvider().createInstance();
            AtomicReference<@Nullable E> taken = new AtomicReference<>();
            assertWaitsFor(deque, d -> taken.set(d.takeFirst()), d -> d.offer(element));
            assertEquals(element, taken.get(), "takeFirst() did not return the element added");
            assertEquals(0, deque.size(), "takeFirst() did not remove the element added");
        }
    }

    /// Tests that `takeFirst` on an empty deque, by a thread whose interrupt status is set, throws
    /// [InterruptedException].
    ///
    /// @throws Exception if the deque throws one other than the expected
    @DisplayName("takeFirst() on an empty deque throws InterruptedException when interrupted")
    @Test
    default void takeFirstWhenEmptyAndInterruptedThrowsInterruptedException() throws Exception {
        if (supportsMethod(BlockingDequeMethods.TAKE_FIRST)) {
            D deque = provider().emptyInstance();
            assertThrows(InterruptedException.class, () -> boundedInterrupted(deque::takeFirst));
        }
    }
}
