/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.LockContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [unlock()][Lock#unlock()] method of a [Lock].
///
/// It follows the Javadoc of [Lock#unlock()], "Releases the lock": once released, another thread can take it, and a
/// thread waiting for it acquires it. Which thread may unlock is not tested: the Javadoc says an implementation "will
/// usually impose restrictions" and "may throw" if they are broken, and the views of a `StampedLock` impose none.
///
/// @param <L> the type of the lock under test
/// @since 0.1.7
/// @see Lock#unlock()
public interface UnlockContract<L extends Lock> extends LockContractSupport<L> {

    /// The number of threads ({@value}) waiting for the lock in [#unlockWakesAWaitingThread()].
    int WAITERS = 3;

    /// Tests that `unlock()` releases the lock, however it was acquired: after each of `lock()`,
    /// `lockInterruptibly()`, `tryLock()` and the timed `tryLock`, and `unlock()`, another thread can take it.
    ///
    /// @throws Exception if a call on the lock throws
    @Test
    @DisplayName("unlock() releases the lock")
    default void unlockReleasesTheLock() throws Exception {
        L lock = newLock();
        List<Acquisition> acquisitions = List.of(() -> {
            lock.lock();
            return true;
        }, () -> {
            lock.lockInterruptibly();
            return true;
        }, lock::tryLock, () -> lock.tryLock(0, TimeUnit.MILLISECONDS));
        List<String> names = List.of("lock()", "lockInterruptibly()", "tryLock()", "tryLock(0, MILLISECONDS)");
        for (int i = 0; i < acquisitions.size(); i++) {
            Acquisition acquisition = acquisitions.get(i);
            String name = names.get(i);
            Boolean acquired = bounded(() -> {
                if (!acquisition.acquire()) {
                    return false;
                }
                lock.unlock();
                return true;
            });
            assertTrue(acquired, () -> name + " of a free lock returned false");
            assertTrue(tryLockFromAnotherThread(lock), () -> "after " + name + " and unlock(), another thread's "
                    + "tryLock() returned false: unlock() did not release the lock");
        }
    }

    /// Tests that each `unlock()` wakes a thread waiting in `lock()`: [#WAITERS] threads wait for a lock another
    /// thread holds; once it is released, each acquires it and unlocks it, so that every one acquires it. That they
    /// hold it one at a time is [ThreadSafeLockingContract]'s to test, under contention: a lock that is not exclusive
    /// there does not fail this test.
    @Test
    @DisplayName("unlock() wakes the threads waiting for the lock, one after another")
    default void unlockWakesAWaitingThread() {
        L lock = newLock();
        Queue<Thread> waiters = new ConcurrentLinkedQueue<>();
        AtomicInteger acquired = new AtomicInteger();
        try (Holder holder = hold(lock)) {
            ContentionCoordinator<Object> coordinator = coordinator();
            coordinator.add(WAITERS, 1, o -> {
                waiters.add(Thread.currentThread());
                lock.lock();
                try {
                    acquired.incrementAndGet();
                } finally {
                    lock.unlock();
                }
            });
            coordinator.add(1, 1, o -> {
                long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SPIN_LIMIT_MILLIS);
                while ((waiters.size() < WAITERS || !waiters.stream().allMatch(this::isWaiting))
                        && System.nanoTime() - deadline < 0) {
                    Thread.onSpinWait();
                }
                holder.release();
            });
            coordinator.execute(new Object());
        }
        assertEquals(WAITERS, acquired.get(), "the waiters that acquired the lock once it was released");
    }
}
