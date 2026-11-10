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

/// **Contract for `forEach` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#forEach` adds, beyond `MapContract`'s tests: a null action throws `NullPointerException`.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#forEach(java.util.function.BiConsumer)
/// @since 0.1.2
public interface ForEachContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `forEach(null)` throws `NullPointerException`, on an empty map too, where no action would run.
    @Test
    @DisplayName("forEach(null) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void forEachWithNullActionThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.FOR_EACH)) {
            return;
        }
        M empty = provider().emptyInstance();
        assertNullPointerAndUnchanged(empty, () -> empty.forEach(null), "forEach(null) on an empty map");
        M map = provider().createInstance(defaultSize());
        assertNullPointerAndUnchanged(map, () -> map.forEach(null), "forEach(null)");
    }
}
