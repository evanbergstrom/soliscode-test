package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [SequencedMap#sequencedEntrySet()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface SequencedEntrySetContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `sequencedEntrySet()` returns a sequenced set of entries.
    @Test
    @DisplayName("Test sequencedEntrySet returns a sequenced set of entries")
    default void testSequencedEntrySet() {
        SequencedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.SEQUENCED_ENTRY_SET)) {
            Set<Map.Entry<K, V>> entrySet = map.sequencedEntrySet();
            assertNotNull(entrySet);
            assertTrue(entrySet.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::sequencedEntrySet);
        }
    }
}
