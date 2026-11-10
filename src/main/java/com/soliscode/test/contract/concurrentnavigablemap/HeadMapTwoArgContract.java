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

import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentNavigableMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.navigablemap.NavigableMapMethods;
import com.soliscode.test.contract.support.ConcurrentNavigableMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `headMap(K, boolean)` of a `ConcurrentNavigableMap`**
///
/// "The returned map is backed by this map, so changes in the returned map are reflected in this map, and
/// vice-versa" (`NavigableMap`, `SortedMap`), and a `null` bound throws `NullPointerException` where the map refuses
/// null keys. The view's contents are `NavigableMapContract`'s and `SortedMapContract`'s tests.
///
/// @param <M> the concurrent navigable map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentNavigableMap#headMap(Object, boolean)
/// @since 0.1.2
public interface HeadMapTwoArgContract<M extends ConcurrentNavigableMap<K, V>, K, V>
        extends ConcurrentNavigableMapContractSupport<M, K, V> {

    /// Tests that `headMap(last, true)` is backed by the map, both ways.
    @Test
    @DisplayName("headMap(K, true) is backed by the map, both ways")
    default void concurrentHeadMapTwoArgWhenChangedWritesThroughBothWays() {
        if (defaultSize() < 2) {
            return;
        }
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        NavigableMap<K, V> expected = sortedReference(map, entries);
        K first = expected.firstKey();
        K last = expected.lastKey();
        if (!supportsMethod(NavigableMapMethods.HEAD_MAP_TWO_ARG)) {
            assertThrows(UnsupportedOperationException.class, () -> map.headMap(last, true));
            return;
        }
        if (!canWriteThrough()) {
            return;
        }
        assertMapViewWritesThrough(map, map.headMap(last, true), first, "headMap(last, true)");
    }

    /// Tests that `headMap(K, boolean)` throws `NullPointerException` for a `null` bound, where null keys are refused.
    @Test
    @DisplayName("headMap(K, boolean) throws NullPointerException for a null bound")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void concurrentHeadMapTwoArgWithNullBoundThrowsNullPointerException() {
        if (!supportsMethod(NavigableMapMethods.HEAD_MAP_TWO_ARG) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        assertThrows(NullPointerException.class, () -> map.headMap(null, true), "headMap(null, true)");
    }
}
