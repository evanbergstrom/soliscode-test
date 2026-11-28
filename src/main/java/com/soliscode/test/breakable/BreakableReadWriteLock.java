/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.io.Serial;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.Nullable;

/// A read-write lock that can be broken in well-defined ways, to show that
/// [com.soliscode.test.contract.readwritelock.ReadWriteLockContract] and
/// [com.soliscode.test.contract.readwritelock.ThreadSafeReadWriteLockContract] can fail.
///
/// Like [BreakableLock], it wraps no JDK implementation: it is a read-write lock of its own, a count of writers in, a
/// count of readers in and a count of writers waiting, under an internal lock and one condition its waiters wait on,
/// so that each break acts on that state and nothing else. Unbroken, it keeps [ReadWriteLock]'s contract:
/// - the read lock is shared by readers while no writer holds the write lock or waits for it (so that writers are
///   not starved);
/// - the write lock is exclusive of writers and readers;
/// - a writer that gives up (a timed `tryLock` that times out, an interrupt) stops counting as waiting, and wakes the
///   readers it held back;
/// - neither lock is reentrant: a thread that takes one again while it holds it, or takes the write lock while it
///   holds the read lock, may wait forever;
/// - each view keeps [Lock]'s contract as [BreakableLock] does, but that any thread may unlock a lock some thread
///   holds, as for the views of a `StampedLock`; an `unlock()` of a lock no thread holds throws
///   [IllegalMonitorStateException], and `newCondition()` throws [UnsupportedOperationException].
///
/// It is made with its constructor, and has no builder.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the view method that they impact.
///
/// ## Thread Safety
/// Thread safe, unless [#WRITE_LOCK_IS_NOT_THREAD_SAFE] or [#READ_LOCK_IS_NOT_THREAD_SAFE] is set: its state is
/// guarded by its internal lock. Its breaks are a copy of the set it was made with, which `addBreak` changes,
/// unsynchronized.
///
/// ## Serialization
/// It is serializable, as every breakable is. Its counts are not serialized, so a deserialized lock is unlocked.
///
/// @since 0.1.7
/// @see ReadWriteLock
public class BreakableReadWriteLock extends AbstractBreakable implements ReadWriteLock {

    /// **Purpose:** the read lock is not shared, where `ReadWriteLock`'s Javadoc says it "may be held simultaneously
    /// by multiple reader threads".
    ///
    /// **Effect:** a reader waits while another reader holds the read lock, as a writer would.
    ///
    /// **Affected Methods:** the acquiring methods of [#readLock()]
    public static final Break READ_LOCK_IS_EXCLUSIVE =
            new Break("read lock is exclusive");

    /// **Purpose:** the read lock does not wait for the writer, where `ReadWriteLock`'s Javadoc says readers share it
    /// "so long as there are no writers".
    ///
    /// **Effect:** a reader takes the read lock while a writer holds the write lock or waits for it.
    ///
    /// **Affected Methods:** the acquiring methods of [#readLock()]
    public static final Break READ_LOCK_IGNORES_THE_WRITE_LOCK =
            new Break("read lock ignores the write lock");

    /// **Purpose:** the write lock is not exclusive of other writers, where `ReadWriteLock`'s Javadoc says "The write
    /// lock is exclusive".
    ///
    /// **Effect:** a writer takes the write lock while another writer holds it; it still waits for readers.
    ///
    /// **Affected Methods:** the acquiring methods of [#writeLock()]
    public static final Break WRITE_LOCK_IS_SHARED =
            new Break("write lock is shared");

    /// **Purpose:** the write lock does not wait for readers.
    ///
    /// **Effect:** a writer takes the write lock while readers hold the read lock; it still waits for another writer.
    ///
    /// **Affected Methods:** the acquiring methods of [#writeLock()]
    public static final Break WRITE_LOCK_IGNORES_READERS =
            new Break("write lock ignores readers");

    /// **Purpose:** the write lock is not exclusive under contention: its acquisition is a check and then an act.
    ///
    /// **Effect:** a writer sees, under the guard, that no writer or reader is in, steps outside the guard and yields,
    /// then counts itself in without checking again, so that two writers, or a writer and a reader, can be in at once.
    /// Correct on one thread.
    ///
    /// **Affected Methods:** the acquiring methods of [#writeLock()]
    public static final Break WRITE_LOCK_IS_NOT_THREAD_SAFE =
            new Break("write lock is not thread safe");

    /// **Purpose:** the read lock does not exclude a writer under contention: its count of readers is not updated
    /// atomically.
    ///
    /// **Effect:** a reader that may go in, and a reader that unlocks, update the count of readers outside the guard,
    /// reading it, yielding and writing it back, so that two readers at once lose a count, and a writer can come in
    /// while a reader is in (or wait for one that left). Correct on one thread.
    ///
    /// **Affected Methods:** the acquiring methods and `unlock()` of [#readLock()]
    public static final Break READ_LOCK_IS_NOT_THREAD_SAFE =
            new Break("read lock is not thread safe");

