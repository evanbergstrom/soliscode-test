package org.soliscode.test.contract.sortedmap;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.sequencedmap.SequencedMapContract;

import java.util.SortedMap;

/// **Contract for the `SortedMap` interface**
///
/// This contract extends [SequencedMapContract] and adds tests for the methods defined in [SortedMap].
/// It is designed to be used as a mix-in interface by test classes that verify [SortedMap]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a sorted map implementation correctly:
/// - Implements all navigation methods (`firstKey`, `lastKey`, etc.).
/// - Correctly implements view methods (`subMap`, `headMap`, `tailMap`).
/// - Handles `comparator` and `reversed` views correctly.
/// - Inherits all tests from [SequencedMapContract].
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MySortedMapContractTest implements SortedMapContract<String, String, MySortedMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MySortedMap<String, String>> provider() {
///         return MySortedMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [SortedMap] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see SortedMap
/// @since 1.0.0
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
