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
package com.soliscode.test.contract.blockingdeque;

import java.util.concurrent.BlockingDeque;

import com.soliscode.test.contract.blockingqueue.BlockingQueueContract;
import com.soliscode.test.contract.deque.DequeContract;

/// **Contract for the `BlockingDeque` interface**
///
/// This contract extends [BlockingQueueContract] and [DequeContract], and adds a method contract for each method that
/// [BlockingDeque] declares of its own: `putFirst` and `putLast` ([PutFirstContract], [PutLastContract]),
/// `takeFirst` and `takeLast` ([TakeFirstContract], [TakeLastContract]), the timed `offerFirst` and `offerLast`
/// ([OfferFirstWithTimeoutContract], [OfferLastWithTimeoutContract]) and the timed `pollFirst` and `pollLast`
/// ([PollFirstWithTimeoutContract], [PollLastWithTimeoutContract]). [FullDequeContract] tests the five non-blocking
/// inserts that `BlockingDeque` restates for a full deque: `addFirst`, `addLast`, `push`, `offerFirst(E)` and
/// `offerLast(E)`.
///
/// What the contract reads from [BlockingDeque]'s Javadoc:
/// - "Like BlockingQueue, a BlockingDeque is thread safe, does not permit null elements, and may (or may not) be
///   capacity-constrained." Nulls, capacity, the tests of a full deque, the lower bound on the timed waits, the
///   reading of interruption and identity equality are as [BlockingQueueContract] reads them.
/// - **Order.** A deque has two ends, so the contract checks position: after `putFirst(e)`, `peekFirst()` is `e`;
///   `takeLast()` returns what `peekLast()` returned just before; and so on.
/// - **Thread safety.** [ThreadSafeBlockingDequeContract] tests it; this contract runs on one thread, except the
///   tests that a call waits for another thread.
/// - **Weakly consistent iterators, stricter**, as [BlockingQueueContract] reads them: `iterator()` and, by
///   [DescendingIteratorWeakConsistencyContract], `descendingIterator()`.
///
/// `BlockingDeque` says that the [java.util.concurrent.BlockingQueue] methods are "precisely equivalent" to its own
/// (`put` to `putLast`, `take` to `takeFirst`, and so on). The contract does not test that equivalence: the
/// inherited contracts test each `BlockingQueue` method by itself, at whichever end.
///
/// `BlockingDeque`'s methods are not optional operations, but a test reads [BlockingDequeMethods] as the queue
/// contracts read [com.soliscode.test.contract.queue.QueueMethods]: where a method is not supported, it expects
/// [UnsupportedOperationException].
///
/// ## Usage Examples
///
/// ```java
/// class LinkedBlockingDequeTest extends AbstractTest
///         implements BlockingDequeContract<LinkedBlockingDeque<Integer>, Integer>, WithIntegerElement {
///
///     @Override
///     public CollectionProvider<LinkedBlockingDeque<Integer>, Integer> provider() {
///         return CollectionProviders.from(LinkedBlockingDeque::new, LinkedBlockingDeque::new,
///                 LinkedBlockingDeque::new, PROVIDER);
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the tests depends on
/// the [BlockingDeque] and [com.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @see BlockingDeque
/// @since 0.1.2
public interface BlockingDequeContract<D extends BlockingDeque<E>, E> extends BlockingQueueContract<D, E>,
        DequeContract<D, E>,
        PutFirstContract<D, E>,
        PutLastContract<D, E>,
        TakeFirstContract<D, E>,
        TakeLastContract<D, E>,
        OfferFirstWithTimeoutContract<D, E>,
        OfferLastWithTimeoutContract<D, E>,
        PollFirstWithTimeoutContract<D, E>,
        PollLastWithTimeoutContract<D, E>,
        FullDequeContract<D, E>,
        DescendingIteratorWeakConsistencyContract<D, E> {

    @Override
    default void doesNotSupportModification() {
        BlockingQueueContract.super.doesNotSupportModification();
        DequeContract.super.doesNotSupportModification();
        doesNotSupportMethod(BlockingDequeMethods.PUT_FIRST);
        doesNotSupportMethod(BlockingDequeMethods.PUT_LAST);
        doesNotSupportMethod(BlockingDequeMethods.TAKE_FIRST);
        doesNotSupportMethod(BlockingDequeMethods.TAKE_LAST);
        doesNotSupportMethod(BlockingDequeMethods.OFFER_FIRST_TIMEOUT);
        doesNotSupportMethod(BlockingDequeMethods.OFFER_LAST_TIMEOUT);
        doesNotSupportMethod(BlockingDequeMethods.POLL_FIRST_TIMEOUT);
        doesNotSupportMethod(BlockingDequeMethods.POLL_LAST_TIMEOUT);
    }
}
