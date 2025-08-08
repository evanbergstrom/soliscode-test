package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/// This interface tests if a collection class has implemented the `toArray` method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#toArray
/// @see Collection#toArray(Object[])
/// @since 1.0.0
public interface ToArrayContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the `toArray()` method works for an empty collection.
    @Test
    @DisplayName("The toArray method works on an empty container.")
    default void testToArrayOnEmptyCollection() {
        Collection<E> collection = provider().emptyInstance();
        int length = collection.toArray().length;
        assertEquals(0, length);
    }

    /// Tests that the `toArray()` method works for a collection with elements.
    @Test
    @DisplayName("The toArray method works on a container with elements")
    default void testToArray() {
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

    /// Tests that the `toArray()` method returns a safe array that can be modified without changing the
    /// elements of the container.
    @Test
    @DisplayName("The toArray method returns a safe array")
    default void testToArrayIsSafe() {
        Collection<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);

        E newValue = elementProvider().createInstance();
        Arrays.fill(collection.toArray(), newValue);

        for (E e : values) {
            assertTrue(collection.contains(e));
        }
    }

    /// Tests that the `toArray(T[])` method works for an empty collection.
    @Test
    @DisplayName("The toArray method works on an empty container.")
    @SuppressWarnings("unchecked")
    default void testToArrayStoreOnEmptyCollection() {
        Collection<E> collection = provider().emptyInstance();
        E[] store = (E[]) new Object[0];
        assertEquals(0, collection.toArray(store).length);
    }

    /// Tests that the `toArray(T[])` method works for a collection with elements.
    @Test
    @DisplayName("The toArray method works on a container with elements")
    @SuppressWarnings("unchecked")
    default void testToArrayStore() {
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

    /// Tests that the `toArray(T[])` method throws on a null array.
    /// # Implementation Notes
    /// This test checks that the `toArray(T[])` function throws the correct exception if it is called on a null
    /// value. IntelliJ will detect a problem when it is called with a null argument since the method declaration
    /// has a NotNull annotation. Since this is what we are trying to test, the inspection is suppressed here.
    ///
    /// Any implementations that use the `NotNull` annotation for the collection parameter may throw an
    /// `IllegalArgumentException` here, so either exception type is accepted.
    @Test
    @DisplayName("The toArray(T[]) method throws on a null array.")
    @SuppressWarnings("DataFlowIssue")
    default void testToArrayStoreThrowsOnNullArray() {
        Collection<E> collection = provider().createInstanceWithUniqueElements();
        Assertions.assertThrowsAny(List.of(NullPointerException.class, IllegalArgumentException.class),
                () -> collection.toArray((E[]) null));
    }
}
