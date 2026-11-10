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

/// **Contract for `getOrDefault` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#getOrDefault` adds to `Map`'s, beyond `MapContract`'s tests: a null key, where the map refuses
/// null keys, throws `NullPointerException` or, as `Map#getOrDefault` makes the exception optional, returns the
/// default.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#getOrDefault(Object, Object)
/// @since 0.1.2
public interface GetOrDefaultContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `getOrDefault(null, d)` throws `NullPointerException` or returns `d`, where null keys are refused.
    @Test
    @DisplayName("getOrDefault(null, V) throws NullPointerException or returns the default")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void getOrDefaultWithNullKeyThrowsNullPointerExceptionOrReturnsDefault() {
        if (!supportsMethod(MapMethods.GET_OR_DEFAULT) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        V defaultValue = valueProvider().createInstance();
        assertResultOrNullPointer(defaultValue, () -> map.getOrDefault(null, defaultValue), "getOrDefault(null, d)");
    }
}
