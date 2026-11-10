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

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.navigablemap.NavigableMapMethods;
import com.soliscode.test.contract.support.ConcurrentNavigableMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `descendingMap()` of a `ConcurrentNavigableMap`**
///
/// "The descending map is backed by this map, so changes to the map are reflected in the descending map, and
/// vice-versa. The returned map has an ordering equivalent to `Collections.reverseOrder(comparator())`." The view's
/// other contents are `NavigableMapContract`'s tests.
///
/// @param <M> the concurrent navigable map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentNavigableMap#descendingMap()
/// @since 0.1.2
public interface DescendingMapContract<M extends ConcurrentNavigableMap<K, V>, K, V>
        extends ConcurrentNavigableMapContractSupport<M, K, V> {

    /// Tests that `descendingMap()` is backed by the map, both ways.
    @Test
    @DisplayName("descendingMap() is backed by the map, both ways")
    default void concurrentDescendingMapWhenChangedWritesThroughBothWays() {
        if (defaultSize() < 2) {
            return;
        }
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        NavigableMap<K, V> expected = sortedReference(map, entries);
        K first = expected.firstKey();
        if (!supportsMethod(NavigableMapMethods.DESCENDING_MAP)) {
            assertThrows(UnsupportedOperationException.class, () -> map.descendingMap());
            return;
        }
        if (!canWriteThrough()) {
            return;
        }
        assertMapViewWritesThrough(map, map.descendingMap(), first, "descendingMap()");
    }

    /// Tests that `descendingMap()` holds the keys in the order of `Collections.reverseOrder(comparator())`.
    @Test
    @DisplayName("descendingMap() holds the keys in reverse order of the comparator")
    default void concurrentDescendingMapWhenCalledHoldsKeysInReverseOrder() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (!supportsMethod(NavigableMapMethods.DESCENDING_MAP)) {
            assertThrows(UnsupportedOperationException.class, map::descendingMap);
            return;
        }
        TreeMap<K, V> expected = new TreeMap<>(Collections.reverseOrder(map.comparator()));
        expected.putAll(entries);
        ConcurrentNavigableMap<K, V> view = map.descendingMap();
        assertNotNull(view, "descendingMap()");
        assertIterableEquals(expected.keySet(), view.keySet(), "descendingMap()'s keys");
    }
}
