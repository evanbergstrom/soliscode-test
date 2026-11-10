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
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.contract.support.ConcurrentMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `replaceAll` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#replaceAll` adds, beyond `MapContract`'s tests: a null function throws `NullPointerException`,
/// and so does a function that returns `null`, where the map refuses null values. `Map#replaceAll` makes that second
/// exception optional; the contract reads it strictly, since the only other outcome is to store a `null` the map
/// cannot hold.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#replaceAll(BiFunction)
/// @since 0.1.2
public interface ReplaceAllContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `replaceAll(null)` throws `NullPointerException`, on an empty map too.
    @Test
    @DisplayName("replaceAll(null) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void replaceAllWithNullFunctionThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.REPLACE_ALL)) {
            return;
        }
        M empty = provider().emptyInstance();
        assertNullPointerAndUnchanged(empty, () -> empty.replaceAll(null), "replaceAll(null) on an empty map");
        M map = provider().createInstance(defaultSize());
        assertNullPointerAndUnchanged(map, () -> map.replaceAll(null), "replaceAll(null)");
    }

    /// Tests that `replaceAll` with a function that returns `null` throws `NullPointerException`, where null values
    /// are refused.
    @Test
    @DisplayName("replaceAll(BiFunction) throws NullPointerException when the function returns null")
    default void replaceAllWhenFunctionReturnsNullThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.REPLACE_ALL) || permitNullValues()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        BiFunction<K, V, @Nullable V> toNull = (k, v) -> null;
        assertThrows(NullPointerException.class, () -> map.replaceAll(toNull),
                "replaceAll with a function that returns null");
    }
}
