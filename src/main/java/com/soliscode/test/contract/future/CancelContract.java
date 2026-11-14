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

import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.FutureContractSupport;
import com.soliscode.test.provider.FutureSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [cancel(boolean)][Future#cancel(boolean)] method of a [Future].
///
/// It follows the Javadoc of [Future#cancel(boolean)]: it "has no effect if the task is already completed or
/// cancelled", and otherwise a task not yet started "should never run". It reads the Javadoc more strictly in two
/// places:
/// - `cancel` of a pending future the provider gave must succeed. The Javadoc lets a task "not be cancelled for some
///   other reason", but then its cancelled state could not be tested; `CompletableFuture` and `FutureTask` both
///   cancel it. As with [com.soliscode.test.contract.autocloseable.CloseContract]'s `close`.
/// - A `cancel` that took effect returns `true`, as `@return` says ("`true` otherwise"), though the Javadoc warns
///   that "the return value from this method does not necessarily indicate whether the task is now cancelled".
///
/// The return value of `cancel` on a future already cancelled is not tested: `CompletableFuture` returns `true`, and
/// `FutureTask` `false`. That `cancel(true)` interrupts a running computation is not tested: a provider's futures
/// have no running state.
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future#cancel(boolean)
public interface CancelContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// Tests that `cancel(false)` and `cancel(true)` on a pending future return `true` and cancel it.
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("cancel(boolean) of a pending future returns true and cancels it")
    default void cancelOfAPendingFutureCancelsIt() throws Exception {
        for (final boolean mayInterrupt : new boolean[] {false, true}) {
            F future = pending().future();
            assertTrue(future.cancel(mayInterrupt), () -> "cancel(" + mayInterrupt + ") of a pending future");
            assertCancelled(future, "after cancel(" + mayInterrupt + ")");
        }
    }

    /// Tests that `cancel` of a future already done has no effect: once succeeded or failed it returns `false` and
    /// leaves the outcome, and once cancelled it leaves it cancelled.
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("cancel(boolean) of a done future has no effect")
    default void cancelOfADoneFutureHasNoEffect() throws Exception {
        for (final boolean mayInterrupt : new boolean[] {false, true}) {
            FutureSource<F, V> succeeded = succeeded();
            assertFalse(succeeded.future().cancel(mayInterrupt), () -> "cancel(" + mayInterrupt
                    + ") of a succeeded future");
            assertSucceeded(succeeded.future(), succeeded.value(), "after cancel(" + mayInterrupt
                    + ") of a succeeded future");
            FutureSource<F, V> failed = failed();
            assertFalse(failed.future().cancel(mayInterrupt), () -> "cancel(" + mayInterrupt
                    + ") of a failed future");
            assertFailed(failed.future(), failed.exception(), "after cancel(" + mayInterrupt
                    + ") of a failed future");
            F cancelled = cancelled().future();
            boolean ignored = cancelled.cancel(mayInterrupt);
            assertCancelled(cancelled, "after a second cancel(" + mayInterrupt + ")");
        }
    }

    /// Tests that finishing a cancelled future, with its value or its exception, leaves it cancelled: "this task
    /// should never run".
    ///
    /// @throws Exception if a call on the future throws
    @Test
    @DisplayName("a cancelled future stays cancelled when finished")
    default void finishingACancelledFutureLeavesItCancelled() throws Exception {
        FutureSource<F, V> succeeding = cancelled();
        succeeding.succeed();
        assertCancelled(succeeding.future(), "after it was cancelled and then succeeded");
        FutureSource<F, V> failing = cancelled();
        failing.fail();
        assertCancelled(failing.future(), "after it was cancelled and then failed");
    }
}
