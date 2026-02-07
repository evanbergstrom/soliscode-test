package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [NavigableMap#tailMap(Object, boolean)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface TailMapTwoArgContract<K, V, M extends NavigableMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `tailMap()` returns a map.
    @Test
    @DisplayName("Test tailMap with two arguments returns a map")
    default void testTailMapTwoArg() {
        NavigableMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K key = map.firstKey();
        if (supportsMethod(NavigableMapMethods.TAIL_MAP_TWO_ARG)) {
            assertNotNull(map.tailMap(key, true));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.tailMap(key, true));
        }
    }
}
