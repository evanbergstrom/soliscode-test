/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.executorservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.jspecify.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.fail;

/// The check of one round of [ThreadSafeShutdownContract]: its executor terminates within the time left, and each
/// task ran as its [ShutdownOutcome] says.
///
/// It is apart from the contract so that its own tests can drive each of its paths: in the contract's run, which of
/// them a failing executor takes depends on how its race falls (#80).
final class ShutdownRound {

    private ShutdownRound() { }

    /// Checks one round: its executor terminates within the time left, and each task ran as its outcome says.
    ///
    /// @param executor the round's executor
    /// @param round    the round
    /// @param runs     the times each task ran
    /// @param outcomes each task's [ShutdownOutcome]
    /// @param rejects  whether the class rejects tasks after shutdown
    ///                 ([ThreadSafeShutdownContract#rejectsTasksAfterShutdown()])
    /// @param deadline the run's deadline, in [System#nanoTime()]'s terms
    static void check(final ExecutorService executor, final int round, final AtomicIntegerArray runs,
            final AtomicIntegerArray outcomes, final boolean rejects, final long deadline) {
        try {
            if (!executor.awaitTermination(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS)) {
                fail("the executor of round " + round + " did not terminate within "
                        + ThreadSafeShutdownContract.DEADLINE_MILLIS + " ms");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("interrupted while waiting for round " + round + " to terminate", e);
        }
        for (int id = 0; id < runs.length(); id++) {
            @Nullable String wrong = taskFailure(id, round, runs.get(id), outcomes.get(id), rejects);
            if (wrong != null) {
                fail(wrong);
            }
        }
    }

    /// What one task of a round got wrong, if anything: it ran more than once, it was accepted (before `shutdown()`
    /// was called, or by a class that rejects tasks after shutdown) and never ran, or it was not accepted and ran.
    ///
    /// @param id      the task
    /// @param round   the round
    /// @param ran     the times it ran
    /// @param outcome its [ShutdownOutcome]
    /// @param rejects whether the class rejects tasks after shutdown
    /// @return the failure message, or `null` if the task ran as its outcome says
    static @Nullable String taskFailure(final int id, final int round, final int ran, final int outcome,
            final boolean rejects) {
        boolean mustRun = outcome == ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN
                || (rejects && outcome == ShutdownOutcome.ACCEPTED);
        if (ran > 1) {
            return "task " + id + " of round " + round + " ran " + ran + " times";
        }
        if (mustRun && ran == 0) {
            return "task " + id + " of round " + round + " was accepted"
                    + (outcome == ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN ? " before shutdown() was called" : "")
                    + " and never ran";
        }
        boolean accepted = outcome == ShutdownOutcome.ACCEPTED || outcome == ShutdownOutcome.ACCEPTED_BEFORE_SHUTDOWN;
        if (!accepted && ran != 0) {
            return "task " + id + " of round " + round + " was rejected, or never submitted, and ran";
        }
        return null;
    }
}
