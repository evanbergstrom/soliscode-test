package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [SortedMap#subMap(Object, Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface SubMapContract<K, V, M extends SortedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `subMap()` returns a map.
    @Test
    @DisplayName("Test subMap returns a map")
    default void testSubMap() {
        SortedMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K first = map.firstKey();
        K last = map.lastKey();
        if (supportsMethod(SortedMapMethods.SUB_MAP)) {
            assertNotNull(map.subMap(first, last));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.subMap(first, last));
        }
    }
}
