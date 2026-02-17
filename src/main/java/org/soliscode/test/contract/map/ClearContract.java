package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
/// @see Map#clear
/// @since 1.0.0
public interface ClearContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [clear][Map#clear] method removes all entries from the map.
    ///
    /// @see Map#clear
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @SuppressWarnings("ConstantValue")
    @Test
    @DisplayName("clear() removes all entries from the map")
    default void clear_whenCalled_removesAllEntries() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.CLEAR)) {
            map.clear();
            assertEquals(0, map.size());
            assertTrue(map.isEmpty());
        } else {
            assertThrows(UnsupportedOperationException.class, map::clear);
        }
    }
}
