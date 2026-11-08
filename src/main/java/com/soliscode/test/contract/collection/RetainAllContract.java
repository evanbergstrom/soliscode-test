package com.soliscode.test.contract.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.assertions.Assertions;
import com.soliscode.test.contract.support.CollectionContractSupport;
import com.soliscode.test.util.MatchNothing;

import static com.soliscode.test.assertions.collection.CollectionAssertions.assertContainsAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `retainAll` method of a `Collection`**
///
/// This interface defines tests for the [retainAll][Collection#retainAll] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `retainAll` implementation correctly:
/// - Retains only the elements in the collection that are contained in the specified collection.
/// - Returns `true` if the collection changed as a result of the call.
/// - Handles incompatible types according to the collection's configuration.
/// - Throws [UnsupportedOperationException] if the method is not supported by the implementation.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionRetainAllTest implements RetainAllContract<String, MyCollection<String>> {
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
/// @see Collection#retainAll
/// @since 1.0.0
public interface RetainAllContract<C extends Collection<E>, E> extends CollectionContractSupport<C, E> {

    /// Tests that the [retainAll][Collection#retainAll] method works when called on an empty collection.
    ///
    /// This test verifies that:
    /// 1. Calling `retainAll()` on an empty collection returns `false`.
    /// 2. The collection remains empty.
    /// 3. If `retainAll` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Collection#retainAll
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("retainAll(Collection) works when called on an empty container")
    @Test
    default void retainAllWhenEmptyReturnsFalse() {
        Collection<E> collection = provider().emptyInstance();
        Collection<E> values = elementProvider().createUniqueInstances(2);
        if (supportsMethod(CollectionMethods.RETAIN_ALL)) {
            boolean changed = collection.retainAll(values);
            assertFalse(changed);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.retainAll(values));
        }
    }

    /// Tests that the [retainAll][Collection#retainAll] method works on a collection with elements.
    ///
    /// This test verifies that:
    /// 1. Elements not contained in the argument collection are removed from the target collection.
    /// 2. The method returns `true` indicating the collection has changed.
    /// 3. The collection only contains the retained elements after the call.
    ///
    /// @see Collection#retainAll
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("retainAll(Collection) works on a container with elements")
    @Test
    default void retainAllWhenNotEmptyReturnsExpectedResults() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);
        if (supportsMethod(CollectionMethods.RETAIN_ALL)) {
            // Retain only elements in the middle
            List<E> toRetain = values.subList(1, values.size() - 1);
            boolean changed = collection.retainAll(toRetain);
            assertTrue(changed);
            assertEquals(toRetain.size(), collection.size());
            assertContainsAll(collection, toRetain);
            assertFalse(collection.contains(values.getFirst()));
            assertFalse(collection.contains(values.getLast()));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.retainAll(values.subList(0, 1)));
        }
    }

    /// Tests that the [retainAll][Collection#retainAll] method works with incompatible types.
    ///
    /// This test verifies that if the argument collection contains incompatible types, they are not found
    /// in the target collection, so everything is removed.
    ///
    /// @see Collection#retainAll
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("retainAll(Collection) works with incompatible types")
    @Test
    default void retainAllWithIncompatibleTypeReturnsExpectedResults() {
        if (supportsMethod(CollectionMethods.RETAIN_ALL)) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            assertResultOrClassCast(true, () -> collection.retainAll(Collections.singleton(new MatchNothing())),
                    "retainAll(Collection) with an incompatible type");
            if (permitIncompatibleTypes()) {
                assertFalse(collection.iterator().hasNext(), "retainAll kept an element it should not");
            }
        }
    }

    /// Tests that the [retainAll][Collection#retainAll] method throws when the argument collection is `null`.
    ///
    /// ## Implementation Notes
    /// Implementations whose not-null annotation on the collection parameter is checked at run time may throw an
    /// `IllegalArgumentException` here, so either exception type is accepted.
    ///
    /// @see Collection#retainAll
    /// @throws NullPointerException or IllegalArgumentException if the argument collection is null
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("retainAll(Collection) throws exception when argument collection is null")
    @Test
    @SuppressWarnings("DataFlowIssue")
    default void retainAllWithNullCollectionThrowsException() {
        if (supportsMethod(CollectionMethods.RETAIN_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            Assertions.assertThrowsAnyOf(List.of(NullPointerException.class, IllegalArgumentException.class),
                    () -> collection.retainAll(null));
        }
    }
}
