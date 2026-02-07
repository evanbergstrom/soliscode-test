package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [NavigableMap#ceilingEntry(Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface CeilingEntryContract<K, V, M extends NavigableMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `ceilingEntry()` can be called.
    @Test
    @DisplayName("Test ceilingEntry can be called")
    default void testCeilingEntry() {
        NavigableMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K key = map.firstKey();
        if (supportsMethod(NavigableMapMethods.CEILING_ENTRY)) {
            map.ceilingEntry(key);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.ceilingEntry(key));
        }
    }
}
