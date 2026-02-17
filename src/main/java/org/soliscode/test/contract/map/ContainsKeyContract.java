package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.contract.map.MapMethods.CONTAINS_KEY;

/// **Contract for the `containsKey` method of a `Map`**
///
/// This interface defines tests for the [containsKey(Object)][Map#containsKey] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `containsKey` implementation
/// correctly returns `true` if the map contains a mapping for the specified key.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapContainsKeyTest implements ContainsKeyContract<String, String, MyMap<String, String>> {
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
/// @see Map#containsKey
/// @since 1.0.0
public interface ContainsKeyContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [containsKey][Map#containsKey] method returns `false` for an empty map.
    ///
    /// @see Map#containsKey
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("containsKey(Object) returns false for an empty map")
    default void containsKey_whenEmpty_returnsFalse() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        if (supportsMethod(CONTAINS_KEY)) {
            assertFalse(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.containsKey(key));
        }
    }

    /// Tests that the [containsKey][Map#containsKey] method returns `true` for an existing key.
    ///
    /// @see Map#containsKey
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("containsKey(Object) returns true for an existing key")
    default void containsKey_whenNotEmpty_returnsTrue() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.CONTAINS_KEY)) {
            assertTrue(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.containsKey(key));
        }
    }
}
