package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [Collection#size()] method.
///
/// This contract provides tests to ensure that the `size()` method of a `Collection`
/// implementation correctly reports the number of elements it contains.
/// It covers both empty collections and collections with multiple elements.
///
/// ## Usage Example
///
/// To use this contract, implement it in your test class along with the necessary
/// provider methods:
///
/// ```java
/// public class MyCollectionSizeTest implements SizeContract<String, MyCollection<String>> {
///     @Override
///     public CollectionProvider<String, MyCollection<String>> provider() {
///         return new MyCollectionProvider();
///     }
///
///     @Override
///     public ElementProvider<String> elementProvider() {
///         return new StringProvider();
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// Implementations of this contract are expected to be thread-safe for use by
/// the JUnit test runner. The tested `Collection` instance itself should be
/// handled according to its own thread safety guarantees.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#size()
/// @since 1.0
public interface SizeContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [size()][Collection#size] method works for an empty collection.
    ///
    /// @see Collection#size()
    /// @throws UnsupportedOperationException if the `size()` method is not supported.
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("size() returns 0 for an empty collection")
    @Test
    default void size_whenEmpty_returnsZero() {
        Collection<E> collection = provider().emptyInstance();
        assertEquals(0, collection.size());
    }

    /// Tests that the [size()][Collection#size] method works for a collection with elements.
    ///
    /// @see Collection#size()
    /// @throws UnsupportedOperationException if the `size()` method is not supported.
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("size() returns the correct number of elements for a collection with elements")
    @Test
    default void size_whenNotEmpty_returnsCorrectSize() {
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            List<E> values = elementProvider().createUniqueInstances(i);
            Collection<E> collection = provider().createInstance(values);
            assertEquals(values.size(), collection.size());
        }
    }
}
