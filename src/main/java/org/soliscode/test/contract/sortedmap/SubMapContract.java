package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [subMap(Object, Object)][SortedMap#subMap(Object, Object)] method of [SortedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface SubMapContract<M extends SortedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [subMap(Object, Object)][SortedMap#subMap(Object, Object)] returns the entries from the first key,
    /// included, to the second, excluded, as a [TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("subMap(K, K) returns the entries between the keys")
    default void subMap_whenCalled_returnsEntriesBetweenKeys() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SortedMapMethods.SUB_MAP)) {
            TreeMap<K, V> expected = sortedReference(map, entries);
            List<K> keys = new ArrayList<>(expected.keySet());
            for (int i = 0; i < keys.size(); i++) {
                for (int j = i; j < keys.size(); j++) {
                    K from = keys.get(i);
                    K to = keys.get(j);
                    assertEquals(expected.subMap(from, to), map.subMap(from, to),
                            () -> "subMap(" + from + ", " + to + ")");
                }
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.subMap(key, key));
        }
    }
}
