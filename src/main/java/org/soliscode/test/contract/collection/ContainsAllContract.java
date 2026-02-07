package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.CollectionTestUtils;
import org.soliscode.test.util.MatchNothing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `containsAll` method of a `Collection`**
///
/// This interface defines tests for the [containsAll][Collection#containsAll] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `containsAll` implementation correctly:
/// - Returns `true` if the collection contains all of the elements in the specified collection.
/// - Handles empty collections correctly.
/// - Handles null elements according to the collection's configuration.
/// - Handles incompatible types according to the collection's configuration.
/// - Throws [UnsupportedOperationException] if the method is not supported by the implementation.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionContainsAllTest implements ContainsAllContract<String, MyCollection<String>> {
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
/// @see Collection#containsAll
/// @since 1.0
public interface ContainsAllContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [containsAll][Collection#containsAll] method works for an empty collection.
    ///
    /// This test verifies that:
    /// 1. An empty collection contains all elements of another empty collection.
    /// 2. An empty collection does not contain all elements of a non-empty collection.
    ///
    /// @see Collection#containsAll
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("containsAll(Collection) works for an empty collection")
    @Test
    default void containsAll_whenEmpty_returnsTrueForEmptyAndFalseForNotEmpty() {
        if (supportsMethod(CollectionMethods.CONTAINS_ALL)) {
            Collection<E> collection = provider().emptyInstance();
            assertTrue(collection.containsAll(Collections.emptyList()));

            Collection<E> notEmptyValue = provider().createInstanceWithUniqueElements();
            assertFalse(collection.containsAll(notEmptyValue));
        }
    }

    /// Tests that the [containsAll][Collection#containsAll] method works for a collection with elements.
    ///
    /// This test verifies that:
    /// 1. A collection contains all elements of an empty collection.
    /// 2. A collection contains all of its own elements.
    /// 3. A collection contains all elements of its sub-parts.
    /// 4. A collection does not contain all elements if some are missing.
    ///
    /// @see Collection#containsAll
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("containsAll(Collection) works for a collection with elements")
    @Test
    default void containsAll_whenNotEmpty_returnsExpectedResults() {
        Supplier<E> elementSupplier = elementProvider().uniqueInstanceSupplier();
        List<E> contained = Stream.generate(elementSupplier).limit(DEFAULT_SIZE).toList();
        List<E> notContained = Stream.generate(elementSupplier).limit(DEFAULT_SIZE).toList();
        Collection<E> collection = provider().createInstance(contained);

        if (supportsMethod(CollectionMethods.CONTAINS_ALL)) {
            assertTrue(collection.containsAll(Collections.emptyList()));
            assertTrue(collection.containsAll(contained));
            assertTrue(collection.containsAll(contained.subList(0, 1)));
            assertFalse(collection.containsAll(notContained));

            List<E> partiallyContained = new ArrayList<>(contained);
            partiallyContained.add(notContained.getFirst());
            assertFalse(collection.containsAll(partiallyContained));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.containsAll(notContained));
        }
    }

    /// Tests that the [containsAll][Collection#containsAll] method works for a collection with null elements.
    ///
    /// This test verifies that:
    /// 1. If nulls are permitted, the collection can contain a collection of null elements.
    ///
    /// @see Collection#containsAll
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("containsAll(Collection) works for a collection with null elements")
    @Test
    default void containsAll_withNullElements_returnsTrue() {
        if (permitNulls()) {
            List<E> values = elementProvider().createUniqueInstances(2);
            List<E> elements = CollectionTestUtils.listOf(values.get(0), null, values.get(1));
            Collection<E> collection = provider().createInstance(elements);
            assertTrue(collection.containsAll(unmodifiableCollection(values)));
            //noinspection RedundantCollectionOperation
            assertTrue(collection.containsAll(singletonList(null)));
        }
    }

    /// Tests that the [containsAll][Collection#containsAll] method works with incompatible types.
    ///
    /// This test verifies that:
    /// 1. `containsAll` returns `false` when the argument collection contains an incompatible type.
    ///
    /// @see Collection#containsAll
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("containsAll(Collection) returns false with incompatible types")
    @Test
    default void containsAll_withIncompatibleType_returnsFalse() {
        Collection<E> collection = provider().createInstanceWithUniqueElements(2);
        //noinspection RedundantCollectionOperation
        assertFalse(collection.containsAll(singletonList(new MatchNothing())));
    }
}
