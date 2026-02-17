package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the two-argument `remove` method of a `Map`**
///
/// This interface defines tests for the [remove(Object, Object)][Map#remove] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's two-argument `remove` implementation
/// correctly removes the entry for the specified key only if it is currently mapped to
/// the specified value.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapRemoveTwoArgTest implements RemoveTwoArgContract<String, String, MyMap<String, String>> {
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
/// @see Map#remove(Object, Object)
/// @since 1.0.0
public interface RemoveTwoArgContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [remove][Map#remove(Object, Object)] method returns `false` if the key is not present.
    ///
    /// @see Map#remove(Object, Object)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("remove(Object, Object) returns false if the key is not present")
    default void remove_whenKeyNotPresent_returnsFalse() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.REMOVE_TWO_ARG)) {
            assertFalse(map.remove(key, value));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.remove(key, value));
        }
    }

    /// Tests that the [remove][Map#remove(Object, Object)] method returns `false` if the key is present but with a different value.
    ///
    /// @see Map#remove(Object, Object)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("remove(Object, Object) returns false if the key is present but with a different value")
    default void remove_whenValueMismatch_returnsFalse() {
        K key = keyProvider().createInstance();
        V value1 = valueProvider().createInstance(1);
        V value2 = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, value1);
        if (supportsMethod(MapMethods.REMOVE_TWO_ARG)) {
            assertFalse(map.remove(key, value2));
            assertTrue(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.remove(key, value2));
        }
    }

    /// Tests that the [remove][Map#remove(Object, Object)] method removes the entry and returns `true` if the key and value match.
    ///
    /// @see Map#remove(Object, Object)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("remove(Object, Object) removes the entry and returns true if the key and value match")
    default void remove_whenMatch_removesEntryAndReturnsTrue() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.REMOVE_TWO_ARG)) {
            assertTrue(map.remove(key, value));
            assertFalse(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.remove(key, value));
        }
    }
}
