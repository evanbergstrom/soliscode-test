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

/// Contract for the [getValue()][Map.Entry#getValue()] method of a [Map.Entry]: it returns the entry's value.
/// [SetValueContract] tests what it returns after `setValue`.
///
/// It does not call it after the mapping is removed from the backing map, where its result is undefined.
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
/// @see Map.Entry#getValue()
public interface GetValueContract<T extends Map.Entry<K, V>, K, V> extends MapEntryContractSupport<T, K, V> {

    /// Tests that `getValue()` returns the value the entry was made with.
    @Test
    @DisplayName("getValue() returns the entry's value")
    default void getValueReturnsValue() {
        V value = uniqueValues(1).getFirst();
        T entry = sourceOf(uniqueKeys(1).getFirst(), value).entry();
        assertEquals(value, entry.getValue());
    }
}
