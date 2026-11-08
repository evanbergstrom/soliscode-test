package com.soliscode.test.contract.navigablemap;

import java.util.Map;
import java.util.NavigableMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.OrderedMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [lowerEntry(Object)][NavigableMap#lowerEntry(Object)] method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface LowerEntryContract<M extends NavigableMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [lowerEntry(Object)][NavigableMap#lowerEntry(Object)] returns the entry with the greatest key
    /// strictly less than the given key, or `null`, for keys in the map and keys not in it, as a
    /// [TreeMap][java.util.TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("lowerEntry(K) returns the entry with the greatest key strictly less than the given key")
    default void lowerEntryWhenCalledReturnsExpectedResult() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.LOWER_ENTRY)) {
            NavigableMap<K, V> expected = sortedReference(map, entries);
            for (final K key : probeKeys(entries)) {
                assertEquals(expected.lowerEntry(key), map.lowerEntry(key), () -> "lowerEntry(" + key + ")");
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.lowerEntry(key));
        }
    }
}
