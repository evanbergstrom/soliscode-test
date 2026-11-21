/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.io.Serial;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.Nullable;

/// A future that can be broken in well-defined ways, to show that
/// [com.soliscode.test.contract.future.FutureContract] and
/// [com.soliscode.test.contract.future.ThreadSafeFutureContract] can fail.
///
/// Like [BreakableCloseable], it wraps no JDK implementation: it is a future of its own, with a state, a result and
/// an exception guarded by a lock, so that each break acts on that state and nothing else, and so that its
/// thread-unsafe breaks can step outside the lock. Unbroken, it is finished by [#complete(Object)] and
/// [#completeExceptionally(Throwable)], as a `CompletableFuture` is, and it keeps [Future]'s contract:
/// - `get` waits under the lock, and checks again on each wakeup;
/// - `cancel` cancels a pending future and returns `true`, and returns `false` on a future already done, cancelled
///   included, as `FutureTask` does;
/// - a `null` result is a result.
///
/// It is made with its constructor, and has no builder.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// ## Thread Safety
/// Thread safe, unless [#GET_IS_NOT_THREAD_SAFE], [#TIMED_GET_IS_NOT_THREAD_SAFE] or [#CANCEL_IS_NOT_THREAD_SAFE] is
/// set: its state is guarded by its lock. Its breaks are a copy of the set it was made with, which `addBreak`
/// changes, unsynchronized.
///
/// ## Serialization
/// It is serializable, as every breakable is, with its state and lock, when its result is.
///
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future
public class BreakableFuture<V> extends AbstractBreakable implements Future<V> {

    /// **Purpose:** `isDone` reports a pending future as done.
    ///
    /// **Effect:** `isDone()` returns `true` while the future is pending.
    ///
    /// **Affected Methods:** [#isDone()]
    public static final Break IS_DONE_IS_TRUE_WHILE_PENDING =
            new Break("is done is true while pending");

    /// **Purpose:** `isDone` leaves out completion by an exception, where its Javadoc says it is completion.
    ///
    /// **Effect:** `isDone()` returns `false` once the future has failed.
    ///
    /// **Affected Methods:** [#isDone()]
    public static final Break IS_DONE_IS_FALSE_ONCE_FAILED =
            new Break("is done is false once failed");

    /// **Purpose:** `isDone` leaves out completion by cancellation, where its Javadoc says it is completion.
    ///
    /// **Effect:** `isDone()` returns `false` once the future has been cancelled.
    ///
    /// **Affected Methods:** [#isDone()]
    public static final Break IS_DONE_IS_FALSE_ONCE_CANCELLED =
            new Break("is done is false once cancelled");

    /// **Purpose:** `isCancelled` takes a failure for a cancellation.
    ///
    /// **Effect:** `isCancelled()` returns `true` once the future has failed.
    ///
    /// **Affected Methods:** [#isCancelled()]
    public static final Break IS_CANCELLED_IS_TRUE_ONCE_FAILED =
            new Break("is cancelled is true once failed");

    /// **Purpose:** `isCancelled` does not report a cancellation.
    ///
    /// **Effect:** `isCancelled()` returns `false` once the future has been cancelled.
    ///
    /// **Affected Methods:** [#isCancelled()]
    public static final Break IS_CANCELLED_IS_FALSE_ONCE_CANCELLED =
            new Break("is cancelled is false once cancelled");

    /// **Purpose:** `cancel` that took effect does not say so, where its `@return` says `true`.
    ///
    /// **Effect:** `cancel` of a pending future cancels it, and returns `false`.
    ///
    /// **Affected Methods:** [#cancel(boolean)]
    public static final Break CANCEL_RETURNS_FALSE =
            new Break("cancel returns false");

    /// **Purpose:** `cancel` does not cancel.
    ///
    /// **Effect:** `cancel` of a pending future returns `true` and leaves it pending.
    ///
    /// **Affected Methods:** [#cancel(boolean)]
    public static final Break CANCEL_DOES_NOTHING =
            new Break("cancel does nothing");

    /// **Purpose:** `cancel` has an effect on a done future, where its Javadoc says it has none.
    ///
    /// **Effect:** `cancel` of a succeeded or failed future replaces its outcome with a cancellation, and returns
    /// `true`.
    ///
    /// **Affected Methods:** [#cancel(boolean)]
    public static final Break CANCEL_CANCELS_A_DONE_FUTURE =
            new Break("cancel cancels a done future");

