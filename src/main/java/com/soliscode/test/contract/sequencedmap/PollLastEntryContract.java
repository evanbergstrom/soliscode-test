package com.soliscode.test.contract.sequencedmap;

import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.OrderedMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [pollLastEntry()][SequencedMap#pollLastEntry()] method of [SequencedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface PollLastEntryContract<M extends SequencedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [pollLastEntry()][SequencedMap#pollLastEntry()] returns `null` for an empty map.
    @Test
    @DisplayName("pollLastEntry() returns null for an empty map")
    default void pollLastEntryWhenEmptyReturnsNull() {
        M map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.POLL_LAST_ENTRY)) {
            assertNull(map.pollLastEntry());
        } else {
            assertThrows(UnsupportedOperationException.class, map::pollLastEntry);
        }
    }

    /// Tests that [pollLastEntry()][SequencedMap#pollLastEntry()] returns the last entry in encounter order, and
    /// removes it.
    @Test
    @DisplayName("pollLastEntry() returns the last entry and removes it")
    default void pollLastEntryWhenNotEmptyReturnsLastEntry() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SequencedMapMethods.POLL_LAST_ENTRY)) {
            List<Map.Entry<K, V>> order = encounterOrder(map, entries);
            Map.Entry<K, V> expected = order.getLast();
            assertEquals(expected, map.pollLastEntry());
            assertFalse(map.containsKey(expected.getKey()), "pollLastEntry() did not remove the entry");
            assertEquals(defaultSize() - 1, map.size());
        } else {
            assertThrows(UnsupportedOperationException.class, map::pollLastEntry);
        }
    }
}
