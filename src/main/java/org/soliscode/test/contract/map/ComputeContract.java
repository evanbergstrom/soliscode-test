package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [Map#compute(Object, java.util.function.BiFunction)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ComputeContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `compute()` computes and adds a value if the key is not present.
    @Test
    @DisplayName("Test compute computes and adds a value if the key is not present")
    default void testComputeNewEntry() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.COMPUTE)) {
            assertEquals(value, map.compute(key, (k, v) -> value));
            assertEquals(value, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.compute(key, (k, v) -> value));
        }
    }

    /// Tests that `compute()` computes and updates the value if the key is present.
    @Test
    @DisplayName("Test compute computes and updates the value if the key is present")
    default void testComputeExistingEntry() {
        K key = keyProvider().createInstance();
        V oldValue = valueProvider().createInstance(1);
        V newValue = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, oldValue);
        if (supportsMethod(MapMethods.COMPUTE)) {
            assertEquals(newValue, map.compute(key, (k, v) -> newValue));
            assertEquals(newValue, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.compute(key, (k, v) -> newValue));
        }
    }
}
