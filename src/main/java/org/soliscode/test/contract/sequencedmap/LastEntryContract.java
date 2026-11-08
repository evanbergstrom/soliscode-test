package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.OrderedMapContractSupport;

import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [lastEntry()][SequencedMap#lastEntry()] method of [SequencedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface LastEntryContract<M extends SequencedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [lastEntry()][SequencedMap#lastEntry()] returns `null` for an empty map.
    @Test
    @DisplayName("lastEntry() returns null for an empty map")
    default void lastEntry_whenEmpty_returnsNull() {
        M map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.LAST_ENTRY)) {
            assertNull(map.lastEntry());
        } else {
            assertThrows(UnsupportedOperationException.class, map::lastEntry);
        }
    }

    /// Tests that [lastEntry()][SequencedMap#lastEntry()] returns the last entry in encounter order.
    @Test
    @DisplayName("lastEntry() returns the last entry")
    default void lastEntry_whenNotEmpty_returnsLastEntry() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SequencedMapMethods.LAST_ENTRY)) {
            List<Map.Entry<K, V>> order = encounterOrder(map, entries);
            Map.Entry<K, V> expected = order.getLast();
            assertEquals(expected, map.lastEntry());
        } else {
            assertThrows(UnsupportedOperationException.class, map::lastEntry);
        }
    }
}
