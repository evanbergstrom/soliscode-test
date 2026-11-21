/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.blockingqueue;

import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.BlockingQueueContractSupport;
import com.soliscode.test.methods.BlockingQueueMethods;
import com.soliscode.test.methods.CollectionMethods;
import com.soliscode.test.methods.QueueMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `remainingCapacity` method of a `BlockingQueue`**
///
/// [BlockingQueue#remainingCapacity()] returns "the number of additional elements that this queue can ideally (in
/// the absence of memory or resource constraints) accept without blocking, or `Integer.MAX_VALUE` if there is no
/// intrinsic limit". The contract checks that, with no other thread, it is one less after an element is added and
/// one more after one is removed, and that a queue reporting no room refuses an `offer`.
///
/// *Lenient reading:* "A BlockingQueue without any intrinsic capacity constraints always reports a remaining
/// capacity of Integer.MAX_VALUE"; a queue bounded at `Integer.MAX_VALUE`, as `new LinkedBlockingQueue<>()` is,
/// reports `Integer.MAX_VALUE` less its size. The contract accepts both: where the capacity was `Integer.MAX_VALUE`,
/// it may stay `Integer.MAX_VALUE` after an element is added.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#remainingCapacity()
/// @since 0.1.2
public interface RemainingCapacityContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// Tests that `remainingCapacity` falls by one when an element is added and rises by one when it is removed,
    /// and that a queue reporting no room refuses an `offer`; or, where it is not supported, throws
    /// [UnsupportedOperationException].
    @DisplayName("remainingCapacity() falls by one per element added and rises by one per element removed")
    @Test
    default void remainingCapacityTracksAddedAndRemovedElements() {
        Q queue = provider().emptyInstance();
        if (!supportsMethod(BlockingQueueMethods.REMAINING_CAPACITY)) {
            assertThrows(UnsupportedOperationException.class, queue::remainingCapacity);
            return;
        }
        if (!supportsMethods(QueueMethods.OFFER, CollectionMethods.ADD, QueueMethods.POLL)) {
            return;
        }
        E element = elementProvider().createInstance();
        int before = queue.remainingCapacity();
        if (before == 0) {
            assertFalse(queue.offer(element), "offer(E) accepted an element where remainingCapacity() was 0");
            return;
        }
        assertTrue(queue.add(element));
        int after = queue.remainingCapacity();
        if (before == Integer.MAX_VALUE && after == Integer.MAX_VALUE) {
            return;
        }
        assertEquals(before - 1, after, "remainingCapacity() after an element was added");
        queue.poll();
        assertEquals(before, queue.remainingCapacity(), "remainingCapacity() after the element was removed");
    }
}
