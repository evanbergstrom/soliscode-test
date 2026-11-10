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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.contract.support.ConcurrentMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `replace(key, value)` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#replace(Object, Object)` adds, beyond `MapContract`'s tests: a null key or value, where the map
/// refuses them, throws `NullPointerException` and leaves the map unchanged, and a key with no value is not added
/// (`MapContract` checks only the `null` return).
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#replace(Object, Object)
/// @since 0.1.2
public interface ReplaceTwoArgContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `replace(null, v)` throws `NullPointerException`, where null keys are refused.
    @Test
    @DisplayName("replace(null, V) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void replaceWithNullKeyThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.REPLACE_TWO_ARG) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        assertNullPointerAndUnchanged(map, () -> map.replace(null, value),
                "replace(null, v)");
    }

    /// Tests that `replace(k, null)` throws `NullPointerException`, for a present key, where null values are refused.
    @Test
    @DisplayName("replace(K, null) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void replaceWithNullValueThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.REPLACE_TWO_ARG) || permitNullValues()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        assertNullPointerAndUnchanged(map, () -> map.replace(key, null),
                "replace(k, null)");
    }

    /// Tests that `replace(k, v)` on a key with no value returns `null` and adds no mapping.
    @Test
    @DisplayName("replace(K, V) does not add a key that has no value")
    default void replaceWhenKeyNotPresentDoesNotAddEntry() {
        M map = provider().createInstance(defaultSize());
        K key = keyProvider().createInstanceNotIn(map.keySet());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        if (supportsMethod(MapMethods.REPLACE_TWO_ARG)) {
            Map<K, V> before = new HashMap<>(map);
            assertNull(map.replace(key, value));
            assertFalse(map.containsKey(key), "replace(k, v) added a key that had no value");
            assertEquals(before, new HashMap<>(map));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, value));
        }
    }
}
