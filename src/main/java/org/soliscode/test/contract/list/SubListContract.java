package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [subList(int, int)][List#subList(int, int)] method of the [List] interface.
///
/// @param <L> The type of the list
/// @param <E> The element type for the list
/// @author evanbergstrom
/// @since 1.0
/// @see List#subList(int, int)
/// @see ListContract
public interface SubListContract<L extends List<E>, E> extends CollectionContractSupport<L, E> {

    /// Tests that [subList(int, int)][List#subList(int, int)] returns the elements from the first index, included,
    /// to the second, excluded.
    @Test
    @DisplayName("subList(int, int) returns the elements in the range")
    default void subList_whenRangeIsValid_returnsElementsInRange() {
        List<E> initial = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        L list = provider().createInstance(initial);
        assertIterableEquals(initial.subList(2, DEFAULT_SIZE - 2), list.subList(2, DEFAULT_SIZE - 2));
        assertIterableEquals(initial, list.subList(0, DEFAULT_SIZE));
        assertTrue(list.subList(3, 3).isEmpty());
    }

    /// Tests that [subList(int, int)][List#subList(int, int)] throws [IndexOutOfBoundsException] for a range outside
    /// the list, and [IllegalArgumentException] or [IndexOutOfBoundsException] for a reversed range.
    @Test
    @DisplayName("subList(int, int) throws for a bad range")
    default void subList_whenRangeIsInvalid_throws() {
        L list = provider().createInstance(elementProvider().createUniqueInstances(DEFAULT_SIZE));
        assertThrows(IndexOutOfBoundsException.class, () -> list.subList(-1, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> list.subList(0, DEFAULT_SIZE + 1));
        RuntimeException e = assertThrows(RuntimeException.class, () -> list.subList(3, 2));
        assertTrue(e instanceof IllegalArgumentException || e instanceof IndexOutOfBoundsException, e.toString());
    }
}
