package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// This interface tests if a list class has implemented the [set][List#set] method correctly.
///
/// @param <E> The element type for the list being tested.
/// @param <L> The type of the list being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see List#set
public interface SetContract<E, L extends List<E>> extends CollectionContractSupport<E, L> {

    /// Tests that the [set][List#set] method works.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the set method works")
    default void testSet() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE * 2);
        List<E> original = values.subList(0, DEFAULT_SIZE);
        List<E> updated = values.subList(DEFAULT_SIZE, DEFAULT_SIZE * 2);
        List<E> list = provider().createInstance(original);

        if (supportsMethod(CollectionMethods.Set)) {
            for (int i = 0; i < DEFAULT_SIZE; i++) {
                list.set(i, updated.get(i));
                assertEquals(updated.get(i), list.get(i));
            }
        } else {
            E e = updated.getFirst();
            assertThrows(UnsupportedOperationException.class, () -> list.set(0, e));
        }
    }

    /// Tests that the [set][List#set] method works on an empty list.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the set method works on an empty list")
    default void testSetWithEmptyCollection() {
        if (supportsMethod(CollectionMethods.Set)) {
            List<E> list = provider().emptyInstance();
            E e = elementProvider().createInstance();
            assertThrows(IndexOutOfBoundsException.class, () -> list.set(0, e));
        }
    }

    /// Tests that the [set][List#set] method throws for an invalid index.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the set method throws for invalid index")
    default void testSetThrowsForInvalidIndex() {
        if (supportsMethod(CollectionMethods.Set)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            List<E> list = provider().createInstanceWithUniqueElements();
            E e = elementProvider().createInstance();
            assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, e));
            assertThrows(IndexOutOfBoundsException.class, () -> list.set(list.size(), e));
        }
    }

    /// Tests that the [set][List#set] method works with a null element.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the set method works with a null element")
    default void testSetWithNullElement() {
        if (supportsMethod(CollectionMethods.Set)) {
            List<E> list = provider().createInstanceWithUniqueElements();
            if (permitNulls()) {
                list.set(1, null);
                assertNull(list.get(1));
            } else {
                assertThrows(NullPointerException.class, () -> list.set(0, null));
            }
        }
    }

}
