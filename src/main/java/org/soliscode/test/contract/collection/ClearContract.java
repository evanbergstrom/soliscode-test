package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;

/// This interface tests if a collection class has implemented the `clear()` method
/// correctly.
///
/// @author evanbergstrom
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @see Collection#clear
/// @since 1.0
public interface ClearContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the `clear()` method works for an empty collection.
    @Test
    @DisplayName("Test clear works for an empty collection")
    default void testClearOnEmptyCollection() {
        Collection<E> collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.Clear)) {
            collection.clear();
            assertIsEmpty(collection);
        } else {
            assertThrows(UnsupportedOperationException.class, collection::clear);
        }
    }

    /// Tests that the `clear()` method works for a collection with elements.
    @Test
    @DisplayName("Test clear works for a collection with elements")
    default void testClearOnCollectionWithElements() {
        Collection<E> collection = provider().createSingleton(elementProvider().createInstance());
        if (supportsMethod(CollectionMethods.Clear)) {
            collection.clear();
            assertIsEmpty(collection);
        } else {
            assertThrows(UnsupportedOperationException.class, collection::clear);
        }
    }
}
