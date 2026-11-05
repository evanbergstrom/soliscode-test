package org.soliscode.test.contract.sequencedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the `reversed()` method of [SequencedMap].
///
/// ### Purpose
/// Verifies that the `reversed()` method correctly returns a reversed view of the map,
/// or handles unsupported operations appropriately.
///
/// ### Usage Examples
///
/// #### Implementation
/// ```java
/// class MySequencedMapTest implements ReversedContract<MySequencedMap<String, String>, String, String> {
///     @Override
///     public MapProvider<MySequencedMap<String, String>, String, String> objectProvider() {
///         return new MySequencedMapProvider();
///     }
/// }
/// ```
///
/// ### Thread Safety
/// The tests in this contract are not guaranteed to be thread-safe. If the map implementation
/// is intended for concurrent use, additional thread-safety tests should be performed.
///
/// @param <M> The map type being tested.
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @see SequencedMap#reversed()
/// @author evanbergstrom
/// @since 1.0.0
public interface ReversedContract<M extends SequencedMap<K, V>, K, V> extends MapContractSupport<M, K, V> {

    /// Verifies that `reversed()` returns a reversed view of the map or throws
    /// [UnsupportedOperationException] if not supported.
    ///
    /// @throws UnsupportedOperationException if the method is not supported and the contract
    ///         specifies it should not be.
    /// @since 1.0.0
    @Test
    @DisplayName("reversed: when called, returns reversed view or throws UnsupportedOperationException")
    default void reversed_whenCalled_returnsReversedView() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);
        K key2 = keyProvider().createInstance(2);
        V value2 = valueProvider().createInstance(2);

        SequencedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.REVERSED)) {
            map.put(key1, value1);
            map.put(key2, value2);

            SequencedMap<K, V> reversed = map.reversed();
            assertNotNull(reversed);
            assertEquals(2, reversed.size());
            assertEquals(key2, reversed.firstEntry().getKey());
            assertEquals(key1, reversed.lastEntry().getKey());
        } else if (!supportsMethod(SequencedMapMethods.REVERSED)) {
            assertThrows(UnsupportedOperationException.class, map::reversed);
        }
    }
}
