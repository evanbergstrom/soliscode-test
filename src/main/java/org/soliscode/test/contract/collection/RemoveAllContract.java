package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.MatchNothing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsNone;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertDoesNotContain;

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
/// @see Collection#removeAll
/// @since 1.0
public interface RemoveAllContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [removeAll][Collection#removeAll] method returns `false` when called on an empty collection.
    ///
    /// @see Collection#removeAll
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("removeAll(Collection) returns false for an empty collection")
    @Test
    default void removeAll_whenEmpty_returnsFalse() {
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
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("removeAll(Collection) removes argument elements from a non-empty collection")
    @Test
    default void removeAll_whenNotEmpty_removesArgumentElements() {
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
    /// @see Collection#removeAll
    /// @throws NullPointerException if nulls are not permitted and the argument contains a null element
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("removeAll(Collection) handles null elements based on permission")
    @Test
    default void removeAll_withNullElement_handlesCorrectly() {
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
    /// @see Collection#removeAll
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("removeAll(Collection) returns false for incompatible types")
    @Test
    default void removeAll_withIncompatibleType_returnsFalse() {
        if (supportsMethod(CollectionMethods.REMOVE_ALL)) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            assertFalse(collection.removeAll(Collections.singleton(new MatchNothing())));
        }
    }

    /// Tests that the [removeAll][Collection#removeAll] method throws [UnsupportedOperationException] when not supported.
    ///
    /// @see Collection#removeAll
    /// @throws UnsupportedOperationException if the method is not supported
    @DisplayName("removeAll(Collection) throws UnsupportedOperationException when not supported")
    @Test
    default void removeAll_whenNotSupported_throwsUnsupportedOperationException() {
        if (!supportsMethod(CollectionMethods.REMOVE_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> collection.removeAll(Collections.emptyList()));
        }
    }

    /// Tests that the [removeAll][Collection#removeAll] method throws when the argument collection is `null`.
    ///
    /// @see Collection#removeAll
    /// @throws NullPointerException or IllegalArgumentException if the argument collection is null
    @SuppressWarnings({"DataFlowIssue", "ThrowableNotThrown"})
    @DisplayName("removeAll(Collection) throws exception when argument collection is null")
    @Test
    default void removeAll_withNullCollection_throwsException() {
        if (supportsMethod(CollectionMethods.REMOVE_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            assertThrows(Throwable.class, () -> collection.removeAll(null));
        }
    }
}
