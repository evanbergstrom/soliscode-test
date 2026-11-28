/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.io.Serial;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

import org.jspecify.annotations.Nullable;

/// A lock that can be broken in well-defined ways, to show that [com.soliscode.test.contract.lock.LockContract] and
/// [com.soliscode.test.contract.lock.ThreadSafeLockContract] can fail.
///
/// Like [BreakableFuture], it wraps no JDK implementation: it is a lock of its own, an owner thread guarded by an
/// internal lock and a condition its waiters wait on, so that each break acts on that state and nothing else, and so
/// that its thread-unsafe break can step outside the guard. Unbroken, it keeps [Lock]'s contract:
/// - it is exclusive, and not reentrant: a thread that takes it again while it holds it waits forever, as for the
///   write lock of a `StampedLock`;
/// - `lock()` waits uninterruptibly; `lockInterruptibly()` and the timed `tryLock` throw [InterruptedException] for
///   an interrupt on entry or while waiting, with the interrupt status cleared;
/// - `unlock()` by a thread that does not hold it throws [IllegalMonitorStateException];
/// - `newCondition()` returns a new condition bound to it, whose `await` methods release it, wait until signalled
///   (or interrupted, or timed out), and take it again before they return; `await` and `signal` by a thread that
///   does not hold it throw [IllegalMonitorStateException].
///
/// It is made with its constructor, and has no builder.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// ## Thread Safety
/// Thread safe, unless [#LOCK_IS_NOT_THREAD_SAFE] is set: its state is guarded by its internal lock. Its breaks are
/// a copy of the set it was made with, which `addBreak` changes, unsynchronized.
///
/// ## Serialization
/// It is serializable, as every breakable is. Its owner is not serialized, so a deserialized lock is unlocked, as a
/// deserialized `ReentrantLock` is.
///
/// @since 0.1.7
/// @see Lock
public class BreakableLock extends AbstractBreakable implements Lock {

    /// **Purpose:** `lock` does not take the lock.
    ///
    /// **Effect:** `lock()` returns at once, without acquiring the lock.
    ///
    /// **Affected Methods:** [#lock()]
    public static final Break LOCK_DOES_NOT_ACQUIRE =
            new Break("lock does not acquire");

    /// **Purpose:** `lock` does not wait for the lock, where its Javadoc says the thread "lies dormant until the lock
    /// has been acquired".
    ///
    /// **Effect:** `lock()` returns at once, without acquiring the lock, while another thread holds it.
    ///
    /// **Affected Methods:** [#lock()]
    public static final Break LOCK_DOES_NOT_WAIT =
            new Break("lock does not wait");

    /// **Purpose:** `lockInterruptibly` does not take the lock.
    ///
    /// **Effect:** `lockInterruptibly()` returns at once, without acquiring the lock.
    ///
    /// **Affected Methods:** [#lockInterruptibly()]
    public static final Break LOCK_INTERRUPTIBLY_DOES_NOT_ACQUIRE =
            new Break("lock interruptibly does not acquire");

    /// **Purpose:** `lockInterruptibly` does not wait for the lock.
    ///
    /// **Effect:** `lockInterruptibly()` returns at once, without acquiring the lock, while another thread holds it.
    ///
    /// **Affected Methods:** [#lockInterruptibly()]
    public static final Break LOCK_INTERRUPTIBLY_DOES_NOT_WAIT =
            new Break("lock interruptibly does not wait");

    /// **Purpose:** `lockInterruptibly` ignores an interrupt on entry, where its Javadoc says it throws
    /// [InterruptedException].
    ///
    /// **Effect:** `lockInterruptibly()` with the interrupt status set acquires the lock as if it were not set, and
    /// leaves the status set.
    ///
    /// **Affected Methods:** [#lockInterruptibly()]
    public static final Break LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT =
            new Break("lock interruptibly ignores interrupt");

    /// **Purpose:** `lockInterruptibly` does not clear the interrupt status when it throws, where its Javadoc says
    /// "the current thread's interrupted status is cleared".
    ///
    /// **Effect:** `lockInterruptibly()` throws [InterruptedException] with the interrupt status set.
    ///
    /// **Affected Methods:** [#lockInterruptibly()]
    public static final Break LOCK_INTERRUPTIBLY_KEEPS_THE_INTERRUPT_STATUS =
            new Break("lock interruptibly keeps the interrupt status");

    /// **Purpose:** `lockInterruptibly` is not interrupted while it waits, though it says it supports it.
    ///
    /// **Effect:** `lockInterruptibly()`, interrupted while it waits, goes on waiting, acquires the lock once it is
    /// released, and returns with the interrupt status set.
    ///
    /// **Affected Methods:** [#lockInterruptibly()]
    public static final Break LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT_WHILE_WAITING =
            new Break("lock interruptibly ignores interrupt while waiting");

    /// **Purpose:** `lockInterruptibly` returns without the lock when interrupted while it waits, where its Javadoc
    /// says it either throws [InterruptedException] or acquires the lock.
    ///
    /// **Effect:** `lockInterruptibly()`, interrupted while it waits, returns normally without acquiring the lock.
    ///
    /// **Affected Methods:** [#lockInterruptibly()]
    public static final Break LOCK_INTERRUPTIBLY_RETURNS_WITHOUT_THE_LOCK_ON_INTERRUPT =
            new Break("lock interruptibly returns without the lock on interrupt");

