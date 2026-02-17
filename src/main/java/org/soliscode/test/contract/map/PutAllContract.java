package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `putAll` method of a `Map`**
///
/// This interface defines tests for the [putAll(Map)][Map#putAll] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `putAll` implementation
/// correctly copies all mappings from the specified map to this map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapPutAllTest implements PutAllContract<String, String, MyMap<String, String>> {
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
/// @see Map#putAll
/// @since 1.0.0
public interface PutAllContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [putAll][Map#putAll] method adds all entries from another map.
    ///
    /// @see Map#putAll
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("putAll(Map) adds all entries from another map")
    default void putAll_whenCalled_addsAllEntries() {
        Map<K, V> map = provider().emptyInstance();
        Map<K, V> other = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.PUT_ALL)) {
            map.putAll(other);
            assertEquals(other.size(), map.size());
            other.forEach((k, v) -> {
                assertTrue(map.containsKey(k));
                assertEquals(v, map.get(k));
            });
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.putAll(other));
        }
    }
}
