package com.soliscode.test.contract.sortedmap;

import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.OrderedMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [tailMap(Object)][SortedMap#tailMap(Object)] method of [SortedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface TailMapContract<M extends SortedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [tailMap(Object)][SortedMap#tailMap(Object)] returns the entries from the key on, as a
    /// [TreeMap][java.util.TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("tailMap(K) returns the entries from the key on")
    default void tailMapWhenCalledReturnsExpectedEntries() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SortedMapMethods.TAIL_MAP)) {
            NavigableMap<K, V> expected = sortedReference(map, entries);
            for (final K key : probeKeys(entries)) {
                assertEquals(expected.tailMap(key), map.tailMap(key), () -> "tailMap(" + key + ")");
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.tailMap(key));
        }
    }
}
