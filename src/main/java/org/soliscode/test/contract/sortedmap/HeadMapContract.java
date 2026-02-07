package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [SortedMap#headMap(Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface HeadMapContract<K, V, M extends SortedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `headMap()` returns a map.
    @Test
    @DisplayName("Test headMap returns a map")
    default void testHeadMap() {
        SortedMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K key = map.firstKey();
        if (supportsMethod(SortedMapMethods.HEAD_MAP)) {
            assertNotNull(map.headMap(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.headMap(key));
        }
    }
}
