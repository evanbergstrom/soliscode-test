package org.soliscode.test.contract;

import org.soliscode.test.OptionalMethod;

/// Values used to identify collection class methods for use with the
/// [org.soliscode.test.contract.collection.CollectionContract#supportsMethod(OptionalMethod)] method.
///
/// @author evanbergstrom
/// @since 1.0
public enum CollectionMethods implements OptionalMethod {

    /// The option al method [java.util.Collection#add(Object)].
    Add,

    /// The option al method [java.util.Collection#addAll(java.util.Collection)].
    AddAll,

    /// The option al method [java.util.List#addAll(int, java.util.Collection)].
    AddAllAtIndex,

    /// The optional method [java.util.List#add(int, java.lang.Object)].
    AddAtIndex,

    /// The option al method [java.util.SequencedCollection#addFirst(Object)].
    AddFirst,

    /// The optional method [java.util.SequencedCollection#addLast(Object)].
    AddLast,

    /// The option al method [java.util.Collection#clear()].
    Clear,

    /// The option al method [java.util.Collection#containsAll(java.util.Collection)].
    ContainsAll,

    /// The method [java.util.List#get(int)].
    Get,

    /// The method [java.util.SequencedCollection#getFirst()].
    GetFirst,

    /// The method [java.util.SequencedCollection#getLast()].
    GetLast,

    /// The option al method [java.util.Iterator#forEachRemaining(java.util.function.Consumer)].
    IteratorForEachRemaining,

    /// The optional method [java.util.Iterator#remove()].
    IteratorRemove,

    /// The optional method [java.util.Collection#remove(Object)].
    Remove,

    /// The optional method [java.util.Collection#removeAll(java.util.Collection)].
    RemoveAll,

    /// The optional method [java.util.List#remove(int)].
    RemoveAtIndex,

    /// The option al method [java.util.SequencedCollection#removeFirst()].
    RemoveFirst,

    /// The optional method [java.util.Collection#removeIf(java.util.function.Predicate)].
    RemoveIf,

    /// The option al method [java.util.SequencedCollection#removeLast()].
    RemoveLast,

    /// The option al method [java.util.List#replaceAll(java.util.function.UnaryOperator)].
    ReplaceAll,

    /// The optional method [java.util.Collection#retainAll(java.util.Collection)].
    RetainAll,

    /// The method [java.util.SequencedCollection#reversed()].
    Reversed,

    /// The optional method [java.util.List#set].
    Set,

    /// The optional method [java.util.List#sort].
    Sort,

    /// The method [java.util.Collection#stream()].
    Stream
}
