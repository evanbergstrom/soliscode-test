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
package com.soliscode.test.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `MapEntryProviders`.
///
/// @see MapEntryProviders
@DisplayName("Tests for MapEntryProviders")
public class MapEntryProvidersTest {

    /// Default constructor.
    public MapEntryProvidersTest() {
        super();
    }

    @Test
    @DisplayName("The JDK's own entries hold the mapping, and have no map behind them")
    void ownEntriesHoldTheMapping() {
        for (final MapEntryProvider<Map.Entry<Integer, String>, Integer, String> provider : java.util.List.of(
                MapEntryProviders.<Integer, String>simpleEntry(), MapEntryProviders.<Integer, String>mapEntry(),
                MapEntryProviders.<Integer, String>simpleImmutableEntry())) {
            MapEntrySource<Map.Entry<Integer, String>, Integer, String> source = provider.source(1, "one");
            assertEquals(Map.entry(1, "one"), source.entry());
            assertSame(source.entry(), source.entry());
            assertTrue(source.backingValue().isEmpty());
        }
    }

    @Test
    @DisplayName("simpleEntry() supports setValue, and the immutable ones do not")
    void setValueSupport() {
        Map.Entry<Integer, String> entry = MapEntryProviders.<Integer, String>simpleEntry().source(1, "one").entry();
        assertEquals("one", entry.setValue("two"));
        assertEquals("two", entry.getValue());
        assertThrows(UnsupportedOperationException.class,
                () -> MapEntryProviders.<Integer, String>mapEntry().source(1, "one").entry().setValue("two"));
        assertThrows(UnsupportedOperationException.class,
                () -> MapEntryProviders.<Integer, String>simpleImmutableEntry().source(1, "one").entry()
                        .setValue("two"));
    }

    @Test
    @DisplayName("fromMap gives the map's entry, and reads the map for backingValue")
    void fromMapIsBacked() {
        Map<Integer, String> map = new TreeMap<>();
        MapEntrySource<Map.Entry<Integer, String>, Integer, String> source =
                MapEntryProviders.<Integer, String>fromMap(() -> map).source(1, "one");
        assertEquals(Optional.of("one"), source.backingValue());
        source.entry().setValue("two");
        assertEquals("two", map.get(1));
        assertEquals(Optional.of("two"), source.backingValue());
        map.clear();
        assertTrue(source.backingValue().isEmpty());
    }

    @Test
    @DisplayName("fromMap refuses a map that is not empty")
    void fromMapRefusesNonEmptyMap() {
        Map<Integer, String> map = new HashMap<>(Map.of(2, "two"));
        assertThrows(IllegalArgumentException.class,
                () -> MapEntryProviders.<Integer, String>fromMap(() -> map).source(1, "one"));
    }
}
