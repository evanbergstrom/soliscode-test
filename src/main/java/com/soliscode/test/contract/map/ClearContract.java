package com.soliscode.test.contract.map;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.MapContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `clear` method of a `Map`**
///
/// This interface defines tests for the [clear()][Map#clear] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Map] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `clear` implementation
/// correctly removes all mappings from the map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapClearTest implements ClearContract<String, String, MyMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MyMap<String, String>> objectProvider() {
///         return MyMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Map] and [com.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <M> The map type being tested.
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @author evanbergstrom
/// @see Map#clear
/// @since 1.0.0
public interface ClearContract<M extends Map<K, V>, K, V> extends MapContractSupport<M, K, V> {

    /// Tests that the [clear][Map#clear] method removes all entries from the map.
    ///
    /// @see Map#clear
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @SuppressWarnings("ConstantValue")
    @Test
    @DisplayName("clear() removes all entries from the map")
    default void clearWhenCalledRemovesAllEntries() {
        Map<K, V> map = provider().createInstance(defaultSize());
        if (supportsMethod(MapMethods.CLEAR)) {
            map.clear();
            assertEquals(0, map.size());
            assertTrue(map.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::clear);
        }
    }
}
