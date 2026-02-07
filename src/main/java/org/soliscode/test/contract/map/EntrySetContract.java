package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [Map#entrySet()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface EntrySetContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `entrySet()` returns a set containing all entries in the map.
    @Test
    @DisplayName("Test entrySet returns a set containing all entries in the map")
    default void testEntrySet() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.ENTRY_SET)) {
            Set<Map.Entry<K, V>> entrySet = map.entrySet();
            assertEquals(map.size(), entrySet.size());
            map.forEach((k, v) -> {
                boolean found = false;
                for (Map.Entry<K, V> entry : entrySet) {
                    if (entry.getKey().equals(k) && entry.getValue().equals(v)) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found);
            });
        } else {
            assertThrows(UnsupportedOperationException.class, map::entrySet);
        }
    }
}
