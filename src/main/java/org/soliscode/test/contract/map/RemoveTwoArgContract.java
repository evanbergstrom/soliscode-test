package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#remove(Object, Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface RemoveTwoArgContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `remove(key, value)` returns `false` if the key is not present.
    @Test
    @DisplayName("Test remove(key, value) returns false if the key is not present")
    default void testRemoveTwoArgKeyNotPresent() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.REMOVE_TWO_ARG)) {
            assertFalse(map.remove(key, value));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.remove(key, value));
        }
    }

    /// Tests that `remove(key, value)` returns `false` if the key is present but with a different value.
    @Test
    @DisplayName("Test remove(key, value) returns false if the key is present but with a different value")
    default void testRemoveTwoArgValueMismatch() {
        K key = keyProvider().createInstance();
        V value1 = valueProvider().createInstance(1);
        V value2 = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, value1);
        if (supportsMethod(MapMethods.REMOVE_TWO_ARG)) {
            assertFalse(map.remove(key, value2));
            assertTrue(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.remove(key, value2));
        }
    }

    /// Tests that `remove(key, value)` removes the entry and returns `true` if the key and value match.
    @Test
    @DisplayName("Test remove(key, value) removes the entry and returns true if the key and value match")
    default void testRemoveTwoArgMatch() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.REMOVE_TWO_ARG)) {
            assertTrue(map.remove(key, value));
            assertFalse(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.remove(key, value));
        }
    }
}
