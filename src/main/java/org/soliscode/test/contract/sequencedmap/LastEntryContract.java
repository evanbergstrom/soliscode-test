package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the `lastEntry()` method of [SequencedMap].
///
/// ### Purpose
/// Verifies that the `lastEntry()` method correctly returns the last entry in the map's
/// sequencing order, or handles the empty case and unsupported operations appropriately.
///
/// ### Usage Examples
///
/// #### Implementation
/// ```java
/// public class MySequencedMapTest implements LastEntryContract<String, String, MySequencedMap<String, String>> {
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
/// @see SequencedMap#lastEntry()
/// @author evanbergstrom
/// @since 1.0.0
public interface LastEntryContract<K, V, M extends SequencedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Verifies that `lastEntry()` can be called successfully or throws [UnsupportedOperationException]
    /// if the method is not supported by the implementation.
    ///
    /// @throws UnsupportedOperationException if the method is not supported and the contract
    ///         specifies it should not be.
    /// @since 1.0.0
    @Test
    @DisplayName("lastEntry() when called, is successful or throws UnsupportedOperationException")
    default void lastEntry_whenCalled_isSuccessful() {
        SequencedMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(SequencedMapMethods.LAST_ENTRY)) {
            map.lastEntry();
        } else {
            assertThrows(UnsupportedOperationException.class, map::lastEntry);
        }
    }
}
