package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertEqualsByIdentity;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertSameSize;

/// This interface tests if a collection class has implemented the `stream()` method
/// correctly and that the iterator that is returned satisfies the [Stream] contract.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#stream
/// @see Stream
/// @since 1.0
public interface StreamContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the `stream` method works for an empty collection.
    @Test
    @DisplayName("The stream method can be called on an empty collection")
    default void testStreamForEmptyCollection() {
        Collection<E> collection = provider().emptyInstance();
        Stream<E> stream = collection.stream();
        assertEquals(0, stream.count());
    }

    /// Tests that the `stream` method works for a collection with elements.
    @Test
    @DisplayName("The stream method can be called on a collection with elements")
    default void testStreamForCollectionWithElements() {
        Collection<E> collection = provider().createInstanceWithUniqueElements();
        Collection<E> elements = collection.stream().toList();
        assertSameSize(collection, elements);
        assertEqualsByIdentity(elements, collection);
    }

    /// Tests that the `parallelStream` method works for an empty collection.
    @Test
    @DisplayName("The parallelStream method can be called on an empty collection")
    default void testParallelStreamForEmptyCollection() {
        Collection<E> collection = provider().emptyInstance();
        Stream<E> stream = collection.parallelStream();
        assertEquals(0, stream.count());
    }

    /// Tests that the `parallelStream` method works for a collection with elements.
    @Test
    @DisplayName("The parallelStream method can be called on a collection with elements")
    default void testParallelStreamForCollectionWithElements() {
        Collection<E> collection = provider().createInstanceWithUniqueElements();
        Collection<E> elements = collection.parallelStream().toList();
        assertSameSize(collection, elements);
        assertEqualsByIdentity(elements, collection);
    }
}
