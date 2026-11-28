/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// The base interface of the lock contracts' support: it holds locks on other threads, sees from another thread
/// whether a lock is held, makes calls that may block with a time limit, and tests that one thread's call waits until
/// another thread releases it, or that a waiting acquisition is interrupted.
///
/// **The time budget.** `ContractRuns`, which shows the contracts can fail, reports a test method that runs past
/// 10 s as hanging. So every call a break can strand is bounded: a [#bounded] call by [#BLOCKING_TIMEOUT_SECONDS], a
/// coordinator run by a start of [#COORDINATOR_START_SECONDS] and a shutdown of [#BLOCKING_TIMEOUT_SECONDS] (which it
/// waits twice for a stuck worker), the spins by [#SPIN_LIMIT_MILLIS] and the joins by [#JOIN_MILLIS]; a test makes at
/// most one such run or call that a break can strand. [#hold] returns at once or fails the test before any coordinator
/// starts, so its bound never adds to a coordinator's. A waiter a break strands in `lock()`, which cannot be
/// interrupted, is left behind as a daemon thread on a lock no other test uses, as [ContentionCoordinator] accepts for
/// such a worker; the holder releases its lock by itself after [#HOLD_LIMIT_SECONDS], so most such waiters end too.
///
/// **Who unlocks.** A thread unlocks only what it locked, since a lock may refuse an unlock by another thread
/// (`ReentrantLock` throws `IllegalMonitorStateException`); and no thread acquires a lock it already holds, since
/// `Lock` does not require reentrancy (`StampedLock` is not reentrant).
///
/// The bounded calls are copies of [ExecutorServiceContractSupport]'s, for the reason it gives: moving them into an
/// interface both extend would rename shipped nested types (#44, #45, #46).
///
/// @since 0.1.7
public interface LockingSupport {

    /// How long ({@value} s) a call that may block is given before the test fails, and how long the coordinators of
    /// [#assertWaitsFor] and [#assertInterruptedWhileWaiting] wait for their workers.
    int BLOCKING_TIMEOUT_SECONDS = 3;

    /// How long ({@value} s) the coordinators of [#assertWaitsFor] and [#assertInterruptedWhileWaiting] wait for their
    /// workers to start.
    int COORDINATOR_START_SECONDS = 1;

    /// The timeout ({@value} ms) given to a timed call that should time out.
    long TIMED_WAIT_MILLIS = 50;

    /// How long ({@value} ms), at most, a thread spins until another is waiting, or waits for its outcome.
    long SPIN_LIMIT_MILLIS = 1_000;

    /// The longest ({@value} s) a [Holder] holds its lock if it is not released: longer than any bounded call a test
    /// makes while it holds, so that the test can rely on it holding past such a call.
    int HOLD_LIMIT_SECONDS = 9;

    /// The longest ({@value} ms) [Holder#close()] waits for the holder to unlock.
    long JOIN_MILLIS = 500;

    /// A call that may block or throw a checked exception.
    @FunctionalInterface
    interface BlockingCall {
        /// Makes the call.
        ///
        /// @throws Exception what the call throws
        void run() throws Exception;
    }

    /// A call that acquires a lock, or tries to.
    @FunctionalInterface
    interface Acquisition {
        /// Makes the call.
        ///
        /// @return `true` if it acquired the lock (a call that returns nothing, such as `lockInterruptibly()`, returns
        ///         `true` when it returns)
        /// @throws InterruptedException if the call was interrupted
        boolean acquire() throws InterruptedException;
    }

    /// A thread that holds a lock until it is released, and then unlocks it itself.
    ///
    /// It takes the lock with `lock()`. It holds it until [#release()], or for at most [#HOLD_LIMIT_SECONDS], so that
    /// a holder left unreleased does not hold forever. [#close()] releases it and waits for it to unlock, for at most
    /// [#JOIN_MILLIS]; each test holds in a try-with-resources.
    final class Holder implements AutoCloseable {
        private final CountDownLatch release = new CountDownLatch(1);
        private final Thread thread;

