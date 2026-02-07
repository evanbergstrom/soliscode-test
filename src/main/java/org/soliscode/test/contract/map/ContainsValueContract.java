package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#containsValue(Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ContainsValueContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `containsValue()` returns `false` for a value not in the map.
    @Test
    @DisplayName("Test containsValue returns false for a non-existent value")
    default void testContainsValueOnEmptyMap() {
        Map<K, V> map = provider().emptyInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.CONTAINS_VALUE)) {
            assertFalse(map.containsValue(value));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.containsValue(value));
        }
    }

    /// Tests that `containsValue()` returns `true` for a value that is in the map.
    @Test
    @DisplayName("Test containsValue returns true for an existing value")
    default void testContainsValueOnMapWithElements() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.CONTAINS_VALUE)) {
            assertTrue(map.containsValue(value));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.containsValue(value));
        }
    }
}
