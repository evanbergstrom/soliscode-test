package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// This interface tests if a collection class has implemented the `isEmpty` method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#isEmpty
/// @since 1.0
public interface IsEmptyContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the `isEmpty` method returns `true` for an empty collection.
    @Test
    @DisplayName("Test isEmpty returns true for an empty collection")
    default void testIsEmptyForEmptyCollection() {
        Collection<E> collection = provider().emptyInstance();
        assertTrue(collection.isEmpty());
    }

    /// Tests that the `isEmpty` method returns `false` for a collection with
    /// at least one element.
    @Test
    @DisplayName("Test isEmpty returns false for a collection with elements")
    default void testIsEmptyForNonEmptyCollection() {
        Collection<E> collection = provider().createSingleton();
        assertFalse(collection.isEmpty());
    }
}
