package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [Map#size()] method.
///
/// This contract provides tests to ensure that the `size()` method of a `Map`
/// implementation correctly reports the number of key-value mappings it contains.
/// It covers both empty maps and maps with elements, and handles cases where
/// the method might not be supported.
///
/// ## Usage Example
///
/// To use this contract, implement it in your test class along with the necessary
/// provider methods:
///
/// ```java
/// public class MyMapSizeTest implements SizeContract<String, String, MyMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MyMap<String, String>> provider() {
///         return new MyMapProvider();
///     }
///
///     @Override
///     public ElementProvider<String> keyProvider() {
///         return new StringProvider();
///     }
///
///     @Override
///     public ElementProvider<String> valueProvider() {
///         return new StringProvider();
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// Implementations of this contract are expected to be thread-safe for use by
/// the JUnit test runner. The tested `Map` instance itself should be handled
/// according to its own thread safety guarantees.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see Map#size()
/// @since 1.0
public interface SizeContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the `size()` method returns 0 for an empty map.
    /// @throws UnsupportedOperationException if the `size()` method is not supported.
    /// @throws AssertionError if the `size()` method does not return 0 for an empty map.
    @Test
    @DisplayName("Test size returns 0 for an empty map")
    default void testSizeOnEmptyMap() {
        Map<K, V> map = provider().emptyInstance();
        if (supportsMethod(MapMethods.SIZE)) {
            assertEquals(0, map.size());
        } else {
            assertThrows(UnsupportedOperationException.class, map::size);
        }
    }

    /// Tests that the `size()` method returns the correct value for a map with elements.
    /// @throws UnsupportedOperationException if the `size()` method is not supported.
    /// @throws AssertionError if the `size()` method does not return the expected value for a map with elements.
    @Test
    @DisplayName("Test size returns correct value for a map with elements")
    default void testSizeOnMapWithElements() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.SIZE)) {
            assertEquals(1, map.size());
        } else {
            assertThrows(UnsupportedOperationException.class, map::size);
        }
    }
}
