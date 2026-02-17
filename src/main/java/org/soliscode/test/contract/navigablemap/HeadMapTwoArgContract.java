package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `headMap` method of a `NavigableMap` with two arguments**
///
/// This interface defines tests for the [headMap(K, boolean)][NavigableMap#headMap] method.
/// It is designed to be used as a mix-in interface by test classes that verify [NavigableMap]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a navigable map's `headMap` implementation
/// correctly returns a view of the portion of this map whose keys are less than (or equal to,
/// if inclusive is true) `toKey`.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyNavigableMapHeadMapTwoArgTest implements HeadMapTwoArgContract<String, String, MyNavigableMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MyNavigableMap<String, String>> provider() {
///         return MyNavigableMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [NavigableMap] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see NavigableMap#headMap(Object, boolean)
/// @since 1.0.0
public interface HeadMapTwoArgContract<K, V, M extends NavigableMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [headMap][NavigableMap#headMap] method can be called and returns
    /// a non-null map.
    ///
    /// @see NavigableMap#headMap(Object, boolean)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("headMap(K, boolean) returns a non-null map")
    default void headMapTwoArg_whenCalled_returnsNonNullMap() {
        NavigableMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K key = map.firstKey();
        if (supportsMethod(NavigableMapMethods.HEAD_MAP_TWO_ARG)) {
            assertNotNull(map.headMap(key, true));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.headMap(key, true));
        }
    }
}
