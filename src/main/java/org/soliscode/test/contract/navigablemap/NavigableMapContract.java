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
/// class MyNavigableMapContractTest implements NavigableMapContract<MyNavigableMap<String, String>, String, String> {
///     @Override
///     public MapProvider<MyNavigableMap<String, String>, String, String> objectProvider() {
///         return MyNavigableMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [NavigableMap] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <M> The map type being tested.
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @author evanbergstrom
/// @see NavigableMap
/// @since 1.0.0
public interface NavigableMapContract<M extends NavigableMap<K, V>, K, V> extends SortedMapContract<M, K, V>,
        LowerEntryContract<M, K, V>,
        LowerKeyContract<M, K, V>,
        FloorEntryContract<M, K, V>,
        FloorKeyContract<M, K, V>,
        CeilingEntryContract<M, K, V>,
        CeilingKeyContract<M, K, V>,
        HigherEntryContract<M, K, V>,
        HigherKeyContract<M, K, V>,
        DescendingMapContract<M, K, V>,
        NavigableKeySetContract<M, K, V>,
        DescendingKeySetContract<M, K, V>,
        SubMapFourArgContract<M, K, V>,
        HeadMapTwoArgContract<M, K, V>,
        TailMapTwoArgContract<M, K, V> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    @Override
    void doesNotSupportMethod(InterfaceMethod method);
}
