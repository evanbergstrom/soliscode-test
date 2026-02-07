package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [NavigableMap#descendingKeySet()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface DescendingKeySetContract<K, V, M extends NavigableMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `descendingKeySet()` can be called.
    @Test
    @DisplayName("Test descendingKeySet can be called")
    default void testDescendingKeySet() {
        NavigableMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(NavigableMapMethods.DESCENDING_KEY_SET)) {
            assertNotNull(map.descendingKeySet());
        } else {
            assertThrows(UnsupportedOperationException.class, map::descendingKeySet);
        }
    }
}
