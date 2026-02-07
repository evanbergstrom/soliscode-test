package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [Map#replaceAll(java.util.function.BiFunction)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ReplaceAllContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `replaceAll()` replaces all values in the map.
    @Test
    @DisplayName("Test replaceAll replaces all values in the map")
    default void testReplaceAll() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        V newValue = valueProvider().createInstance();
        if (supportsMethod(MapMethods.REPLACE_ALL)) {
            map.replaceAll((k, v) -> newValue);
            map.forEach((k, v) -> assertEquals(newValue, v));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((k, v) -> newValue));
        }
    }
}
