package org.soliscode.test.contract.sequencedcollection;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

/// Values used to identify [java.util.SequencedCollection] methods for use with the
/// [org.soliscode.test.contract.support.ContractSupport#supportsMethod] method.
///
/// @author evanbergstrom
/// @since 1.0.0
public enum SequencedCollectionMethods implements InterfaceMethod {

    /// The optional method [java.util.SequencedCollection#addFirst(Object)].
    ADD_FIRST("addFirst(Object)"),

    /// The optional method [java.util.SequencedCollection#addLast(Object)].
    ADD_LAST("addLast(Object)"),

    /// The method [java.util.SequencedCollection#getFirst()].
    GET_FIRST("getFirst)"),

    /// The method [java.util.SequencedCollection#getLast()].
    GET_LAST("getlast()"),

    /// The optional method [java.util.SequencedCollection#removeFirst()].
    REMOVE_FIRST("removeFirst()"),

    /// The optional method [java.util.SequencedCollection#removeLast()].
    REMOVE_LAST("removeLast()"),

    /// The method [java.util.SequencedCollection#reversed()].
    REVERSED("reversed()");

    private final String name;

    SequencedCollectionMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }
}
