package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.util.MatchNothing;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// This interface tests if a list class has implemented the [indexOf][List#indexOf] method correctly.
///
/// @param <E> The element type being tested.
/// @param <L> The list type being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see List#indexOf
public interface IndexOfContract<E, L extends List<E>> extends CollectionContractSupport<E, L> {

    /// Tests that the [indexOf][List#indexOf] method works.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the indexOf method works")
    default void testIndexOf() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        List<E> list = provider().createInstance(values);
        for (int i = 0; i<list.size(); i++) {
            assertEquals(i, list.indexOf(values.get(i)));
        }
    }

    /// Tests that the [indexOf][List#indexOf] method works on an empty list.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the indexOf method works on an empty list")
    default void testIndexOfWithEmptyCollection() {
        E e = elementProvider().createInstance();
        List<E> list = provider().emptyInstance();
        assertEquals(-1, list.indexOf(e));
    }

    /// Tests that the [indexOf][List#indexOf] method with an incompatible type.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the indexOf method works with an incompatible type")
    default void testIndexOfWithIncompatibleType() {
        List<E> list = provider().createInstanceWithUniqueElements();
        assertEquals(-1, list.indexOf(new MatchNothing()));
    }

    /// Tests that the [indexOf][List#indexOf] method works with a null value.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the indexOf method works with a null value")
    default void testIndexOfWithNullValue() {
        List<E> list = provider().createInstanceWithUniqueElements();
        assertEquals(-1, list.indexOf(null));
    }
}
