package com.soliscode.test.safety;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.annotations.Nondeterministic;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.assertions.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Unit tests for the [ContentionCoordinator] class.
@DisplayName("ContentionCoordinator Tests")
@Nondeterministic
class ContentionCoordinatorTest extends AbstractTest {

    /// Default constructor.
    public ContentionCoordinatorTest() {
        super();
    }

    @Test
    @DisplayName("execute() with single operation runs successfully")
    void executeWithSingleOperationRunsSuccessfully() {
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
    void executeWithMultipleOperationsRunsSuccessfully() {
        ContentionCoordinator<LongAdder> coordinator = new ContentionCoordinator<>();
        coordinator.add(2, 500, LongAdder::increment);
        coordinator.add(3, 200, LongAdder::increment);

        LongAdder subject = new LongAdder();
        coordinator.execute(subject);

        assertEquals((2 * 500) + (3 * 200), subject.sum(),
                "Subject should be incremented by all threads of all operations");
    }

    @Test
    @DisplayName("execute() with default settings runs successfully")
    void executeWithDefaultSettingsRunsSuccessfully() {
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
    void executeWhenOperationThrowsExceptionReportsAllExceptions() {
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>();
        int threads = 2;
        int iterations = 1;
        coordinator.add(threads, iterations, (s) -> {
            throw new RuntimeException("Test Exception");
        });

        AssertionError error = assertThrows(AssertionError.class, () -> coordinator.execute(new Object()));

        assertTrue(error.getMessage().contains("Operations failed: " + threads),
                "Error message should report failure count");
        assertEquals(threads, error.getSuppressed().length,
                "AssertionError should have suppressed exceptions from all failed threads");
        for (final Throwable suppressed : error.getSuppressed()) {
            assertEquals("Test Exception", suppressed.getMessage());
        }
    }

    @Test
    @DisplayName("execute() ensures high contention")
    void executeEnsuresHighContention() {
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
                if (current <= max) {
                    break;
                }
            } while (!maxConcurrent.compareAndSet(max, current));

            Thread.yield();
            activeThreads.decrementAndGet();
        });

        coordinator.execute(new AtomicInteger(0));

