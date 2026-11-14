/*
 * Copyright 2026 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soliscode.test.breakable;

import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.soliscode.test.breakable.BreakableFuture.CANCEL_CANCELS_A_DONE_FUTURE;
import static com.soliscode.test.breakable.BreakableFuture.CANCEL_DOES_NOTHING;
import static com.soliscode.test.breakable.BreakableFuture.CANCEL_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableFuture.CANCEL_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableFuture.COMPLETE_OVERRIDES_CANCELLATION;
import static com.soliscode.test.breakable.BreakableFuture.EXCEPTION_NOW_RETURNS_CANCELLATION_EXCEPTION;
import static com.soliscode.test.breakable.BreakableFuture.EXCEPTION_NOW_RETURNS_EXECUTION_EXCEPTION;
import static com.soliscode.test.breakable.BreakableFuture.EXCEPTION_NOW_RETURNS_NULL_ONCE_SUCCEEDED;
import static com.soliscode.test.breakable.BreakableFuture.GET_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableFuture.GET_IGNORES_INTERRUPT;
import static com.soliscode.test.breakable.BreakableFuture.GET_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableFuture.GET_RETURNS_NULL_ONCE_SUCCEEDED;
import static com.soliscode.test.breakable.BreakableFuture.GET_THROWS_EXECUTION_EXCEPTION_ONCE_CANCELLED;
import static com.soliscode.test.breakable.BreakableFuture.GET_THROWS_EXECUTION_EXCEPTION_WITHOUT_CAUSE;
import static com.soliscode.test.breakable.BreakableFuture.IS_CANCELLED_IS_FALSE_ONCE_CANCELLED;
import static com.soliscode.test.breakable.BreakableFuture.IS_CANCELLED_IS_TRUE_ONCE_FAILED;
import static com.soliscode.test.breakable.BreakableFuture.IS_DONE_IS_FALSE_ONCE_CANCELLED;
import static com.soliscode.test.breakable.BreakableFuture.IS_DONE_IS_FALSE_ONCE_FAILED;
import static com.soliscode.test.breakable.BreakableFuture.IS_DONE_IS_TRUE_WHILE_PENDING;
import static com.soliscode.test.breakable.BreakableFuture.NULL_RESULT_READS_AS_PENDING;
import static com.soliscode.test.breakable.BreakableFuture.RESULT_NOW_RETURNS_NULL_ONCE_FAILED;
import static com.soliscode.test.breakable.BreakableFuture.RESULT_NOW_RETURNS_NULL_WHILE_PENDING;
import static com.soliscode.test.breakable.BreakableFuture.RESULT_NOW_THROWS_ONCE_SUCCEEDED;
import static com.soliscode.test.breakable.BreakableFuture.STATE_IS_FAILED_ONCE_CANCELLED;
import static com.soliscode.test.breakable.BreakableFuture.STATE_IS_RUNNING_ONCE_FAILED;
import static com.soliscode.test.breakable.BreakableFuture.STATE_IS_SUCCESS_WHILE_PENDING;
import static com.soliscode.test.breakable.BreakableFuture.TIMED_GET_DOES_NOT_TIME_OUT;
import static com.soliscode.test.breakable.BreakableFuture.TIMED_GET_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableFuture.TIMED_GET_IGNORES_INTERRUPT;
import static com.soliscode.test.breakable.BreakableFuture.TIMED_GET_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableFuture.TIMED_GET_TIMES_OUT_WITH_ZERO_TIMEOUT_ONCE_DONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `BreakableFuture`: what it does unbroken, and what each break changes.
///
/// @see BreakableFuture
@DisplayName("Tests for BreakableFuture")
public class BreakableFutureTest {

    private static final Exception FAILURE = new Exception("failure");

    /// Default constructor.
    public BreakableFutureTest() {
        super();
    }

    private static BreakableFuture<Integer> of(final Break... breaks) {
        return new BreakableFuture<>(Set.of(breaks));
    }

    private static BreakableFuture<Integer> succeeded(final Break... breaks) {
        BreakableFuture<Integer> future = of(breaks);
        assertTrue(future.complete(1));
        return future;
    }

    private static BreakableFuture<Integer> failed(final Break... breaks) {
        BreakableFuture<Integer> future = of(breaks);
        assertTrue(future.completeExceptionally(FAILURE));
        return future;
    }

    private static BreakableFuture<Integer> cancelled(final Break... breaks) {
        BreakableFuture<Integer> future = of(breaks);
        future.cancel(false);
        return future;
    }

    /// Runs a call on a thread of its own, waits until the thread is waiting, interrupts it, and returns what the
    /// call threw once the thread has ended. It fails if the thread is not seen waiting within 5 s, so that a call
    /// that ends without waiting cannot pass, or if it does not end within 5 s of the interrupt.
    private static @Nullable Throwable interruptedWhileWaiting(final Callable call) throws InterruptedException {
        AtomicReference<@Nullable Throwable> thrown = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                call.call();
            } catch (final Throwable t) {
                thrown.set(t);
            }
        });
        thread.setDaemon(true);
        thread.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        Thread.State seen = thread.getState();
        while (seen != Thread.State.WAITING && seen != Thread.State.TIMED_WAITING
                && System.nanoTime() - deadline < 0) {
            Thread.onSpinWait();
            seen = thread.getState();
        }
        Thread.State last = seen;
        assertTrue(last == Thread.State.WAITING || last == Thread.State.TIMED_WAITING,
                () -> "the call's thread was not seen waiting, but " + last + ", having thrown " + thrown.get());
        thread.interrupt();
        thread.join(TimeUnit.SECONDS.toMillis(5));
        assertFalse(thread.isAlive(), "the waiting thread ended once interrupted");
        return thrown.get();
    }

    /// A call that may throw.
    @FunctionalInterface
    private interface Callable {
        void call() throws Exception;
    }

    @Test
    @DisplayName("Unbroken, a pending future is not done and refuses resultNow and exceptionNow")
    void unbrokenPending() {
        BreakableFuture<Integer> future = of();
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        assertEquals(Future.State.RUNNING, future.state());
        assertThrows(IllegalStateException.class, future::resultNow);
        assertThrows(IllegalStateException.class, future::exceptionNow);
        assertThrows(TimeoutException.class, () -> future.get(1, TimeUnit.MILLISECONDS));
        assertTrue(future.toString().contains("RUNNING"));
    }

    @Test
    @DisplayName("Unbroken, a succeeded future gives its result, and a second completion has no effect")
    void unbrokenSucceeded() throws Exception {
        BreakableFuture<Integer> future = succeeded();
        assertFalse(future.complete(2));
        assertFalse(future.completeExceptionally(FAILURE));
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertEquals(Future.State.SUCCESS, future.state());
        assertEquals(1, future.get());
        assertEquals(1, future.get(0, TimeUnit.SECONDS));
        assertEquals(1, future.resultNow());
        assertThrows(IllegalStateException.class, future::exceptionNow);
        assertTrue(future.toString().contains("SUCCESS, 1"));
    }

    @Test
    @DisplayName("Unbroken, a failed future throws its failure from get and returns it from exceptionNow")
    void unbrokenFailed() {
        BreakableFuture<Integer> future = failed();
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertEquals(Future.State.FAILED, future.state());
        assertSame(FAILURE, assertThrows(ExecutionException.class, future::get).getCause());
        assertSame(FAILURE, assertThrows(ExecutionException.class, () -> future.get(0, TimeUnit.SECONDS))
                .getCause());
        assertSame(FAILURE, future.exceptionNow());
        assertThrows(IllegalStateException.class, future::resultNow);
        assertTrue(future.toString().contains("FAILED"));
    }

    @Test
    @DisplayName("Unbroken, cancel cancels a pending future and returns false once done, cancelled included")
    void unbrokenCancel() {
        BreakableFuture<Integer> future = of();
        assertTrue(future.cancel(true));
        assertFalse(future.cancel(true), "cancel of a cancelled future, as FutureTask answers");
        assertFalse(future.complete(1));
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        assertEquals(Future.State.CANCELLED, future.state());
        assertThrows(CancellationException.class, future::get);
        assertThrows(IllegalStateException.class, future::resultNow);
        assertThrows(IllegalStateException.class, future::exceptionNow);
        assertFalse(succeeded().cancel(false));
        assertFalse(failed().cancel(false));
    }

    @Test
    @DisplayName("Unbroken, a null result is a result")
    void unbrokenNullResult() throws Exception {
        BreakableFuture<Integer> future = of();
        assertTrue(future.complete(null));
        assertTrue(future.isDone());
        assertEquals(Future.State.SUCCESS, future.state());
        assertNull(future.get());
        assertNull(future.resultNow());
    }

    @Test
    @DisplayName("Unbroken, get waits for another thread's completion, and an interrupt ends the wait")
    void unbrokenGetWaits() throws Exception {
        BreakableFuture<Integer> future = of();
        Thread completer = new Thread(() -> future.complete(7));
        completer.start();
        assertEquals(7, future.get(5, TimeUnit.SECONDS));
        completer.join();
        assertInstanceOf(InterruptedException.class, interruptedWhileWaiting(of()::get));
        assertInstanceOf(InterruptedException.class,
                interruptedWhileWaiting(() -> of().get(5, TimeUnit.SECONDS)));
        Thread.currentThread().interrupt();
        assertThrows(InterruptedException.class, of()::get);
        Thread.currentThread().interrupt();
        assertThrows(InterruptedException.class, () -> of().get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("The isDone breaks change isDone only")
    void isDoneBreaks() {
        assertTrue(of(IS_DONE_IS_TRUE_WHILE_PENDING).isDone());
        assertEquals(Future.State.RUNNING, of(IS_DONE_IS_TRUE_WHILE_PENDING).state());
        assertFalse(failed(IS_DONE_IS_FALSE_ONCE_FAILED).isDone());
        assertEquals(Future.State.FAILED, failed(IS_DONE_IS_FALSE_ONCE_FAILED).state());
        assertFalse(cancelled(IS_DONE_IS_FALSE_ONCE_CANCELLED).isDone());
        assertTrue(cancelled(IS_DONE_IS_FALSE_ONCE_CANCELLED).isCancelled());
    }

    @Test
    @DisplayName("The isCancelled breaks change isCancelled only")
    void isCancelledBreaks() {
        assertTrue(failed(IS_CANCELLED_IS_TRUE_ONCE_FAILED).isCancelled());
        assertFalse(of(IS_CANCELLED_IS_TRUE_ONCE_FAILED).isCancelled());
        assertFalse(cancelled(IS_CANCELLED_IS_FALSE_ONCE_CANCELLED).isCancelled());
        assertEquals(Future.State.CANCELLED, cancelled(IS_CANCELLED_IS_FALSE_ONCE_CANCELLED).state());
    }

    @Test
    @DisplayName("The cancel breaks change what cancel returns or does")
    void cancelBreaks() {
        BreakableFuture<Integer> returnsFalse = of(CANCEL_RETURNS_FALSE);
        assertFalse(returnsFalse.cancel(false));
        assertTrue(returnsFalse.isCancelled());
        BreakableFuture<Integer> doesNothing = of(CANCEL_DOES_NOTHING);
        assertTrue(doesNothing.cancel(false));
        assertFalse(doesNothing.isDone());
        BreakableFuture<Integer> cancelsDone = succeeded(CANCEL_CANCELS_A_DONE_FUTURE);
        assertTrue(cancelsDone.cancel(false));
        assertTrue(cancelsDone.isCancelled());
        assertFalse(cancelled(CANCEL_CANCELS_A_DONE_FUTURE).cancel(false));
        BreakableFuture<Integer> overridden = cancelled(COMPLETE_OVERRIDES_CANCELLATION);
        assertTrue(overridden.complete(1));
        assertEquals(Future.State.SUCCESS, overridden.state());
        BreakableFuture<Integer> overriddenByFailure = cancelled(COMPLETE_OVERRIDES_CANCELLATION);
        assertTrue(overriddenByFailure.completeExceptionally(FAILURE));
        assertEquals(Future.State.FAILED, overriddenByFailure.state());
    }

    @Test
    @DisplayName("CANCEL_IS_NOT_THREAD_SAFE changes nothing on one thread")
    void cancelIsNotThreadSafeOnOneThread() {
        BreakableFuture<Integer> future = of(CANCEL_IS_NOT_THREAD_SAFE);
        assertTrue(future.cancel(false), "the call that cancels, with no other call in between");
        assertFalse(future.cancel(false), "a later call, as unbroken");
        assertTrue(future.isCancelled());
        assertFalse(succeeded(CANCEL_IS_NOT_THREAD_SAFE).cancel(false));
    }

    @Test
    @DisplayName("The get breaks change what get returns or throws")
    void getBreaks() throws Exception {
        assertNull(succeeded(GET_RETURNS_NULL_ONCE_SUCCEEDED).get());
        assertEquals(1, succeeded(GET_RETURNS_NULL_ONCE_SUCCEEDED).get(0, TimeUnit.SECONDS));
        assertNull(assertThrows(ExecutionException.class, failed(GET_THROWS_EXECUTION_EXCEPTION_WITHOUT_CAUSE)::get)
                .getCause());
        assertInstanceOf(CancellationException.class, assertThrows(ExecutionException.class,
                cancelled(GET_THROWS_EXECUTION_EXCEPTION_ONCE_CANCELLED)::get).getCause());
        assertNull(of(GET_DOES_NOT_WAIT).get());
        assertEquals(1, succeeded(GET_DOES_NOT_WAIT).get());
    }

    @Test
    @DisplayName("GET_IGNORES_INTERRUPT clears the status and waits on, until a later interrupt")
    void getIgnoresInterrupt() throws Exception {
        BreakableFuture<Integer> future = of(GET_IGNORES_INTERRUPT);
        assertInstanceOf(InterruptedException.class, interruptedWhileWaiting(() -> {
            Thread.currentThread().interrupt();
            future.get();
        }));
    }

    @Test
    @DisplayName("The timed get breaks change what the timed get returns or throws")
    void timedGetBreaks() throws Exception {
        assertThrows(TimeoutException.class,
                () -> succeeded(TIMED_GET_TIMES_OUT_WITH_ZERO_TIMEOUT_ONCE_DONE).get(0, TimeUnit.SECONDS));
        assertEquals(1, succeeded(TIMED_GET_TIMES_OUT_WITH_ZERO_TIMEOUT_ONCE_DONE).get(1, TimeUnit.SECONDS));
        assertNull(of(TIMED_GET_DOES_NOT_TIME_OUT).get(1, TimeUnit.MILLISECONDS));
        assertThrows(TimeoutException.class, () -> of(TIMED_GET_DOES_NOT_WAIT).get(1, TimeUnit.DAYS));
        BreakableFuture<Integer> ignores = of(TIMED_GET_IGNORES_INTERRUPT);
        Thread.currentThread().interrupt();
        assertThrows(TimeoutException.class, () -> ignores.get(10, TimeUnit.MILLISECONDS));
        assertFalse(Thread.interrupted(), "the interrupt status, cleared by the break");
    }

    @Test
    @DisplayName("The resultNow, exceptionNow and state breaks change those methods only")
    void nowAndStateBreaks() {
        assertThrows(IllegalStateException.class, succeeded(RESULT_NOW_THROWS_ONCE_SUCCEEDED)::resultNow);
        assertNull(of(RESULT_NOW_RETURNS_NULL_WHILE_PENDING).resultNow());
        assertNull(failed(RESULT_NOW_RETURNS_NULL_ONCE_FAILED).resultNow());
        assertSame(FAILURE, assertInstanceOf(ExecutionException.class,
                failed(EXCEPTION_NOW_RETURNS_EXECUTION_EXCEPTION).exceptionNow()).getCause());
        assertNull(succeeded(EXCEPTION_NOW_RETURNS_NULL_ONCE_SUCCEEDED).exceptionNow());
        assertInstanceOf(CancellationException.class,
                cancelled(EXCEPTION_NOW_RETURNS_CANCELLATION_EXCEPTION).exceptionNow());
        assertEquals(Future.State.SUCCESS, of(STATE_IS_SUCCESS_WHILE_PENDING).state());
        assertEquals(Future.State.FAILED, cancelled(STATE_IS_FAILED_ONCE_CANCELLED).state());
        assertEquals(Future.State.RUNNING, failed(STATE_IS_RUNNING_ONCE_FAILED).state());
        assertFalse(of(STATE_IS_RUNNING_ONCE_FAILED).isDone());
    }

    @Test
    @DisplayName("NULL_RESULT_READS_AS_PENDING changes isDone and state for a null result only")
    void nullResultReadsAsPending() throws Exception {
        BreakableFuture<Integer> future = of(NULL_RESULT_READS_AS_PENDING);
        future.complete(null);
        assertFalse(future.isDone());
        assertEquals(Future.State.RUNNING, future.state());
        assertNull(future.get());
        assertTrue(succeeded(NULL_RESULT_READS_AS_PENDING).isDone());
    }

    @Test
    @DisplayName("The thread-unsafe gets see a completion made while they wait, and end once interrupted")
    void threadUnsafeGets() throws Exception {
        for (final Break b : new Break[] {GET_IS_NOT_THREAD_SAFE, TIMED_GET_IS_NOT_THREAD_SAFE}) {
            assertEquals(1, succeeded(b).get());
            assertEquals(1, succeeded(b).get(0, TimeUnit.SECONDS));
            assertEquals(1, completedWhileWaiting(of(b), b == TIMED_GET_IS_NOT_THREAD_SAFE));
            assertInstanceOf(InterruptedException.class, interruptedWhileWaiting(of(b)::get));
            assertInstanceOf(InterruptedException.class,
                    interruptedWhileWaiting(() -> of(b).get(30, TimeUnit.SECONDS)));
        }
        assertThrows(TimeoutException.class, () -> of(TIMED_GET_IS_NOT_THREAD_SAFE).get(1, TimeUnit.MILLISECONDS));
    }

    /// Calls `get` (or the timed `get`) on a thread of its own, completes the future with 1 once that thread is
    /// waiting, and returns what `get` returned.
    private static @Nullable Integer completedWhileWaiting(final BreakableFuture<Integer> future, final boolean timed)
            throws InterruptedException {
        AtomicReference<@Nullable Integer> got = new AtomicReference<>();
        Thread waiter = new Thread(() -> {
            try {
                got.set(timed ? future.get(30, TimeUnit.SECONDS) : future.get());
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                throw new AssertionError(e);
            }
        });
        waiter.setDaemon(true);
        waiter.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (waiter.getState() != Thread.State.WAITING && waiter.getState() != Thread.State.TIMED_WAITING
                && System.nanoTime() - deadline < 0) {
            Thread.onSpinWait();
        }
        future.complete(1);
        waiter.join(TimeUnit.SECONDS.toMillis(5));
        assertFalse(waiter.isAlive(), "the waiter ended once the future completed");
        return got.get();
    }
}
