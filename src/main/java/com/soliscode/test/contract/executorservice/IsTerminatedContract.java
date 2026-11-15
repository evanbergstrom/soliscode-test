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
package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [isTerminated()][ExecutorService#isTerminated()] method of an [ExecutorService].
///
/// It follows the Javadoc: it "returns `true` if all tasks have completed following shut down", and it "is never
/// true unless either shutdown or shutdownNow was called first".
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#isTerminated()
public interface IsTerminatedContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that `isTerminated()` is `false` before `shutdown`: for a new executor, and for one whose task has
    /// completed.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("isTerminated() is false before shutdown")
    default void isTerminatedIsFalseBeforeShutdown() throws Exception {
        using(executor -> {
            assertFalse(executor.isTerminated(), "isTerminated() of a new executor");
            Future<?> future = executor.submit(() -> { });
            bounded(future::get);
            assertFalse(executor.isTerminated(), "isTerminated() of an idle executor, not shut down");
        });
    }

    /// Tests that `isTerminated()` is `false` after `shutdown` while a task runs.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("isTerminated() is false after shutdown() while a task runs")
    default void isTerminatedIsFalseWhileTasksRun() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                executor.execute(gate.task());
                gate.awaitStarted(1);
                executor.shutdown();
                assertFalse(executor.isTerminated(), "isTerminated() after shutdown() while a task runs");
            } finally {
                gate.open();
            }
            assertTerminates(executor, "after shutdown()");
        });
    }

    /// Tests that `isTerminated()` is `true` once `awaitTermination` has returned `true`.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("isTerminated() is true once terminated")
    default void isTerminatedIsTrueOnceTerminated() throws Exception {
        using(executor -> {
            executor.execute(() -> { });
            executor.shutdown();
            assertTerminates(executor, "after shutdown()");
            assertTrue(executor.isTerminated(), "isTerminated() once awaitTermination returned true");
        });
    }
}
