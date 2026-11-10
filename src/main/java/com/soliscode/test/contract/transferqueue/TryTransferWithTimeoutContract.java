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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.blockingqueue.BlockingQueueMethods;
import com.soliscode.test.contract.support.TransferQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the timed `tryTransfer` method of a `TransferQueue`**
///
/// [TransferQueue#tryTransfer(Object, long, TimeUnit)] "transfers the specified element immediately if there exists a
/// consumer already waiting to receive it (in take() or timed poll), else waits until the element is received by a
/// consumer, returning false if the specified wait time elapses before the element can be transferred", "in which
/// case the element is not left enqueued"; interrupted, it throws [InterruptedException], and again the element "is
/// not left enqueued". The contract checks that it hands the element to a waiting consumer, waits for one when there
/// is none, returns `false` only after at least the timeout
/// ([TransferQueueContractSupport#TIMED_WAIT_MILLIS], measured with [System#nanoTime]) without leaving the element
/// enqueued, throws when interrupted without leaving it enqueued, and refuses `null`.
///
/// *Stricter reading:* the interrupt status is set before the call, as
/// [com.soliscode.test.contract.blockingqueue.PutContract] does.
///
/// @param <Q> the transfer queue type being tested
/// @param <E> the element type being tested
/// @see TransferQueue#tryTransfer(Object, long, TimeUnit)
/// @since 0.1.2
public interface TryTransferWithTimeoutContract<Q extends TransferQueue<E>, E>
        extends TransferQueueContractSupport<Q, E> {

    /// Tests that the timed `tryTransfer`, with a consumer waiting in the timed `poll`, returns `true` and hands
    /// the element over; or, where it is not supported, throws [UnsupportedOperationException].
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("tryTransfer(E, long, TimeUnit) with a waiting consumer returns true and hands the element over")
    @Test
    default void tryTransferWithTimeoutWithWaitingConsumerReturnsTrue() throws Exception {
        Q queue = provider().emptyInstance();
        E element = elementProvider().createInstance();
        if (!supportsMethod(TransferQueueMethods.TRY_TRANSFER_TIMEOUT)) {
            assertThrows(UnsupportedOperationException.class,
                    () -> bounded(() -> queue.tryTransfer(element, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
            return;
        }
        if (!supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            return;
        }
        AtomicBoolean result = new AtomicBoolean();
        List<@Nullable E> received = withWaitingConsumers(queue, 1, (q, anyReturned) ->
                result.set(q.tryTransfer(element, CONSUMER_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
        assertTrue(result.get(), "tryTransfer(E, long, TimeUnit) returned false while a consumer waited");
        assertEquals(element, received.get(0), "the waiting consumer did not receive the element");
        assertEquals(0, queue.size(), "tryTransfer(E, long, TimeUnit) left elements in the queue");
    }

    /// Tests that the timed `tryTransfer`, with no consumer waiting, does not return until another thread's timed
    /// `poll`, and then returns `true`.
    @DisplayName("tryTransfer(E, long, TimeUnit) with no consumer waits until another thread receives the element")
    @Test
    default void tryTransferWithTimeoutWithNoConsumerWaitsForConsumer() {
        if (supportsMethod(TransferQueueMethods.TRY_TRANSFER_TIMEOUT)
                && supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            E element = elementProvider().createInstance();
            AtomicReference<@Nullable E> received = new AtomicReference<>();
            assertWaitsFor(queue,
                    q -> assertTrue(q.tryTransfer(element, CONSUMER_WAIT_MILLIS, TimeUnit.MILLISECONDS),
                            "tryTransfer(E, long, TimeUnit) returned false though a consumer came"),
                    q -> received.set(q.poll(CONSUMER_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
            assertEquals(element, received.get(), "the consumer did not receive the element transferred");
            assertEquals(0, queue.size(), "tryTransfer(E, long, TimeUnit) left elements in the queue");
        }
    }

    /// Tests that the timed `tryTransfer`, with no consumer waiting, returns `false` only once at least the timeout
    /// has passed, and does not leave the element enqueued.
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("tryTransfer(E, long, TimeUnit) with no consumer returns false after the timeout")
    @Test
    default void tryTransferWithTimeoutWithNoConsumerReturnsFalseAfterTimeout() throws Exception {
        if (supportsMethod(TransferQueueMethods.TRY_TRANSFER_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            E element = elementProvider().createInstance();
            assertReturnsFalseAfterTimeout(queue, element);
            assertEquals(0, queue.size(), "tryTransfer(E, long, TimeUnit) changed the queue");
        }
    }

    /// Tests that the timed `tryTransfer` on a full queue returns `false` only once at least the timeout has
    /// passed, without adding the element.
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("tryTransfer(E, long, TimeUnit) on a full queue returns false after the timeout")
    @Test
    default void tryTransferWithTimeoutWhenFullReturnsFalseAfterTimeout() throws Exception {
        if (supportsMethod(TransferQueueMethods.TRY_TRANSFER_TIMEOUT)) {
            Optional<Q> full = fullInstance();
            if (full.isEmpty()) {
                return;
            }
            Q queue = full.get();
            int size = queue.size();
            assertReturnsFalseAfterTimeout(queue, elementProvider().createInstanceNotIn(new ArrayList<>(queue)));
            assertEquals(size, queue.size(), "tryTransfer(E, long, TimeUnit) changed a full queue");
        }
    }

    /// Tests that the timed `tryTransfer` with no consumer, by a thread whose interrupt status is set, throws
    /// [InterruptedException] and does not leave the element enqueued.
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("tryTransfer(E, long, TimeUnit) throws InterruptedException when interrupted, leaving nothing "
            + "enqueued")
    @Test
    default void tryTransferWithTimeoutWhenInterruptedThrowsAndDoesNotEnqueue() throws Exception {
        if (supportsMethod(TransferQueueMethods.TRY_TRANSFER_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            E element = elementProvider().createInstance();
            assertThrows(InterruptedException.class, () -> boundedInterrupted(
                    () -> queue.tryTransfer(element, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
            assertFalse(queue.contains(element), "an interrupted tryTransfer(E, long, TimeUnit) left the element "
                    + "enqueued");
            assertEquals(0, queue.size(), "an interrupted tryTransfer(E, long, TimeUnit) changed the queue");
        }
    }

    /// Tests that the timed `tryTransfer` of `null` throws [NullPointerException].
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("tryTransfer(null, long, TimeUnit) throws NullPointerException")
    @Test
    // NullAway: the test passes null on purpose, to check that the timed tryTransfer refuses it.
    @SuppressWarnings("NullAway")
    default void tryTransferWithTimeoutWithNullThrowsNullPointerException() throws Exception {
        if (supportsMethod(TransferQueueMethods.TRY_TRANSFER_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            assertThrows(NullPointerException.class,
                    () -> bounded(() -> queue.tryTransfer(null, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
            assertEquals(0, queue.size(), "tryTransfer(null, long, TimeUnit) changed the queue");
        }
    }

    private void assertReturnsFalseAfterTimeout(final Q queue, final E element) throws Exception {
        long waited = bounded(() -> {
            long start = System.nanoTime();
            assertFalse(queue.tryTransfer(element, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS),
                    "tryTransfer(E, long, TimeUnit) returned true with no consumer");
            return System.nanoTime() - start;
        });
        assertTrue(waited >= TimeUnit.MILLISECONDS.toNanos(TIMED_WAIT_MILLIS),
                "tryTransfer(E, long, TimeUnit) returned false after " + waited + " ns, before its timeout of "
                        + TIMED_WAIT_MILLIS + " ms");
        assertFalse(queue.contains(element), "tryTransfer(E, long, TimeUnit) left the element enqueued");
    }
}
