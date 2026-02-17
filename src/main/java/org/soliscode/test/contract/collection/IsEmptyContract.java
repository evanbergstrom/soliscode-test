package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `isEmpty` method of a `Collection`**
///
/// This interface defines tests for the [isEmpty][Collection#isEmpty] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `isEmpty` implementation correctly:
/// - Returns `true` if the collection contains no elements.
/// - Returns `false` if the collection contains at least one element.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionIsEmptyTest implements IsEmptyContract<String, MyCollection<String>> {
///     @Override
///     public CollectionProvider<String, MyCollection<String>> provider() {
///         return MyCollection::new;
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
/// @see Collection#isEmpty
/// @since 1.0.0
public interface IsEmptyContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [isEmpty][Collection#isEmpty] method returns `true` for an empty collection.
    ///
    /// This test verifies that calling `isEmpty()` on an empty collection returns `true`.
    ///
    /// @see Collection#isEmpty
    /// @throws org.opentest4j.AssertionFailedError if the assertion fails
    /// @since 1.0.0
    @DisplayName("isEmpty() returns true for an empty collection")
    @Test
    default void isEmpty_whenEmpty_returnsTrue() {
        Collection<E> collection = provider().emptyInstance();
        assertTrue(collection.isEmpty());
    }

    /// Tests that the [isEmpty][Collection#isEmpty] method returns `false` for a collection with
    /// at least one element.
    ///
    /// This test verifies that calling `isEmpty()` on a non-empty collection returns `false`.
    ///
    /// @see Collection#isEmpty
    /// @throws org.opentest4j.AssertionFailedError if the assertion fails
    /// @since 1.0.0
    @DisplayName("isEmpty() returns false for a collection with elements")
    @Test
    default void isEmpty_whenNotEmpty_returnsFalse() {
        Collection<E> collection = provider().createSingleton();
        assertFalse(collection.isEmpty());
    }
}
