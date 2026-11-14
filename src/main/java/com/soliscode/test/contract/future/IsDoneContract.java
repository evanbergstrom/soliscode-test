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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [isDone()][Future#isDone()] method of a [Future].
///
/// It follows the Javadoc: "Completion may be due to normal termination, an exception, or cancellation -- in all of
/// these cases, this method will return `true`."
///
/// @param <F> the type of the future under test
/// @param <V> the type of its result
/// @since 0.1.4
/// @see Future#isDone()
public interface IsDoneContract<F extends Future<V>, V> extends FutureContractSupport<F, V> {

    /// Tests that `isDone()` is `false` while the future is pending.
    @Test
    @DisplayName("isDone() is false while pending")
    default void isDoneIsFalseWhilePending() {
        assertFalse(pending().future().isDone(), "isDone() while pending");
    }

    /// Tests that `isDone()` is `true` once the future has succeeded, failed or been cancelled.
    @Test
    @DisplayName("isDone() is true once succeeded, failed or cancelled")
    default void isDoneIsTrueOnceDone() {
        assertTrue(succeeded().future().isDone(), "isDone() once succeeded");
        assertTrue(failed().future().isDone(), "isDone() once failed");
        assertTrue(cancelled().future().isDone(), "isDone() once cancelled");
    }
}
