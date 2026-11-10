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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.contract.support.ConcurrentMapContractSupport;

/// **Contract for `putIfAbsent` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#putIfAbsent` adds, beyond `MapContract`'s tests: a null key or value, where the map refuses
/// them, throws `NullPointerException` and leaves the map unchanged.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#putIfAbsent(Object, Object)
/// @since 0.1.2
public interface PutIfAbsentContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `putIfAbsent(null, v)` throws `NullPointerException`, where null keys are refused.
    @Test
    @DisplayName("putIfAbsent(null, V) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void putIfAbsentWithNullKeyThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.PUT_IF_ABSENT) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        assertNullPointerAndUnchanged(map, () -> map.putIfAbsent(null, value),
                "putIfAbsent(null, v)");
    }

    /// Tests that `putIfAbsent(k, null)` throws `NullPointerException`, where null values are refused.
    @Test
    @DisplayName("putIfAbsent(K, null) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void putIfAbsentWithNullValueThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.PUT_IF_ABSENT) || permitNullValues()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        K key = keyProvider().createInstanceNotIn(map.keySet());
        assertNullPointerAndUnchanged(map, () -> map.putIfAbsent(key, null),
                "putIfAbsent(k, null)");
    }
}
