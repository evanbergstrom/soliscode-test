package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.sequencedmap.PutLastContract;

import java.util.SequencedMap;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

public interface SortedPutLastContract<K, V, M extends SortedMap<K, V>> extends PutLastContract<K, V, M> {

    /// Tests that `putLast()` throws UnsupportedOperation exception
    @Override
    @Test
    @DisplayName("Test putLast adds a new entry to the end of the map")
    default void testPutLastNewEntry() {
        SequencedMap<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();

        assertThrows(UnsupportedOperationException.class, () -> map.putLast(key, value));
    }

    /// Tests that `putLast()` throws UnsupportedOperation exception
    @Override
    @Test
    @DisplayName("Test putLast updates and moves an existing entry to the end of the map")
    default void testPutLastExistingEntry() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);

        SequencedMap<K, V> map = provider().emptyInstance();
        assertThrows(UnsupportedOperationException.class, () -> map.putLast(key1, value1));
    }
}
