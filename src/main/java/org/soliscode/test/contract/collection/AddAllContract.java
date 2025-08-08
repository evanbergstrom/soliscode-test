package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.*;

/// This interface tests if a collection class has implemented the `addAll()` method correctly. This is a "mix-in"
/// interface that is added to a test class.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#addAll
/// @since 1.0.0
public interface AddAllContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {


    /// Tests that the `addAll()` method works.
    @Test
    default void testAddAllToContainer() {
        if (supportsMethod(CollectionMethods.AddAll)) {
            Collection<E> collection = provider().emptyInstance();
            Collection<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);

            assertTrue(collection.addAll(values));
            assertContainsSameByIdentity(values, collection);
        } else {
            Collection<E> collection = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> collection.add(elementProvider().createInstance()));
        }
    }

    /// Tests that the `addAll()` method handles null values correctly.
    @Test
    default void testAddAllWithNullValue() {
        if (supportsMethod(CollectionMethods.AddAll)) {
            Collection<E> collection = provider().emptyInstance();
            if (permitNulls()) {
                collection.add(null);
                assertTrue(collection.contains(null));
                assertEquals(1, collection.size());
            } else {
                assertThrows(NullPointerException.class, () -> collection.add(null));
            }
        }
    }

    /// Tests that the `addAll()` method handles duplicate values correctly.
    @Test
    @SuppressWarnings("RedundantCollectionOperation")
    default void testAddAllWithDuplicateValue() {
        if (supportsMethod(CollectionMethods.AddAll)) {
            if (permitDuplicates()) {
                List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
                Collection<E> collection = provider().createInstance(values);
                collection.addAll(values);
                assertEquals(values.size() * 2, collection.size());
                assertContainsAll(values, collection);
            } else {
                Collection<E> collection = provider().emptyInstance();
                E value = elementProvider().createInstance();
                collection.add(value);
                E otherValue = elementProvider().copyInstance(value);
                collection.addAll(Collections.singleton(otherValue));
                assertEquals(1, collection.size());
            }
        }
    }
}
