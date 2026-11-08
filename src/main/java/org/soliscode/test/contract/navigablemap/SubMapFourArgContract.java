package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [subMap(Object, boolean, Object, boolean)][NavigableMap#subMap(Object, boolean, Object, boolean)]
/// method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface SubMapFourArgContract<M extends NavigableMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [subMap(Object, boolean, Object, boolean)][NavigableMap#subMap(Object, boolean, Object, boolean)]
    /// returns the entries between two keys, with or without each, as a [TreeMap] with the same entries and
    /// comparator does.
    @Test
    @DisplayName("subMap(K, boolean, K, boolean) returns the entries between the keys")
    default void subMapFourArg_whenCalled_returnsEntriesBetweenKeys() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.SUB_MAP_FOUR_ARG)) {
            TreeMap<K, V> expected = sortedReference(map, entries);
            List<K> keys = new ArrayList<>(expected.keySet());
            for (int i = 0; i < keys.size(); i++) {
                for (int j = i; j < keys.size(); j++) {
                    K from = keys.get(i);
                    K to = keys.get(j);
                    for (boolean fromInclusive : new boolean[] {false, true}) {
                        for (boolean toInclusive : new boolean[] {false, true}) {
                            assertEquals(expected.subMap(from, fromInclusive, to, toInclusive),
                                    map.subMap(from, fromInclusive, to, toInclusive),
                                    () -> "subMap(" + from + ", " + fromInclusive + ", " + to + ", " + toInclusive + ")");
                        }
                    }
                }
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.subMap(key, true, key, true));
        }
    }
}
