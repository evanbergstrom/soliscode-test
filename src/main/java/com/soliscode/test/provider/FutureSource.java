/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.concurrent.Future;

import org.jspecify.annotations.Nullable;

/// A future under test, not yet done when made, with the way to finish it: with its value, a result of the caller's,
/// or its exception. [FutureProvider#pending()] makes one per test.
///
/// Each source finishes its future at most once. A call after the first, or after the future was cancelled, is
/// passed to the future all the same, so that a test can check that it has no effect.
///
/// @param <F> the type of the future
/// @param <V> the type of its result
/// @since 0.1.4
/// @see FutureProvider
public interface FutureSource<F extends Future<V>, V> {

    /// Returns the future.
    ///
    /// @return the future
    F future();

    /// Returns the value that [#succeed()] completes the future with: not null, and distinct from other sources'.
    ///
    /// @return the value
    V value();

    /// Returns the exception that [#fail()] completes the future with: a checked exception of the source's own, the
    /// same object on every call, so that a test can compare by identity.
    ///
    /// @return the exception
    Exception exception();

    /// Completes the future with [#value()].
    default void succeed() {
        succeedWith(value());
    }

    /// Completes the future with the given result, which may be `null`.
    ///
    /// @param result the result
    void succeedWith(@Nullable V result);

    /// Completes the future with [#exception()].
    void fail();
}
