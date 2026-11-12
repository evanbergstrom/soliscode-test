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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [equals(Object)][Map.Entry#equals(Object)] method of a [Map.Entry]: an entry is equal to any
/// entry, of whatever class, with an equal key and an equal value, and to nothing else.
///
/// Each test calls the `equals` of the entry under test itself, not `assertEquals`, which calls its expected
/// argument's. Reflexivity is [Object#equals(Object)]'s, which [com.soliscode.test.contract.object.ObjectContract]
/// tests, and is not tested here; nor are null keys and values, until #13.
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
/// @see Map.Entry#equals(Object)
public interface EqualsContract<T extends Map.Entry<K, V>, K, V> extends MapEntryContractSupport<T, K, V> {

    /// Tests that the entry is equal to an entry of another class with the same mapping, and that one to it.
    @Test
    @DisplayName("equals() returns true for an entry of another class with the same key and value")
    default void equalsWhenOtherClassHasSameMappingReturnsTrue() {
        K key = uniqueKeys(1).getFirst();
        V value = uniqueValues(1).getFirst();
        T entry = sourceOf(key, value).entry();
        Map.Entry<K, V> reference = referenceEntry(key, value);
        assertTrue(entry.equals(reference), "the entry equals the reference entry");
        assertTrue(reference.equals(entry), "the reference entry equals the entry");
    }

    /// Tests that the entry is equal to another entry from the same provider with the same mapping.
    @Test
    @DisplayName("equals() returns true for another entry with the same key and value")
    default void equalsWhenSameMappingReturnsTrue() {
        K key = uniqueKeys(1).getFirst();
        V value = uniqueValues(1).getFirst();
        T entry = sourceOf(key, value).entry();
        T other = sourceOf(key, value).entry();
        assertTrue(entry.equals(other));
    }

    /// Tests that the entry is not equal to an entry with another key and the same value.
    @Test
    @DisplayName("equals() returns false for an entry with another key")
    default void equalsWhenKeyDiffersReturnsFalse() {
        List<K> keys = uniqueKeys(2);
        V value = uniqueValues(1).getFirst();
        T entry = sourceOf(keys.get(0), value).entry();
        assertFalse(entry.equals(referenceEntry(keys.get(1), value)));
    }

    /// Tests that the entry is not equal to an entry with the same key and another value.
    @Test
    @DisplayName("equals() returns false for an entry with another value")
    default void equalsWhenValueDiffersReturnsFalse() {
        K key = uniqueKeys(1).getFirst();
        List<V> values = uniqueValues(2);
        T entry = sourceOf(key, values.get(0)).entry();
        assertFalse(entry.equals(referenceEntry(key, values.get(1))));
    }

    /// Tests that `equals(null)` returns `false`, and does not throw.
    @Test
    @DisplayName("equals() returns false for null")
    default void equalsNullReturnsFalse() {
        T entry = sourceOf(uniqueKeys(1).getFirst(), uniqueValues(1).getFirst()).entry();
        assertFalse(entry.equals(null));
    }

    /// Tests that the entry is not equal to its own key, and does not throw: where the key is not itself a
    /// [Map.Entry], which might be one equal to the entry.
    @Test
    @DisplayName("equals() returns false for the entry's key")
    default void equalsItsKeyReturnsFalse() {
        K key = uniqueKeys(1).getFirst();
        if (key instanceof Map.Entry<?, ?>) {
            return;
        }
        T entry = sourceOf(key, uniqueValues(1).getFirst()).entry();
        assertFalse(entry.equals(key));
    }

    /// Tests that the entry is not equal to an object that is not an entry, a list of its key and value, and does not
    /// throw.
    @Test
    @DisplayName("equals() returns false for an object that is not an entry")
    default void equalsWhenNotAnEntryReturnsFalse() {
        K key = uniqueKeys(1).getFirst();
        V value = uniqueValues(1).getFirst();
        T entry = sourceOf(key, value).entry();
        assertFalse(entry.equals(List.of(key, value)));
    }

    /// Tests that after `setValue`, the entry is equal to an entry with the new value, and not to one with the old.
    @Test
    @DisplayName("equals() compares the new value after setValue()")
    default void equalsAfterSetValueComparesNewValue() {
        if (!supportsMethod(MapEntryMethods.MAP_ENTRY_SET_VALUE)) {
            return;
        }
        K key = uniqueKeys(1).getFirst();
        List<V> values = uniqueValues(2);
        T entry = sourceOf(key, values.get(0)).entry();
        entry.setValue(values.get(1));
        assertTrue(entry.equals(referenceEntry(key, values.get(1))), "equal to an entry with the new value");
        assertFalse(entry.equals(referenceEntry(key, values.get(0))), "equal to an entry with the old value");
    }
}