    /// **Purpose:** a cancelled task runs, where `cancel`'s Javadoc says it "should never run".
    ///
    /// **Effect:** `complete` and `completeExceptionally` of a cancelled future replace the cancellation with their
    /// outcome.
    ///
    /// **Affected Methods:** [#complete(Object)], [#completeExceptionally(Throwable)]
    public static final Break COMPLETE_OVERRIDES_CANCELLATION =
            new Break("complete overrides cancellation");

    /// **Purpose:** `get` loses the result.
    ///
    /// **Effect:** `get()` returns `null` once the future has succeeded.
    ///
    /// **Affected Methods:** [#get()]
    public static final Break GET_RETURNS_NULL_ONCE_SUCCEEDED =
            new Break("get returns null once succeeded");

    /// **Purpose:** `get` loses the failure.
    ///
    /// **Effect:** `get()` of a failed future throws an [ExecutionException] with no cause.
    ///
    /// **Affected Methods:** [#get()]
    public static final Break GET_THROWS_EXECUTION_EXCEPTION_WITHOUT_CAUSE =
            new Break("get throws execution exception without cause");

    /// **Purpose:** `get` takes a cancellation for a failure.
    ///
    /// **Effect:** `get()` of a cancelled future throws an [ExecutionException], whose cause is a
    /// [CancellationException], in place of a `CancellationException`.
    ///
    /// **Affected Methods:** [#get()]
    public static final Break GET_THROWS_EXECUTION_EXCEPTION_ONCE_CANCELLED =
            new Break("get throws execution exception once cancelled");

    /// **Purpose:** `get` does not wait for the computation to complete.
    ///
    /// **Effect:** `get()` of a pending future returns `null` at once.
    ///
    /// **Affected Methods:** [#get()]
    public static final Break GET_DOES_NOT_WAIT =
            new Break("get does not wait");

    /// **Purpose:** `get` ignores an interrupt.
    ///
    /// **Effect:** `get()` of a pending future clears the thread's interrupt status and waits on; an interrupt during
    /// the wait still ends it.
    ///
    /// **Affected Methods:** [#get()]
    public static final Break GET_IGNORES_INTERRUPT =
            new Break("get ignores interrupt");

    /// **Purpose:** the timed `get` times out with no need to wait.
    ///
    /// **Effect:** `get(timeout, unit)` with a timeout of zero or less throws [TimeoutException], done or not.
    ///
    /// **Affected Methods:** [#get(long, TimeUnit)]
    public static final Break TIMED_GET_TIMES_OUT_WITH_ZERO_TIMEOUT_ONCE_DONE =
            new Break("timed get times out with zero timeout once done");

    /// **Purpose:** the timed `get` does not report its timeout.
    ///
    /// **Effect:** `get(timeout, unit)` of a future still pending once its timeout passes returns `null`.
    ///
    /// **Affected Methods:** [#get(long, TimeUnit)]
    public static final Break TIMED_GET_DOES_NOT_TIME_OUT =
            new Break("timed get does not time out");

    /// **Purpose:** the timed `get` does not wait for its timeout.
    ///
    /// **Effect:** `get(timeout, unit)` of a pending future throws [TimeoutException] at once.
    ///
    /// **Affected Methods:** [#get(long, TimeUnit)]
    public static final Break TIMED_GET_DOES_NOT_WAIT =
            new Break("timed get does not wait");

    /// **Purpose:** the timed `get` ignores an interrupt.
    ///
    /// **Effect:** `get(timeout, unit)` of a pending future clears the thread's interrupt status and waits out its
    /// timeout; an interrupt during the wait still ends it.
    ///
    /// **Affected Methods:** [#get(long, TimeUnit)]
    public static final Break TIMED_GET_IGNORES_INTERRUPT =
            new Break("timed get ignores interrupt");

    /// **Purpose:** `resultNow` refuses a result it has.
    ///
    /// **Effect:** `resultNow()` throws [IllegalStateException] once the future has succeeded.
    ///
    /// **Affected Methods:** [#resultNow()]
    public static final Break RESULT_NOW_THROWS_ONCE_SUCCEEDED =
            new Break("result now throws once succeeded");

