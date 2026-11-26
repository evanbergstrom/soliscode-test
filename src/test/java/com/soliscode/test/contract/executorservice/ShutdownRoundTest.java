/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.AbstractTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [ShutdownRound]'s own paths, each driven directly: in [ThreadSafeShutdownContract]'s run, which of them a
/// failing executor takes depends on how its race falls, so the contract's own tests cover them only some of the
/// time (#80).
@DisplayName("ShutdownRound Tests")
class ShutdownRoundTest extends AbstractTest {

    private static final int ROUND = 7;
    private static final int ID = 3;
    private static final long FAR_MILLIS = 60_000;

    /// Default constructor.
    ShutdownRoundTest() {
        super();
    }

    @Test
    @DisplayName("check passes a round whose executor has terminated and whose tasks ran as their outcomes say")
    void checkPassesATerminatedRoundWhoseTasksRanAsTheirOutcomesSay() {
        ExecutorService executor = terminated();
        AtomicIntegerArray runs = new AtomicIntegerArray(new int[] {1, 1, 0, 0});
        AtomicIntegerArray outcomes = new AtomicIntegerArray(new int[] {ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN,
            ShutdownOutcome.ACCEPTED, ShutdownOutcome.REJECTED, ShutdownOutcome.NONE});
        assertDoesNotThrow(() -> ShutdownRound.check(executor, ROUND, runs, outcomes, true, far()));
    }

    @Test
    @DisplayName("check fails a round with the first wrong task's message")
    void checkFailsARoundWithTheFirstWrongTasksMessage() {
        ExecutorService executor = terminated();
        AtomicIntegerArray runs = new AtomicIntegerArray(new int[] {1, 0, 2});
        AtomicIntegerArray outcomes = new AtomicIntegerArray(new int[] {ShutdownOutcome.ACCEPTED,
            ShutdownOutcome.ACCEPTED, ShutdownOutcome.ACCEPTED});
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> ShutdownRound.check(executor, ROUND, runs, outcomes, true, far()));
        assertEquals("task 1 of round 7 was accepted and never ran", error.getMessage(), "the message");
    }

    @Test
    @DisplayName("check fails a round whose executor does not terminate within the time left")
    void checkFailsARoundWhoseExecutorDoesNotTerminate() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            AtomicIntegerArray none = new AtomicIntegerArray(1);
            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> ShutdownRound.check(executor, ROUND, none, none, true, System.nanoTime()));
            assertEquals("the executor of round 7 did not terminate within "
                    + ThreadSafeShutdownContract.DEADLINE_MILLIS + " ms", error.getMessage(), "the message");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("check fails a round when its wait is interrupted, and sets the interrupt status again")
    void checkFailsARoundWhenInterruptedAndSetsTheInterruptStatusAgain() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            AtomicIntegerArray none = new AtomicIntegerArray(1);
            Thread.currentThread().interrupt();
            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> ShutdownRound.check(executor, ROUND, none, none, true, far()));
            assertTrue(Thread.interrupted(), "the interrupt status was not set again");
            assertEquals("interrupted while waiting for round 7 to terminate", error.getMessage(), "the message");
            assertInstanceOf(InterruptedException.class, error.getCause(), "the cause");
        } finally {
            boolean ignored = Thread.interrupted();
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("taskFailure finds nothing wrong with a task that ran as its outcome says")
    void taskFailureFindsNothingWrongWithATaskThatRanAsItsOutcomeSays() {
        for (final boolean rejects : new boolean[] {true, false}) {
            assertNull(failure(1, ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN, rejects), "accepted before, ran once");
            assertNull(failure(1, ShutdownOutcome.ACCEPTED, rejects), "accepted, ran once");
            assertNull(failure(0, ShutdownOutcome.REJECTED, rejects), "rejected, never ran");
            assertNull(failure(0, ShutdownOutcome.NONE, rejects), "never submitted, never ran");
        }
        assertNull(failure(0, ShutdownOutcome.ACCEPTED, false),
                "accepted after shutdown() by a class that may discard it, never ran");
    }

    @Test
    @DisplayName("taskFailure finds a task that ran more than once, whatever its outcome")
    void taskFailureFindsATaskThatRanMoreThanOnce() {
        for (final int outcome : new int[] {ShutdownOutcome.NONE, ShutdownOutcome.ACCEPTED,
            ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN, ShutdownOutcome.REJECTED}) {
            assertEquals("task 3 of round 7 ran 2 times", failure(2, outcome, true), "outcome " + outcome);
        }
    }

    @Test
    @DisplayName("taskFailure finds a task accepted before shutdown() that never ran, whether or not the class rejects")
    void taskFailureFindsATaskAcceptedBeforeShutdownThatNeverRan() {
        String expected = "task 3 of round 7 was accepted before shutdown() was called and never ran";
        assertEquals(expected, failure(0, ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN, true), "a class that rejects");
        assertEquals(expected, failure(0, ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN, false), "a class that discards");
    }

    @Test
    @DisplayName("taskFailure finds a task accepted by a class that rejects tasks after shutdown that never ran")
    void taskFailureFindsATaskAcceptedByARejectingClassThatNeverRan() {
        assertEquals("task 3 of round 7 was accepted and never ran", failure(0, ShutdownOutcome.ACCEPTED, true),
                "the message");
    }

    @Test
    @DisplayName("taskFailure finds a task that was rejected, or never submitted, and ran")
    void taskFailureFindsATaskThatWasNotAcceptedAndRan() {
        String expected = "task 3 of round 7 was rejected, or never submitted, and ran";
        assertEquals(expected, failure(1, ShutdownOutcome.REJECTED, true), "rejected");
        assertEquals(expected, failure(1, ShutdownOutcome.NONE, false), "never submitted");
    }

    private static @Nullable String failure(final int ran, final int outcome, final boolean rejects) {
        return ShutdownRound.taskFailure(ID, ROUND, ran, outcome, rejects);
    }

    private static long far() {
        return System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(FAR_MILLIS);
    }

    private static ExecutorService terminated() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.shutdown();
        return executor;
    }
}
