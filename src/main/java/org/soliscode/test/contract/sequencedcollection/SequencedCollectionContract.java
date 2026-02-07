package org.soliscode.test.contract.sequencedcollection;

import org.soliscode.test.contract.collection.CollectionContract;

import java.util.SequencedCollection;


/// Test suite for classes that implement the [SequencedCollection] interface.
///
/// @param <E> The element type being tested.s
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see SequencedCollection
/// @since 1.0
public interface SequencedCollectionContract<E, C extends SequencedCollection<E>>
        extends CollectionContract<E, C>,
        AddFirstContract<E, C>,
        AddLastContract<E, C>,
        GetFirstContract<E, C>,
        GetLastContract<E, C>,
        RemoveFirstContract<E, C>,
        RemoveLastContract<E, C>,
        ReversedContract<E, C> {


    /// This method can be called when testing collection classes that do not permit modification.
    default void doesNotSupportModification() {
        CollectionContract.super.doesNotSupportModification();
        doesNotSupportMethod(SequencedCollectionMethods.ADD_FIRST);
        doesNotSupportMethod(SequencedCollectionMethods.ADD_LAST);
        doesNotSupportMethod(SequencedCollectionMethods.REMOVE_FIRST);
        doesNotSupportMethod(SequencedCollectionMethods.REMOVE_LAST);
    }
}
