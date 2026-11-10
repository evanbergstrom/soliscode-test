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

/// **Contract for `remove(key, value)` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#remove(Object, Object)` adds, beyond `MapContract`'s tests: a null key or value, where the map
/// refuses them, throws `NullPointerException` or, as the JDK makes the exception "(optional)", returns `false`; either
/// way the map is unchanged.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#remove(Object, Object)
/// @since 0.1.2
public interface RemoveTwoArgContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `remove(null, v)` throws `NullPointerException` or returns `false`, where null keys are refused.
    @Test
    @DisplayName("remove(null, V) throws NullPointerException or returns false")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void removeWithNullKeyThrowsNullPointerExceptionOrReturnsFalse() {
        if (!supportsMethod(MapMethods.REMOVE_TWO_ARG) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        assertResultOrNullPointerAndUnchanged(map, false, () -> map.remove(null, value), "remove(null, v)");
    }

    /// Tests that `remove(k, null)` throws `NullPointerException` or returns `false`, for a present key, where null
    /// values are refused.
    @Test
    @DisplayName("remove(K, null) throws NullPointerException or returns false")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void removeWithNullValueThrowsNullPointerExceptionOrReturnsFalse() {
        if (!supportsMethod(MapMethods.REMOVE_TWO_ARG) || permitNullValues()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        assertResultOrNullPointerAndUnchanged(map, false, () -> map.remove(key, null), "remove(k, null)");
    }
}
