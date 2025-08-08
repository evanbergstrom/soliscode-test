package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.MatchNothing;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// This interface tests if a collection class has implemented the `remove` method correctly.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#remove
/// @since 1.0.0
public interface RemoveContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the `remove` method works on an empty container.
    @Test
    @DisplayName("The remove method can be called on an empty container")
    default void testRemoveOnEmptyContainer() {
        Collection<E> collection = provider().emptyInstance();
        E e = elementProvider().createInstance();
        if (supportsMethod(CollectionMethods.Remove)) {
            boolean changed = collection.remove(e);
            assertFalse(changed);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.remove(e));
        }
    }

    /// Tests that the `remove` method works on a container with elements.
    /// #Implementation Note
    /// There is no guarantee of the order that the elements are added to the collection, but in case
    /// they are stored in the same order they are added, this test will remove elements that are added first,
    /// added lsat, and added in the middle.
    @Test
    @DisplayName("The remove method works on a container with elements")
    default void testRemoveOnContainerWithElements() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);
        if (supportsMethod(CollectionMethods.Remove)) {
            // Remove element in middle of collection
            int middleIndex = values.size() / 2;
            E middle = values.get(middleIndex);
            boolean changed = collection.remove(middle);
            assertTrue(changed);
            assertFalse(collection.contains(middle));

            // Remove first element
            E first = values.getFirst();
            changed = collection.remove(first);
            assertTrue(changed);
            assertFalse(collection.contains(first));

            // Remove first element
            E last = values.getLast();
            changed = collection.remove(last);
            assertTrue(changed);
            assertFalse(collection.contains(last));

            // Remove the remaining elements
            for (int i = 1; i < values.size() - 1; i++) {
                if (i != middleIndex) {
                    E e = values.get(i);
                    changed = collection.remove(e);
                    assertTrue(changed);
                    assertFalse(collection.contains(e));
                }
            }
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.remove(values.getFirst()));
        }
    }

    /// Tests that the `remove` method works with `null` elements.
    @Test
    @DisplayName("The remove method works with null elements")
    default void testRemoveOnContainerWithNulls() {
        if (supportsMethod(CollectionMethods.Remove) && permitNulls()) {
            List<E> values = elementProvider().createUniqueInstances(2);
            Collection<E> collection = provider().emptyInstance();
            collection.add(values.get(0));
            collection.add(null);
            collection.add(values.get(1));

            assertTrue(collection.remove(values.get(0)));
            assertFalse(collection.contains(values.get(0)));

            assertTrue(collection.remove(null));
            assertFalse(collection.contains(null));

            assertTrue(collection.remove(values.get(1)));
            assertFalse(collection.contains(values.get(1)));
        }
    }

    /// Tests that the `remove` method works with incompatible objects.
    @Test
    @DisplayName("The remove method works with incompatible types")
    default void testRemoveOnIncompatibleObject() {
        if (supportsMethod(CollectionMethods.Remove)) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            assertFalse(collection.remove(new MatchNothing()));
        }
    }
}
