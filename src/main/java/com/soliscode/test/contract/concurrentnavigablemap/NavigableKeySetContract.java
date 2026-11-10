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
import java.util.concurrent.ConcurrentNavigableMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.navigablemap.NavigableMapMethods;
import com.soliscode.test.contract.support.ConcurrentNavigableMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `navigableKeySet()` of a `ConcurrentNavigableMap`**
///
/// "The set is backed by the map, so changes to the map are reflected in the set, and vice-versa. The set supports
/// element removal, which removes the corresponding mapping from the map, via the Iterator.remove, Set.remove,
/// removeAll, retainAll, and clear operations. It does not support the add or addAll operations." Removal is tested
/// through `Set.remove` and `Iterator.remove` only (looser). The set's order is `NavigableMapContract`'s test.
///
/// @param <M> the concurrent navigable map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentNavigableMap#navigableKeySet()
/// @since 0.1.2
public interface NavigableKeySetContract<M extends ConcurrentNavigableMap<K, V>, K, V>
        extends ConcurrentNavigableMapContractSupport<M, K, V> {

    /// Tests that `navigableKeySet()` is backed by the map, both ways, and refuses `add` and `addAll`.
    @Test
    @DisplayName("navigableKeySet() is backed by the map, both ways, and refuses add")
    default void concurrentNavigableKeySetWhenChangedWritesThroughAndRefusesAdd() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (!supportsMethod(NavigableMapMethods.NAVIGABLE_KEY_SET)) {
            assertThrows(UnsupportedOperationException.class, map::navigableKeySet);
            return;
        }
        if (!canWriteThrough()) {
            return;
        }
        assertKeySetWritesThrough(map, map.navigableKeySet(), sortedReference(map, entries).firstKey(),
                "navigableKeySet()");
    }
}