    /// **Purpose:** `resultNow` gives a result the future does not have.
    ///
    /// **Effect:** `resultNow()` returns `null` while the future is pending.
    ///
    /// **Affected Methods:** [#resultNow()]
    public static final Break RESULT_NOW_RETURNS_NULL_WHILE_PENDING =
            new Break("result now returns null while pending");

    /// **Purpose:** `resultNow` gives a result the future does not have.
    ///
    /// **Effect:** `resultNow()` returns `null` once the future has failed.
    ///
    /// **Affected Methods:** [#resultNow()]
    public static final Break RESULT_NOW_RETURNS_NULL_ONCE_FAILED =
            new Break("result now returns null once failed");

    /// **Purpose:** `exceptionNow` wraps the exception, where its Javadoc says it returns "the exception thrown by the
    /// task".
    ///
    /// **Effect:** `exceptionNow()` of a failed future returns an [ExecutionException] whose cause is the failure.
    ///
    /// **Affected Methods:** [#exceptionNow()]
    public static final Break EXCEPTION_NOW_RETURNS_EXECUTION_EXCEPTION =
            new Break("exception now returns execution exception");

    /// **Purpose:** `exceptionNow` gives an exception the future does not have.
    ///
    /// **Effect:** `exceptionNow()` returns `null` once the future has succeeded.
    ///
    /// **Affected Methods:** [#exceptionNow()]
    public static final Break EXCEPTION_NOW_RETURNS_NULL_ONCE_SUCCEEDED =
            new Break("exception now returns null once succeeded");

    /// **Purpose:** `exceptionNow` takes a cancellation for a failure, where its Javadoc says it throws.
    ///
    /// **Effect:** `exceptionNow()` of a cancelled future returns a [CancellationException].
    ///
    /// **Affected Methods:** [#exceptionNow()]
    public static final Break EXCEPTION_NOW_RETURNS_CANCELLATION_EXCEPTION =
            new Break("exception now returns cancellation exception");

    /// **Purpose:** `state` reports a pending future as done.
    ///
    /// **Effect:** `state()` returns `SUCCESS` while the future is pending.
    ///
    /// **Affected Methods:** [#state()]
    public static final Break STATE_IS_SUCCESS_WHILE_PENDING =
            new Break("state is success while pending");

    /// **Purpose:** `state` takes a cancellation for a failure.
    ///
    /// **Effect:** `state()` returns `FAILED` once the future has been cancelled.
    ///
    /// **Affected Methods:** [#state()]
    public static final Break STATE_IS_FAILED_ONCE_CANCELLED =
            new Break("state is failed once cancelled");

    /// **Purpose:** `state` does not report a failure.
    ///
    /// **Effect:** `state()` returns `RUNNING` once the future has failed.
    ///
    /// **Affected Methods:** [#state()]
    public static final Break STATE_IS_RUNNING_ONCE_FAILED =
            new Break("state is running once failed");

    /// **Purpose:** a `null` result reads as no result, the fault of a future that uses `null` to mean not done.
    ///
    /// **Effect:** once the future has succeeded with `null`, `isDone()` returns `false` and `state()` returns
    /// `RUNNING`.
    ///
    /// **Affected Methods:** [#isDone()], [#state()]
    public static final Break NULL_RESULT_READS_AS_PENDING =
            new Break("null result reads as pending");

    /// **Purpose:** `get` misses a completion made by another thread while it waits.
    ///
    /// **Effect:** `get()` checks for completion without the lock, yields, and then takes the lock and waits for the
    /// next wakeup, not for the future to be done: a completion between the check and the wait is missed, and the
    /// thread waits until it is interrupted.
    ///
    /// **Affected Methods:** [#get()]
    public static final Break GET_IS_NOT_THREAD_SAFE =
            new Break("get is not thread safe");

    /// **Purpose:** the timed `get` misses a completion made by another thread while it waits.
    ///
    /// **Effect:** `get(timeout, unit)` checks for completion without the lock, yields, and then takes the lock and
    /// waits for the next wakeup, not for the future to be done: a completion between the check and the wait is
    /// missed, and the thread waits out its timeout.
    ///
    /// **Affected Methods:** [#get(long, TimeUnit)]
    public static final Break TIMED_GET_IS_NOT_THREAD_SAFE =
            new Break("timed get is not thread safe");

