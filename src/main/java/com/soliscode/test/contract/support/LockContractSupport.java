/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.concurrent.locks.Lock;

import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// The base interface of the [Lock] contracts: the provider's locks, checked, what the class under test supports,
/// and the helpers of [LockingSupport].
///
/// @param <L> the type of the lock under test
/// @see com.soliscode.test.contract.lock.LockContract
/// @since 0.1.7
public interface LockContractSupport<L extends Lock> extends LockingSupport, LockProviderSupport<L> {

    /// Returns a new lock from the provider, checked: a null one fails the test with a message that says the provider
    /// is at fault.
    ///
    /// @return the lock
    default L newLock() {
        L lock = lockProvider().newLock();
        if (lock == null) {
            throw new AssertionFailedError("the lock provider is at fault: it gave a null lock");
        }
        return lock;
    }

    /// Whether the class under test supports conditions. `newCondition()`'s Javadoc allows it to throw
    /// [UnsupportedOperationException] "if this Lock implementation does not support conditions", as the views of a
    /// `StampedLock` do; such a class overrides this to return `false`, and the tests then check that it throws.
    ///
    /// @return `true` unless overridden
    default boolean supportsConditions() {
        return true;
    }

    /// Whether the class under test interrupts an acquisition that is waiting. `lockInterruptibly()` and the timed
    /// `tryLock` throw [InterruptedException] for a thread "interrupted while acquiring the lock, and interruption of
    /// lock acquisition is supported"; a class that does not support it overrides this to return `false`, and the
    /// tests then check what `Lock`'s Javadoc keeps for it: an interrupted waiter throws, or returns only once it has
    /// acquired the lock. An interrupt already set on entry is tested either way. `Lock`'s Javadoc lets the forms
    /// differ ("not required to define exactly the same guarantees or semantics for all three forms"); a lock that
    /// supports interruption in only one of `lockInterruptibly()` and the timed `tryLock` returns `false`, and both
    /// are then checked by the looser rule.
    ///
    /// @return `true` unless overridden
    default boolean interruptsLockAcquisition() {
        return true;
    }

    /// Asserts that an acquisition of a free lock acquires it: on a bounded thread, the call returns `true`, and
    /// another thread's `tryLock()` is then `false` until this thread unlocks it; after the unlock, another thread's
    /// `tryLock()` is `true`.
    ///
    /// @param lock        the lock, which no thread holds
    /// @param acquisition the call that acquires it
    /// @param what        the call, for the failure messages
    /// @throws Exception what the calls throw
    default void assertAcquires(final Lock lock, final Acquisition acquisition, final String what) throws Exception {
        bounded(() -> {
            assertTrue(acquisition.acquire(), () -> what + " of a lock no thread held returned false");
            try {
                assertFalse(tryLockFromAnotherThread(lock), () -> "after " + what + ", another thread's tryLock() "
                        + "took the lock: " + what + " did not acquire it");
            } finally {
                lock.unlock();
            }
            return null;
        });
        assertTrue(tryLockFromAnotherThread(lock), () -> "after " + what + " and unlock(), another thread's tryLock() "
                + "returned false");
    }

    /// Asserts that an acquisition of a lock another thread holds waits until that thread releases it, then acquires
    /// it ([#assertWaitsFor]): once the call returns, it must have returned `true`, after the release; and once the
    /// holder has unlocked and ended (so that a probe cannot find the holder's hold), another thread's `tryLock()`
    /// must be `false` until this one unlocks.
    ///
    /// @param lock        the lock, which no thread holds
    /// @param acquisition the call that acquires it
    /// @param what        the call, for the failure messages
    default void assertWaitsForTheHolder(final Lock lock, final Acquisition acquisition, final String what) {
        try (Holder holder = hold(lock)) {
            assertWaitsFor(acquisition, acquired -> {
                assertTrue(acquired, () -> what + " returned false once the holder released the lock");
                try {
                    assertTrue(holder.awaitUnlocked(), () -> "the holder had not unlocked within " + SPIN_LIMIT_MILLIS
                            + " ms after " + what + " returned");
                    assertFalse(tryLockFromAnotherThread(lock), () -> "after " + what + " returned, another thread's "
                            + "tryLock() took the lock: " + what + " did not acquire it");
                } finally {
                    lock.unlock();
                }
            }, holder::release);
        }
    }

    /// Asserts that an acquisition made with the interrupt status set, on a free lock, throws [InterruptedException],
    /// clears the status, and leaves the lock free. The acquisition unlocks the lock itself if it acquired it.
    ///
    /// @param lock        the lock, which no thread holds
    /// @param acquisition the call, which unlocks what it acquires
    /// @param what        the call, for the failure messages
    /// @throws Exception what the calls throw
    default void assertInterruptedOnEntry(final Lock lock, final Acquisition acquisition, final String what)
            throws Exception {
        Interrupted result = boundedInterrupted(acquisition);
        assertFalse(result.acquired(), () -> what + " with the interrupt status set acquired the lock");
        assertTrue(result.thrown() != null, () -> what + " with the interrupt status set did not throw "
                + "InterruptedException");
        assertFalse(result.stillMarked(), () -> what + " threw InterruptedException with the interrupt status still "
                + "set");
        assertTrue(tryLockFromAnotherThread(lock), () -> "after " + what + " threw InterruptedException, another "
                + "thread's tryLock() returned false: the lock was left held");
    }
}
