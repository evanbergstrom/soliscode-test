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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.support.FutureContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/// **Contract for `cancel` of a `Future` under contention**
///
/// [Future#cancel(boolean)]'s Javadoc: "If two or more threads cause a task to be cancelled, then at least one of
/// them returns `true`."
///
/// Over [ThreadSafeGetContract#ROUNDS] pending futures, [#CANCELLERS] cancellers go through the rounds in order,
/// meeting at a barrier before each so that they all call `cancel(false)` on the round's future at once. Each round
/// must have at least one call that returned `true`, and its future must then be cancelled and done.
///
/// The run is bounded by the same deadline and coordinator timeouts as [ThreadSafeGetContract]: each wait at the
/// barrier is timed, and a broken or timed-out barrier, or the deadline, fails the run and stops every canceller.
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @see Future#cancel(boolean)
/// @since 0.1.4
public interface ThreadSafeCancelContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// The number of threads ({@value}) that cancel each future at once.
    int CANCELLERS = 4;

    /// The longest ({@value} ms) a canceller waits at the barrier for the others.
    long BARRIER_TIMEOUT_MILLIS = 1_000;

    /// Tests that when several threads cancel a pending future at once, at least one of them returns `true`, and the
    /// future is cancelled.
    @DisplayName("cancel(boolean) called by several threads at once returns true for at least one")
    @VerySlow
    @ConcurrencyTest
    default void cancelWhenCalledUnderContentionIsThreadSafe() {
        int rounds = ThreadSafeGetContract.ROUNDS;
        List<F> futures = new ArrayList<>(rounds);
        for (int r = 0; r < rounds; r++) {
            futures.add(pending().future());
        }
        AtomicIntegerArray trues = new AtomicIntegerArray(rounds);
        AtomicReference<@Nullable String> failure = new AtomicReference<>();
        CyclicBarrier barrier = new CyclicBarrier(CANCELLERS);
        ContentionCoordinator<List<F>> coordinator = new ContentionCoordinator<>(
                ThreadSafeGetContract.START_TIMEOUT_SECONDS, ThreadSafeGetContract.SHUTDOWN_TIMEOUT_SECONDS,
                ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
        coordinator.add(CANCELLERS, 1, list -> {
            long deadline = System.nanoTime()
                    + TimeUnit.MILLISECONDS.toNanos(ThreadSafeGetContract.DEADLINE_MILLIS);
            for (int r = 0; r < rounds; r++) {
                long left = deadline - System.nanoTime();
                if (failure.get() != null || left <= 0) {
                    failure.compareAndSet(null, "the cancellers did not finish within "
                            + ThreadSafeGetContract.DEADLINE_MILLIS + " ms");
                    barrier.reset();
                    return;
                }
                try {
                    barrier.await(Math.min(left, TimeUnit.MILLISECONDS.toNanos(BARRIER_TIMEOUT_MILLIS)),
                            TimeUnit.NANOSECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (final BrokenBarrierException | TimeoutException e) {
                    failure.compareAndSet(null, "the cancellers did not meet at round " + r + ": " + e);
                    return;
                }
                if (list.get(r).cancel(false)) {
                    trues.incrementAndGet(r);
                }
            }
        });
        coordinator.execute(futures);
        assertNull(failure.get(), failure::get);
        for (int r = 0; r < rounds; r++) {
            if (trues.get(r) == 0) {
                fail("none of " + CANCELLERS + " cancel(false) calls at once on the future of round " + r
                        + " returned true");
            }
            F future = futures.get(r);
            int round = r;
            assertTrue(future.isCancelled(), () -> "isCancelled() of round " + round + " after the cancels");
            assertTrue(future.isDone(), () -> "isDone() of round " + round + " after the cancels");
        }
    }
}
