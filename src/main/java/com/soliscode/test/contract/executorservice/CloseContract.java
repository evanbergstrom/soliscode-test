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
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [close()][ExecutorService#close()] method of an [ExecutorService]. It is not
/// [com.soliscode.test.contract.autocloseable.CloseContract], which tests `AutoCloseable.close` with a resource
/// provider.
///
/// It follows the Javadoc: `close` "initiates an orderly shutdown in which previously submitted tasks are executed,
/// but no new tasks will be accepted. This method waits until all tasks have completed execution and the executor
/// has terminated. If interrupted while waiting, this method stops all executing tasks as if by invoking
/// shutdownNow(). It then continues to wait until all actively executing tasks have completed. ... The interrupt
/// status will be re-asserted before this method returns. If already terminated, invoking this method has no
/// effect." That the tasks awaiting execution are not executed once interrupted is not tested, nor that `shutdownNow`
/// stops the running ones, which its Javadoc calls best effort: the interrupted test lets its task end from another
/// thread.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#close()
public interface CloseContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that `close` waits until another thread lets the held tasks end, and that they have all run and the
    /// executor has terminated when it returns.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("close() waits until the tasks have completed and the executor has terminated")
    default void closeWaitsForTheTasks() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                for (int i = 0; i < HELD_TASKS; i++) {
                    executor.execute(gate.task());
                }
                assertWaitsFor(() -> {
                    executor.close();
                    assertEquals(HELD_TASKS, gate.ended(), "the tasks that had run when close() returned");
                    assertTrue(executor.isTerminated(), "isTerminated() when close() returned");
                }, gate::open);
            } finally {
                gate.open();
            }
        });
    }

    /// Tests that a second `close` has no effect: it returns, and the executor is still terminated.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("a second close() has no effect")
    default void secondCloseHasNoEffect() throws Exception {
        using(executor -> {
            bounded(() -> {
                executor.close();
                return null;
            });
            bounded(() -> {
                executor.close();
                return null;
            });
            assertTrue(executor.isTerminated(), "isTerminated() after a second close()");
        });
    }

    /// Tests that `close` with the thread interrupted while a task runs returns once the task has ended, with the
    /// interrupt status set again and the executor terminated. Another thread opens the task's gate once the closing
    /// thread waits, so that the test does not depend on `shutdownNow` interrupting the task.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("close() when interrupted sets the interrupt status again")
    default void closeWhenInterruptedSetsTheInterruptStatusAgain() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                executor.execute(gate.task());
                gate.awaitStarted(1);
                Boolean interrupted = bounded(() -> {
                    Thread closer = Thread.currentThread();
                    Thread releaser = new Thread(() -> {
                        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SPIN_LIMIT_MILLIS);
                        while (!isWaiting(closer) && closer.isAlive() && System.nanoTime() - deadline < 0) {
                            Thread.onSpinWait();
                        }
                        gate.open();
                    }, "executor-service-contract-releaser");
                    releaser.setDaemon(true);
                    releaser.start();
                    closer.interrupt();
                    executor.close();
                    return Thread.interrupted();
                });
                assertTrue(interrupted, "the interrupt status after close() of an interrupted thread");
                assertEquals(1, gate.ended(), "the tasks that had ended when close() returned");
                assertTrue(executor.isTerminated(), "isTerminated() after close()");
            } finally {
                gate.open();
            }
        });
    }
}