        // If coordination works, we expect more than 1 thread to be active at some point
        assertTrue(maxConcurrent.get() > 1,
                "Expected multiple threads to be active concurrently: " + maxConcurrent.get());
    }

    @Test
    @DisplayName("execute() with custom timeouts and yield interval")
    void executeWithCustomTimeoutsAndYieldIntervalRunsSuccessfully() {
        ContentionCoordinator<AtomicInteger> coordinator = new ContentionCoordinator<>(2, 2, 1);
        coordinator.add(2, 10, AtomicInteger::incrementAndGet);

        AtomicInteger subject = new AtomicInteger(0);
        coordinator.execute(subject);

        assertEquals(20, subject.get());
    }

    @Test
    @DisplayName("execute() yields before the first iteration and once every yield interval (deterministic)")
    void executeYieldsOnceEveryYieldInterval() {
        // 2 workers of 1,000 iterations each yield ceil(1000 / interval) times apiece.
        assertEquals(2 * 16, yieldsOf(64, 1000), "interval 64");
        assertEquals(2 * 1000, yieldsOf(1, 1000), "interval 1");
        assertEquals(2, yieldsOf(1000, 1000), "interval 1000");
        assertEquals(0, yieldsOf(64, 0), "no iterations");
    }

    @Test
    @DisplayName("ContentionCoordinator() with a yield interval below 1 throws IllegalArgumentException")
    void constructorWithYieldIntervalBelowOneThrowsIllegalArgumentException() {
        IllegalArgumentException zero = assertThrows(IllegalArgumentException.class,
                () -> new ContentionCoordinator<>(1, 1, 0));
        assertEquals("yieldInterval must be at least 1: 0", zero.getMessage());
        assertThrows(IllegalArgumentException.class, () -> new ContentionCoordinator<>(1, 1, -1));
    }

    @Test
    @DisplayName("execute() with zero iterations does nothing")
    void executeWithZeroIterationsDoesNothing() {
        ContentionCoordinator<AtomicInteger> coordinator = new ContentionCoordinator<>();
        coordinator.add(2, 0, AtomicInteger::incrementAndGet);

        AtomicInteger subject = new AtomicInteger(0);
        coordinator.execute(subject);

        assertEquals(0, subject.get(), "Subject should not be incremented");
    }

    @Test
    @DisplayName("execute() with null subject throws NullPointerException")
    void executeWithNullSubjectThrowsNullPointerException() {
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>();
        coordinator.add(o -> {});

        assertThrows(NullPointerException.class, () -> coordinator.execute(null));
    }

    @DisplayName("execute() causes collisions with non-tread safe operations")
    @Nondeterministic
    @Test
    void executeWhenCallingNonThreadSafeOperationCausesCollisions() {
        final AtomicInteger counter = new AtomicInteger(0);
        final List<Integer> list = new ArrayList<>();  // not thread safe

        // A finish timeout of 5 s and 10,000 calls a thread, not the default 30,000, and a yield every 4 calls: with a
        // yield before every call, 1 s was too short under load (#12), and 5 s timed out in 1 of 20 runs on 18 busy
        // processes, where every 4 calls collided in 20 of 20, in at most 1.2 s (#54). A timeout is not a collision.
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(1, 5, 4);
        coordinator.add(ContentionCoordinator.DEFAULT_THREADS, 10_000, o -> list.add(counter.getAndIncrement()));
        try {
            coordinator.execute(list);
        } catch (final AssertionError e) {
            // A collision inside ArrayList.add can throw (ArrayIndexOutOfBoundsException), which the coordinator
            // reports as failed operations: that is a collision too.
            assertTrue(e.getMessage().startsWith("Operations failed"), e.getMessage());
            return;
        }

        // Otherwise the list should have dropped some elements due to contention
        assertNotEquals(counter.get(), list.size());
    }

    @DisplayName("execute() detects and reports deadlocks")
    @VerySlow
    @Test
    void executeDetectsAndReportsDeadlocks() {
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
                        throw new AssertionError("Thread A Lock 2 acquisition timed out");
                    }
                    lock2.unlock();
                } finally {
                    lock1.unlock();
                }
            } catch (final InterruptedException e) {
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
                        throw new AssertionError("Thread B Lock 1 acquisition timed out");
                    }
                    lock1.unlock();
                } finally {
                    lock2.unlock();
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        coordinator.add(1, 1, (s) -> {
            // Wait for all the threads to grab their lock
            try {
                if (!ready.await(5, TimeUnit.SECONDS)) {
                    throw new AssertionError("Timeout waiting for threads to get ready");
                }
            } catch (final InterruptedException e) {
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

    @DisplayName("execute() returns, reporting the deadlock, when workers deadlock and ignore interrupts")
    @VerySlow
    @Test
    @Timeout(value = 20, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void executeWhenWorkersDeadlockAndIgnoreInterruptsReturnsWithinBound() throws InterruptedException {
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();
        CountDownLatch bothHoldFirstLock = new CountDownLatch(2);
        CountDownLatch exited = new CountDownLatch(2);
        AtomicBoolean released = new AtomicBoolean(false);
        Set<Thread> workers = ConcurrentHashMap.newKeySet();

        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(1, 1, 1);
        // The workers ignore the coordinator's interrupts, so they stay deadlocked after it gives up on them.
        coordinator.add(1, 1, (s) -> lockBothIgnoringInterrupts(lock1, lock2, bothHoldFirstLock, released, workers,
                exited));
        coordinator.add(1, 1, (s) -> lockBothIgnoringInterrupts(lock2, lock1, bothHoldFirstLock, released, workers,
                exited));

        try {
            AssertionError error = assertThrows(AssertionError.class, () -> coordinator.execute(new Object()));
            assertTrue(error.getMessage().contains("Deadlock(s) detected"),
                    "Expected deadlock detection in error message: " + error.getMessage());
        } finally {
            // End the deadlock so that it does not reach later tests: the JVM-wide deadlock check would see it.
            released.set(true);
            workers.forEach(Thread::interrupt);
            assertTrue(exited.await(5, TimeUnit.SECONDS), "Deadlocked workers did not end when released");
            Assertions.assertNoDeadlocks("a deadlock was left behind for later tests");
        }
    }

    @Test
    @DisplayName("execute() in phases makes exactly each worker's calls, whatever their number")
    @Timeout(30)
    void executeInPhasesMakesExactlyEachWorkersCalls() {
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(5, 10, 64, 10);
        int[] counts = {0, 1, 3, 10, 11, 25, 37};
        List<AtomicInteger> calls = new ArrayList<>();
        for (final int count : counts) {
            AtomicInteger made = new AtomicInteger();
            calls.add(made);
            coordinator.add(2, count, o -> made.incrementAndGet());
        }
        coordinator.execute(new Object());
        for (int i = 0; i < counts.length; i++) {
            assertEquals(2 * counts[i], calls.get(i).get(), "the calls of the workers of " + counts[i]);
        }
    }

    @Test
    @DisplayName("execute() in phases does not hold a worker of few calls that waits for the others to finish")
    @Timeout(30)
    void executeInPhasesDoesNotHoldAFewCallWorkerThatWaitsForTheOthers() {
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(5, 10, 64, 10);
        AtomicInteger made = new AtomicInteger();
        AtomicBoolean sawAll = new AtomicBoolean();
        coordinator.add(2, 50, o -> made.incrementAndGet());
        // One call that waits, bounded, until the others have made all theirs: it must have left the phases first.
        coordinator.add(1, 1, o -> {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (made.get() < 100 && System.nanoTime() - deadline < 0) {
                Thread.onSpinWait();
            }
            sawAll.set(made.get() == 100);
        });
        coordinator.execute(new Object());
        assertTrue(sawAll.get(), "the waiting worker saw the others' calls: " + made.get() + " of 100");
    }

    @Test
    @DisplayName("execute() in phases reports a worker's exception, not a timeout, and the others finish")
    @Timeout(30)
    void executeInPhasesReportsAWorkersException() {
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(5, 10, 64, 10);
        AtomicInteger made = new AtomicInteger();
        coordinator.add(2, 50, o -> made.incrementAndGet());
        AtomicInteger thrower = new AtomicInteger();
        coordinator.add(1, 50, o -> {
            if (thrower.incrementAndGet() == 15) {
                throw new IllegalStateException("the 15th call");
            }
        });
        AssertionError error = assertThrows(AssertionError.class, () -> coordinator.execute(new Object()));
        assertTrue(error.getMessage().startsWith("Operations failed"), error.getMessage());
        assertEquals(100, made.get(), "the other workers' calls");
    }

    @Test
    @DisplayName("execute() in phases times out within its bound when a worker blocks, and every worker ends")
    @Timeout(30)
    void executeInPhasesTimesOutWhenAWorkerBlocks() throws InterruptedException {
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(5, 1, 64, 2);
        CountDownLatch never = new CountDownLatch(1);
        CountDownLatch blockerEnded = new CountDownLatch(1);
        AtomicInteger blocker = new AtomicInteger();
        coordinator.add(1, 100, o -> {
            try {
                if (blocker.incrementAndGet() == 5) {
                    never.await();  // interruptible: the coordinator's shutdownNow ends it
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            } finally {
                if (blocker.get() == 5) {
                    blockerEnded.countDown();
                }
            }
        });
        // The others reach the phase after call 4 and wait there for the blocked worker.
        AtomicInteger others = new AtomicInteger();
        coordinator.add(2, 100, o -> others.incrementAndGet());
        long start = System.nanoTime();
        AssertionError error = assertThrows(AssertionError.class, () -> coordinator.execute(new Object()));
        long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertTrue(error.getMessage().startsWith("Timeout"), error.getMessage());
        assertTrue(error.getMessage().contains("phase"), "the message names the phases: " + error.getMessage());
        assertTrue(millis < 5_000, "the timeout took " + millis + " ms");
        assertTrue(blockerEnded.await(5, TimeUnit.SECONDS), "the blocked worker did not end");
        // Released by the phaser's termination, the others make their calls and end, or are interrupted.
        assertTrue(othersStopped(others), "the other workers kept calling: " + others.get());
    }

    /// Whether the count stays the same for 100 ms: the workers that make it have ended.
    private static boolean othersStopped(final AtomicInteger count) {
        int before = count.get();
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(100);
        while (System.nanoTime() - deadline < 0) {
            if (count.get() != before) {
                return false;
            }
            Thread.onSpinWait();
        }
        return true;
    }

    @Test
    @DisplayName("execute() in phases keeps the workers within a phase of each other (deterministic)")
    @Timeout(30)
    void executeInPhasesKeepsTheWorkersTogether() {
        assertTrue(largestLead(1) <= 1, "with phases of 1 call, a worker ran ahead");
        assertTrue(largestLead(0) > 1, "without phases, the free worker should have run ahead: the check cannot fail");
    }

    @Test
    @DisplayName("execute() in phases with more workers than cores makes every worker's calls")
    @Timeout(60)
    void executeInPhasesWithMoreWorkersThanCoresMakesEveryCall() {
        int workers = ContentionCoordinator.NUMBER_OF_CORES + 1;
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(10, 20, 64, 10);
        AtomicInteger made = new AtomicInteger();
        coordinator.add(workers, 30, o -> made.incrementAndGet());
        coordinator.execute(new Object());
        assertEquals(workers * 30, made.get(), "the calls");
    }

    @Test
    @DisplayName("execute() in phases ends at once when a worker sets its own interrupt status and the phaser ends")
    @Timeout(30)
    void executeInPhasesEndsWhenAWorkerSetsItsInterruptStatus() {
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(5, 5, 64, 10);
        AtomicInteger made = new AtomicInteger();
        coordinator.add(2, 100, o -> made.incrementAndGet());
        // At its 15th call the worker sets its interrupt status and returns, as code that restores it does: at its
        // next arrival it ends the phases, and the others' later arrivals must return at once.
        AtomicInteger interrupter = new AtomicInteger();
        coordinator.add(1, 100, o -> {
            if (interrupter.incrementAndGet() == 15) {
                Thread.currentThread().interrupt();
            }
        });
        long start = System.nanoTime();
        AssertionError error = assertThrows(AssertionError.class, () -> coordinator.execute(new Object()));
        long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertTrue(error.getMessage().startsWith("Operations failed"), error.getMessage());
        assertTrue(millis < 4_000, "the run took " + millis + " ms, as long as its timeout");
        assertEquals(200, made.get(), "the other workers' calls");
    }

    @Test
    @DisplayName("ContentionCoordinator refuses a negative phase interval")
    void constructorWithNegativePhaseIntervalThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ContentionCoordinator<>(1, 1, 1, -1));
    }

    /// Runs two workers of 10 calls, A and B, with the given phase interval, where B's first call waits (at most
    /// 1 s) until A has made more than 2 calls, and returns the largest number of calls by which one was ahead of the
    /// other at the start of a call. In phases of 1 call, A cannot get ahead, so B's wait runs out.
    private static int largestLead(final int phaseInterval) {
        ContentionCoordinator<Object> coordinator = new ContentionCoordinator<>(5, 10, 64, phaseInterval);
        AtomicInteger a = new AtomicInteger();
        AtomicInteger b = new AtomicInteger();
        AtomicInteger lead = new AtomicInteger();
        coordinator.add(1, 10, o -> lead.accumulateAndGet(Math.abs(a.incrementAndGet() - b.get()), Math::max));
        coordinator.add(1, 10, o -> {
            int mine = b.incrementAndGet();
            lead.accumulateAndGet(Math.abs(mine - a.get()), Math::max);
            if (mine == 1) {
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
                while (a.get() <= 2 && System.nanoTime() - deadline < 0) {
                    Thread.onSpinWait();
                }
            }
        });
        coordinator.execute(new Object());
        return lead.get();
    }

    /// Runs 2 workers of `iterations` each through a coordinator with the given yield interval, and counts the yields.
    private static long yieldsOf(final int yieldInterval, final int iterations) {
        LongAdder yields = new LongAdder();
        ContentionCoordinator<AtomicInteger> coordinator = new ContentionCoordinator<>(
                ContentionCoordinator.DEFAULT_START_TIMEOUT_SECONDS,
                ContentionCoordinator.DEFAULT_SHUTDOWN_TIMEOUT_SECONDS, yieldInterval,
                ContentionCoordinator.DEFAULT_PHASE_INTERVAL, yields::increment);
        coordinator.add(2, iterations, AtomicInteger::incrementAndGet);
        coordinator.execute(new AtomicInteger(0));
        return yields.sum();
    }

    private static void lockBothIgnoringInterrupts(final ReentrantLock first, final ReentrantLock second,
                                                   final CountDownLatch bothHoldFirstLock,
                                                   final AtomicBoolean released, final Set<Thread> workers,
                                                   final CountDownLatch exited) {
        workers.add(Thread.currentThread());
        try {
            first.lock();
            try {
                bothHoldFirstLock.countDown();
                while (!released.get()) {
                    try {
                        bothHoldFirstLock.await();
                        second.lockInterruptibly();
                        second.unlock();
                        return;
                    } catch (final InterruptedException e) {
                        // ignored unless released: the point is a deadlock the coordinator cannot interrupt
                    }
                }
            } finally {
                first.unlock();
            }
        } finally {
            exited.countDown();
        }
    }
}