    /// **Purpose:** `lockInterruptibly` takes the lock when interrupted while it waits, and throws anyway.
    ///
    /// **Effect:** `lockInterruptibly()`, interrupted while it waits, goes on waiting, uninterruptibly, until the lock
    /// is released, takes it, and then throws [InterruptedException], leaving it held.
    ///
    /// **Affected Methods:** [#lockInterruptibly()]
    public static final Break LOCK_INTERRUPTIBLY_TAKES_THE_LOCK_ON_INTERRUPT =
            new Break("lock interruptibly takes the lock on interrupt");

    /// **Purpose:** `lockInterruptibly` throws [InterruptedException] when the lock is held, though no thread
    /// interrupted it.
    ///
    /// **Effect:** `lockInterruptibly()` while another thread holds the lock throws [InterruptedException] at once.
    ///
    /// **Affected Methods:** [#lockInterruptibly()]
    public static final Break LOCK_INTERRUPTIBLY_THROWS_WHILE_HELD =
            new Break("lock interruptibly throws while held");

    /// **Purpose:** `lockInterruptibly` answers an interrupt while it waits only once a release wakes it.
    ///
    /// **Effect:** `lockInterruptibly()`, interrupted while it waits, goes on waiting until the lock is released, and
    /// then throws [InterruptedException] without acquiring it.
    ///
    /// **Affected Methods:** [#lockInterruptibly()]
    public static final Break LOCK_INTERRUPTIBLY_DEFERS_INTERRUPT_UNTIL_RELEASE =
            new Break("lock interruptibly defers interrupt until release");

    /// **Purpose:** `tryLock` fails on a free lock, where its Javadoc says it "acquires the lock if it is available".
    ///
    /// **Effect:** `tryLock()` returns `false` without acquiring the lock when no thread holds it.
    ///
    /// **Affected Methods:** [#tryLock()]
    public static final Break TRY_LOCK_RETURNS_FALSE_WHEN_FREE =
            new Break("try lock returns false when free");

    /// **Purpose:** `tryLock` takes a lock another thread holds.
    ///
    /// **Effect:** `tryLock()` while another thread holds the lock makes this thread its owner, and returns `true`.
    ///
    /// **Affected Methods:** [#tryLock()]
    public static final Break TRY_LOCK_ACQUIRES_A_HELD_LOCK =
            new Break("try lock acquires a held lock");

    /// **Purpose:** `tryLock` waits, where its Javadoc says it "will return immediately with the value false".
    ///
    /// **Effect:** `tryLock()` while another thread holds the lock waits, uninterruptibly, until it is released, then
    /// acquires it and returns `true`.
    ///
    /// **Affected Methods:** [#tryLock()]
    public static final Break TRY_LOCK_WAITS_FOR_THE_HOLDER =
            new Break("try lock waits for the holder");

    /// **Purpose:** the timed `tryLock` fails on a free lock when given no time, where its Javadoc says "If the lock
    /// is available this method returns immediately with the value true".
    ///
    /// **Effect:** `tryLock(time, unit)` with a time of zero or less returns `false` without acquiring the lock.
    ///
    /// **Affected Methods:** [#tryLock(long, TimeUnit)]
    public static final Break TIMED_TRY_LOCK_FAILS_WITH_ZERO_TIMEOUT =
            new Break("timed try lock fails with zero timeout");

    /// **Purpose:** the timed `tryLock` does not wait its time.
    ///
    /// **Effect:** `tryLock(time, unit)` while another thread holds the lock returns `false` at once.
    ///
    /// **Affected Methods:** [#tryLock(long, TimeUnit)]
    public static final Break TIMED_TRY_LOCK_DOES_NOT_WAIT =
            new Break("timed try lock does not wait");

    /// **Purpose:** the timed `tryLock` misses a release within its time.
    ///
    /// **Effect:** `tryLock(time, unit)` while another thread holds the lock waits out its whole time and returns
    /// `false`, even if the lock is released meanwhile.
    ///
    /// **Affected Methods:** [#tryLock(long, TimeUnit)]
    public static final Break TIMED_TRY_LOCK_MISSES_A_RELEASE =
            new Break("timed try lock misses a release");

    /// **Purpose:** the timed `tryLock` ignores an interrupt on entry.
    ///
    /// **Effect:** `tryLock(time, unit)` with the interrupt status set acquires a free lock and returns `true`,
    /// leaving the status set.
    ///
    /// **Affected Methods:** [#tryLock(long, TimeUnit)]
    public static final Break TIMED_TRY_LOCK_IGNORES_INTERRUPT =
            new Break("timed try lock ignores interrupt");

    /// **Purpose:** the timed `tryLock` does not clear the interrupt status when it throws.
    ///
    /// **Effect:** `tryLock(time, unit)` throws [InterruptedException] with the interrupt status set.
    ///
    /// **Affected Methods:** [#tryLock(long, TimeUnit)]
    public static final Break TIMED_TRY_LOCK_KEEPS_THE_INTERRUPT_STATUS =
            new Break("timed try lock keeps the interrupt status");

    /// **Purpose:** the timed `tryLock` is not interrupted while it waits, though it says it supports it.
    ///
    /// **Effect:** `tryLock(time, unit)`, interrupted while it waits, goes on waiting until the lock is released or
    /// its time elapses, and returns with the interrupt status set.
    ///
    /// **Affected Methods:** [#tryLock(long, TimeUnit)]
    public static final Break TIMED_TRY_LOCK_IGNORES_INTERRUPT_WHILE_WAITING =
            new Break("timed try lock ignores interrupt while waiting");

