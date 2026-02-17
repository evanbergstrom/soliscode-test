package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `toArray` methods of a `Collection`**
///
/// This interface defines tests for the [toArray][Collection#toArray] and
/// [toArray(T[])][Collection#toArray(Object[])] methods. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `toArray` implementations correctly:
/// - Return an array containing all elements of the collection.
/// - Maintain the same size as the collection.
/// - Provide a safe array that does not modify the underlying collection when changed.
/// - Handle empty collections correctly.
/// - Throws appropriate exceptions for invalid inputs (e.g., null arrays).
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionToArrayTest implements ToArrayContract<String, MyCollection<String>> {
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
/// @see Collection#toArray
/// @see Collection#toArray(Object[])
/// @since 1.0.0
public interface ToArrayContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [toArray()][Collection#toArray] method works for an empty collection.
    ///
    /// This test verifies that:
    /// 1. Calling `toArray()` on an empty collection returns an empty array.
    /// 2. The returned array has a length of 0.
    ///
    /// @see Collection#toArray
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("toArray() returns an empty array for an empty collection")
    @Test
    default void toArray_whenEmpty_returnsEmptyArray() {
        Collection<E> collection = provider().emptyInstance();
        int length = collection.toArray().length;
        assertEquals(0, length);
    }

    /// Tests that the [toArray()][Collection#toArray] method works for a collection with elements.
    ///
    /// This test verifies that:
    /// 1. The returned array has the same size as the collection.
    /// 2. All elements in the array are present in the collection.
    /// 3. All elements from the collection are present in the array.
    ///
    /// @see Collection#toArray
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("toArray() returns an array containing all elements")
    @Test
    default void toArray_whenNotEmpty_returnsArrayWithElements() {
        Collection<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);

        Object[] array = collection.toArray();
        assertEquals(collection.size(), array.length);

        ArrayList<E> elements = new ArrayList<>(collection);
        for (Object obj : array) {
            assertTrue(collection.contains(obj));
            elements.remove(obj);
        }
        assertTrue(elements.isEmpty());
    }

    /// Tests that the [toArray()][Collection#toArray] method returns a safe array that can be
    /// modified without changing the elements of the container.
    ///
    /// This test verifies that modifying the returned array does not affect the collection.
    ///
    /// @see Collection#toArray
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("toArray() returns a safe array")
    @Test
    default void toArray_whenCalled_returnsSafeArray() {
        Collection<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);

        E newValue = elementProvider().createInstance();
        Arrays.fill(collection.toArray(), newValue);

        for (E e : values) {
            assertTrue(collection.contains(e));
        }
    }

    /// Tests that the [toArray(T[])][Collection#toArray(Object[])] method works for an empty collection.
    ///
    /// This test verifies that calling `toArray(T[])` with an empty array on an empty collection
    /// returns an empty array.
    ///
    /// @see Collection#toArray(Object[])
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("toArray(T[]) returns an empty array for an empty collection")
    @Test
    @SuppressWarnings("unchecked")
    default void toArrayStore_whenEmpty_returnsEmptyArray() {
        Collection<E> collection = provider().emptyInstance();
        E[] store = (E[]) new Object[0];
        assertEquals(0, collection.toArray(store).length);
    }

    /// Tests that the [toArray(T[])][Collection#toArray(Object[])] method works for a collection with elements.
    ///
    /// This test verifies that:
    /// 1. The returned array has the same size as the collection.
    /// 2. All elements in the array are present in the collection.
    /// 3. All elements from the collection are present in the array.
    ///
    /// @see Collection#toArray(Object[])
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("toArray(T[]) returns an array containing all elements")
    @Test
    @SuppressWarnings("unchecked")
    default void toArrayStore_whenNotEmpty_returnsArrayWithElements() {
        Collection<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);

        E[] store = (E[]) new Object[collection.size()];
        E[] array = collection.toArray(store);
        assertEquals(collection.size(), array.length);

        ArrayList<E> elements = new ArrayList<>(collection);
        for (E e : array) {
            assertTrue(collection.contains(e));
            elements.remove(e);
        }
        assertTrue(elements.isEmpty());
    }

    /// Tests that the [toArray(T[])][Collection#toArray(Object[])] method throws on a null array.
    ///
    /// ## Implementation Notes
    /// This test checks that the `toArray(T[])` function throws the correct exception if it is called on a null
    /// value. IntelliJ will detect a problem when it is called with a null argument since the method declaration
    /// has a NonNull annotation. Since this is what we are trying to test, the inspection is suppressed here.
    ///
    /// Any implementations that use the `NonNull` annotation for the collection parameter may throw an
    /// `IllegalArgumentException` here, so either exception type is accepted.
    ///
    /// @see Collection#toArray(Object[])
    /// @throws NullPointerException or IllegalArgumentException if the argument array is null
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("toArray(T[]) throws exception when the argument array is null")
    @Test
    @SuppressWarnings("DataFlowIssue")
    default void toArrayStore_withNullArray_throwsException() {
        Collection<E> collection = provider().createInstanceWithUniqueElements();
        Assertions.assertThrowsAnyOf(List.of(NullPointerException.class, IllegalArgumentException.class),
                () -> collection.toArray((E[]) null));
    }
}
