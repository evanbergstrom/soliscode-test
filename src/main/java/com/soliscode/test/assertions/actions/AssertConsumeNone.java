/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.assertions.actions;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A consumer that expects to consume no objects. It will throw an exception if any object is consumed, including
/// 'null'. Instead of instantiating this class directly, the [AssertActions] utility class should be used:
/// ```java
/// Consumer<Integer> consumer = AssertActions.consumeNone();
/// ```
/// @param <T> element type
/// @author evanbergstrom
/// @since 1.0
/// @see AssertActions
/// @see java.util.function.Consumer
public class AssertConsumeNone<T> implements Consumer<T> {

    private final @Nullable Object messageOrSupplier;

    /// Create a consumer that expect to consume no objects.
    public AssertConsumeNone() {
        this.messageOrSupplier = null;
    }

    /// Create a consumer that expect to consume no objects. This constructor allows specification of a string to be
    /// included in the exception of the assertion fails.
    /// @param message the text to include in the exception.
    public AssertConsumeNone(final @Nullable String message) {
        this.messageOrSupplier = message;
    }

    /// Create a consumer that expect to consume no objects. This constructor allows specification of a supplier of a
    /// message to be included in the exception.
    /// @param messageSupplier the supplier of the text to include in the exception.
    public AssertConsumeNone(final @Nullable Supplier<String> messageSupplier) {
        this.messageOrSupplier = messageSupplier;
    }

    @Override
    public void accept(final T e) {
        throw assertionFailure()
                .message(messageOrSupplier)
                .build();
    }
}
