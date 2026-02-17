package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NoSuchElementException;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `lastKey` method of a `SortedMap`**
///
/// This interface defines tests for the [lastKey()][SortedMap#lastKey] method.
/// It is designed to be used as a mix-in interface by test classes that verify [SortedMap]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a sorted map's `lastKey` implementation
/// correctly returns the last (highest) key currently in this map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MySortedMapLastKeyTest implements LastKeyContract<String, String, MySortedMap<String, String>> {
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
/// @see SortedMap#lastKey
/// @since 1.0.0
public interface LastKeyContract<K, V, M extends SortedMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [lastKey][SortedMap#lastKey] method throws [NoSuchElementException]
    /// for an empty map.
    ///
    /// @see SortedMap#lastKey
    /// @throws NoSuchElementException if the map is empty
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("lastKey() throws NoSuchElementException for an empty map")
    default void lastKey_whenEmpty_throwsException() {
        SortedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SortedMapMethods.LAST_KEY)) {
            assertThrows(NoSuchElementException.class, map::lastKey);
        } else {
            assertThrows(UnsupportedOperationException.class, map::lastKey);
        }
    }

    /// Tests that the [lastKey][SortedMap#lastKey] method returns the highest key for a
    /// map with elements.
    ///
    /// @see SortedMap#lastKey
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("lastKey() returns the highest key for a non-empty map")
    default void lastKey_whenNotEmpty_returnsKey() {
        SortedMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(SortedMapMethods.LAST_KEY)) {
            assertNotNull(map.lastKey());
        } else {
            assertThrows(UnsupportedOperationException.class, map::lastKey);
        }
    }
}
