package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [SequencedMap#pollLastEntry()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface PollLastEntryContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `pollLastEntry()` can be called.
    @Test
    @DisplayName("Test pollLastEntry can be called")
    default void testPollLastEntry() {
        SequencedMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(SequencedMapMethods.POLL_LAST_ENTRY)) {
            map.pollLastEntry();
        } else {
            assertThrows(UnsupportedOperationException.class, map::pollLastEntry);
        }
    }
}
