package org.soliscode.test.contract.queue;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;

/// Values used to identify queue class methods for use with the
/// [org.soliscode.test.contract.queue.QueueContract#supportsMethod(InterfaceMethod)] method.
///
/// ## Purpose
/// This enum provides a standardized way to reference [java.util.Queue] methods within the
/// contract testing framework, allowing tests to selectively enable or disable checks
/// based on which optional methods a specific implementation supports.
///
/// ## Usage Examples
/// ```java
/// // Checking if the poll() method is supported in a contract test
/// if (supportsMethod(QueueMethods.POLL)) {
///     // perform poll tests
/// }
/// ```
///
/// ## Thread Safety
/// This enum is immutable and thread-safe.
///
/// @author evanbergstrom
/// @since 1.0
public enum QueueMethods implements InterfaceMethod {

    /// The method [java.util.Queue#offer(Object)].
    OFFER("offer(Object)"),

    /// The method [java.util.Queue#remove()].
    REMOVE("remove()"),

    /// The method [java.util.Queue#poll()].
    POLL("poll()"),

    /// The method [java.util.Queue#element()].
    ELEMENT("element()"),

    /// The method [java.util.Queue#peek()].
    PEEK("peek()");

    private final String name;

    QueueMethods(final @NonNull String name) {
        this.name = name;
    }

    /// {@inheritDoc}
    @Override
    public @NonNull String methodName() {
        return name;
    }
}
