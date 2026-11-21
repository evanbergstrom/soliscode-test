/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.concurrentmap;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ConcurrentMapContractSupport;
import com.soliscode.test.methods.MapMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for `computeIfPresent` of a `ConcurrentMap`**
///
/// What `ConcurrentMap#computeIfPresent` adds, beyond `MapContract`'s tests: a null key, where the map refuses null
/// keys, and a null function throw `NullPointerException`, the function even on an absent key, where it would not be
/// called (`Map` requires it); a `null` result removes the mapping; and the function is not called for an absent key
/// (`MapContract` checks only the result).
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#computeIfPresent(Object, BiFunction)
/// @since 0.1.2
public interface ComputeIfPresentContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// Tests that `computeIfPresent(null, f)` throws `NullPointerException`, where null keys are refused.
    @Test
    @DisplayName("computeIfPresent(null, BiFunction) throws NullPointerException")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void computeIfPresentWithNullKeyThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.COMPUTE_IF_PRESENT) || permitNullKeys()) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        assertNullPointerAndUnchanged(map, () -> map.computeIfPresent(null, (k, v) -> value),
                "computeIfPresent(null, f)");
    }

    /// Tests that `computeIfPresent(k, null)` throws `NullPointerException` for an absent key, where the function
    /// would not be called.
    @Test
    @DisplayName("computeIfPresent(K, null) throws NullPointerException for an absent key")
    // NullAway: the test passes null on purpose, to check that the map refuses it.
    @SuppressWarnings("NullAway")
    default void computeIfPresentWithNullFunctionThrowsNullPointerException() {
        if (!supportsMethod(MapMethods.COMPUTE_IF_PRESENT)) {
            return;
        }
        M map = provider().createInstance(defaultSize());
        K key = keyProvider().createInstanceNotIn(map.keySet());
        assertNullPointerAndUnchanged(map, () -> map.computeIfPresent(key, null),
                "computeIfPresent(absent, null)");
    }

    /// Tests that `computeIfPresent` with a function that returns `null` removes the mapping and returns `null`.
    @Test
    @DisplayName("computeIfPresent(K, BiFunction) removes the mapping when the function returns null")
    default void computeIfPresentWhenFunctionReturnsNullRemovesEntry() {
        M map = provider().createInstance(defaultSize());
        K key = map.keySet().iterator().next();
        BiFunction<K, V, @Nullable V> toNull = (k, v) -> null;
        if (supportsMethod(MapMethods.COMPUTE_IF_PRESENT)) {
            int size = map.size();
            assertNull(map.computeIfPresent(key, toNull));
            assertFalse(map.containsKey(key), "computeIfPresent kept the mapping for a null result");
            assertEquals(size - 1, map.size());
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfPresent(key, toNull));
        }
    }

    /// Tests that `computeIfPresent` does not call the function for an absent key.
    @Test
    @DisplayName("computeIfPresent(K, BiFunction) does not call the function for an absent key")
    default void computeIfPresentWhenKeyNotPresentDoesNotCallFunction() {
        M map = provider().createInstance(defaultSize());
        K key = keyProvider().createInstanceNotIn(map.keySet());
        V value = valueProvider().createInstance(defaultSize() + 1L);
        AtomicInteger calls = new AtomicInteger();
        if (supportsMethod(MapMethods.COMPUTE_IF_PRESENT)) {
            assertNull(map.computeIfPresent(key, (k, v) -> {
                calls.incrementAndGet();
                return value;
            }));
            assertEquals(0, calls.get(), "computeIfPresent called the function for an absent key");
            assertFalse(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfPresent(key, (k, v) -> value));
        }
    }
}
