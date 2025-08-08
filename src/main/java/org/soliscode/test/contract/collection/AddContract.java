package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/// This interface tests if a collection class has implemented the [add][Collection#add] method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#add
/// @since 1.0
public interface AddContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [add][Collection#add] method works.
    @DisplayName("Test that the add method works")
    @Test
    default void testAdd() {
        if (supportsMethod(CollectionMethods.Add)) {
            Collection<E> collection = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                assertTrue(collection.add(values.get(i)));
                assertTrue(collection.contains(values.get(i)));
                assertEquals(i + 1, collection.size());
            }
        } else {
            Collection<E> collection = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> collection.add(elementProvider().createInstance()));
        }
    }

    /// Tests that the [add][Collection#add] method handles null values correctly.
    @DisplayName("Test that the add method works with null element values")
    @Test
    default void testAddWithNullValue() {
        if (supportsMethod(CollectionMethods.Add)) {
            Collection<E> collection = provider().emptyInstance();
            if (permitNulls()) {
                assertTrue(collection.add(null));
                assertTrue(collection.contains(null));
                assertEquals(1, collection.size());
            } else {
                assertThrows(NullPointerException.class, () -> collection.add(null));
            }
        }
    }


    /// Tests that the [add][Collection#add] method handles duplicate values correctly.
    @DisplayName("Test that the add method works with duplicate element values")
    @Test
    default void addWithDuplicateValue() {
        if (supportsMethod(CollectionMethods.Add)) {
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
}
