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

/// **Contract for `tailMap(K, boolean)` of a `ConcurrentNavigableMap`**
///
/// "The returned map is backed by this map, so changes in the returned map are reflected in this map, and
/// vice-versa" (`NavigableMap`, `SortedMap`), and a `null` bound throws `NullPointerException` where the map refuses
/// null keys. The view's contents are `NavigableMapContract`'s and `SortedMapContract`'s tests.
///
/// @param <M> the concurrent navigable map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentNavigableMap#tailMap(Object, boolean)
/// @since 0.1.2
public interface TailMapTwoArgContract<M extends ConcurrentNavigableMap<K, V>, K, V>
        extends ConcurrentNavigableMapContractSupport<M, K, V> {

    /// Tests that `tailMap(first, true)` is backed by the map, both ways.
    @Test
    @DisplayName("tailMap(K, true) is backed by the map, both ways")
    default void concurrentTailMapTwoArgWhenChangedWritesThroughBothWays() {
        if (defaultSize() < 2) {
            return;
        }
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        NavigableMap<K, V> expected = sortedReference(map, entries);
        K first = expected.firstKey();
        K last = expected.lastKey();
        if (!supportsMethod(NavigableMapMethods.TAIL_MAP_TWO_ARG)) {
            assertThrows(UnsupportedOperationException.class, () -> map.tailMap(first, true));
            return;
        }
        if (!canWriteThrough()) {
            return;
        }
        assertMapViewWritesThrough(map, map.tailMap(first, true), last, "tailMap(first, true)");
    }

    /// Tests that `tailMap(K, boolean)` throws `NullPointerException` for a `null` bound, where null keys are refused.
    @Test
    @DisplayName("tailMap(K, boolean) throws NullPointerException for a null bound")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void concurrentTailMapTwoArgWithNullBoundThrowsNullPointerException() {
        if (!supportsMethod(NavigableMapMethods.TAIL_MAP_TWO_ARG) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        assertThrows(NullPointerException.class, () -> map.tailMap(null, true), "tailMap(null, true)");
    }
}
