/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

/// The exception a breakable throws where a break makes it throw the wrong one: a [RuntimeException] that is none
/// of the exceptions the JDK's interfaces document, such as [NullPointerException], [ClassCastException],
/// [UnsupportedOperationException], [IllegalStateException], [IndexOutOfBoundsException] or
/// [java.util.NoSuchElementException], so that a test expecting one of those fails.
///
/// Before #64 these breaks threw a plain `RuntimeException`; the architecture gate refuses one.
///
/// @since 0.1.5
public final class WrongException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /// Makes one with no message.
    public WrongException() {
        super();
    }

    /// Makes one with a message.
    ///
    /// @param message the message
    public WrongException(final String message) {
        super(message);
    }

    /// Makes one with a message and the exception it stands in for.
    ///
    /// @param message the message
    /// @param cause   the exception the right behaviour would have thrown
    public WrongException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
