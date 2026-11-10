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

import com.soliscode.test.contract.blockingqueue.BlockingQueueContract;

/// **Contract for the `TransferQueue` interface**
///
/// This contract extends [BlockingQueueContract] and adds a method contract for each method that [TransferQueue]
/// declares: `transfer` ([TransferContract]), `tryTransfer` ([TryTransferContract]), the timed `tryTransfer`
/// ([TryTransferWithTimeoutContract]), `hasWaitingConsumer` ([HasWaitingConsumerContract]) and
/// `getWaitingConsumerCount` ([GetWaitingConsumerCountContract]).
///
/// What the contract reads from [TransferQueue]'s Javadoc:
/// - **Consumers.** A waiting consumer is one "already waiting to receive it (in take() or timed poll)". The tests
///   start their consumers in the timed `poll`, so that a broken queue cannot hang them, and know one is waiting once
///   its thread is ([com.soliscode.test.contract.support.TransferQueueContractSupport#withWaitingConsumers]).
/// - **Capacity.** "Like other blocking queues, a TransferQueue may be capacity bounded. If so, an attempted transfer
///   operation may initially block waiting for available space, and/or subsequently block waiting for reception by a
///   consumer." The tests of a full queue run only on a bounded queue, as [BlockingQueueContract]'s do; the JDK's
///   only `TransferQueue`, `LinkedTransferQueue`, is unbounded.
/// - **The rest** is [BlockingQueueContract]'s: nulls refused, identity equality, and the interrupt reading.
/// - **Thread safety.** [ThreadSafeTransferQueueContract] tests it.
///
/// `TransferQueue`'s methods are not optional operations, but a test reads [TransferQueueMethods] as
/// [BlockingQueueContract] reads its methods: where a method is not supported, it expects
/// [UnsupportedOperationException].
///
/// ## Usage Examples
///
/// ```java
/// class LinkedTransferQueueTest extends AbstractTest
///         implements TransferQueueContract<LinkedTransferQueue<Integer>, Integer>, WithIntegerElement {
///
///     @Override
///     public CollectionProvider<LinkedTransferQueue<Integer>, Integer> provider() {
///         return CollectionProviders.from(LinkedTransferQueue::new, LinkedTransferQueue::new,
///                 LinkedTransferQueue::new, PROVIDER);
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the tests depends on
/// the [TransferQueue] and [com.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <Q> the transfer queue type being tested
/// @param <E> the element type being tested
/// @see TransferQueue
/// @since 0.1.2
public interface TransferQueueContract<Q extends TransferQueue<E>, E> extends BlockingQueueContract<Q, E>,
        TransferContract<Q, E>,
        TryTransferContract<Q, E>,
        TryTransferWithTimeoutContract<Q, E>,
        HasWaitingConsumerContract<Q, E>,
        GetWaitingConsumerCountContract<Q, E> {

    @Override
    default void doesNotSupportModification() {
        BlockingQueueContract.super.doesNotSupportModification();
        doesNotSupportMethod(TransferQueueMethods.TRANSFER);
        doesNotSupportMethod(TransferQueueMethods.TRY_TRANSFER);
        doesNotSupportMethod(TransferQueueMethods.TRY_TRANSFER_TIMEOUT);
    }
}
