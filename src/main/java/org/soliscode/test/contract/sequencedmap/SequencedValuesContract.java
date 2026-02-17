package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Collection;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the `sequencedValues()` method of [SequencedMap].
///
/// ### Purpose
/// Verifies that the `sequencedValues()` method correctly returns a sequenced collection
/// view of the map's values, or handles unsupported operations appropriately.
///
/// ### Usage Examples
///
/// #### Implementation
/// ```java
/// public class MySequencedMapTest implements SequencedValuesContract<String, String, MySequencedMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MySequencedMap<String, String>> provider() {
///         return new MySequencedMapProvider();
///     }
/// }
/// ```
///
/// ### Thread Safety
/// The tests in this contract are not guaranteed to be thread-safe. If the map implementation
/// is intended for concurrent use, additional thread-safety tests should be performed.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @see SequencedMap#sequencedValues()
/// @author evanbergstrom
/// @since 1.0.0
public interface SequencedValuesContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Verifies that `sequencedValues()` returns a sequenced collection of values or
    /// throws [UnsupportedOperationException] if not supported.
    ///
    /// @throws UnsupportedOperationException if the method is not supported and the contract
    ///         specifies it should not be.
    /// @since 1.0.0
    @Test
    @DisplayName("sequencedValues: when called, returns sequenced collection or throws UnsupportedOperationException")
    default void sequencedValues_whenCalled_returnsSequencedCollection() {
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
