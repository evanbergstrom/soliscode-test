package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [NavigableMap#higherKey(Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface HigherKeyContract<K, V, M extends NavigableMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `higherKey()` can be called.
    @Test
    @DisplayName("Test higherKey can be called")
    default void testHigherKey() {
        NavigableMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K key = map.firstKey();
        if (supportsMethod(NavigableMapMethods.HIGHER_KEY)) {
            map.higherKey(key);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.higherKey(key));
        }
    }
}
