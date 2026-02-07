package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#get(Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface GetContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `get()` returns `null` for a key not in the map.
    @Test
    @DisplayName("Test get returns null for a non-existent key")
    default void testGetOnEmptyMap() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        if (supportsMethod(MapMethods.GET)) {
            assertNull(map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.get(key));
        }
    }

    /// Tests that `get()` returns the correct value for a key that is in the map.
    @Test
    @DisplayName("Test get returns correct value for an existing key")
    default void testGetOnMapWithElements() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.GET)) {
            assertEquals(value, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.get(key));
        }
    }
}