    /// **Purpose:** racing `cancel` calls all return `false`, where its Javadoc says at least one returns `true`.
    ///
    /// **Effect:** the `cancel` that cancels a pending future yields, and then returns `true` only if no other `cancel`
    /// was called in between. A later call returns `false`, as it does unbroken.
    ///
    /// **Affected Methods:** [#cancel(boolean)]
    public static final Break CANCEL_IS_NOT_THREAD_SAFE =
            new Break("cancel is not thread safe");

    @Serial
    private static final long serialVersionUID = 1L;

    /// Guards the state, the result, the exception and the count of `cancel` calls.
    private final ReentrantLock lock = new ReentrantLock();

    /// Signalled when the future is done.
    private final Condition finished = lock.newCondition();

    /// Its state. Read without the lock only by the thread-unsafe breaks.
    private State current = State.RUNNING;

    /// Its result, once succeeded.
    private @Nullable V result;

    /// Its exception, once failed.
    private @Nullable Throwable exception;

    /// The number of `cancel` calls, for [#CANCEL_IS_NOT_THREAD_SAFE].
    private int cancelCalls;

    /// The number of times the waiting threads were woken, for the thread-unsafe waits.
    private long signals;

    /// Creates a pending `BreakableFuture`, with the given breaks.
    ///
    /// @param breaks the set of breaks to apply, copied
    public BreakableFuture(final Set<Break> breaks) {
        super(breaks, Map.of(), false);
    }

    /// Completes it with a result, if it is pending.
    ///
    /// # Breaks
    /// - [#COMPLETE_OVERRIDES_CANCELLATION]
    ///
    /// @param value the result, which may be `null`
    /// @return `true` if this call completed it
    public boolean complete(final @Nullable V value) {
        return finish(State.SUCCESS, value, null);
    }

    /// Completes it with an exception, if it is pending.
    ///
    /// # Breaks
    /// - [#COMPLETE_OVERRIDES_CANCELLATION]
    ///
    /// @param failure the exception
    /// @return `true` if this call completed it
    public boolean completeExceptionally(final Throwable failure) {
        return finish(State.FAILED, null, failure);
    }

