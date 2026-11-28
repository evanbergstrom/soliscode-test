/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.support.LockContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/// **Contract for a `Lock` under contention**
///
/// [Lock]'s Javadoc: "All Lock implementations must enforce the same memory synchronization semantics as provided by
/// the built-in monitor lock": a successful lock has the effect of a monitor Lock action, an unlock that of an
/// Unlock action. With exclusion, updates made under the lock are neither lost nor torn. The test checks exclusion
/// under contention. Its plain fields are checked too, but the atomic counter of threads in, which a thread updates
/// after it writes them and the next reads before it reads them, orders them by itself: so the test shows nothing of
/// the lock's own memory effects, and a lock that excludes but gives no happens-before edge passes it.
///
/// [#LOCKERS] threads each acquire the lock [#ACQUISITIONS] times, cycling through `lock()`, `lockInterruptibly()`,
/// `tryLock()` and `tryLock(1, SECONDS)`, the two `tryLock` forms retried while they return `false`. Under the lock, a
/// thread counts itself in (and records an overlap if another thread is in), increments one plain field, spins
/// [#HOLD_SPINS] times, increments a second and checks they are equal, and counts itself out (recording an overlap if
/// another thread came in meanwhile). The spin and the second check let an acquisition that is not exclusive be seen
/// overlapping, though the other thread's stay is short. At the end: no overlap, no torn pair, and both fields
/// `LOCKERS × ACQUISITIONS`.
///
/// The run is bounded in time: a [ContentionCoordinator] with a start of [#START_TIMEOUT_SECONDS] and a shutdown of
/// [#SHUTDOWN_TIMEOUT_SECONDS], and a deadline of [#DEADLINE_MILLIS] of its own, by which each thread must have
/// finished, as [com.soliscode.test.contract.future.ThreadSafeGetContract]'s.
///
/// @param <L> the type of the lock under test
/// @see Lock
/// @since 0.1.7
public interface ThreadSafeLockingContract<L extends Lock> extends LockContractSupport<L> {

    /// The number of threads ({@value}) that contend for the lock.
    int LOCKERS = 4;

    /// The number of times ({@value}) each thread acquires the lock.
    int ACQUISITIONS = 10_000;

    /// The time ({@value} ms) by which every thread must have finished.
    long DEADLINE_MILLIS = 3_000;

    /// The coordinator's start timeout ({@value} s).
    int START_TIMEOUT_SECONDS = 1;

    /// The coordinator's shutdown timeout ({@value} s).
    int SHUTDOWN_TIMEOUT_SECONDS = 4;

    /// The number of ways ({@value}) the test acquires the lock.
    int ACQUISITION_FORMS = 4;

    /// How many times ({@value}) a thread spins, holding the lock, between its two checks for another thread in, so
    /// that an acquisition that is not exclusive overlaps with another for long enough to be seen.
    int HOLD_SPINS = 64;

    /// Tests that threads acquiring the lock at once, in each of its four ways, hold it one at a time, and that what
    /// they change under it is neither lost nor torn.
    @DisplayName("lock, lockInterruptibly and tryLock called by several threads at once are mutually exclusive")
    @VerySlow
    @ConcurrencyTest
    default void lockUnderContentionIsMutuallyExclusive() {
        L lock = newLock();
        int[] pair = new int[2];
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger overlaps = new AtomicInteger();
        AtomicInteger torn = new AtomicInteger();
        AtomicReference<@Nullable String> failure = new AtomicReference<>();
        ContentionCoordinator<Lock> coordinator = new ContentionCoordinator<>(START_TIMEOUT_SECONDS,
                SHUTDOWN_TIMEOUT_SECONDS, ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
        coordinator.add(LOCKERS, 1, l -> {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(DEADLINE_MILLIS);
            for (int i = 0; i < ACQUISITIONS; i++) {
                try {
                    if (!acquireUntil(l, i % ACQUISITION_FORMS, deadline)) {
                        failure.compareAndSet(null, "the lockers did not finish within " + DEADLINE_MILLIS + " ms");
                        return;
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    if (inside.incrementAndGet() != 1) {
                        overlaps.incrementAndGet();
                    }
                    pair[0]++;
                    hold();
                    pair[1]++;
                    if (pair[0] != pair[1]) {
                        torn.incrementAndGet();
                    }
                    if (inside.decrementAndGet() != 0) {
                        overlaps.incrementAndGet();
                    }
                } finally {
                    l.unlock();
                }
            }
        });
        coordinator.execute(lock);
        assertNull(failure.get(), failure::get);
        assertEquals(0, overlaps.get(), "the acquisitions that found another thread holding the lock");
        assertEquals(0, torn.get(), "the acquisitions that saw the two fields differ");
        assertEquals(LOCKERS * ACQUISITIONS, pair[0], "the first field, incremented under the lock");
        assertEquals(LOCKERS * ACQUISITIONS, pair[1], "the second field, incremented under the lock");
    }

    /// Spins [#HOLD_SPINS] times, holding the lock.
    private static void hold() {
        for (int s = 0; s < HOLD_SPINS; s++) {
            Thread.onSpinWait();
        }
    }

    /// Acquires the lock in one of the four ways, retrying the `tryLock` forms while they return `false`, until
    /// `deadline`.
    ///
    /// @return `true` if it acquired the lock, `false` if the deadline passed first
    private static boolean acquireUntil(final Lock lock, final int form, final long deadline)
            throws InterruptedException {
        switch (form) {
            case 0 -> lock.lock();
            case 1 -> lock.lockInterruptibly();
            case 2 -> {
                while (!lock.tryLock()) {
                    if (System.nanoTime() - deadline > 0) {
                        return false;
                    }
                    Thread.onSpinWait();
                }
            }
            default -> {
                while (!lock.tryLock(1, TimeUnit.SECONDS)) {
                    if (System.nanoTime() - deadline > 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
