/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.mapentry;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.MapEntryContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [getKey()][Map.Entry#getKey()] method of a [Map.Entry]: it returns the entry's key.
///
/// It does not call it after the mapping is removed from the backing map, where it "may" throw
/// [IllegalStateException]; nor after `setValue`, which no plausible fault makes change the key.
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
/// @see Map.Entry#getKey()
public interface GetKeyContract<T extends Map.Entry<K, V>, K, V> extends MapEntryContractSupport<T, K, V> {

    /// Tests that `getKey()` returns the key the entry was made with.
    @Test
    @DisplayName("getKey() returns the entry's key")
    default void getKeyReturnsKey() {
        K key = uniqueKeys(1).getFirst();
        T entry = sourceOf(key, uniqueValues(1).getFirst()).entry();
        assertEquals(key, entry.getKey());
    }
}