        private Holder(final Lock lock, final CountDownLatch held) {
            thread = new Thread(() -> {
                lock.lock();
                try {
                    held.countDown();
                    boolean ignored = release.await(HOLD_LIMIT_SECONDS, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    unlockQuietly(lock);
                }
            }, "lock-contract-holder");
            thread.setDaemon(true);
        }

        /// Unlocks a lock the holder took. Cleanup only: a broken lock's `unlock()` that throws, as one taken over by
        /// another thread may, must not print from the holder's thread; the test has seen the fault or will.
        private static void unlockQuietly(final Lock lock) {
            try {
                lock.unlock();
            } catch (final RuntimeException e) {
                // The holder's own unlock: what is wrong with it is the test's to see.
            }
        }

        /// Releases the lock: the holder unlocks it.
        public void release() {
            release.countDown();
        }

        /// Waits, for at most [#SPIN_LIMIT_MILLIS], until the holder has unlocked the lock and ended: after that, a
        /// probe that finds the lock held is not finding the holder's hold. A correct lock fails a test that asserts
        /// this only if the holder's thread gets no CPU for that long after its `unlock()` hands the lock over.
        ///
        /// @return `true` if the holder has ended
        public boolean awaitUnlocked() {
            try {
                thread.join(SPIN_LIMIT_MILLIS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return !thread.isAlive();
        }

        /// Releases the lock, and waits for the holder to unlock it, for at most [#JOIN_MILLIS].
        @Override
        public void close() {
            release();
            try {
                thread.join(JOIN_MILLIS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /// Holds a lock on another thread, and returns once that thread holds it.
    ///
    /// @param lock the lock, which no thread holds
    /// @return the holder
    /// @throws AssertionFailedError if the thread did not hold it within [#BLOCKING_TIMEOUT_SECONDS]
    default Holder hold(final Lock lock) {
        CountDownLatch held = new CountDownLatch(1);
        Holder holder = new Holder(lock, held);
        holder.thread.start();
        try {
            if (!held.await(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                holder.release();
                throw new AssertionFailedError("another thread's lock() of a lock no thread holds did not return "
                        + "within " + BLOCKING_TIMEOUT_SECONDS + " s");
            }
        } catch (final InterruptedException e) {
            holder.release();
            Thread.currentThread().interrupt();
            throw new AssertionFailedError("the test thread was interrupted while another thread took the lock", e);
        }
        return holder;
    }

    /// Returns whether another thread could take the lock at once: on a bounded thread, `tryLock()`, and, if it
    /// returned `true`, `unlock()` at once. `Lock` has no query of whether it is held, so the contracts see it this
    /// way.
    ///
    /// @param lock the lock
    /// @return `true` if another thread's `tryLock()` returned `true`
    /// @throws Exception what `tryLock()` or `unlock()` throws
    default boolean tryLockFromAnotherThread(final Lock lock) throws Exception {
        return bounded(() -> {
            if (!lock.tryLock()) {
                return false;
            }
            lock.unlock();
            return true;
        });
    }

    /// Makes a call that may block, on a daemon thread of its own, and returns its result; fails the test if it does
    /// not return within [#BLOCKING_TIMEOUT_SECONDS], after interrupting the thread.
    ///
    /// An exception the call throws is thrown again, as itself, so that a test can expect it. If the calling thread
    /// is interrupted while it waits, the call's thread is interrupted too, the caller's interrupt status is set
    /// again, and the test fails: that interrupt is not the call's.
    ///
    /// @param call the call
    /// @param <T> the result type
    /// @return the call's result
    /// @throws Exception what the call throws
    default <T extends @Nullable Object> T bounded(final Callable<T> call) throws Exception {
        FutureTask<T> task = new FutureTask<>(call);
        Thread thread = new Thread(task, "lock-contract");
        thread.setDaemon(true);
        thread.start();
        try {
            return task.get(BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            thread.interrupt();
            throw new AssertionFailedError("the call did not return within " + BLOCKING_TIMEOUT_SECONDS
                    + " s: it blocked", e);
        } catch (final InterruptedException e) {
            thread.interrupt();
            Thread.currentThread().interrupt();
            throw new AssertionFailedError("the test thread was interrupted while it waited for the call", e);
        } catch (final ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }

    /// Makes a call that may block, as [#bounded(Callable)] does, with the thread's interrupt status set, and returns
    /// its result and whether the status was still set when the call returned or threw; the status is then cleared.
    /// The status is set on the thread that makes the call.
    ///
    /// @param call the call
    /// @return what happened
    /// @throws Exception what [#bounded(Callable)] throws for the call's thread
    default Interrupted boundedInterrupted(final Acquisition call) throws Exception {
        return bounded(() -> {
            Thread.currentThread().interrupt();
            boolean acquired = false;
            @Nullable InterruptedException thrown = null;
            try {
                acquired = call.acquire();
            } catch (final InterruptedException e) {
                thrown = e;
            }
            return new Interrupted(acquired, thrown, Thread.interrupted());
        });
    }

    /// What a call made with the interrupt status set did.
    ///
    /// @param acquired    whether it returned having acquired the lock
    /// @param thrown      the [InterruptedException] it threw, or `null` if it returned
    /// @param stillMarked whether the interrupt status was still set afterwards
    record Interrupted(boolean acquired, @Nullable InterruptedException thrown, boolean stillMarked) {
    }

    /// What a waiter does once its acquisition has returned: its checks of what it got, and the unlock of what it
    /// acquired.
    @FunctionalInterface
    interface AfterAcquisition {
        /// Runs the checks.
        ///
        /// @param acquired what the acquisition returned
        /// @throws Exception what the checks throw
        void run(boolean acquired) throws Exception;
    }

    /// Asserts that an acquisition waits until another thread's call releases it: `acquire` must not return before
    /// `release` has been called.
    ///
    /// Two threads of a [ContentionCoordinator] run the calls, once each, and it bounds them in time. The waiter reads
    /// whether it was released, and records that it returned, the moment `acquire` returns, before `then` makes its
    /// checks (which may block, and so look like waiting). The releasing thread waits (with [Thread#onSpinWait], at
    /// most [#SPIN_LIMIT_MILLIS]) until the waiter is waiting or has returned, and only then calls `release`, so that
    /// an acquisition that returns without waiting is caught however the threads are scheduled, but for a waiter
    /// descheduled for longer than that between its call's return and its next step. A call still in at the end of
    /// the spin counts as waiting, so that a lock that waits by spinning, not parking, passes. A run in which the
    /// waiter had not even begun its call by then fails, with a message that says the test could not tell: a correct
    /// lock fails it only if its waiter does not run for [#SPIN_LIMIT_MILLIS].
    ///
    /// @param acquire the acquisition that should wait
    /// @param then    the waiter's checks of what it got, which unlock what it acquired, whatever they find
    /// @param release the call that should release it
    default void assertWaitsFor(final Acquisition acquire, final AfterAcquisition then, final Runnable release) {
        AtomicReference<@Nullable Thread> waiter = new AtomicReference<>();
        AtomicBoolean released = new AtomicBoolean();
        AtomicBoolean returned = new AtomicBoolean();
        AtomicBoolean inCall = new AtomicBoolean();
        ContentionCoordinator<Object> coordinator = coordinator();
        coordinator.add(1, 1, unchecked(() -> {
            waiter.set(Thread.currentThread());
            inCall.set(true);
            boolean acquired = acquire.acquire();
            boolean wasReleased = released.get();
            returned.set(true);
            try {
                then.run(acquired);
            } catch (final Exception | Error e) {
                if (!wasReleased) {
                    AssertionFailedError early = new AssertionFailedError("the call returned before another thread "
                            + "released the lock");
                    early.addSuppressed(e);
                    throw early;
                }
                throw e;
            }
            assertTrue(wasReleased, "the call returned before another thread released the lock");
        }));
        coordinator.add(1, 1, o -> {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SPIN_LIMIT_MILLIS);
            boolean observed = false;
            while (!observed && System.nanoTime() - deadline < 0) {
                observed = isWaiting(waiter.get()) || returned.get();
                Thread.onSpinWait();
            }
            // A call begun is observed: still in after the whole spin, it is waiting, by spinning rather than parking;
            // returned, the waiter's own check judges whether it returned too early.
            observed |= inCall.get();
            released.set(true);
            release.run();
            if (!observed) {
                throw new AssertionFailedError("the waiter had not begun its call within " + SPIN_LIMIT_MILLIS
                        + " ms, so the test could not tell whether the call waited");
            }
        });
        coordinator.execute(new Object());
    }

    /// Asserts that an acquisition of a lock another thread holds is interrupted: another thread holds `lock`; a
    /// waiter makes `acquisition`; an interrupter waits (at most [#SPIN_LIMIT_MILLIS]) until the waiter is waiting,
    /// interrupts it, waits (at most [#SPIN_LIMIT_MILLIS]) for its outcome, and then releases the holder.
    ///
    /// Either way, an [InterruptedException] counts only if the interrupter had sent the interrupt (a lock that throws
    /// it at once because another thread holds the lock fails), and only if it leaves the lock free: once the holder
    /// has unlocked, another thread can take it.
    ///
    /// Where `interruptible`, the waiter must throw [InterruptedException], with its interrupt status cleared, while
    /// the holder still holds the lock: a lock that notices the interrupt only once a release wakes it fails. A correct
    /// lock passes whatever the timing: if the interrupter stops waiting early, the waiter is interrupted on entry,
    /// which a correct lock also throws for, but which would not show the acquisition interrupted while it waits; so
    /// where `interruptible`, a waiter not seen waiting fails the test, with a message that says the test could not
    /// interrupt a waiting acquisition. That reads `Lock`'s "becomes disabled for thread scheduling purposes and lies
    /// dormant" strictly: a waiter must park (`WAITING` or `TIMED_WAITING`), as every JDK lock's does; a lock that
    /// waits by spinning overrides `interruptsLockAcquisition()` to return `false`, which skips this check. It does
    /// depend on the waiter getting the CPU: a correct lock whose waiter does not reach its wait, or does not run, for
    /// [#SPIN_LIMIT_MILLIS] fails.
    ///
    /// Otherwise it checks what `Lock`'s Javadoc keeps for a lock whose acquisition cannot be interrupted while it
    /// waits ("either totally, or only on method entry", the thread "dormant until ... the lock is acquired"): the
    /// waiter throws [InterruptedException], or returns only once the holder has released, holding the lock.
    ///
    /// @param lock          the lock, which no thread holds
    /// @param acquisition   the waiter's call on `lock`
    /// @param interruptible whether the lock's acquisition is interrupted while it waits
    default void assertInterruptedWhileWaiting(final Lock lock, final Acquisition acquisition,
                                               final boolean interruptible) {
        AtomicReference<@Nullable Thread> waiter = new AtomicReference<>();
        AtomicReference<@Nullable String> failure = new AtomicReference<>();
        AtomicBoolean released = new AtomicBoolean();
        AtomicBoolean interruptSent = new AtomicBoolean();
        AtomicBoolean threw = new AtomicBoolean();
        CountDownLatch outcome = new CountDownLatch(1);
        try (Holder holder = hold(lock)) {
            ContentionCoordinator<Object> coordinator = coordinator();
            coordinator.add(1, 1, o -> {
                waiter.set(Thread.currentThread());
                try {
                    if (acquisition.acquire()) {
                        boolean wasReleased = released.get();
                        boolean ignored = Thread.interrupted();
                        failure.compareAndSet(null, checkAcquiredAfterInterrupt(lock, wasReleased, interruptible,
                                holder::awaitUnlocked));
                    } else {
                        failure.compareAndSet(null, "it returned without the lock when interrupted while waiting");
                    }
                } catch (final InterruptedException e) {
                    threw.set(true);
                    if (!interruptSent.get()) {
                        failure.compareAndSet(null, "it threw InterruptedException before it was interrupted, "
                                + "while another thread held the lock");
                    } else if (Thread.currentThread().isInterrupted()) {
                        failure.compareAndSet(null, "it threw InterruptedException with the interrupt status still "
                                + "set");
                    }
                } finally {
                    outcome.countDown();
                }
            });
            coordinator.add(1, 1, o -> {
                interruptWhenWaiting(waiter, outcome, failure, interruptSent, interruptible);
                try {
                    boolean answered = outcome.await(SPIN_LIMIT_MILLIS, TimeUnit.MILLISECONDS);
                    if (interruptible && !answered) {
                        failure.compareAndSet(null, "it did not answer the interrupt within " + SPIN_LIMIT_MILLIS
                                + " ms while another thread held the lock");
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    released.set(true);
                    holder.release();
                }
            });
            coordinator.execute(new Object());
            if (threw.get() && failure.get() == null) {
                failure.set(checkFreeAfterThrow(lock, holder));
            }
        }
        assertNull(failure.get(), () -> "an acquisition interrupted while another thread held the lock: "
                + failure.get());
    }

    /// The interrupter of [#assertInterruptedWhileWaiting]: waits until the waiter has started and is waiting (each
    /// at most [#SPIN_LIMIT_MILLIS]), records that it sends the interrupt, and interrupts it; where `interruptible`,
    /// a waiter not seen waiting, and not yet done, is a failure, since the test could not then interrupt a waiting
    /// acquisition.
    private void interruptWhenWaiting(final AtomicReference<@Nullable Thread> waiter, final CountDownLatch outcome,
                                      final AtomicReference<@Nullable String> failure,
                                      final AtomicBoolean interruptSent, final boolean interruptible) {
        spinUntilStarted(waiter);
        boolean seenWaiting = spinUntilWaiting(waiter);
        Thread target = waiter.get();
        if (target == null) {
            failure.compareAndSet(null, "the waiter did not start within " + SPIN_LIMIT_MILLIS + " ms");
            return;
        }
        if (interruptible && !seenWaiting && outcome.getCount() > 0) {
            failure.compareAndSet(null, "the waiter was not seen waiting within " + SPIN_LIMIT_MILLIS
                    + " ms, so the test could not interrupt a waiting acquisition");
        }
        interruptSent.set(true);
        target.interrupt();
    }

    /// Checks, once a waiter has thrown [InterruptedException], that it left the lock free: once the holder has
    /// unlocked and ended, another thread's `tryLock()` must be `true`. Returns what is wrong, or `null`.
    private @Nullable String checkFreeAfterThrow(final Lock lock, final Holder holder) {
        if (!holder.awaitUnlocked()) {
            return "the holder had not unlocked within " + SPIN_LIMIT_MILLIS + " ms after it was released";
        }
        try {
            return tryLockFromAnotherThread(lock) ? null
                    : "it threw InterruptedException, but left the lock held: another thread's tryLock() was false";
        } catch (final Exception | AssertionError e) {
            return "another thread's tryLock() failed: " + e;
        }
    }

    /// Checks a waiter that acquired the lock after it was interrupted, unlocks it whatever the check finds, and
    /// returns what is wrong, or `null` for nothing. The waiter has already taken and cleared its interrupt status,
    /// which a lock that does not interrupt acquisition may set again, so that the check's bounded call can wait.
    private @Nullable String checkAcquiredAfterInterrupt(final Lock lock, final boolean released,
                                                         final boolean interruptible,
                                                         final BooleanSupplier holderUnlocked) {
        String wrong = null;
        if (interruptible) {
            wrong = "it acquired the lock instead of throwing InterruptedException";
        } else if (!released) {
            wrong = "it returned before the holder released the lock";
        } else if (!holderUnlocked.getAsBoolean()) {
            wrong = "the holder had not unlocked within " + SPIN_LIMIT_MILLIS + " ms after the waiter returned";
        } else {
            try {
                if (tryLockFromAnotherThread(lock)) {
                    wrong = "it returned, but another thread could then take the lock: it did not hold it";
                }
            } catch (final Exception | AssertionError e) {
                wrong = "another thread's tryLock() failed: " + e;
            }
        }
        try {
            lock.unlock();
        } catch (final RuntimeException e) {
            if (wrong == null) {
                wrong = "its unlock() threw " + e + ": it did not hold the lock";
            }
        }
        return wrong;
    }

    /// Returns a coordinator, bounded as the time budget says: it starts its workers within
    /// [#COORDINATOR_START_SECONDS] and waits [#BLOCKING_TIMEOUT_SECONDS] for them.
    ///
    /// @return the coordinator
    default ContentionCoordinator<Object> coordinator() {
        return new ContentionCoordinator<>(COORDINATOR_START_SECONDS, BLOCKING_TIMEOUT_SECONDS,
                ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
    }

    /// Spins, at most [#SPIN_LIMIT_MILLIS], until a thread is in `waiter`.
    private void spinUntilStarted(final AtomicReference<@Nullable Thread> waiter) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SPIN_LIMIT_MILLIS);
        while (waiter.get() == null && System.nanoTime() - deadline < 0) {
            Thread.onSpinWait();
        }
    }

    /// Spins, at most [#SPIN_LIMIT_MILLIS], until the thread in `waiter` is waiting, and returns whether it saw it
    /// waiting.
    private boolean spinUntilWaiting(final AtomicReference<@Nullable Thread> waiter) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SPIN_LIMIT_MILLIS);
        while (System.nanoTime() - deadline < 0) {
            if (isWaiting(waiter.get())) {
                return true;
            }
            Thread.onSpinWait();
        }
        return isWaiting(waiter.get());
    }

    /// Turns a call that throws checked exceptions into a [Consumer] for a [ContentionCoordinator], which ignores its
    /// subject; a checked exception becomes an [AssertionError], and an [InterruptedException] sets the interrupt
    /// status again.
    ///
    /// @param call the call
    /// @param <S>  the type of the coordinator's subject
    /// @return the consumer
    default <S> Consumer<S> unchecked(final BlockingCall call) {
        return subject -> {
            try {
                call.run();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("interrupted", e);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new AssertionError(e);
            }
        };
    }

    /// Returns whether a thread is waiting: in the state [Thread.State#WAITING] or [Thread.State#TIMED_WAITING].
    ///
    /// @param thread the thread, or `null` for none yet
    /// @return `true` if the thread is waiting
    default boolean isWaiting(final @Nullable Thread thread) {
        if (thread == null) {
            return false;
        }
        Thread.State state = thread.getState();
        return state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING;
    }
}
