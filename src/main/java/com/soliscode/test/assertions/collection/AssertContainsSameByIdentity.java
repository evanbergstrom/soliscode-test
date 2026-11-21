/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.assertions.collection;

import java.util.List;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that two collections contain the same elements by identity.
///
/// @author evanbergstromÒ
/// @since 1.0.0
public final class AssertContainsSameByIdentity {

    private AssertContainsSameByIdentity() {
    }

    /// Test if two iterables contain the same set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e* they are the same object).
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSameByIdentity(final Iterable<?> expected, final Iterable<?> actual) {
        checkContainsSameByIdentity(expected, actual, null);
    }

    /// Test if two iterables contain the same set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e* they are the same object).
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSameByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final @Nullable String message) {
        checkContainsSameByIdentity(expected, actual, message);
    }

    /// Test if two iterables contain the same set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e* they are the same object).
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSameByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final @Nullable Supplier<String> messageSupplier) {
        checkContainsSameByIdentity(expected, actual, messageSupplier);
    }

    private static void checkContainsSameByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final @Nullable Object messageOrSupplier) {

        List<?> actualList = IterableTestUtils.asList(actual);
        List<?> expectedList = IterableTestUtils.asList(expected);

        if (actualList.size() != expectedList.size()) {
            throw buildException(expected, actual, messageOrSupplier);
        }
        while (!actualList.isEmpty()) {
            Object o = actualList.getFirst();
            if (!IterableTestUtils.containsByIdentity(expectedList, o)) {
                throw buildException(expected, actual, messageOrSupplier);
            }
            actualList.remove(o);
            expectedList.remove(o);
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
