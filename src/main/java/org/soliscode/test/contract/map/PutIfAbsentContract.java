package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#putIfAbsent(Object, Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface PutIfAbsentContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `putIfAbsent()` adds a new entry if the key is not present.
    @Test
    @DisplayName("Test putIfAbsent adds a new entry if the key is not present")
    default void testPutIfAbsentNewEntry() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.PUT_IF_ABSENT)) {
            assertNull(map.putIfAbsent(key, value));
            assertEquals(value, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent(key, value));
        }
    }

    /// Tests that `putIfAbsent()` returns the existing value and does not update if the key is present.
    @Test
    @DisplayName("Test putIfAbsent returns the existing value and does not update if the key is present")
    default void testPutIfAbsentExistingEntry() {
        K key = keyProvider().createInstance();
        V value1 = valueProvider().createInstance(1);
        V value2 = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, value1);
        if (supportsMethod(MapMethods.PUT_IF_ABSENT)) {
            assertEquals(value1, map.putIfAbsent(key, value2));
            assertEquals(value1, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent(key, value2));
        }
    }
}
