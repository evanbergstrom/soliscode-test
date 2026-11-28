/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.readwritelock;

import java.util.concurrent.locks.ReadWriteLock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ReadWriteLockContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [readLock()][ReadWriteLock#readLock()] method of a [ReadWriteLock]: what the lock it returns does.
///
/// It follows the Javadoc of [ReadWriteLock]: "The read lock may be held simultaneously by multiple reader threads, so
/// long as there are no writers." It reads that "may" strictly, as the defining capability of a read-write lock (the
/// class Javadoc: "any number of threads can concurrently read"), so a read-write lock whose read lock is exclusive
/// fails it.
///
/// Not tested: the read lock's `newCondition()` (`ReentrantReadWriteLock.ReadLock` throws
/// [UnsupportedOperationException]), downgrading or upgrading, fairness, and whether `readLock()` returns the same
/// object each time, which the Javadoc does not say.
///
/// @param <L> the type of the read-write lock under test
/// @since 0.1.7
/// @see ReadWriteLock#readLock()
public interface ReadLockContract<L extends ReadWriteLock> extends ReadWriteLockContractSupport<L> {

    /// Tests that the read lock is shared: while another thread holds it, this thread's read `tryLock()` takes it.
    ///
    /// @throws Exception if a call on the lock throws
    @Test
    @DisplayName("the read lock is shared between readers")
    default void readLockIsShared() throws Exception {
        L lock = newReadWriteLock();
        Holder holder = hold(lock.readLock());
        try {
            assertTrue(tryLockFromAnotherThread(lock.readLock()), "a read tryLock() while another thread held the "
                    + "read lock");
        } finally {
            holder.close();
        }
    }

    /// Tests that the read lock waits for the writer: while another thread holds the write lock, `readLock().lock()`
    /// does not return until it is released; then it holds the read lock, and another thread cannot take the write
    /// lock until it unlocks.
    @Test
    @DisplayName("the read lock waits until the writer releases the write lock")
    default void readLockWaitsForTheWriter() {
        L lock = newReadWriteLock();
        try (Holder holder = hold(lock.writeLock())) {
            assertWaitsFor(() -> {
                lock.readLock().lock();
                return true;
            }, acquired -> {
                try {
                    assertTrue(holder.awaitUnlocked(), "the writer had not unlocked after the read lock was taken");
                    assertFalse(tryLockFromAnotherThread(lock.writeLock()), "a write tryLock() while another thread "
                            + "held the read lock");
                } finally {
                    lock.readLock().unlock();
                }
            }, holder::release);
        }
    }
}
