/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.provider.FutureSource;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// The base interface of the future contracts: it gives them the provider's futures, checked, pending or finished
/// each way, calls that may block run with a time limit, and a test that one thread's call waits until another thread
/// finishes the future.
///
/// Every `get`, timed `get`, `resultNow`, `exceptionNow` and `state` the contracts make is bounded in time: through
/// [#bounded], or by the [ContentionCoordinator] or the deadline that runs it ([#assertWaitsFor], the thread-safety
/// contracts), so that a faulty future, one that blocks where it should not, fails a test rather than hanging it.
///
/// The bounded calls are copies of [BlockingQueueContractSupport]'s, with [BlockingCall] for its `BlockingAction`:
/// moving them into an interface both extend would rename `BlockingQueueContractSupport.BlockingAction`, which
/// shipped in 0.1.3, and break compiled callers (#44).
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @see com.soliscode.test.contract.future.FutureContract
/// @since 0.1.4
public interface FutureContractSupport<F extends Future<V>, V> extends FutureProviderSupport<F, V> {

    /// How long ({@value} s) a call that may block is given before the test fails: a call that should return at once
    /// and does not, fails the test rather than hanging it.
    int BLOCKING_TIMEOUT_SECONDS = 5;

    /// The timeout ({@value} ms) given to a timed `get` that should time out, or end at once.
    long TIMED_WAIT_MILLIS = 50;

    /// How long ({@value} ms), at most, the releasing thread of [#assertWaitsFor] waits to see the other thread
    /// waiting, before it releases it anyway.
    long SPIN_LIMIT_MILLIS = 1_000;

    /// A call that may block or throw a checked exception.
    @FunctionalInterface
    interface BlockingCall {
        /// Makes the call.
        ///
        /// @throws Exception what the call throws
        void run() throws Exception;
    }

    /// Whether the class under test permits a `null` result. [Future]'s Javadoc invites one ("declare types of the
    /// form `Future<?>` and return `null`"), so it is tested unless a class whose results cannot be null overrides
    /// this to return `false`.
    ///
    /// @return `true` unless overridden
    default boolean permitsNullResult() {
        return true;
    }

    /// Returns a new source from the provider, its future not yet done, checked: a null source or future fails the
    /// test with a message that says the provider is at fault, and a done one with a message that says the provider
    /// or `isDone()` is.
    ///
    /// @return the source
    default FutureSource<F, V> pending() {
        FutureSource<F, V> source = futureProvider().pending();
        if (source == null) {
            throw new AssertionFailedError("the future provider is at fault: it gave a null source");
        }
        if (source.future() == null) {
            throw new AssertionFailedError("the future provider is at fault: its source gave a null future");
        }
        if (source.future().isDone()) {
            throw new AssertionFailedError("the future provider gave a done future, or isDone() is wrong while "
                    + "pending");
        }
        return source;
    }

    /// Returns a new source whose future has succeeded with the source's value.
    ///
    /// @return the source
    default FutureSource<F, V> succeeded() {
        FutureSource<F, V> source = pending();
        source.succeed();
        return source;
    }

    /// Returns a new source whose future has failed with the source's exception.
    ///
    /// @return the source
    default FutureSource<F, V> failed() {
        FutureSource<F, V> source = pending();
        source.fail();
        return source;
    }

    /// Returns a new source whose future has been cancelled, by `cancel(false)` while pending.
    ///
    /// @return the source
    default FutureSource<F, V> cancelled() {
        FutureSource<F, V> source = pending();
        source.future().cancel(false);
        return source;
    }

    /// Calls `get()`, bounded.
    ///
    /// @param future the future
    /// @return its result
    /// @throws Exception what `get()` throws
    default @Nullable V get(final F future) throws Exception {
        return bounded(future::get);
    }

    /// Calls `get(timeout, unit)`, bounded.
    ///
    /// @param future  the future
    /// @param timeout the timeout
    /// @param unit    its unit
    /// @return its result
    /// @throws Exception what the timed `get` throws
    default @Nullable V get(final F future, final long timeout, final TimeUnit unit) throws Exception {
        return bounded(() -> future.get(timeout, unit));
    }

    /// Calls `resultNow()`, bounded.
    ///
    /// @param future the future
    /// @return its result
    /// @throws Exception what `resultNow()` throws
    default @Nullable V resultNow(final F future) throws Exception {
        return bounded(future::resultNow);
    }

    /// Calls `exceptionNow()`, bounded.
    ///
    /// @param future the future
    /// @return its exception
    /// @throws Exception what `exceptionNow()` throws
    default @Nullable Throwable exceptionNow(final F future) throws Exception {
        return bounded(future::exceptionNow);
    }

