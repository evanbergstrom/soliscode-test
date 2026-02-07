package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#replace(Object, Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ReplaceTwoArgContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `replace(key, value)` returns `null` if the key is not present.
    @Test
    @DisplayName("Test replace(key, value) returns null if the key is not present")
    default void testReplaceTwoArgKeyNotPresent() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.REPLACE_TWO_ARG)) {
            assertNull(map.replace(key, value));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, value));
        }
    }

    /// Tests that `replace(key, value)` updates the entry and returns the old value if the key is present.
    @Test
    @DisplayName("Test replace(key, value) updates the entry and returns the old value if the key is present")
    default void testReplaceTwoArgKeyPresent() {
        K key = keyProvider().createInstance();
        V oldValue = valueProvider().createInstance(1);
        V newValue = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, oldValue);
        if (supportsMethod(MapMethods.REPLACE_TWO_ARG)) {
            assertEquals(oldValue, map.replace(key, newValue));
            assertEquals(newValue, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, newValue));
        }
    }
}
