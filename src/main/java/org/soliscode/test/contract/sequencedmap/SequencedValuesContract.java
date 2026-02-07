package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Collection;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [SequencedMap#sequencedValues()] method.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface SequencedValuesContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that `sequencedValues()` returns a sequenced collection of values.
    @Test
    @DisplayName("Test sequencedValues returns a sequenced collection of values")
    default void testSequencedValues() {
        SequencedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.SEQUENCED_VALUES)) {
            Collection<V> values = map.sequencedValues();
            assertNotNull(values);
            assertTrue(values.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::sequencedValues);
        }
    }
}
