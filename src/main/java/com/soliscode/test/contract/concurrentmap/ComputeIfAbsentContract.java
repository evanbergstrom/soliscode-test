/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.concurrentmap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ConcurrentMapContractSupport;
import com.soliscode.test.methods.MapMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `computeIfAbsent` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#computeIfAbsent` adds, beyond `MapContract`'s tests: a null key, where the map refuses null
/// keys, and a null function throw `NullPointerException`, the function even on a present key, where it would not be
/// called (`Map` requires it); a `null` result records no mapping; and the function is not called for a present key.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#computeIfAbsent(Object, Function)
/// @since 0.1.2
public interface ComputeIfAbsentContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `computeIfAbsent(null, f)` throws `NullPointerException`, where null keys are refused.
    @Test
    @DisplayName("computeIfAbsent(null, Function) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void computeIfAbsentWithNullKeyThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.COMPUTE_IF_ABSENT) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        assertNullPointerAndUnchanged(map, () -> map.computeIfAbsent(null, k -> value),
                "computeIfAbsent(null, f)");
    }

    /// Tests that `computeIfAbsent(k, null)` throws `NullPointerException` for a present key, where the function
    /// would not be called.
    @Test
    @DisplayName("computeIfAbsent(K, null) throws NullPointerException for a present key")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void computeIfAbsentWithNullFunctionThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.COMPUTE_IF_ABSENT)) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        assertNullPointerAndUnchanged(map, () -> map.computeIfAbsent(key, null),
                "computeIfAbsent(present, null)");
    }

    /// Tests that `computeIfAbsent` with a function that returns `null` returns `null` and records no mapping.
    @Test
    @DisplayName("computeIfAbsent(K, Function) records no mapping when the function returns null")
    default void computeIfAbsentWhenFunctionReturnsNullRecordsNoMapping() {
        M map = provider().createInstance(defaultSize());
        K key = keyProvider().createInstanceNotIn(map.keySet());
        Function<K, @Nullable V> toNull = k -> null;
        if (supportsMethod(MapMethods.COMPUTE_IF_ABSENT)) {
            Map<K, V> before = new HashMap<>(map);
            assertNull(map.computeIfAbsent(key, toNull));
            assertFalse(map.containsKey(key), "computeIfAbsent recorded a mapping for a null result");
            assertEquals(before, new HashMap<>(map));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(key, toNull));
        }
    }

    /// Tests that `computeIfAbsent` does not call the function for a present key.
    @Test
    @DisplayName("computeIfAbsent(K, Function) does not call the function for a present key")
    default void computeIfAbsentWhenKeyPresentDoesNotCallFunction() {
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        V value = valueProvider().createInstance(defaultSize() + 1L);
        V old = map.get(key);
        AtomicInteger calls = new AtomicInteger();
        if (supportsMethod(MapMethods.COMPUTE_IF_ABSENT)) {
            assertEquals(old, map.computeIfAbsent(key, k -> {
                calls.incrementAndGet();
                return value;
            }));
            assertEquals(0, calls.get(), "computeIfAbsent called the function for a present key");
            assertEquals(old, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(key, k -> value));
        }
    }
}
