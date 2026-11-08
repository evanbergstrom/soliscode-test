package com.soliscode.test.contract.collection;

import java.util.Collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionContractSupport;

import static com.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
///     public CollectionProvider<String, MyCollection<String>> objectProvider() {
///         return MyCollection::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Collection] and [com.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#clear
/// @since 1.0.0
public interface ClearContract<C extends Collection<E>, E> extends CollectionContractSupport<C, E> {

    /// Tests that the [clear][Collection#clear] method works for an empty collection.
    ///
    /// This test verifies that:
    /// 1. Calling `clear()` on an already empty collection is successful.
    /// 2. The collection remains empty.
    /// 3. If `clear` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Collection#clear
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("clear() works for an empty collection")
    @Test
    default void clearWhenEmptyIsSuccessful() {
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
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("clear() works for a collection with elements")
    @Test
    default void clearWhenNotEmptyRemovesAllElements() {
        Collection<E> collection = provider().createSingleton(elementProvider().createInstance());
        if (supportsMethod(CollectionMethods.CLEAR)) {
            collection.clear();
            assertIsEmpty(collection);
        } else {
            assertThrows(UnsupportedOperationException.class, collection::clear);
        }
    }

    /// Tests that the [Collection#clear()] method throws [UnsupportedOperationException]
    /// when the operation is not supported by a collection.
    ///
    /// This test verifies that:
    ///     - Calling `clear()` on a collection that does not support the operation
    ///     results in an [UnsupportedOperationException] being thrown.
    ///
    /// The test uses the `objectProvider().emptyInstance()` method to supply an empty
    /// instance of the collection being tested. If the collection's implementation declares
    /// `clear()` as unsupported, the operation must adhere to this contract by throwing
    /// [UnsupportedOperationException].
    ///
    /// @see Collection#clear()
    /// @throws UnsupportedOperationException if the `clear()` method is not supported
    /// @throws org.opentest4j.AssertionFailedError if the expected exception is not thrown
    @DisplayName("clear() throws UnsupportedOperationException if not supported")
    @Test
    default void clearWhenNotSupportedThrowsUnsupportedOperationException() {
        if (!supportsMethod(CollectionMethods.CLEAR)) {
            Collection<E> collection = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, collection::clear);
        }
    }
}