    /// **Purpose:** the timed `tryLock` gives up when interrupted while it waits, where its Javadoc says it throws
    /// [InterruptedException], and returns `false` only once its time has elapsed.
    ///
    /// **Effect:** `tryLock(time, unit)`, interrupted while it waits, returns `false` at once.
    ///
    /// **Affected Methods:** [#tryLock(long, TimeUnit)]
    public static final Break TIMED_TRY_LOCK_RETURNS_FALSE_ON_INTERRUPT =
            new Break("timed try lock returns false on interrupt");

    /// **Purpose:** the timed `tryLock` takes the lock when interrupted while it waits, and throws anyway.
    ///
    /// **Effect:** `tryLock(time, unit)`, interrupted while it waits, goes on waiting, uninterruptibly, until the lock
    /// is released or its time elapses, takes it if it can, and then throws [InterruptedException], leaving it held.
    ///
    /// **Affected Methods:** [#tryLock(long, TimeUnit)]
    public static final Break TIMED_TRY_LOCK_TAKES_THE_LOCK_ON_INTERRUPT =
            new Break("timed try lock takes the lock on interrupt");

    /// **Purpose:** the timed `tryLock` throws [InterruptedException] when the lock is held, though no thread
    /// interrupted it.
    ///
    /// **Effect:** `tryLock(time, unit)` with a time above zero, while another thread holds the lock, throws
    /// [InterruptedException] at once.
    ///
    /// **Affected Methods:** [#tryLock(long, TimeUnit)]
    public static final Break TIMED_TRY_LOCK_THROWS_WHILE_HELD =
            new Break("timed try lock throws while held");

    /// **Purpose:** the timed `tryLock` answers an interrupt while it waits only once a release wakes it.
    ///
    /// **Effect:** `tryLock(time, unit)`, interrupted while it waits, goes on waiting until the lock is released or
    /// its time elapses, and then throws [InterruptedException] without acquiring it.
    ///
    /// **Affected Methods:** [#tryLock(long, TimeUnit)]
    public static final Break TIMED_TRY_LOCK_DEFERS_INTERRUPT_UNTIL_RELEASE =
            new Break("timed try lock defers interrupt until release");

    /// **Purpose:** `unlock` does not release the lock.
    ///
    /// **Effect:** `unlock()` by the owner leaves it the owner.
    ///
    /// **Affected Methods:** [#unlock()]
    public static final Break UNLOCK_DOES_NOT_RELEASE =
            new Break("unlock does not release");

    /// **Purpose:** `unlock` does not wake a thread that waits for the lock.
    ///
    /// **Effect:** `unlock()` releases the lock without waking the threads waiting for it; a thread already waiting
    /// in `lock()` waits until a later unlock wakes it.
    ///
    /// **Affected Methods:** [#unlock()]
    public static final Break UNLOCK_DOES_NOT_WAKE_A_WAITER =
            new Break("unlock does not wake a waiter");

    /// **Purpose:** `newCondition` gives the same condition each time, where its Javadoc says it "Returns a new
    /// Condition instance".
    ///
    /// **Effect:** every call of `newCondition()` returns the condition the first one made.
    ///
    /// **Affected Methods:** [#newCondition()]
    public static final Break NEW_CONDITION_RETURNS_THE_SAME_CONDITION =
            new Break("new condition returns the same condition");

    /// **Purpose:** `newCondition` refuses, though the lock supports conditions.
    ///
    /// **Effect:** `newCondition()` throws [UnsupportedOperationException].
    ///
    /// **Affected Methods:** [#newCondition()]
    public static final Break NEW_CONDITION_IS_UNSUPPORTED =
            new Break("new condition is unsupported");

    /// **Purpose:** a condition's `await` does not release the lock, where `Condition`'s Javadoc says "The lock
    /// associated with this Condition is atomically released".
    ///
    /// **Effect:** the `await` methods of its conditions wait with the lock still held.
    ///
    /// **Affected Methods:** the `await` methods of [#newCondition()]'s conditions
    public static final Break AWAIT_DOES_NOT_RELEASE_THE_LOCK =
            new Break("await does not release the lock");

    /// **Purpose:** a condition's `await` returns without the lock, where `Condition`'s Javadoc says "Before this
    /// method can return the current thread must re-acquire the lock".
    ///
    /// **Effect:** the `await` methods of its conditions return, once signalled, without taking the lock again.
    ///
    /// **Affected Methods:** the `await` methods of [#newCondition()]'s conditions
    public static final Break AWAIT_DOES_NOT_REACQUIRE_THE_LOCK =
            new Break("await does not reacquire the lock");

    /// **Purpose:** a condition's `signal` does not wake a waiting thread.
    ///
    /// **Effect:** `signal()` of its conditions does nothing.
    ///
    /// **Affected Methods:** `signal()` of [#newCondition()]'s conditions
    public static final Break SIGNAL_DOES_NOTHING =
            new Break("signal does nothing");

    /// **Purpose:** a condition's `signalAll` wakes only one waiting thread, where its Javadoc says it "Wakes up all
    /// waiting threads".
    ///
    /// **Effect:** `signalAll()` of its conditions wakes the longest-waiting thread only, as `signal()` does.
    ///
    /// **Affected Methods:** `signalAll()` of [#newCondition()]'s conditions
    public static final Break SIGNAL_ALL_WAKES_ONE =
            new Break("signal all wakes one");

