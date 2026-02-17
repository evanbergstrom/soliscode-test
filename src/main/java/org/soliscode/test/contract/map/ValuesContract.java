package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `values` method of a `Map`**
///
/// This interface defines tests for the [values()][Map#values] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `values` implementation
/// correctly returns a [java.util.Collection] view of the values contained in this map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapValuesTest implements ValuesContract<String, String, MyMap<String, String>> {
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
/// @see Map#values
/// @since 1.0.0
public interface ValuesContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [values][Map#values] method returns a collection containing all values in the map.
    ///
    /// @see Map#values
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("values() returns a collection containing all values in the map")
    default void values_whenCalled_returnsAllValues() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.VALUES)) {
            Collection<V> values = map.values();
            assertEquals(map.size(), values.size());
            map.forEach((k, v) -> assertTrue(values.contains(v)));
        } else {
            assertThrows(UnsupportedOperationException.class, map::values);
        }
    }
}
