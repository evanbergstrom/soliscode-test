/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.concurrentnavigablemap;

import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentNavigableMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ConcurrentNavigableMapContractSupport;
import com.soliscode.test.methods.SortedMapMethods;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `subMap(K, K)` of a `ConcurrentNavigableMap`**
///
/// "The returned map is backed by this map, so changes in the returned map are reflected in this map, and
/// vice-versa" (`NavigableMap`, `SortedMap`), and a `null` bound throws `NullPointerException` where the map refuses
/// null keys. The view's contents are `NavigableMapContract`'s and `SortedMapContract`'s tests.
///
/// @param <M> the concurrent navigable map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentNavigableMap#subMap(Object, Object)
/// @since 0.1.2
public interface SubMapContract<M extends ConcurrentNavigableMap<K, V>, K, V>
        extends ConcurrentNavigableMapContractSupport<M, K, V> {

    /// Tests that `subMap(first, last)` is backed by the map, both ways.
    @Test
    @DisplayName("subMap(K, K) is backed by the map, both ways")
    default void concurrentSubMapWhenChangedWritesThroughBothWays() {
        if (defaultSize() < 2) {
            return;
        }
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        NavigableMap<K, V> expected = sortedReference(map, entries);
        K first = expected.firstKey();
        K last = expected.lastKey();
        if (!supportsMethod(SortedMapMethods.SUB_MAP)) {
            assertThrows(UnsupportedOperationException.class, () -> map.subMap(first, last));
            return;
        }
        if (!canWriteThrough()) {
            return;
        }
        assertMapViewWritesThrough(map, map.subMap(first, last), first, "subMap(first, last)");
    }

    /// Tests that `subMap(K, K)` throws `NullPointerException` for a `null` bound, where null keys are refused.
    @Test
    @DisplayName("subMap(K, K) throws NullPointerException for a null bound")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void concurrentSubMapWithNullBoundThrowsNullPointerException() {
        if (!supportsMethod(SortedMapMethods.SUB_MAP) || permitNullKeys() || defaultSize() < 1) {
            return;
        }
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        K key = entries.keySet().iterator().next();
        assertThrows(NullPointerException.class, () -> map.subMap(null, key), "subMap(null, key)");
        assertThrows(NullPointerException.class, () -> map.subMap(key, null), "subMap(key, null)");
    }
}
