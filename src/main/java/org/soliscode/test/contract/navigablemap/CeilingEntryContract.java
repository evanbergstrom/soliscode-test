package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `ceilingEntry` method of a `NavigableMap`**
///
/// This interface defines tests for the [ceilingEntry(K)][NavigableMap#ceilingEntry] method.
/// It is designed to be used as a mix-in interface by test classes that verify [NavigableMap]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a navigable map's `ceilingEntry` implementation
/// correctly returns a key-value mapping associated with the least key greater than or equal
/// to the given key, or `null` if there is no such key.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyNavigableMapCeilingEntryTest implements CeilingEntryContract<String, String, MyNavigableMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MyNavigableMap<String, String>> provider() {
///         return MyNavigableMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [NavigableMap] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see NavigableMap#ceilingEntry
/// @since 1.0.0
public interface CeilingEntryContract<K, V, M extends NavigableMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [ceilingEntry][NavigableMap#ceilingEntry] method can be called and
    /// returns expected results when elements are present.
    ///
    /// @see NavigableMap#ceilingEntry
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("ceilingEntry(K) returns expected results when elements are present")
    default void ceilingEntry_whenNotEmpty_returnsExpectedResults() {
        NavigableMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (map.isEmpty()) {
            return;
        }
        K key = map.firstKey();
        if (supportsMethod(NavigableMapMethods.CEILING_ENTRY)) {
            map.ceilingEntry(key);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> map.ceilingEntry(key));
        }
    }
}
