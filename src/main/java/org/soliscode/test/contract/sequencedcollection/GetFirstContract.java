package org.soliscode.test.contract.sequencedcollection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for testing the [getFirst][SequencedCollection#getFirst] method of a [SequencedCollection].
///
/// ### Purpose
/// Verifies that `getFirst()` correctly returns the first element of the collection and
/// throws the appropriate exception when the collection is empty.
///
/// ### Usage Example
/// This contract is typically used through [SequencedCollectionContract]:
/// ```java
/// class MySequencedCollectionTest extends SequencedCollectionContract<String, MySequencedCollection<String>> {
///     // Inherits all getFirst tests
/// }
/// ```
/// To exclude this contract if the method is not supported:
/// ```java
/// class MySequencedCollectionTest extends SequencedCollectionContract<String, MySequencedCollection<String>> {
///     public MySequencedCollectionTest() {
///         doesNotSupportMethod(SequencedCollectionMethods.GET_FIRST);
///     }
/// }
/// ```
///
/// ### Thread Safety
/// The tests in this contract are not thread-safe and should be run in a single-threaded environment
/// unless the underlying collection implementation specifically guarantees thread safety for these operations.
///
/// @param <E> The element type.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see SequencedCollection#getFirst
/// @since 1.0.0
public interface GetFirstContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Verifies that [getFirst][SequencedCollection#getFirst] returns the first element when the collection is not empty.
    ///
    /// The test follows these steps:
    /// 1. Creates a collection with multiple elements.
    /// 2. Verifies that `getFirst()` returns the element at the front of the collection.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#getFirst
    /// @since 1.0.0
    @DisplayName("getFirst() when not empty returns the first element")
    @Test
    default void getFirst_whenNotEmpty_returnsFirstElement() {
        if (supportsMethod(SequencedCollectionMethods.GET_FIRST)) {
            List<E> elements = elementProvider().createUniqueInstances(2);
            SequencedCollection<E> collection = provider().createInstance(elements);
            assertEquals(elements.getFirst(), collection.getFirst());
        }
    }

    /// Verifies that [getFirst][SequencedCollection#getFirst] throws [NoSuchElementException] when the collection is empty.
    ///
    /// The test follows these steps:
    /// 1. Creates an empty collection.
    /// 2. Verifies that calling `getFirst()` throws a `NoSuchElementException`.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @throws java.util.NoSuchElementException if the collection is empty (expected).
    /// @see SequencedCollection#getFirst
    /// @since 1.0.0
    @DisplayName("getFirst() when empty throws NoSuchElementException")
    @Test
    default void getFirst_whenEmpty_throwsNoSuchElementException() {
        if (supportsMethod(SequencedCollectionMethods.GET_FIRST)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            assertThrows(NoSuchElementException.class, collection::getFirst);
        }
    }
}
