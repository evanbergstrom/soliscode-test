package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the `sequencedEntrySet()` method of [SequencedMap].
///
/// ### Purpose
/// Verifies that the `sequencedEntrySet()` method correctly returns a sequenced set view
/// of the map's entries, or handles unsupported operations appropriately.
///
/// ### Usage Examples
///
/// #### Implementation
/// ```java
/// public class MySequencedMapTest implements SequencedEntrySetContract<String, String, MySequencedMap<String, String>> {
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
/// @see SequencedMap#sequencedEntrySet()
/// @author evanbergstrom
/// @since 1.0.0
public interface SequencedEntrySetContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Verifies that `sequencedEntrySet()` returns a sequenced set of entries or
    /// throws [UnsupportedOperationException] if not supported.
    ///
    /// @throws UnsupportedOperationException if the method is not supported and the contract
    ///         specifies it should not be.
    /// @since 1.0.0
    @Test
    @DisplayName("sequencedEntrySet: when called, returns sequenced set or throws UnsupportedOperationException")
    default void sequencedEntrySet_whenCalled_returnsSequencedSet() {
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
