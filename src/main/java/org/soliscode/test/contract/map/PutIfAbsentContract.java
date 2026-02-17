package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `putIfAbsent` method of a `Map`**
///
/// This interface defines tests for the [putIfAbsent(K, V)][Map#putIfAbsent] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `putIfAbsent` implementation
/// correctly associates the specified value with the specified key if the key
/// is not already associated with a value.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapPutIfAbsentTest implements PutIfAbsentContract<String, String, MyMap<String, String>> {
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
/// @see Map#putIfAbsent
/// @since 1.0.0
public interface PutIfAbsentContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [putIfAbsent][Map#putIfAbsent] method adds a new entry if the key is not present.
    ///
    /// @see Map#putIfAbsent
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("putIfAbsent(K, V) adds a new entry if the key is not present")
    default void putIfAbsent_whenKeyNotPresent_addsEntry() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.PUT_IF_ABSENT)) {
            assertNull(map.putIfAbsent(key, value));
            assertEquals(value, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent(key, value));
        }
    }

    /// Tests that the [putIfAbsent][Map#putIfAbsent] method returns the existing value and does not update if the key is present.
    ///
    /// @see Map#putIfAbsent
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("putIfAbsent(K, V) returns existing value and does not update if the key is present")
    default void putIfAbsent_whenKeyPresent_returnsExistingValue() {
        K key = keyProvider().createInstance();
        V value1 = valueProvider().createInstance(1);
        V value2 = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, value1);
        if (supportsMethod(MapMethods.PUT_IF_ABSENT)) {
            assertEquals(value1, map.putIfAbsent(key, value2));
            assertEquals(value1, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent(key, value2));
        }
    }
}
