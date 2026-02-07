package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#remove(Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface RemoveContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `remove()` returns `null` for a key not in the map.
    @Test
    @DisplayName("Test remove returns null for a non-existent key")
    default void testRemoveOnEmptyMap() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        if (supportsMethod(MapMethods.REMOVE)) {
            assertNull(map.remove(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.remove(key));
        }
    }

    /// Tests that `remove()` removes an existing entry and returns its value.
    @Test
    @DisplayName("Test remove removes an existing entry and returns its value")
    default void testRemoveOnMapWithElements() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.REMOVE)) {
            assertEquals(value, map.remove(key));
            assertFalse(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.remove(key));
        }
    }
}
