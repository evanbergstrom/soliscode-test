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

import com.soliscode.test.contract.DoesNotPermitNulls;
import com.soliscode.test.contract.queue.QueueContract;

/// **Contract for the `BlockingQueue` interface**
///
/// This contract extends [QueueContract] and adds a method contract for each method that [BlockingQueue] declares
/// with a meaning of its own: `put` ([PutContract]), `take` ([TakeContract]), the timed `offer`
/// ([OfferWithTimeoutContract]) and `poll` ([PollWithTimeoutContract]), `remainingCapacity`
/// ([RemainingCapacityContract]), both forms of `drainTo` ([DrainToContract], [DrainToMaxElementsContract]), and
/// `add` and `offer` on a full queue ([AddContract], [OfferContract]). `BlockingQueue` restates `remove(Object)` and
/// `contains(Object)` with no meaning of their own; the inherited contracts test them.
///
/// What the contract reads from [BlockingQueue]'s Javadoc:
/// - **Nulls.** "A BlockingQueue does not accept null elements": the contract is [DoesNotPermitNulls].
/// - **Capacity.** A queue "may be capacity bounded". The tests of a full queue run only on a queue with at most
///   [com.soliscode.test.contract.support.BlockingQueueContractSupport#FILL_LIMIT] places left when empty; a larger
///   one, an unbounded one among them, is not tested when full. The inherited tests add some tens of elements
///   ([com.soliscode.test.contract.support.CollectionContractSupport#DEFAULT_SIZE] and more), so a bounded queue
///   needs room for them: the coverage test uses a capacity of 100.
/// - **Order.** A queue need not be FIFO, so no test checks the order of elements: `take` and `poll` are checked
///   against `peek()`, and `drainTo` by the elements it moves.
/// - **Equality.** [java.util.Queue] implementations "generally do not define element-based versions of methods
///   equals and hashCode but instead inherit the identity based versions from class Object", as
///   `ArrayBlockingQueue` and `LinkedBlockingQueue` do. So [#equalsComparesValues()] is `false`, and the tests of
///   `equals` and `hashCode` that compare a queue with its copy do not run. A test of a queue that compares its
///   elements overrides it to return `true`.
/// - **Thread safety.** [ThreadSafeBlockingQueueContract] tests it; this contract runs on one thread, except the
///   tests that `put` and `take` wait for another thread.
///
/// `BlockingQueue`'s methods are not optional operations, but a test reads [BlockingQueueMethods] as the queue
/// contracts read [com.soliscode.test.contract.queue.QueueMethods]: where a method is not supported, it expects
/// [UnsupportedOperationException].
///
/// A call that may block runs with a time limit, so that a queue that blocks where it should not fails the test
/// instead of hanging it ([com.soliscode.test.contract.support.BlockingQueueContractSupport#bounded]).
///
/// ## Usage Examples
///
/// ```java
/// class ArrayBlockingQueueTest extends AbstractTest
///         implements BlockingQueueContract<ArrayBlockingQueue<Integer>, Integer>, WithIntegerElement {
///
///     @Override
///     public CollectionProvider<ArrayBlockingQueue<Integer>, Integer> provider() {
///         return CollectionProviders.from(() -> new ArrayBlockingQueue<>(100),
///                 q -> new ArrayBlockingQueue<>(100, false, q),
///                 c -> new ArrayBlockingQueue<>(100, false, c), PROVIDER);
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the tests depends on
/// the [BlockingQueue] and [com.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue
/// @since 0.1.2
public interface BlockingQueueContract<Q extends BlockingQueue<E>, E> extends QueueContract<Q, E>, DoesNotPermitNulls,
        PutContract<Q, E>,
        TakeContract<Q, E>,
        OfferWithTimeoutContract<Q, E>,
        PollWithTimeoutContract<Q, E>,
        RemainingCapacityContract<Q, E>,
        AddContract<Q, E>,
        OfferContract<Q, E>,
        DrainToContract<Q, E>,
        DrainToMaxElementsContract<Q, E> {

    /// Returns `false`: a [BlockingQueue] keeps [Object]'s identity equality, as `Queue` says queues generally do.
    /// A test of a queue that compares its elements overrides it.
    ///
    /// @return `false`
    @Override
    default boolean equalsComparesValues() {
        return false;
    }

    @Override
    default void doesNotSupportModification() {
        QueueContract.super.doesNotSupportModification();
        doesNotSupportMethod(BlockingQueueMethods.PUT);
        doesNotSupportMethod(BlockingQueueMethods.TAKE);
        doesNotSupportMethod(BlockingQueueMethods.OFFER_TIMEOUT);
        doesNotSupportMethod(BlockingQueueMethods.POLL_TIMEOUT);
        doesNotSupportMethod(BlockingQueueMethods.DRAIN_TO);
        doesNotSupportMethod(BlockingQueueMethods.DRAIN_TO_MAX_ELEMENTS);
    }
}
