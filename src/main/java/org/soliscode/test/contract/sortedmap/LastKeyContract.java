package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [lastKey()][SortedMap#lastKey()] method of [SortedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface LastKeyContract<M extends SortedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [lastKey()][SortedMap#lastKey()] throws [NoSuchElementException] for an empty map.
    @Test
    @DisplayName("lastKey() throws NoSuchElementException for an empty map")
    default void lastKey_whenEmpty_throwsException() {
        SortedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SortedMapMethods.LAST_KEY)) {
            assertThrows(NoSuchElementException.class, map::lastKey);
        } else {
            assertThrows(UnsupportedOperationException.class, map::lastKey);
        }
    }

    /// Tests that [lastKey()][SortedMap#lastKey()] returns the key a [TreeMap] with the same entries and comparator
    /// returns.
    @Test
    @DisplayName("lastKey() returns the last key in the map's order")
    default void lastKey_whenNotEmpty_returnsKey() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SortedMapMethods.LAST_KEY)) {
            TreeMap<K, V> expected = sortedReference(map, entries);
            assertEquals(expected.lastKey(), map.lastKey());
        } else {
            assertThrows(UnsupportedOperationException.class, map::lastKey);
        }
    }
}
