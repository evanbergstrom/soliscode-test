package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `remove` method of a `Map`**
///
/// This interface defines tests for the [remove(Object)][Map#remove] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `remove` implementation
/// correctly removes the mapping for the specified key from the map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapRemoveTest implements RemoveContract<String, String, MyMap<String, String>> {
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
/// @see Map#remove
/// @since 1.0.0
public interface RemoveContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [remove][Map#remove] method returns `null` for a non-existent key.
    ///
    /// @see Map#remove
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("remove(Object) returns null for a non-existent key")
    default void remove_whenKeyNotPresent_returnsNull() {
        Map<K, V> map = provider().emptyInstance();
        K key = keyProvider().createInstance();
        if (supportsMethod(MapMethods.REMOVE)) {
            assertNull(map.remove(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.remove(key));
        }
    }

    /// Tests that the [remove][Map#remove] method removes an existing entry and returns its value.
    ///
    /// @see Map#remove
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("remove(Object) removes an existing entry and returns its value")
    default void remove_whenKeyPresent_removesEntryAndReturnsValue() {
        K key = keyProvider().createInstance();
        V value = valueProvider().createInstance();
        Map<K, V> map = provider().createSingleton(key, value);
        if (supportsMethod(MapMethods.REMOVE)) {
            assertEquals(value, map.remove(key));
            assertFalse(map.containsKey(key));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.remove(key));
        }
    }
}
