/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.blockingqueue;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.BlockingQueueContractSupport;
import com.soliscode.test.methods.CollectionMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `add` method of a capacity-bounded `BlockingQueue`**
///
/// [BlockingQueue#add(Object)] throws [IllegalStateException] "if the element cannot be added at this time due to
/// capacity restrictions". The contract checks that on a full queue it throws that, and leaves the queue unchanged.
/// [com.soliscode.test.contract.collection.AddContract] tests the rest of `add`.
///
/// It runs only on a queue with at most [BlockingQueueContractSupport#FILL_LIMIT] places
/// ([BlockingQueueContractSupport#fullInstance]).
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#add(Object)
/// @since 0.1.2
public interface AddContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// Tests that `add` on a full queue throws [IllegalStateException] and does not add the element.
    @DisplayName("add(E) on a full queue throws IllegalStateException")
    @Test
    default void addWhenFullThrowsIllegalStateException() {
        if (supportsMethod(CollectionMethods.ADD)) {
            Optional<Q> full = fullInstance();
            if (full.isEmpty()) {
                return;
            }
            Q queue = full.get();
            int size = queue.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(queue));
            assertThrows(IllegalStateException.class, () -> queue.add(extra));
            assertFalse(queue.contains(extra), "add(E) added the element to a full queue");
            assertEquals(size, queue.size());
        }
    }
}
