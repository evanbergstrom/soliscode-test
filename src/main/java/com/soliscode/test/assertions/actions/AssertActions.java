/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.assertions.actions;


import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/// Utility class for creating assertion actions to provide to methods that are being tested. Three versions of each
/// method are provided, with two versions allowing the provision of additional message text.
///
/// @author evanbergstrom
/// @since 1.o
public final class AssertActions {

    private AssertActions() { }

    /// Creates a consumer that checks that it only consumes objects from a specified set of objects.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @return a consumer that expects a specific set of objects.
    /// @see AssertConsumeOnly
    public static <T> AssertConsumeOnly<T> consumeOnly(final Iterable<T> expected) {
        return new AssertConsumeOnly<>(expected);
    }

    /// Creates a consumer that checks that it only consumes objects from a specified set of objects. This method
    /// allows specification of a string to be included in the exception of the assertion fails.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @param message the text to include in the exception.
    /// @return a consumer that expects a specific set of objects.
    public static <T> AssertConsumeOnly<T> consumeOnly(final Iterable<T> expected,
                                                             final @Nullable String message) {
        return new AssertConsumeOnly<>(expected, message);
    }

    /// Creates a consumer that checks that it only consumes objects from a specified set of objects. This method
    /// allows specification of a supplier of a message to be included in the exception.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @param messageSupplier the supplier of the text to include in the exception.
    /// @return a consumer that expects a specific set of objects.
    public static <T> AssertConsumeOnly<T> consumeOnly(final Iterable<T> expected,
                                                             final @Nullable Supplier<String> messageSupplier) {
        return new AssertConsumeOnly<>(expected, messageSupplier);
    }

    /// A consumer that checks that is consumes a specified set of objects.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @return a consumer that expects a specific set of objects.
    /// @see AssertConsumeExactly
    public static <T> AssertConsumeExactly<T> consumeExactly(final Iterable<T> expected) {
        return new AssertConsumeExactly<>(expected);
    }

    /// A consumer that checks that is consumes a specified set of objects. This method allows specification of
    /// a string to be included in the exception of the assertion fails.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @param message the text to include in the exception.
    /// @return a consumer that expects a specific set of objects.
    /// @see AssertConsumeExactly
    public static <T> AssertConsumeExactly<T> consumeExactly(final Iterable<T> expected,
                                                       final @Nullable String message) {
        return new AssertConsumeExactly<>(expected, message);
    }

    /// A consumer that checks that is consumes a specified set of objects. This method allows specification of
    /// a supplier of a message to be included in the exception.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @param messageSupplier the supplier of the text to include in the exception.
    /// @return a consumer that expects a specific set of objects.
    /// @see AssertConsumeExactly
    public static <T> AssertConsumeExactly<T> consumeExactly(final Iterable<T> expected,
                                                       final @Nullable Supplier<String> messageSupplier) {
        return new AssertConsumeExactly<>(expected, messageSupplier);
    }

    /// Create a consumer that expect to consume a specified number of objects.
    /// @param <T> the type of object being consumed.
    /// @param expected the number of objects to expect.
    /// @return a consumer that expects the specified number of objects.
    /// @see AssertConsumeCount
    public static <T> AssertConsumeCount<T> consumeCount(final int expected) {
        return new AssertConsumeCount<>(expected);
    }

    /// Create a consumer that expect to consume a specified number of objects. This method allows specification of
    /// a string to be included in the exception of the assertion fails.
    /// @param <T> the type of object being consumed.
    /// @param expected the number of objects to expect.
    /// @param message the text to include in the exception.
    /// @return a consumer that expects the specified number of objects.
    /// @see AssertConsumeCount
    public static <T> AssertConsumeCount<T> consumeCount(final int expected,
                                                     final @Nullable String message) {
        return new AssertConsumeCount<>(expected, message);
    }

    /// Create a consumer that expect to consume a specified number of objects. This method allows specification of
    /// a supplier of a message to be included in the exception.
    /// @param <T> the type of object being consumed.
    /// @param expected the number of objects to expect.
    /// @param messageSupplier the supplier of the text to include in the exception.
    /// @return a consumer that expects the specified number of objects.
    /// @see AssertConsumeCount
    public static <T> AssertConsumeCount<T> consumeCount(final int expected,
                                                     final @Nullable Supplier<String> messageSupplier) {
        return new AssertConsumeCount<>(expected, messageSupplier);
    }

    /// Create a consumer that expect to consume no objects.
    /// @param <T> the type of object being consumed.
    /// @return a consumer that expects no objects.
    /// @see AssertConsumeNone
    public static <T> AssertConsumeNone<T> consumeNone() {
        return new AssertConsumeNone<>();
    }

    /// Create a consumer that expect to consume no objects. This method allows specification of a string to be
    /// included in the exception of the assertion fails.
    /// @param <T> the type of object being consumed.
    /// @param message the text to include in the exception.
    /// @return a consumer that expects no objects.
    /// @see AssertConsumeNone
    public static <T> AssertConsumeNone<T> consumeNone(final @Nullable String message) {
        return new AssertConsumeNone<>(message);
    }

    /// Create a consumer that expect to consume no objects. This method allows specification of a supplier of a
    /// message to be included in the exception.
    /// @param <T> the type of object being consumed.
    /// @param messageSupplier the supplier of the text to include in the exception.
    /// @return a consumer that expects no objects.
    /// @see AssertConsumeNone
    public static <T> AssertConsumeNone<T> consumeNone(final @Nullable Supplier<String> messageSupplier) {
        return new AssertConsumeNone<>(messageSupplier);
    }
}
