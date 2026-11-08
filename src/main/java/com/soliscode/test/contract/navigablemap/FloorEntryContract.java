package com.soliscode.test.contract.navigablemap;

import java.util.Map;
import java.util.NavigableMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.OrderedMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [floorEntry(Object)][NavigableMap#floorEntry(Object)] method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface FloorEntryContract<M extends NavigableMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [floorEntry(Object)][NavigableMap#floorEntry(Object)] returns the entry with the greatest key less
    /// than or equal to the given key, or `null`, for keys in the map and keys not in it, as a
    /// [TreeMap][java.util.TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("floorEntry(K) returns the entry with the greatest key less than or equal to the given key")
    default void floorEntryWhenCalledReturnsExpectedResult() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.FLOOR_ENTRY)) {
            NavigableMap<K, V> expected = sortedReference(map, entries);
            for (final K key : probeKeys(entries)) {
                assertEquals(expected.floorEntry(key), map.floorEntry(key), () -> "floorEntry(" + key + ")");
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.floorEntry(key));
        }
    }
}
