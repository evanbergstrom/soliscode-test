package com.soliscode.test.contract.sequencedmap;

import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.OrderedMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [firstEntry()][SequencedMap#firstEntry()] method of [SequencedMap].
///
/// @param <M> the type of map
/// @param <K> the type of keys
/// @param <V> the type of values
/// @since 1.0
public interface FirstEntryContract<M extends SequencedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Tests that [firstEntry()][SequencedMap#firstEntry()] returns `null` for an empty map.
    @Test
    @DisplayName("firstEntry() returns null for an empty map")
    default void firstEntryWhenEmptyReturnsNull() {
        M map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.FIRST_ENTRY)) {
            assertNull(map.firstEntry());
        } else {
            assertThrows(UnsupportedOperationException.class, map::firstEntry);
        }
    }

    /// Tests that [firstEntry()][SequencedMap#firstEntry()] returns the first entry in encounter order.
    @Test
    @DisplayName("firstEntry() returns the first entry")
    default void firstEntryWhenNotEmptyReturnsFirstEntry() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SequencedMapMethods.FIRST_ENTRY)) {
            List<Map.Entry<K, V>> order = encounterOrder(map, entries);
            Map.Entry<K, V> expected = order.getFirst();
            assertEquals(expected, map.firstEntry());
        } else {
            assertThrows(UnsupportedOperationException.class, map::firstEntry);
        }
    }
}
