package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [Map#computeIfAbsent(Object, java.util.function.Function)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ComputeIfAbsentContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `computeIfAbsent()` computes and adds a value if the key is not present.
    @Test
    @DisplayName("Test computeIfAbsent computes and adds a value if the key is not present")
    default void testComputeIfAbsentNewEntry() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.COMPUTE_IF_ABSENT)) {
            assertEquals(value, map.computeIfAbsent(key, k -> value));
            assertEquals(value, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(key, k -> value));
        }
    }

    /// Tests that `computeIfAbsent()` returns the existing value if the key is present.
    @Test
    @DisplayName("Test computeIfAbsent returns the existing value if the key is present")
    default void testComputeIfAbsentExistingEntry() {
        K key = keyProvider().createInstance();
        V value1 = valueProvider().createInstance(1);
        V value2 = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, value1);
        if (supportsMethod(MapMethods.COMPUTE_IF_ABSENT)) {
            assertEquals(value1, map.computeIfAbsent(key, k -> value2));
            assertEquals(value1, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(key, k -> value2));
        }
    }
}
