package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `isEmpty` method of a `Map`**
///
/// This interface defines tests for the [isEmpty()][Map#isEmpty] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `isEmpty` implementation
/// correctly returns `true` if the map contains no key-value mappings.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapIsEmptyTest implements IsEmptyContract<String, String, MyMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MyMap<String, String>> provider() {
///         return MyMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Map] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see Map#isEmpty
/// @since 1.0.0
public interface IsEmptyContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [isEmpty][Map#isEmpty] method returns `true` for an empty map.
    ///
    /// @see Map#isEmpty
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("isEmpty() returns true for an empty map")
    default void isEmpty_whenEmpty_returnsTrue() {
        Map<K, V> map = provider().emptyInstance();
        if (supportsMethod(MapMethods.IS_EMPTY)) {
            assertTrue(map.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::isEmpty);
        }
    }

    /// Tests that the [isEmpty][Map#isEmpty] method returns `false` for a map with elements.
    ///
    /// @see Map#isEmpty
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("isEmpty() returns false for a map with elements")
    default void isEmpty_whenNotEmpty_returnsFalse() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.IS_EMPTY)) {
            assertFalse(map.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::isEmpty);
        }
    }
}
