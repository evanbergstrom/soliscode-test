package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.assertions.collection.CollectionAssertions;
import org.soliscode.test.contract.collection.ToArrayContract;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// This contract tests if a list class has implemented the [toArray][List#toArray()] method correctly.
///
/// @param <E> The element type being tested.
/// @param <L> The list type being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see List#sort(Comparator)
public interface ListToArrayContract<E, L extends List<E>> extends ToArrayContract<E, L>, CollectionContractSupport<E, L> {


    /// Tests that the `toArray()` method works for a collection with elements.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("The toArray method works on a container with elements")
    default void testToArray() {
        Collection<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);
        Object[] array = collection.toArray();
        CollectionAssertions.assertEquals(values, array);
    }

    /// Tests that the `toArray()` method returns a safe array that can be modified without changing the
    /// elements of the container.
    /// @throws AssertionFailedError if the test fails.
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
}
