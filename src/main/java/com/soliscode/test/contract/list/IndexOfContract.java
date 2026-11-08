package com.soliscode.test.contract.list;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionContractSupport;
import com.soliscode.test.util.MatchNothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// **Contract for the `indexOf` method of a `List`**
///
/// This interface defines tests for the [indexOf(Object)][List#indexOf] method. It is designed
/// to be used as a mix-in interface by test classes that verify [List] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a list's `indexOf` implementation correctly:
/// - Returns the index of the first occurrence of the specified element.
/// - Returns -1 if the element is not present.
/// - Handles empty collections, null values, and incompatible types.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyListIndexOfTest implements IndexOfContract<String, MyList<String>> {
///     @Override
///     public CollectionProvider<String, MyList<String>> objectProvider() {
///         return MyList::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [List] and [com.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <L> The list type being tested.
/// @author evanbergstrom
/// @see List#indexOf
/// @since 1.0.0
public interface IndexOfContract<L extends List<E>, E> extends CollectionContractSupport<L, E> {

    /// Tests that the [indexOf][List#indexOf] method works for elements present in the list.
    ///
    /// This test verifies that `indexOf(Object)` returns the correct index for every element in the list.
    ///
    /// @see List#indexOf
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("indexOf(Object) returns correct index for present elements")
    default void indexOfWhenElementIsPresentReturnsCorrectIndex() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        List<E> list = provider().createInstance(values);
        for (int i = 0; i < list.size(); i++) {
            assertEquals(i, list.indexOf(values.get(i)));
        }
    }

    /// Tests that the [indexOf][List#indexOf] method works on an empty list.
    ///
    /// @see List#indexOf
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("indexOf(Object) returns -1 for an empty list")
    default void indexOfWhenEmptyReturnsMinusOne() {
        E e = elementProvider().createInstance();
        List<E> list = provider().emptyInstance();
        assertEquals(-1, list.indexOf(e));
    }

    /// Tests that the [indexOf][List#indexOf] method works with an incompatible type.
    ///
    /// @see List#indexOf
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("indexOf(Object) returns -1 for incompatible types")
    default void indexOfWithIncompatibleTypeReturnsMinusOne() {
        List<E> list = provider().createInstanceWithUniqueElements();
        assertEquals(-1, list.indexOf(new MatchNothing()));
    }

    /// Tests that the [indexOf][List#indexOf] method works with a null value.
    ///
    /// A list that permits null returns -1. A list that does not may instead throw [NullPointerException]:
    /// [List#indexOf] makes that exception optional (T-0051).
    ///
    /// @see List#indexOf
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @since 1.0.0
    @Test
    @DisplayName("indexOf(null) returns -1 when not present, or throws NPE for a list that refuses null")
    default void indexOfWithNullValueReturnsMinusOne() {
        List<E> list = provider().createInstanceWithUniqueElements();
        try {
            assertEquals(-1, list.indexOf(null));
        } catch (final NullPointerException e) {
            if (permitNulls()) {
                throw e;
            }
        }
    }
}
