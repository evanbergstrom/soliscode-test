/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

/// The checked exception a task or a future fails with on purpose, in a test or from a provider, so that the test
/// can tell its failure from any other.
///
/// Before #64 these failures were a plain `Exception`; the architecture gate refuses one.
///
/// @since 0.1.5
public final class DeliberateException extends Exception {

    private static final long serialVersionUID = 1L;

    /// Makes one with a message.
    ///
    /// @param message the message, which names the test or provider that made it
    public DeliberateException(final String message) {
        super(message);
    }

    /// Makes one that stands for another, as `ForkJoinTask` does when it reports a task's exception: it makes a copy
    /// of the same class, through a public constructor of this form, whose cause is the original.
    ///
    /// @param cause the exception this one is a copy of
    public DeliberateException(final Throwable cause) {
        super(cause);
    }
}
