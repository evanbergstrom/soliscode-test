package org.soliscode.test.contract.navigablemap;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.sortedmap.SortedMapContract;

import java.util.NavigableMap;

/// **Contract for the `NavigableMap` interface**
///
/// This contract extends [SortedMapContract] and adds tests for the methods defined in [NavigableMap].
/// It is designed to be used as a mix-in interface by test classes that verify [NavigableMap]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a navigable map implementation correctly:
/// - Implements all navigation methods (`lowerEntry`, `floorEntry`, etc.).
/// - Correctly implements view methods (`descendingMap`, `navigableKeySet`, etc.).
/// - Inherits all tests from [SortedMapContract].
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyNavigableMapContractTest implements NavigableMapContract<String, String, MyNavigableMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MyNavigableMap<String, String>> provider() {
///         return MyNavigableMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [NavigableMap] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see NavigableMap
/// @since 1.0.0
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
