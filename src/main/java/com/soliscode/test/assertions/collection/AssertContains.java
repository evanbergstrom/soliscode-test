/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.assertions.collection;

import java.util.Collection;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// `AssertContain` is a collection of utility methods that support asserting that a collection contains a specific
///  element.
///
/// @author evanbergstrom
/// @see CollectionAssertions
/// @since 1.0.0
final class AssertContains {

    private AssertContains() {
    }

    /// Test if an iterable contains a specific element. The elements are considered the same if the `equals` method
    /// returns true.
    ///
    /// @param expected  The element it should contain
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not the element or if either of the iterable
    ///                              arguments is null.
    public static void assertContains(final Object expected, final Iterable<?> actual) {
        checkContains(expected, actual, null);
    }

    /// Test if an iterable contains a specific element. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The element it should contain
    /// @param actual    The iterable that should contain the element.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not the element or if either of the iterable
    ///                              arguments is null.
    public static void assertContains(final Object expected, final Iterable<?> actual,
                                      final @Nullable String message) {
        checkContains(expected, actual, message);
    }

    /// Test if an iterable contains a specific element. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The element it should contain
    /// @param actual    The iterable that should contain the element.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not the element or if either of the iterable
    ///                              arguments is null.
    public static void assertContains(final Object expected, final Iterable<?> actual,
                                      final @Nullable Supplier<String> messageSupplier) {
        checkContains(expected, actual, messageSupplier);
    }

    private static void checkContains(final Object expected, final Iterable<?> actual,
                                      final @Nullable Object messageOrSupplier) {

        if (actual instanceof Collection<?> collection) {
            if (!collection.contains(expected)) {
                throw buildException(expected, actual, messageOrSupplier);
            }
        } else {
            for (final Object e : actual) {
                if (e.equals(expected)) {
                    return;
                }
            }
            throw buildException(expected, actual, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Object expected,
                                                       final Iterable<?> actual,
                                                       final @Nullable Object messageOrSupplier) {
        return assertionFailure()
                .message(messageOrSupplier)
                .expected(expected)
                .actual(actual)
                .reason("missing element: " + StringUtils.nullSafeToString(expected))
                .build();
    }
}
