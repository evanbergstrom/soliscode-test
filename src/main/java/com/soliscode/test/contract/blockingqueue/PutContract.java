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
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.queue.QueueMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `put` method of a `BlockingQueue`**
///
/// [BlockingQueue#put(Object)] "Inserts the specified element into this queue, waiting if necessary for space to
/// become available". The contract checks that it adds each element to a queue with room, refuses `null`, waits on a
/// full queue until another thread makes room, and throws [InterruptedException] when interrupted on a full queue.
///
/// *Stricter reading:* `put` throws `InterruptedException` "if interrupted while waiting"; the contract sets the
/// interrupt status before the call, and expects the exception, as [java.util.concurrent.locks.Lock#lockInterruptibly]
/// and [java.util.concurrent.locks.Condition#await] do for a thread that "has its interrupted status set on entry".
///
/// The tests of a full queue run only on a queue with at most
/// [BlockingQueueContractSupport#FILL_LIMIT] places ([BlockingQueueContractSupport#fullInstance]).
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#put(Object)
/// @since 0.1.2
public interface PutContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// Tests that `put` adds each element to a queue with room, or, where it is not supported, throws
    /// [UnsupportedOperationException].
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("put(E) adds each element to a queue with room")
    @Test
    default void putWithRoomAddsElement() throws Exception {
        Q queue = provider().emptyInstance();
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        if (supportsMethod(BlockingQueueMethods.PUT)) {
            boundedRun(() -> {
                for (int i = 0; i < values.size(); i++) {
                    queue.put(values.get(i));
                    assertTrue(queue.contains(values.get(i)), "put(E) did not add the element");
                    assertEquals(i + 1, queue.size());
                }
            });
        } else {
            assertThrows(UnsupportedOperationException.class, () -> boundedRun(() -> queue.put(values.get(0))));
        }
    }

    /// Tests that `put(null)` throws [NullPointerException]: "A BlockingQueue does not accept null elements".
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("put(null) throws NullPointerException")
    @Test
    // NullAway: the test passes null on purpose, to check that put refuses it.
    @SuppressWarnings("NullAway")
    default void putWithNullThrowsNullPointerException() throws Exception {
        if (supportsMethod(BlockingQueueMethods.PUT)) {
            Q queue = provider().emptyInstance();
            assertThrows(NullPointerException.class, () -> boundedRun(() -> queue.put(null)));
            assertEquals(0, queue.size(), "put(null) changed the queue");
        }
    }

    /// Tests that `put` on a full queue waits until another thread makes room (with `poll()`), then adds the
    /// element.
    @DisplayName("put(E) on a full queue waits until another thread makes room")
    @Test
    default void putWhenFullWaitsForRoom() {
        if (supportsMethod(BlockingQueueMethods.PUT) && supportsMethod(QueueMethods.POLL)) {
            Optional<Q> full = fullInstance();
            if (full.isEmpty() || full.get().size() == 0) {
                return;
            }
            Q queue = full.get();
            int size = queue.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(queue));
            assertWaitsFor(queue, q -> q.put(extra), q -> q.poll());
            assertTrue(queue.contains(extra), "put(E) returned without adding the element");
            assertEquals(size, queue.size());
        }
    }

    /// Tests that `put` on a full queue, by a thread whose interrupt status is set, throws [InterruptedException]
    /// and does not add the element.
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("put(E) on a full queue throws InterruptedException when interrupted")
    @Test
    default void putWhenFullAndInterruptedThrowsInterruptedException() throws Exception {
        if (supportsMethod(BlockingQueueMethods.PUT)) {
            Optional<Q> full = fullInstance();
            if (full.isEmpty()) {
                return;
            }
            Q queue = full.get();
            int size = queue.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(queue));
            assertThrows(InterruptedException.class, () -> boundedInterrupted(() -> queue.put(extra)));
            assertFalse(queue.contains(extra), "an interrupted put(E) added the element");
            assertEquals(size, queue.size());
        }
    }
}
