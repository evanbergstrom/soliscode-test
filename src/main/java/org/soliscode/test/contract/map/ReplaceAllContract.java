package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `replaceAll` method of a `Map`**
///
/// This interface defines tests for the [replaceAll(BiFunction)][Map#replaceAll] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `replaceAll` implementation
/// correctly replaces each entry's value with the result of invoking the given
/// function on that entry.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapReplaceAllTest implements ReplaceAllContract<String, String, MyMap<String, String>> {
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
/// @see Map#replaceAll
/// @since 1.0.0
public interface ReplaceAllContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [replaceAll][Map#replaceAll] method replaces all values in the map.
    ///
    /// @see Map#replaceAll
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("replaceAll(BiFunction) replaces all values in the map")
    default void replaceAll_whenCalled_replacesAllValues() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        V newValue = valueProvider().createInstance();
        if (supportsMethod(MapMethods.REPLACE_ALL)) {
            map.replaceAll((k, v) -> newValue);
            map.forEach((k, v) -> assertEquals(newValue, v));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((k, v) -> newValue));
        }
    }
}
