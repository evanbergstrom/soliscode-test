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
package com.soliscode.test.breakable;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.mapentry.MapEntryMethods;

import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_EQUALS_IGNORES_KEY;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_EQUALS_IGNORES_VALUE;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_EQUALS_IS_IDENTITY;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_EQUALS_IS_TRUE_FOR_ITS_KEY;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_EQUALS_REQUIRES_SAME_CLASS;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_EQUALS_THROWS_FOR_NON_ENTRY;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_GET_KEY_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_GET_VALUE_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_HASH_CODE_IGNORES_VALUE;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_HASH_CODE_IS_IDENTITY;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_HASH_CODE_IS_OBJECTS_HASH;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_HASH_CODE_IS_STALE_AFTER_SET_VALUE;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_SET_VALUE_DOES_NOTHING;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_SET_VALUE_RETURNS_NEW_VALUE;
import static com.soliscode.test.breakable.BreakableMapEntry.MAP_ENTRY_SET_VALUE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `BreakableMapEntry`: that it delegates unbroken, and that each break has the effect it describes.
///
/// @see BreakableMapEntry
@DisplayName("Tests for BreakableMapEntry")
public class BreakableMapEntryTest {

    private static final Map.Entry<Integer, String> REFERENCE = new AbstractMap.SimpleImmutableEntry<>(1, "one");

    /// Default constructor.
    public BreakableMapEntryTest() {
        super();
    }

    private static BreakableMapEntry<Integer, String> entry(final Break... breaks) {
        return new BreakableMapEntry<>(new AbstractMap.SimpleEntry<>(1, "one"), Set.of(breaks), Map.of());
    }

    private static Map.Entry<Integer, String> mapEntry(final Map<Integer, String> map) {
        map.put(1, "one");
        return map.entrySet().iterator().next();
    }

    @Test
    @DisplayName("Unbroken, it delegates and follows Map.Entry's formulas")
    void unbrokenDelegates() {
        Map<Integer, String> map = new HashMap<>();
        BreakableMapEntry<Integer, String> entry = new BreakableMapEntry<>(mapEntry(map), Set.of(), Map.of());
        assertEquals(1, entry.getKey());
        assertEquals("one", entry.getValue());
        assertTrue(entry.equals(REFERENCE));
        assertFalse(entry.equals(null));
        assertFalse(entry.equals(1));
        assertEquals(REFERENCE.hashCode(), entry.hashCode());
        assertEquals("1=one", entry.toString());
        assertEquals("one", entry.setValue("two"));
        assertEquals("two", entry.getValue());
        assertEquals("two", map.get(1));
        assertEquals(Objects.hashCode(1) ^ "two".hashCode(), entry.hashCode());
    }

    @Test
    @DisplayName("An unsupported setValue throws UnsupportedOperationException, or RuntimeException if broken")
    void setValueWhenNotSupported() {
        Map<InterfaceMethod, MethodStatus> unsupported =
                Map.of(MapEntryMethods.MAP_ENTRY_SET_VALUE, new MethodStatus(false));
        BreakableMapEntry<Integer, String> entry =
                new BreakableMapEntry<>(new AbstractMap.SimpleEntry<>(1, "one"), Set.of(), unsupported);
        assertThrows(UnsupportedOperationException.class, () -> entry.setValue("two"));
        BreakableMapEntry<Integer, String> broken = new BreakableMapEntry<>(new AbstractMap.SimpleEntry<>(1, "one"),
                Set.of(MAP_ENTRY_SET_VALUE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED), unsupported);
        RuntimeException e = assertThrows(RuntimeException.class, () -> broken.setValue("two"));
        assertFalse(e instanceof UnsupportedOperationException);
        assertEquals("one", broken.getValue());
    }

    @Test
    @DisplayName("MAP_ENTRY_GET_KEY_RETURNS_NULL and MAP_ENTRY_GET_VALUE_RETURNS_NULL return null")
    void gettersReturnNull() {
        BreakableMapEntry<Integer, String> noKey = entry(MAP_ENTRY_GET_KEY_RETURNS_NULL);
        assertNull(noKey.getKey());
        assertEquals("null=one", noKey.toString());
        assertEquals("one".hashCode(), noKey.hashCode());
        BreakableMapEntry<Integer, String> noValue = entry(MAP_ENTRY_GET_VALUE_RETURNS_NULL);
        assertNull(noValue.getValue());
        assertFalse(noValue.equals(REFERENCE));
    }

    @Test
    @DisplayName("MAP_ENTRY_SET_VALUE_RETURNS_NEW_VALUE sets the value and returns it")
    void setValueReturnsNewValue() {
        BreakableMapEntry<Integer, String> entry = entry(MAP_ENTRY_SET_VALUE_RETURNS_NEW_VALUE);
        assertEquals("two", entry.setValue("two"));
        assertEquals("two", entry.getValue());
    }

