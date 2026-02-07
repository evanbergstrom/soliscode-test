package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SequencedMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [SequencedMap#sequencedKeySet()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface SequencedKeySetContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `sequencedKeySet()` returns a sequenced set of keys.
    @Test
    @DisplayName("Test sequencedKeySet returns a sequenced set of keys")
    default void testSequencedKeySet() {
        SequencedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.SEQUENCED_KEY_SET)) {
            Set<K> keySet = map.sequencedKeySet();
            assertNotNull(keySet);
            assertTrue(keySet.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::sequencedKeySet);
        }
    }
}
