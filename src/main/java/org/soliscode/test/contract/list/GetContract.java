package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `get` method of a `List`**
///
/// This interface defines tests for the [get(int)][List#get] method. It is designed
/// to be used as a mix-in interface by test classes that verify [List] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a list's `get` implementation correctly:
/// - Returns the element at the specified position.
/// - Throws [IndexOutOfBoundsException] for invalid indices.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyListGetTest implements GetContract<String, MyList<String>> {
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
/// @see List#get
/// @since 1.0.0
public interface GetContract<E, L extends List<E>> extends CollectionContractSupport<E, L> {

    /// Tests that the [get][List#get] method works for valid indices.
    ///
    /// This test verifies that `get(int)` returns the correct element for every valid index in the list.
    ///
    /// @see List#get
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("get(int) returns the correct element for valid indices")
    default void get_whenIndexIsValid_returnsCorrectElement() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        List<E> list = provider().createInstance(values);
        for (int i = 0; i < list.size(); i++) {
            assertEquals(values.get(i), list.get(i));
        }
    }

    /// Tests that the [get][List#get] method throws [IndexOutOfBoundsException] for an empty list.
    ///
    /// @see List#get
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("get(int) throws IndexOutOfBoundsException for an empty list")
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    default void get_whenEmpty_throwsIndexOutOfBoundsException() {
        List<E> list = provider().emptyInstance();
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }

    /// Tests that the [get][List#get] method throws for an invalid index.
    ///
    /// This test verifies that `get(int)` throws [IndexOutOfBoundsException] for indices
    /// that are less than 0 or greater than or equal to the size of the list.
    ///
    /// @see List#get
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("get(int) throws IndexOutOfBoundsException for invalid index")
    @SuppressWarnings("DataFlowIssue")
    default void get_whenIndexIsInvalid_throwsIndexOutOfBoundsException() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        List<E> list = provider().createInstance(values);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(DEFAULT_SIZE));
    }
}
