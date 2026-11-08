package com.soliscode.test.contract.navigablemap;

import java.util.Map;
import java.util.NavigableMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.OrderedMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [higherEntry(Object)][NavigableMap#higherEntry(Object)] method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface HigherEntryContract<M extends NavigableMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [higherEntry(Object)][NavigableMap#higherEntry(Object)] returns the entry with the least key strictly
    /// greater than the given key, or `null`, for keys in the map and keys not in it, as a [TreeMap][java.util.TreeMap]
    /// with the same entries and comparator does.
    @Test
    @DisplayName("higherEntry(K) returns the entry with the least key strictly greater than the given key")
    default void higherEntryWhenCalledReturnsExpectedResult() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.HIGHER_ENTRY)) {
            NavigableMap<K, V> expected = sortedReference(map, entries);
            for (final K key : probeKeys(entries)) {
                assertEquals(expected.higherEntry(key), map.higherEntry(key), () -> "higherEntry(" + key + ")");
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.higherEntry(key));
        }
    }
}
