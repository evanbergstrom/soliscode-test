package org.soliscode.test.contract.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;

/// **Contract for the `clear` method of a `Collection`**
///
/// This interface defines tests for the [clear][Collection#clear] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `clear` implementation correctly:
/// - Removes all elements from the collection.
/// - Leaves the collection empty.
/// - Throws [UnsupportedOperationException] if the method is not supported by the implementation.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyCollectionClearTest implements ClearContract<String, MyCollection<String>> {
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
/// @see Collection#clear
/// @since 1.0
public interface ClearContract<E, C extends Collection<E>> extends CollectionContractSupport<E, C> {

    /// Tests that the [clear][Collection#clear] method works for an empty collection.
    ///
    /// This test verifies that:
    /// 1. Calling `clear()` on an already empty collection is successful.
    /// 2. The collection remains empty.
    /// 3. If `clear` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Collection#clear
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("clear() works for an empty collection")
    @Test
    default void clear_whenEmpty_isSuccessful() {
        Collection<E> collection = provider().emptyInstance();
        if (supportsMethod(CollectionMethods.CLEAR)) {
            collection.clear();
            assertIsEmpty(collection);
        } else {
            assertThrows(UnsupportedOperationException.class, collection::clear);
        }
    }

    /// Tests that the [clear][Collection#clear] method works for a collection with elements.
    ///
    /// This test verifies that:
    /// 1. Calling `clear()` on a collection with elements removes all elements.
    /// 2. The collection is empty after the call.
    /// 3. If `clear` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Collection#clear
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws AssertionFailedError if any assertions failed
    @DisplayName("clear() works for a collection with elements")
    @Test
    default void clear_whenNotEmpty_removesAllElements() {
        Collection<E> collection = provider().createSingleton(elementProvider().createInstance());
        if (supportsMethod(CollectionMethods.CLEAR)) {
            collection.clear();
            assertIsEmpty(collection);
        } else {
            assertThrows(UnsupportedOperationException.class, collection::clear);
        }
    }
}
