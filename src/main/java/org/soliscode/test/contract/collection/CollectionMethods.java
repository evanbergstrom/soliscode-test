package org.soliscode.test.contract.collection;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

/// Values used to identify collection class methods for use with the
/// [org.soliscode.test.contract.collection.CollectionContract#supportsMethod(InterfaceMethod)] method.
///
/// @author evanbergstrom
/// @since 1.0
public enum CollectionMethods implements InterfaceMethod {

    /// The option al method [java.util.Collection#add(Object)].
    ADD("add_singleElement_returnsTrueAndUpdatesSize(Object)"),

    /// The option al method [java.util.Collection#addAll(java.util.Collection)].
    ADD_ALL("addAll(Collection)"),

    /// The option al method [java.util.Collection#clear()].
    CLEAR("clear()"),

    /// The optional method [java.util.Collection#contains(java.lang.Object)].
    CONTAINS("contains(Object)"),

    /// The option al method [java.util.Collection#containsAll(java.util.Collection)].
    CONTAINS_ALL("containsAll(Collection)"),

    /// The method [java.util.SequencedCollection#isEmpty()].
    IS_EMPTY("isEmpty()"),

    /// The method [java.util.Collection#remove(Object)].
    REMOVE("remove(Object)"),

    /// The method [java.util.Collection#removeAll(java.util.Collection)].
    REMOVE_ALL("removeAll(Collection)"),

    /// The method [java.util.Collection#removeIf(java.util.function.Predicate)].
    REMOVE_IF("remnoveIf(Predicate)"),

    /// The method [java.util.Collection#retainAll(java.util.Collection)].
    RETAIN_ALL("retainAll(Collection)"),

    /// The method [java.util.Collection#size()].
    SIZE("size()"),

    /// The method [java.util.Collection#stream()].
    STREAM("stream()");

    private final String methodName;

    CollectionMethods(final @NonNull String methodName) {
        this.methodName = methodName;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return methodName;
    }
}
