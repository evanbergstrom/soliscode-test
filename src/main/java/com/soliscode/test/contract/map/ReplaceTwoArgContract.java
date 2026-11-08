package com.soliscode.test.contract.map;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.MapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the two-argument `replace` method of a `Map`**
///
/// This interface defines tests for the [replace(K, V)][Map#replace] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's two-argument `replace` implementation
/// correctly replaces the entry for the specified key only if it is currently mapped to
/// some value.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapReplaceTwoArgTest implements ReplaceTwoArgContract<String, String, MyMap<String, String>> {
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
/// @see Map#replace(Object, Object)
/// @since 1.0.0
public interface ReplaceTwoArgContract<M extends Map<K, V>, K, V> extends MapContractSupport<M, K, V> {

    /// Tests that the [replace][Map#replace(Object, Object)] method returns `null` if the key is not present.
    ///
    /// @see Map#replace(Object, Object)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("replace(K, V) returns null if the key is not present")
    default void replaceWhenKeyNotPresentReturnsNull() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.REPLACE_TWO_ARG)) {
            assertNull(map.replace(key, value));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, value));
        }
    }

    /// Tests that the [replace][Map#replace(Object, Object)] method updates the entry and returns the old value if
    /// the key is present.
    ///
    /// @see Map#replace(Object, Object)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("replace(K, V) updates the entry and returns the old value if the key is present")
    default void replaceWhenKeyPresentUpdatesEntryAndReturnsOldValue() {
        K key = keyProvider().createInstance();
        V oldValue = valueProvider().createInstance(1);
        V newValue = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, oldValue);
        if (supportsMethod(MapMethods.REPLACE_TWO_ARG)) {
            assertEquals(oldValue, map.replace(key, newValue));
            assertEquals(newValue, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, newValue));
        }
    }
}
