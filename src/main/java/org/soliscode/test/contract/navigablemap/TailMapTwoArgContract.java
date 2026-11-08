package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [tailMap(Object, boolean)][NavigableMap#tailMap(Object, boolean)] method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface TailMapTwoArgContract<M extends NavigableMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [tailMap(Object, boolean)][NavigableMap#tailMap(Object, boolean)] returns the entries from the
    /// key on, with or without it, as a [TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("tailMap(K, boolean) returns the entries from the key on")
    default void tailMapTwoArg_whenCalled_returnsEntriesFromKey() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.TAIL_MAP_TWO_ARG)) {
            TreeMap<K, V> expected = sortedReference(map, entries);
            for (K key : probeKeys(entries)) {
                for (boolean inclusive : new boolean[] {false, true}) {
                    assertEquals(expected.tailMap(key, inclusive), map.tailMap(key, inclusive),
                            () -> "tailMap(" + key + ", " + inclusive + ")");
                }
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.tailMap(key, true));
        }
    }
}