    /// **Purpose:** the lock is not exclusive under contention: its acquisition is a check and then an act, not one
    /// step.
    ///
    /// **Effect:** each acquiring method sees the lock free under the guard, steps outside the guard and yields, then
    /// takes it without checking again, so that two threads can hold it at once; `unlock()` then does not check the
    /// caller is the owner, since a second thread may have taken it over. Correct on one thread.
    ///
    /// **Affected Methods:** [#lock()], [#lockInterruptibly()], [#tryLock()], [#tryLock(long, TimeUnit)], [#unlock()]
    public static final Break LOCK_IS_NOT_THREAD_SAFE =
            new Break("lock is not thread safe");

    @Serial
    private static final long serialVersionUID = 1L;

    /// For [#acquire(long, boolean)]: wait with no time limit.
    private static final long FOREVER = -1;

    /// The longest wait ({@value} ns, about 73 years) the timed methods keep as given; a longer one is taken as this
    /// long, so that a deadline made by adding it to [System#nanoTime()] keeps its difference from it.
    private static final long LONGEST_WAIT_NANOS = Long.MAX_VALUE / 4;

    /// Guards the owner, and the conditions' waiters.
    private final ReentrantLock guard = new ReentrantLock();

    /// Signalled when the lock is released.
    private final Condition released = guard.newCondition();

    /// The thread that holds the lock, or `null` if none does.
    private transient @Nullable Thread owner;

    /// The condition [#NEW_CONDITION_RETURNS_THE_SAME_CONDITION] returns, once made.
    private transient @Nullable Condition sharedCondition;

    /// Creates an unlocked lock with the given breaks.
    ///
    /// @param breaks the breaks to apply, copied; empty for an unbroken lock
    /// @throws NullPointerException if `breaks` is null
    public BreakableLock(final Set<Break> breaks) {
        super(breaks, Map.of(), false);
    }

