package org.soliscode.test.contract.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `entrySet` method of a `Map`**
///
/// This interface defines tests for the [entrySet()][Map#entrySet] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Map]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map's `entrySet` implementation
/// correctly returns a [java.util.Set] view of the mappings contained in this map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapEntrySetTest implements EntrySetContract<String, String, MyMap<String, String>> {
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
/// @see Map#entrySet
/// @since 1.0.0
public interface EntrySetContract<K, V, M extends Map<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [entrySet][Map#entrySet] method returns a set containing all entries in the map.
    ///
    /// @see Map#entrySet
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("entrySet() returns a set containing all entries in the map")
    default void entrySet_whenCalled_returnsAllEntries() {
        Map<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(MapMethods.ENTRY_SET)) {
            Set<Map.Entry<K, V>> entrySet = map.entrySet();
            assertEquals(map.size(), entrySet.size());
            map.forEach((k, v) -> {
                boolean found = false;
                for (Map.Entry<K, V> entry : entrySet) {
                    if (entry.getKey().equals(k) && entry.getValue().equals(v)) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found);
            });
        } else {
            assertThrows(UnsupportedOperationException.class, map::entrySet);
        }
    }
}