    @Test
    @DisplayName("MAP_ENTRY_SET_VALUE_DOES_NOTHING returns the old value and keeps it")
    void setValueDoesNothing() {
        BreakableMapEntry<Integer, String> entry = entry(MAP_ENTRY_SET_VALUE_DOES_NOTHING);
        assertEquals("one", entry.setValue("two"));
        assertEquals("one", entry.getValue());
    }

    @Test
    @DisplayName("MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH changes the entry but not the map")
    void setValueDoesNotWriteThrough() {
        Map<Integer, String> map = new HashMap<>();
        BreakableMapEntry<Integer, String> entry = new BreakableMapEntry<>(mapEntry(map),
                Set.of(MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH), Map.of());
        assertEquals("one", entry.setValue("two"));
        assertEquals("two", entry.getValue());
        assertEquals("one", map.get(1));
        assertEquals("two", entry.setValue("three"));
        assertTrue(entry.equals(new AbstractMap.SimpleImmutableEntry<>(1, "three")));
        assertEquals(Objects.hashCode(1) ^ "three".hashCode(), entry.hashCode());
    }

    @Test
    @DisplayName("MAP_ENTRY_EQUALS_IS_IDENTITY equals only itself")
    void equalsIsIdentity() {
        BreakableMapEntry<Integer, String> entry = entry(MAP_ENTRY_EQUALS_IS_IDENTITY);
        assertTrue(entry.equals(entry));
        assertFalse(entry.equals(REFERENCE));
        assertFalse(entry.equals(entry(MAP_ENTRY_EQUALS_IS_IDENTITY)));
    }

    @Test
    @DisplayName("MAP_ENTRY_EQUALS_IGNORES_KEY and MAP_ENTRY_EQUALS_IGNORES_VALUE compare one part only")
    void equalsIgnoresAPart() {
        assertTrue(entry(MAP_ENTRY_EQUALS_IGNORES_KEY).equals(new AbstractMap.SimpleImmutableEntry<>(2, "one")));
        assertFalse(entry(MAP_ENTRY_EQUALS_IGNORES_KEY).equals(new AbstractMap.SimpleImmutableEntry<>(1, "two")));
        assertTrue(entry(MAP_ENTRY_EQUALS_IGNORES_VALUE).equals(new AbstractMap.SimpleImmutableEntry<>(1, "two")));
        assertFalse(entry(MAP_ENTRY_EQUALS_IGNORES_VALUE).equals(new AbstractMap.SimpleImmutableEntry<>(2, "one")));
    }

    @Test
    @DisplayName("MAP_ENTRY_EQUALS_REQUIRES_SAME_CLASS equals only BreakableMapEntry instances")
    void equalsRequiresSameClass() {
        BreakableMapEntry<Integer, String> entry = entry(MAP_ENTRY_EQUALS_REQUIRES_SAME_CLASS);
        assertFalse(entry.equals(REFERENCE));
        assertTrue(entry.equals(entry()));
    }

    @Test
    @DisplayName("MAP_ENTRY_EQUALS_IS_TRUE_FOR_ITS_KEY equals its key, and no other non-entry")
    void equalsIsTrueForItsKey() {
        BreakableMapEntry<Integer, String> entry = entry(MAP_ENTRY_EQUALS_IS_TRUE_FOR_ITS_KEY);
        assertTrue(entry.equals(1));
        assertFalse(entry.equals(null));
        assertFalse(entry.equals(List.of(1, "one")));
        assertTrue(entry.equals(REFERENCE));
    }

    @Test
    @DisplayName("MAP_ENTRY_EQUALS_THROWS_FOR_NON_ENTRY throws for null and for a non-entry")
    void equalsThrowsForNonEntry() {
        BreakableMapEntry<Integer, String> entry = entry(MAP_ENTRY_EQUALS_THROWS_FOR_NON_ENTRY);
        assertThrows(NullPointerException.class, () -> entry.equals(null));
        assertThrows(ClassCastException.class, () -> entry.equals(List.of(1, "one")));
        assertTrue(entry.equals(REFERENCE));
    }

    @Test
    @DisplayName("The hashCode breaks give the hash codes they describe")
    void hashCodeBreaks() {
        BreakableMapEntry<Integer, String> identity = entry(MAP_ENTRY_HASH_CODE_IS_IDENTITY);
        assertEquals(System.identityHashCode(identity), identity.hashCode());
        assertEquals(Objects.hashCode(1), entry(MAP_ENTRY_HASH_CODE_IGNORES_VALUE).hashCode());
        assertEquals(Objects.hash(1, "one"), entry(MAP_ENTRY_HASH_CODE_IS_OBJECTS_HASH).hashCode());
        BreakableMapEntry<Integer, String> stale = entry(MAP_ENTRY_HASH_CODE_IS_STALE_AFTER_SET_VALUE);
        stale.setValue("two");
        assertEquals(REFERENCE.hashCode(), stale.hashCode());
    }
}
