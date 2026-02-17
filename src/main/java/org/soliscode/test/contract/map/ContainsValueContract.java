package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `containsValue` method of a `Map`**
///
/// This interface defines tests for the [containsValue(Object)][Map#containsValue] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `containsValue` implementation
/// correctly returns `true` if the map contains one or more keys mapped to the specified value.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapContainsValueTest implements ContainsValueContract<String, String, MyMap<String, String>> {
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
/// @see Map#containsValue
/// @since 1.0.0
public interface ContainsValueContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [containsValue][Map#containsValue] method returns `false` for an empty map.
    ///
    /// @see Map#containsValue
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("containsValue(Object) returns false for an empty map")
    default void containsValue_whenEmpty_returnsFalse() {
        Map<K, V> map = provider().emptyInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.CONTAINS_VALUE)) {
            assertFalse(map.containsValue(value));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.containsValue(value));
        }
    }

    /// Tests that the [containsValue][Map#containsValue] method returns `true` for an existing value.
    ///
    /// @see Map#containsValue
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("containsValue(Object) returns true for an existing value")
    default void containsValue_whenNotEmpty_returnsTrue() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.CONTAINS_VALUE)) {
            assertTrue(map.containsValue(value));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.containsValue(value));
        }
    }
}
