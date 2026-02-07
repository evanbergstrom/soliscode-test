package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NoSuchElementException;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [SortedMap#lastKey()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface LastKeyContract<K, V, M extends SortedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `lastKey()` throws [NoSuchElementException] for an empty map.
    @Test
    @DisplayName("Test lastKey throws NoSuchElementException for an empty map")
    default void testLastKeyOnEmptyMap() {
        SortedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SortedMapMethods.LAST_KEY)) {
            assertThrows(NoSuchElementException.class, map::lastKey);
        } else {
            assertThrows(UnsupportedOperationException.class, map::lastKey);
        }
    }

    /// Tests that `lastKey()` returns a key for a map with elements.
    @Test
    @DisplayName("Test lastKey returns a key for a map with elements")
    default void testLastKeyOnMapWithElements() {
        SortedMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(SortedMapMethods.LAST_KEY)) {
            assertNotNull(map.lastKey());
        } else {
            assertThrows(UnsupportedOperationException.class, map::lastKey);
        }
    }
}
