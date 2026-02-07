package org.soliscode.test.assertions;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.annotations.Nondeterministic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.AssertNoDeadlocks.assertNoDeadlocks;
import static org.soliscode.test.assertions.Assertions.enableDeadlockDetection;

/// Unit tests for the [AssertNoDeadlocks] class.
/// This test suite validates the deadlock detection functionality by verifying that
/// deadlocks are correctly identified when they exist and that the assertion passes
/// when no deadlocks are present.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see AssertNoDeadlocks
@DisplayName("AssertNoDeadlocks Tests")
class AssertNoDeadlocksTest extends AbstractTest {

    /// ## Test: No Deadlock - Success Case
    /// Verifies that `assertNoDeadlocks()` completes normally when there are
    /// no deadlocked threads in the JVM.
    @Test
    @DisplayName("assertNoDeadlocks() passes when no deadlocks exist")
    void assertNoDeadlocks_whenNoDeadlocks_passes() {
        assertDoesNotThrow(() -> assertNoDeadlocks());
    }

    /// ## Test: Deadlock Detection - Failure Case
    /// Verifies that `assertNoDeadlocks()` correctly identifies a deadlock
    /// situation and throws an [AssertionError].
    @DisplayName("assertNoDeadlocks() fails when a deadlock is detected")
    @Nondeterministic
    @Test
    void assertNoDeadlocks_whenDeadlockExists_fails() throws InterruptedException {
        runTestWithSimpleDeadlock(() -> {
            AssertionError error = assertThrows(AssertionError.class, Assertions::assertNoDeadlocks);

            // Verify message contains thread names and deadlock indication
            String message = error.getMessage();
            assertTrue(message.contains("Deadlock(s) detected"), "Message should indicate deadlocks");
            assertTrue(message.contains("Deadlock-Thread-1"), "Message should mention Thread 1");
            assertTrue(message.contains("Deadlock-Thread-2"), "Message should mention Thread 2");
        });

    }

    /// ## Test: Custom Message - Failure Case
    /// Verifies that `assertNoDeadlocks(String)` includes the custom message
    /// in the [AssertionError] when a deadlock is detected.
    @DisplayName("assertNoDeadlocks(String) includes custom message")
    @Nondeterministic
    @Test
    void assertNoDeadlocks_withCustomMessage_failsWithDeadlock() throws InterruptedException {
        runTestWithSimpleDeadlock(() -> {
            String customMessage = "Custom Error Message";
            AssertionError error = assertThrows(AssertionError.class, () -> assertNoDeadlocks(customMessage));

            assertTrue(error.getMessage().contains(customMessage), "Message should contain custom message");
            assertTrue(error.getMessage().contains("Deadlock(s) detected"), "Message should indicate deadlocks");
        });
    }

    /// ## Test: Lazy Supplier Message - Failure Case
    /// Verifies that `assertNoDeadlocks(Supplier)` includes the lazily-supplied
    /// message in the [AssertionError] when a deadlock is detected.
    @DisplayName("assertNoDeadlocks(Supplier) includes lazy custom message")
    @Nondeterministic
    @Test
    void assertNoDeadlocks_withSupplierMessage_failsWithDeadlock() throws InterruptedException {
        runTestWithSimpleDeadlock(() -> {
            String lazyMessage = "Lazy Custom Error Message";
            AssertionError error = assertThrows(AssertionError.class, () ->
                    Assertions.assertNoDeadlocks(() -> lazyMessage));
            assertTrue(error.getMessage().contains(lazyMessage), "Message should contain lazy message");
        });
    }

    private static void runTestWithSimpleDeadlock(final @NonNull Runnable runnable) throws InterruptedException {
        enableDeadlockDetection();
        Lock lock1 = new ReentrantLock();
        Lock lock2 = new ReentrantLock();
        CountDownLatch deadlockStarted = new CountDownLatch(2);

        Thread t1 = new Thread(() -> {
            try {
                lock1.lockInterruptibly();
                try {
                    deadlockStarted.countDown();
                    Thread.sleep(100);
                    lock2.lockInterruptibly();
                } finally {
                    lock1.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Deadlock-Thread-1");

        Thread t2 = new Thread(() -> {
            try {
                lock2.lockInterruptibly();
                try {
                    deadlockStarted.countDown();
                    Thread.sleep(100);
                    lock1.lockInterruptibly();
                } finally {
                    lock2.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Deadlock-Thread-2");

        t1.start();
        t2.start();

        try {
            assertTrue(deadlockStarted.await(5, TimeUnit.SECONDS), "Threads failed to start deadlock scenario");
            Thread.sleep(200);
            runnable.run();
        } finally {
            t1.interrupt();
            t2.interrupt();
        }
    }
}
