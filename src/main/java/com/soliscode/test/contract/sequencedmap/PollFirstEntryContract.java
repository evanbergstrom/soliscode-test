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

/// Contract for the [pollFirstEntry()][SequencedMap#pollFirstEntry()] method of [SequencedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface PollFirstEntryContract<M extends SequencedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [pollFirstEntry()][SequencedMap#pollFirstEntry()] returns `null` for an empty map.
    @Test
    @DisplayName("pollFirstEntry() returns null for an empty map")
    default void pollFirstEntryWhenEmptyReturnsNull() {
        M map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.POLL_FIRST_ENTRY)) {
            assertNull(map.pollFirstEntry());
        } else {
            assertThrows(UnsupportedOperationException.class, map::pollFirstEntry);
        }
    }

    /// Tests that [pollFirstEntry()][SequencedMap#pollFirstEntry()] returns the first entry in encounter order, and
    /// removes it.
    @Test
    @DisplayName("pollFirstEntry() returns the first entry and removes it")
    default void pollFirstEntryWhenNotEmptyReturnsFirstEntry() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SequencedMapMethods.POLL_FIRST_ENTRY)) {
            List<Map.Entry<K, V>> order = encounterOrder(map, entries);
            Map.Entry<K, V> expected = order.getFirst();
            assertEquals(expected, map.pollFirstEntry());
            assertFalse(map.containsKey(expected.getKey()), "pollFirstEntry() did not remove the entry");
            assertEquals(defaultSize() - 1, map.size());
        } else {
            assertThrows(UnsupportedOperationException.class, map::pollFirstEntry);
        }
    }
}
