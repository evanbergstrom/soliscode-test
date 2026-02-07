package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [NavigableMap#floorKey(Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface FloorKeyContract<K, V, M extends NavigableMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `floorKey()` can be called.
    @Test
    @DisplayName("Test floorKey can be called")
    default void testFloorKey() {
        NavigableMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K key = map.firstKey();
        if (supportsMethod(NavigableMapMethods.FLOOR_KEY)) {
            map.floorKey(key);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.floorKey(key));
        }
    }
}
