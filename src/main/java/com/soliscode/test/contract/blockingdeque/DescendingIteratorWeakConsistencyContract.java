/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.blockingdeque;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.BlockingQueueContractSupport;
import com.soliscode.test.contract.support.WeakConsistencyChecks;
import com.soliscode.test.contract.support.WeaklyConsistentIteratorsSupport;
import com.soliscode.test.methods.CollectionMethods;
import com.soliscode.test.methods.DequeMethods;
import com.soliscode.test.methods.IterableMethods;

/// Contract that a blocking deque's `descendingIterator()` is weakly consistent, as its `iterator()` is
/// ([com.soliscode.test.contract.blockingqueue.IteratorWeakConsistencyContract]): the `java.util.concurrent` package
/// summary's reading, which every JDK `BlockingDeque` keeps and [BlockingDeque] does not state. A test class whose
/// deque has fail-fast iterators overrides [#iteratorsAreWeaklyConsistent()] to return `false`.
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @since 0.1.3
/// @see WeakConsistencyChecks
public interface DescendingIteratorWeakConsistencyContract<D extends BlockingDeque<E>, E>
        extends BlockingQueueContractSupport<D, E>, WeaklyConsistentIteratorsSupport {

    /// Tests that the deque's descending iterator is weakly consistent: changed other than through it, during a
    /// traversal, it throws no `ConcurrentModificationException`, returns no element twice, returns every element the
    /// deque held throughout, and only elements it held.
    @Test
    @DisplayName("descendingIterator() is weakly consistent: a change to the deque during a traversal is safe")
    default void descendingIteratorWhenDequeChangesDuringTraversalIsWeaklyConsistent() {
        if (!iteratorsAreWeaklyConsistent() || !supportsMethod(DequeMethods.DESCENDING_ITERATOR)) {
            return;
        }
        List<E> all = elementProvider().createUniqueInstances(DEFAULT_SIZE + 1);
        WeakConsistencyChecks.checkCollection(this::dequeOf, Deque::descendingIterator,
                supportsMethod(CollectionMethods.REMOVE), supportsMethod(CollectionMethods.ADD),
                supportsMethod(IterableMethods.ITERATOR_FOR_EACH_REMAINING), all.subList(0, DEFAULT_SIZE),
                all.getLast(), "the deque's descendingIterator()");
    }

    private D dequeOf(final List<E> elements) {
        return provider().createInstance(elements);
    }
}
