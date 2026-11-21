/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import com.soliscode.test.SupportedMethods;
import com.soliscode.test.safety.ContentionCoordinator;

/// Common interface for contract classes.
/// @param <T> the class of the objects being tested.
/// @author evanbergstrom
/// @since 1.0
public interface ContractSupport<T> extends ProviderSupport<T>, SupportedMethods {

    /// How many calls ({@value}) each thread makes in a phase of [#contentionCoordinator()]'s coordinator. A thread's
    /// calls in a thread-safety test can take less than a time slice, so on a machine whose cores are all busy the
    /// threads could run one after another and miss a check-then-act break; in phases, the threads still running
    /// start every phase together. On 18 busy processes on 18 cores, map breaks that had been caught in as few as 2
    /// of 20 single runs were caught in 20 of 20 (measured on #54).
    ///
    /// @since 0.1.5
    int PHASE_INTERVAL = 100;

    /// Returns the number of times an operation is repeated when a contract tests that it gives the same
    /// result each time, such as `equals` called twice on the same arguments.
    ///
    /// A test class may override it to repeat more or fewer times.
    ///
    /// @return the number of repetitions; 10 unless overridden
    /// @since 0.1.1
    default int consistencyRepeats() {
        return 10;
    }

    /// Returns whether the class under test compares its values for equality, so that a value and a copy of it are
    /// equal and have the same hash code.
    ///
    /// The tests of `equals` and `hashCode` that compare a value with its copy run only where this returns `true`.
    /// A class that keeps [Object]'s identity equality, as most queues do ([java.util.Queue]: implementations
    /// "generally do not define element-based versions of methods equals and hashCode"), returns `false`; the tests
    /// that hold for identity equality (reflexive, `null`, another type, a stable hash code) still run.
    ///
    /// @return `true` unless overridden
    /// @since 0.1.2
    default boolean equalsComparesValues() {
        return true;
    }

    /// Returns a coordinator for a thread-safety test: the default timeouts and yield interval, in phases of
    /// [#PHASE_INTERVAL] calls ([ContentionCoordinator#ContentionCoordinator(int, int, int, int)]).
    ///
    /// In phase mode no call may wait for another thread's progress beyond the current phase; a test whose calls
    /// block on one another keeps each phase balanced (as many puts as takes, for example).
    ///
    /// @return a new coordinator
    /// @since 0.1.5
    default ContentionCoordinator<T> contentionCoordinator() {
        return new ContentionCoordinator<>(ContentionCoordinator.DEFAULT_START_TIMEOUT_SECONDS,
                ContentionCoordinator.DEFAULT_SHUTDOWN_TIMEOUT_SECONDS, ContentionCoordinator.DEFAULT_YIELD_INTERVAL,
                PHASE_INTERVAL);
    }
}
