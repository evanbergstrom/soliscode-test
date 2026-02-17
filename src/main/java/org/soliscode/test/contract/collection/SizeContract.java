package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// **Contract for the `size` method of a `Collection`**
///
/// This contract provides tests to ensure that the `size()` method of a `Collection`
/// implementation correctly reports the number of elements it contains.
/// It covers both empty collections and collections with multiple elements.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `size` implementation correctly:
/// - Returns 0 if the collection contains no elements.
/// - Returns the correct count of elements if the collection is not empty.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the necessary
/// provider methods:
///
/// ```java
/// public class MyCollectionSizeTest implements SizeContract<String, MyCollection<String>> {
///     @Override
///     public CollectionProvider<String, MyCollection<String>> provider() {
///         return MyCollection::new;
///     }
///
///     @Override
///     public ObjectProvider<String> elementProvider() {
///         return new StringProvider();
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Collection] and [org.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#size()
/// @since 1.0.0
public interface SizeContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [size()][Collection#size] method works for an empty collection.
    ///
    /// This test verifies that calling `size()` on an empty collection returns 0.
    ///
    /// @see Collection#size()
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("size() returns 0 for an empty collection")
    @Test
    default void size_whenEmpty_returnsZero() {
        Collection<E> collection = provider().emptyInstance();
        assertEquals(0, collection.size());
    }

    /// Tests that the [size()][Collection#size] method works for a collection with elements.
    ///
    /// This test verifies that calling `size()` on collections of various sizes returns the correct count.
    ///
    /// @see Collection#size()
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
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