    /// Cancels it, if it is pending. `mayInterruptIfRunning` changes nothing: it has no computation to interrupt.
    ///
    /// # Breaks
    /// - [#CANCEL_RETURNS_FALSE]
    /// - [#CANCEL_DOES_NOTHING]
    /// - [#CANCEL_CANCELS_A_DONE_FUTURE]
    /// - [#CANCEL_IS_NOT_THREAD_SAFE]
    ///
    /// @param mayInterruptIfRunning ignored
    /// @return `true` if this call cancelled it
    @Override
    // ThreadPriorityCheck: CANCEL_IS_NOT_THREAD_SAFE yields to let another cancel in, on purpose.
    @SuppressWarnings("ThreadPriorityCheck")
    public boolean cancel(final boolean mayInterruptIfRunning) {
        int call;
        lock.lock();
        try {
            call = ++cancelCalls;
            if (current != State.RUNNING) {
                if (hasBreak(CANCEL_CANCELS_A_DONE_FUTURE) && current != State.CANCELLED) {
                    setState(State.CANCELLED, null, null);
                    return true;
                }
                return false;
            }
            if (hasBreak(CANCEL_DOES_NOTHING)) {
                return true;
            }
            setState(State.CANCELLED, null, null);
        } finally {
            lock.unlock();
        }
        if (hasBreak(CANCEL_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(CANCEL_IS_NOT_THREAD_SAFE)) {
            Thread.yield();
            lock.lock();
            try {
                return cancelCalls == call;
            } finally {
                lock.unlock();
            }
        }
        return true;
    }

    /// Returns whether it was cancelled.
    ///
    /// # Breaks
    /// - [#IS_CANCELLED_IS_TRUE_ONCE_FAILED]
    /// - [#IS_CANCELLED_IS_FALSE_ONCE_CANCELLED]
    ///
    /// @return whether it was cancelled
    @Override
    public boolean isCancelled() {
        State state = locked();
        if (state == State.FAILED && hasBreak(IS_CANCELLED_IS_TRUE_ONCE_FAILED)) {
            return true;
        }
        if (state == State.CANCELLED && hasBreak(IS_CANCELLED_IS_FALSE_ONCE_CANCELLED)) {
            return false;
        }
        return state == State.CANCELLED;
    }

    /// Returns whether it is done: succeeded, failed or cancelled.
    ///
    /// # Breaks
    /// - [#IS_DONE_IS_TRUE_WHILE_PENDING]
    /// - [#IS_DONE_IS_FALSE_ONCE_FAILED]
    /// - [#IS_DONE_IS_FALSE_ONCE_CANCELLED]
    /// - [#NULL_RESULT_READS_AS_PENDING]
    ///
    /// @return whether it is done
    @Override
    public boolean isDone() {
        lock.lock();
        try {
            return switch (current) {
                case RUNNING -> hasBreak(IS_DONE_IS_TRUE_WHILE_PENDING);
                case SUCCESS -> !(result == null && hasBreak(NULL_RESULT_READS_AS_PENDING));
                case FAILED -> !hasBreak(IS_DONE_IS_FALSE_ONCE_FAILED);
                case CANCELLED -> !hasBreak(IS_DONE_IS_FALSE_ONCE_CANCELLED);
            };
        } finally {
            lock.unlock();
        }
    }

    /// Waits until it is done, and returns its result.
    ///
    /// # Breaks
    /// - [#GET_RETURNS_NULL_ONCE_SUCCEEDED]
    /// - [#GET_THROWS_EXECUTION_EXCEPTION_WITHOUT_CAUSE]
    /// - [#GET_THROWS_EXECUTION_EXCEPTION_ONCE_CANCELLED]
    /// - [#GET_DOES_NOT_WAIT]
    /// - [#GET_IGNORES_INTERRUPT]
    /// - [#GET_IS_NOT_THREAD_SAFE]
    ///
    /// @return the result
    /// @throws CancellationException if it was cancelled
    /// @throws ExecutionException if it failed
    /// @throws InterruptedException if the thread is interrupted while it waits
    @Override
    // NullAway: a null result is a result, as Future permits (#13 types V), and GET_DOES_NOT_WAIT and
    // GET_RETURNS_NULL_ONCE_SUCCEEDED return null for one, on purpose.
    @SuppressWarnings("NullAway")
    public V get() throws InterruptedException, ExecutionException {
        if (!awaitForGet()) {
            return null;
        }
        return resultForGet();
    }

    /// Waits, for `get()`, until it is done; `false` if it is running and [#GET_DOES_NOT_WAIT] is set.
    private boolean awaitForGet() throws InterruptedException {
        if (hasBreak(GET_IS_NOT_THREAD_SAFE)) {
            waitUnsafely(0L);
            return true;
        }
        lock.lock();
        try {
            if (current == State.RUNNING && hasBreak(GET_DOES_NOT_WAIT)) {
                return false;
            }
            if (current == State.RUNNING && hasBreak(GET_IGNORES_INTERRUPT)) {
                boolean ignored = Thread.interrupted();
            }
            while (current == State.RUNNING) {
                finished.await();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /// What `get()` returns or throws once it is done.
    private @Nullable V resultForGet() throws ExecutionException {
        lock.lock();
        try {
            if (current == State.CANCELLED && hasBreak(GET_THROWS_EXECUTION_EXCEPTION_ONCE_CANCELLED)) {
                throw new ExecutionException(new CancellationException("cancelled"));
            }
            if (current == State.FAILED && hasBreak(GET_THROWS_EXECUTION_EXCEPTION_WITHOUT_CAUSE)) {
                throw new ExecutionException("failed", null);
            }
            if (current == State.SUCCESS && hasBreak(GET_RETURNS_NULL_ONCE_SUCCEEDED)) {
                return null;
            }
            return report();
        } finally {
            lock.unlock();
        }
    }

    /// Waits until it is done, for at most the given time, and returns its result.
    ///
    /// # Breaks
    /// - [#TIMED_GET_TIMES_OUT_WITH_ZERO_TIMEOUT_ONCE_DONE]
    /// - [#TIMED_GET_DOES_NOT_TIME_OUT]
    /// - [#TIMED_GET_DOES_NOT_WAIT]
    /// - [#TIMED_GET_IGNORES_INTERRUPT]
    /// - [#TIMED_GET_IS_NOT_THREAD_SAFE]
    ///
    /// @param timeout the longest time to wait
    /// @param unit    its unit
    /// @return the result
    /// @throws CancellationException if it was cancelled
    /// @throws ExecutionException if it failed
    /// @throws InterruptedException if the thread is interrupted while it waits
    /// @throws TimeoutException if it is not done by the timeout
    @Override
    // NullAway: a null result is a result, as Future permits (#13 types V), and TIMED_GET_DOES_NOT_TIME_OUT
    // returns null for one, on purpose.
    @SuppressWarnings("NullAway")
    public V get(final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        if (nanos <= 0 && hasBreak(TIMED_GET_TIMES_OUT_WITH_ZERO_TIMEOUT_ONCE_DONE)) {
            throw new TimeoutException("a timeout of " + timeout + " " + unit);
        }
        if (hasBreak(TIMED_GET_IS_NOT_THREAD_SAFE)) {
            waitUnsafely(Math.max(1L, nanos));
        } else {
            lock.lock();
            try {
                if (current == State.RUNNING && hasBreak(TIMED_GET_DOES_NOT_WAIT)) {
                    throw new TimeoutException("did not wait");
                }
                if (current == State.RUNNING && hasBreak(TIMED_GET_IGNORES_INTERRUPT)) {
                    boolean ignored = Thread.interrupted();
                }
                while (current == State.RUNNING && nanos > 0) {
                    nanos = finished.awaitNanos(nanos);
                }
            } finally {
                lock.unlock();
            }
        }
        lock.lock();
        try {
            if (current == State.RUNNING) {
                if (hasBreak(TIMED_GET_DOES_NOT_TIME_OUT)) {
                    return null;
                }
                throw new TimeoutException("not done after " + timeout + " " + unit);
            }
            return report();
        } finally {
            lock.unlock();
        }
    }

    /// Returns its result, without waiting.
    ///
    /// # Breaks
    /// - [#RESULT_NOW_THROWS_ONCE_SUCCEEDED]
    /// - [#RESULT_NOW_RETURNS_NULL_WHILE_PENDING]
    /// - [#RESULT_NOW_RETURNS_NULL_ONCE_FAILED]
    ///
    /// @return the result
    /// @throws IllegalStateException if it has not succeeded
    @Override
    // NullAway: a null result is a result, as Future permits (#13 types V), and RESULT_NOW_RETURNS_NULL_WHILE_PENDING
    // and RESULT_NOW_RETURNS_NULL_ONCE_FAILED return null, on purpose.
    @SuppressWarnings("NullAway")
    public V resultNow() {
        lock.lock();
        try {
            return switch (current) {
                case SUCCESS -> {
                    if (hasBreak(RESULT_NOW_THROWS_ONCE_SUCCEEDED)) {
                        throw new IllegalStateException("succeeded");
                    }
                    yield result;
                }
                case RUNNING -> {
                    if (hasBreak(RESULT_NOW_RETURNS_NULL_WHILE_PENDING)) {
                        yield null;
                    }
                    throw new IllegalStateException("not done");
                }
                case FAILED -> {
                    if (hasBreak(RESULT_NOW_RETURNS_NULL_ONCE_FAILED)) {
                        yield null;
                    }
                    throw new IllegalStateException("failed");
                }
                case CANCELLED -> throw new IllegalStateException("cancelled");
            };
        } finally {
            lock.unlock();
        }
    }

    /// Returns its exception, without waiting.
    ///
    /// # Breaks
    /// - [#EXCEPTION_NOW_RETURNS_EXECUTION_EXCEPTION]
    /// - [#EXCEPTION_NOW_RETURNS_NULL_ONCE_SUCCEEDED]
    /// - [#EXCEPTION_NOW_RETURNS_CANCELLATION_EXCEPTION]
    ///
    /// @return the exception
    /// @throws IllegalStateException if it has not failed
    @Override
    // NullAway: the exception field is set whenever the state is FAILED, and EXCEPTION_NOW_RETURNS_NULL_ONCE_SUCCEEDED
    // returns null, on purpose.
    @SuppressWarnings("NullAway")
    public Throwable exceptionNow() {
        lock.lock();
        try {
            return switch (current) {
                case FAILED -> {
                    if (hasBreak(EXCEPTION_NOW_RETURNS_EXECUTION_EXCEPTION)) {
                        yield new ExecutionException(exception);
                    }
                    yield exception;
                }
                case SUCCESS -> {
                    if (hasBreak(EXCEPTION_NOW_RETURNS_NULL_ONCE_SUCCEEDED)) {
                        yield null;
                    }
                    throw new IllegalStateException("succeeded");
                }
                case CANCELLED -> {
                    if (hasBreak(EXCEPTION_NOW_RETURNS_CANCELLATION_EXCEPTION)) {
                        yield new CancellationException("cancelled");
                    }
                    throw new IllegalStateException("cancelled");
                }
                case RUNNING -> throw new IllegalStateException("not done");
            };
        } finally {
            lock.unlock();
        }
    }

    /// Returns its state.
    ///
    /// # Breaks
    /// - [#STATE_IS_SUCCESS_WHILE_PENDING]
    /// - [#STATE_IS_FAILED_ONCE_CANCELLED]
    /// - [#STATE_IS_RUNNING_ONCE_FAILED]
    /// - [#NULL_RESULT_READS_AS_PENDING]
    ///
    /// @return its state
    @Override
    public State state() {
        lock.lock();
        try {
            return switch (current) {
                case RUNNING -> hasBreak(STATE_IS_SUCCESS_WHILE_PENDING) ? State.SUCCESS : State.RUNNING;
                case SUCCESS -> result == null && hasBreak(NULL_RESULT_READS_AS_PENDING) ? State.RUNNING
                        : State.SUCCESS;
                case FAILED -> hasBreak(STATE_IS_RUNNING_ONCE_FAILED) ? State.RUNNING : State.FAILED;
                case CANCELLED -> hasBreak(STATE_IS_FAILED_ONCE_CANCELLED) ? State.FAILED : State.CANCELLED;
            };
        } finally {
            lock.unlock();
        }
    }

    /// Returns its state and outcome.
    ///
    /// @return the string form
    @Override
    public String toString() {
        lock.lock();
        try {
            return "BreakableFuture[" + current + (current == State.SUCCESS ? ", " + result
                    : current == State.FAILED ? ", " + exception : "") + "]";
        } finally {
            lock.unlock();
        }
    }

    private boolean finish(final State state, final @Nullable V value, final @Nullable Throwable failure) {
        lock.lock();
        try {
            boolean overrides = current == State.CANCELLED && hasBreak(COMPLETE_OVERRIDES_CANCELLATION);
            if (current != State.RUNNING && !overrides) {
                return false;
            }
            setState(state, value, failure);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /// Sets the state and outcome, and wakes the threads waiting. The lock is held.
    private void setState(final State state, final @Nullable V value, final @Nullable Throwable failure) {
        current = state;
        result = value;
        exception = failure;
        signals++;
        finished.signalAll();
    }

    /// Returns the state, under the lock.
    private State locked() {
        lock.lock();
        try {
            return current;
        } finally {
            lock.unlock();
        }
    }

    /// The thread-unsafe wait of [#GET_IS_NOT_THREAD_SAFE] and [#TIMED_GET_IS_NOT_THREAD_SAFE]: it checks the state
    /// without the lock, yields, and then waits under the lock for the next wakeup after that, not for the future to
    /// be done, so that a completion between the check and the wait is missed.
    ///
    /// @param nanos the longest wait, or 0 for no limit
    // ThreadPriorityCheck: the yield widens the window in which a completion is missed, on purpose.
    @SuppressWarnings("ThreadPriorityCheck")
    private void waitUnsafely(final long nanos) throws InterruptedException {
        if (current != State.RUNNING) {
            return;
        }
        Thread.yield();
        lock.lock();
        try {
            long seen = signals;
            long left = nanos;
            while (signals == seen && (nanos == 0L || left > 0)) {
                if (nanos == 0L) {
                    finished.await();
                } else {
                    left = finished.awaitNanos(left);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /// The outcome of a done future: its result, or the exception `get` throws. The lock is held.
    private @Nullable V report() throws ExecutionException {
        return switch (current) {
            case SUCCESS -> result;
            case FAILED -> throw new ExecutionException(exception);
            case CANCELLED -> throw new CancellationException("cancelled");
            case RUNNING -> result;
        };
    }
}
