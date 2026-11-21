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
import com.soliscode.test.methods.NavigableMapMethods;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `subMap(K, boolean, K, boolean)` of a `ConcurrentNavigableMap`**
///
/// "The returned map is backed by this map, so changes in the returned map are reflected in this map, and
/// vice-versa" (`NavigableMap`, `SortedMap`), and a `null` bound throws `NullPointerException` where the map refuses
/// null keys. The view's contents are `NavigableMapContract`'s and `SortedMapContract`'s tests.
///
/// @param <M> the concurrent navigable map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentNavigableMap#subMap(Object, boolean, Object, boolean)
/// @since 0.1.2
public interface SubMapFourArgContract<M extends ConcurrentNavigableMap<K, V>, K, V>
        extends ConcurrentNavigableMapContractSupport<M, K, V> {

    /// Tests that `subMap(first, true, last, true)` is backed by the map, both ways.
    @Test
    @DisplayName("subMap(K, true, K, true) is backed by the map, both ways")
    default void concurrentSubMapFourArgWhenChangedWritesThroughBothWays() {
        if (defaultSize() < 2) {
            return;
        }
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        NavigableMap<K, V> expected = sortedReference(map, entries);
        K first = expected.firstKey();
        K last = expected.lastKey();
        if (!supportsMethod(NavigableMapMethods.SUB_MAP_FOUR_ARG)) {
            assertThrows(UnsupportedOperationException.class, () -> map.subMap(first, true, last, true));
            return;
        }
        if (!canWriteThrough()) {
            return;
        }
        assertMapViewWritesThrough(map, map.subMap(first, true, last, true), first, "subMap(first, true, last, true)");
    }

    /// Tests that `subMap(K, boolean, K, boolean)` throws `NullPointerException` for a `null` bound, where null keys
    /// are refused.
    @Test
    @DisplayName("subMap(K, boolean, K, boolean) throws NullPointerException for a null bound")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void concurrentSubMapFourArgWithNullBoundThrowsNullPointerException() {
        if (!supportsMethod(NavigableMapMethods.SUB_MAP_FOUR_ARG) || permitNullKeys() || defaultSize() < 1) {
            return;
        }
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        K key = entries.keySet().iterator().next();
        assertThrows(NullPointerException.class, () -> map.subMap(null, true, key, true),
                "subMap(null, true, key, true)");
        assertThrows(NullPointerException.class, () -> map.subMap(key, true, null, true),
                "subMap(key, true, null, true)");
    }
}
