package com.soliscode.test.contract.map;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.MapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `size` method of a `Map`**
///
/// This contract provides tests to ensure that the [size()][Map#size] method of a `Map`
/// implementation correctly reports the number of key-value mappings it contains.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `size` implementation
/// correctly returns the number of key-value mappings in this map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapSizeTest implements SizeContract<String, String, MyMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MyMap<String, String>> objectProvider() {
///         return MyMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Map] and [com.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see Map#size
/// @since 1.0.0
public interface SizeContract<M extends Map<K, V>, K, V> extends MapContractSupport<M, K, V> {

    /// Tests that the [size][Map#size] method returns 0 for an empty map.
    ///
    /// @see Map#size
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("size() returns 0 for an empty map")
    default void sizeWhenEmptyReturnsZero() {
        Map<K, V> map = provider().emptyInstance();
        if (supportsMethod(MapMethods.SIZE)) {
            assertEquals(0, map.size());
        } else {
            assertThrows(UnsupportedOperationException.class, map::size);
        }
    }

    /// Tests that the [size][Map#size] method returns the correct value for a map with elements.
    ///
    /// @see Map#size
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("size() returns correct value for a map with elements")
    default void sizeWhenNotEmptyReturnsCorrectSize() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.SIZE)) {
            assertEquals(1, map.size());
        } else {
            assertThrows(UnsupportedOperationException.class, map::size);
        }
    }
}
