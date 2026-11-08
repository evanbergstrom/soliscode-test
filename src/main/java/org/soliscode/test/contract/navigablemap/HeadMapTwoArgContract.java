package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [headMap(Object, boolean)][NavigableMap#headMap(Object, boolean)] method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface HeadMapTwoArgContract<M extends NavigableMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [headMap(Object, boolean)][NavigableMap#headMap(Object, boolean)] returns the entries before the
    /// key, with or without it, as a [TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("headMap(K, boolean) returns the entries before the key")
    default void headMapTwoArg_whenCalled_returnsEntriesBeforeKey() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.HEAD_MAP_TWO_ARG)) {
            TreeMap<K, V> expected = sortedReference(map, entries);
            for (K key : probeKeys(entries)) {
                for (boolean inclusive : new boolean[] {false, true}) {
                    assertEquals(expected.headMap(key, inclusive), map.headMap(key, inclusive),
                            () -> "headMap(" + key + ", " + inclusive + ")");
                }
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.headMap(key, true));
        }
    }
}
