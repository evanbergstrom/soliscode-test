package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [lowerKey(Object)][NavigableMap#lowerKey(Object)] method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface LowerKeyContract<M extends NavigableMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [lowerKey(Object)][NavigableMap#lowerKey(Object)] returns the greatest key strictly less than the given key, or `null`, for keys in the map and
    /// keys not in it, as a [TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("lowerKey(K) returns the greatest key strictly less than the given key")
    default void lowerKey_whenCalled_returnsExpectedResult() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.LOWER_KEY)) {
            TreeMap<K, V> expected = sortedReference(map, entries);
            for (K key : probeKeys(entries)) {
                assertEquals(expected.lowerKey(key), map.lowerKey(key), () -> "lowerKey(" + key + ")");
            }
        } else {
            K key = entries.keySet().iterator().next();
            assertThrows(UnsupportedOperationException.class, () -> map.lowerKey(key));
        }
    }
}
