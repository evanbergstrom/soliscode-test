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
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.contract.support.ConcurrentMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `compute` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#compute` adds, beyond `MapContract`'s tests: a null key, where the map refuses null keys, and a
/// null function throw `NullPointerException`; a `null` result removes a present mapping and leaves an absent key
/// absent, returning `null`; and the value returned is the value then mapped.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#compute(Object, BiFunction)
/// @since 0.1.2
public interface ComputeContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `compute(null, f)` throws `NullPointerException`, where null keys are refused.
    @Test
    @DisplayName("compute(null, BiFunction) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void computeWithNullKeyThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.COMPUTE) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        assertNullPointerAndUnchanged(map, () -> map.compute(null, (k, v) -> value),
                "compute(null, f)");
    }

    /// Tests that `compute(k, null)` throws `NullPointerException`, for a present key.
    @Test
    @DisplayName("compute(K, null) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void computeWithNullFunctionThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.COMPUTE)) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        assertNullPointerAndUnchanged(map, () -> map.compute(key, null),
                "compute(k, null)");
    }

    /// Tests that `compute` with a function that returns `null` removes a present mapping, and returns `null`.
    @Test
    @DisplayName("compute(K, BiFunction) removes a present mapping when the function returns null")
    default void computeWhenFunctionReturnsNullRemovesEntry() {
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        BiFunction<K, @Nullable V, @Nullable V> toNull = (k, v) -> null;
        if (supportsMethod(MapMethods.COMPUTE)) {
            int size = map.size();
            assertNull(map.compute(key, toNull));
            assertFalse(map.containsKey(key), "compute kept the mapping for a null result");
            assertEquals(size - 1, map.size());
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.compute(key, toNull));
        }
    }

    /// Tests that `compute` with a function that returns `null` leaves an absent key absent, and returns `null`.
    @Test
    @DisplayName("compute(K, BiFunction) leaves an absent key absent when the function returns null")
    default void computeWhenFunctionReturnsNullForAbsentKeyLeavesItAbsent() {
        M map = provider().createInstance(defaultSize());
        K key = keyProvider().createInstanceNotIn(map.keySet());
        BiFunction<K, @Nullable V, @Nullable V> toNull = (k, v) -> null;
        if (supportsMethod(MapMethods.COMPUTE)) {
            Map<K, V> before = new HashMap<>(map);
            assertNull(map.compute(key, toNull));
            assertFalse(map.containsKey(key), "compute added a mapping for a null result");
            assertEquals(before, new HashMap<>(map));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.compute(key, toNull));
        }
    }

    /// Tests that the value `compute` returns is the value then mapped to the key.
    @Test
    @DisplayName("compute(K, BiFunction) returns the value it maps")
    default void computeWhenCalledReturnsValueThenMapped() {
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        V value = valueProvider().createInstance(defaultSize() + 1L);
        if (supportsMethod(MapMethods.COMPUTE)) {
            V result = map.compute(key, (k, v) -> value);
            assertEquals(value, result);
            assertEquals(result, map.get(key), "compute returned a value other than the one it mapped");
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.compute(key, (k, v) -> value));
        }
    }
}
