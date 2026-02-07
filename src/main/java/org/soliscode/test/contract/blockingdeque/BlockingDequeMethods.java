package org.soliscode.test.contract.blockingdeque;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

/// Values used to identify blocking deque class methods for use with the
/// [org.soliscode.test.contract.collection.CollectionContract#supportsMethod(InterfaceMethod)] method.
///
/// @author evanbergstrom
/// @since 1.0
public enum BlockingDequeMethods implements InterfaceMethod {

    /// The method [java.util.concurrent.BlockingDeque#putFirst(Object)].
    PUT_FIRST("putFirst(Object)"),

    /// The method [java.util.concurrent.BlockingDeque#putLast(Object)].
    PUT_LAST("putLast(Object)"),

    /// The method [java.util.concurrent.BlockingDeque#takeFirst()].
    TAKE_FIRST("takeFirst()"),

    /// The method [java.util.concurrent.BlockingDeque#takeLast()].
    TAKE_LAST("takeLast()"),

    /// The method [java.util.concurrent.BlockingDeque#offerFirst(Object, long, java.util.concurrent.TimeUnit)].
    OFFER_FIRST_TIMEOUT("offerFirst(Object, long, TimeUnit)"),

    /// The method [java.util.concurrent.BlockingDeque#offerLast(Object, long, java.util.concurrent.TimeUnit)].
    OFFER_LAST_TIMEOUT("offerLast(Object, long, TimeUnit)"),

    /// The method [java.util.concurrent.BlockingDeque#pollFirst(long, java.util.concurrent.TimeUnit)].
    POLL_FIRST_TIMEOUT("pollFirst(long, TimeUnit)"),

    /// The method [java.util.concurrent.BlockingDeque#pollLast(long, java.util.concurrent.TimeUnit)].
    POLL_LAST_TIMEOUT("pollLast(long, TimeUnit)");

    private final String name;
    
    BlockingDequeMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }
}
