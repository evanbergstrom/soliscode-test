package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [SequencedMap#reversed()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ReversedContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `reversed()` returns a view that is the reverse of the original map.
    @Test
    @DisplayName("Test reversed returns a reversed view of the map")
    default void testReversed() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);
        K key2 = keyProvider().createInstance(2);
        V value2 = valueProvider().createInstance(2);

        SequencedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.REVERSED) && supportsMethod(SequencedMapMethods.PUT_LAST)) {
            map.putLast(key1, value1);
            map.putLast(key2, value2);

            SequencedMap<K, V> reversed = map.reversed();
            assertNotNull(reversed);
            assertEquals(2, reversed.size());
            assertEquals(key2, reversed.firstEntry().getKey());
            assertEquals(key1, reversed.lastEntry().getKey());
        } else if (!supportsMethod(SequencedMapMethods.REVERSED)) {
            assertThrows(UnsupportedOperationException.class, map::reversed);
        }
    }
}
