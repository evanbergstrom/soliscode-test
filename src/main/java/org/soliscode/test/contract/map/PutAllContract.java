package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#putAll(Map)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface PutAllContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `putAll()` adds all entries from another map.
    @Test
    @DisplayName("Test putAll adds all entries from another map")
    default void testPutAll() {
        Map<K, V> map = provider().emptyInstance();
        Map<K, V> other = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.PUT_ALL)) {
            map.putAll(other);
            assertEquals(other.size(), map.size());
            other.forEach((k, v) -> {
                assertTrue(map.containsKey(k));
                assertEquals(v, map.get(k));
            });
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.putAll(other));
        }
    }
}
