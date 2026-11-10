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

import java.util.List;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.blockingqueue.BlockingQueueMethods;
import com.soliscode.test.contract.support.TransferQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `tryTransfer(E)` method of a `TransferQueue`**
///
/// [TransferQueue#tryTransfer(Object)] "transfers the specified element immediately if there exists a consumer
/// already waiting to receive it (in take() or timed poll), otherwise returning false without enqueuing the element".
/// The contract checks that it returns `false` and leaves the queue unchanged when no consumer is waiting, returns
/// `true` and hands the element over when one is, and refuses `null`.
///
/// @param <Q> the transfer queue type being tested
/// @param <E> the element type being tested
/// @see TransferQueue#tryTransfer(Object)
/// @since 0.1.2
public interface TryTransferContract<Q extends TransferQueue<E>, E> extends TransferQueueContractSupport<Q, E> {

    /// Tests that `tryTransfer`, with no consumer waiting, returns `false` without enqueuing the element, on an
    /// empty queue and on one holding elements; or, where it is not supported, throws
    /// [UnsupportedOperationException].
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("tryTransfer(E) with no consumer returns false without enqueuing the element")
    @Test
    default void tryTransferWithNoConsumerReturnsFalseWithoutEnqueuing() throws Exception {
        Q empty = provider().emptyInstance();
        E element = elementProvider().createInstance();
        if (!supportsMethod(TransferQueueMethods.TRY_TRANSFER)) {
            assertThrows(UnsupportedOperationException.class, () -> bounded(() -> empty.tryTransfer(element)));
            return;
        }
        assertFalse(bounded(() -> empty.tryTransfer(element)), "tryTransfer(E) returned true with no consumer");
        assertFalse(empty.contains(element), "tryTransfer(E) enqueued the element");
        assertEquals(0, empty.size(), "tryTransfer(E) changed the queue");

        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Q queue = provider().createInstance(values);
        E extra = elementProvider().createInstanceNotIn(values);
        assertFalse(bounded(() -> queue.tryTransfer(extra)), "tryTransfer(E) returned true with no consumer");
        assertFalse(queue.contains(extra), "tryTransfer(E) enqueued the element");
        assertEquals(values.size(), queue.size(), "tryTransfer(E) changed the queue");
    }

    /// Tests that `tryTransfer`, with a consumer waiting in the timed `poll`, returns `true` and hands the element
    /// over. The call is repeated until it returns `true`, or the consumer returns by itself.
    @DisplayName("tryTransfer(E) with a waiting consumer returns true and hands the element over")
    @Test
    default void tryTransferWithWaitingConsumerReturnsTrue() {
        if (supportsMethod(TransferQueueMethods.TRY_TRANSFER) && supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            E element = elementProvider().createInstance();
            AtomicBoolean result = new AtomicBoolean();
            List<@Nullable E> received = withWaitingConsumers(queue, 1,
                    (q, anyReturned) -> result.set(spinUntil(() -> q.tryTransfer(element), anyReturned)));
            assertTrue(result.get(), "tryTransfer(E) returned false while a consumer waited");
            assertEquals(element, received.get(0), "the waiting consumer did not receive the element");
            assertEquals(0, queue.size(), "tryTransfer(E) to a waiting consumer left elements in the queue");
        }
    }

    /// Tests that `tryTransfer(null)` throws [NullPointerException].
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("tryTransfer(null) throws NullPointerException")
    @Test
    // NullAway: the test passes null on purpose, to check that tryTransfer refuses it.
    @SuppressWarnings("NullAway")
    default void tryTransferWithNullThrowsNullPointerException() throws Exception {
        if (supportsMethod(TransferQueueMethods.TRY_TRANSFER)) {
            Q queue = provider().emptyInstance();
            assertThrows(NullPointerException.class, () -> bounded(() -> queue.tryTransfer(null)));
            assertEquals(0, queue.size(), "tryTransfer(null) changed the queue");
        }
    }
}
