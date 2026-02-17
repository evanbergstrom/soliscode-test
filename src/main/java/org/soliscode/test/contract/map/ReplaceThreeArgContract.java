package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the three-argument `replace` method of a `Map`**
///
/// This interface defines tests for the [replace(K, V, V)][Map#replace] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's three-argument `replace` implementation
/// correctly replaces the entry for the specified key only if it is currently mapped to
/// the specified old value.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapReplaceThreeArgTest implements ReplaceThreeArgContract<String, String, MyMap<String, String>> {
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
/// @see Map#replace(Object, Object, Object)
/// @since 1.0.0
public interface ReplaceThreeArgContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [replace][Map#replace(Object, Object, Object)] method returns `false` if the key is not present.
    ///
    /// @see Map#replace(Object, Object, Object)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("replace(K, V, V) returns false if the key is not present")
    default void replace_whenKeyNotPresent_returnsFalse() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V oldValue = valueProvider().createInstance(1);
        V newValue = valueProvider().createInstance(2);
        if (supportsMethod(MapMethods.REPLACE_THREE_ARG)) {
            assertFalse(map.replace(key, oldValue, newValue));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, oldValue, newValue));
        }
    }

    /// Tests that the [replace][Map#replace(Object, Object, Object)] method returns `false` if the key is present but with a different value.
    ///
    /// @see Map#replace(Object, Object, Object)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("replace(K, V, V) returns false if the key is present but with a different value")
    default void replace_whenValueMismatch_returnsFalse() {
        K key = keyProvider().createInstance();
        V valueInMap = valueProvider().createInstance(1);
        V oldValue = valueProvider().createInstance(2);
        V newValue = valueProvider().createInstance(3);
        Map<K, V> map = provider().createSingleton(key, valueInMap);
        if (supportsMethod(MapMethods.REPLACE_THREE_ARG)) {
            assertFalse(map.replace(key, oldValue, newValue));
            assertEquals(valueInMap, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, oldValue, newValue));
        }
    }

    /// Tests that the [replace][Map#replace(Object, Object, Object)] method updates the entry and returns `true` if the key and value match.
    ///
    /// @see Map#replace(Object, Object, Object)
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("replace(K, V, V) updates the entry and returns true if the key and value match")
    default void replace_whenMatch_updatesEntryAndReturnsTrue() {
        K key = keyProvider().createInstance();
        V valueInMap = valueProvider().createInstance(1);
        V newValue = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, valueInMap);
        if (supportsMethod(MapMethods.REPLACE_THREE_ARG)) {
            assertTrue(map.replace(key, valueInMap, newValue));
            assertEquals(newValue, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replace(key, valueInMap, newValue));
        }
    }
}
