/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.assertions.actions;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A consumer that checks that it only consumes objects from a specified set of objects, and not consuming any objects
/// not in the set. This consumer does not require that all the objects in the set are consumed. Instead of
/// instantiating this class directly, the [AssertActions] utility class should be used:
/// ```java
/// Consumer<Integer> consumer = AssertActions.consumeOnly(Set.of(1, 3, 5, 7));
/// ```
/// @param <T> element type
/// @author evanbergstrom
/// @since 1.0
/// @see AssertActions
/// @see java.util.function.Consumer
public class AssertConsumeOnly<T> implements Consumer<T> {

    private final Iterable<T> expected;
    private final @Nullable Object messageOrSupplier;

    /// Creates a consumer that checks that it only consumes objects from a specified set of objects.
    /// @param expectedArg the set of objects that should be consumed.
    public AssertConsumeOnly(final Iterable<T> expectedArg) {
        this.expected = expectedArg;
        this.messageOrSupplier = null;
    }

    /// Creates a consumer that checks that it only consumes objects from a specified set of objects. This constructor
    /// allows specification of a string to be included in the exception if the assertion fails.
    /// @param expectedArg the set of objects that should be consumed.
    /// @param message the text to include in the exception.
    public AssertConsumeOnly(final Iterable<T> expectedArg, final @Nullable String message) {
        this.expected = expectedArg;
        this.messageOrSupplier = message;
    }

    /// Creates a consumer that checks that it only consumes objects from a specified set of objects. This constructor
    /// allows specification of a supplier of a message to be included in the exception.
    /// @param expectedArg the set of objects that should be consumed.
    /// @param messageSupplier the supplier of the text to include in the exception.
    public AssertConsumeOnly(final Iterable<T> expectedArg, final @Nullable Supplier<String> messageSupplier) {
        this.expected = expectedArg;
        this.messageOrSupplier = messageSupplier;
    }

    @Override
    public void accept(final T e) {
        if (!IterableTestUtils.contains(expected, e)) {
            throw assertionFailure()
                .message(messageOrSupplier)
                .expected(expected)
                .actual(e)
                .build();
        }
    }
}
