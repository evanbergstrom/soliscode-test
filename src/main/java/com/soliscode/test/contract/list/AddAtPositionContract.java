package com.soliscode.test.contract.list;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionContractSupport;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [add(int, Object)][List#add(int, Object)] method of the [List] interface.
///
/// @param <L> The type of the list
/// @param <E> The element type for the list
/// @author evanbergstrom
/// @since 1.0
/// @see List#add(int, Object)
/// @see ListContract
public interface AddAtPositionContract<L extends List<E>, E> extends CollectionContractSupport<L, E> {

    /// Tests that [add(int, Object)][List#add(int, Object)] inserts the element at the index, at the start, the
    /// middle and the end, and shifts the later elements up.
    @Test
    @DisplayName("add(int, E) inserts the element at the index")
    default void addAtIndexWhenIndexIsValidInsertsAtIndex() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE + 3);
        List<E> initial = values.subList(0, DEFAULT_SIZE);
        L list = provider().createInstance(initial);
        if (supportsMethod(ListMethods.ADD_AT_INDEX)) {
            List<E> expected = new ArrayList<>(initial);
            int[] indices = {0, DEFAULT_SIZE / 2, DEFAULT_SIZE + 2};
            for (int i = 0; i < indices.length; i++) {
                E e = values.get(DEFAULT_SIZE + i);
                expected.add(indices[i], e);
                list.add(indices[i], e);
                assertIterableEquals(expected, list);
            }
        } else {
            assertThrows(UnsupportedOperationException.class, () -> list.add(0, values.getLast()));
        }
    }

    /// Tests that [add(int, Object)][List#add(int, Object)] throws [IndexOutOfBoundsException] for an index below
    /// zero or above the size, and leaves the list unchanged.
    @Test
    @DisplayName("add(int, E) throws IndexOutOfBoundsException for a bad index")
    default void addAtIndexWhenIndexIsInvalidThrowsIndexOutOfBoundsException() {
        if (supportsMethod(ListMethods.ADD_AT_INDEX)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE + 1);
            List<E> initial = values.subList(0, DEFAULT_SIZE);
            L list = provider().createInstance(initial);
            E e = values.getLast();
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, e));
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(DEFAULT_SIZE + 1, e));
            assertIterableEquals(initial, list);
        }
    }
}
