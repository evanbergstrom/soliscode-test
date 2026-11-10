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

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
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

/// **Contract for `merge` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#merge` adds, beyond `MapContract`'s tests: a null key or value, where the map refuses them, and
/// a null function throw `NullPointerException`, the function even on an absent key, where it would not be called
/// (`Map` requires it); the function is not called for an absent key (`MapContract` checks only the value put); and a
/// `null` result removes the mapping.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#merge(Object, Object, BiFunction)
/// @since 0.1.2
public interface MergeContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `merge(null, v, f)` throws `NullPointerException`, where null keys are refused.
    @Test
    @DisplayName("merge(null, V, BiFunction) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void mergeWithNullKeyThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.MERGE) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        assertNullPointerAndUnchanged(map, () -> map.merge(null, value, (a, b) -> b),
                "merge(null, v, f)");
    }

    /// Tests that `merge(k, null, f)` throws `NullPointerException`, for a present key, where null values are refused.
    @Test
    @DisplayName("merge(K, null, BiFunction) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void mergeWithNullValueThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.MERGE) || permitNullValues()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        assertNullPointerAndUnchanged(map, () -> map.merge(key, null, (a, b) -> a),
                "merge(k, null, f)");
    }

    /// Tests that `merge(k, v, null)` throws `NullPointerException` for an absent key, where the function would not be
    /// called.
    @Test
    @DisplayName("merge(K, V, null) throws NullPointerException for an absent key")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void mergeWithNullFunctionThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.MERGE)) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        K key = keyProvider().createInstanceNotIn(map.keySet());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        assertNullPointerAndUnchanged(map, () -> map.merge(key, value, null),
                "merge(absent, v, null)");
    }

    /// Tests that `merge` does not call the function for an absent key, and maps the value.
    @Test
    @DisplayName("merge(K, V, BiFunction) does not call the function for an absent key")
    default void mergeWhenKeyNotPresentDoesNotCallFunction() {
        M map = provider().createInstance(defaultSize());
        K key = keyProvider().createInstanceNotIn(map.keySet());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        AtomicInteger calls = new AtomicInteger();
        if (supportsMethod(MapMethods.MERGE)) {
            assertEquals(value, map.merge(key, value, (a, b) -> {
                calls.incrementAndGet();
                return b;
            }));
            assertEquals(0, calls.get(), "merge called the function for an absent key");
            assertEquals(value, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.merge(key, value, (a, b) -> b));
        }
    }

    /// Tests that `merge` with a function that returns `null` removes the mapping and returns `null`.
    @Test
    @DisplayName("merge(K, V, BiFunction) removes the mapping when the function returns null")
    default void mergeWhenFunctionReturnsNullRemovesEntry() {
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        V value = valueProvider().createInstance(defaultSize() + 1L);
        BiFunction<V, V, @Nullable V> toNull = (a, b) -> null;
        if (supportsMethod(MapMethods.MERGE)) {
            int size = map.size();
            assertNull(map.merge(key, value, toNull));
            assertFalse(map.containsKey(key), "merge kept the mapping for a null result");
            assertEquals(size - 1, map.size());
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.merge(key, value, toNull));
        }
    }
}
