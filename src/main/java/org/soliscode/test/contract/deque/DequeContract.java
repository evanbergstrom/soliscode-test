package org.soliscode.test.contract.deque;

import org.soliscode.test.contract.queue.QueueContract;
import org.soliscode.test.contract.sequencedcollection.SequencedCollectionContract;

import java.util.Deque;

/// Test suite for classes that implement the [Deque] interface. When implementing this class, the only method that will
/// need to be implemented is [org.soliscode.test.contract.support.CollectionContractSupport#provider()].
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface DequeContract<E, D extends Deque<E>> extends QueueContract<E, D>, SequencedCollectionContract<E, D>,
        OfferFirstContract<E, D>,
        OfferLastContract<E, D>,
        PollFirstContract<E, D>,
        PollLastContract<E, D>,
        PeekFirstContract<E, D>,
        PeekLastContract<E, D>,
        RemoveFirstOccurrenceContract<E, D>,
        RemoveLastOccurrenceContract<E, D>,
        PushContract<E, D>,
        PopContract<E, D>,
        DescendingIteratorContract<E, D> {

    @Override
    default void doesNotSupportModification() {
        QueueContract.super.doesNotSupportModification();
        doesNotSupportMethod(DequeMethods.OFFER_FIRST);
        doesNotSupportMethod(DequeMethods.OFFER_LAST);
        doesNotSupportMethod(DequeMethods.POLL_FIRST);
        doesNotSupportMethod(DequeMethods.POLL_LAST);
        doesNotSupportMethod(DequeMethods.REMOVE_FIRST_OCCURRENCES);
        doesNotSupportMethod(DequeMethods.REMOVE_LAST_OCCURRENCES);
        doesNotSupportMethod(DequeMethods.PUSH);
        doesNotSupportMethod(DequeMethods.POP);
    }
}
