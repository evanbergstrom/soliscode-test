package org.soliscode.test.contract.navigablemap;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.sortedmap.SortedMapContract;

import java.util.NavigableMap;

/// Contract for the [NavigableMap] interface.
///
/// This contract extends [SortedMapContract] and adds tests for the methods defined in [NavigableMap].
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface NavigableMapContract<K, V, M extends NavigableMap<K, V>> extends SortedMapContract<K, V, M>,
        LowerEntryContract<K, V, M>,
        LowerKeyContract<K, V, M>,
        FloorEntryContract<K, V, M>,
        FloorKeyContract<K, V, M>,
        CeilingEntryContract<K, V, M>,
        CeilingKeyContract<K, V, M>,
        HigherEntryContract<K, V, M>,
        HigherKeyContract<K, V, M>,
        DescendingMapContract<K, V, M>,
        NavigableKeySetContract<K, V, M>,
        DescendingKeySetContract<K, V, M>,
        SubMapFourArgContract<K, V, M>,
        HeadMapTwoArgContract<K, V, M>,
        TailMapTwoArgContract<K, V, M> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    @Override
    void doesNotSupportMethod(InterfaceMethod method);
}
