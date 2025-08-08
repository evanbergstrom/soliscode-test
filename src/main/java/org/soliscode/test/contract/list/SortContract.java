package org.soliscode.test.contract.list;

import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Comparator;
import java.util.List;

/// This interface tests if a list class has implemented the [sort][List#sort(Comparator)] method correctly.
///
/// @param <E> The element type being tested.
/// @param <L> The list type being tested.
/// @author evanbergstrom
/// @since 1.0
/// @see List#sort(Comparator)
public interface SortContract<E, L extends List<E>> extends CollectionContractSupport<E, L> {

    // TODO: Implement tests for the List::sort method.
}
