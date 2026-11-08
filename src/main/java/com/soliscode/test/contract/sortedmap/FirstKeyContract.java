package com.soliscode.test.contract.sortedmap;

import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.OrderedMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [firstKey()][SortedMap#firstKey()] method of [SortedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface FirstKeyContract<M extends SortedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [firstKey()][SortedMap#firstKey()] throws [NoSuchElementException] for an empty map.
    @Test
    @DisplayName("firstKey() throws NoSuchElementException for an empty map")
    default void firstKeyWhenEmptyThrowsException() {
        M map = provider().emptyInstance();
        if (supportsMethod(SortedMapMethods.FIRST_KEY)) {
            assertThrows(NoSuchElementException.class, map::firstKey);
        } else {
            assertThrows(UnsupportedOperationException.class, map::firstKey);
        }
    }

    /// Tests that [firstKey()][SortedMap#firstKey()] returns the key a [TreeMap][java.util.TreeMap] with the same
    /// entries and comparator returns.
    @Test
    @DisplayName("firstKey() returns the first key in the map's order")
    default void firstKeyWhenNotEmptyReturnsKey() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SortedMapMethods.FIRST_KEY)) {
            NavigableMap<K, V> expected = sortedReference(map, entries);
            assertEquals(expected.firstKey(), map.firstKey());
        } else {
            assertThrows(UnsupportedOperationException.class, map::firstKey);
        }
    }
}
