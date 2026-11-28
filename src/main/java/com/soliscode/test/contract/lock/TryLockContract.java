/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.LockContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [tryLock()][Lock#tryLock()] method of a [Lock].
///
/// It follows the Javadoc of [Lock#tryLock()]: it "Acquires the lock if it is available and returns immediately with
/// the value true. If the lock is not available then this method will return immediately with the value false."
///
/// @param <L> the type of the lock under test
/// @since 0.1.7
/// @see Lock#tryLock()
public interface TryLockContract<L extends Lock> extends LockContractSupport<L> {

    /// Tests that `tryLock()` of a free lock returns `true` and acquires it.
    ///
    /// @throws Exception if a call on the lock throws
    @Test
    @DisplayName("tryLock() of a free lock returns true and acquires it")
    default void tryLockOfAFreeLockAcquiresIt() throws Exception {
        L lock = newLock();
        assertAcquires(lock, lock::tryLock, "tryLock()");
    }

    /// Tests that `tryLock()` of a lock another thread holds returns `false` at once, without waiting for that
    /// thread (which holds it past the bounded call) and without taking the lock.
    ///
    /// @throws Exception if a call on the lock throws
    @Test
    @DisplayName("tryLock() of a held lock returns false at once")
    default void tryLockOfAHeldLockReturnsFalseAtOnce() throws Exception {
        L lock = newLock();
        Holder holder = hold(lock);
        try {
            assertFalse(tryLockFromAnotherThread(lock), "tryLock() while another thread held the lock");
        } finally {
            holder.close();
        }
        assertTrue(holder.awaitUnlocked(), "the holder had not unlocked within " + SPIN_LIMIT_MILLIS + " ms");
        assertTrue(tryLockFromAnotherThread(lock), "tryLock() once the holder had released the lock: an earlier "
                + "tryLock() left it held");
    }
}
