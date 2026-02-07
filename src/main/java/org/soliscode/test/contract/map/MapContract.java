package org.soliscode.test.contract.map;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.object.ObjectContract;

import java.util.Map;

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

    boolean supportsMethod(InterfaceMethod method);

    /// Used to indicate that the class being tested does not support an optional method.
    /// @param method the method that the class being tested does not support.
    void doesNotSupportMethod(InterfaceMethod method);
}
