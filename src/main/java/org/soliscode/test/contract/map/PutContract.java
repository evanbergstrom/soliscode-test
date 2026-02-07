package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#put(Object, Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface PutContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `put()` adds a new entry to the map.
    @Test
    @DisplayName("Test put adds a new entry to the map")
    default void testPutNewEntry() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.PUT)) {
            assertNull(map.put(key, value));
            assertTrue(map.containsKey(key));
            assertEquals(value, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.put(key, value));
        }
    }

    /// Tests that `put()` updates an existing entry in the map.
    @Test
    @DisplayName("Test put updates an existing entry in the map")
    default void testPutExistingEntry() {
        K key = keyProvider().createInstance();
        V value1 = valueProvider().createInstance(1);
        V value2 = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, value1);
        if (supportsMethod(MapMethods.PUT)) {
            assertEquals(value1, map.put(key, value2));
            assertEquals(value2, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.put(key, value2));
        }
    }
}
