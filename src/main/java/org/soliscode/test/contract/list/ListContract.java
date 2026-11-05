package org.soliscode.test.contract.list;

import org.soliscode.test.contract.sequencedcollection.SequencedCollectionContract;

import java.util.List;

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
        RemoveAtPositionContract<L, E>,
        ReplaceAllContract<L, E>,
        SortContract<L, E>,
        SubListContract<L, E> {

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
