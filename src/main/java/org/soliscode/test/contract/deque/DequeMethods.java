package org.soliscode.test.contract.deque;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

/// This enumeration represents a collection of methods available in the [java.util.Deque] interface.
/// Each constant maps to a specific method in the Deque API. It is primarily used to facilitate
/// testing and ensure contract compliance in test setups.
///
/// **Usage in Testing:**
/// The constants in this enumeration are utilized in contract testing to identify which methods
/// are supported by a specific implementation of [java.util.Deque]. Methods in this enumeration
/// are tested through interfaces such as `PopContract` and `PushContract`.
///
/// This enum implements the [InterfaceMethod] interface, mandating the implementation
/// of [InterfaceMethod#methodName()] to retrieve the method name.
///
/// @see java.util.Deque
///
/// @author Evan Bergstrom
/// @since 1.0.0
public enum DequeMethods implements InterfaceMethod {

    /// The method [java.util.Deque#addLast(Object)].
    ADD_LAST("addLast(Object)"),

    /// The method [java.util.Deque#offerFirst(Object)].
    OFFER_FIRST("offerFirst(Object)"),

    /// The method [java.util.Deque#offerLast(Object)].
    OFFER_LAST("offerLast(Object)"),

    /// The method [java.util.Deque#pollFirst()].
    POLL_FIRST("pollFirst()"),

    /// The method [java.util.Deque#pollLast()].
    POLL_LAST("pollLast()"),

    /// The method [java.util.Deque#getFirst()].
    GET_FIRST("getFirst()"),

    /// The method [java.util.Deque#getLast()].
    GET_LAST("getLast()"),

    /// The method [java.util.Deque#peekFirst].
    PEEK_FIRST("peekFirst()"),

    /// The method [java.util.Deque#peekLast].
    PEEK_LAST("peekLast()"),

    /// The method [java.util.Deque#removeFirst()].
    REMOVE_FIRST("removeFirst()"),

    /// The method [java.util.Deque#removeLast()].
    REMOVE_LAST("removeLast()"),

    /// The method [java.util.Deque#removeFirstOccurrence(Object)].
    REMOVE_FIRST_OCCURRENCES("removeFirstOccurrence(Object)"),

    /// The method [java.util.Deque#removeLastOccurrence(Object)].
    REMOVE_LAST_OCCURRENCES("removeLastOccurrence(Object)"),

    /// The method [java.util.Deque#push(Object)].
    PUSH("push(Object)"),

    /// The method [java.util.Deque#pop()].
    POP("pop()"),

    /// The method [java.util.Deque#descendingIterator()].
    DESCENDING_ITERATOR("descendingIterator()");

    private final String name;

    DequeMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }
}
