package org.soliscode.test.contract.transferqueue;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

/// Values used to identify [java.util.concurrent.TransferQueue] methods for use with the
/// [org.soliscode.test.SupportedMethods#supportsMethod(InterfaceMethod)] method.
///
/// This enum contains methods defined in the `TransferQueue` interface that are not
/// present in the `Queue` or `BlockingQueue` interfaces.
///
/// @author evanbergstrom
/// @since 1.0
public enum TransferQueueMethods implements InterfaceMethod {

    /// The method [java.util.concurrent.TransferQueue#tryTransfer(Object)].
    TRY_TRANSFER("tryTransfer(E)"),

    /// The method [java.util.concurrent.TransferQueue#transfer(Object)].
    TRANSFER("transfer(E)"),

    /// The method [java.util.concurrent.TransferQueue#tryTransfer(Object, long, java.util.concurrent.TimeUnit)].
    TRY_TRANSFER_TIMEOUT("tryTransfer(E, long, TimeUnit)"),

    /// The method [java.util.concurrent.TransferQueue#hasWaitingConsumer()].
    HAS_WAITING_CONSUMER("hasWaitingConsumer()"),

    /// The method [java.util.concurrent.TransferQueue#getWaitingConsumerCount()].
    GET_WAITING_CONSUMER_COUNT("getWaitingConsumerCount()");

    private final String name;

    TransferQueueMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
