package org.soliscode.test.contract.sortedmap;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.sequencedmap.SequencedMapContract;

import java.util.SortedMap;

/// Contract for the [SortedMap] interface.
///
/// This contract extends [SequencedMapContract] and adds tests for the methods defined in [SortedMap].
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface SortedMapContract<K, V, M extends SortedMap<K, V>> extends SequencedMapContract<K, V, M>,
        ComparatorContract<K, V, M>,
        SubMapContract<K, V, M>,
        HeadMapContract<K, V, M>,
        TailMapContract<K, V, M>,
        FirstKeyContract<K, V, M>,
        LastKeyContract<K, V, M>,
        SortedPutFirstContract<K, V, M>,
        SortedPutLastContract<K, V, M>,
        SortedReversedContract<K, V, M> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    @Override
    void doesNotSupportMethod(InterfaceMethod method);
}
