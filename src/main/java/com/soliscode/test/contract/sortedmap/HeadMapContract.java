package com.soliscode.test.contract.sortedmap;

import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.OrderedMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [headMap(Object)][SortedMap#headMap(Object)] method of [SortedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface HeadMapContract<M extends SortedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [headMap(Object)][SortedMap#headMap(Object)] returns the entries before the key, as a
    /// [TreeMap][java.util.TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("headMap(K) returns the entries before the key")
    default void headMapWhenCalledReturnsExpectedEntries() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SortedMapMethods.HEAD_MAP)) {
            NavigableMap<K, V> expected = sortedReference(map, entries);
            for (final K key : probeKeys(entries)) {
                assertEquals(expected.headMap(key), map.headMap(key), () -> "headMap(" + key + ")");
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.headMap(key));
        }
    }
}
