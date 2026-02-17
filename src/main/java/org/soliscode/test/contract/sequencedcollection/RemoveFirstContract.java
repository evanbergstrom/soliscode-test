package org.soliscode.test.contract.sequencedcollection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for testing the [removeFirst][SequencedCollection#removeFirst] method of a [SequencedCollection].
///
/// ### Purpose
/// Verifies that `removeFirst()` correctly removes and returns the first element of the collection,
/// handles unsupported operations, and throws the appropriate exception when the collection is empty.
///
/// ### Usage Example
/// This contract is typically used through [SequencedCollectionContract]:
/// ```java
/// class MySequencedCollectionTest extends SequencedCollectionContract<String, MySequencedCollection<String>> {
///     // Inherits all removeFirst tests
/// }
/// ```
/// To exclude this contract if the method is not supported:
/// ```java
/// class MySequencedCollectionTest extends SequencedCollectionContract<String, MySequencedCollection<String>> {
///     public MySequencedCollectionTest() {
///         doesNotSupportMethod(SequencedCollectionMethods.REMOVE_FIRST);
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
/// @see SequencedCollection#removeFirst
/// @since 1.0.0
public interface RemoveFirstContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Verifies that [removeFirst][SequencedCollection#removeFirst] removes and returns the first element when not empty.
    ///
    /// The test follows these steps:
    /// 1. Creates a collection with multiple elements.
    /// 2. Iteratively calls `removeFirst()` and verifies that:
    ///    - The returned element is the one that was at the front.
    ///    - The collection no longer contains the removed element.
    /// 3. If `REMOVE_FIRST` is not supported, verifies that `UnsupportedOperationException` is thrown.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#removeFirst
    /// @since 1.0.0
    @DisplayName("removeFirst() when not empty removes and returns the first element")
    @Test
    default void removeFirst_whenNotEmpty_removesAndReturnsFirstElement() {
        List<E> elements = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        if (supportsMethod(SequencedCollectionMethods.REMOVE_FIRST)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            collection.addAll(elements);
            for (E element : elements) {
                E removed = collection.removeFirst();
                assertEquals(element, removed);
                assertFalse(collection.contains(removed));
            }
        } else {
            SequencedCollection<E> collection = provider().createInstance(elements);
            assertThrows(UnsupportedOperationException.class, collection::removeFirst);
        }
    }

    /// Verifies that [removeFirst][SequencedCollection#removeFirst] throws [NoSuchElementException] when the collection is empty.
    ///
    /// The test follows these steps:
    /// 1. Creates an empty collection.
    /// 2. Verifies that calling `removeFirst()` throws a `NoSuchElementException`.
    /// 3. If `REMOVE_FIRST` is not supported, verifies that either `UnsupportedOperationException` or `NoSuchElementException` is thrown.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @throws java.util.NoSuchElementException if the collection is empty (expected).
    /// @see SequencedCollection#removeFirst
    /// @since 1.0.0
    @DisplayName("removeFirst() when empty throws NoSuchElementException")
    @Test
    default void removeFirst_whenEmpty_throwsNoSuchElementException() {
        if (supportsMethod(SequencedCollectionMethods.REMOVE_FIRST)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            assertThrows(NoSuchElementException.class, collection::removeFirst);
        } else {
            SequencedCollection<E> collection = provider().emptyInstance();
            Assertions.assertThrowsAnyOf(List.of(UnsupportedOperationException.class, NoSuchElementException.class),
                    collection::removeFirst);
        }
    }
}
