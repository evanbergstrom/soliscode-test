package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.MatchNothing;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// This interface defines tests for the [add][Collection#add] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `add` implementation correctly:
/// - Adds a single element to the collection.
/// - Returns `true` if the collection changed as a result of the call.
/// - Ensures the collection contains the added element.
/// - Updates the collection size appropriately.
/// - Handles null values according to the collection's configuration (permitting or rejecting nulls).
/// - Handles duplicate values according to the collection's configuration (permitting or ignoring duplicates).
/// - Throws [UnsupportedOperationException] if the method is not supported by the implementation.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionAddTest implements AddContract<String, MyCollection<String>> {
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
/// @see Collection#add
/// @since 1.0.0
public interface AddContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [add][Collection#add] method successfully adds an element to the collection.
    ///
    /// This test verifies that:
    /// 1. A single element is added to the collection.
    /// 2. The method returns `true` indicating the collection has changed.
    /// 3. The collection contains the added element after the call.
    /// 4. The size of the collection increases by 1.
    /// 5. If `add` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Collection#add
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("add(E) adds a single element and updates size")
    @Test
    default void add_singleElement_returnsTrueAndUpdatesSize() {
        if (supportsMethod(CollectionMethods.ADD)) {
            Collection<E> collection = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                assertTrue(collection.add(values.get(i)));
                assertTrue(collection.contains(values.get(i)));
                assertEquals(i + 1, collection.size());
            }
        }
    }

    /// Tests that the [add][Collection#add] method handles `null` values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitNulls()]:
    /// - If `null` is permitted: Adding `null` should succeed, and the collection should contain `null`.
    /// - If `null` is not permitted: Adding `null` should throw [NullPointerException].
    ///
    /// @see Collection#add
    /// @throws NullPointerException if null is not permitted and the argument is null
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("add(E) handles null values based on permission")
    @Test
    default void add_withNullValue_handlesCorrectly() {
        if (supportsMethod(CollectionMethods.ADD)) {
            Collection<E> collection = provider().emptyInstance();
            if (permitNulls()) {
                assertTrue(collection.add(null));
                assertTrue(collection.contains(null));
                assertEquals(1, collection.size());
            }
        }
    }

    /// Tests that the [add][Collection#add] method handles duplicate values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitDuplicates()]:
    /// - If duplicates are permitted: Adding an existing element should increase the collection size and return `true`.
    /// - If duplicates are not permitted: Adding an existing element should not increase the collection size,
    ///   and the method should return `false`.
    ///
    /// @see Collection#add
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("add(E) handles duplicate values based on permission")
    @Test
    default void add_withDuplicateValue_handlesCorrectly() {
        if (supportsMethod(CollectionMethods.ADD)) {
            if (permitDuplicates()) {
                List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
                Collection<E> collection = provider().createInstance(values);
                for (int i = 0; i < values.size(); i++) {
                    assertTrue(collection.add(values.get(i)));
                    assertTrue(collection.contains(values.get(i)));
                    assertEquals(values.size() + i + 1, collection.size());
                }
            } else {
                Collection<E> collection = provider().emptyInstance();
                E value = elementProvider().createInstance();
                assertTrue(collection.add(value));
                E otherValue = elementProvider().copyInstance(value);
                assertFalse(collection.add(otherValue));
                assertEquals(1, collection.size());
            }
        }
    }

    /// Tests that the [add][Collection#add] method handles incompatible element types correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitIncompatibleTypes()]:
    /// - If incompatible types are not permitted: Adding an incompatible type should throw [ClassCastException].
    /// - If incompatible types are permitted: The behavior is implementation-dependent, but usually it should either
    ///   work or throw [ClassCastException] depending on how strict the implementation is.
    ///
    /// @see Collection#add
    /// @throws ClassCastException if incompatible types are not permitted and the argument is incompatible
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("add(E) handles incompatible types based on permission")
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    default void add_withIncompatibleType_throwsClassCastException() {
        if (supportsMethod(CollectionMethods.ADD) && !permitIncompatibleTypes()) {
            Collection<E> collection = provider().emptyInstance();
            assertThrows(ClassCastException.class, () -> ((Collection) collection).add(new MatchNothing()));
        }
    }


    /**
     * Tests that the {@link Collection#add(Object)} method throws an {@link UnsupportedOperationException}
     * when the operation is not supported by the implementation.
     *
     * <p>This test verifies the behavior of the {@code add} operation for collections that do not support
     * element addition. If the {@code add} method is unsupported as indicated by the {@link #supportsMethod}
     * method returning {@code false}, the test ensures that an {@link UnsupportedOperationException} is
     * thrown when {@code add} is invoked with a valid element.</p>
     *
     * @see Collection#add(Object)
     * @throws org.opentest4j.AssertionFailedError if the expected exception is not thrown
     */
    @DisplayName("add(E) throws UnsupportedOperationException when method is not supported")
    @Test
    default void add_whenNotSupported_throwsUnsupportedOperationException() {
        if (!supportsMethod(CollectionMethods.ADD)) {
            assertThrows(UnsupportedOperationException.class,
                    () -> provider().emptyInstance().add(elementProvider().createInstance()));
        }
    }
}
