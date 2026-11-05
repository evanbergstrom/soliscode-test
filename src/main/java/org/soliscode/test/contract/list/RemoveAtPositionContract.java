package org.soliscode.test.contract.list;

import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;

/// This interface tests if a list class has implemented the [remove][List#remove(int)] method correctly.
///
/// @param <E> The element type being tested.
/// @param <L> The list type being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see List#sort(java.util.Comparator)
public interface RemoveAtPositionContract<L extends List<E>, E> extends CollectionContractSupport<L, E> {

    // TODO: Implement tests for the List::remove(int) method.
}
