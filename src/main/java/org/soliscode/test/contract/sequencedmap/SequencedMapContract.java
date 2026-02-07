package org.soliscode.test.contract.sequencedmap;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.map.MapContract;

import java.util.SequencedMap;

/// Contract for the [SequencedMap] interface.
///
/// This contract extends [MapContract] and adds tests for the methods defined in [SequencedMap].
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface SequencedMapContract<K, V, M extends SequencedMap<K, V>> extends MapContract<K, V, M>,
        PutFirstContract<K, V, M>,
        PutLastContract<K, V, M>,
        ReversedContract<K, V, M>,
        FirstEntryContract<K, V, M>,
        LastEntryContract<K, V, M>,
        PollFirstEntryContract<K, V, M>,
        PollLastEntryContract<K, V, M>,
        SequencedKeySetContract<K, V, M>,
        SequencedValuesContract<K, V, M>,
        SequencedEntrySetContract<K, V, M> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    @Override
    void doesNotSupportMethod(InterfaceMethod method);
}
