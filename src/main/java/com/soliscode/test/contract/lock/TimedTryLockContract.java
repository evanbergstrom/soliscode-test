/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.LockContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [tryLock(long, TimeUnit)][Lock#tryLock(long, TimeUnit)] method of a [Lock].
///
/// It follows the Javadoc of [Lock#tryLock(long, TimeUnit)]: "If the lock is available this method returns
/// immediately with the value true"; otherwise it waits until the lock is acquired, the thread is interrupted, or "the
/// specified waiting time elapses", when it returns `false`; "If the time is less than or equal to zero, the method
/// will not wait at all." It reads the interrupt rules as [LockInterruptiblyContract] does, and the interrupt test
/// while waiting gives the call [#BLOCKING_TIMEOUT_SECONDS], so that a correct lock is still waiting when the
/// interrupt comes. A null `TimeUnit` is not tested, as [com.soliscode.test.contract.future.TimedGetContract] leaves
/// it out.
///
/// @param <L> the type of the lock under test
/// @since 0.1.7
/// @see Lock#tryLock(long, TimeUnit)
public interface TimedTryLockContract<L extends Lock> extends LockContractSupport<L> {

    /// Tests that the timed `tryLock` of a free lock, with a timeout of zero, returns `true` and acquires it.
    ///
    /// @throws Exception if a call on the lock throws
    @Test
    @DisplayName("tryLock(0, MILLISECONDS) of a free lock returns true and acquires it")
    default void timedTryLockOfAFreeLockAcquiresItAtOnce() throws Exception {
        L lock = newLock();
        assertAcquires(lock, () -> lock.tryLock(0, TimeUnit.MILLISECONDS), "tryLock(0, MILLISECONDS)");
    }

    /// Tests that the timed `tryLock` of a lock another thread holds throughout returns `false` once its time has
    /// elapsed, no sooner, and does not take the lock.
    ///
    /// @throws Exception if a call on the lock throws
    @Test
    @DisplayName("tryLock(time, unit) of a held lock returns false once the time has elapsed")
    default void timedTryLockTimesOutWhileHeld() throws Exception {
        L lock = newLock();
        long[] elapsed = new long[1];
        Holder holder = hold(lock);
        try {
            Boolean acquired = bounded(() -> {
                long start = System.nanoTime();
                boolean got = lock.tryLock(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS);
                elapsed[0] = System.nanoTime() - start;
                if (got) {
                    lock.unlock();
                }
                return got;
            });
            assertFalse(acquired, "tryLock(" + TIMED_WAIT_MILLIS + ", MILLISECONDS) while another thread held the "
                    + "lock");
        } finally {
            holder.close();
        }
        assertTrue(holder.awaitUnlocked(), "the holder had not unlocked within " + SPIN_LIMIT_MILLIS + " ms");
        assertTrue(elapsed[0] >= TimeUnit.MILLISECONDS.toNanos(TIMED_WAIT_MILLIS), () -> "tryLock(" + TIMED_WAIT_MILLIS
                + ", MILLISECONDS) returned false after " + elapsed[0] + " ns, before its time had elapsed");
        assertTrue(tryLockFromAnotherThread(lock), "tryLock() once the holder had released the lock: the timed "
                + "tryLock left it held");
    }

    /// Tests that the timed `tryLock` of a lock another thread holds waits until that thread releases it, within
    /// its time, and then acquires it and returns `true`.
    @Test
    @DisplayName("tryLock(time, unit) waits until another thread releases the lock")
    default void timedTryLockWaitsForTheHolder() {
        L lock = newLock();
        assertWaitsForTheHolder(lock, () -> lock.tryLock(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "tryLock(" + BLOCKING_TIMEOUT_SECONDS + ", SECONDS)");
    }

    /// Tests that the timed `tryLock` with the interrupt status set throws [InterruptedException] on a free lock,
    /// clears the status, and does not take the lock.
    ///
    /// @throws Exception if a call on the lock throws other than as tested
    @Test
    @DisplayName("tryLock(time, unit) throws InterruptedException when interrupted on entry")
    default void timedTryLockThrowsInterruptedExceptionWhenInterruptedOnEntry() throws Exception {
        L lock = newLock();
        assertInterruptedOnEntry(lock, () -> {
            boolean got = lock.tryLock(0, TimeUnit.MILLISECONDS);
            if (got) {
                lock.unlock();
            }
            return got;
        }, "tryLock(0, MILLISECONDS)");
    }

    /// Tests that the timed `tryLock`, interrupted while it waits for a lock another thread holds, throws
    /// [InterruptedException] while that thread still holds it; or, where [#interruptsLockAcquisition()] is `false`,
    /// throws or returns `true` only once it has acquired the lock.
    @Test
    @DisplayName("tryLock(time, unit) throws InterruptedException when interrupted while waiting")
    default void timedTryLockThrowsInterruptedExceptionWhenInterruptedWhileWaiting() {
        L lock = newLock();
        assertInterruptedWhileWaiting(lock, () -> lock.tryLock(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                interruptsLockAcquisition());
    }
}
