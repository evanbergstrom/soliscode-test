package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#values()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ValuesContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `values()` returns a collection containing all values in the map.
    @Test
    @DisplayName("Test values returns a collection containing all values in the map")
    default void testValues() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.VALUES)) {
            Collection<V> values = map.values();
            assertEquals(map.size(), values.size());
            map.forEach((k, v) -> assertTrue(values.contains(v)));
        } else {
            assertThrows(UnsupportedOperationException.class, map::values);
        }
    }
}
