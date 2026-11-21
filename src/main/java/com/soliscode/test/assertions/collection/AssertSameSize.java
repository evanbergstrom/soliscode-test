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

/// A collection of utility methods that support asserting that two collections are the same size.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class AssertSameSize {

    private AssertSameSize() {
    }

    /**
     * <em>Assert</em> that the collections are the same size.
     * @param expected the collection with the expected size
     * @param actual the collection with the actual size
     * @throws AssertionFailedError if the collection is not empty.
     * @throws NullPointerException is either of the collections are null.
     */
    public static void assertSameSize(final Iterable<?> expected, final Iterable<?> actual) {
        checkSameSize(expected, actual, null);
    }

    /**
     * <em>Assert</em> that the collections are the same size.
     * @param expected the collection with the expected size
     * @param actual the collection with the actual size
     * @param message The message to supply if the assertion fails.
     * @throws AssertionFailedError if the collection is not empty.
     * @throws NullPointerException is either of the collections are null.
     */
    public static void assertSameSize(final Iterable<?> expected, final Iterable<?> actual,
                                      final @Nullable String message) {
        checkSameSize(expected, actual, message);
    }

    /**
     * <em>Assert</em> that the collections are the same size.
     * @param expected the collection with the expected size
     * @param actual the collection with the actual size
     * @param messageSupplier The supplier to call top generate  message to supply if the assertion fails.
     * @throws AssertionFailedError if the collection is not empty.
     * @throws NullPointerException is either of the collections are null.
     */
    public static void assertSameSize(final Iterable<?> expected, final Iterable<?> actual,
            final @Nullable Supplier<String> messageSupplier) {
        checkSameSize(expected, actual, messageSupplier);
    }

    static void checkSameSize(final Iterable<?> expected, final Iterable<?> actual,
            final @Nullable Object messageOrSupplier) {
        if (IterableTestUtils.size(expected) != IterableTestUtils.size(actual)) {
            throw buildException(expected, actual, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Iterable<?> expected, final Iterable<?> actual,
            final @Nullable Object messageOrSupplier) {
        return assertionFailure()
            .message(messageOrSupplier)
            .expected(expected)
            .actual(actual)
            .build();
    }
}
