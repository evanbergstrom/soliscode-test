package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#keySet()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface KeySetContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `keySet()` returns a set containing all keys in the map.
    @Test
    @DisplayName("Test keySet returns a set containing all keys in the map")
    default void testKeySet() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.KEY_SET)) {
            Set<K> keySet = map.keySet();
            assertEquals(map.size(), keySet.size());
            map.forEach((k, v) -> assertTrue(keySet.contains(k)));
        } else {
            assertThrows(UnsupportedOperationException.class, map::keySet);
        }
    }
}
