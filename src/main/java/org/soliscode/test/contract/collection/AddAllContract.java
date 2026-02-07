package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.MatchNothing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.unmodifiableCollection;
import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsAll;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSameByIdentity;

/// **Contract for the `addAll` method of a `Collection`**
///
/// This interface defines tests for the [addAll][Collection#addAll] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `addAll` implementation correctly:
/// - Adds all elements from the specified collection to the target collection.
/// - Returns `true` if the collection changed as a result of the call.
/// - Handles null values according to the collection's configuration (permitting or rejecting nulls).
/// - Handles duplicate values according to the collection's configuration (permitting or ignoring duplicates).
/// - Throws [UnsupportedOperationException] if the method is not supported by the implementation.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionAddAllTest implements AddAllContract<String, MyCollection<String>> {
///     @Override
///     public CollectionProvider<String, MyCollection<String>> provider() {
///         return MyCollection::new;
///     }
///
///     @Override
///     public ObjectProvider<String> elementProvider() {
///         return () -> Stream.of("a", "b", "c");
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
/// @see Collection#addAll
/// @since 1.0.0
public interface AddAllContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {


    /// Tests that the [addAll][Collection#addAll] method successfully adds elements to an empty collection.
    ///
    /// This test verifies that:
    /// 1. Elements from a source collection are added to the target collection.
    /// 2. The method returns `true` indicating the collection has changed.
    /// 3. If `addAll` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    @DisplayName("addAll(Collection) adds all argument elements to an empty collection")
    @Test
    default void addAll_whenContainerIsEmpty_addsAllArgumentElements() {
        if (supportsMethod(CollectionMethods.ADD_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            Collection<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);

            assertTrue(collection.addAll(unmodifiableCollection(values)));
            assertContainsSameByIdentity(values, collection);
        } else {
            Collection<E> collection = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> collection.addAll(Collections.emptyList()));
        }
    }

    /// Tests that the [addAll][Collection#addAll] method returns `false` when adding an empty collection.
    ///
    /// This test verifies that:
    /// 1. Adding an empty collection to the target collection returns `false`.
    /// 2. The target collection remains unchanged.
    @DisplayName("addAll(Collection) returns false when adding an empty collection")
    @Test
    default void addAll_withEmptyCollection_returnsFalse() {
        if (supportsMethod(CollectionMethods.ADD_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            assertFalse(collection.addAll(Collections.emptyList()));
            assertTrue(collection.isEmpty());
        }
    }

    /// Tests that the [addAll][Collection#addAll] method throws [NullPointerException] when the argument is `null`.
    @DisplayName("addAll(Collection) throws NullPointerException when the argument is null")
    @Test
    default void addAll_withNullCollection_throwsNullPointerException() {
        if (supportsMethod(CollectionMethods.ADD_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            //noinspection DataFlowIssue
            assertThrows(NullPointerException.class, () -> collection.addAll(null));
        }
    }

    /// Tests that the [addAll][Collection#addAll] method handles `null` values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitNulls()]:
    /// - If `null` is permitted: Adding a collection containing `null` should succeed.
    /// - If `null` is not permitted: Adding a collection containing `null` should throw [NullPointerException].
    @DisplayName("addAll(Collection) handles null values based on permission")
    @Test
    default void addAll_withNullValue_handlesCorrectly() {
        if (supportsMethod(CollectionMethods.ADD_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            Collection<E> values = Collections.singletonList(null);
            if (permitNulls()) {
                assertTrue(collection.addAll(values));
                assertTrue(collection.contains(null));
                assertEquals(1, collection.size());
            } else {
                assertThrows(NullPointerException.class, () -> collection.addAll(values));
            }
        }
    }

    /// Tests that the [addAll][Collection#addAll] method handles duplicate values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitDuplicates()]:
    /// - If duplicates are permitted: Adding existing elements should increase the collection size.
    /// - If duplicates are not permitted: Adding existing elements should not increase the collection size,
    ///   and the method should return `false` if no elements were added.
    @DisplayName("addAll(Collection) handles duplicate values based on permission")
    @Test
    @SuppressWarnings("RedundantCollectionOperation")
    default void addAll_withDuplicateValue_handlesCorrectly() {
        if (supportsMethod(CollectionMethods.ADD_ALL)) {
            if (permitDuplicates()) {
                List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
                Collection<E> collection = provider().createInstance(values);
                assertTrue(collection.addAll(unmodifiableCollection(values)));
                assertEquals(values.size() * 2, collection.size());
                assertContainsAll(values, collection);
            } else {
                Collection<E> collection = provider().emptyInstance();
                E value = elementProvider().createInstance();
                collection.add(value);
                E otherValue = elementProvider().copyInstance(value);
                assertFalse(collection.addAll(Collections.singleton(otherValue)));
                assertEquals(1, collection.size());
            }
        }
    }

    /// Tests that the [addAll][Collection#addAll] method handles incompatible element types correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitIncompatibleTypes()]:
    /// - If incompatible types are not permitted: Adding a collection with incompatible types should throw [ClassCastException].
    /// - If incompatible types are permitted: The behavior is implementation-dependent, but usually it should either
    ///   work or throw [ClassCastException] depending on how strict the implementation is.
    @DisplayName("addAll(Collection) handles incompatible types based on permission")
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    default void addAll_withIncompatibleType_throwsClassCastException() {
        if (supportsMethod(CollectionMethods.ADD_ALL) && !permitIncompatibleTypes()) {
            Collection<E> collection = provider().emptyInstance();
            Collection incompatibleValues = Collections.singletonList(new MatchNothing());
            assertThrows(ClassCastException.class, () -> collection.addAll(incompatibleValues));
        }
    }
}
