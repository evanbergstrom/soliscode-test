package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `keySet` method of a `Map`**
///
/// This interface defines tests for the [keySet()][Map#keySet] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `keySet` implementation
/// correctly returns a [java.util.Set] view of the keys contained in this map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapKeySetTest implements KeySetContract<String, String, MyMap<String, String>> {
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
/// @see Map#keySet
/// @since 1.0.0
public interface KeySetContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [keySet][Map#keySet] method returns a set containing all keys in the map.
    ///
    /// @see Map#keySet
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("keySet() returns a set containing all keys in the map")
    default void keySet_whenCalled_returnsAllKeys() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.KEY_SET)) {
            Set<K> keySet = map.keySet();
            assertEquals(map.size(), keySet.size());
            map.forEach((k, v) -> assertTrue(keySet.contains(k)));
        } else {
            assertThrows(UnsupportedOperationException.class, map::keySet);
        }
    }
}
