package com.soliscode.test.contract.collection;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionContractSupport;

import static com.soliscode.test.assertions.collection.CollectionAssertions.assertContainsNone;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `removeIf` method of a `Collection`**
///
/// This interface defines tests for the [removeIf][Collection#removeIf] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `removeIf` implementation correctly:
/// - Removes all elements from the collection that satisfy the given predicate.
/// - Returns `true` if any elements were removed.
/// - Handles empty collections correctly.
/// - Throws [NullPointerException] if the predicate is `null`.
/// - Throws [UnsupportedOperationException] if the method is not supported by the implementation.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionRemoveIfTest implements RemoveIfContract<String, MyCollection<String>> {
///     @Override
///     public CollectionProvider<String, MyCollection<String>> objectProvider() {
///         return MyCollection::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Collection] and [com.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#removeIf
/// @since 1.0.0
public interface RemoveIfContract<C extends Collection<E>, E> extends CollectionContractSupport<C, E> {

    /// Tests that the [removeIf][Collection#removeIf] method returns `false` when called on an empty collection.
    ///
    /// This test verifies that calling `removeIf` on an empty collection returns `false` and does not throw.
    ///
    /// @see Collection#removeIf
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("removeIf(Predicate) returns false for an empty collection")
    @Test
    default void removeIfWhenEmptyReturnsFalse() {
        Collection<E> collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.REMOVE_IF)) {
            boolean changed = collection.removeIf((e) -> true);
            assertFalse(changed);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.removeIf((e) -> true));
        }
    }

    /// Tests that the [removeIf][Collection#removeIf] method works for a collection with elements.
    ///
    /// This test verifies that:
    /// 1. Elements satisfying the predicate are removed from the collection.
    /// 2. The method returns `true` indicating the collection has changed.
    /// 3. The collection no longer contains the removed elements.
    ///
    /// @see Collection#removeIf
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("removeIf(Predicate) returns expected results for a collection with elements")
    @Test
    default void removeIfWhenNotEmptyReturnsExpectedResults() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);
        if (supportsMethod(CollectionMethods.REMOVE_IF)) {
            // Remove first element
            E first = values.getFirst();
            boolean changed = collection.removeIf((e) -> e.equals(first));
            assertTrue(changed);
            assertFalse(collection.contains(first));

            // Remove last element
            E last = values.getLast();
            changed = collection.removeIf((e) -> e.equals(last));
            assertTrue(changed);
            assertFalse(collection.contains(last));

            // Remove the remaining elements
            Collection<E> remaining = values.subList(1, values.size() - 1);
            changed = collection.removeIf(remaining::contains);
            assertTrue(changed);
            assertContainsNone(collection, values);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.removeIf((e) -> true));
        }
    }

    /// Tests that the [removeIf][Collection#removeIf] method throws [NullPointerException] when the filter is `null`.
    ///
    /// @see Collection#removeIf
    /// @throws NullPointerException if the filter is null
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @SuppressWarnings("DataFlowIssue")
    @DisplayName("removeIf(Predicate) throws NullPointerException when filter is null")
    @Test
    default void removeIfWithNullFilterThrowsNullPointerException() {
        if (supportsMethod(CollectionMethods.REMOVE_IF)) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            assertThrows(NullPointerException.class, () -> collection.removeIf(null));
        }
    }
}
