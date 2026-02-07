package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [SequencedMap#pollFirstEntry()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface PollFirstEntryContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `pollFirstEntry()` can be called.
    @Test
    @DisplayName("Test pollFirstEntry can be called")
    default void testPollFirstEntry() {
        SequencedMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(SequencedMapMethods.POLL_FIRST_ENTRY)) {
            map.pollFirstEntry();
        } else {
            assertThrows(UnsupportedOperationException.class, map::pollFirstEntry);
        }
    }
}
