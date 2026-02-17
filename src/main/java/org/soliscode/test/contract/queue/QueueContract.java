package org.soliscode.test.contract.queue;

import org.soliscode.test.contract.collection.CollectionContract;

import java.util.Queue;

/// **Contract for the `Queue` interface**
///
/// This contract interface provides a comprehensive suite of tests for implementations
/// of the [Queue] interface. It is designed to be used as a mix-in interface by test
/// classes that verify queue-based collections.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a queue implementation correctly:
/// - Implements all [Queue] methods according to their specifications.
/// - Correctly handles insertions, removals, and retrievals from the head of the queue.
/// - Inherits all tests from [CollectionContract].
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyQueueContractTest implements QueueContract<String, MyQueue<String>> {
///     @Override
///     public CollectionProvider<String, MyQueue<String>> provider() {
///         return MyQueue::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Queue] and [org.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <Q> The queue type being tested.
/// @author evanbergstrom
/// @see Queue
/// @since 1.0.0
public interface QueueContract<E, Q extends Queue<E>> extends CollectionContract<E, Q>,
        ElementContract<E, Q>,
        OfferContract<E, Q>,
        PeekContract<E, Q>,
        PollContract<E, Q>,
        RemoveContract<E, Q> {

    @Override
    default void doesNotSupportModification() {
        CollectionContract.super.doesNotSupportModification();
        doesNotSupportMethod(QueueMethods.OFFER);
        doesNotSupportMethod(QueueMethods.POLL);
        doesNotSupportMethod(QueueMethods.REMOVE);
    }
}
