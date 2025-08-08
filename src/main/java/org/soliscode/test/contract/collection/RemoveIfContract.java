package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsNone;

/// This interface tests if a collection class has implemented the `removeIf` method correctly.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#removeIf
/// @since 1.0.0
public interface RemoveIfContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the `removeIf` method works on an empty container.
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    @Test
    @DisplayName("The removeIf method can be called on an empty container")
    default void testRemoveIfOnEmptyContainer() {
        Collection<E> collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.RemoveIf)) {
            boolean changed = collection.removeIf((e) -> true);
            assertFalse(changed);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.removeIf((e) -> true));
        }
    }

    /// Tests that the `removeIf` method works on a container with elements.
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    @Test
    @DisplayName("The removeIf method works on a container with elements")
    default void testRemoveIfOnContainerWithElements() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);
        if (supportsMethod(CollectionMethods.RemoveIf)) {
            // Remove first element
            E first = values.getFirst();
            boolean changed = collection.removeIf((e) -> e.equals(first));
            assertTrue(changed);
            assertFalse(collection.contains(first));

            // Remove first element
            E last = values.getLast();
            changed = collection.removeIf((e) -> e.equals(last));
            assertTrue(changed);
            assertFalse(collection.contains(last));

            // Remove the remaining elements
            Collection<E> remaining = values.subList(1, values.size() - 1);
            changed = collection.removeIf(remaining::contains);
            assertTrue(changed);
            assertContainsNone(collection, values);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.remove(values.getFirst()));
        }
    }

    /// Tests that the `removeIf` method throws an exception when passed a `null` filter.
    ///
    /// @implNote
    /// This test checks that the `removeIf` function throws the correct exception if it is called on a `null`
    /// value. IntelliJ will detect a problem when it is called with a null argument since the method declaration
    /// has a NotNull annotation. Since this is what we are trying to test, the inspection is suppressed here.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("The removeIf method throws on a null filter")
    default void testRemoveIfThrowsOnANullFilter() {
        if (supportsMethod(CollectionMethods.RemoveIf)) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            assertThrows(NullPointerException.class, () -> collection.removeIf(null));
        }
    }
}
