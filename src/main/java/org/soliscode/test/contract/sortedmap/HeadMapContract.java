package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `headMap` method of a `SortedMap`**
///
/// This interface defines tests for the [headMap(K)][SortedMap#headMap] method.
/// It is designed to be used as a mix-in interface by test classes that verify [SortedMap]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a sorted map's `headMap` implementation
/// correctly returns a view of the portion of this map whose keys are strictly less than `toKey`.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MySortedMapHeadMapTest implements HeadMapContract<String, String, MySortedMap<String, String>> {
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
/// @see SortedMap#headMap(Object)
/// @since 1.0.0
public interface HeadMapContract<K, V, M extends SortedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [headMap][SortedMap#headMap] method can be called and returns
    /// a non-null map.
    ///
    /// @see SortedMap#headMap(Object)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("headMap(K) returns a non-null map")
    default void headMap_whenCalled_returnsNonNullMap() {
        SortedMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K key = map.firstKey();
        if (supportsMethod(SortedMapMethods.HEAD_MAP)) {
            assertNotNull(map.headMap(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.headMap(key));
        }
    }
}
