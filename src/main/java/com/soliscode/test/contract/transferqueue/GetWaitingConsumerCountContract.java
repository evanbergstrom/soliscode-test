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
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.blockingqueue.BlockingQueueMethods;
import com.soliscode.test.contract.support.TransferQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `getWaitingConsumerCount` method of a `TransferQueue`**
///
/// [TransferQueue#getWaitingConsumerCount()] "Returns an estimate of the number of consumers waiting to receive
/// elements via take() or timed poll. The return value is an approximation of a momentary state of affairs, that may
/// be inaccurate if consumers have completed or given up waiting." The contract checks that it returns 0 with no
/// consumer waiting, and 2 while two wait in the timed `poll`.
///
/// *Stricter reading:* with consumers known to be waiting, none of them completed or given up, the count is read as
/// exact: the Javadoc names only those as making it inaccurate. For the momentary state, the check is repeated until
/// it holds or a consumer returns by itself ([TransferQueueContractSupport#spinUntil]).
///
/// @param <Q> the transfer queue type being tested
/// @param <E> the element type being tested
/// @see TransferQueue#getWaitingConsumerCount()
/// @since 0.1.2
public interface GetWaitingConsumerCountContract<Q extends TransferQueue<E>, E>
        extends TransferQueueContractSupport<Q, E> {

    /// The number of consumers that wait in [#getWaitingConsumerCountWithWaitingConsumersReturnsCount].
    int WAITING_CONSUMERS = 2;

    /// Tests that `getWaitingConsumerCount` returns 0 with no consumer waiting, on an empty queue and on one
    /// holding elements; or, where it is not supported, throws [UnsupportedOperationException].
    @DisplayName("getWaitingConsumerCount() returns 0 with no consumer waiting")
    @Test
    default void getWaitingConsumerCountWithNoConsumerReturnsZero() {
        Q empty = provider().emptyInstance();
        if (!supportsMethod(TransferQueueMethods.GET_WAITING_CONSUMER_COUNT)) {
            assertThrows(UnsupportedOperationException.class, empty::getWaitingConsumerCount);
            return;
        }
        assertEquals(0, empty.getWaitingConsumerCount(), "getWaitingConsumerCount() of an empty queue");
        Q queue = provider().createInstance(elementProvider().createUniqueInstances(DEFAULT_SIZE));
        assertEquals(0, queue.getWaitingConsumerCount(), "getWaitingConsumerCount() of a queue with no consumer");
    }

    /// Tests that `getWaitingConsumerCount` returns the number of consumers waiting in the timed `poll`.
    @DisplayName("getWaitingConsumerCount() returns the number of consumers waiting")
    @Test
    default void getWaitingConsumerCountWithWaitingConsumersReturnsCount() {
        if (supportsMethod(TransferQueueMethods.GET_WAITING_CONSUMER_COUNT)
                && supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            AtomicInteger seen = new AtomicInteger(-1);
            withWaitingConsumers(queue, WAITING_CONSUMERS, (q, anyReturned) -> spinUntil(() -> {
                seen.set(q.getWaitingConsumerCount());
                return seen.get() == WAITING_CONSUMERS;
            }, anyReturned));
            assertEquals(WAITING_CONSUMERS, seen.get(), "getWaitingConsumerCount() while consumers waited");
        }
    }
}
