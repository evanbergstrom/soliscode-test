package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#isEmpty()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface IsEmptyContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the `isEmpty()` method returns `true` for an empty map.
    @Test
    @DisplayName("Test isEmpty returns true for an empty map")
    default void testIsEmptyOnEmptyMap() {
        Map<K, V> map = provider().emptyInstance();
        if (supportsMethod(MapMethods.IS_EMPTY)) {
            assertTrue(map.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::isEmpty);
        }
    }

    /// Tests that the `isEmpty()` method returns `false` for a map with elements.
    @Test
    @DisplayName("Test isEmpty returns false for a map with elements")
    default void testIsEmptyOnMapWithElements() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.IS_EMPTY)) {
            assertFalse(map.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::isEmpty);
        }
    }
}
