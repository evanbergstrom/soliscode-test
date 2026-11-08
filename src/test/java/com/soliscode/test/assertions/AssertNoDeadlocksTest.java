package com.soliscode.test.assertions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;

import static com.soliscode.test.assertions.AssertNoDeadlocks.assertNoDeadlocks;
import static com.soliscode.test.assertions.Assertions.enableDeadlockDetection;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


    /// The longest the deadlock helper waits for its threads to start, deadlock, or end.
    private static final long BOUND_NANOS = TimeUnit.SECONDS.toNanos(5);

    /// Default constructor.

    /// ## Test: No Deadlock - Success Case
    /// Verifies that `assertNoDeadlocks()` completes normally when there are
    /// no deadlocked threads in the JVM.
    @Test
    @DisplayName("assertNoDeadlocks() passes when no deadlocks exist")
    void assertNoDeadlocksWhenNoDeadlocksPasses() {
        enableDeadlockDetection();
        assertDoesNotThrow(() -> assertNoDeadlocks());
    }

    /// ## Test: Deadlock Detection - Failure Case
    /// Verifies that `assertNoDeadlocks()` correctly identifies a deadlock
    /// situation and throws an [AssertionError].
    @DisplayName("assertNoDeadlocks() fails when a deadlock is detected")
    @Test
    void assertNoDeadlocksWhenDeadlockExistsFails() throws InterruptedException {
        enableDeadlockDetection();
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
    @Test
    void assertNoDeadlocksWithCustomMessageFailsWithDeadlock() throws InterruptedException {
        enableDeadlockDetection();
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
    @Test
    void assertNoDeadlocksWithSupplierMessageFailsWithDeadlock() throws InterruptedException {
        enableDeadlockDetection();
        runTestWithSimpleDeadlock(() -> {
            String lazyMessage = "Lazy Custom Error Message";
            AssertionError error = assertThrows(AssertionError.class, () ->
                    Assertions.assertNoDeadlocks(() -> lazyMessage));
            assertTrue(error.getMessage().contains(lazyMessage), "Message should contain lazy message");
        });
    }

    @DisplayName("assertNoDeadlocks(Supplier) throws correct exception when deadlock detection is not enabled")
    @Test
    void assertNoDeadlocksWhenDeadlockDetectionNotEnabledThrowsAssertionError() throws InterruptedException{
        runTestWithSimpleDeadlock(() -> {
            AssertionError error = assertThrows(AssertionError.class, Assertions::assertNoDeadlocks);
        });
    }

    /// Runs `runnable` while two threads are deadlocked, then ends the deadlock.
    ///
    /// The threads each take one lock, wait until both hold theirs, and then ask for the other's. The deadlock is
    /// established when each lock has the other thread queued on it, which is waited for, with a bound, instead of
    /// sleeping.
    private static void runTestWithSimpleDeadlock(final Runnable runnable) throws InterruptedException {
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();
        CountDownLatch bothHoldFirstLock = new CountDownLatch(2);

        Thread t1 = new Thread(() -> lockBoth(lock1, lock2, bothHoldFirstLock), "Deadlock-Thread-1");
        Thread t2 = new Thread(() -> lockBoth(lock2, lock1, bothHoldFirstLock), "Deadlock-Thread-2");
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();

        try {
            assertTrue(bothHoldFirstLock.await(5, TimeUnit.SECONDS), "Threads failed to start deadlock scenario");
            long deadline = System.nanoTime() + BOUND_NANOS;
            // Queued and parked: the deadlock check reads each thread's park blocker, which is set only once it parks.
            while (!(lock2.hasQueuedThread(t1) && lock1.hasQueuedThread(t2)
                    && LockSupport.getBlocker(t1) != null && LockSupport.getBlocker(t2) != null)) {
                assertTrue(System.nanoTime() < deadline, "Threads failed to deadlock");
                Thread.onSpinWait();
            }
            runnable.run();
        } finally {
            t1.interrupt();
            t2.interrupt();
            t1.join(TimeUnit.NANOSECONDS.toMillis(BOUND_NANOS));
            t2.join(TimeUnit.NANOSECONDS.toMillis(BOUND_NANOS));
        }
        assertFalse(t1.isAlive() || t2.isAlive(), "Deadlocked threads did not end when interrupted");
    }

    /// Takes `first`, waits until the other thread holds its first lock too, then takes `second`.
    private static void lockBoth(final ReentrantLock first, final ReentrantLock second,
                                 final CountDownLatch bothHoldFirstLock) {
        try {
            first.lockInterruptibly();
            try {
                bothHoldFirstLock.countDown();
                bothHoldFirstLock.await();
                second.lockInterruptibly();
                second.unlock();
            } finally {
                first.unlock();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
