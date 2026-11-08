package org.soliscode.test.contract.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.object.ObjectMethods;
import org.soliscode.test.contract.sequencedcollection.SequencedCollectionContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Test suite for classes that implement the [List] interface. When implementing  this class, the only method that will
///  need to be implemented is [org.soliscode.test.contract.support.CollectionContractSupport#provider()].
///
/// @param <E> The type of the elements.
/// @param <L> The type of the list.
/// @author evanbergstrom
/// @since 1.0
public interface ListContract<L extends List<E>, E> extends SequencedCollectionContract<L, E>,
        ListAddContract<L, E>,
        ListToArrayContract<L, E>,
        GetContract<L, E>,
        SetContract<L, E>,
        IndexOfContract<L, E>,
        LastIndexOfContract<L, E>,
        AddAtPositionContract<L, E>,
        AddAllAtPositionContract<L, E>,
        RemoveAtPositionContract<L, E>,
        ReplaceAllContract<L, E>,
        SortContract<L, E>,
        SubListContract<L, E> {

    /// Tests that the list's spliterator reports [SIZED][Spliterator#SIZED] and [ORDERED][Spliterator#ORDERED], as
    /// [List#spliterator()] requires.
    @Test
    @DisplayName("spliterator() reports SIZED and ORDERED")
    default void spliterator_whenCalled_reportsSizedAndOrdered() {
        Spliterator<E> spliterator = provider().createInstanceWithUniqueElements().spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED), "SIZED");
        assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED), "ORDERED");
    }

    /// Tests that [equals(Object)][List#equals(Object)] is true for a list with the same elements in the same order
    /// and false for one in another order, and that [hashCode()][List#hashCode()] follows the formula
    /// [List#hashCode()] gives.
    @Test
    @DisplayName("equals(Object) and hashCode() follow the List definitions")
    default void equalsAndHashCode_whenCalled_followListDefinitions() {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        L list = provider().createInstance(values);
        if (supportsMethod(ObjectMethods.EQUALS)) {
            List<E> reversed = new ArrayList<>(values);
            Collections.reverse(reversed);
            // Both ways: equality across List implementations must be symmetric.
            assertEquals(new ArrayList<>(values), list);
            assertEquals(list, new ArrayList<>(values));
            assertNotEquals(reversed, list);
            assertNotEquals(list, reversed);
        }
        if (supportsMethod(ObjectMethods.HASH_CODE)) {
            int expected = 1;
            for (E e : values) {
                expected = 31 * expected + Objects.hashCode(e);
            }
            assertEquals(expected, list.hashCode());
        }
    }

    default void doesNotSupportModification() {
        SequencedCollectionContract.super.doesNotSupportModification();
        doesNotSupportMethod(ListMethods.ADD_AT_INDEX);
        doesNotSupportMethod(ListMethods.ADD_ALL_AT_INDEX);
        doesNotSupportMethod(ListMethods.REMOVE_AT_INDEX);
        doesNotSupportMethod(ListMethods.REPLACE_ALL);
        doesNotSupportMethod(ListMethods.SET);
        doesNotSupportMethod(ListMethods.SORT);
        doesNotSupportMethod(ListMethods.SUB_LIST);
    }
}
