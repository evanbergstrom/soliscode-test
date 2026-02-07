package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.sequencedmap.ReversedContract;
import org.soliscode.test.contract.sequencedmap.SequencedMapMethods;

import java.util.SequencedMap;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [SequencedMap#reversed()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface SortedReversedContract<K, V, M extends SortedMap<K, V>> extends ReversedContract<K, V, M> {

    /// Tests that `reversed()` throws an UnsupportedOperation exception
    @Override
    @Test
    @DisplayName("Test reversed returns a reversed view of the map")
    default void testReversed() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);
        K key2 = keyProvider().createInstance(2);
        V value2 = valueProvider().createInstance(2);

        SortedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.REVERSED) && supportsMethod(SequencedMapMethods.PUT_LAST)) {
            map.put(key1, value1);
            map.put(key2, value2);

            SortedMap<K, V> reversed = map.reversed();
            assertNotNull(reversed);
            assertEquals(2, reversed.size());

            assertEquals(map.lastKey(), reversed.firstKey());
            assertEquals(map.firstKey(), reversed.lastKey());
        } else if (!supportsMethod(SequencedMapMethods.REVERSED)) {
            assertThrows(UnsupportedOperationException.class, map::reversed);
        }
    }
}
