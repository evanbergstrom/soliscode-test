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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.MapEntryContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [hashCode()][Map.Entry#hashCode()] method of a [Map.Entry]: it is the key's hash code, exclusive
/// or the value's, as its Javadoc's formula says, so that equal entries of any class have equal hash codes.
///
/// The formula's null branches are not tested, until #13; nor is a hash code that is the same when called twice,
/// [Object#hashCode()]'s.
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
/// @see Map.Entry#hashCode()
public interface HashCodeContract<T extends Map.Entry<K, V>, K, V> extends MapEntryContractSupport<T, K, V> {

    /// Tests that `hashCode()` is `key.hashCode() ^ value.hashCode()`.
    @Test
    @DisplayName("hashCode() is the key's hash code exclusive or the value's")
    default void hashCodeFollowsFormula() {
        K key = uniqueKeys(1).getFirst();
        V value = uniqueValues(1).getFirst();
        T entry = sourceOf(key, value).entry();
        assertEquals(key.hashCode() ^ value.hashCode(), entry.hashCode());
    }

    /// Tests that after `setValue`, `hashCode()` follows the formula with the new value.
    @Test
    @DisplayName("hashCode() follows the formula with the new value after setValue()")
    default void hashCodeAfterSetValueFollowsFormula() {
        if (!supportsMethod(MapEntryMethods.MAP_ENTRY_SET_VALUE)) {
            return;
        }
        K key = uniqueKeys(1).getFirst();
        List<V> values = uniqueValues(2);
        T entry = sourceOf(key, values.get(0)).entry();
        entry.setValue(values.get(1));
        assertEquals(key.hashCode() ^ values.get(1).hashCode(), entry.hashCode());
    }
}
