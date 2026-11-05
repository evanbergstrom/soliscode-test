package org.soliscode.test.contract.sortedmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `comparator` method of a `SortedMap`**
///
/// This interface defines tests for the [comparator()][SortedMap#comparator] method.
/// It is designed to be used as a mix-in interface by test classes that verify [SortedMap]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a sorted map's `comparator` implementation
/// correctly returns the comparator used to order the keys in this map, or `null` if it
/// uses the natural ordering of its keys.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MySortedMapComparatorTest implements ComparatorContract<MySortedMap<String, String>, String, String> {
///     @Override
///     public MapProvider<MySortedMap<String, String>, String, String> objectProvider() {
///         return MySortedMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [SortedMap] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <M> The map type being tested.
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @author evanbergstrom
/// @see SortedMap#comparator
/// @since 1.0.0
public interface ComparatorContract<M extends SortedMap<K, V>, K, V> extends MapContractSupport<M, K, V> {

    /// Tests that the [comparator][SortedMap#comparator] method can be called successfully.
    ///
    /// @see SortedMap#comparator
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("comparator() can be called successfully")
    default void comparator_whenCalled_isSuccessful() {
        SortedMap<K, V> map = provider().emptyInstance();
        if (supportsMethod(SortedMapMethods.COMPARATOR)) {
            map.comparator();
        } else {
            assertThrows(UnsupportedOperationException.class, map::comparator);
        }
    }
}
