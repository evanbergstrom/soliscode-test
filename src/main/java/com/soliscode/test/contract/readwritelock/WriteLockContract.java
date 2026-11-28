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

/// Contract for the [writeLock()][ReadWriteLock#writeLock()] method of a [ReadWriteLock]: what the lock it returns
/// does.
///
/// It follows the Javadoc of [ReadWriteLock]: "The write lock is exclusive", of other writers and of readers. The
/// write lock is a [java.util.concurrent.locks.Lock]: a test class runs
/// [com.soliscode.test.contract.lock.LockContract] on it with
/// [com.soliscode.test.provider.LockProviders#writeLocksOf], for what `Lock` says of it.
///
/// @param <L> the type of the read-write lock under test
/// @since 0.1.7
/// @see ReadWriteLock#writeLock()
public interface WriteLockContract<L extends ReadWriteLock> extends ReadWriteLockContractSupport<L> {

    /// Tests that the write lock is exclusive of other writers: while another thread holds it, this thread's write
    /// `tryLock()` returns `false`; once it is released, `true`.
    ///
    /// @throws Exception if a call on the lock throws
    @Test
    @DisplayName("the write lock is exclusive of other writers")
    default void writeLockIsExclusive() throws Exception {
        L lock = newReadWriteLock();
        Holder holder = hold(lock.writeLock());
        try {
            assertFalse(tryLockFromAnotherThread(lock.writeLock()), "a write tryLock() while another thread held "
                    + "the write lock");
        } finally {
            holder.close();
        }
        assertTrue(holder.awaitUnlocked(), "the other writer had not unlocked within " + SPIN_LIMIT_MILLIS + " ms");
        assertTrue(tryLockFromAnotherThread(lock.writeLock()), "a write tryLock() once the other thread had "
                + "released the write lock");
    }

    /// Tests that the write lock waits for readers: while another thread holds the read lock, `writeLock().lock()`
    /// does not return until it is released; then it holds the write lock, and another thread can take neither lock
    /// until it unlocks.
    @Test
    @DisplayName("the write lock waits until the readers release the read lock")
    default void writeLockWaitsForReaders() {
        L lock = newReadWriteLock();
        try (Holder holder = hold(lock.readLock())) {
            assertWaitsFor(() -> {
                lock.writeLock().lock();
                return true;
            }, acquired -> {
                try {
                    assertTrue(holder.awaitUnlocked(), "the reader had not unlocked after the write lock was taken");
                    assertFalse(tryLockFromAnotherThread(lock.readLock()), "a read tryLock() while another thread "
                            + "held the write lock");
                    assertFalse(tryLockFromAnotherThread(lock.writeLock()), "a write tryLock() while another "
                            + "thread held the write lock");
                } finally {
                    lock.writeLock().unlock();
                }
            }, holder::release);
        }
    }
}
