package org.soliscode.test.safety;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.annotations.Nondeterministic;
import org.soliscode.test.annotations.VerySlow;
import org.soliscode.test.assertions.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

/// Unit tests for the [ContentionCoordinator] class.
@DisplayName("ContentionCoordinator Tests")
@Nondeterministic
class ContentionCoordinatorTest extends AbstractTest {

    @Test
    @DisplayName("execute() with single operation runs successfully")
    void execute_withSingleOperation_runsSuccessfully() {
        ContentionCoordinator<AtomicInteger> coordinator = new ContentionCoordinator<>();
        int threads = 4;
        int iterations = 1000;
        coordinator.add(threads, iterations, AtomicInteger::incrementAndGet);

        AtomicInteger subject = new AtomicInteger(0);
        coordinator.execute(subject);

        assertEquals(threads * iterations, subject.get(), "Subject should be incremented by total iterations");
    }

    @Test
    @DisplayName("execute() with multiple operations runs successfully")
    void execute_withMultipleOperations_runsSuccessfully() {
        ContentionCoordinator<LongAdder> coordinator = new ContentionCoordinator<>();
        coordinator.add(2, 500, LongAdder::increment);
        coordinator.add(3, 200, LongAdder::increment);

        LongAdder subject = new LongAdder();
        coordinator.execute(subject);

        assertEquals((2 * 500) + (3 * 200), subject.sum(), "Subject should be incremented by all threads of all operations");
    }

    @Test
    @DisplayName("execute() with default settings runs successfully")
    void execute_withDefaultSettings_runsSuccessfully() {
        ContentionCoordinator<AtomicInteger> coordinator = new ContentionCoordinator<>();
        coordinator.add(AtomicInteger::incrementAndGet);

        AtomicInteger subject = new AtomicInteger(0);
        // Default iterations is 30000. Default threads depends on cores.
        coordinator.execute(subject);

        assertTrue(subject.get() >= ContentionCoordinator.DEFAULT_ITERATIONS,
                "Subject should be incremented at least by default iterations");
    }

    @Test
    @DisplayName("execute() when operation throws exception reports all exceptions")
    void execute_whenOperationThrowsException_reportsAllExceptions() {
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>();
        int threads = 2;
        int iterations = 1;
        coordinator.add(threads, iterations, (s) -> {
            throw new RuntimeException("Test Exception");
        });

        AssertionError error = assertThrows(AssertionError.class, () -> coordinator.execute(new Object()));

        assertTrue(error.getMessage().contains("Operations failed: " + threads), "Error message should report failure count");
        assertEquals(threads, error.getSuppressed().length, "AssertionError should have suppressed exceptions from all failed threads");
        for (Throwable suppressed : error.getSuppressed()) {
            assertEquals("Test Exception", suppressed.getMessage());
        }
    }

    @Test
    @DisplayName("execute() ensures high contention")
    void execute_ensuresHighContention() {
        // This test tries to verify that threads are actually waiting for the start signal.
        // We can't strictly prove it, but we can check if they increment a counter roughly together.
        ContentionCoordinator<AtomicInteger> coordinator = new ContentionCoordinator<>();
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        AtomicInteger activeThreads = new AtomicInteger(0);

        int threads = 10;
        coordinator.add(threads, 100, (s) -> {
            int current = activeThreads.incrementAndGet();
            int max;
            do {
                max = maxConcurrent.get();
                if (current <= max) break;
            } while (!maxConcurrent.compareAndSet(max, current));

            Thread.yield();
            activeThreads.decrementAndGet();
        });

        coordinator.execute(new AtomicInteger(0));

        // If coordination works, we expect more than 1 thread to be active at some point
        assertTrue(maxConcurrent.get() > 1, "Expected multiple threads to be active concurrently: " + maxConcurrent.get());
    }

    @Test
    @DisplayName("execute() with custom timeouts and yield interval")
    void execute_withCustomTimeoutsAndYieldInterval_runsSuccessfully() {
        ContentionCoordinator<AtomicInteger> coordinator = new ContentionCoordinator<>(2, 2, 1);
        coordinator.add(2, 10, AtomicInteger::incrementAndGet);

        AtomicInteger subject = new AtomicInteger(0);
        coordinator.execute(subject);

        assertEquals(20, subject.get());
    }

