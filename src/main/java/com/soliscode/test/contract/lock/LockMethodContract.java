/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.LockContractSupport;

/// Contract for the [lock()][Lock#lock()] method of a [Lock]. (It is not named `LockContract`, which is the
/// interface's.)
///
/// It follows the Javadoc of [Lock#lock()]: it "Acquires the lock. If the lock is not available then the current
/// thread becomes disabled for thread scheduling purposes and lies dormant until the lock has been acquired." It reads
/// "acquires" as exclusive, as [LockContract] says.
///
/// @param <L> the type of the lock under test
/// @since 0.1.7
/// @see Lock#lock()
public interface LockMethodContract<L extends Lock> extends LockContractSupport<L> {

    /// Tests that `lock()` of a free lock acquires it: another thread cannot take it until it is unlocked.
    ///
    /// @throws Exception if a call on the lock throws
    @Test
    @DisplayName("lock() acquires a free lock")
    default void lockAcquiresTheLock() throws Exception {
        L lock = newLock();
        assertAcquires(lock, () -> {
            lock.lock();
            return true;
        }, "lock()");
    }

    /// Tests that `lock()` of a lock another thread holds waits until that thread releases it, and then acquires it.
    @Test
    @DisplayName("lock() waits until another thread releases the lock")
    default void lockWaitsForTheHolder() {
        L lock = newLock();
        assertWaitsForTheHolder(lock, () -> {
            lock.lock();
            return true;
        }, "lock()");
    }
}
