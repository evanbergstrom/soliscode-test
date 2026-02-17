package org.soliscode.test.contract.navigablemap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.MapContractSupport;

import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `descendingKeySet` method of a `NavigableMap`**
///
/// This interface defines tests for the [descendingKeySet()][NavigableMap#descendingKeySet] method.
/// It is designed to be used as a mix-in interface by test classes that verify [NavigableMap]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a navigable map's `descendingKeySet` implementation
/// correctly returns a reverse order [java.util.NavigableSet] view of the keys contained in this map.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyNavigableMapDescendingKeySetTest implements DescendingKeySetContract<String, String, MyNavigableMap<String, String>> {
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
/// @see NavigableMap#descendingKeySet
/// @since 1.0.0
public interface DescendingKeySetContract<K, V, M extends NavigableMap<K, V>> extends MapContractSupport<K, V, M> {

    /// Tests that the [descendingKeySet][NavigableMap#descendingKeySet] method can be called and
    /// returns a non-null view.
    ///
    /// @see NavigableMap#descendingKeySet
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Test
    @DisplayName("descendingKeySet() returns a non-null view")
    default void descendingKeySet_whenCalled_returnsNonNullView() {
        NavigableMap<K, V> map = provider().createInstance(DEFAULT_SIZE);
        if (supportsMethod(NavigableMapMethods.DESCENDING_KEY_SET)) {
            assertNotNull(map.descendingKeySet());
        } else {
            assertThrows(UnsupportedOperationException.class, map::descendingKeySet);
        }
    }
}
