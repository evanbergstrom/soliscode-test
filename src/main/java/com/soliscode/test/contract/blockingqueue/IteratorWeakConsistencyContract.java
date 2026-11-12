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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.collection.CollectionMethods;
import com.soliscode.test.contract.iterable.IterableMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;
import com.soliscode.test.contract.support.WeakConsistencyChecks;
import com.soliscode.test.contract.support.WeaklyConsistentIteratorsSupport;

/// Contract that a blocking queue's `iterator()` is weakly consistent, as the `java.util.concurrent` package summary
/// says of "most concurrent Collection implementations (including most Queues)", and every JDK `BlockingQueue` keeps.
/// [BlockingQueue] does not say so itself: the contract reads it more strictly, and a test class whose queue has
/// fail-fast iterators overrides [#iteratorsAreWeaklyConsistent()] to return `false`.
///
/// The test runs the changes of [WeakConsistencyChecks], each on a new queue, where the queue supports the methods
/// that make them (`remove(Object)` and `add`).
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @since 0.1.3
/// @see WeakConsistencyChecks
public interface IteratorWeakConsistencyContract<Q extends BlockingQueue<E>, E>
        extends BlockingQueueContractSupport<Q, E>, WeaklyConsistentIteratorsSupport {

    /// Tests that the queue's iterator is weakly consistent: changed other than through it, during a traversal, it
    /// throws no `ConcurrentModificationException`, returns no element twice, returns every element the queue held
    /// throughout, and only elements it held.
    @Test
    @DisplayName("iterator() is weakly consistent: a change to the queue during a traversal is safe")
    default void iteratorWhenQueueChangesDuringTraversalIsWeaklyConsistent() {
        if (!iteratorsAreWeaklyConsistent()) {
            return;
        }
        List<E> all = elementProvider().createUniqueInstances(DEFAULT_SIZE + 1);
        WeakConsistencyChecks.checkCollection(this::queueOf, Collection::iterator,
                supportsMethod(CollectionMethods.REMOVE), supportsMethod(CollectionMethods.ADD),
                supportsMethod(IterableMethods.ITERATOR_FOR_EACH_REMAINING), all.subList(0, DEFAULT_SIZE),
                all.getLast(), "the queue's iterator()");
    }

    private Q queueOf(final List<E> elements) {
        return provider().createInstance(elements);
    }
}
