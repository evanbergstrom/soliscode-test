package com.soliscode.test.contract.navigablemap;

import java.util.Map;
import java.util.NavigableMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.OrderedMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [navigableKeySet()][NavigableMap#navigableKeySet()] method of [NavigableMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface NavigableKeySetContract<M extends NavigableMap<K, V>, K, V>
        extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [navigableKeySet()][NavigableMap#navigableKeySet()] returns the keys in ascending order, as a
    /// [TreeMap][java.util.TreeMap] with the same entries and comparator does.
    @Test
    @DisplayName("navigableKeySet() returns the keys in ascending order")
    default void navigableKeySetWhenCalledReturnsKeysInOrder() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(NavigableMapMethods.NAVIGABLE_KEY_SET)) {
            NavigableMap<K, V> expected = sortedReference(map, entries);
            assertIterableEquals(expected.navigableKeySet(), map.navigableKeySet());
        } else {
            assertThrows(UnsupportedOperationException.class, map::navigableKeySet);
        }
    }
}
