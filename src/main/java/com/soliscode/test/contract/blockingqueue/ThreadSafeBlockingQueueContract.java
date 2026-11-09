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

import java.util.concurrent.BlockingQueue;

/// **Contract for the thread safety of a `BlockingQueue`**
///
/// [BlockingQueue]: "BlockingQueue implementations are thread-safe. All queuing methods achieve their effects
/// atomically using internal locks or other forms of concurrency control. However, the bulk Collection operations
/// addAll, containsAll, retainAll and removeAll are not necessarily performed atomically unless specified otherwise
/// in an implementation." This contract extends [BlockingQueueContract] with a test of the queuing methods under
/// contention: `put`, `add` and `take` ([ThreadSafePutTakeContract]); `offer`, `poll`, `remove()` and the timed
/// forms ([ThreadSafeOfferPollContract]); and `drainTo` ([ThreadSafeDrainToContract]). Each checks that no element is
/// lost or delivered twice.
///
/// It does not extend [com.soliscode.test.contract.collection.ThreadSafeCollectionContract], which tests the bulk
/// operations too; a test of a queue that makes those atomic can implement both.
///
/// The tests are tagged [com.soliscode.test.annotations.ConcurrencyTest] and
/// [com.soliscode.test.annotations.VerySlow], and run on a [com.soliscode.test.safety.ContentionCoordinator], which
/// bounds them in time.
///
/// ## Usage Examples
///
/// ```java
/// class LinkedBlockingQueueSafetyTest extends AbstractTest
///         implements ThreadSafeBlockingQueueContract<LinkedBlockingQueue<Integer>, Integer>, WithIntegerElement {
///
///     @Override
///     public CollectionProvider<LinkedBlockingQueue<Integer>, Integer> provider() {
///         return CollectionProviders.from(LinkedBlockingQueue::new, LinkedBlockingQueue::new,
///                 LinkedBlockingQueue::new, PROVIDER);
///     }
/// }
/// ```
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue
/// @since 0.1.2
public interface ThreadSafeBlockingQueueContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContract<Q, E>,
        ThreadSafePutTakeContract<Q, E>,
        ThreadSafeOfferPollContract<Q, E>,
        ThreadSafeDrainToContract<Q, E> {
}
