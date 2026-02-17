package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.MatchNothing;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `remove` method of a `Collection`**
///
/// This interface defines tests for the [remove][Collection#remove] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `remove` implementation correctly:
/// - Removes a single instance of the specified element from the collection.
/// - Returns `true` if the collection changed as a result of the call.
/// - Updates the collection size appropriately.
/// - Handles `null` values according to the collection's configuration.
/// - Handles incompatible types according to the collection's configuration.
/// - Throws [UnsupportedOperationException] if the method is not supported by the implementation.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionRemoveTest implements RemoveContract<String, MyCollection<String>> {
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
/// @see Collection#remove
/// @since 1.0.0
public interface RemoveContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [remove][Collection#remove] method returns `false` when called on an empty collection.
    ///
    /// This test verifies that calling `remove` on an empty collection returns `false` and does not throw.
    ///
    /// @see Collection#remove
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("remove(Object) returns false for an empty collection")
    @Test
    default void remove_whenEmpty_returnsFalse() {
        Collection<E> collection = provider().emptyInstance();
        E e = elementProvider().createInstance();
        if (supportsMethod(CollectionMethods.REMOVE)) {
            boolean changed = collection.remove(e);
            assertFalse(changed);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.remove(e));
        }
    }

    /// Tests that the [remove][Collection#remove] method works for a collection with elements.
    ///
    /// This test verifies that:
    /// 1. A single element is removed from the collection.
    /// 2. The method returns `true` indicating the collection has changed.
    /// 3. The collection no longer contains the removed element.
    ///
    /// @see Collection#remove
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("remove(Object) returns expected results for a collection with elements")
    @Test
    default void remove_whenNotEmpty_returnsExpectedResults() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Collection<E> collection = provider().createInstance(values);
        if (supportsMethod(CollectionMethods.REMOVE)) {
            // Remove element in middle of collection
            int middleIndex = values.size() / 2;
            E middle = values.get(middleIndex);
            boolean changed = collection.remove(middle);
            assertTrue(changed);
            assertFalse(collection.contains(middle));

            // Remove first element
            E first = values.getFirst();
            changed = collection.remove(first);
            assertTrue(changed);
            assertFalse(collection.contains(first));

            // Remove last element
            E last = values.getLast();
            changed = collection.remove(last);
            assertTrue(changed);
            assertFalse(collection.contains(last));

            // Remove the remaining elements
            for (int i = 1; i < values.size() - 1; i++) {
                if (i != middleIndex) {
                    E e = values.get(i);
                    changed = collection.remove(e);
                    assertTrue(changed);
                    assertFalse(collection.contains(e));
                }
            }
        } else {
            assertThrows(UnsupportedOperationException.class, () -> collection.remove(values.getFirst()));
        }
    }

    /// Tests that the [remove][Collection#remove] method handles `null` values correctly.
    ///
    /// This test verifies that adding and then removing `null` works correctly if permitted.
    ///
    /// @see Collection#remove
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("remove(Object) handles null values correctly")
    @Test
    default void remove_withNullValue_returnsExpectedResults() {
        if (supportsMethod(CollectionMethods.REMOVE) && permitNulls()) {
            List<E> values = elementProvider().createUniqueInstances(2);
            Collection<E> collection = provider().emptyInstance();
            collection.add(values.get(0));
            collection.add(null);
            collection.add(values.get(1));

            assertTrue(collection.remove(values.get(0)));
            assertFalse(collection.contains(values.get(0)));

            assertTrue(collection.remove(null));
            assertFalse(collection.contains(null));

            assertTrue(collection.remove(values.get(1)));
            assertFalse(collection.contains(values.get(1)));
        }
    }

    /// Tests that the [remove][Collection#remove] method handles incompatible types correctly.
    ///
    /// This test verifies that calling `remove` with an incompatible type returns `false`.
    ///
    /// @see Collection#remove
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("remove(Object) returns false for incompatible types")
    @Test
    default void remove_withIncompatibleType_returnsFalse() {
        if (supportsMethod(CollectionMethods.REMOVE)) {
            Collection<E> collection = provider().createInstanceWithUniqueElements();
            assertFalse(collection.remove(new MatchNothing()));
        }
    }
}
