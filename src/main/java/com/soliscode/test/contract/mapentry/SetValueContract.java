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
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.MapEntryContractSupport;
import com.soliscode.test.provider.MapEntrySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the optional [setValue(V)][Map.Entry#setValue(Object)] method of a [Map.Entry]: it replaces the
/// entry's value, returns the old one, and writes through to the map behind the entry; or throws
/// [UnsupportedOperationException] where it is not supported ([MapEntryMethods#MAP_ENTRY_SET_VALUE]).
///
/// The new values come from `uniqueValues`, so each differs from the value it replaces.
///
/// It reads write-through strictly: `setValue`'s own Javadoc says "(Writes through to the map.)", where the class
/// Javadoc of [Map.Entry] adds "if supported by the backing map"; so where a source has a backing map
/// ([MapEntrySource#backingValue()] present), a supported `setValue` must write through to it. A test class whose
/// entries need not supplies a source without one.
///
/// It reads the Javadoc more loosely in three places. Its [ClassCastException], [NullPointerException] and
/// [IllegalArgumentException] come from the backing map's refusal of the value, which a contract that knows only the
/// entry cannot provoke, and no contract tests them yet; values are not null until #13. An unsupported `setValue` is
/// checked only for the exception, as [com.soliscode.test.contract.iterator.RemoveContract] does for `Iterator`. And
/// it does not call `setValue` after the mapping is removed from the backing map, where it "may" throw
/// [IllegalStateException].
///
/// @param <T> the type of the entry under test
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
/// @see Map.Entry#setValue(Object)
public interface SetValueContract<T extends Map.Entry<K, V>, K, V> extends MapEntryContractSupport<T, K, V> {

    /// Tests that `setValue` returns the value the entry held before.
    @Test
    @DisplayName("setValue() returns the old value")
    default void setValueReturnsOldValue() {
        if (!supportsMethod(MapEntryMethods.MAP_ENTRY_SET_VALUE)) {
            return;
        }
        List<V> values = uniqueValues(2);
        T entry = sourceOf(uniqueKeys(1).getFirst(), values.get(0)).entry();
        assertEquals(values.get(0), entry.setValue(values.get(1)));
    }

    /// Tests that `getValue()` returns the new value after `setValue`.
    @Test
    @DisplayName("setValue() replaces the value getValue() returns")
    default void setValueReplacesValue() {
        if (!supportsMethod(MapEntryMethods.MAP_ENTRY_SET_VALUE)) {
            return;
        }
        List<V> values = uniqueValues(2);
        T entry = sourceOf(uniqueKeys(1).getFirst(), values.get(0)).entry();
        entry.setValue(values.get(1));
        assertEquals(values.get(1), entry.getValue());
    }

    /// Tests that a second `setValue` returns the value the first one set.
    @Test
    @DisplayName("setValue() called twice returns the value of the first call")
    default void setValueTwiceReturnsValueOfFirstCall() {
        if (!supportsMethod(MapEntryMethods.MAP_ENTRY_SET_VALUE)) {
            return;
        }
        List<V> values = uniqueValues(3);
        T entry = sourceOf(uniqueKeys(1).getFirst(), values.get(0)).entry();
        entry.setValue(values.get(1));
        assertEquals(values.get(1), entry.setValue(values.get(2)));
        assertEquals(values.get(2), entry.getValue());
    }

    /// Tests that `setValue` writes through: where a map is behind the entry, it holds the new value for the key
    /// afterwards. The map is read before and after, so that a `setValue` that drops the mapping fails too.
    @Test
    @DisplayName("setValue() writes the new value through to the map behind the entry")
    default void setValueWritesThroughToMap() {
        if (!supportsMethod(MapEntryMethods.MAP_ENTRY_SET_VALUE)) {
            return;
        }
        List<V> values = uniqueValues(2);
        MapEntrySource<T, K, V> source = sourceOf(uniqueKeys(1).getFirst(), values.get(0));
        Optional<V> before = source.backingValue();
        if (before.isEmpty()) {
            return;
        }
        assertEquals(values.get(0), before.get(), "the map's value before setValue");
        source.entry().setValue(values.get(1));
        Optional<V> after = source.backingValue();
        assertTrue(after.isPresent(), "the map no longer holds the key after setValue");
        assertEquals(values.get(1), after.get(), "the map's value after setValue");
    }

    /// Tests that an unsupported `setValue` throws [UnsupportedOperationException].
    @Test
    @DisplayName("setValue() throws UnsupportedOperationException where it is not supported")
    default void setValueWhenNotSupportedThrowsUnsupportedOperationException() {
        if (supportsMethod(MapEntryMethods.MAP_ENTRY_SET_VALUE)) {
            return;
        }
        List<V> values = uniqueValues(2);
        T entry = sourceOf(uniqueKeys(1).getFirst(), values.get(0)).entry();
        assertThrows(UnsupportedOperationException.class, () -> entry.setValue(values.get(1)));
    }
}
