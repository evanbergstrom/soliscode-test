/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.concurrentmap;

import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ConcurrentMapContractSupport;
import com.soliscode.test.methods.MapMethods;

/// **Contract for `replace(key, oldValue, newValue)` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#replace(Object, Object, Object)` adds, beyond `MapContract`'s tests: a null key, old value or
/// new value, where the map refuses them, throws `NullPointerException` and leaves the map unchanged.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#replace(Object, Object, Object)
/// @since 0.1.2
public interface ReplaceThreeArgContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `replace(null, old, new)` throws `NullPointerException`, where null keys are refused.
    @Test
    @DisplayName("replace(null, V, V) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void replaceWithNullKeyAndValuesThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.REPLACE_THREE_ARG) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        V old = map.values().iterator().next();
        assertNullPointerAndUnchanged(map, () -> map.replace(null, old, value),
                "replace(null, old, new)");
    }

    /// Tests that `replace(k, null, new)` throws `NullPointerException`, where null values are refused.
    @Test
    @DisplayName("replace(K, null, V) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void replaceWithNullOldValueThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.REPLACE_THREE_ARG) || permitNullValues()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        V value = valueProvider().createInstance(defaultSize() + 1L);
        assertNullPointerAndUnchanged(map, () -> map.replace(key, null, value),
                "replace(k, null, new)");
    }

    /// Tests that `replace(k, old, null)` throws `NullPointerException`, where null values are refused.
    @Test
    @DisplayName("replace(K, V, null) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void replaceWithNullNewValueThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.REPLACE_THREE_ARG) || permitNullValues()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        V old = map.get(key);
        assertNullPointerAndUnchanged(map, () -> map.replace(key, old, null),
                "replace(k, old, null)");
    }
}
