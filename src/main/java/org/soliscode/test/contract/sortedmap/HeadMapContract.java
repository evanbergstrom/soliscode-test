package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [headMap(Object)][SortedMap#headMap(Object)] method of [SortedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface HeadMapContract<M extends SortedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [headMap(Object)][SortedMap#headMap(Object)] returns the entries before the key, as a [TreeMap] with the same
    /// entries and comparator does.
    @Test
    @DisplayName("headMap(K) returns the entries before the key")
    default void headMap_whenCalled_returnsExpectedEntries() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SortedMapMethods.HEAD_MAP)) {
            TreeMap<K, V> expected = sortedReference(map, entries);
            for (K key : probeKeys(entries)) {
                assertEquals(expected.headMap(key), map.headMap(key), () -> "headMap(" + key + ")");
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.headMap(key));
        }
    }
}
