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

import com.soliscode.test.contract.blockingqueue.ThreadSafeBlockingQueueContract;

/// **Contract for the thread safety of a `TransferQueue`**
///
/// A `TransferQueue` is a [java.util.concurrent.BlockingQueue]: "BlockingQueue implementations are thread-safe. All
/// queuing methods achieve their effects atomically". This contract extends [TransferQueueContract] and
/// [ThreadSafeBlockingQueueContract] (so the `put`, `offer`, `poll`, `take` and `drainTo` tests run too) with a test
/// of each queuing method of its own under contention: `transfer` ([ThreadSafeTransferContract]) and `tryTransfer`
/// ([ThreadSafeTryTransferContract]). `hasWaitingConsumer` and `getWaitingConsumerCount` report "a momentary state of
/// affairs", with no atomicity to test.
///
/// The tests are tagged [com.soliscode.test.annotations.ConcurrencyTest] and
/// [com.soliscode.test.annotations.VerySlow], and run on a [com.soliscode.test.safety.ContentionCoordinator], which
/// bounds them in time.
///
/// ## Usage Examples
///
/// ```java
/// class LinkedTransferQueueSafetyTest extends AbstractTest
///         implements ThreadSafeTransferQueueContract<LinkedTransferQueue<Integer>, Integer>, WithIntegerElement {
///
///     @Override
///     public CollectionProvider<LinkedTransferQueue<Integer>, Integer> provider() {
///         return CollectionProviders.from(LinkedTransferQueue::new, LinkedTransferQueue::new,
///                 LinkedTransferQueue::new, PROVIDER);
///     }
/// }
/// ```
///
/// @param <Q> the transfer queue type being tested
/// @param <E> the element type being tested
/// @see TransferQueue
/// @since 0.1.2
public interface ThreadSafeTransferQueueContract<Q extends TransferQueue<E>, E> extends TransferQueueContract<Q, E>,
        ThreadSafeBlockingQueueContract<Q, E>,
        ThreadSafeTransferContract<Q, E>,
        ThreadSafeTryTransferContract<Q, E> {
}
