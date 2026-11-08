package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Contract for the [sort(Comparator)][List#sort(Comparator)] method of the [List] interface.
///
/// @param <L> The type of the list
/// @param <E> The element type for the list
/// @author evanbergstrom
/// @since 1.0
/// @see List#sort(Comparator)
/// @see ListContract
public interface SortContract<L extends List<E>, E> extends CollectionContractSupport<L, E> {

    /// Tests that [sort(Comparator)][List#sort(Comparator)] puts the elements in the comparator's order.
    @Test
    @DisplayName("sort(Comparator) orders the elements by the comparator")
    default void sort_withComparator_ordersElements() {
        List<E> ordered = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Comparator<E> byPosition = Comparator.comparingInt(ordered::indexOf);
        List<E> shuffled = new ArrayList<>(ordered);
        Collections.reverse(shuffled);
        Collections.rotate(shuffled, DEFAULT_SIZE / 3);
        L list = provider().createInstance(shuffled);
        if (supportsMethod(ListMethods.SORT)) {
            list.sort(byPosition);
            assertIterableEquals(ordered, list);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> list.sort(byPosition));
        }
    }

    /// Tests that [sort(Comparator)][List#sort(Comparator)] with a null comparator orders [Comparable] elements by
    /// their natural order. Elements that are not [Comparable] are not tested.
    @Test
    @DisplayName("sort(null) orders comparable elements by their natural order")
    @SuppressWarnings({"unchecked", "rawtypes"})
    default void sort_withNullComparator_ordersByNaturalOrder() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        if (supportsMethod(ListMethods.SORT) && values.getFirst() instanceof Comparable) {
            List<E> shuffled = new ArrayList<>(values);
            Collections.rotate(shuffled, DEFAULT_SIZE / 3);
            L list = provider().createInstance(shuffled);
            List expected = new ArrayList<>(values);
            Collections.sort(expected);
            list.sort(null);
            assertIterableEquals(expected, list);
        }
    }
}
