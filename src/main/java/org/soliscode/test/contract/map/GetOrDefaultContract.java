package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [Map#getOrDefault(Object, Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface GetOrDefaultContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `getOrDefault()` returns the default value for a key not in the map.
    @Test
    @DisplayName("Test getOrDefault returns default value for a non-existent key")
    default void testGetOrDefaultOnEmptyMap() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V defaultValue = valueProvider().createInstance();
        if (supportsMethod(MapMethods.GET_OR_DEFAULT)) {
            assertEquals(defaultValue, map.getOrDefault(key, defaultValue));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.getOrDefault(key, defaultValue));
        }
    }

    /// Tests that `getOrDefault()` returns the correct value for a key that is in the map.
    @Test
    @DisplayName("Test getOrDefault returns correct value for an existing key")
    default void testGetOrDefaultOnMapWithElements() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance(1);
        V defaultValue = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.GET_OR_DEFAULT)) {
            assertEquals(value, map.getOrDefault(key, defaultValue));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.getOrDefault(key, defaultValue));
        }
    }
}
