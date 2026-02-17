package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.sequencedmap.ReversedContract;
import org.soliscode.test.contract.sequencedmap.SequencedMapMethods;

import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `reversed` method of a `SortedMap`**
///
/// This interface defines tests for the [reversed()][java.util.SequencedMap#reversed] method
/// for [SortedMap] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a sorted map's `reversed` implementation
/// correctly returns a reverse-order view of the mappings contained in this map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MySortedMapReversedTest implements SortedReversedContract<String, String, MySortedMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MySortedMap<String, String>> provider() {
///         return MySortedMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [SortedMap] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see java.util.SequencedMap#reversed
/// @since 1.0.0
public interface SortedReversedContract<K, V, M extends SortedMap<K, V>> extends ReversedContract<K, V, M> {

    /// Tests that the [reversed][java.util.SequencedMap#reversed] method returns a reversed view.
    ///
    /// @see java.util.SequencedMap#reversed
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("reversed() returns a reversed view of the map")
    default void reversed_whenCalled_returnsExpectedView() {
        K key1 = keyProvider().createInstance(1);
        V value1 = valueProvider().createInstance(1);
        K key2 = keyProvider().createInstance(2);
        V value2 = valueProvider().createInstance(2);

        SortedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SequencedMapMethods.REVERSED) && supportsMethod(SequencedMapMethods.PUT_LAST)) {
            map.put(key1, value1);
            map.put(key2, value2);

            SortedMap<K, V> reversed = map.reversed();
            assertNotNull(reversed);
            assertEquals(2, reversed.size());

            assertEquals(map.lastKey(), reversed.firstKey());
            assertEquals(map.firstKey(), reversed.lastKey());
        } else if (!supportsMethod(SequencedMapMethods.REVERSED)) {
            assertThrows(UnsupportedOperationException.class, map::reversed);
        }
    }
}
