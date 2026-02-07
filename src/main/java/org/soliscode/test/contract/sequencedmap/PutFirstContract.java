package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [SequencedMap#putFirst(Object, Object)] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface PutFirstContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `putFirst()` adds a new entry to the front of the map.
    @Test
    @DisplayName("Test putFirst adds a new entry to the front of the map")
    default void testPutFirstNewEntry() {
        SequencedMap<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(SequencedMapMethods.PUT_FIRST)) {
            assertNull(map.putFirst(key, value));
            assertTrue(map.containsKey(key));
            assertEquals(value, map.get(key));
            assertEquals(key, map.firstEntry().getKey());
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.putFirst(key, value));
        }
    }

    /// Tests that `putFirst()` updates and moves an existing entry to the front of the map.
    @Test
    @DisplayName("Test putFirst updates and moves an existing entry to the front of the map")
    default void testPutFirstExistingEntry() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);
        K key2 = keyProvider().createInstance(2);
        V value2 = valueProvider().createInstance(2);

        SequencedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.PUT_FIRST) && supportsMethod(SequencedMapMethods.PUT_LAST)) {
            map.putLast(key1, value1);
            map.putLast(key2, value2);

            V newValue1 = valueProvider().createInstance(3);
            assertEquals(value1, map.putFirst(key1, newValue1));
            assertEquals(key1, map.firstEntry().getKey());
            assertEquals(newValue1, map.get(key1));
        } else if (!supportsMethod(SequencedMapMethods.PUT_FIRST)) {
            assertThrows(UnsupportedOperationException.class, () -> map.putFirst(key1, value1));
        }
    }
}
