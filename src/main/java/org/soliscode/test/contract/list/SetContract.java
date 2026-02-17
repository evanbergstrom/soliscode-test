package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `set` method of a `List`**
///
/// This interface defines tests for the [set(int, E)][List#set] method. It is designed
/// to be used as a mix-in interface by test classes that verify [List] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a list's `set` implementation correctly:
/// - Replaces the element at the specified position with the specified element.
/// - Returns the element previously at the specified position (though currently not verified by these tests).
/// - Throws [IndexOutOfBoundsException] for invalid indices.
/// - Handles null values according to the list's configuration.
/// - Throws [UnsupportedOperationException] if the method is not supported.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyListSetTest implements SetContract<String, MyList<String>> {
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
/// @param <E> The element type for the list being tested.
/// @param <L> The type of the list being tested.
/// @author evanbergstrom
/// @see List#set
/// @since 1.0.0
public interface SetContract<E, L extends List<E>> extends CollectionContractSupport<E, L> {

    /// Tests that the [set][List#set] method works for valid indices.
    ///
    /// This test verifies that `set(int, E)` correctly replaces elements at valid positions.
    ///
    /// @see List#set
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("set(int, E) replaces elements at valid indices")
    default void set_whenIndexIsValid_replacesElement() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE * 2);
        List<E> original = values.subList(0, DEFAULT_SIZE);
        List<E> updated = values.subList(DEFAULT_SIZE, DEFAULT_SIZE * 2);
        List<E> list = provider().createInstance(original);

        if (supportsMethod(ListMethods.SET)) {
            for (int i = 0; i < DEFAULT_SIZE; i++) {
                list.set(i, updated.get(i));
                assertEquals(updated.get(i), list.get(i));
            }
        } else {
            E e = updated.getFirst();
            assertThrows(UnsupportedOperationException.class, () -> list.set(0, e));
        }
    }

    /// Tests that the [set][List#set] method throws [IndexOutOfBoundsException] for an empty list.
    ///
    /// @see List#set
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("set(int, E) throws IndexOutOfBoundsException for an empty list")
    default void set_whenEmpty_throwsIndexOutOfBoundsException() {
        if (supportsMethod(ListMethods.SET)) {
            List<E> list = provider().emptyInstance();
            E e = elementProvider().createInstance();
            assertThrows(IndexOutOfBoundsException.class, () -> list.set(0, e));
        }
    }

    /// Tests that the [set][List#set] method throws for an invalid index.
    ///
    /// This test verifies that `set(int, E)` throws [IndexOutOfBoundsException] for indices
    /// that are less than 0 or greater than or equal to the size of the list.
    ///
    /// @see List#set
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("set(int, E) throws IndexOutOfBoundsException for invalid index")
    default void set_whenIndexIsInvalid_throwsIndexOutOfBoundsException() {
        if (supportsMethod(ListMethods.SET)) {
            List<E> list = provider().createInstanceWithUniqueElements();
            E e = elementProvider().createInstance();
            assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, e));
            assertThrows(IndexOutOfBoundsException.class, () -> list.set(list.size(), e));
        }
    }

    /// Tests that the [set][List#set] method handles `null` values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitNulls()].
    ///
    /// @see List#set
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("set(int, E) handles null values based on permission")
    default void set_withNullValue_handlesCorrectly() {
        if (supportsMethod(ListMethods.SET)) {
            List<E> list = provider().createInstanceWithUniqueElements();
            if (permitNulls()) {
                list.set(1, null);
                assertNull(list.get(1));
            } else {
                assertThrows(NullPointerException.class, () -> list.set(0, null));
            }
        }
    }
}
