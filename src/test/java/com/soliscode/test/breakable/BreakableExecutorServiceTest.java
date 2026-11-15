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

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.soliscode.test.breakable.BreakableExecutorService.AWAIT_TERMINATION_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableExecutorService.AWAIT_TERMINATION_RETURNS_TRUE_ON_TIMEOUT;
import static com.soliscode.test.breakable.BreakableExecutorService.AWAIT_TERMINATION_TIMES_OUT_ONCE_TERMINATED;
import static com.soliscode.test.breakable.BreakableExecutorService.CLOSE_CLEARS_THE_INTERRUPT;
import static com.soliscode.test.breakable.BreakableExecutorService.CLOSE_DOES_NOT_WAIT;
import static com.soliscode.test.breakable.BreakableExecutorService.EXECUTE_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.EXECUTE_DOES_NOT_RUN_THE_TASK;
import static com.soliscode.test.breakable.BreakableExecutorService.EXECUTE_RUNS_THE_TASK_TWICE;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ALL_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ALL_REJECTS_NO_TASKS;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ALL_REVERSES_THE_ORDER;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ANY_ACCEPTS_EMPTY;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ANY_FAILS_IF_ANY_TASK_FAILS;
import static com.soliscode.test.breakable.BreakableExecutorService.INVOKE_ANY_RETURNS_NULL_WHEN_ALL_FAIL;
import static com.soliscode.test.breakable.BreakableExecutorService.IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN;
import static com.soliscode.test.breakable.BreakableExecutorService.IS_TERMINATED_IS_FALSE_ONCE_TERMINATED;
import static com.soliscode.test.breakable.BreakableExecutorService.IS_TERMINATED_IS_TRUE_BEFORE_SHUTDOWN;
import static com.soliscode.test.breakable.BreakableExecutorService.SECOND_CLOSE_THROWS;
import static com.soliscode.test.breakable.BreakableExecutorService.SECOND_SHUTDOWN_RESTARTS;
import static com.soliscode.test.breakable.BreakableExecutorService.SHUTDOWN_DOES_NOT_REJECT;
import static com.soliscode.test.breakable.BreakableExecutorService.SHUTDOWN_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableExecutorService.SHUTDOWN_NOW_DISCARDS_WAITING_TASKS;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_CALLABLE_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_CALLABLE_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_CALLABLE_SWALLOWS_THE_EXCEPTION;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_IS_NOT_THREAD_SAFE;
import static com.soliscode.test.breakable.BreakableExecutorService.SUBMIT_RUNNABLE_WITH_RESULT_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableExecutorService.TIMED_INVOKE_ALL_DOES_NOT_CANCEL;
import static com.soliscode.test.breakable.BreakableExecutorService.TIMED_INVOKE_ANY_DOES_NOT_TIME_OUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `BreakableExecutorService`: what it does unbroken, and what a break of each kind changes. That each
/// break fails the test it targets is shown by `ContractCoverageTest` (`ExecutorServiceSubjects`).
///
/// @see BreakableExecutorService
@DisplayName("Tests for BreakableExecutorService")
public class BreakableExecutorServiceTest {

    private static final long WAIT_SECONDS = 5;

    /// Default constructor.
    public BreakableExecutorServiceTest() {
        super();
    }

    private static BreakableExecutorService executor(final Break... breaks) {
        return new BreakableExecutorService(2, Set.of(breaks));
    }

    private static void terminate(final BreakableExecutorService executor) throws InterruptedException {
        executor.shutdown();
        assertTrue(executor.awaitTermination(WAIT_SECONDS, TimeUnit.SECONDS), "terminated after shutdown()");
    }

    @Test
    @DisplayName("Its constructor refuses no threads and null breaks")
    // NullAway: passes null breaks on purpose, to test that the constructor throws.
    @SuppressWarnings("NullAway")
    void constructorChecksItsArguments() {
        assertThrows(IllegalArgumentException.class, () -> new BreakableExecutorService(0, Set.of()));
        assertThrows(NullPointerException.class, () -> new BreakableExecutorService(1, null));
    }