    /// Refuses to be cloned, even by a subclass that implements [Cloneable]: a copy would share this lock's guard
    /// and state (and, for its views, this lock itself), so that locking the copy locked the original.
    ///
    /// @return never
    /// @throws CloneNotSupportedException always
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("a BreakableLock is not cloneable: a copy would share its state");
    }

    /// Acquires the lock, waiting uninterruptibly while another thread holds it.
    ///
    /// Breaks: [#LOCK_DOES_NOT_ACQUIRE], [#LOCK_DOES_NOT_WAIT], [#LOCK_IS_NOT_THREAD_SAFE].
    @Override
    public void lock() {
        if (hasBreak(LOCK_DOES_NOT_ACQUIRE) || (hasBreak(LOCK_DOES_NOT_WAIT) && isHeld())) {
            return;
        }
        acquireUninterruptibly(FOREVER);
    }

    /// Acquires the lock unless the thread is interrupted, on entry or while it waits.
    ///
    /// Breaks: [#LOCK_INTERRUPTIBLY_DOES_NOT_ACQUIRE], [#LOCK_INTERRUPTIBLY_DOES_NOT_WAIT],
    /// [#LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT], [#LOCK_INTERRUPTIBLY_KEEPS_THE_INTERRUPT_STATUS],
    /// [#LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT_WHILE_WAITING],
    /// [#LOCK_INTERRUPTIBLY_RETURNS_WITHOUT_THE_LOCK_ON_INTERRUPT],
    /// [#LOCK_INTERRUPTIBLY_DEFERS_INTERRUPT_UNTIL_RELEASE], [#LOCK_INTERRUPTIBLY_THROWS_WHILE_HELD],
    /// [#LOCK_INTERRUPTIBLY_TAKES_THE_LOCK_ON_INTERRUPT], [#LOCK_IS_NOT_THREAD_SAFE].
    ///
    /// @throws InterruptedException if the thread is interrupted on entry or while it waits
    @Override
    public void lockInterruptibly() throws InterruptedException {
        if (checkInterruptOnEntry(LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT,
                LOCK_INTERRUPTIBLY_KEEPS_THE_INTERRUPT_STATUS)) {
            acquire(FOREVER, true);
            Thread.currentThread().interrupt();
            return;
        }
        if (hasBreak(LOCK_INTERRUPTIBLY_DOES_NOT_ACQUIRE)
                || (hasBreak(LOCK_INTERRUPTIBLY_DOES_NOT_WAIT) && isHeld())) {
            return;
        }
        if (hasBreak(LOCK_INTERRUPTIBLY_THROWS_WHILE_HELD) && isHeld()) {
            throw new InterruptedException();
        }
        try {
            acquire(FOREVER, true);
        } catch (final InterruptedException e) {
            afterInterrupt(e);
        }
    }

    /// What `lockInterruptibly` does once interrupted while it waits: throws `e`, with the status cleared, unless a
    /// break says otherwise.
    private void afterInterrupt(final InterruptedException e) throws InterruptedException {
        if (hasBreak(LOCK_INTERRUPTIBLY_RETURNS_WITHOUT_THE_LOCK_ON_INTERRUPT)) {
            return;
        }
        if (hasBreak(LOCK_INTERRUPTIBLY_IGNORES_INTERRUPT_WHILE_WAITING)) {
            acquireUninterruptibly(FOREVER);
            Thread.currentThread().interrupt();
            return;
        }
        if (hasBreak(LOCK_INTERRUPTIBLY_DEFERS_INTERRUPT_UNTIL_RELEASE)) {
            awaitRelease(FOREVER);
        }
        if (hasBreak(LOCK_INTERRUPTIBLY_TAKES_THE_LOCK_ON_INTERRUPT)) {
            acquireUninterruptibly(FOREVER);
        }
        if (hasBreak(LOCK_INTERRUPTIBLY_KEEPS_THE_INTERRUPT_STATUS)) {
            Thread.currentThread().interrupt();
        }
        throw e;
    }

    /// Acquires the lock if no thread holds it.
    ///
    /// Breaks: [#TRY_LOCK_RETURNS_FALSE_WHEN_FREE], [#TRY_LOCK_ACQUIRES_A_HELD_LOCK],
    /// [#TRY_LOCK_WAITS_FOR_THE_HOLDER], [#LOCK_IS_NOT_THREAD_SAFE].
    ///
    /// @return `true` if it acquired the lock
    @Override
    public boolean tryLock() {
        boolean held = (hasBreak(TRY_LOCK_RETURNS_FALSE_WHEN_FREE) || hasBreak(TRY_LOCK_ACQUIRES_A_HELD_LOCK))
                && isHeld();
        if (hasBreak(TRY_LOCK_RETURNS_FALSE_WHEN_FREE) && !held) {
            return false;
        }
        if (hasBreak(TRY_LOCK_ACQUIRES_A_HELD_LOCK) && held) {
            setOwner(Thread.currentThread());
            return true;
        }
        if (hasBreak(TRY_LOCK_WAITS_FOR_THE_HOLDER)) {
            return acquireUninterruptibly(FOREVER);
        }
        return acquireUninterruptibly(0);
    }

    /// Acquires the lock if it is free within the given time, unless the thread is interrupted, on entry or while it
    /// waits.
    ///
    /// Breaks: [#TIMED_TRY_LOCK_FAILS_WITH_ZERO_TIMEOUT], [#TIMED_TRY_LOCK_DOES_NOT_WAIT],
    /// [#TIMED_TRY_LOCK_MISSES_A_RELEASE], [#TIMED_TRY_LOCK_IGNORES_INTERRUPT],
    /// [#TIMED_TRY_LOCK_KEEPS_THE_INTERRUPT_STATUS], [#TIMED_TRY_LOCK_IGNORES_INTERRUPT_WHILE_WAITING],
    /// [#TIMED_TRY_LOCK_RETURNS_FALSE_ON_INTERRUPT], [#TIMED_TRY_LOCK_DEFERS_INTERRUPT_UNTIL_RELEASE],
    /// [#TIMED_TRY_LOCK_THROWS_WHILE_HELD], [#TIMED_TRY_LOCK_TAKES_THE_LOCK_ON_INTERRUPT],
    /// [#LOCK_IS_NOT_THREAD_SAFE].
    ///
    /// @param time the longest time to wait
    /// @param unit the unit of `time`
    /// @return `true` if it acquired the lock, `false` if the time elapsed first
    /// @throws InterruptedException if the thread is interrupted on entry or while it waits
    /// @throws NullPointerException if `unit` is null
    @Override
    public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
        long nanos = boundedWait(unit.toNanos(time));
        long deadline = System.nanoTime() + nanos;
        if (checkInterruptOnEntry(TIMED_TRY_LOCK_IGNORES_INTERRUPT, TIMED_TRY_LOCK_KEEPS_THE_INTERRUPT_STATUS)) {
            boolean acquired = acquireUninterruptibly(nanos);
            Thread.currentThread().interrupt();
            return acquired;
        }
        Boolean broken = timedTryLockBreak(nanos);
        if (broken != null) {
            return broken;
        }
        try {
            return acquire(nanos, true);
        } catch (final InterruptedException e) {
            return afterTimedInterrupt(e, deadline);
        }
    }

    /// The result of the timed `tryLock`'s breaks that act before it waits, or `null` to go on.
    private @Nullable Boolean timedTryLockBreak(final long nanos) throws InterruptedException {
        if (hasBreak(TIMED_TRY_LOCK_FAILS_WITH_ZERO_TIMEOUT) && nanos == 0) {
            return false;
        }
        boolean held = isHeld();
        if (hasBreak(TIMED_TRY_LOCK_THROWS_WHILE_HELD) && nanos > 0 && held) {
            throw new InterruptedException();
        }
        if (hasBreak(TIMED_TRY_LOCK_MISSES_A_RELEASE) && held) {
            waitOut(nanos);
            return false;
        }
        if (hasBreak(TIMED_TRY_LOCK_DOES_NOT_WAIT) && held) {
            return false;
        }
        return null;
    }

    /// What the timed `tryLock` does once interrupted while it waits: throws `e`, with the status cleared, unless a
    /// break says otherwise.
    private boolean afterTimedInterrupt(final InterruptedException e, final long deadline)
            throws InterruptedException {
        if (hasBreak(TIMED_TRY_LOCK_RETURNS_FALSE_ON_INTERRUPT)) {
            return false;
        }
        if (hasBreak(TIMED_TRY_LOCK_IGNORES_INTERRUPT_WHILE_WAITING)) {
            boolean acquired = acquireUninterruptibly(Math.max(0, deadline - System.nanoTime()));
            Thread.currentThread().interrupt();
            return acquired;
        }
        if (hasBreak(TIMED_TRY_LOCK_DEFERS_INTERRUPT_UNTIL_RELEASE)) {
            awaitRelease(Math.max(0, deadline - System.nanoTime()));
        }
        if (hasBreak(TIMED_TRY_LOCK_TAKES_THE_LOCK_ON_INTERRUPT)) {
            acquireUninterruptibly(Math.max(0, deadline - System.nanoTime()));
        }
        if (hasBreak(TIMED_TRY_LOCK_KEEPS_THE_INTERRUPT_STATUS)) {
            Thread.currentThread().interrupt();
        }
        throw e;
    }

    /// Releases the lock, and wakes the threads waiting for it.
    ///
    /// Breaks: [#UNLOCK_DOES_NOT_RELEASE], [#UNLOCK_DOES_NOT_WAKE_A_WAITER], [#LOCK_IS_NOT_THREAD_SAFE].
    ///
    /// @throws IllegalMonitorStateException if the current thread does not hold the lock
    @Override
    public void unlock() {
        guard.lock();
        try {
            if (owner != Thread.currentThread() && !hasBreak(LOCK_IS_NOT_THREAD_SAFE)) {
                throw new IllegalMonitorStateException("unlock() by a thread that does not hold the BreakableLock");
            }
            if (hasBreak(UNLOCK_DOES_NOT_RELEASE)) {
                return;
            }
            owner = null;
            if (!hasBreak(UNLOCK_DOES_NOT_WAKE_A_WAITER)) {
                released.signalAll();
            }
        } finally {
            guard.unlock();
        }
    }

    /// Returns a new condition bound to this lock.
    ///
    /// Breaks: [#NEW_CONDITION_RETURNS_THE_SAME_CONDITION], [#NEW_CONDITION_IS_UNSUPPORTED], and, on the condition,
    /// [#AWAIT_DOES_NOT_RELEASE_THE_LOCK], [#AWAIT_DOES_NOT_REACQUIRE_THE_LOCK], [#SIGNAL_DOES_NOTHING],
    /// [#SIGNAL_ALL_WAKES_ONE].
    ///
    /// @return the condition
    @Override
    public Condition newCondition() {
        if (hasBreak(NEW_CONDITION_IS_UNSUPPORTED)) {
            throw new UnsupportedOperationException("newCondition() is not supported by BreakableLock");
        }
        if (!hasBreak(NEW_CONDITION_RETURNS_THE_SAME_CONDITION)) {
            return new BreakableCondition();
        }
        guard.lock();
        try {
            Condition shared = sharedCondition;
            if (shared == null) {
                shared = new BreakableCondition();
                sharedCondition = shared;
            }
            return shared;
        } finally {
            guard.unlock();
        }
    }

    /// Throws [InterruptedException], clearing the status, if the thread is interrupted, keeping the status if `keeps`
    /// is set; or, if `ignores` is set, returns `true`, with the status cleared, so that the caller acquires as if it
    /// were not set and then sets it again.
    ///
    /// @return `true` if the interrupt was there and `ignores` is set
    private boolean checkInterruptOnEntry(final Break ignores, final Break keeps) throws InterruptedException {
        if (!Thread.interrupted()) {
            return false;
        }
        if (hasBreak(ignores)) {
            return true;
        }
        if (hasBreak(keeps)) {
            Thread.currentThread().interrupt();
        }
        throw new InterruptedException();
    }

    /// Returns whether a thread holds the lock.
    private boolean isHeld() {
        guard.lock();
        try {
            return owner != null;
        } finally {
            guard.unlock();
        }
    }

    /// Makes a thread the owner, whoever holds the lock.
    private void setOwner(final Thread thread) {
        guard.lock();
        try {
            owner = thread;
        } finally {
            guard.unlock();
        }
    }

    /// Acquires the lock, as [#acquire(long, boolean)] does, but not interruptibly: an interrupt while it waits is
    /// kept, and the status set again once it returns.
    private boolean acquireUninterruptibly(final long nanos) {
        try {
            return acquire(nanos, false);
        } catch (final InterruptedException e) {
            throw new IllegalStateException("an uninterruptible acquisition was interrupted", e);
        }
    }

    /// Acquires the lock, waiting while another thread holds it: with no limit for [#FOREVER], otherwise for at most
    /// `nanos`; interruptibly, or keeping an interrupt and setting the status again once it returns.
    ///
    /// @return `true` if it acquired the lock, `false` if the time elapsed first
    private boolean acquire(final long nanos, final boolean interruptible) throws InterruptedException {
        if (hasBreak(LOCK_IS_NOT_THREAD_SAFE)) {
            return acquireUnsafely(nanos, interruptible);
        }
        long deadline = System.nanoTime() + Math.max(0, nanos);
        boolean interrupted = false;
        if (interruptible) {
            guard.lockInterruptibly();
        } else {
            guard.lock();
        }
        try {
            while (owner != null) {
                long left = deadline - System.nanoTime();
                if (nanos != FOREVER && left <= 0) {
                    return false;
                }
                try {
                    if (nanos == FOREVER) {
                        released.await();
                    } else {
                        long ignored = released.awaitNanos(left);
                    }
                } catch (final InterruptedException e) {
                    if (interruptible) {
                        throw e;
                    }
                    interrupted = true;
                }
            }
            owner = Thread.currentThread();
            return true;
        } finally {
            guard.unlock();
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /// [#LOCK_IS_NOT_THREAD_SAFE]'s acquisition: waits, under the guard, until the lock is free, steps outside the
    /// guard and yields, then takes it without checking again.
    private boolean acquireUnsafely(final long nanos, final boolean interruptible) throws InterruptedException {
        long deadline = System.nanoTime() + Math.max(0, nanos);
        boolean interrupted = false;
        try {
            while (true) {
                long left = nanos == FOREVER ? FOREVER : Math.max(0, deadline - System.nanoTime());
                try {
                    if (awaitFree(left)) {
                        break;
                    }
                } catch (final InterruptedException e) {
                    if (interruptible) {
                        throw e;
                    }
                    interrupted = true;
                    continue;
                }
                if (nanos != FOREVER) {
                    return false;
                }
            }
            takeAfterYielding();
            return true;
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /// Yields, outside the guard, and then makes this thread the owner without checking again: the step of
    /// [#LOCK_IS_NOT_THREAD_SAFE] that lets another thread take the lock between.
    // ThreadPriorityCheck: the yield widens the window of the fault LOCK_IS_NOT_THREAD_SAFE simulates.
    @SuppressWarnings("ThreadPriorityCheck")
    private void takeAfterYielding() {
        Thread.yield();
        setOwner(Thread.currentThread());
    }

    /// Waits under the guard until no thread holds the lock, with no limit for [#FOREVER], otherwise for at most
    /// `nanos`, and returns whether it is free; the guard is released on return.
    private boolean awaitFree(final long nanos) throws InterruptedException {
        long deadline = System.nanoTime() + Math.max(0, nanos);
        guard.lockInterruptibly();
        try {
            while (owner != null) {
                long left = deadline - System.nanoTime();
                if (nanos != FOREVER && left <= 0) {
                    return false;
                }
                if (nanos == FOREVER) {
                    released.await();
                } else {
                    long ignored = released.awaitNanos(left);
                }
            }
            return true;
        } finally {
            guard.unlock();
        }
    }

    /// Waits, uninterruptibly, until no thread holds the lock, with no limit for [#FOREVER], otherwise for at most
    /// `nanos`: the wait of the breaks that defer an interrupt until a release.
    private void awaitRelease(final long nanos) {
        long deadline = System.nanoTime() + Math.max(0, nanos);
        guard.lock();
        try {
            while (owner != null) {
                long left = deadline - System.nanoTime();
                if (nanos != FOREVER && left <= 0) {
                    return;
                }
                if (nanos == FOREVER) {
                    released.awaitUninterruptibly();
                } else {
                    try {
                        long ignored = released.awaitNanos(left);
                    } catch (final InterruptedException e) {
                        // Deferred: the caller throws once the lock is released.
                    }
                }
            }
        } finally {
            guard.unlock();
        }
    }

    /// Waits for `nanos`, interruptibly, on a condition no one signals: [#TIMED_TRY_LOCK_MISSES_A_RELEASE]'s wait.
    private void waitOut(final long nanos) throws InterruptedException {
        Condition never = guard.newCondition();
        long deadline = System.nanoTime() + nanos;
        guard.lock();
        try {
            for (long left = nanos; left > 0; left = deadline - System.nanoTime()) {
                long ignored = never.awaitNanos(left);
            }
        } finally {
            guard.unlock();
        }
    }

    /// The interrupt a condition's wait met: the exception that ended an interruptible wait, or whether an
    /// uninterruptible one was interrupted.
    ///
    /// @param thrown      the exception to throw once the lock is taken again, or `null`
    /// @param interrupted whether to set the interrupt status again
    private record WaitEnd(@Nullable InterruptedException thrown, boolean interrupted) {
        /// Throws the exception, or sets the interrupt status again, as the wait met.
        void finish() throws InterruptedException {
            if (thrown != null) {
                throw thrown;
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /// Returns the time left of a relative wait of `nanos`, from now, read again each call: zero or less once it has
    /// elapsed.
    private static LongSupplier timeLeftOf(final long nanos) {
        long deadline = System.nanoTime() + boundedWait(nanos);
        return () -> deadline - System.nanoTime();
    }

    /// Returns a wait of `nanos`, no less than zero and no longer than [#LONGEST_WAIT_NANOS].
    private static long boundedWait(final long nanos) {
        return Math.min(Math.max(0, nanos), LONGEST_WAIT_NANOS);
    }

    /// Returns the time left, in nanoseconds, until the wall-clock time `until` (milliseconds since the epoch): zero
    /// once it has passed, without overflow for a time long past.
    private static long nanosUntil(final long until) {
        long now = System.currentTimeMillis();
        return until <= now ? 0 : TimeUnit.MILLISECONDS.toNanos(until - now);
    }

    /// What a condition's wait came to: whether it was signalled, and the time left once it had the lock again.
    private record Awaited(boolean signalled, long left) {
    }

    /// A thread waiting on a condition, and whether it has been signalled.
    private static final class Waiter {
        private boolean signalled;
    }

    /// A condition bound to this lock: its waiters are queued under the guard, and a signal marks them.
    private final class BreakableCondition implements Condition {
        private final Condition wakeup = guard.newCondition();
        private final ArrayDeque<Waiter> waiters = new ArrayDeque<>();

        @Override
        public void await() throws InterruptedException {
            Awaited ignored = blockUntilSignalled(null, true);
        }

        @Override
        public void awaitUninterruptibly() {
            try {
                Awaited ignored = blockUntilSignalled(null, false);
            } catch (final InterruptedException e) {
                throw new IllegalStateException("an uninterruptible await was interrupted", e);
            }
        }

        @Override
        public long awaitNanos(final long nanosTimeout) throws InterruptedException {
            return blockUntilSignalled(timeLeftOf(nanosTimeout), true).left();
        }

        @Override
        public boolean await(final long time, final TimeUnit unit) throws InterruptedException {
            return blockUntilSignalled(timeLeftOf(unit.toNanos(time)), true).left() > 0;
        }

        // JavaUtilDate: Condition.awaitUntil's signature takes a Date.
        @SuppressWarnings("JavaUtilDate")
        @Override
        public boolean awaitUntil(final Date deadline) throws InterruptedException {
            long until = deadline.getTime();
            return blockUntilSignalled(() -> nanosUntil(until), true).left() > 0;
        }

        @Override
        public void signal() {
            guard.lock();
            try {
                checkOwner("signal()");
                if (hasBreak(SIGNAL_DOES_NOTHING)) {
                    return;
                }
                signalOne();
            } finally {
                guard.unlock();
            }
        }

        @Override
        public void signalAll() {
            guard.lock();
            try {
                checkOwner("signalAll()");
                if (hasBreak(SIGNAL_ALL_WAKES_ONE)) {
                    signalOne();
                    return;
                }
                for (final Waiter w : waiters) {
                    w.signalled = true;
                }
                wakeup.signalAll();
            } finally {
                guard.unlock();
            }
        }

        /// Marks the longest-waiting thread not yet signalled, and wakes the waiters to look. Under the guard.
        private void signalOne() {
            for (final Waiter w : waiters) {
                if (!w.signalled) {
                    w.signalled = true;
                    wakeup.signalAll();
                    return;
                }
            }
        }

        /// Throws [IllegalMonitorStateException] if the current thread does not hold the lock. Under the guard.
        private void checkOwner(final String method) {
            if (owner != Thread.currentThread()) {
                throw new IllegalMonitorStateException(method + " by a thread that does not hold the BreakableLock");
            }
        }

        /// Releases the lock, waits until signalled, interrupted (if `interruptible`) or, unless `timeLeft` is `null`
        /// (no limit), no time is left, then takes the lock again, and returns whether it was signalled, and the time
        /// left once it has the lock again, which may be zero or less even if it was signalled, as `Condition`'s
        /// `awaitNanos` allows. The boolean timed waits return whether time is left then, as `Condition`'s Javadoc
        /// words it ("false if the waiting time detectably elapsed before return"), not whether the wait was
        /// signalled, as AQS's do. An interrupt throws [InterruptedException] once the lock is taken again, unless
        /// the waiter had already been signalled: then it returns normally, with the interrupt status set, so that the
        /// signal is not lost, as `Condition`'s Javadoc requires of an implementation that favours either.
        ///
        /// @param timeLeft the time left of the wait, in nanoseconds, read again on each wakeup (an absolute deadline
        ///                 reads its clock each time), or `null` for no limit
        private Awaited blockUntilSignalled(final @Nullable LongSupplier timeLeft, final boolean interruptible)
                throws InterruptedException {
            guard.lock();
            try {
                checkOwner("await");
                if (interruptible && Thread.interrupted()) {
                    throw new InterruptedException();
                }
                Waiter waiter = new Waiter();
                waiters.add(waiter);
                if (!hasBreak(AWAIT_DOES_NOT_RELEASE_THE_LOCK)) {
                    owner = null;
                    released.signalAll();
                }
                WaitEnd end = waitForSignal(waiter, timeLeft, interruptible);
                waiters.remove(waiter);
                if (!hasBreak(AWAIT_DOES_NOT_REACQUIRE_THE_LOCK)) {
                    reacquire();
                }
                end.finish();
                return new Awaited(waiter.signalled, timeLeft == null ? 1 : timeLeft.getAsLong());
            } finally {
                guard.unlock();
            }
        }

        /// Waits under the guard until `waiter` is signalled, no time is left (unless `timeLeft` is `null`), or, if
        /// `interruptible`, the thread is interrupted before it is signalled, and returns the interrupt it met, if any.
        private WaitEnd waitForSignal(final Waiter waiter, final @Nullable LongSupplier timeLeft,
                                      final boolean interruptible) {
            boolean interrupted = false;
            while (!waiter.signalled) {
                long left = timeLeft == null ? 1 : timeLeft.getAsLong();
                if (left <= 0) {
                    break;
                }
                try {
                    if (timeLeft == null) {
                        wakeup.await();
                    } else {
                        long ignored = wakeup.awaitNanos(left);
                    }
                } catch (final InterruptedException e) {
                    if (interruptible && !waiter.signalled) {
                        return new WaitEnd(e, false);
                    }
                    interrupted = true;
                }
            }
            return new WaitEnd(null, interrupted);
        }

        /// Takes the lock again after a wait, uninterruptibly, as `Condition`'s Javadoc requires. Under the guard.
        private void reacquire() {
            Thread current = Thread.currentThread();
            while (owner != null && owner != current) {
                released.awaitUninterruptibly();
            }
            owner = current;
        }
    }
}
