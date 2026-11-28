/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.LockContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [newCondition()][Lock#newCondition()] method of a [Lock], and for what [Condition]'s Javadoc
/// says of the lock its conditions are bound to.
///
/// It follows the Javadoc of [Lock#newCondition()]: it "Returns a new Condition instance that is bound to this Lock
/// instance", or throws [UnsupportedOperationException] "if this Lock implementation does not support conditions",
/// which a test class says by overriding [#supportsConditions()]. And of [Condition#await()]: "The lock associated
/// with this Condition is atomically released"; "Before this method can return the current thread must re-acquire
/// the lock"; [Condition#signal()] "Wakes up one waiting thread", and [Condition#signalAll()] "Wakes up all waiting
/// threads".
///
/// Each waiter waits in a loop on a flag, as `Condition`'s Javadoc says spurious wakeups require, so that a spurious
/// wakeup does not fail a correct lock. No signal comes before a wait: each waiter takes the lock and counts down a
/// latch before it waits, and the signaller takes the lock only after the latch, which it can do only once every
/// waiter has released it in `await`. Every wait is bounded by a [ContentionCoordinator], and every `newCondition()`
/// by [#bounded]. The tests of the condition
/// pass at once where [#supportsConditions()] is `false`.
///
/// Not tested: `Condition`'s other methods (`awaitNanos`, `awaitUninterruptibly`, `awaitUntil`, the timed `await`)
/// and its interruption, which are `Condition`'s contract, not `Lock`'s; and that `signal` wakes only one thread,
/// which spurious wakeups make unobservable.
///
/// @param <L> the type of the lock under test
/// @since 0.1.7
/// @see Lock#newCondition()
/// @see Condition
public interface NewConditionContract<L extends Lock> extends LockContractSupport<L> {

    /// The number of threads ({@value}) waiting on the condition in [#signalAllWakesEveryWaitingThread()].
    int CONDITION_WAITERS = 3;

    /// A waiter's checks once its `await` loop has ended, while it holds the lock.
    @FunctionalInterface
    interface AfterAwait {
        /// Runs the checks.
        ///
        /// @throws Exception what the checks throw
        void run() throws Exception;
    }

    /// Tests that `newCondition()` returns a new condition each call where the class supports conditions, and throws
    /// [UnsupportedOperationException] where it does not.
    ///
    /// @throws Exception if a call on the lock or its condition throws other than as tested
    @Test
    @DisplayName("newCondition() returns a new condition, or throws UnsupportedOperationException")
    default void newConditionReturnsANewCondition() throws Exception {
        L lock = newLock();
        if (!supportsConditions()) {
            assertThrows(UnsupportedOperationException.class, () -> bounded(lock::newCondition),
                    "newCondition() of a lock the test class says does not support conditions");
            return;
        }
        Condition first = bounded(lock::newCondition);
        Condition second = bounded(lock::newCondition);
        assertNotNull(first, "newCondition()");
        assertNotNull(second, "a second newCondition()");
        assertNotSame(first, second, "two calls of newCondition(): it returns a new Condition instance");
    }

    /// Tests that a thread that holds the lock and awaits a condition releases the lock while it waits: another
    /// thread can take it, within [#SPIN_LIMIT_MILLIS], and signal.
    ///
    /// @throws Exception if a call on the lock or its condition throws other than as tested
    @Test
    @DisplayName("await() releases the lock while it waits")
    default void awaitReleasesTheLock() throws Exception {
        if (!supportsConditions()) {
            return;
        }
        L lock = newLock();
        Condition condition = bounded(lock::newCondition);
        AtomicBoolean signalled = new AtomicBoolean();
        CountDownLatch waiting = new CountDownLatch(1);
        AtomicReference<@Nullable Thread> waiter = new AtomicReference<>();
        AtomicReference<@Nullable String> failure = new AtomicReference<>();
        ContentionCoordinator<Object> coordinator = coordinator();
        coordinator.add(1, 1, unchecked(() -> {
            waiter.set(Thread.currentThread());
            awaitSignal(lock, condition, signalled, waiting, () -> { }, () -> { });
        }));
        coordinator.add(1, 1, unchecked(() -> {
            if (!waiting.await(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                failure.set("the waiter did not take the lock within " + BLOCKING_TIMEOUT_SECONDS + " s");
                return;
            }
            if (!lock.tryLock(SPIN_LIMIT_MILLIS, TimeUnit.MILLISECONDS)) {
                failure.set("another thread could not take the lock within " + SPIN_LIMIT_MILLIS + " ms while a "
                        + "thread awaited a condition: await() did not release it");
                Thread stuck = waiter.get();
                if (stuck != null) {
                    stuck.interrupt();
                }
                return;
            }
            try {
                signalled.set(true);
                condition.signal();
            } finally {
                lock.unlock();
            }
        }));
        try {
            coordinator.execute(new Object());
        } catch (final AssertionError e) {
            if (failure.get() == null) {
                throw e;
            }
        }
        assertNull(failure.get(), failure::get);
    }

    /// Tests that `await()` takes the lock again before it returns: it does not return before the signaller, which
    /// holds the lock while it signals, unlocks; and once it returns and the signaller's `unlock()` has returned (so
    /// that a probe cannot find the signaller's hold), another thread cannot take the lock until the waiter unlocks.
    ///
    /// @throws Exception if a call on the lock or its condition throws other than as tested
    @Test
    @DisplayName("await() takes the lock again before it returns")
    default void awaitReacquiresTheLockBeforeReturning() throws Exception {
        if (!supportsConditions()) {
            return;
        }
        L lock = newLock();
        Condition condition = bounded(lock::newCondition);
        AtomicBoolean signalled = new AtomicBoolean();
        AtomicBoolean signallerUnlocking = new AtomicBoolean();
        CountDownLatch signallerDone = new CountDownLatch(1);
        CountDownLatch waiting = new CountDownLatch(1);
        ContentionCoordinator<Object> coordinator = coordinator();
        coordinator.add(1, 1, unchecked(() -> awaitSignal(lock, condition, signalled, waiting, () -> { }, () -> {
            boolean afterUnlock = signallerUnlocking.get();
            assertTrue(afterUnlock, "await() returned while the signalling thread still held the lock");
            assertTrue(signallerDone.await(SPIN_LIMIT_MILLIS, TimeUnit.MILLISECONDS), "the signalling thread had "
                    + "not unlocked within " + SPIN_LIMIT_MILLIS + " ms after await() returned");
            assertFalse(tryLockFromAnotherThread(lock), "after await() returned, another thread's tryLock() took "
                    + "the lock: await() did not take it again");
        })));
        Consumer<Object> signaller = signaller(lock, condition, signalled, waiting, 1,
                () -> signallerUnlocking.set(true), false);
        coordinator.add(1, 1, o -> {
            try {
                signaller.accept(o);
            } finally {
                signallerDone.countDown();
            }
        });
        coordinator.execute(new Object());
    }

    /// Tests that `signal()` wakes a thread waiting on the condition.
    ///
    /// @throws Exception if a call on the lock or its condition throws other than as tested
    @Test
    @DisplayName("signal() wakes a waiting thread")
    default void signalWakesAWaitingThread() throws Exception {
        assertSignalWakes(1, false);
    }

    /// Tests that `signalAll()` wakes every thread waiting on the condition: [#CONDITION_WAITERS] of them.
    ///
    /// @throws Exception if a call on the lock or its condition throws other than as tested
    @Test
    @DisplayName("signalAll() wakes every waiting thread")
    default void signalAllWakesEveryWaitingThread() throws Exception {
        assertSignalWakes(CONDITION_WAITERS, true);
    }

    /// Asserts that one `signal()`, or one `signalAll()`, wakes `waiters` threads waiting on a condition, each of
    /// which then holds the lock. Passes at once where [#supportsConditions()] is `false`.
    ///
    /// The waiters take the lock in turn: each starts its `lock()` only once the one before has taken it, so that no
    /// two contend for a free lock, and a lock that is not exclusive under contention, which
    /// [ThreadSafeLockingContract] tests, does not fail this test.
    ///
    /// @param waiters the number of waiting threads
    /// @param all     whether to call `signalAll()` rather than `signal()`
    private void assertSignalWakes(final int waiters, final boolean all) throws Exception {
        if (!supportsConditions()) {
            return;
        }
        L lock = newLock();
        Condition condition = bounded(lock::newCondition);
        AtomicBoolean signalled = new AtomicBoolean();
        CountDownLatch waiting = new CountDownLatch(waiters);
        AtomicInteger woken = new AtomicInteger();
        AtomicInteger arrivals = new AtomicInteger();
        AtomicInteger turn = new AtomicInteger();
        ContentionCoordinator<Object> coordinator = coordinator();
        coordinator.add(waiters, 1, unchecked(() -> {
            int mine = arrivals.getAndIncrement();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(BLOCKING_TIMEOUT_SECONDS);
            while (turn.get() != mine && System.nanoTime() - deadline < 0) {
                Thread.onSpinWait();
            }
            awaitSignal(lock, condition, signalled, waiting, turn::incrementAndGet, woken::incrementAndGet);
        }));
        coordinator.add(1, 1, signaller(lock, condition, signalled, waiting, waiters, () -> { }, all));
        coordinator.execute(new Object());
        assertEquals(waiters, woken.get(), (all ? "signalAll()" : "signal()") + ": the waiting threads it woke");
    }

    /// A waiter: takes the lock, counts down `waiting` and runs `taken`, awaits the condition until `signalled`, runs
    /// `after` while it holds the lock, and unlocks.
    private void awaitSignal(final Lock lock, final Condition condition, final AtomicBoolean signalled,
                             final CountDownLatch waiting, final Runnable taken, final AfterAwait after)
            throws Exception {
        lock.lock();
        try {
            waiting.countDown();
            taken.run();
            while (!signalled.get()) {
                condition.await();
            }
            after.run();
        } finally {
            lock.unlock();
        }
    }

    /// The signaller: waits for `waiters` waiters to have taken the lock, takes it, sets `signalled`, signals (all, or
    /// one), runs `beforeUnlock`, and unlocks.
    private Consumer<Object> signaller(final Lock lock, final Condition condition,
                                        final AtomicBoolean signalled, final CountDownLatch waiting,
                                        final int waiters, final Runnable beforeUnlock, final boolean all) {
        return unchecked(() -> {
            assertTrue(waiting.await(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS), () -> waiting.getCount() + " of "
                    + waiters + " waiters did not take the lock within " + BLOCKING_TIMEOUT_SECONDS + " s");
            lock.lock();
            try {
                signalled.set(true);
                if (all) {
                    condition.signalAll();
                } else {
                    condition.signal();
                }
                beforeUnlock.run();
            } finally {
                lock.unlock();
            }
        });
    }
}
