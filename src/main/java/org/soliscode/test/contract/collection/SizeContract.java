package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// This interface tests if a collection class has implemented the `size()` method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#size
/// @since 1.0
public interface SizeContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the `size()` method works for an empty collection.
    @Test
    default void testSizeOnEmptyCollection() {
        Collection<E> collection = provider().emptyInstance();
        assertEquals(0, collection.size());
    }

    /// Tests that the `size()` method works for a collection with elements.
    @Test
    default void testSizeOnCollectionWithElements() {
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            List<E> values = elementProvider().createUniqueInstances(i);
            Collection<E> collection = provider().createInstance(values);
            assertEquals(values.size(), collection.size());
        }
    }
}
