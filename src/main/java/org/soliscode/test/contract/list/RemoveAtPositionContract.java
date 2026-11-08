package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [remove(int)][List#remove(int)] method of the [List] interface.
///
/// @param <L> The type of the list
/// @param <E> The element type for the list
/// @author evanbergstrom
/// @since 1.0
/// @see List#remove(int)
/// @see ListContract
public interface RemoveAtPositionContract<L extends List<E>, E> extends CollectionContractSupport<L, E> {

    /// Tests that [remove(int)][List#remove(int)] removes and returns the element at the index, at the end, the
    /// middle and the start.
    @Test
    @DisplayName("remove(int) removes and returns the element at the index")
    default void removeAtIndex_whenIndexIsValid_removesAndReturnsElement() {
        List<E> initial = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        L list = provider().createInstance(initial);
        if (supportsMethod(ListMethods.REMOVE_AT_INDEX)) {
            List<E> expected = new ArrayList<>(initial);
            for (int index : new int[] {DEFAULT_SIZE - 1, DEFAULT_SIZE / 2, 0}) {
                assertEquals(expected.remove(index), list.remove(index));
                assertIterableEquals(expected, list);
            }
        } else {
            assertThrows(UnsupportedOperationException.class, () -> list.remove(0));
        }
    }

    /// Tests that [remove(int)][List#remove(int)] throws [IndexOutOfBoundsException] for an index below zero or not
    /// below the size, and leaves the list unchanged.
    @Test
    @DisplayName("remove(int) throws IndexOutOfBoundsException for a bad index")
    default void removeAtIndex_whenIndexIsInvalid_throwsIndexOutOfBoundsException() {
        if (supportsMethod(ListMethods.REMOVE_AT_INDEX)) {
            List<E> initial = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            L list = provider().createInstance(initial);
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(DEFAULT_SIZE));
            assertIterableEquals(initial, list);
        }
    }
}
