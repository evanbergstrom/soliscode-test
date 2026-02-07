package org.soliscode.test.contract.deque;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

public enum DequeMethods implements InterfaceMethod {

    /// The method [java.util.Deque#offerFirst(Object)].
    OFFER_FIRST("offerFirst(Object)"),

    /// The method [java.util.Deque#offerLast(Object)].
    OFFER_LAST("offerLast(Object)"),

    /// The method [java.util.Deque#pollFirst()].
    POLL_FIRST("pollFirst()"),

    /// The method [java.util.Deque#pollLast()].
    POLL_LAST("pollLast()"),

    /// The method [java.util.Deque#peekFirst].
    PEEK_FIRST("peekFirst()"),

    /// The method [java.util.Deque#peekLast].
    PEEK_LAST("peekLast()"),

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