package org.soliscode.test.contract.list;

import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;

/// This interface tests if a list class has implemented the [indexOf][List#indexOf(Object)] method correctly.
///
/// @param <E> The element type being tested.
/// @param <L> The list type being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see List#indexOf(Object)
public interface LastIndexOfContract<E, L extends List<E>> extends CollectionContractSupport<E, L> {

    // TODO: Implement tests for the List::indexOf methods.
}