    @Test
    @DisplayName("execute() with zero iterations does nothing")
    void execute_withZeroIterations_doesNothing() {
        ContentionCoordinator<AtomicInteger> coordinator = new ContentionCoordinator<>();
        coordinator.add(2, 0, AtomicInteger::incrementAndGet);

        AtomicInteger subject = new AtomicInteger(0);
        coordinator.execute(subject);

        assertEquals(0, subject.get(), "Subject should not be incremented");
    }

    @Test
    @DisplayName("execute() with null subject throws NullPointerException")
    void execute_withNullSubject_throwsNullPointerException() {
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>();
        coordinator.add(o -> {});

        assertThrows(NullPointerException.class, () -> coordinator.execute(null));
    }

    @DisplayName("execute() causes collisions with non-tread safe operations")
    @Nondeterministic
    @Test
    void execute_whenCallingNonThreadSafeOperation_causesCollisions() {
        final AtomicInteger counter = new AtomicInteger(0);
        final List<Integer> list = new ArrayList<>();  // not thread safe

        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(1,1,1);
        coordinator.add(o ->  list.add(counter.getAndIncrement()));
        coordinator.execute(list);

        // The list should have dropped some elements due to contention
        assertNotEquals(counter.get(), list.size());
    }

    @DisplayName("execute() detects and reports deadlocks")
    @VerySlow
    @Test
    void execute_detectsAndReportsDeadlocks() {
        Assertions.enableDeadlockDetection();

        // We use two locks to create a classic deadlock
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();

        // Use short timeouts so the test doesn't take too long
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(1, 1, 1);

        final int lockTimeout = 2;
        final CountDownLatch ready = new CountDownLatch(2);  // Make sure all threads are ready
        final CountDownLatch start = new CountDownLatch(1);  // Start all operations simultaneously

        // Thread A: lock1 -> lock2
        coordinator.add(1, 1, (s) -> {
            try {
                lock1.lockInterruptibly();
                ready.countDown();
                try {
                    start.await(); // wait for thread B to grab lock 2
                    if (!lock2.tryLock(lockTimeout, TimeUnit.SECONDS)) {
                        System.out.println("Thread A Lock 2 acquisition timed out");
                        throw new AssertionError("Thread A Lock 2 acquisition timed out");
                    }
                    lock2.unlock();
                } finally {
                    lock1.unlock();
                }
            } catch (InterruptedException e) {
                System.out.println("Thread A interrupted");
                Thread.currentThread().interrupt();
            }
        });

        // Thread B: lock2 -> lock1
        coordinator.add(1, 1, (s) -> {
            try {
                lock2.lockInterruptibly();
                ready.countDown();
                try {
                    start.await(); // wait for thread A to grab lock 1
                    if (!lock1.tryLock(lockTimeout, TimeUnit.SECONDS)) {
                        System.out.println("Thread B Lock 1 acquisition timed out");
                        throw new AssertionError("Thread B Lock 1 acquisition timed out");
                    }
                    lock1.unlock();
                } finally {
                    lock2.unlock();
                }
            } catch (InterruptedException e) {
                System.out.println("Thread B interrupted");
                Thread.currentThread().interrupt();
            }
        });

        coordinator.add(1, 1, (s) -> {
            // Wait for all the threads to grab their lock
            try {
                if (!ready.await(5, TimeUnit.SECONDS)) {
                    throw new AssertionError("Timeout waiting for threads to get ready");
                }
                System.out.println("All threads ready");
            } catch (InterruptedException e) {
                throw new AssertionError("Interrupted while waiting for threads to get ready", e);
            }

            // Let threads deadlock on each others lock
            start.countDown();
        });


        AssertionError error = assertThrows(AssertionError.class, () -> coordinator.execute(new Object()));

        // ContentionCoordinator should fail with a timeout first, and then assertNoDeadlocks() should trigger.
        // We expect the deadlock info in the message.
        assertTrue(error.getMessage().contains("Deadlock(s) detected"),
                "Expected deadlock detection in error message: " + error.getMessage());
        assertTrue(error.getMessage().contains("ContentionCoordinator-"),
                "Error message should contain thread info");
    }
}