    @Test
    @DisplayName("It refuses serialization")
    void refusesSerialization() throws Exception {
        BreakableExecutorService executor = executor();
        try (ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream())) {
            assertThrows(NotSerializableException.class, () -> out.writeObject(executor));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Unbroken, it runs tasks in order on at most its threads, and terminates after shutdown")
    void unbrokenRunsAndTerminates() throws Exception {
        BreakableExecutorService executor = new BreakableExecutorService(1, Set.of());
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int task = i;
            executor.execute(() -> order.add(task));
        }
        assertEquals("hello", executor.submit(() -> "hello").get(WAIT_SECONDS, TimeUnit.SECONDS));
        assertFalse(executor.isShutdown());
        assertFalse(executor.isTerminated());
        terminate(executor);
        assertTrue(executor.isShutdown());
        assertTrue(executor.isTerminated());
        assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), order, "one worker runs the tasks in order");
        assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> { }));
        assertTrue(executor.toString().contains("TERMINATED"), executor::toString);
    }

    @Test
    @DisplayName("Unbroken, shutdownNow returns the queued tasks, which never run, and interrupts the running one")
    void unbrokenShutdownNow() throws Exception {
        BreakableExecutorService executor = new BreakableExecutorService(1, Set.of());
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch never = new CountDownLatch(1);
        AtomicInteger interrupted = new AtomicInteger();
        AtomicInteger queuedRuns = new AtomicInteger();
        executor.execute(() -> {
            started.countDown();
            try {
                boolean ignored = never.await(WAIT_SECONDS, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                interrupted.incrementAndGet();
            }
        });
        Runnable queued = queuedRuns::incrementAndGet;
        executor.execute(queued);
        assertTrue(started.await(WAIT_SECONDS, TimeUnit.SECONDS));
        List<Runnable> returned = executor.shutdownNow();
        assertEquals(List.of(queued), returned);
        assertTrue(executor.awaitTermination(WAIT_SECONDS, TimeUnit.SECONDS));
        assertEquals(1, interrupted.get(), "the running task was interrupted");
        assertEquals(0, queuedRuns.get(), "the returned task did not run");
    }

    @Test
    @DisplayName("Unbroken, invokeAll and invokeAny give the tasks' outcomes, and the timed forms time out")
    void unbrokenInvoke() throws Exception {
        BreakableExecutorService executor = executor();
        try {
            List<Future<Integer>> futures = executor.invokeAll(List.of(() -> 1, () -> 2));
            assertEquals(1, futures.get(0).get());
            assertEquals(2, futures.get(1).get());
            List<Callable<Integer>> oneFails = List.of(() -> {
                throw new IllegalStateException("fails");
            }, () -> 7);
            assertEquals(7, executor.invokeAny(oneFails));
            CountDownLatch held = new CountDownLatch(1);
            Callable<Integer> waits = () -> {
                boolean ignored = held.await(WAIT_SECONDS, TimeUnit.SECONDS);
                return 3;
            };
            List<Future<Integer>> timed = executor.invokeAll(List.of(waits), 10, TimeUnit.MILLISECONDS);
            assertTrue(timed.get(0).isCancelled(), "the timed invokeAll cancels what is not done");
            assertThrows(TimeoutException.class, () -> executor.invokeAny(List.of(waits), 10,
                    TimeUnit.MILLISECONDS));
            held.countDown();
        } finally {
            terminate(executor);
        }
    }

    @Test
    @DisplayName("The execute and submit breaks change what runs and what the future gives")
    // NullAway: passes null tasks on purpose, to test the breaks that accept them.
    @SuppressWarnings("NullAway")
    void executeAndSubmitBreaks() throws Exception {
        AtomicInteger runs = new AtomicInteger();
        BreakableExecutorService dropping = executor(EXECUTE_DOES_NOT_RUN_THE_TASK);
        dropping.execute(runs::incrementAndGet);
        terminate(dropping);
        assertEquals(0, runs.get());
        BreakableExecutorService twice = executor(EXECUTE_RUNS_THE_TASK_TWICE);
        twice.execute(runs::incrementAndGet);
        terminate(twice);
        assertEquals(2, runs.get());
        BreakableExecutorService broken = executor(EXECUTE_ACCEPTS_NULL, SUBMIT_CALLABLE_ACCEPTS_NULL,
                SUBMIT_CALLABLE_RETURNS_NULL, SUBMIT_RUNNABLE_WITH_RESULT_RETURNS_NULL);
        try {
            broken.execute(null);
            assertNull(broken.submit((Callable<Object>) null).get());
            assertNull(broken.submit(() -> "result").get(WAIT_SECONDS, TimeUnit.SECONDS));
            assertNull(broken.submit(() -> { }, "result").get(WAIT_SECONDS, TimeUnit.SECONDS));
        } finally {
            terminate(broken);
        }
        BreakableExecutorService swallowing = executor(SUBMIT_CALLABLE_SWALLOWS_THE_EXCEPTION);
        try {
            assertNull(swallowing.submit(() -> {
                throw new IllegalStateException("swallowed");
            }).get(WAIT_SECONDS, TimeUnit.SECONDS));
        } finally {
            terminate(swallowing);
        }
    }

    @Test
    @DisplayName("The shutdown and state breaks change what shutdown, isShutdown and isTerminated do")
    void shutdownAndStateBreaks() throws Exception {
        BreakableExecutorService accepting = executor(SHUTDOWN_DOES_NOT_REJECT, IS_SHUTDOWN_IS_FALSE_AFTER_SHUTDOWN);
        accepting.shutdown();
        accepting.execute(() -> { });
        assertFalse(accepting.isShutdown());
        accepting.shutdownNow();
        BreakableExecutorService restarting = executor(SECOND_SHUTDOWN_RESTARTS, IS_TERMINATED_IS_TRUE_BEFORE_SHUTDOWN);
        assertTrue(restarting.isTerminated(), "an idle executor reads as terminated");
        restarting.shutdown();
        restarting.shutdown();
        assertFalse(restarting.isShutdown(), "a second shutdown restarts it");
        restarting.shutdownNow();
        BreakableExecutorService unterminated = executor(IS_TERMINATED_IS_FALSE_ONCE_TERMINATED,
                AWAIT_TERMINATION_TIMES_OUT_ONCE_TERMINATED);
        terminate(unterminated);
        assertFalse(unterminated.isTerminated());
        assertFalse(unterminated.awaitTermination(0, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("The awaitTermination breaks change how it waits and what it returns")
    void awaitTerminationBreaks() throws Exception {
        CountDownLatch held = new CountDownLatch(1);
        BreakableExecutorService early = executor(AWAIT_TERMINATION_DOES_NOT_WAIT);
        BreakableExecutorService lying = executor(AWAIT_TERMINATION_RETURNS_TRUE_ON_TIMEOUT);
        try {
            for (final BreakableExecutorService executor : List.of(early, lying)) {
                executor.execute(() -> {
                    try {
                        boolean ignored = held.await(WAIT_SECONDS, TimeUnit.SECONDS);
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                executor.shutdown();
            }
            assertFalse(early.awaitTermination(WAIT_SECONDS, TimeUnit.SECONDS), "returns at once, not terminated");
            assertTrue(lying.awaitTermination(10, TimeUnit.MILLISECONDS), "returns true on its timeout");
        } finally {
            held.countDown();
            early.shutdownNow();
            lying.shutdownNow();
        }
    }

    @Test
    @DisplayName("The shutdownNow and close breaks change what they return and how they wait")
    void shutdownNowAndCloseBreaks() throws Exception {
        CountDownLatch held = new CountDownLatch(1);
        BreakableExecutorService discarding = new BreakableExecutorService(1, Set.of(
                SHUTDOWN_NOW_DISCARDS_WAITING_TASKS));
        discarding.execute(() -> {
            try {
                boolean ignored = held.await(WAIT_SECONDS, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        discarding.execute(() -> { });
        assertEquals(List.of(), discarding.shutdownNow());
        held.countDown();
        BreakableExecutorService closing = executor(CLOSE_DOES_NOT_WAIT, SECOND_CLOSE_THROWS);
        closing.close();
        assertTrue(closing.isShutdown());
        assertTrue(closing.awaitTermination(WAIT_SECONDS, TimeUnit.SECONDS));
        assertThrows(IllegalStateException.class, closing::close);
        BreakableExecutorService clearing = executor(CLOSE_CLEARS_THE_INTERRUPT);
        Thread.currentThread().interrupt();
        clearing.close();
        assertFalse(Thread.interrupted(), "close() cleared the interrupt status");
    }

    @Test
    @DisplayName("The invokeAll and invokeAny breaks change what they return")
    // NullAway: passes a null task on purpose, to test the break that accepts it.
    @SuppressWarnings("NullAway")
    void invokeBreaks() throws Exception {
        BreakableExecutorService broken = executor(INVOKE_ALL_REVERSES_THE_ORDER, INVOKE_ALL_ACCEPTS_NULL,
                INVOKE_ALL_REJECTS_NO_TASKS, INVOKE_ANY_RETURNS_NULL_WHEN_ALL_FAIL, INVOKE_ANY_ACCEPTS_EMPTY,
                TIMED_INVOKE_ALL_DOES_NOT_CANCEL, TIMED_INVOKE_ANY_DOES_NOT_TIME_OUT);
        CountDownLatch held = new CountDownLatch(1);
        try {
            Callable<Integer> one = () -> 1;
            List<Future<Integer>> futures = broken.invokeAll(Arrays.asList(one, null, () -> 2));
            assertEquals(2, futures.size(), "the null task is skipped");
            assertEquals(2, futures.get(0).get(), "the order is reversed");
            assertEquals(List.of(), broken.invokeAll(null));
            assertThrows(IllegalArgumentException.class, () -> broken.invokeAll(List.<Callable<Integer>>of()));
            assertNull(broken.invokeAny(List.<Callable<Integer>>of(() -> {
                throw new IllegalStateException("fails");
            })));
            assertNull(broken.invokeAny(List.<Callable<Integer>>of()));
            Callable<Integer> waits = () -> {
                boolean ignored = held.await(WAIT_SECONDS, TimeUnit.SECONDS);
                return 3;
            };
            assertFalse(broken.invokeAll(List.of(waits), 10, TimeUnit.MILLISECONDS).get(0).isDone(),
                    "the timed invokeAll leaves the task not done");
            assertNull(broken.invokeAny(List.of(waits), 10, TimeUnit.MILLISECONDS));
        } finally {
            held.countDown();
            terminate(broken);
        }
        BreakableExecutorService failing = executor(INVOKE_ANY_FAILS_IF_ANY_TASK_FAILS);
        try {
            assertThrows(ExecutionException.class, () -> failing.invokeAny(List.of(() -> 1, () -> {
                throw new IllegalStateException("fails");
            })));
        } finally {
            terminate(failing);
        }
    }

    @Test
    @DisplayName("The thread-unsafe breaks change nothing on one thread")
    void threadUnsafeBreaksOnOneThread() throws Exception {
        for (final Break b : List.of(SUBMIT_IS_NOT_THREAD_SAFE, SHUTDOWN_IS_NOT_THREAD_SAFE)) {
            BreakableExecutorService executor = executor(b);
            AtomicInteger runs = new AtomicInteger();
            for (int i = 0; i < 100; i++) {
                executor.execute(runs::incrementAndGet);
            }
            Future<String> future = executor.submit(() -> "done");
            assertSame("done", future.get(WAIT_SECONDS, TimeUnit.SECONDS));
            terminate(executor);
            assertEquals(100, runs.get(), () -> "the tasks run with " + b.description());
            assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> { }));
        }
    }
}
