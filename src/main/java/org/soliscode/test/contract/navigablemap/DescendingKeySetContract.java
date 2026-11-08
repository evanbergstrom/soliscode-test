package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [descendingKeySet()][NavigableMap#descendingKeySet()] method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface DescendingKeySetContract<M extends NavigableMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [descendingKeySet()][NavigableMap#descendingKeySet()] returns the keys in descending order, as a [TreeMap] with the same entries and
    /// comparator does.
    @Test
    @DisplayName("descendingKeySet() returns the keys in descending order")
    default void descendingKeySet_whenCalled_returnsKeysInOrder() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.DESCENDING_KEY_SET)) {
            TreeMap<K, V> expected = sortedReference(map, entries);
            assertIterableEquals(expected.descendingKeySet(), map.descendingKeySet());
        } else {
            assertThrows(UnsupportedOperationException.class, map::descendingKeySet);
        }
    }
}
