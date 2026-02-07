package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#computeIfPresent(Object, java.util.function.BiFunction)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ComputeIfPresentContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `computeIfPresent()` returns `null` and does not add_singleElement_returnsTrueAndUpdatesSize if the key is not present.
    @Test
    @DisplayName("Test computeIfPresent returns null and does not add_singleElement_returnsTrueAndUpdatesSize if the key is not present")
    default void testComputeIfPresentKeyNotPresent() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.COMPUTE_IF_PRESENT)) {
            assertNull(map.computeIfPresent(key, (k, v) -> value));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfPresent(key, (k, v) -> value));
        }
    }

    /// Tests that `computeIfPresent()` computes and updates the value if the key is present.
    @Test
    @DisplayName("Test computeIfPresent computes and updates the value if the key is present")
    default void testComputeIfPresentKeyPresent() {
        K key = keyProvider().createInstance();
        V oldValue = valueProvider().createInstance(1);
        V newValue = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, oldValue);
        if (supportsMethod(MapMethods.COMPUTE_IF_PRESENT)) {
            assertEquals(newValue, map.computeIfPresent(key, (k, v) -> newValue));
            assertEquals(newValue, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfPresent(key, (k, v) -> newValue));
        }
    }
}