    @Serial
    private static final long serialVersionUID = 1L;

    /// For [#acquire]: wait with no time limit.
    private static final long FOREVER = -1;

    /// Guards the counts.
    private final ReentrantLock guard = new ReentrantLock();

    /// Signalled when the counts change so that a waiter may go in.
    private final Condition changed = guard.newCondition();

    /// The number of writers in.
    private transient int writers;

    /// The number of readers in.
    private transient int readers;

    /// The number of writers waiting to go in.
    private transient int waitingWriters;

    /// The read lock, once made.
    private transient @Nullable Lock readView;

    /// The write lock, once made.
    private transient @Nullable Lock writeView;

    /// Creates an unlocked read-write lock with the given breaks.
    ///
    /// @param breaks the breaks to apply, copied; empty for an unbroken lock
    /// @throws NullPointerException if `breaks` is null
    public BreakableReadWriteLock(final Set<Break> breaks) {
        super(breaks, Map.of(), false);
    }

    /// Refuses to be cloned, even by a subclass that implements [Cloneable]: a copy would share this read-write lock's
    /// guard and state (and, for its views, this read-write lock itself), so that locking the copy locked the
    /// original.
    ///
    /// @return never
    /// @throws CloneNotSupportedException always
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("a BreakableReadWriteLock is not cloneable: a copy would share its state");
    }

    /// Returns the read lock.
    ///
    /// Breaks: [#READ_LOCK_IS_EXCLUSIVE], [#READ_LOCK_IGNORES_THE_WRITE_LOCK], [#READ_LOCK_IS_NOT_THREAD_SAFE].
    ///
    /// @return the read lock, the same object each time
    @Override
    public Lock readLock() {
        guard.lock();
        try {
            Lock view = readView;
            if (view == null) {
                view = new View(false);
                readView = view;
            }
            return view;
        } finally {
            guard.unlock();
        }
    }

    /// Returns the write lock.
    ///
    /// Breaks: [#WRITE_LOCK_IS_SHARED], [#WRITE_LOCK_IGNORES_READERS], [#WRITE_LOCK_IS_NOT_THREAD_SAFE].
    ///
    /// @return the write lock, the same object each time
    @Override
    public Lock writeLock() {
        guard.lock();
        try {
            Lock view = writeView;
            if (view == null) {
                view = new View(true);
                writeView = view;
            }
            return view;
        } finally {
            guard.unlock();
        }
    }

    /// Returns whether a reader may go in now. Under the guard.
    private boolean canRead() {
        if (hasBreak(READ_LOCK_IS_EXCLUSIVE) && readers > 0) {
            return false;
        }
        return hasBreak(READ_LOCK_IGNORES_THE_WRITE_LOCK) || (writers == 0 && waitingWriters == 0);
    }

    /// Returns whether a writer may go in now. Under the guard.
    private boolean canWrite() {
        boolean noOtherWriter = writers == 0 || hasBreak(WRITE_LOCK_IS_SHARED);
        return noOtherWriter && (readers == 0 || hasBreak(WRITE_LOCK_IGNORES_READERS));
    }

    /// Returns whether a reader or writer may go in now. Under the guard.
    private boolean canEnter(final boolean write) {
        return write ? canWrite() : canRead();
    }

    /// Counts a reader or writer in. Under the guard.
    private void enter(final boolean write) {
        if (write) {
            writers++;
        } else {
            readers++;
        }
    }

    /// The thread-unsafe breaks' step outside the guard: a writer yields and then counts itself in without checking
    /// again ([#WRITE_LOCK_IS_NOT_THREAD_SAFE]); a reader counts itself in outside the guard, reading the count,
    /// yielding and writing it back ([#READ_LOCK_IS_NOT_THREAD_SAFE]).
    // ThreadPriorityCheck: the yield widens the window of the faults the thread-unsafe breaks simulate.
    @SuppressWarnings("ThreadPriorityCheck")
    private void enterAfterYielding(final boolean write) {
        if (write) {
            Thread.yield();
            enterGuarded(true);
            return;
        }
        int count = readers;
        Thread.yield();
        readers = count + 1;
    }

    /// [#READ_LOCK_IS_NOT_THREAD_SAFE]'s release: counts a reader out outside the guard, reading the count, yielding
    /// and writing it back, then wakes the waiters under the guard.
    // ThreadPriorityCheck: the yield widens the window of the fault READ_LOCK_IS_NOT_THREAD_SAFE simulates.
    @SuppressWarnings("ThreadPriorityCheck")
    private void releaseReadUnsafely() {
        int count = readers;
        if (count == 0) {
            throw new IllegalMonitorStateException("unlock() of a read lock of a BreakableReadWriteLock no thread "
                    + "holds");
        }
        Thread.yield();
        readers = count - 1;
        guard.lock();
        try {
            changed.signalAll();
        } finally {
            guard.unlock();
        }
    }

    /// Counts a reader or writer in, under the guard of its own.
    private void enterGuarded(final boolean write) {
        guard.lock();
        try {
            enter(write);
        } finally {
            guard.unlock();
        }
    }

    /// Goes in as a reader or writer, waiting while it may not: with no limit for [#FOREVER], otherwise for at most
    /// `nanos`; interruptibly, or keeping an interrupt and setting the status again once it returns. A writer counts
    /// as waiting while it waits, and stops counting, waking the others, however it stops.
    ///
    /// @return `true` if it went in, `false` if the time elapsed first
    private boolean acquire(final boolean write, final long nanos, final boolean interruptible)
            throws InterruptedException {
        boolean unsafe = hasBreak(write ? WRITE_LOCK_IS_NOT_THREAD_SAFE : READ_LOCK_IS_NOT_THREAD_SAFE);
        long deadline = System.nanoTime() + Math.max(0, nanos);
        if (interruptible) {
            guard.lockInterruptibly();
        } else {
            guard.lock();
        }
        try {
            if (!waitAndEnter(write, nanos, deadline, interruptible, unsafe)) {
                return false;
            }
        } finally {
            guard.unlock();
        }
        if (unsafe) {
            enterAfterYielding(write);
        }
        return true;
    }

    /// Under the guard: counts a writer as waiting, waits until it may go in ([#awaitEntry]), and counts it in unless
    /// `unsafe` (the thread-unsafe breaks count themselves in later, outside the guard); a writer stops counting as
    /// waiting however it stops, and wakes the others if it gave up or has not gone in yet.
    ///
    /// @return `true` if it may go in, `false` if the time elapsed first
    private boolean waitAndEnter(final boolean write, final long nanos, final long deadline,
                                 final boolean interruptible, final boolean unsafe) throws InterruptedException {
        if (write) {
            waitingWriters++;
        }
        boolean ready = false;
        try {
            ready = awaitEntry(write, nanos, deadline, interruptible);
            if (ready && !unsafe) {
                enter(write);
            }
        } finally {
            if (write) {
                waitingWriters--;
                if (!ready || unsafe) {
                    changed.signalAll();
                }
            }
        }
        return ready;
    }

    /// Waits under the guard until a reader or writer may go in, or `deadline` passes (unless [#FOREVER]); an
    /// interrupt ends the wait if `interruptible`, and is otherwise kept, the status set again before it returns.
    ///
    /// @return `true` if it may go in, `false` if the time elapsed first
    private boolean awaitEntry(final boolean write, final long nanos, final long deadline,
                               final boolean interruptible) throws InterruptedException {
        boolean interrupted = false;
        try {
            while (!canEnter(write)) {
                long left = deadline - System.nanoTime();
                if (nanos != FOREVER && left <= 0) {
                    return false;
                }
                try {
                    if (nanos == FOREVER) {
                        changed.await();
                    } else {
                        long ignored = changed.awaitNanos(left);
                    }
                } catch (final InterruptedException e) {
                    if (interruptible) {
                        throw e;
                    }
                    interrupted = true;
                }
            }
            return true;
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /// Goes in, uninterruptibly, as [#acquire] does.
    private boolean acquireUninterruptibly(final boolean write, final long nanos) {
        try {
            return acquire(write, nanos, false);
        } catch (final InterruptedException e) {
            throw new IllegalStateException("an uninterruptible acquisition was interrupted", e);
        }
    }

    /// Counts a reader or writer out, and wakes the waiters.
    private void release(final boolean write) {
        if (!write && hasBreak(READ_LOCK_IS_NOT_THREAD_SAFE)) {
            releaseReadUnsafely();
            return;
        }
        guard.lock();
        try {
            if (write ? writers == 0 : readers == 0) {
                throw new IllegalMonitorStateException("unlock() of a " + (write ? "write" : "read")
                        + " lock of a BreakableReadWriteLock no thread holds");
            }
            if (write) {
                writers--;
            } else {
                readers--;
            }
            changed.signalAll();
        } finally {
            guard.unlock();
        }
    }

    /// The read lock or the write lock.
    private final class View implements Lock {
        private final boolean write;

        View(final boolean writeArg) {
            this.write = writeArg;
        }

        @Override
        public void lock() {
            acquireUninterruptibly(write, FOREVER);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            acquire(write, FOREVER, true);
        }

        @Override
        public boolean tryLock() {
            return acquireUninterruptibly(write, 0);
        }

        @Override
        public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
            long nanos = Math.min(Math.max(0, unit.toNanos(time)), Long.MAX_VALUE / 4);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            return acquire(write, nanos, true);
        }

        @Override
        public void unlock() {
            release(write);
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException("newCondition() is not supported by the " + (write ? "write"
                    : "read") + " lock of a BreakableReadWriteLock");
        }

        @Override
        public String toString() {
            return "BreakableReadWriteLock." + (write ? "writeLock()" : "readLock()");
        }
    }
}
