package com.soliscode.test.contract.list;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [addAll(int, Collection)][List#addAll(int, java.util.Collection)] method of the [List]
/// interface.
///
/// @param <L> The type of the list
/// @param <E> The element type for the list
/// @since 1.0
/// @see List#addAll(int, java.util.Collection)
/// @see ListContract
public interface AddAllAtPositionContract<L extends List<E>, E> extends CollectionContractSupport<L, E> {

    /// Tests that [addAll(int, Collection)][List#addAll(int, java.util.Collection)] inserts the elements, in order,
    /// at the index, at the start, the middle and the end, and returns `true`.
    @Test
    @DisplayName("addAll(int, Collection) inserts the elements at the index")
    default void addAllAtIndexWhenIndexIsValidInsertsAtIndex() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE + 6);
        List<E> initial = values.subList(0, DEFAULT_SIZE);
        L list = provider().createInstance(initial);
        if (supportsMethod(ListMethods.ADD_ALL_AT_INDEX)) {
            List<E> expected = new ArrayList<>(initial);
            int[] indices = {0, DEFAULT_SIZE / 2, DEFAULT_SIZE + 4};
            for (int i = 0; i < indices.length; i++) {
                List<E> more = values.subList(DEFAULT_SIZE + 2 * i, DEFAULT_SIZE + 2 * i + 2);
                expected.addAll(indices[i], more);
                assertTrue(list.addAll(indices[i], more));
                assertIterableEquals(expected, list);
            }
        } else {
            assertThrows(UnsupportedOperationException.class, () -> list.addAll(0, values.subList(0, 1)));
        }
    }

    /// Tests that [addAll(int, Collection)][List#addAll(int, java.util.Collection)] returns `false`, and leaves the
    /// list unchanged, for an empty collection.
    @Test
    @DisplayName("addAll(int, Collection) returns false for an empty collection")
    default void addAllAtIndexWithEmptyCollectionReturnsFalse() {
        if (supportsMethod(ListMethods.ADD_ALL_AT_INDEX)) {
            List<E> initial = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            L list = provider().createInstance(initial);
            assertFalse(list.addAll(1, new ArrayList<>()));
            assertIterableEquals(initial, list);
        }
    }

    /// Tests that [addAll(int, Collection)][List#addAll(int, java.util.Collection)] throws
    /// [IndexOutOfBoundsException] for a bad index and [NullPointerException] for a null collection.
    @Test
    @DisplayName("addAll(int, Collection) throws for a bad index or a null collection")
    default void addAllAtIndexWithBadArgumentsThrows() {
        if (supportsMethod(ListMethods.ADD_ALL_AT_INDEX)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE + 1);
            List<E> initial = values.subList(0, DEFAULT_SIZE);
            L list = provider().createInstance(initial);
            List<E> more = values.subList(DEFAULT_SIZE, DEFAULT_SIZE + 1);
            assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(-1, more));
            assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(DEFAULT_SIZE + 1, more));
            assertThrows(NullPointerException.class, () -> list.addAll(0, null));
            assertIterableEquals(initial, list);
        }
    }

    /// Tests that [addAll(int, Collection)][List#addAll(int, java.util.Collection)] throws
    /// [UnsupportedOperationException] when it is not supported.
    @Test
    @DisplayName("addAll(int, Collection) throws UnsupportedOperationException when not supported")
    default void addAllAtIndexWhenNotSupportedThrowsUnsupportedOperationException() {
        if (!supportsMethod(ListMethods.ADD_ALL_AT_INDEX)) {
            L list = provider().createInstanceWithUniqueElements();
            List<E> more = elementProvider().createUniqueInstances(1);
            assertThrows(UnsupportedOperationException.class, () -> list.addAll(0, more));
        }
    }
}
