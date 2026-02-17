package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertEqualsByIdentity;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertSameSize;

/// **Contract for the `stream` and `parallelStream` methods of a `Collection`**
///
/// This interface defines tests for the [stream][Collection#stream] and
/// [parallelStream][Collection#parallelStream] methods. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `stream` and `parallelStream`
/// implementations correctly:
/// - Return a stream that represents the elements of the collection.
/// - Handle empty collections.
/// - Handle collections with elements.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionStreamTest implements StreamContract<String, MyCollection<String>> {
///     @Override
///     public CollectionProvider<String, MyCollection<String>> provider() {
///         return MyCollection::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Collection] and [org.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#stream
/// @see Collection#parallelStream
/// @see Stream
/// @since 1.0.0
public interface StreamContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [stream][Collection#stream] method works for an empty collection.
    ///
    /// This test verifies that calling `stream()` on an empty collection returns an empty stream.
    ///
    /// @see Collection#stream
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("stream() returns an empty stream for an empty collection")
    @Test
    default void stream_whenEmpty_returnsEmptyStream() {
        Collection<E> collection = provider().emptyInstance();
        Stream<E> stream = collection.stream();
        assertEquals(0, stream.count());
    }

    /// Tests that the [stream][Collection#stream] method works for a collection with elements.
    ///
    /// This test verifies that calling `stream()` on a non-empty collection returns a stream
    /// containing all elements.
    ///
    /// @see Collection#stream
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("stream() returns a stream containing all elements for a non-empty collection")
    @Test
    default void stream_whenNotEmpty_returnsStreamWithElements() {
        Collection<E> collection = provider().createInstanceWithUniqueElements();
        Collection<E> elements = collection.stream().toList();
        assertSameSize(collection, elements);
        assertEqualsByIdentity(elements, collection);
    }

    /// Tests that the [parallelStream][Collection#parallelStream] method works for an empty collection.
    ///
    /// This test verifies that calling `parallelStream()` on an empty collection returns an empty stream.
    ///
    /// @see Collection#parallelStream
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("parallelStream() returns an empty stream for an empty collection")
    @Test
    default void parallelStream_whenEmpty_returnsEmptyStream() {
        Collection<E> collection = provider().emptyInstance();
        Stream<E> stream = collection.parallelStream();
        assertEquals(0, stream.count());
    }

    /// Tests that the [parallelStream][Collection#parallelStream] method works for a collection with elements.
    ///
    /// This test verifies that calling `parallelStream()` on a non-empty collection returns a stream
    /// containing all elements.
    ///
    /// @see Collection#parallelStream
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("parallelStream() returns a stream containing all elements for a non-empty collection")
    @Test
    default void parallelStream_whenNotEmpty_returnsStreamWithElements() {
        Collection<E> collection = provider().createInstanceWithUniqueElements();
        Collection<E> elements = collection.parallelStream().toList();
        assertSameSize(collection, elements);
        assertEqualsByIdentity(elements, collection);
    }
}
