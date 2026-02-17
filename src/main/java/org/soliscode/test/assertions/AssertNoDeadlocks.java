package org.soliscode.test.assertions;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/// **Deadlock Detection Assertions**
///
/// This class provides utility methods to detect deadlocked threads within the JVM.
/// It uses the [ThreadMXBean] to find threads that are in a deadlock cycle waiting
/// for object monitors or ownable synchronizers.
///
/// ## Purpose
/// In multi-threaded tests, deadlocks can cause tests to hang indefinitely. This assertion
/// allows for explicit verification that no deadlocks have occurred, providing detailed
/// information about the threads involved if a deadlock is detected.
///
/// ## Implementation Details
/// The detection is performed using [ThreadMXBean#findDeadlockedThreads()]. If deadlocks
/// are found, an [AssertionError] is thrown containing:
/// - Thread Name
/// - Thread ID
/// - Thread State
/// - Lock Name
/// - Lock Owner Name
///
/// Note: Deadlock detection can be an expensive operation and should typically be used
/// at strategic points in concurrency tests, such as after a contention period or during
/// cleanup.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see ThreadMXBean
public final class AssertNoDeadlocks {

    private AssertNoDeadlocks() {
    }

    /// **Enables Thread Contention Monitoring**
    ///
    /// This method enables thread contention monitoring in the JVM, which is required
    /// for accurate deadlock detection using [ThreadMXBean#findDeadlockedThreads()].
    ///
    /// ## Effect
    /// Sets `ThreadMXBean.setThreadContentionMonitoringEnabled(true)`. If the JVM does
    /// not support or fails to enable this feature, an [AssertionError] is thrown.
    ///
    /// ## Use Case
    /// Call this method at the beginning of a test suite or specific test case that
    /// requires deadlock detection.
    ///
    /// @throws AssertionError if thread contention monitoring cannot be enabled
    /// @since 1.0.0
    public static void enableDeadlockDetection() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        if (!threadMXBean.isThreadContentionMonitoringEnabled()) {
            throw new AssertionError("Thread contention monitoring must be enabled for deadlock detection");
        }
    }

    /// Asserts that no threads are currently deadlocked.
    ///
    /// This method checks for cycles of threads that are in a deadlock waiting to
    /// acquire object monitors or ownable synchronizers.
    ///
    /// NOTE: For this assertion to work effectively, thread contention monitoring should be enabled.
    /// Consider calling [#enableDeadlockDetection()] before calling this method.
    ///
    /// @throws AssertionError if one or more deadlocked threads are detected.
    /// @see #enableDeadlockDetection
    public static void assertNoDeadlocks() {
        checkNoDeadlocks(null);
    }

    /// Asserts that no threads are currently deadlocked, with a custom error message.
    ///
    /// This method checks for cycles of threads that are in a deadlock waiting to
    /// acquire object monitors or ownable synchronizers.
    ///
    /// NOTE: For this assertion to work effectively, thread contention monitoring should be enabled.
    /// Consider calling [#enableDeadlockDetection()] before calling this method.
    ///
    /// @param message the detail message for the [AssertionError]; may be null
    /// @throws AssertionError if one or more deadlocked threads are detected.
    /// @see #enableDeadlockDetection
    public static void assertNoDeadlocks(final String message) {
        checkNoDeadlocks(message);
    }

    /// Asserts that no threads are currently deadlocked, with a lazily-supplied error message.
    ///
    /// This method checks for cycles of threads that are in a deadlock waiting to
    /// acquire object monitors or ownable synchronizers.
    ///
    /// NOTE: For this assertion to work effectively, thread contention monitoring should be enabled.
    /// Consider calling [#enableDeadlockDetection()] before calling this method.
    ///
    /// @param messageSupplier the supplier for the detail message of the [AssertionError];
    ///                        must not be null
    /// @throws AssertionError if one or more deadlocked threads are detected.
    /// @throws NullPointerException if messageSupplier is null
    /// @see #enableDeadlockDetection
    public static void assertNoDeadlocks(final Supplier<String> messageSupplier) {
        checkNoDeadlocks(messageSupplier);
    }

    /// Internal method to perform the deadlock check.
    ///
    /// @param messageOrSupplier the detail message or a [Supplier] for the message; may be null
    /// @throws AssertionError if one or more deadlocked threads are detected.
    public static void checkNoDeadlocks(final Object messageOrSupplier) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        try {
            long[] ids = mx.findDeadlockedThreads();
            if (ids != null) {
                String allThreadInfo = Arrays.stream(ids)
                        .mapToObj(id -> ManagementFactory.getThreadMXBean().getThreadInfo(id))
                        .map(threadInfo -> threadInfo.getThreadName()
                                + " (ID: " + threadInfo.getThreadId()
                                + ", State: " + threadInfo.getThreadState()
                                + ", Lock: " + threadInfo.getLockName()
                                + ", Lock Owner: " + threadInfo.getLockOwnerName()
                                + ")")
                        .collect(Collectors.joining(", "));

                String prefix = "";
                if (messageOrSupplier != null) {
                    if (messageOrSupplier instanceof Supplier<?> supplier) {
                        prefix = supplier.get().toString() + " ==> ";
                    } else {
                        prefix = messageOrSupplier + " ==> ";
                    }
                }

                throw new AssertionError(prefix + "Deadlock(s) detected in threads: [" + allThreadInfo + "]");
            }
        } catch (UnsupportedOperationException e) {
            // Best efforts. Ignore UnsupportedOperationException as it may occur in certain environments
        }
    }
}
