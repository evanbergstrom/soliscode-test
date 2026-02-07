package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#replace(Object, Object, Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ReplaceThreeArgContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `replace(key, oldValue, newValue)` returns `false` if the key is not present.
    @Test
    @DisplayName("Test replace(key, oldValue, newValue) returns false if the key is not present")
    default void testReplaceThreeArgKeyNotPresent() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V oldValue = valueProvider().createInstance(1);
        V newValue = valueProvider().createInstance(2);
        if (supportsMethod(MapMethods.REPLACE_THREE_ARG)) {
            assertFalse(map.replace(key, oldValue, newValue));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, oldValue, newValue));
        }
    }

    /// Tests that `replace(key, oldValue, newValue)` returns `false` if the key is present but with a different value.
    @Test
    @DisplayName("Test replace(key, oldValue, newValue) returns false if the key is present but with a different value")
    default void testReplaceThreeArgValueMismatch() {
        K key = keyProvider().createInstance();
        V valueInMap = valueProvider().createInstance(1);
        V oldValue = valueProvider().createInstance(2);
        V newValue = valueProvider().createInstance(3);
        Map<K, V> map = provider().createSingleton(key, valueInMap);
        if (supportsMethod(MapMethods.REPLACE_THREE_ARG)) {
            assertFalse(map.replace(key, oldValue, newValue));
            assertEquals(valueInMap, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, oldValue, newValue));
        }
    }

    /// Tests that `replace(key, oldValue, newValue)` updates the entry and returns `true` if the key and value match.
    @Test
    @DisplayName("Test replace(key, oldValue, newValue) updates the entry and returns true if the key and value match")
    default void testReplaceThreeArgMatch() {
        K key = keyProvider().createInstance();
        V valueInMap = valueProvider().createInstance(1);
        V newValue = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, valueInMap);
        if (supportsMethod(MapMethods.REPLACE_THREE_ARG)) {
            assertTrue(map.replace(key, valueInMap, newValue));
            assertEquals(newValue, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, valueInMap, newValue));
        }
    }
}
