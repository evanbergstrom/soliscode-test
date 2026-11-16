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
