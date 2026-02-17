package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `computeIfAbsent` method of a `Map`**
///
/// This interface defines tests for the [computeIfAbsent(K, Function)][Map#computeIfAbsent] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `computeIfAbsent` implementation
/// correctly computes a value for the specified key if it is not already present.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapComputeIfAbsentTest implements ComputeIfAbsentContract<String, String, MyMap<String, String>> {
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
/// @see Map#computeIfAbsent
/// @since 1.0.0
public interface ComputeIfAbsentContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [computeIfAbsent][Map#computeIfAbsent] method computes and adds a value if the key is not present.
    ///
    /// @see Map#computeIfAbsent
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("computeIfAbsent(K, Function) computes and adds a value if the key is not present")
    default void computeIfAbsent_whenKeyNotPresent_addsValue() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.COMPUTE_IF_ABSENT)) {
            assertEquals(value, map.computeIfAbsent(key, k -> value));
            assertEquals(value, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(key, k -> value));
        }
    }

    /// Tests that the [computeIfAbsent][Map#computeIfAbsent] method returns the existing value if the key is present.
    ///
    /// @see Map#computeIfAbsent
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("computeIfAbsent(K, Function) returns the existing value if the key is present")
    default void computeIfAbsent_whenKeyPresent_returnsExistingValue() {
        K key = keyProvider().createInstance();
        V value1 = valueProvider().createInstance(1);
        V value2 = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, value1);
        if (supportsMethod(MapMethods.COMPUTE_IF_ABSENT)) {
            assertEquals(value1, map.computeIfAbsent(key, k -> value2));
            assertEquals(value1, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(key, k -> value2));
        }
    }
}
