package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [Map#merge(Object, Object, java.util.function.BiFunction)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface MergeContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `merge()` adds the value if the key is not present.
    @Test
    @DisplayName("Test merge adds the value if the key is not present")
    default void testMergeNewEntry() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.MERGE)) {
            assertEquals(value, map.merge(key, value, (v1, v2) -> v2));
            assertEquals(value, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.merge(key, value, (v1, v2) -> v2));
        }
    }

    /// Tests that `merge()` merges the values if the key is present.
    @Test
    @DisplayName("Test merge merges the values if the key is present")
    default void testMergeExistingEntry() {
        K key = keyProvider().createInstance();
        V oldValue = valueProvider().createInstance(1);
        V newValue = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, oldValue);
        if (supportsMethod(MapMethods.MERGE)) {
            assertEquals(newValue, map.merge(key, newValue, (v1, v2) -> v2));
            assertEquals(newValue, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.merge(key, newValue, (v1, v2) -> v2));
        }
    }
}
