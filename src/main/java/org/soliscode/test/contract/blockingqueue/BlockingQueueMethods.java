package org.soliscode.test.contract.blockingqueue;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

/// Values used to identify blocking queue class methods for use with the
/// [org.soliscode.test.contract.collection.CollectionContract#supportsMethod(InterfaceMethod)] method.
///
/// @author evanbergstrom
/// @since 1.0
public enum BlockingQueueMethods implements InterfaceMethod {

    /// The method [java.util.concurrent.BlockingQueue#put(Object)].
    PUT("put(Object)"),

    /// The method [java.util.concurrent.BlockingQueue#take()].
    TAKE("take()"),

    /// The method [java.util.concurrent.BlockingQueue#offer(Object, long, java.util.concurrent.TimeUnit)].
    OFFER_TIMEOUT("offer(Object, long, TimeUnit)"),

    /// The method [java.util.concurrent.BlockingQueue#poll(long, java.util.concurrent.TimeUnit)].
    POLL_TIMEOUT("poll(long, TimeUnit)"),

    /// The method [java.util.concurrent.BlockingQueue#remainingCapacity()].
    REMAINING_CAPACITY("remainingCapacity()"),

    /// The method [java.util.concurrent.BlockingQueue#remove(Object)].
    REMOVE_OBJECT("remove(Object)"),

    /// The method [java.util.concurrent.BlockingQueue#drainTo(java.util.Collection)].
    DRAIN_TO("drainTo(Collection)"),

    /// The method [java.util.concurrent.BlockingQueue#drainTo(java.util.Collection, int)].
    DRAIN_TO_MAX_ELEMENTS("drainTo(Collection, int)");

    private final String name;

    BlockingQueueMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }
}
