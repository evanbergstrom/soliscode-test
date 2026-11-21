/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.concurrentnavigablemap;

import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ConcurrentNavigableMapContractSupport;
import com.soliscode.test.methods.MapMethods;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `keySet()` of a `ConcurrentNavigableMap`**
///
/// "The set is backed by the map, so changes to the map are reflected in the set, and vice-versa. The set supports
/// element removal, which removes the corresponding mapping from the map, via the Iterator.remove, Set.remove,
/// removeAll, retainAll, and clear operations. It does not support the add or addAll operations." Removal is tested
/// through `Set.remove` and `Iterator.remove` only (looser). The set's order is `NavigableMapContract`'s test.
///
/// @param <M> the concurrent navigable map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentNavigableMap#keySet()
/// @since 0.1.2
public interface KeySetContract<M extends ConcurrentNavigableMap<K, V>, K, V>
        extends ConcurrentNavigableMapContractSupport<M, K, V> {

    /// Tests that `keySet()` is backed by the map, both ways, and refuses `add` and `addAll`.
    @Test
    @DisplayName("keySet() is backed by the map, both ways, and refuses add")
    default void concurrentKeySetWhenChangedWritesThroughAndRefusesAdd() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (!supportsMethod(MapMethods.KEY_SET)) {
            assertThrows(UnsupportedOperationException.class, map::keySet);
            return;
        }
        if (!canWriteThrough()) {
            return;
        }
        assertKeySetWritesThrough(map, map.keySet(), sortedReference(map, entries).firstKey(), "keySet()");
    }
}
