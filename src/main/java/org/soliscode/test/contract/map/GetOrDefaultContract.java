package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `getOrDefault` method of a `Map`**
///
/// This interface defines tests for the [getOrDefault(Object, V)][Map#getOrDefault] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `getOrDefault` implementation
/// correctly returns the value to which the specified key is mapped, or the
/// `defaultValue` if the map contains no mapping for the key.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapGetOrDefaultTest implements GetOrDefaultContract<String, String, MyMap<String, String>> {
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
/// @see Map#getOrDefault
/// @since 1.0.0
public interface GetOrDefaultContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [getOrDefault][Map#getOrDefault] method returns the default value for a non-existent key.
    ///
    /// @see Map#getOrDefault
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("getOrDefault(Object, V) returns default value for a non-existent key")
    default void getOrDefault_whenKeyNotPresent_returnsDefaultValue() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V defaultValue = valueProvider().createInstance();
        if (supportsMethod(MapMethods.GET_OR_DEFAULT)) {
            assertEquals(defaultValue, map.getOrDefault(key, defaultValue));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.getOrDefault(key, defaultValue));
        }
    }

    /// Tests that the [getOrDefault][Map#getOrDefault] method returns the correct value for an existing key.
    ///
    /// @see Map#getOrDefault
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("getOrDefault(Object, V) returns correct value for an existing key")
    default void getOrDefault_whenKeyPresent_returnsCorrectValue() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance(1);
        V defaultValue = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.GET_OR_DEFAULT)) {
            assertEquals(value, map.getOrDefault(key, defaultValue));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.getOrDefault(key, defaultValue));
        }
    }
}
