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
package com.soliscode.test.contract.concurrentmap;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.contract.support.ConcurrentMapContractSupport;
import com.soliscode.test.contract.support.WeakConsistencyChecks;
import com.soliscode.test.contract.support.WeaklyConsistentIteratorsSupport;

/// Contract that the iterators of a concurrent map's views, `keySet()`, `values()` and `entrySet()`, are weakly
/// consistent, as the `java.util.concurrent` package summary says of "most concurrent Collection implementations",
/// and `ConcurrentHashMap` and `ConcurrentSkipListMap` keep. [ConcurrentMap] does not say so itself (its sub-interface
/// [java.util.concurrent.ConcurrentNavigableMap] does, of its key sets): the contract reads it more strictly, and a
/// test class whose map's views have fail-fast iterators overrides [#iteratorsAreWeaklyConsistent()] to return
/// `false`.
///
/// Each test runs the changes of [WeakConsistencyChecks] on a view, each on a new map, changing the map other than
/// through the view (`remove(key)`, `put`), where the map supports the methods that make them.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
/// @see WeakConsistencyChecks
public interface ViewIteratorsWeakConsistencyContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V>, WeaklyConsistentIteratorsSupport {

    /// Tests that `keySet()`'s iterator is weakly consistent.
    @Test
    @DisplayName("keySet().iterator() is weakly consistent: a change to the map during a traversal is safe")
    default void keySetIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent() {
        if (supportsMethod(MapMethods.KEY_SET)) {
            checkViewIteratorWeakConsistency(m -> m.keySet().iterator(), (k, v) -> k, "keySet().iterator()");
        }
    }

    /// Tests that `values()`'s iterator is weakly consistent.
    @Test
    @DisplayName("values().iterator() is weakly consistent: a change to the map during a traversal is safe")
    default void valuesIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent() {
        if (supportsMethod(MapMethods.VALUES)) {
            checkViewIteratorWeakConsistency(m -> m.values().iterator(), (k, v) -> v, "values().iterator()");
        }
    }

    /// Tests that `entrySet()`'s iterator is weakly consistent.
    @Test
    @DisplayName("entrySet().iterator() is weakly consistent: a change to the map during a traversal is safe")
    default void entrySetIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent() {
        if (supportsMethod(MapMethods.ENTRY_SET)) {
            checkViewIteratorWeakConsistency(m -> m.entrySet().iterator(), AbstractMap.SimpleImmutableEntry::new,
                    "entrySet().iterator()");
        }
    }

    /// Runs the checks of [WeakConsistencyChecks#checkMapView] on a view's iterator, on maps of
    /// [#defaultSize()] entries (at least four) with unique keys and values, unless
    /// [#iteratorsAreWeaklyConsistent()] is `false`.
    ///
    /// @param iterators the iterator under test of a map
    /// @param element makes the view's element of a key and its value
    /// @param what the iterator, for failure messages
    /// @param <T> the view's element type
    default <T> void checkViewIteratorWeakConsistency(final Function<M, Iterator<T>> iterators,
                                                      final BiFunction<K, V, T> element, final String what) {
        if (!iteratorsAreWeaklyConsistent()) {
            return;
        }
        int size = Math.max(4, defaultSize());
        List<K> keys = keyProvider().createUniqueInstances(size + 1);
        List<V> values = valueProvider().createUniqueInstances(size + 1);
        Map<K, V> entries = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            entries.put(keys.get(i), values.get(i));
        }
        WeakConsistencyChecks.checkMapView(m -> provider().createInstance(m), entries, keys.getLast(),
                values.getLast(), iterators, element, supportsMethod(MapMethods.REMOVE),
                supportsMethod(MapMethods.PUT), what);
    }
}
