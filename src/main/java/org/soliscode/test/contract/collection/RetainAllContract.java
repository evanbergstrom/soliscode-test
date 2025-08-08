package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.MatchNothing;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsAll;

/// This interface tests if a collection class has implemented the `retainAll` method correctly.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#retainAll
/// @since 1.0.0
public interface RetainAllContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the `retainAll` method works on an empty container.
    @Test
    @DisplayName("The retainAll method can be called on an empty container")
    default void testRetainAllOnEmptyContainer() {
        Collection<E> collection = provider().emptyInstance();
        Collection<E> values = elementProvider().createUniqueInstances(2);
        if (supportsMethod(CollectionMethods.RetainAll)) {
            boolean changed = collection.retainAll(values);
            assertFalse(changed);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.removeIf((e) -> true));
        }
    }

    /// Test that the `retainAll` method works on a container with elements.
    @Test
    @DisplayName("The retainAll method works on a container with elements")
    default void testRetainAllOnContainerWithElements() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);
        if (supportsMethod(CollectionMethods.RemoveAll)) {
            // Remove the remaining elements
            boolean changed = collection.retainAll(values.subList(1, values.size() - 1));
            assertTrue(changed);
            assertEquals(values.size() - 2, collection.size());
            assertContainsAll(collection, values);
            assertFalse(collection.contains(values.getFirst()));
            assertFalse(collection.contains(values.getLast()));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.remove(values.getFirst()));
        }
    }

    /// Tests that the `retainAll` method works with incompatible objects.
    @Test
    @DisplayName("The retainAll method works with incompatible types")
    default void testRetainAllOnIncompatibleObject() {
        if (supportsMethod(CollectionMethods.RemoveAll)) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            assertTrue(collection.retainAll(Collections.singleton(new MatchNothing())));
            assertTrue(collection.isEmpty());
        }
    }

    /// Tests that the `retainAll` method throws on a null collection.
    /// # Implementation Notes
    /// This test checks that the `retainAll` function throws the correct exception if it is called on a null
    /// value. IntelliJ will detect a problem when it is called with a null argument since the method declaration
    /// has a NotNull annotation. Since this is what we are trying to test, the inspection is suppressed here.
    ///
    /// Any implementations that use the `NotNull` annotation for the collection parameter may throw an
    /// `IllegalArgumentException` here, so either exception type is accepted.
    @Test
    @DisplayName("The retainAll method throws on a null collection")
    @SuppressWarnings("DataFlowIssue")
    default void testRetainAllThrowsOnNullCollection() {
        if (supportsMethod(CollectionMethods.RetainAll)) {
            Collection<E> collection = provider().emptyInstance();
            Assertions.assertThrowsAny(List.of(NullPointerException.class, IllegalArgumentException.class),
                    () -> collection.retainAll(null));
        }
    }
}
