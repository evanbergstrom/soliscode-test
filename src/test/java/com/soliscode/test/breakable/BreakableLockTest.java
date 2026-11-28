/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `BreakableLock` beyond what `ContractCoverageTest` shows of it through `LockContract`: an unlock or a
/// signal by a thread that does not hold it, its conditions' timed and uninterruptible waits, a signal that meets an
/// interrupt, and serialization.
class BreakableLockTest {

    private static final long TIMEOUT_SECONDS = 5;

    @Test
    @DisplayName("unlock() by a thread that does not hold the lock throws IllegalMonitorStateException")
    void unlockByANonOwnerThrows() throws Exception {
        BreakableLock lock = new BreakableLock(Set.of());
        lock.lock();
        try {
            Throwable thrown = onAnotherThread(() -> {
                try {
                    lock.unlock();
                    return null;
                } catch (final IllegalMonitorStateException e) {
                    return e;
                }
            });
            assertInstanceOf(IllegalMonitorStateException.class, thrown, "unlock() by another thread");
        } finally {
            lock.unlock();
        }
        assertThrows(IllegalMonitorStateException.class, lock::unlock, "unlock() of a lock no thread holds");
    }

    @Test
    @DisplayName("await() and signal() without the lock throw IllegalMonitorStateException")
    void conditionWithoutTheLockThrows() {
        Condition condition = new BreakableLock(Set.of()).newCondition();
        assertThrows(IllegalMonitorStateException.class, condition::await, "await() without the lock");
        assertThrows(IllegalMonitorStateException.class, condition::signal, "signal() without the lock");
        assertThrows(IllegalMonitorStateException.class, condition::signalAll, "signalAll() without the lock");
    }

    @Test
    @DisplayName("the timed awaits time out with no signal, and say so")
    void timedAwaitsTimeOut() throws Exception {
        BreakableLock lock = new BreakableLock(Set.of());
        Condition condition = lock.newCondition();
        lock.lock();
        try {
            assertTrue(condition.awaitNanos(TimeUnit.MILLISECONDS.toNanos(20)) <= 0, "awaitNanos(20 ms)");
            assertFalse(condition.await(20, TimeUnit.MILLISECONDS), "await(20, MILLISECONDS)");
            assertFalse(condition.awaitUntil(new Date(System.currentTimeMillis() + 20)), "awaitUntil(now + 20 ms)");
            assertFalse(condition.awaitUntil(new Date(Long.MIN_VALUE)), "awaitUntil of a date long past");
            assertFalse(condition.await(Long.MIN_VALUE, TimeUnit.NANOSECONDS), "await with a negative time");
        } finally {
            lock.unlock();
        }
        assertTrue(lock.tryLock(), "tryLock() after the awaits: they took the lock again and the unlock released it");
        lock.unlock();
    }

    @Test
    @DisplayName("a timed await signalled in time returns false if the signaller holds the lock past the deadline")
    void timedAwaitReportsTheTimeLeftOnReturn() throws Exception {
        BreakableLock lock = new BreakableLock(Set.of());
        Condition condition = lock.newCondition();
        long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(100);
        CountDownLatch waiting = new CountDownLatch(1);
        FutureTask<Boolean> waiter = new FutureTask<>(() -> {
            lock.lock();
            try {
                waiting.countDown();
                return condition.await(timeoutNanos, TimeUnit.NANOSECONDS);
            } finally {
                lock.unlock();
            }
        });
        start(waiter);
        assertTrue(waiting.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "the waiter took the lock");
        lock.lock();
        try {
            condition.signal();
            long until = System.nanoTime() + 2 * timeoutNanos;
            while (System.nanoTime() - until < 0) {
                Thread.onSpinWait();
            }
        } finally {
            lock.unlock();
        }
        assertFalse(waiter.get(TIMEOUT_SECONDS, TimeUnit.SECONDS), "await(100 ms), signalled in time, returned once "
                + "the signaller had held the lock for 200 ms");
    }

    @Test
    @DisplayName("awaitUninterruptibly() goes on waiting when interrupted, and returns with the status set")
    void awaitUninterruptiblyKeepsTheInterrupt() throws Exception {
        BreakableLock lock = new BreakableLock(Set.of());
        Condition condition = lock.newCondition();
        CountDownLatch waiting = new CountDownLatch(1);
        Thread[] thread = new Thread[1];
        FutureTask<Boolean> waiter = new FutureTask<>(() -> {
            lock.lock();
            try {
                thread[0] = Thread.currentThread();
                waiting.countDown();
                condition.awaitUninterruptibly();
                return Thread.interrupted();
            } finally {
                lock.unlock();
            }
        });
        start(waiter);
        assertTrue(waiting.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "the waiter took the lock");
        lock.lock();
        try {
            thread[0].interrupt();
            condition.signal();
        } finally {
            lock.unlock();
        }
        assertTrue(waiter.get(TIMEOUT_SECONDS, TimeUnit.SECONDS), "the interrupt status once awaitUninterruptibly() "
                + "returned");
    }

    @Test
    @DisplayName("a waiter signalled and then interrupted returns normally, so that the signal is not lost")
    void aSignalledWaiterThatIsInterruptedKeepsTheSignal() throws Exception {
        BreakableLock lock = new BreakableLock(Set.of());
        Condition condition = lock.newCondition();
        CountDownLatch waiting = new CountDownLatch(1);
        Thread[] thread = new Thread[1];
        FutureTask<Boolean> waiter = new FutureTask<>(() -> {
            lock.lock();
            try {
                thread[0] = Thread.currentThread();
                waiting.countDown();
                condition.await();
                return Thread.interrupted();
            } finally {
                lock.unlock();
            }
        });
        start(waiter);
        assertTrue(waiting.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "the waiter took the lock");
        lock.lock();
        try {
            condition.signal();
            thread[0].interrupt();
        } finally {
            lock.unlock();
        }
        assertTrue(waiter.get(TIMEOUT_SECONDS, TimeUnit.SECONDS), "await() returned normally, with the interrupt "
                + "status set");
    }

    @Test
    @DisplayName("clone() throws CloneNotSupportedException")
    void refusesToBeCloned() {
        BreakableLock lock = new BreakableLock(Set.of());
        assertThrows(CloneNotSupportedException.class, lock::clone, "clone()");
    }

    @Test
    @DisplayName("a deserialized lock is unlocked")
    void deserializesUnlocked() throws Exception {
        BreakableLock lock = new BreakableLock(Set.of(BreakableLock.SIGNAL_DOES_NOTHING));
        lock.lock();
        BreakableLock copy;
        try {
            copy = roundTrip(lock);
        } finally {
            lock.unlock();
        }
        assertTrue(copy.hasBreak(BreakableLock.SIGNAL_DOES_NOTHING), "the copy's breaks");
        assertTrue(onAnotherThread(() -> {
            boolean got = copy.tryLock();
            if (got) {
                copy.unlock();
            }
            return got;
        }), "another thread's tryLock() of the copy of a held lock");
    }

    /// Serializes an object and reads it back.
    static <T> T roundTrip(final T object) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(object);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            @SuppressWarnings("unchecked")
            T read = (T) in.readObject();
            return read;
        }
    }

    /// Makes a call on a daemon thread of its own, and returns its result within [#TIMEOUT_SECONDS].
    static <T> T onAnotherThread(final Callable<T> call) throws Exception {
        FutureTask<T> task = new FutureTask<>(call);
        start(task);
        try {
            return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw e;
        }
    }

    private static void start(final Runnable task) {
        Thread thread = new Thread(task, "breakable-lock-test");
        thread.setDaemon(true);
        thread.start();
    }
}
