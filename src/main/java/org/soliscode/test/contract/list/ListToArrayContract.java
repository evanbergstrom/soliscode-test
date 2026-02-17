package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.assertions.collection.CollectionAssertions;
import org.soliscode.test.contract.collection.ToArrayContract;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `toArray` method of a `List`**
///
/// This interface defines tests for the [toArray()][List#toArray] method specifically for lists.
/// It extends [ToArrayContract] and adds or overrides tests to ensure list-specific
/// requirements (such as order) are met.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a list's `toArray` implementation correctly:
/// - Returns an array containing all elements in the correct order.
/// - Returns a safe array that does not modify the underlying list when changed.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyListToArrayTest implements ListToArrayContract<String, MyList<String>> {
///     @Override
///     public CollectionProvider<String, MyList<String>> provider() {
///         return MyList::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [List] and [org.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <L> The list type being tested.
/// @author evanbergstrom
/// @see List#toArray
/// @since 1.0.0
public interface ListToArrayContract<E, L extends List<E>>
        extends ToArrayContract<E, L>, CollectionContractSupport<E, L> {


    /// Tests that the [toArray()][List#toArray] method works for a collection with elements.
    ///
    /// This test verifies that the returned array contains all elements in the same order as the list.
    ///
    /// @see List#toArray
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Override
    @Test
    @DisplayName("toArray() returns an array containing all elements in order")
    default void toArray_whenNotEmpty_returnsArrayWithElements() {
        Collection<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);
        Object[] array = collection.toArray();
        CollectionAssertions.assertEquals(values, array);
    }

    /// Tests that the [toArray()][List#toArray] method returns a safe array that can be modified
    /// without changing the elements of the container.
    ///
    /// @see List#toArray
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @Override
    @Test
    @DisplayName("toArray() returns a safe array")
    default void toArray_whenCalled_returnsSafeArray() {
        Collection<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);

        E newValue = elementProvider().createInstance();
        Arrays.fill(collection.toArray(), newValue);

        for (E e : values) {
            assertTrue(collection.contains(e));
        }
    }
}
