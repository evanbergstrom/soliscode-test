/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.performance;

import java.lang.management.ThreadMXBean;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import com.soliscode.test.AbstractTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [AllocationAssertion].
@DisplayName("AllocationAssertion")
class AllocationAssertionTest extends AbstractTest {

    /// Where the operations put what they allocate, so that the JIT cannot remove the allocation.
    private static volatile Object kept = new Object();

    private static volatile long counter;

    @Test
    @DisplayName("an operation that allocates nothing passes a bound of 0")
    void anOperationThatAllocatesNothingPasses() throws Exception {
        AllocationAssertion.assertAllocatesAtMost(0, 100_000, () -> counter++);
    }

    @Test
    @DisplayName("an operation that keeps a 1 KiB array fails a bound of 512, naming both values")
    void anOperationThatAllocatesFailsWithItsMessage() {
        AssertionFailedError failure = assertThrows(AssertionFailedError.class,
            () -> AllocationAssertion.assertAllocatesAtMost(512, 1_000, () -> kept = new byte[1024]));

        assertTrue(failure.getMessage().startsWith("the operation allocates "), failure.getMessage());
        assertTrue(failure.getMessage().endsWith(" bytes per run, more than the bound of 512.0"),
            failure.getMessage());
        double measured = (Double) failure.getActual().getValue();
        assertTrue(measured >= 1024 && measured < 1024 + 64, "measured " + measured);
    }

    @Test
    @DisplayName("bytesPerOperation() measures what one run allocates")
    void bytesPerOperationMeasuresOneRun() throws Exception {
        double measured = AllocationAssertion.bytesPerOperation(1_000, () -> kept = new long[256]);

        assertTrue(measured >= 2048 && measured < 2048 + 64, "measured " + measured);
    }

    @Test
    @DisplayName("an operation's exception ends the measurement and propagates")
    void anOperationsExceptionPropagates() {
        IllegalStateException thrown = new IllegalStateException("broken");

        assertEquals(thrown, assertThrows(IllegalStateException.class,
            () -> AllocationAssertion.bytesPerOperation(10, () -> {
                throw thrown;
            })));
    }

    @Test
    @DisplayName("n outside 1 to MAX_OPERATIONS, and a bound that is negative or not finite, are refused")
    void refusesItsArgumentsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> AllocationAssertion.bytesPerOperation(0, () -> { }));
        assertThrows(IllegalArgumentException.class,
            () -> AllocationAssertion.bytesPerOperation(AllocationAssertion.MAX_OPERATIONS + 1, () -> { }));
        assertThrows(IllegalArgumentException.class,
            () -> AllocationAssertion.assertAllocatesAtMost(-1, 10, () -> { }));
        assertThrows(IllegalArgumentException.class,
            () -> AllocationAssertion.assertAllocatesAtMost(Double.NaN, 10, () -> { }));
        assertThrows(IllegalArgumentException.class,
            () -> AllocationAssertion.assertAllocatesAtMost(Double.POSITIVE_INFINITY, 10, () -> { }));
    }

    @Test
    @DisplayName("a ThreadMXBean that is not com.sun.management's skips the test")
    void aPlainThreadMxBeanSkips() {
        ThreadMXBean plain = (ThreadMXBean) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[] {ThreadMXBean.class}, (proxy, method, args) -> null);

        assertThrows(TestAbortedException.class, () -> AllocationAssertion.bytesPerOperation(plain, 1, () -> { }));
    }

    @Test
    @DisplayName("a JVM that does not measure threads' allocation skips the test")
    void anUnsupportedMeasurementSkips() {
        ThreadMXBean bean = bean(Map.of("isThreadAllocatedMemorySupported", false), new AtomicBoolean());

        assertThrows(TestAbortedException.class,
            () -> AllocationAssertion.assertAllocatesAtMost(bean, 0, 1, () -> { }));
    }

    @Test
    @DisplayName("a negative reading skips the test rather than passing it")
    void aNegativeReadingSkips() {
        ThreadMXBean bean = bean(Map.of("isThreadAllocatedMemorySupported", true,
            "isThreadAllocatedMemoryEnabled", true, "getCurrentThreadAllocatedBytes", -1L), new AtomicBoolean());

        assertThrows(TestAbortedException.class,
            () -> AllocationAssertion.assertAllocatesAtMost(bean, 0, 1, () -> { }));
    }

    @Test
    @DisplayName("measurement supported but off is turned on")
    void measurementThatIsOffIsTurnedOn() throws Exception {
        AtomicBoolean enabled = new AtomicBoolean();
        ThreadMXBean bean = bean(Map.of("isThreadAllocatedMemorySupported", true,
            "isThreadAllocatedMemoryEnabled", false, "getCurrentThreadAllocatedBytes", 100L), enabled);

        assertEquals(0.0, AllocationAssertion.bytesPerOperation(bean, 1, () -> { }));
        assertTrue(enabled.get());
    }

    @Test
    @DisplayName("a virtual thread skips the test")
    void aVirtualThreadSkips() throws InterruptedException {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread thread = Thread.ofVirtual().start(() -> {
            try {
                AllocationAssertion.assertAllocatesAtMost(0, 1, () -> { });
            } catch (final Throwable e) {
                thrown.set(e);
            }
        });
        thread.join();

        assertInstanceOf(TestAbortedException.class, thrown.get());
    }

    /// A `com.sun.management.ThreadMXBean` that answers each method named from the map, and records a call of
    /// `setThreadAllocatedMemoryEnabled`.
    private static ThreadMXBean bean(final Map<String, Object> answers, final AtomicBoolean enabled) {
        return (ThreadMXBean) Proxy.newProxyInstance(AllocationAssertionTest.class.getClassLoader(),
            new Class<?>[] {com.sun.management.ThreadMXBean.class}, (proxy, method, args) -> {
                if (method.getName().equals("setThreadAllocatedMemoryEnabled")) {
                    enabled.set((Boolean) args[0]);
                    return null;
                }
                return answers.get(method.getName());
            });
    }
}
