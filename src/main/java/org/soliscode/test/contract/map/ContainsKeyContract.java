package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.contract.map.MapMethods.CONTAINS_KEY;

/// Contract for the [Map#containsKey(Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ContainsKeyContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `containsKey()` returns `false` for a key not in the map.
    @Test
    @DisplayName("Test containsKey returns false for a non-existent key")
    default void testContainsKeyOnEmptyMap() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        if (supportsMethod(CONTAINS_KEY)) {
            assertFalse(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.containsKey(key));
        }
    }

    /// Tests that `containsKey()` returns `true` for a key that is in the map.
    @Test
    @DisplayName("Test containsKey returns true for an existing key")
    default void testContainsKeyOnMapWithElements() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.CONTAINS_KEY)) {
            assertTrue(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.containsKey(key));
        }
    }
}
