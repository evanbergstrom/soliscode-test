package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.CollectionTestOps;
import org.soliscode.test.util.MatchNothing;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// This interface tests if a collection class has implemented the `containsAll()` method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#containsAll
/// @since 1.0
public interface ContainsAllContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the `containsAll()` method works for an empty collection.
    @Test
    default void testContainsAllOnEmptyCollection() {
        Collection<E> collection = provider().emptyInstance();

        List<E> emptyValues = Collections.emptyList();
        assertTrue(collection.containsAll(emptyValues));

        Collection<E> notEmptyValue = provider().createInstanceWithUniqueElements();
        assertFalse(collection.containsAll(notEmptyValue));
    }

    /// Tests that the `containsAll()` method works for a collection with elements.
    @Test
    default void testContainsAllOnCollectionWithElements() {
        Supplier<E> elementSupplier = elementProvider().uniqueInstanceSupplier();

        List<E> contained = Stream.generate(elementSupplier).limit(DEFAULT_SIZE).toList();
        List<E> notContained = Stream.generate(elementSupplier).limit(DEFAULT_SIZE).toList();
        Collection<E> collection = provider().createInstance(contained);

        assertFalse(collection.contains(Collections.emptyList()));
        assertTrue(collection.containsAll(contained));
        assertTrue(collection.containsAll(contained.subList(0, 1)));
        assertFalse(collection.containsAll(notContained));

        List<E> partiallyContained = new ArrayList<>(contained);
        partiallyContained.add(notContained.getFirst());
        assertFalse(collection.containsAll(partiallyContained));
    }

    /// Tests that the `containsAll()` method works for a collection with null elements.
    @SuppressWarnings("DataFlowIssue")
    @Test
    default void testContainsAllOnCollectionWithNulls() {
        List<E> values = elementProvider().createUniqueInstances(2);
        List<E> elements = CollectionTestOps.listOf(values.get(0), null, values.get(1));
        Collection<E> collection = provider().createInstance(elements);
        assertTrue(collection.containsAll(values));
        assertTrue(collection.contains(null));
    }

    /// Tests that the `containsAll()` method works with incompatible types.
    @Test
    default void testContainsAllWithIncompatibleType() {
        Collection<E> collection = provider().createInstanceWithUniqueElements(2);
        assertFalse(collection.contains(new MatchNothing()));
    }
}
