package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.Map;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [comparator()][SortedMap#comparator()] method of [SortedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface ComparatorContract<M extends SortedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [comparator()][SortedMap#comparator()] describes the map's order: the keys, in iteration order,
    /// are in the order of the comparator it returns (natural order, for `null`).
    @Test
    @DisplayName("comparator() describes the order of the keys")
    default void comparator_whenCalled_describesKeyOrder() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SortedMapMethods.COMPARATOR)) {
            assertIterableEquals(sortedReference(map, entries).keySet(), map.keySet(),
                    "the keys are not in the order of comparator()");
        } else {
            assertThrows(UnsupportedOperationException.class, map::comparator);
        }
    }
}
