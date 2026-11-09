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
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.queue.QueueMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `take` method of a `BlockingQueue`**
///
/// [BlockingQueue#take()] "Retrieves and removes the head of this queue, waiting if necessary until an element
/// becomes available". The contract checks that it returns the head (what `peek()` returned just before) and removes
/// it, waits on an empty queue until another thread adds an element, and throws [InterruptedException] when
/// interrupted on an empty queue. It does not assume an order: a `BlockingQueue` need not be FIFO.
///
/// *Stricter reading:* `take` throws `InterruptedException` "if interrupted while waiting"; the contract sets the
/// interrupt status before the call, as [PutContract] does.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#take()
/// @since 0.1.2
public interface TakeContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// Tests that `take` returns the head and removes it, once for each element, leaving the queue empty; or, where
    /// it is not supported, throws [UnsupportedOperationException].
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("take() returns and removes the head of the queue")
    @Test
    default void takeReturnsAndRemovesHead() throws Exception {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Q queue = provider().createInstance(values);
        if (supportsMethod(BlockingQueueMethods.TAKE)) {
            List<E> taken = new ArrayList<>();
            boundedRun(() -> {
                for (int i = 0; i < values.size(); i++) {
                    @Nullable E head = supportsMethod(QueueMethods.PEEK) ? queue.peek() : null;
                    E e = queue.take();
                    if (head != null) {
                        assertEquals(head, e, "take() did not return the head");
                    }
                    taken.add(e);
                    assertEquals(values.size() - i - 1, queue.size(), "take() did not remove the head");
                }
            });
            assertEquals(0, queue.size(), "take() left elements in the queue");
            assertSameElements(values, taken, "the elements take() returned");
        } else {
            assertThrows(UnsupportedOperationException.class, () -> boundedRun(queue::take));
        }
    }

    /// Tests that `take` on an empty queue waits until another thread adds an element (with `offer`), then returns
    /// it.
    @DisplayName("take() on an empty queue waits until another thread adds an element")
    @Test
    default void takeWhenEmptyWaitsForElement() {
        if (supportsMethod(BlockingQueueMethods.TAKE) && supportsMethod(QueueMethods.OFFER)) {
            Q queue = provider().emptyInstance();
            E element = elementProvider().createInstance();
            AtomicReference<@Nullable E> taken = new AtomicReference<>();
            assertWaitsFor(queue, q -> taken.set(q.take()), q -> q.offer(element));
            assertEquals(element, taken.get(), "take() did not return the element added");
            assertEquals(0, queue.size(), "take() did not remove the element added");
        }
    }

    /// Tests that `take` on an empty queue, by a thread whose interrupt status is set, throws
    /// [InterruptedException].
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("take() on an empty queue throws InterruptedException when interrupted")
    @Test
    default void takeWhenEmptyAndInterruptedThrowsInterruptedException() throws Exception {
        if (supportsMethod(BlockingQueueMethods.TAKE)) {
            Q queue = provider().emptyInstance();
            assertThrows(InterruptedException.class, () -> boundedInterrupted(queue::take));
        }
    }
}
