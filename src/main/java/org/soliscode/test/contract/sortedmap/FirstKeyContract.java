package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NoSuchElementException;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [SortedMap#firstKey()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface FirstKeyContract<K, V, M extends SortedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `firstKey()` throws [NoSuchElementException] for an empty map.
    @Test
    @DisplayName("Test firstKey throws NoSuchElementException for an empty map")
    default void testFirstKeyOnEmptyMap() {
        SortedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SortedMapMethods.FIRST_KEY)) {
            assertThrows(NoSuchElementException.class, map::firstKey);
        } else {
            assertThrows(UnsupportedOperationException.class, map::firstKey);
        }
    }

    /// Tests that `firstKey()` returns a key for a map with elements.
    @Test
    @DisplayName("Test firstKey returns a key for a map with elements")
    default void testFirstKeyOnMapWithElements() {
        SortedMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(SortedMapMethods.FIRST_KEY)) {
            assertNotNull(map.firstKey());
        } else {
            assertThrows(UnsupportedOperationException.class, map::firstKey);
        }
    }
}