    /// Calls `state()`, bounded.
    ///
    /// @param future the future
    /// @return its state
    /// @throws Exception what `state()` throws
    default Future.@Nullable State state(final F future) throws Exception {
        return bounded(future::state);
    }

    /// Asserts that a future has succeeded with a result: it is done and not cancelled, its state is `SUCCESS`, and
    /// `get()` returns the result.
    ///
    /// @param future the future
    /// @param result the result expected
    /// @param when   when it is checked, for the failure message
    /// @throws Exception what a call on the future throws
    default void assertSucceeded(final F future, final @Nullable V result, final String when) throws Exception {
        assertTrue(future.isDone(), () -> "isDone() " + when);
        assertFalse(future.isCancelled(), () -> "isCancelled() " + when);
        assertEquals(Future.State.SUCCESS, state(future), () -> "state() " + when);
        assertEquals(result, get(future), () -> "get() " + when);
    }

    /// Asserts that a future has failed with an exception: it is done and not cancelled, its state is `FAILED`, and
    /// `get()` throws an [ExecutionException] whose cause is the exception.
    ///
    /// @param future    the future
    /// @param exception the exception expected
    /// @param when      when it is checked, for the failure message
    /// @throws Exception what a call on the future throws
    default void assertFailed(final F future, final Exception exception, final String when) throws Exception {
        assertTrue(future.isDone(), () -> "isDone() " + when);
        assertFalse(future.isCancelled(), () -> "isCancelled() " + when);
        assertEquals(Future.State.FAILED, state(future), () -> "state() " + when);
        ExecutionException thrown = assertThrows(ExecutionException.class, () -> get(future),
                () -> "get() " + when);
        assertSame(exception, thrown.getCause(), () -> "the cause of get()'s ExecutionException " + when);
    }

    /// Asserts that a future has been cancelled: it is done and cancelled, its state is `CANCELLED`, and `get()`
    /// throws [CancellationException].
    ///
    /// @param future the future
    /// @param when   when it is checked, for the failure message
    /// @throws Exception what a call on the future throws
    default void assertCancelled(final F future, final String when) throws Exception {
        assertTrue(future.isDone(), () -> "isDone() " + when);
        assertTrue(future.isCancelled(), () -> "isCancelled() " + when);
        assertEquals(Future.State.CANCELLED, state(future), () -> "state() " + when);
        assertThrows(CancellationException.class, () -> get(future), () -> "get() " + when);
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
        Thread thread = new Thread(task, "future-contract");
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

    /// Makes a call that may block, as [#bounded(Callable)] does, with the thread's interrupt status set, and clears
    /// the status afterwards. The status is set on the thread that makes the call.
    ///
    /// @param call the call
    /// @throws Exception what the call throws, [InterruptedException] for a call that waits
    default void boundedInterrupted(final BlockingCall call) throws Exception {
        bounded(() -> {
            Thread.currentThread().interrupt();
            try {
                call.run();
            } finally {
                boolean ignored = Thread.interrupted();
            }
            return null;
        });
    }

    /// Asserts that one call waits until another thread's call releases it: `waiting` must not return before
    /// `release` has been called.
    ///
    /// Two threads of a [ContentionCoordinator] run the calls, once each, and it bounds them in time. The releasing
    /// thread waits (with [Thread#onSpinWait], at most [#SPIN_LIMIT_MILLIS]) until the other is waiting, then calls
    /// `release`. The result does not depend on timing: if the releasing thread stops waiting early, a correct
    /// future still passes.
    ///
    /// @param future  the future
    /// @param waiting the call that should wait (a `get` on a pending future), with its own checks of what it gets
    /// @param release the call that should release it (finishing or cancelling the future)
    default void assertWaitsFor(final F future, final BlockingCall waiting, final Runnable release) {
        AtomicReference<@Nullable Thread> waiter = new AtomicReference<>();
        AtomicBoolean released = new AtomicBoolean();
        ContentionCoordinator<F> coordinator = new ContentionCoordinator<>(BLOCKING_TIMEOUT_SECONDS,
                BLOCKING_TIMEOUT_SECONDS, ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
        coordinator.add(1, 1, unchecked(() -> {
            waiter.set(Thread.currentThread());
            waiting.run();
            assertTrue(released.get(), "the call returned before another thread finished the future");
        }));
        coordinator.add(1, 1, f -> {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SPIN_LIMIT_MILLIS);
            while (!isWaiting(waiter.get()) && System.nanoTime() - deadline < 0) {
                Thread.onSpinWait();
            }
            released.set(true);
            release.run();
        });
        coordinator.execute(future);
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
