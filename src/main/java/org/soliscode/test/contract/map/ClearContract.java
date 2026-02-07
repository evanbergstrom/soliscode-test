package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#clear()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ClearContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `clear()` removes all entries from the map.
    @SuppressWarnings("ConstantValue")
    @Test
    @DisplayName("Test clear removes all entries from the map")
    default void testClear() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.CLEAR)) {
            map.clear();
            assertEquals(0, map.size());
            assertTrue(map.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::clear);
        }
    }
}
