package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// This interface tests if a list class has implemented the [get][List#get] method correctly.
///
/// @param <E> The element type being tested.
/// @param <L> The list type being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see List#get
public interface GetContract<E, L extends List<E>> extends CollectionContractSupport<E, L> {

    /// Tests that the [get][List#get] method works.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the get method works")
    default void testGet() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        List<E> list = provider().createInstance(values);
        for (int i = 0; i<list.size(); i++) {
            assertEquals(values.get(i), list.get(i));
        }
    }

    /// Tests that the [get][List#get] method works on an empty list.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the get method works on an empty list")
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    default void testGetWithEmptyCollection() {
        List<E> list = provider().emptyInstance();
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }

    /// Tests that the [get][List#get] method throws for an invalid index.
    /// @throws AssertionFailedError if the test fails.
    @Test
    @DisplayName("Test that the get method throws for invalid index")
    @SuppressWarnings("DataFlowIssue")
    default void testGetThrowsForInvalidIndex() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        List<E> list = provider().createInstance(values);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(DEFAULT_SIZE));
    }
}
