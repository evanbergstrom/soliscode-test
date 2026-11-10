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

import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.blockingqueue.BlockingQueueMethods;
import com.soliscode.test.contract.support.TransferQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `hasWaitingConsumer` method of a `TransferQueue`**
///
/// [TransferQueue#hasWaitingConsumer()] "Returns true if there is at least one consumer waiting to receive an element
/// via take() or timed poll. The return value represents a momentary state of affairs." The contract checks that it
/// returns `false` with no consumer waiting, and `true` while one waits in the timed `poll`; for the momentary state,
/// that check is repeated until it holds or the consumer returns by itself
/// ([TransferQueueContractSupport#spinUntil]).
///
/// @param <Q> the transfer queue type being tested
/// @param <E> the element type being tested
/// @see TransferQueue#hasWaitingConsumer()
/// @since 0.1.2
public interface HasWaitingConsumerContract<Q extends TransferQueue<E>, E> extends TransferQueueContractSupport<Q, E> {

    /// Tests that `hasWaitingConsumer` returns `false` with no consumer waiting, on an empty queue and on one
    /// holding elements; or, where it is not supported, throws [UnsupportedOperationException].
    @DisplayName("hasWaitingConsumer() returns false with no consumer waiting")
    @Test
    default void hasWaitingConsumerWithNoConsumerReturnsFalse() {
        Q empty = provider().emptyInstance();
        if (!supportsMethod(TransferQueueMethods.HAS_WAITING_CONSUMER)) {
            assertThrows(UnsupportedOperationException.class, empty::hasWaitingConsumer);
            return;
        }
        assertFalse(empty.hasWaitingConsumer(), "hasWaitingConsumer() returned true for an empty queue");
        Q queue = provider().createInstance(elementProvider().createUniqueInstances(DEFAULT_SIZE));
        assertFalse(queue.hasWaitingConsumer(), "hasWaitingConsumer() returned true for a queue with no consumer");
    }

    /// Tests that `hasWaitingConsumer` returns `true` while a consumer waits in the timed `poll`.
    @DisplayName("hasWaitingConsumer() returns true while a consumer waits")
    @Test
    default void hasWaitingConsumerWithWaitingConsumerReturnsTrue() {
        if (supportsMethod(TransferQueueMethods.HAS_WAITING_CONSUMER)
                && supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            AtomicBoolean result = new AtomicBoolean();
            withWaitingConsumers(queue, 1,
                    (q, anyReturned) -> result.set(spinUntil(q::hasWaitingConsumer, anyReturned)));
            assertTrue(result.get(), "hasWaitingConsumer() returned false while a consumer waited");
        }
    }
}
