/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.assertions.collection;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that a collection is empty.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class AssertIsEmpty {

    private AssertIsEmpty() {
    }

    /// Assert that the collection is empty.
    ///
    /// @param actual The iterable that should be empty.
    /// @throws AssertionFailedError if the iterable is not empty or the iterable is null.
    public static void assertIsEmpty(final Iterable<?> actual) {
        checkIsEmpty(actual, null);
    }

    /// Assert that the collection is empty.
    ///
    /// @param actual The iterable that should be empty.
    /// @param message The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable is not empty or the iterable is null.
    public static void assertIsEmpty(final Iterable<?> actual, final @Nullable String message) {
        checkIsEmpty(actual, message);
    }

    /// Assert that the iterable is empty.
    ///
    /// @param actual The iterable that should be empty.
    /// @param messageSupplier The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable is not empty or the iterable is null.
    public static void assertIsEmpty(final Iterable<?> actual, final @Nullable Supplier<String> messageSupplier) {
        checkIsEmpty(actual, messageSupplier);
    }

    private static void checkIsEmpty(final Iterable<?> actual, final @Nullable Object messageOrSupplier) {
        if (!IterableTestUtils.isEmpty(actual)) {
            throw buildException(actual, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Iterable<?> actual,
            final @Nullable Object messageOrSupplier) {
        return assertionFailure()
            .message(messageOrSupplier)
            .expected(IterableTestUtils.empty())
            .actual(actual)
            .build();
    }
}
