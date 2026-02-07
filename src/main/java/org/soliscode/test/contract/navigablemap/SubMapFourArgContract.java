package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [NavigableMap#subMap(Object, boolean, Object, boolean)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface SubMapFourArgContract<K, V, M extends NavigableMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `subMap()` returns a map.
    @Test
    @DisplayName("Test subMap with four arguments returns a map")
    default void testSubMapFourArg() {
        NavigableMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K first = map.firstKey();
        K last = map.lastKey();
        if (supportsMethod(NavigableMapMethods.SUB_MAP_FOUR_ARG)) {
            assertNotNull(map.subMap(first, true, last, true));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.subMap(first, true, last, true));
        }
    }
}
