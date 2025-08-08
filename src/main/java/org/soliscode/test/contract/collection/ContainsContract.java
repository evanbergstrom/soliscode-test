package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.CollectionTestOps;
import org.soliscode.test.util.MatchNothing;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// This interface tests if a collection class has implemented the `contains()` method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#contains
/// @since 1.0
public interface ContainsContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /**
     * Tests that the {@code contains()} method works for an empty collection.
     */
    @Test
    default void testContainsOnEmptyCollection() {
        Collection<E> collection = provider().emptyInstance();
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        for (E e : values) {
            assertFalse(collection.contains(e));
        }
    }

    /**
     * Tests that the {@code size()} method works for a collection with elements.
     */
    @Test
    default void testContainsOnCollectionWithElements() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        int middle = values.size() / 2;
        List<E> contained = values.subList(0, middle);
        List<E> notContained = values.subList(middle, values.size());
        Collection<E> collection = provider().createInstance(contained);

        for (E e : contained) {
            assertTrue(collection.contains(e));
        }

        for (E e : notContained) {
            assertFalse(collection.contains(e));
        }
    }

    /**
     * Tests that the {@code contains()} method works for a collection with null elements.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    default void testContainsOnCollectionWithNulls() {
        List<E> values = elementProvider().createUniqueInstances(2);
        List<E> elements = CollectionTestOps.listOf(values.get(0), null, values.get(1));
        Collection<E> collection = provider().createInstance(elements);

        for (E e : values) {
            assertTrue(collection.contains(e));
        }
    }

    /**
     * Tests that the {@code contains()} method works with incompatible types.
     */
    @Test
    default void testContainsWithIncompatibleType() {
        Collection<E> collection = provider().createInstanceWithUniqueElements(2);
        assertFalse(collection.contains(new MatchNothing()));
    }
}
