package org.soliscode.test.contract.map;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.object.ObjectContract;

import java.util.Map;

/// **Contract for the `Map` interface**
///
/// This contract interface provides a comprehensive suite of tests for implementations
/// of the [Map] interface. It is designed to be used as a mix-in interface by test
/// classes that verify map-based collections.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a map implementation correctly:
/// - Manages key-value mappings (put, get, remove, etc.).
/// - Reports its state correctly (size, isEmpty, containsKey, etc.).
/// - Provides consistent views of its contents (keySet, values, entrySet).
/// - Implements functional-style methods (forEach, compute, merge, etc.).
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyMapContractTest implements MapContract<String, String, MyMap<String, String>> {
///     @Override
///     public MapProvider<String, String, MyMap<String, String>> provider() {
///         return MyMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Map] and [org.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see Map
/// @since 1.0.0
public interface MapContract<K, V, M extends Map<K, V>> extends ObjectContract<M>,
        SizeContract<K, V, M>,
        IsEmptyContract<K, V, M>,
        ContainsKeyContract<K, V, M>,
        ContainsValueContract<K, V, M>,
        GetContract<K, V, M>,
        PutContract<K, V, M>,
        RemoveContract<K, V, M>,
        PutAllContract<K, V, M>,
        ClearContract<K, V, M>,
        KeySetContract<K, V, M>,
        ValuesContract<K, V, M>,
        EntrySetContract<K, V, M>,
        GetOrDefaultContract<K, V, M>,
        ForEachContract<K, V, M>,
        ReplaceAllContract<K, V, M>,
        PutIfAbsentContract<K, V, M>,
        RemoveTwoArgContract<K, V, M>,
        ReplaceTwoArgContract<K, V, M>,
        ReplaceThreeArgContract<K, V, M>,
        ComputeIfAbsentContract<K, V, M>,
        ComputeIfPresentContract<K, V, M>,
        ComputeContract<K, V, M>,
        MergeContract<K, V, M> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    /// Indicates that the map being tested does not support an optional method.
    ///
    /// @param method the method that is not supported
    /// @since 1.0.0
    void doesNotSupportMethod(InterfaceMethod method);
}
