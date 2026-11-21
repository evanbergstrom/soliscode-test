/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Objects;

import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assumptions.abort;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/// Measures the bytes an operation allocates on the calling thread, and asserts a bound on them.
///
/// The operation is run `n` times to warm it, then `n` times and `10n` times, the thread's allocated bytes read
/// (`com.sun.management.ThreadMXBean.getCurrentThreadAllocatedBytes()`) before and after each run. The bytes per
/// operation are `(a(10n) − a(n)) / 9n`, where `a(k)` is what a run of `k` allocated: what the measuring itself
/// allocates is the same in both runs, and cancels.
///
/// Where the JVM cannot measure (no `com.sun.management.ThreadMXBean`, allocation measurement unsupported, the
/// calling thread virtual, or a reading negative), JUnit's `assumeTrue` skips the test, rather than passing it.
/// Where measurement is supported but off, it is turned on, for the whole JVM.
///
/// ## Usage Examples
/// ```java
/// @Test
/// void recordAllocatesNothing() throws Exception {
///     LatencyRecorder recorder = LatencyRecorder.upTo(Duration.ofSeconds(1));
///     AllocationAssertion.assertAllocatesAtMost(0, 10_000, () -> recorder.record(1_000));
/// }
/// ```
///
/// ## Thread Safety
/// Each call measures its own thread; calls on different threads do not interfere.
///
/// @since 0.1.6
public final class AllocationAssertion {

    /// The largest `n`: `11n` operations are run, after the `n` that warm.
    public static final long MAX_OPERATIONS = Long.MAX_VALUE / 11;

    private static final int RUNS_COMPARED = 9;

    private AllocationAssertion() {
    }

    /// An operation whose allocation is measured.
    @FunctionalInterface
    public interface Operation {

        /// Runs the operation once.
        ///
        /// @throws Exception if it fails, which ends the measurement
        void run() throws Exception;
    }

    /// Returns the bytes the operation allocates per run on the calling thread.
    ///
    /// @param n the runs to warm it and the shorter of the two measured runs, 1 to [#MAX_OPERATIONS]
    /// @param operation the operation
    /// @return the bytes per run; may be slightly negative where the two runs differ by noise alone
    /// @throws IllegalArgumentException if `n` is out of range
    /// @throws org.opentest4j.TestAbortedException if the JVM cannot measure, which skips the test
    /// @throws Exception if the operation throws
    public static double bytesPerOperation(final long n, final Operation operation) throws Exception {
        return bytesPerOperation(ManagementFactory.getThreadMXBean(), n, operation);
    }

    /// Asserts that the operation allocates at most `bound` bytes per run on the calling thread.
    ///
    /// @param bound the most bytes per run, finite and not negative
    /// @param n the runs to warm it and the shorter of the two measured runs, 1 to [#MAX_OPERATIONS]
    /// @param operation the operation
    /// @throws IllegalArgumentException if the bound or `n` is out of range
    /// @throws AssertionFailedError if the operation allocates more
    /// @throws org.opentest4j.TestAbortedException if the JVM cannot measure, which skips the test
    /// @throws Exception if the operation throws
    public static void assertAllocatesAtMost(final double bound, final long n, final Operation operation)
        throws Exception {
        assertAllocatesAtMost(ManagementFactory.getThreadMXBean(), bound, n, operation);
    }

    /// [#assertAllocatesAtMost(double, long, Operation)] on the bean given, for the tests.
    static void assertAllocatesAtMost(final ThreadMXBean platform, final double bound, final long n,
        final Operation operation) throws Exception {
        if (!(bound >= 0.0 && Double.isFinite(bound))) {
            throw new IllegalArgumentException("the allocation bound " + bound + " is not a finite number of 0 or "
                + "more bytes");
        }
        double measured = bytesPerOperation(platform, n, operation);
        if (measured > bound) {
            throw new AssertionFailedError("the operation allocates " + measured + " bytes per run, more than the "
                + "bound of " + bound, bound, measured);
        }
    }

    /// [#bytesPerOperation(long, Operation)] on the bean given, for the tests.
    static double bytesPerOperation(final ThreadMXBean platform, final long n, final Operation operation)
        throws Exception {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(operation, "operation");
        if (n < 1 || n > MAX_OPERATIONS) {
            throw new IllegalArgumentException("the runs are " + n + ", not 1 to " + MAX_OPERATIONS);
        }
        com.sun.management.ThreadMXBean bean = measuring(platform);
        repeat(n, operation);
        long before = allocated(bean);
        repeat(n, operation);
        long afterOnce = allocated(bean);
        repeat(n * 10, operation);
        long afterTen = allocated(bean);
        long once = afterOnce - before;
        long ten = afterTen - afterOnce;
        return (double) (ten - once) / ((double) n * RUNS_COMPARED);
    }

    private static com.sun.management.ThreadMXBean measuring(final ThreadMXBean platform) {
        assumeTrue(platform instanceof com.sun.management.ThreadMXBean,
            "this JVM's ThreadMXBean is not com.sun.management's, which measures allocation");
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) platform;
        assumeTrue(!Thread.currentThread().isVirtual(), "the JVM does not measure a virtual thread's allocation");
        assumeTrue(bean.isThreadAllocatedMemorySupported(), "this JVM does not measure threads' allocation");
        if (!bean.isThreadAllocatedMemoryEnabled()) {
            bean.setThreadAllocatedMemoryEnabled(true);
        }
        return bean;
    }

    private static long allocated(final com.sun.management.ThreadMXBean bean) {
        long bytes = bean.getCurrentThreadAllocatedBytes();
        if (bytes < 0) {
            // the message is built only here, so that a reading allocates nothing in the runs it brackets
            abort("the JVM could not read this thread's allocated bytes (" + bytes + ")");
        }
        return bytes;
    }

    private static void repeat(final long times, final Operation operation) throws Exception {
        for (long i = 0; i < times; i++) {
            operation.run();
        }
    }
}
