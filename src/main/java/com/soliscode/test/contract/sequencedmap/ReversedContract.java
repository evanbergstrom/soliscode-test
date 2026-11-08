package com.soliscode.test.contract.sequencedmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.OrderedMapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
public interface ReversedContract<M extends SequencedMap<K, V>, K, V> extends OrderedMapContractSupport<M, K, V> {

    /// Verifies that `reversed()` returns a reversed view of the map or throws
    /// [UnsupportedOperationException] if not supported.
    ///
    /// @throws UnsupportedOperationException if the method is not supported and the contract
    ///         specifies it should not be.
    /// @since 1.0.0
    @Test
    @DisplayName("reversed: when called, returns reversed view or throws UnsupportedOperationException")
    default void reversedWhenCalledReturnsReversedView() {
        Map<K, V> entries = sourceEntries();
        M map = provider().createInstance(entries);
        if (supportsMethod(SequencedMapMethods.REVERSED)) {
            List<Map.Entry<K, V>> expected = new ArrayList<>(encounterOrder(map, entries));
            Collections.reverse(expected);
            SequencedMap<K, V> reversed = map.reversed();
            assertNotNull(reversed);
            assertIterableEquals(expected, reversed.entrySet());
            assertEquals(expected.getFirst(), reversed.firstEntry());
            assertEquals(expected.getLast(), reversed.lastEntry());
        } else {
            assertThrows(UnsupportedOperationException.class, map::reversed);
        }
    }
}
