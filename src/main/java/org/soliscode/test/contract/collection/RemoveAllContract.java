package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.MatchNothing;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsNone;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertDoesNotContain;

/// This interface tests if a collection class has implemented the `removeAll` method correctly.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#removeAll
/// @since 1.0
public interface RemoveAllContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Test that the `removeAll` method works on an empty container.
    @Test
    @DisplayName("The removeAll method can be called on an empty container")
    default void testRemoveAllOnEmptyContainer() {
        Collection<E> collection = provider().emptyInstance();
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        if (supportsMethod(CollectionMethods.RemoveAll)) {
            boolean changed = collection.removeAll(values);
            assertFalse(changed);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.removeAll(values));
        }
    }

    /// Test that the `removeAll` method works on a container with elements.
    @Test
    @DisplayName("The removeAll method works on a container with elements")
    default void testRemoveAllOnContainerWithElements() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);
        if (supportsMethod(CollectionMethods.RemoveAll)) {
            // Remove first element
            E first = values.getFirst();
            boolean changed = collection.removeAll(Collections.singleton(first));
            assertTrue(changed);
            assertDoesNotContain(first, collection);

            // Remove first element
            E last = values.getLast();
            changed = collection.removeAll(Collections.singleton(last));
            assertTrue(changed);
            assertFalse(collection.contains(last));

            // Remove the remaining elements
            changed = collection.removeAll(values.subList(1, values.size() - 1));
            assertTrue(changed);
            assertContainsNone(values, collection);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.remove(values.getFirst()));
        }
    }

    /// Tests that the `removeAll` method works with incompatible objects.
    @Test
    @DisplayName("The removeAll method works with null elements")
    default void testRemoveAllOnNullElement() {
        if (supportsMethod(CollectionMethods.RemoveAll) && permitNulls()) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            Collection<E> argument = Collections.singleton(null);
            if (permitNulls()) {
                assertFalse(collection.removeAll(argument));
            } else {
                assertThrows(NullPointerException.class, () -> collection.removeAll(argument));
            }
        }
    }

    /// Tests that the `removeAll` method works with incompatible objects.
    @Test
    @DisplayName("The removeAll method works with incompatible types")
    default void testRemoveAllOnIncompatibleObject() {
        if (supportsMethod(CollectionMethods.RemoveAll)) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            assertFalse(collection.removeAll(Collections.singleton(new MatchNothing())));
        }
    }


    /// Tests that the `removeAll` method throws on a null collection.
    /// # Implementation Notes
    /// This test checks that the `removeAll` function throws the correct exception if it is called on a null
    /// value. IntelliJ will detect a problem when it is called with a null argument since the method declaration
    /// has a NotNull annotation. Since this is what we are trying to test, the inspection is suppressed here.
    ///
    /// Any implementations that use the `NotNull` annotation for the collection parameter may throw an
    /// `IllegalArgumentException` here, so either exception type is accepted.
    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("The removeAll method throws on a null collection")
    default void testRemoveAllThrowsOnNullCollection() {
        if (supportsMethod(CollectionMethods.RemoveAll)) {
            Collection<E> collection = provider().emptyInstance();
            Assertions.assertThrowsAny(List.of(NullPointerException.class, IllegalArgumentException.class),
                    () -> collection.removeAll(null));
        }
    }
}
