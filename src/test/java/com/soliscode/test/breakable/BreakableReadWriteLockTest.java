/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.soliscode.test.breakable.BreakableLockTest.onAnotherThread;
import static com.soliscode.test.breakable.BreakableLockTest.roundTrip;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `BreakableReadWriteLock` beyond what `ContractCoverageTest` shows of it: a writer that gives up wakes the
/// readers it held back, its views, an unlock of a lock no thread holds, and serialization.
class BreakableReadWriteLockTest {

    @Test
    @DisplayName("a writer that times out no longer holds readers back")
    void aWriterThatGivesUpLetsReadersIn() throws Exception {
        BreakableReadWriteLock lock = new BreakableReadWriteLock(Set.of());
        Lock read = lock.readLock();
        read.lock();
        try {
            assertFalse(onAnotherThread(() -> {
                boolean got = lock.writeLock().tryLock(20, TimeUnit.MILLISECONDS);
                if (got) {
                    lock.writeLock().unlock();
                }
                return got;
            }), "a timed write tryLock() while a reader held the read lock");
            assertTrue(onAnotherThread(() -> {
                boolean got = read.tryLock();
                if (got) {
                    read.unlock();
                }
                return got;
            }), "a read tryLock() once the writer had given up");
        } finally {
            read.unlock();
        }
    }

    @Test
    @DisplayName("a writer that times out wakes a reader it held back")
    void aWriterThatGivesUpWakesAWaitingReader() throws Exception {
        BreakableReadWriteLock lock = new BreakableReadWriteLock(Set.of());
        Lock read = lock.readLock();
        AtomicReference<Thread> writerThread = new AtomicReference<>();
        FutureTask<Boolean> writer = new FutureTask<>(() -> {
            writerThread.set(Thread.currentThread());
            boolean got = lock.writeLock().tryLock(3, TimeUnit.SECONDS);
            if (got) {
                lock.writeLock().unlock();
            }
            return got;
        });
        AtomicReference<Thread> readerThread = new AtomicReference<>();
        FutureTask<Boolean> reader = new FutureTask<>(() -> {
            readerThread.set(Thread.currentThread());
            read.lock();
            read.unlock();
            return true;
        });
        read.lock();
        try {
            start(writer);
            awaitWaiting(writerThread);
            start(reader);
            awaitWaiting(readerThread);
            assertFalse(writer.get(5, TimeUnit.SECONDS), "the timed write tryLock() while a reader held the lock");
            assertTrue(reader.get(5, TimeUnit.SECONDS), "a reader that waited behind the writer, once the writer "
                    + "gave up, while the first reader still held the read lock");
        } finally {
            read.unlock();
        }
    }

    @Test
    @DisplayName("readLock() and writeLock() return the same view each time, and the views refuse conditions")
    void viewsAreTheSameEachTime() {
        BreakableReadWriteLock lock = new BreakableReadWriteLock(Set.of());
        assertSame(lock.readLock(), lock.readLock(), "readLock()");
        assertSame(lock.writeLock(), lock.writeLock(), "writeLock()");
        assertThrows(UnsupportedOperationException.class, lock.readLock()::newCondition, "readLock().newCondition()");
        assertThrows(UnsupportedOperationException.class, lock.writeLock()::newCondition,
                "writeLock().newCondition()");
    }

    @Test
    @DisplayName("unlock() of a lock no thread holds throws IllegalMonitorStateException")
    void unlockOfAFreeLockThrows() {
        BreakableReadWriteLock lock = new BreakableReadWriteLock(Set.of());
        assertThrows(IllegalMonitorStateException.class, lock.readLock()::unlock, "readLock().unlock()");
        assertThrows(IllegalMonitorStateException.class, lock.writeLock()::unlock, "writeLock().unlock()");
    }

    @Test
    @DisplayName("clone() throws CloneNotSupportedException")
    void refusesToBeCloned() {
        BreakableReadWriteLock lock = new BreakableReadWriteLock(Set.of());
        assertThrows(CloneNotSupportedException.class, lock::clone, "clone()");
    }

    @Test
    @DisplayName("a deserialized read-write lock is unlocked")
    void deserializesUnlocked() throws Exception {
        BreakableReadWriteLock lock = new BreakableReadWriteLock(Set.of(BreakableReadWriteLock.WRITE_LOCK_IS_SHARED));
        lock.writeLock().lock();
        BreakableReadWriteLock copy;
        try {
            copy = roundTrip(lock);
        } finally {
            lock.writeLock().unlock();
        }
        assertTrue(copy.hasBreak(BreakableReadWriteLock.WRITE_LOCK_IS_SHARED), "the copy's breaks");
        assertTrue(copy.readLock().tryLock(), "a read tryLock() of the copy of a write-locked lock");
        copy.readLock().unlock();
    }

    /// Waits, for at most 5 s, until the thread in `thread` is waiting.
    private static void awaitWaiting(final AtomicReference<Thread> thread) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() - deadline < 0) {
            Thread t = thread.get();
            if (t != null && (t.getState() == Thread.State.WAITING || t.getState() == Thread.State.TIMED_WAITING)) {
                return;
            }
            Thread.onSpinWait();
        }
        throw new AssertionError("the thread was not seen waiting within 5 s");
    }

    private static void start(final Runnable task) {
        Thread thread = new Thread(task, "breakable-read-write-lock-test");
        thread.setDaemon(true);
        thread.start();
    }
}
