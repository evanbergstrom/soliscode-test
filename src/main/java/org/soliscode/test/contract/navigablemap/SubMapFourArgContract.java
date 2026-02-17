package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `subMap` method of a `NavigableMap` with four arguments**
///
/// This interface defines tests for the [subMap(K, boolean, K, boolean)][NavigableMap#subMap]
/// method. It is designed to be used as a mix-in interface by test classes that verify
/// [NavigableMap] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a navigable map's `subMap` implementation
/// correctly returns a view of the portion of this map whose keys range from `fromKey` to `toKey`.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyNavigableMapSubMapFourArgTest
///     implements SubMapFourArgContract<String, String, MyNavigableMap<String, String>> {
///
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
/// @see NavigableMap#subMap(Object, boolean, Object, boolean)
/// @since 1.0.0
public interface SubMapFourArgContract<K, V, M extends NavigableMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [subMap][NavigableMap#subMap] method can be called and returns
    /// a non-null map.
    ///
    /// @see NavigableMap#subMap(Object, boolean, Object, boolean)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("subMap(K, boolean, K, boolean) returns a non-null map")
    default void subMapFourArg_whenCalled_returnsNonNullMap() {
        NavigableMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K first = map.firstKey();
        K last = map.lastKey();
        if (supportsMethod(NavigableMapMethods.SUB_MAP_FOUR_ARG)) {
            assertNotNull(map.subMap(first, true, last, true));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.subMap(first, true, last, true));
        }
    }
}
