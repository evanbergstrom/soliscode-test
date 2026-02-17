package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.CollectionTestUtils;
import org.soliscode.test.util.MatchNothing;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `contains` method of a `Collection`**
///
/// This interface defines tests for the [contains][Collection#contains] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `contains` implementation correctly:
/// - Returns `true` if the collection contains the specified element.
/// - Returns `false` if the collection does not contain the specified element.
/// - Handles `null` elements according to the collection's configuration.
/// - Handles incompatible types according to the collection's configuration.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionContainsTest implements ContainsContract<String, MyCollection<String>> {
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
/// @see Collection#contains
/// @since 1.0.0
public interface ContainsContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [contains][Collection#contains] method works for an empty collection.
    ///
    /// This test verifies that:
    /// 1. Calling `contains()` on an empty collection always returns `false` for any element.
    ///
    /// @see Collection#contains
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("contains(Object) returns false for an empty collection")
    @Test
    default void contains_whenEmpty_returnsFalse() {
        Collection<E> collection = provider().emptyInstance();
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        for (E e : values) {
            assertFalse(collection.contains(e));
        }
    }

    /// Tests that the [contains][Collection#contains] method works for a collection with elements.
    ///
    /// This test verifies that:
    /// 1. `contains()` returns `true` for elements present in the collection.
    /// 2. `contains()` returns `false` for elements not present in the collection.
    ///
    /// @see Collection#contains
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("contains(Object) returns expected results for a collection with elements")
    @Test
    default void contains_whenNotEmpty_returnsExpectedResults() {
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

    /// Tests that the [contains][Collection#contains] method works for a collection with null elements.
    ///
    /// This test verifies that:
    /// 1. If `null` is permitted, `contains(null)` returns `true` if the collection contains `null`.
    /// 2. If `null` is permitted, `contains()` still returns `true` for non-null elements.
    ///
    /// @see Collection#contains
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("contains(Object) works for a collection with null elements")
    @Test
    default void contains_withNullValue_returnsTrue() {
        if (permitNulls()) {
            List<E> values = elementProvider().createUniqueInstances(2);
            List<E> elements = CollectionTestUtils.listOf(values.get(0), null, values.get(1));
            Collection<E> collection = provider().createInstance(elements);

            for (E e : values) {
                assertTrue(collection.contains(e));
            }
            assertTrue(collection.contains(null));
        }
    }

    /// Tests that the [contains][Collection#contains] method works with incompatible types.
    ///
    /// This test verifies that:
    /// 1. `contains()` returns `false` when called with an incompatible type.
    ///
    /// @see Collection#contains
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("contains(Object) returns false for incompatible types")
    @Test
    default void contains_withIncompatibleType_returnsFalse() {
        Collection<E> collection = provider().createInstanceWithUniqueElements(2);
        assertFalse(collection.contains(new MatchNothing()));
    }
}
