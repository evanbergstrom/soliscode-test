package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [SequencedMap#putLast(Object, Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface PutLastContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `putLast()` adds a new entry to the end of the map.
    @Test
    @DisplayName("Test putLast adds a new entry to the end of the map")
    default void testPutLastNewEntry() {
        SequencedMap<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(SequencedMapMethods.PUT_LAST)) {
            assertNull(map.putLast(key, value));
            assertTrue(map.containsKey(key));
            assertEquals(value, map.get(key));
            assertEquals(key, map.lastEntry().getKey());
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.putLast(key, value));
        }
    }

    /// Tests that `putLast()` updates and moves an existing entry to the end of the map.
    @Test
    @DisplayName("Test putLast updates and moves an existing entry to the end of the map")
    default void testPutLastExistingEntry() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);
        K key2 = keyProvider().createInstance(2);
        V value2 = valueProvider().createInstance(2);

        SequencedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.PUT_LAST) && supportsMethod(SequencedMapMethods.PUT_FIRST)) {
            map.putFirst(key1, value1);
            map.putFirst(key2, value2);

            V newValue1 = valueProvider().createInstance(3);
            assertEquals(value1, map.putLast(key1, newValue1));
            assertEquals(key1, map.lastEntry().getKey());
            assertEquals(newValue1, map.get(key1));
        } else if (!supportsMethod(SequencedMapMethods.PUT_LAST)) {
            assertThrows(UnsupportedOperationException.class, () -> map.putLast(key1, value1));
        }
    }
}
