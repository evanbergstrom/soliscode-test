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
package com.soliscode.test.contract.concurrentnavigablemap;

import java.util.concurrent.ConcurrentNavigableMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.concurrentmap.ViewIteratorsWeakConsistencyContract;
import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.contract.navigablemap.NavigableMapMethods;

/// Contract that the iterators of a concurrent navigable map's key sets are weakly consistent, as
/// [ConcurrentNavigableMap] says of `keySet()`, `navigableKeySet()` and `descendingKeySet()`: "The view's iterators
/// and spliterators are weakly consistent."
///
/// A key set has two iterators, `iterator()` and `descendingIterator()` ([java.util.NavigableSet]); this contract
/// tests five of the six, and [ViewIteratorsWeakConsistencyContract] the sixth, `keySet().iterator()`. A test class
/// may turn them off with [#iteratorsAreWeaklyConsistent()], though the interface promises them.
///
/// @param <M> the concurrent navigable map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
/// @see com.soliscode.test.contract.support.WeakConsistencyChecks
public interface KeySetViewsWeakConsistencyContract<M extends ConcurrentNavigableMap<K, V>, K, V>
        extends ViewIteratorsWeakConsistencyContract<M, K, V> {

    /// Tests that `keySet()`'s descending iterator is weakly consistent.
    @Test
    @DisplayName("keySet().descendingIterator() is weakly consistent: a change to the map during a traversal is safe")
    default void keySetDescendingIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent() {
        if (supportsMethod(MapMethods.KEY_SET)) {
            checkViewIteratorWeakConsistency(m -> m.keySet().descendingIterator(), (k, v) -> k,
                    "keySet().descendingIterator()");
        }
    }

    /// Tests that `navigableKeySet()`'s iterator is weakly consistent.
    @Test
    @DisplayName("navigableKeySet().iterator() is weakly consistent: a change to the map during a traversal is safe")
    default void navigableKeySetIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent() {
        if (supportsMethod(NavigableMapMethods.NAVIGABLE_KEY_SET)) {
            checkViewIteratorWeakConsistency(m -> m.navigableKeySet().iterator(), (k, v) -> k,
                    "navigableKeySet().iterator()");
        }
    }

    /// Tests that `navigableKeySet()`'s descending iterator is weakly consistent.
    @Test
    @DisplayName("navigableKeySet().descendingIterator() is weakly consistent: a change to the map during a "
            + "traversal is safe")
    default void navigableKeySetDescendingIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent() {
        if (supportsMethod(NavigableMapMethods.NAVIGABLE_KEY_SET)) {
            checkViewIteratorWeakConsistency(m -> m.navigableKeySet().descendingIterator(), (k, v) -> k,
                    "navigableKeySet().descendingIterator()");
        }
    }

    /// Tests that `descendingKeySet()`'s iterator is weakly consistent.
    @Test
    @DisplayName("descendingKeySet().iterator() is weakly consistent: a change to the map during a traversal is safe")
    default void descendingKeySetIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent() {
        if (supportsMethod(NavigableMapMethods.DESCENDING_KEY_SET)) {
            checkViewIteratorWeakConsistency(m -> m.descendingKeySet().iterator(), (k, v) -> k,
                    "descendingKeySet().iterator()");
        }
    }

    /// Tests that `descendingKeySet()`'s descending iterator is weakly consistent.
    @Test
    @DisplayName("descendingKeySet().descendingIterator() is weakly consistent: a change to the map during a "
            + "traversal is safe")
    default void descendingKeySetDescendingIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent() {
        if (supportsMethod(NavigableMapMethods.DESCENDING_KEY_SET)) {
            checkViewIteratorWeakConsistency(m -> m.descendingKeySet().descendingIterator(), (k, v) -> k,
                    "descendingKeySet().descendingIterator()");
        }
    }
}
