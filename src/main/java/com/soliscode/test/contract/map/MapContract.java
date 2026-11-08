package com.soliscode.test.contract.map;

import java.util.Map;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.contract.object.ObjectContract;

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
///     public MapProvider<String, String, MyMap<String, String>> objectProvider() {
///         return MyMap::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Map] and [com.soliscode.test.provider.MapProvider] implementations being tested.
///
/// @param <K> The key type being tested.
/// @param <V> The value type being tested.
/// @param <M> The map type being tested.
/// @author evanbergstrom
/// @see Map
/// @since 1.0.0
public interface MapContract<M extends Map<K, V>, K, V> extends ObjectContract<M>,
        SizeContract<M, K, V>,
        IsEmptyContract<M, K, V>,
        ContainsKeyContract<M, K, V>,
        ContainsValueContract<M, K, V>,
        GetContract<M, K, V>,
        PutContract<M, K, V>,
        RemoveContract<M, K, V>,
        PutAllContract<M, K, V>,
        ClearContract<M, K, V>,
        KeySetContract<M, K, V>,
        ValuesContract<M, K, V>,
        EntrySetContract<M, K, V>,
        GetOrDefaultContract<M, K, V>,
        ForEachContract<M, K, V>,
        ReplaceAllContract<M, K, V>,
        PutIfAbsentContract<M, K, V>,
        RemoveTwoArgContract<M, K, V>,
        ReplaceTwoArgContract<M, K, V>,
        ReplaceThreeArgContract<M, K, V>,
        ComputeIfAbsentContract<M, K, V>,
        ComputeIfPresentContract<M, K, V>,
        ComputeContract<M, K, V>,
        MergeContract<M, K, V> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    /// Indicates that the map being tested does not support an optional method.
    ///
    /// @param method the method that is not supported
    /// @since 1.0.0
    void doesNotSupportMethod(InterfaceMethod method);
}
