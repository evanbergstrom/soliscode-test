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
package com.soliscode.test.contract.transferqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.blockingqueue.BlockingQueueMethods;
import com.soliscode.test.contract.queue.QueueMethods;
import com.soliscode.test.contract.support.TransferQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `transfer` method of a `TransferQueue`**
///
/// [TransferQueue#transfer(Object)] "transfers the specified element immediately if there exists a consumer already
/// waiting to receive it (in take() or timed poll), else waits until the element is received by a consumer". It
/// throws [InterruptedException] "if interrupted while waiting, in which case the element is not left enqueued". The
/// contract checks that it hands the element to a waiting consumer, waits for one when there is none, refuses
/// `null`, and when interrupted throws and leaves nothing enqueued.
///
/// On a bounded queue, "an attempted transfer operation may initially block waiting for available space, and/or
/// subsequently block waiting for reception by a consumer" ([TransferQueue]). The tests of a full queue run only on a
/// queue with at most [TransferQueueContractSupport#FILL_LIMIT] places
/// ([TransferQueueContractSupport#fullInstance]).
///
/// *Stricter readings:*
/// - The interrupt status is set before the call, as [com.soliscode.test.contract.blockingqueue.PutContract] does.
/// - `transfer` on a full queue takes no place past the capacity while it waits for space: the contract reads
///   "waiting for available space" as holding no place beyond the capacity.
///
/// @param <Q> the transfer queue type being tested
/// @param <E> the element type being tested
/// @see TransferQueue#transfer(Object)
/// @since 0.1.2
public interface TransferContract<Q extends TransferQueue<E>, E> extends TransferQueueContractSupport<Q, E> {

    /// Tests that `transfer` hands the element to a consumer waiting in the timed `poll`, or, where it is not
    /// supported, throws [UnsupportedOperationException].
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("transfer(E) hands the element to a waiting consumer")
    @Test
    default void transferWithWaitingConsumerHandsOverElement() throws Exception {
        Q queue = provider().emptyInstance();
        E element = elementProvider().createInstance();
        if (!supportsMethod(TransferQueueMethods.TRANSFER)) {
            assertThrows(UnsupportedOperationException.class, () -> boundedRun(() -> queue.transfer(element)));
            return;
        }
        if (!supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            return;
        }
        List<@Nullable E> received = withWaitingConsumers(queue, 1, (q, anyReturned) -> q.transfer(element));
        assertEquals(element, received.get(0), "the waiting consumer did not receive the element transferred");
        assertEquals(0, queue.size(), "transfer(E) to a waiting consumer left elements in the queue");
    }

    /// Tests that `transfer`, with no consumer waiting, does not return until another thread's timed `poll`, which
    /// receives the element.
    @DisplayName("transfer(E) with no consumer waits until another thread receives the element")
    @Test
    default void transferWithNoConsumerWaitsForConsumer() {
        if (supportsMethod(TransferQueueMethods.TRANSFER) && supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            E element = elementProvider().createInstance();
            AtomicReference<@Nullable E> received = new AtomicReference<>();
            assertWaitsFor(queue, q -> q.transfer(element),
                    q -> received.set(q.poll(CONSUMER_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
            assertEquals(element, received.get(), "the consumer did not receive the element transferred");
            assertEquals(0, queue.size(), "transfer(E) left elements in the queue");
        }
    }

    /// Tests that `transfer(null)` throws [NullPointerException]: a `BlockingQueue` "does not accept null elements".
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("transfer(null) throws NullPointerException")
    @Test
    // NullAway: the test passes null on purpose, to check that transfer refuses it.
    @SuppressWarnings("NullAway")
    default void transferWithNullThrowsNullPointerException() throws Exception {
        if (supportsMethod(TransferQueueMethods.TRANSFER)) {
            Q queue = provider().emptyInstance();
            assertThrows(NullPointerException.class, () -> boundedRun(() -> queue.transfer(null)));
            assertEquals(0, queue.size(), "transfer(null) changed the queue");
        }
    }

    /// Tests that `transfer` with no consumer, by a thread whose interrupt status is set, throws
    /// [InterruptedException] and does not leave the element enqueued.
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("transfer(E) throws InterruptedException when interrupted, leaving nothing enqueued")
    @Test
    default void transferWhenInterruptedThrowsAndDoesNotEnqueue() throws Exception {
        if (supportsMethod(TransferQueueMethods.TRANSFER)) {
            Q queue = provider().emptyInstance();
            E element = elementProvider().createInstance();
            assertThrows(InterruptedException.class, () -> boundedInterrupted(() -> queue.transfer(element)));
            assertFalse(queue.contains(element), "an interrupted transfer(E) left the element enqueued");
            assertEquals(0, queue.size(), "an interrupted transfer(E) changed the queue");
        }
    }

    /// Tests that `transfer` on a full queue waits for room, without taking a place past the capacity, and then for
    /// a consumer: another thread polls each element, and then the one transferred.
    @DisplayName("transfer(E) on a full queue waits for room, then for a consumer")
    @Test
    default void transferWhenFullWaitsForRoomAndConsumer() {
        if (!supportsMethod(TransferQueueMethods.TRANSFER) || !supportsMethod(QueueMethods.POLL)
                || !supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            return;
        }
        Optional<Q> full = fullInstance();
        if (full.isEmpty() || full.get().size() == 0) {
            return;
        }
        Q queue = full.get();
        int size = queue.size();
        E extra = elementProvider().createInstanceNotIn(new ArrayList<>(queue));
        AtomicReference<@Nullable E> last = new AtomicReference<>();
        assertWaitsFor(queue, q -> q.transfer(extra), q -> {
            assertEquals(size, q.size(), "transfer(E) on a full queue took a place past the capacity");
            assertFalse(q.contains(extra), "transfer(E) on a full queue added the element before there was room");
            for (int i = 0; i < size; i++) {
                q.poll();
            }
            last.set(q.poll(CONSUMER_WAIT_MILLIS, TimeUnit.MILLISECONDS));
        });
        assertEquals(extra, last.get(), "the consumer did not receive the element transferred");
        assertEquals(0, queue.size(), "elements left after every element was polled");
    }

    /// Tests that `transfer` on a full queue, by a thread whose interrupt status is set, throws
    /// [InterruptedException] and does not add the element.
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("transfer(E) on a full queue throws InterruptedException when interrupted")
    @Test
    default void transferWhenFullAndInterruptedThrowsInterruptedException() throws Exception {
        if (supportsMethod(TransferQueueMethods.TRANSFER)) {
            Optional<Q> full = fullInstance();
            if (full.isEmpty()) {
                return;
            }
            Q queue = full.get();
            int size = queue.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(queue));
            assertThrows(InterruptedException.class, () -> boundedInterrupted(() -> queue.transfer(extra)));
            assertFalse(queue.contains(extra), "an interrupted transfer(E) added the element");
            assertEquals(size, queue.size());
        }
    }
}
