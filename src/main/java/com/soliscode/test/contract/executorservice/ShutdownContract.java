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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ExecutorServiceContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [shutdown()][ExecutorService#shutdown()] method of an [ExecutorService].
///
/// It follows the Javadoc: `shutdown` "initiates an orderly shutdown in which previously submitted tasks are
/// executed, but no new tasks will be accepted. Invocation has no additional effect if already shut down. This
/// method does not wait for previously submitted tasks to complete execution."
///
/// It reads "no new tasks will be accepted" strictly: `execute`, each `submit`, `invokeAll` and `invokeAny` throw
/// [RejectedExecutionException] after `shutdown`, as their `@throws` say they do for a task that "cannot be
/// scheduled for execution". A class that discards such tasks overrides [#rejectsTasksAfterShutdown()] to return
/// `false`; then `execute` and `submit` are checked only to never run the task, and `invokeAll` and `invokeAny` are
/// not called, since a discarded task's future never completes.
///
/// @param <E> the type of the executor under test
/// @since 0.1.5
/// @see ExecutorService#shutdown()
public interface ShutdownContract<E extends ExecutorService> extends ExecutorServiceContractSupport<E> {

    /// Tests that the tasks submitted before `shutdown` still run: [#HELD_TASKS] tasks wait at a gate, so that some
    /// are queued on a small pool, and once the executor has terminated after `shutdown`, every one has run.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("shutdown() lets the tasks already submitted run")
    default void shutdownRunsTheTasksAlreadySubmitted() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                for (int i = 0; i < HELD_TASKS; i++) {
                    executor.execute(gate.task());
                }
                executor.shutdown();
            } finally {
                gate.open();
            }
            assertTerminates(executor, "after shutdown()");
            assertEquals(HELD_TASKS, gate.ended(), "the tasks submitted before shutdown() that ran");
        });
    }

    /// Tests that after `shutdown`, `execute`, each `submit`, `invokeAll` and `invokeAny` throw
    /// [RejectedExecutionException], and that no task submitted after it runs.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("shutdown() rejects new tasks")
    default void shutdownRejectsNewTasks() throws Exception {
        using(executor -> {
            executor.shutdown();
            AtomicInteger runs = new AtomicInteger();
            Callable<Integer> callable = runs::incrementAndGet;
            Runnable runnable = runs::incrementAndGet;
            if (rejectsTasksAfterShutdown()) {
                assertThrows(RejectedExecutionException.class, () -> executor.execute(runnable),
                        "execute after shutdown()");
                assertThrows(RejectedExecutionException.class, () -> executor.submit(callable),
                        "submit(Callable) after shutdown()");
                assertThrows(RejectedExecutionException.class, () -> executor.submit(runnable),
                        "submit(Runnable) after shutdown()");
                assertThrows(RejectedExecutionException.class, () -> executor.submit(runnable, "result"),
                        "submit(Runnable, Object) after shutdown()");
                assertThrows(RejectedExecutionException.class, () -> bounded(() -> executor.invokeAll(
                        List.of(callable))), "invokeAll after shutdown()");
                assertThrows(RejectedExecutionException.class, () -> bounded(() -> executor.invokeAny(
                        List.of(callable))), "invokeAny after shutdown()");
            } else {
                try {
                    executor.execute(runnable);
                } catch (final RejectedExecutionException e) {
                    // A class that discards may also reject: either keeps the task from running.
                }
                List<Future<?>> discarded = new ArrayList<>();
                submitIgnoringRejection(discarded, () -> executor.submit(callable));
                submitIgnoringRejection(discarded, () -> executor.submit(runnable));
                submitIgnoringRejection(discarded, () -> executor.submit(runnable, "result"));
                for (final Future<?> future : discarded) {
                    boolean ignored = future.cancel(false);
                }
            }
            assertTerminates(executor, "after shutdown()");
            assertEquals(0, runs.get(), "the tasks submitted after shutdown() that ran");
        });
    }

    /// Tests that a second `shutdown` has no effect: the executor is still shut down, and terminates.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("a second shutdown() has no effect")
    default void secondShutdownHasNoEffect() throws Exception {
        using(executor -> {
            executor.shutdown();
            executor.shutdown();
            assertTrue(executor.isShutdown(), "isShutdown() after a second shutdown()");
            assertTerminates(executor, "after a second shutdown()");
        });
    }

    /// Tests that `shutdown` does not wait for the tasks: it returns while a task is held at a gate.
    ///
    /// @throws Exception if a call on the executor throws
    @Test
    @DisplayName("shutdown() does not wait for the tasks")
    default void shutdownDoesNotWait() throws Exception {
        using(executor -> {
            Gate gate = new Gate();
            try {
                executor.execute(gate.task());
                gate.awaitStarted(1);
                bounded(() -> {
                    executor.shutdown();
                    return null;
                });
                assertEquals(0, gate.ended(), "the tasks ended when shutdown() returned: it waited for them");
            } finally {
                gate.open();
            }
            assertTerminates(executor, "after shutdown()");
        });
    }

    /// Makes a submission that may be rejected or discarded, for a class that discards tasks after shutdown, and
    /// keeps the future it returns: a discarded task's future never completes, so the test cancels it rather than
    /// wait on it.
    ///
    /// @param futures    the futures kept
    /// @param submission the submission
    private static void submitIgnoringRejection(final List<Future<?>> futures,
            final Supplier<? extends Future<?>> submission) {
        try {
            futures.add(submission.get());
        } catch (final RejectedExecutionException e) {
            // A class that discards may also reject: either keeps the task from running.
        }
    }
}
