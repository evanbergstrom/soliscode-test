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
package com.soliscode.test.contract.future;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.FutureContractSupport;
import com.soliscode.test.provider.FutureSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [get()][Future#get()] method of a [Future], and for a `null` result.
///
/// It follows the Javadoc of [Future#get()]: it "waits if necessary for the computation to complete, and then
/// retrieves its result", and throws [CancellationException] "if the computation was cancelled",
/// [ExecutionException] "if the computation threw an exception", and [InterruptedException] "if the current thread
/// was interrupted while waiting". It reads that last more strictly: `get` on a pending future with the interrupt
/// status already set throws it, as [com.soliscode.test.contract.blockingqueue.TakeContract] reads `take`. `get` on
/// a done future while interrupted is not tested: the default `state()` notes that `get` "may throw
/// InterruptedException when done".
///
/// A `null` result, which [Future]'s Javadoc invites (a `Future<?>` that returns `null`), is tested here unless
/// [#permitsNullResult()] is overridden to return `false`.
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future#get()
public interface GetContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// Tests that `get()` returns the value once succeeded.
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("get() returns the result once succeeded")
    default void getReturnsTheResultOnceSucceeded() throws Exception {
        FutureSource<F, V> source = succeeded();
        assertEquals(source.value(), get(source.future()), "get() once succeeded");
    }

    /// Tests that `get()` throws [ExecutionException] once failed, whose cause is the exception the future failed
    /// with.
    @Test
    @DisplayName("get() throws ExecutionException with the failure as its cause once failed")
    default void getThrowsExecutionExceptionOnceFailed() {
        FutureSource<F, V> source = failed();
        ExecutionException thrown = assertThrows(ExecutionException.class, () -> get(source.future()),
                "get() once failed");
        assertSame(source.exception(), thrown.getCause(), "the cause of get()'s ExecutionException");
    }

    /// Tests that `get()` throws [CancellationException] once cancelled.
    @Test
    @DisplayName("get() throws CancellationException once cancelled")
    default void getThrowsCancellationExceptionOnceCancelled() {
        F future = cancelled().future();
        assertThrows(CancellationException.class, () -> get(future), "get() once cancelled");
    }

    /// Tests that `get()` on a pending future waits until another thread finishes it: it returns the value once the
    /// other thread succeeds it, and throws [CancellationException] once the other thread cancels it.
    @Test
    @DisplayName("get() waits until another thread succeeds or cancels the future")
    default void getWaitsForCompletion() {
        FutureSource<F, V> succeeding = pending();
        assertWaitsFor(succeeding.future(), () -> assertEquals(succeeding.value(), succeeding.future().get(),
                "get() after another thread succeeded the future"), succeeding::succeed);
        FutureSource<F, V> cancelling = pending();
        assertWaitsFor(cancelling.future(), () -> assertThrows(CancellationException.class,
                        cancelling.future()::get, "get() after another thread cancelled the future"),
                () -> cancelling.future().cancel(false));
    }

    /// Tests that `get()` on a pending future throws [InterruptedException] when the thread is interrupted.
    @Test
    @DisplayName("get() throws InterruptedException when interrupted while pending")
    default void getThrowsInterruptedExceptionWhenInterrupted() {
        F future = pending().future();
        assertThrows(InterruptedException.class, () -> boundedInterrupted(future::get),
                "get() of a pending future with the thread interrupted");
    }

    /// Tests that a `null` result is a result: once the future succeeds with `null`, it is done, its state is
    /// `SUCCESS`, and `get()`, the timed `get` and `resultNow()` return `null`. It passes at once where the class
    /// does not permit a null result ([#permitsNullResult()]).
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("a null result is a result, where the class permits one")
    default void aNullResultIsAResult() throws Exception {
        if (!permitsNullResult()) {
            return;
        }
        FutureSource<F, V> source = pending();
        source.succeedWith(null);
        F future = source.future();
        assertTrue(future.isDone(), "isDone() once succeeded with null");
        assertEquals(Future.State.SUCCESS, state(future), "state() once succeeded with null");
        assertNull(get(future), "get() once succeeded with null");
        assertNull(get(future, 0, TimeUnit.MILLISECONDS), "get(0, MILLISECONDS) once succeeded with null");
        assertNull(resultNow(future), "resultNow() once succeeded with null");
    }
}
