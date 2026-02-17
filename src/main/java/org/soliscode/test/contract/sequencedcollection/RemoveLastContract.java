package org.soliscode.test.contract.sequencedcollection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for testing the [removeLast][SequencedCollection#removeLast] method of a [SequencedCollection].
///
/// ### Purpose
/// Verifies that `removeLast()` correctly removes and returns the last element of the collection,
/// handles unsupported operations, and throws the appropriate exception when the collection is empty.
///
/// ### Usage Example
/// This contract is typically used through [SequencedCollectionContract]:
/// ```java
/// class MySequencedCollectionTest extends SequencedCollectionContract<String, MySequencedCollection<String>> {
///     // Inherits all removeLast tests
/// }
/// ```
/// To exclude this contract if the method is not supported:
/// ```java
/// class MySequencedCollectionTest extends SequencedCollectionContract<String, MySequencedCollection<String>> {
///     public MySequencedCollectionTest() {
///         doesNotSupportMethod(SequencedCollectionMethods.REMOVE_LAST);
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
/// @see SequencedCollection#removeLast
/// @since 1.0.0
public interface RemoveLastContract<E, C extends SequencedCollection<E>> extends CollectionContractSupport<E, C> {

    /// Verifies that [removeLast][SequencedCollection#removeLast] removes and returns the last element when not empty.
    ///
    /// The test follows these steps:
    /// 1. Creates a collection with multiple elements.
    /// 2. Iteratively calls `removeLast()` and verifies that:
    ///    - The returned element is the one that was at the back.
    ///    - The collection no longer contains the removed element.
    /// 3. If `REMOVE_LAST` is not supported, verifies that `UnsupportedOperationException` is thrown.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see SequencedCollection#removeLast
    /// @since 1.0.0
    @DisplayName("removeLast() when not empty removes and returns the last element")
    @Test
    default void removeLast_whenNotEmpty_removesAndReturnsLastElement() {
        List<E> elements = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        if (supportsMethod(SequencedCollectionMethods.REMOVE_LAST)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            collection.addAll(elements);

            for (int i = elements.size() - 1; i >= 0; i--) {
                E removed = collection.removeLast();
                assertEquals(elements.get(i), removed);
                assertFalse(collection.contains(removed));
            }
        } else {
            SequencedCollection<E> collection = provider().createInstance(elements);
            assertThrows(UnsupportedOperationException.class, collection::removeLast);
        }
    }

    /// Verifies that [removeLast][SequencedCollection#removeLast] throws [NoSuchElementException] when the collection is empty.
    ///
    /// The test follows these steps:
    /// 1. Creates an empty collection.
    /// 2. Verifies that calling `removeLast()` throws a `NoSuchElementException`.
    /// 3. If `REMOVE_LAST` is not supported, verifies that either `UnsupportedOperationException` or `NoSuchElementException` is thrown.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @throws java.util.NoSuchElementException if the collection is empty (expected).
    /// @see SequencedCollection#removeLast
    /// @since 1.0.0
    @DisplayName("removeLast() when empty throws NoSuchElementException")
    @Test
    default void removeLast_whenEmpty_throwsNoSuchElementException() {
        if (supportsMethod(SequencedCollectionMethods.REMOVE_LAST)) {
            SequencedCollection<E> collection = provider().emptyInstance();
            assertThrows(NoSuchElementException.class, collection::removeLast);
        } else {
            SequencedCollection<E> collection = provider().emptyInstance();
            Assertions.assertThrowsAnyOf(List.of(UnsupportedOperationException.class, NoSuchElementException.class),
                    collection::removeLast);
        }
    }
}
