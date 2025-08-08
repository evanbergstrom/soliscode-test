package org.soliscode.test.contract;

import org.soliscode.test.OptionalMethod;
import org.soliscode.test.contract.collection.CollectionContract;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/// Values used to identify collection class methods for use with the [CollectionContract#supportsMethod(OptionalMethod)]
/// method.
///
/// @author evanbergstrom
/// @since 1.0
public enum CollectionMethods implements OptionalMethod {

    /// The option al method [Collection#add(Object)].
    Add,

    /// The option al method [Collection#addAll(Collection)].
    AddAll,

    /// The option al method [List#addAll(int, Collection)].
    AddAllAtIndex,

    /// The optional method [List#add(int, java.lang.Object)].
    AddAtIndex,

    /// The option al method [SequencedCollection#addFirst(Object)].
    AddFirst,

    /// The optional method [SequencedCollection#addLast(Object)].
    AddLast,

    /// The option al method [Collection#clear()].
    Clear,

    /// The option al method [Collection#containsAll(Collection)].
    ContainsAll,

    /// The method [List#get(int)].
    Get,

    /// The method [SequencedCollection#getFirst()].
    GetFirst,

    /// The method [SequencedCollection#getLast()].
    GetLast,

    /// The option al method [Iterator#forEachRemaining(Consumer)].
    IteratorForEachRemaining,

    /// The optional method [Iterator#remove()].
    IteratorRemove,

    /// The optional method [Collection#remove(Object)].
    Remove,

    /// The optional method [Collection#removeAll(Collection)].
    RemoveAll,

    /// The optional method [List#remove(int)].
    RemoveAtIndex,

    /// The option al method [SequencedCollection#removeFirst()].
    RemoveFirst,

    /// The optional method [Collection#removeIf(Predicate)].
    RemoveIf,

    /// The option al method [SequencedCollection#removeLast()].
    RemoveLast,

    /// The option al method [List#replaceAll()].
    ReplaceAll,

    /// The optional method [Collection#retainAll(Collection)].
    RetainAll,

    /// The method [SequencedCollection#reversed()].
    Reversed,

    /// The optional method [List#set].
    Set,

    /// The optional method [List#sort].
    Sort,

    /// The method [Collection#stream()].
    Stream
}
