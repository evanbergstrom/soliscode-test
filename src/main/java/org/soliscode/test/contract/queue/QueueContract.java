package org.soliscode.test.contract.queue;

import org.soliscode.test.contract.collection.CollectionContract;

import java.util.Queue;

/// Test suite for classes that implement the [Queue] interface. When implementing this class, the only method that will
/// need to be implemented is [org.soliscode.test.contract.support.CollectionContractSupport#provider()].
///
/// @param <E> The element type being tested.
/// @param <Q> The queue type being tested.
/// @author evanbergstrom
/// @since 1.0
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
