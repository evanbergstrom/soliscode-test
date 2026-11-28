/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.readwritelock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.support.ReadWriteLockContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/// **Contract for a `ReadWriteLock` under contention**
///
/// [ReadWriteLock]'s Javadoc: "The read lock may be held simultaneously by multiple reader threads, so long as there
/// are no writers. The write lock is exclusive", and "a thread successfully acquiring the read lock will see all
/// updates made upon previous release of the write lock".
///
/// [#WRITERS] threads each take the write lock [#WRITES] times, and [#READERS] threads each take the read lock
/// [#READS] times, all at once, each alternating `lock()` and `tryLock()` (retried while it returns `false`). A writer
/// checks that it is the only writer and that no reader is in, and increments two plain fields one after the other; a
/// reader checks that no writer is in, and that the two fields are equal. Each spins [#HOLD_SPINS] times between its
/// two reads or writes, and checks again before it leaves, so that an acquisition that does not exclude is seen
/// overlapping. At the end: no overlap, no torn read, and both fields `WRITERS × WRITES`. As
/// [com.soliscode.test.contract.lock.ThreadSafeLockingContract] says, this shows exclusion only: the atomic counters
/// of writers and readers in order the plain fields by themselves, so a lock that excludes but gives no
/// happens-before edge passes it, though the Javadoc's "will see all updates" is not met.
///
/// The run is bounded as that contract's is: a [ContentionCoordinator] with a start of [#START_TIMEOUT_SECONDS] and a
/// shutdown of [#SHUTDOWN_TIMEOUT_SECONDS], and a deadline of [#DEADLINE_MILLIS] of its own. They are its own
/// constants, the same values, since `contract.readwritelock` does not depend on `contract.lock`: `ReadWriteLock`
/// does not extend `Lock`.
///
/// @param <L> the type of the read-write lock under test
/// @see ReadWriteLock
/// @since 0.1.7
public interface ThreadSafeReadWriteContract<L extends ReadWriteLock> extends ReadWriteLockContractSupport<L> {

    /// The number of threads ({@value}) that take the write lock.
    int WRITERS = 3;

    /// The number of times ({@value}) each writer takes it.
    int WRITES = 5_000;

    /// The number of threads ({@value}) that take the read lock.
    int READERS = 3;

    /// The number of times ({@value}) each reader takes it.
    int READS = 5_000;

    /// How many times ({@value}) a thread spins, holding its lock, between its two checks for a thread it excludes,
    /// so that an acquisition that does not exclude overlaps for long enough to be seen.
    int HOLD_SPINS = 64;

    /// The time ({@value} ms) by which every thread must have finished.
    long DEADLINE_MILLIS = 3_000;

    /// The coordinator's start timeout ({@value} s).
    int START_TIMEOUT_SECONDS = 1;

    /// The coordinator's shutdown timeout ({@value} s).
    int SHUTDOWN_TIMEOUT_SECONDS = 4;

    /// Tests that writers and readers taking the lock at once exclude as `ReadWriteLock` says, and that what writers
    /// change is neither lost nor seen torn.
    @DisplayName("readLock and writeLock taken by several threads at once exclude writers from each other and readers")
    @VerySlow
    @ConcurrencyTest
    default void readWriteLockUnderContentionIsSafe() {
        L lock = newReadWriteLock();
        int[] pair = new int[2];
        AtomicInteger writersIn = new AtomicInteger();
        AtomicInteger readersIn = new AtomicInteger();
        AtomicInteger overlaps = new AtomicInteger();
        AtomicInteger torn = new AtomicInteger();
        AtomicReference<@Nullable String> failure = new AtomicReference<>();
        ContentionCoordinator<ReadWriteLock> coordinator = new ContentionCoordinator<>(START_TIMEOUT_SECONDS,
                SHUTDOWN_TIMEOUT_SECONDS, ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
        long deadlineMillis = DEADLINE_MILLIS;
        coordinator.add(WRITERS, 1, rw -> {
            Lock write = rw.writeLock();
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(deadlineMillis);
            for (int i = 0; i < WRITES; i++) {
                if (!acquireUntil(write, i % 2 == 0, deadline)) {
                    failure.compareAndSet(null, "the writers did not finish within " + deadlineMillis + " ms");
                    return;
                }
                try {
                    overlaps.addAndGet(writeHeld(pair, writersIn, readersIn));
                } finally {
                    write.unlock();
                }
            }
        });
        coordinator.add(READERS, 1, rw -> {
            Lock read = rw.readLock();
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(deadlineMillis);
            for (int i = 0; i < READS; i++) {
                if (!acquireUntil(read, i % 2 == 0, deadline)) {
                    failure.compareAndSet(null, "the readers did not finish within " + deadlineMillis + " ms");
                    return;
                }
                try {
                    int found = readHeld(pair, writersIn, readersIn);
                    overlaps.addAndGet(found & 1);
                    torn.addAndGet(found >> 1);
                } finally {
                    read.unlock();
                }
            }
        });
        coordinator.execute(lock);
        assertNull(failure.get(), failure::get);
        assertEquals(0, overlaps.get(), "the acquisitions that found a writer with another writer or a reader");
        assertEquals(0, torn.get(), "the reads that saw the two fields differ");
        assertEquals(WRITERS * WRITES, pair[0], "the first field, incremented under the write lock");
        assertEquals(WRITERS * WRITES, pair[1], "the second field, incremented under the write lock");
    }

    /// A writer's work while it holds the write lock: counts itself in, increments the two fields with a spin
    /// between, and counts itself out, checking on entry and on leaving that no other writer or reader is in.
    ///
    /// @return the number of overlaps it saw, 0 to 2
    private static int writeHeld(final int[] pair, final AtomicInteger writersIn, final AtomicInteger readersIn) {
        int found = 0;
        if (writersIn.incrementAndGet() != 1 || readersIn.get() != 0) {
            found++;
        }
        pair[0]++;
        hold();
        pair[1]++;
        if (writersIn.decrementAndGet() != 0 || readersIn.get() != 0) {
            found++;
        }
        return found;
    }

    /// A reader's work while it holds the read lock: counts itself in, reads the two fields with a spin between, and
    /// counts itself out, checking on entry and before it leaves that no writer is in.
    ///
    /// @return 1 if it saw a writer in, plus 2 if it saw the fields differ
    private static int readHeld(final int[] pair, final AtomicInteger writersIn, final AtomicInteger readersIn) {
        readersIn.incrementAndGet();
        boolean overlap = writersIn.get() != 0;
        int first = pair[0];
        hold();
        boolean tornRead = first != pair[1];
        overlap |= writersIn.get() != 0;
        readersIn.decrementAndGet();
        return (overlap ? 1 : 0) + (tornRead ? 2 : 0);
    }

    /// Spins [#HOLD_SPINS] times, holding a lock.
    private static void hold() {
        for (int s = 0; s < HOLD_SPINS; s++) {
            Thread.onSpinWait();
        }
    }

    /// Takes a lock with `lock()`, or with `tryLock()` retried while it returns `false`, until `deadline`.
    ///
    /// @return `true` if it took the lock, `false` if the deadline passed first
    private static boolean acquireUntil(final Lock lock, final boolean blocking, final long deadline) {
        if (blocking) {
            lock.lock();
            return true;
        }
        while (!lock.tryLock()) {
            if (System.nanoTime() - deadline > 0) {
                return false;
            }
            Thread.onSpinWait();
        }
        return true;
    }
}
