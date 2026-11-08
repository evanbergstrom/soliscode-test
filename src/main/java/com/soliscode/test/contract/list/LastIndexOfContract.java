package com.soliscode.test.contract.list;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [lastIndexOf(Object)][List#lastIndexOf(Object)] method of the [List] interface.
///
/// @param <L> The type of the list
/// @param <E> The element type for the list
/// @author evanbergstrom
/// @since 1.0
/// @see List#lastIndexOf(Object)
/// @see ListContract
public interface LastIndexOfContract<L extends List<E>, E> extends CollectionContractSupport<L, E> {

    /// Tests that [lastIndexOf(Object)][List#lastIndexOf(Object)] returns the index of the last occurrence of an
    /// element, and -1 for an element not in the list.
    @Test
    @DisplayName("lastIndexOf(Object) returns the index of the last occurrence")
    default void lastIndexOfWhenCalledReturnsLastIndex() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE + 1);
        List<E> unique = values.subList(0, DEFAULT_SIZE);
        if (permitDuplicates()) {
            List<E> twice = new ArrayList<>(unique);
            twice.addAll(unique);
            L list = provider().createInstance(twice);
            for (int i = 0; i < DEFAULT_SIZE; i++) {
                assertEquals(DEFAULT_SIZE + i, list.lastIndexOf(unique.get(i)));
            }
        } else {
            L list = provider().createInstance(unique);
            for (int i = 0; i < DEFAULT_SIZE; i++) {
                assertEquals(i, list.lastIndexOf(unique.get(i)));
            }
        }
        assertEquals(-1, provider().createInstance(unique).lastIndexOf(values.getLast()));
    }
}
