package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `computeIfPresent` method of a `Map`**
///
/// This interface defines tests for the [computeIfPresent(K, BiFunction)][Map#computeIfPresent] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `computeIfPresent` implementation
/// correctly computes a mapping for the specified key and its current mapped value,
/// provided the key is present.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapComputeIfPresentTest implements ComputeIfPresentContract<String, String, MyMap<String, String>> {
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
/// @see Map#computeIfPresent
/// @since 1.0.0
public interface ComputeIfPresentContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [computeIfPresent][Map#computeIfPresent] method returns `null` and does not add if the key is
    /// not present.
    ///
    /// @see Map#computeIfPresent
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("computeIfPresent(K, BiFunction) returns null and does not add if the key is not present")
    default void computeIfPresent_whenKeyNotPresent_returnsNull() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        if (supportsMethod(MapMethods.COMPUTE_IF_PRESENT)) {
            assertNull(map.computeIfPresent(key, (k, v) -> value));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfPresent(key, (k, v) -> value));
        }
    }

    /// Tests that the [computeIfPresent][Map#computeIfPresent] method computes and updates the value if the key is
    /// present.
    ///
    /// @see Map#computeIfPresent
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("computeIfPresent(K, BiFunction) computes and updates the value if the key is present")
    default void computeIfPresent_whenKeyPresent_updatesValue() {
        K key = keyProvider().createInstance();
        V oldValue = valueProvider().createInstance(1);
        V newValue = valueProvider().createInstance(2);
        Map<K, V> map = provider().createSingleton(key, oldValue);
        if (supportsMethod(MapMethods.COMPUTE_IF_PRESENT)) {
            assertEquals(newValue, map.computeIfPresent(key, (k, v) -> newValue));
            assertEquals(newValue, map.get(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.computeIfPresent(key, (k, v) -> newValue));
        }
    }
}
