package org.soliscode.test.contract.list;

import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.sequenced.SequencedCollectionContract;

import java.util.List;

/// Test suite for classes that implement the [List] interface. When implementing  this class, the only method that will
///  need to be implemented is [org.soliscode.test.contract.support.CollectionContractSupport#collectionProvider()].
///
/// @param <E> The type of the elements.
/// @param <L> The type of the list.
/// @author evanbergstrom
/// @since 1.0
public interface ListContract<E, L extends List<E>> extends SequencedCollectionContract<E, L>,
        ListAddContract<E, L>,
        ListToArrayContract<E, L>,
        GetContract<E, L>,
        SetContract<E, L>,
        IndexOfContract<E, L>,
        LastIndexOfContract<E, L>,
        AddAtPositionContract<E, L>,
        RemoveAtPositionContract<E, L>,
        ReplaceAllContract<E, L>,
        SortContract<E, L>,
        SubListContract<E, L> {

    default void doesNotSupportModification() {
        SequencedCollectionContract.super.doesNotSupportModification();
        doesNotSupportMethod(CollectionMethods.Set);
        doesNotSupportMethod(CollectionMethods.AddAtIndex);
        doesNotSupportMethod(CollectionMethods.RemoveAtIndex);
        doesNotSupportMethod(CollectionMethods.Sort);
        doesNotSupportMethod(CollectionMethods.RetainAll);
    }
}
