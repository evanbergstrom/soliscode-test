/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.LockContractSupport;

/// Contract for the [lockInterruptibly()][Lock#lockInterruptibly()] method of a [Lock].
///
/// It follows the Javadoc of [Lock#lockInterruptibly()]: it acquires the lock "unless the current thread is
/// interrupted", waiting while it is not available, and throws [InterruptedException], clearing the interrupt status,
/// if the thread "has its interrupted status set on entry to this method; or is interrupted while acquiring the lock,
/// and interruption of lock acquisition is supported".
///
/// How it reads it:
/// - The method also says "Acquires the lock if it is available and returns immediately". On a free lock with the
///   interrupt status set, the contract reads the on-entry rule as taking precedence: the class Javadoc says a lock
///   obeys the interruption semantics "either totally, or only on method entry", so every lock throws on entry. It is
///   not opted out.
/// - An interrupt while waiting is tested strictly unless [#interruptsLockAcquisition()] is overridden to return
///   `false`; the `InterruptedException` must then come while the other thread still holds the lock. With it
///   `false`, the interrupted waiter must throw, or return only once it has acquired the lock.
///
/// @param <L> the type of the lock under test
/// @since 0.1.7
/// @see Lock#lockInterruptibly()
public interface LockInterruptiblyContract<L extends Lock> extends LockContractSupport<L> {

    /// Tests that `lockInterruptibly()` of a free lock acquires it.
    ///
    /// @throws Exception if a call on the lock throws
    @Test
    @DisplayName("lockInterruptibly() acquires a free lock")
    default void lockInterruptiblyAcquiresTheLock() throws Exception {
        L lock = newLock();
        assertAcquires(lock, () -> {
            lock.lockInterruptibly();
            return true;
        }, "lockInterruptibly()");
    }

    /// Tests that `lockInterruptibly()` of a lock another thread holds waits until that thread releases it, and then
    /// acquires it.
    @Test
    @DisplayName("lockInterruptibly() waits until another thread releases the lock")
    default void lockInterruptiblyWaitsForTheHolder() {
        L lock = newLock();
        assertWaitsForTheHolder(lock, () -> {
            lock.lockInterruptibly();
            return true;
        }, "lockInterruptibly()");
    }

    /// Tests that `lockInterruptibly()` with the interrupt status set throws [InterruptedException] on a free lock,
    /// clears the status, and does not take the lock.
    ///
    /// @throws Exception if a call on the lock throws other than as tested
    @Test
    @DisplayName("lockInterruptibly() throws InterruptedException when interrupted on entry")
    default void lockInterruptiblyThrowsInterruptedExceptionWhenInterruptedOnEntry() throws Exception {
        L lock = newLock();
        assertInterruptedOnEntry(lock, () -> {
            lock.lockInterruptibly();
            lock.unlock();
            return true;
        }, "lockInterruptibly()");
    }

    /// Tests that `lockInterruptibly()`, interrupted while it waits for a lock another thread holds, throws
    /// [InterruptedException] while that thread still holds it; or, where [#interruptsLockAcquisition()] is `false`,
    /// throws or returns only once it has acquired the lock.
    @Test
    @DisplayName("lockInterruptibly() throws InterruptedException when interrupted while waiting")
    default void lockInterruptiblyThrowsInterruptedExceptionWhenInterruptedWhileWaiting() {
        L lock = newLock();
        assertInterruptedWhileWaiting(lock, () -> {
            lock.lockInterruptibly();
            return true;
        }, interruptsLockAcquisition());
    }
}
