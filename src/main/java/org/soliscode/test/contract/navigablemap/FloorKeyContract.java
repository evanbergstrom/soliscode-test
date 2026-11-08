package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [floorKey(Object)][NavigableMap#floorKey(Object)] method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface FloorKeyContract<M extends NavigableMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [floorKey(Object)][NavigableMap#floorKey(Object)] returns the greatest key less than or equal to the given key, or `null`, for keys in the map and
    /// keys not in it, as a [TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("floorKey(K) returns the greatest key less than or equal to the given key")
    default void floorKey_whenCalled_returnsExpectedResult() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.FLOOR_KEY)) {
            TreeMap<K, V> expected = sortedReference(map, entries);
            for (K key : probeKeys(entries)) {
                assertEquals(expected.floorKey(key), map.floorKey(key), () -> "floorKey(" + key + ")");
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.floorKey(key));
        }
    }
}
