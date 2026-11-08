package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [ceilingEntry(Object)][NavigableMap#ceilingEntry(Object)] method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface CeilingEntryContract<M extends NavigableMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [ceilingEntry(Object)][NavigableMap#ceilingEntry(Object)] returns the entry with the least key greater than or equal to the given key, or `null`, for keys in the map and
    /// keys not in it, as a [TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("ceilingEntry(K) returns the entry with the least key greater than or equal to the given key")
    default void ceilingEntry_whenCalled_returnsExpectedResult() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.CEILING_ENTRY)) {
            TreeMap<K, V> expected = sortedReference(map, entries);
            for (K key : probeKeys(entries)) {
                assertEquals(expected.ceilingEntry(key), map.ceilingEntry(key), () -> "ceilingEntry(" + key + ")");
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.ceilingEntry(key));
        }
    }
}
