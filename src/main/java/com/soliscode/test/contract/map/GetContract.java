package com.soliscode.test.contract.map;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.MapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `get` method of a `Map`**
///
/// This interface defines tests for the [get(Object)][Map#get] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `get` implementation
/// correctly returns the value to which the specified key is mapped, or `null`
/// if the map contains no mapping for the key.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapGetTest implements GetContract<String, String, MyMap<String, String>> {
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
/// @see Map#get
/// @since 1.0.0
public interface GetContract<M extends Map<K, V>, K, V> extends MapContractSupport<M, K, V> {

    /// Tests that the [get][Map#get] method returns `null` for a non-existent key.
    ///
    /// @see Map#get
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("get(Object) returns null for a non-existent key")
    default void getWhenEmptyReturnsNull() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        if (supportsMethod(MapMethods.GET)) {
            assertNull(map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.get(key));
        }
    }

    /// Tests that the [get][Map#get] method returns the correct value for an existing key.
    ///
    /// @see Map#get
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("get(Object) returns correct value for an existing key")
    default void getWhenNotEmptyReturnsCorrectValue() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.GET)) {
            assertEquals(value, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.get(key));
        }
    }
}
