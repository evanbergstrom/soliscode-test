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
