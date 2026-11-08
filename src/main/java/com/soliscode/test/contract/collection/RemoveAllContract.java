package com.soliscode.test.contract.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionContractSupport;
import com.soliscode.test.util.MatchNothing;

import static com.soliscode.test.assertions.collection.CollectionAssertions.assertContainsNone;
import static com.soliscode.test.assertions.collection.CollectionAssertions.assertDoesNotContain;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `removeAll` method of a `Collection`**
///
/// This interface defines tests for the [removeAll][Collection#removeAll] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `removeAll` implementation correctly:
/// - Removes all elements from the collection that are also contained in the specified collection.
/// - Returns `true` if the collection changed as a result of the call.
/// - Handles `null` elements according to the collection's configuration.
/// - Handles incompatible types according to the collection's configuration.
/// - Throws [UnsupportedOperationException] if the method is not supported by the implementation.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionRemoveAllTest implements RemoveAllContract<String, MyCollection<String>> {
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
/// @see Collection#removeAll
/// @since 1.0.0
public interface RemoveAllContract<C extends Collection<E>, E> extends CollectionContractSupport<C, E> {

    /// Tests that the [removeAll][Collection#removeAll] method returns `false` when called on an empty collection.
    ///
    /// This test verifies that calling `removeAll` on an empty collection returns `false` and does not throw.
    ///
    /// @see Collection#removeAll
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("removeAll(Collection) returns false for an empty collection")
    @Test
    default void removeAllWhenEmptyReturnsFalse() {
        if (supportsMethod(CollectionMethods.REMOVE_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            boolean changed = collection.removeAll(values);
            assertFalse(changed);
        }
    }

    /// Tests that the [removeAll][Collection#removeAll] method works for a collection with elements.
    ///
    /// This test verifies that:
    /// 1. Elements contained in the argument collection are removed from the target collection.
    /// 2. The method returns `true` if any elements were removed.
    /// 3. The collection no longer contains any of the removed elements.
    ///
    /// @see Collection#removeAll
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("removeAll(Collection) removes argument elements from a non-empty collection")
    @Test
    default void removeAllWhenNotEmptyRemovesElements() {
        if (supportsMethod(CollectionMethods.REMOVE_ALL)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Collection<E> collection = provider().createInstance(values);

            // Remove first element
            E first = values.getFirst();
            boolean changed = collection.removeAll(Collections.singleton(first));
            assertTrue(changed);
            assertDoesNotContain(first, collection);

            // Remove last element
            E last = values.getLast();
            changed = collection.removeAll(Collections.singleton(last));
            assertTrue(changed);
            assertFalse(collection.contains(last));

            // Remove the remaining elements
            changed = collection.removeAll(values.subList(1, values.size() - 1));
            assertTrue(changed);
            assertContainsNone(values, collection);
        }
    }

    /// Tests that the [removeAll][Collection#removeAll] method handles `null` values correctly.
    ///
    /// This test verifies behavior when trying to remove `null` from a collection.
    ///
    /// @see Collection#removeAll
    /// @throws NullPointerException if nulls are not permitted and the argument contains a null element
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("removeAll(Collection) handles null elements based on permission")
    @Test
    default void removeAllWithNullElementHandlesCorrectly() {
        if (supportsMethod(CollectionMethods.REMOVE_ALL)) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            Collection<E> argument = Collections.singleton(null);
            if (permitNulls()) {
                assertFalse(collection.removeAll(argument));
            } else {
                assertThrows(NullPointerException.class, () -> collection.removeAll(argument));
            }
        }
    }

    /// Tests that the [removeAll][Collection#removeAll] method handles incompatible types correctly.
    ///
    /// This test verifies that `removeAll` returns `false` when called with incompatible types.
    ///
    /// @see Collection#removeAll
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("removeAll(Collection) returns false for incompatible types")
    @Test
    default void removeAllWithIncompatibleTypeReturnsFalse() {
        if (supportsMethod(CollectionMethods.REMOVE_ALL)) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            assertResultOrClassCast(false, () -> collection.removeAll(Collections.singleton(new MatchNothing())),
                    "removeAll(Collection) with an incompatible type");
        }
    }

    /// Tests that the [removeAll][Collection#removeAll] method throws [UnsupportedOperationException] when not
    /// supported.
    ///
    /// @see Collection#removeAll
    /// @throws UnsupportedOperationException if the method is not supported
    /// @since 1.0.0
    @DisplayName("removeAll(Collection) throws UnsupportedOperationException when not supported")
    @Test
    default void removeAllWhenNotSupportedThrowsUnsupportedOperationException() {
        if (!supportsMethod(CollectionMethods.REMOVE_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> collection.removeAll(Collections.emptyList()));
        }
    }

    /// Tests that the [removeAll][Collection#removeAll] method throws when the argument collection is `null`.
    ///
    /// @see Collection#removeAll
    /// @throws NullPointerException or IllegalArgumentException if the argument collection is null
    /// @since 1.0.0
    @SuppressWarnings("DataFlowIssue")
    @DisplayName("removeAll(Collection) throws exception when argument collection is null")
    @Test
    default void removeAllWithNullCollectionThrowsException() {
        if (supportsMethod(CollectionMethods.REMOVE_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            assertThrows(Throwable.class, () -> collection.removeAll(null));
        }
    }
}
