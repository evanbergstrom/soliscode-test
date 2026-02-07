package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.sequencedmap.PutFirstContract;

import java.util.SequencedMap;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

public interface SortedPutFirstContract<K, V, M extends SortedMap<K, V>> extends PutFirstContract<K, V, M> {

    /// Tests that `putFirst()` throws UnsupportedOperation exception
    @Override
    @Test
    @DisplayName("Test putFirst adds a new entry to the front of the map")
    default void testPutFirstNewEntry() {
        SequencedMap<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        assertThrows(UnsupportedOperationException.class, () -> map.putFirst(key, value));
    }

    /// Tests that `putFirst()` throws UnsupportedOperation exception
    @Test
    @DisplayName("Test putFirst updates and moves an existing entry to the front of the map")
    default void testPutFirstExistingEntry() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);
        SequencedMap<K, V> map = provider().emptyInstance();
        assertThrows(UnsupportedOperationException.class, () -> map.putFirst(key1, value1));
    }
}
