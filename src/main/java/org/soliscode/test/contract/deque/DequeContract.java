package org.soliscode.test.contract.deque;

import org.soliscode.test.contract.queue.QueueContract;

import java.util.Deque;

/// Test suite for classes that implement the [Deque] interface. When implementing this class, the only method that will
/// need to be implemented is [org.soliscode.test.contract.support.CollectionContractSupport#provider()].
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface DequeContract<E, D extends Deque<E>> extends QueueContract<E, D>,
        OfferFirstContract<E, D> {

    @Override
    default void doesNotSupportModification() {
        QueueContract.super.doesNotSupportModification();
        doesNotSupportMethod(DequeMethods.OFFER_FIRST);
        doesNotSupportMethod(DequeMethods.OFFER_LAST);
        doesNotSupportMethod(DequeMethods.POLL_FIRST);
        doesNotSupportMethod(DequeMethods.POLL_LAST);
        doesNotSupportMethod(DequeMethods.PUSH);
        doesNotSupportMethod(DequeMethods.POP);
    }
}
