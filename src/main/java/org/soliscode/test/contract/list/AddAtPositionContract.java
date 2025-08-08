package org.soliscode.test.contract.list;

import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;

/// Contract for testing the [add][List#add(int, java.lang.Object)] method in the [List] interface.
///
/// @param <E> The element type for the list
/// @param <L> The type of the list
/// @author evanbergstrom
/// @since 1.0
/// @see List#add(int, java.lang.Object)
/// @see ListContract
public interface AddAtPositionContract<E, L extends List<E>> extends CollectionContractSupport<E, L> {
}
