/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.assertions.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// `AssertContains` is a collection of utility methods that support asserting that a collection contains specific
/// elements.
///
/// @author evanbergstrom
/// @since 1.0.0
final class AssertContainsAllByIdentity {

    private AssertContainsAllByIdentity() {
    }

    /// Test if an iterable contains all of a set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e.* they are the same object).
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsAllByIdentity(final Iterable<?> expected,
                                                   final Iterable<?> actual) {
        checkContainsAllByIdentity(expected, actual, null);
    }

    /// * Test if an iterable contains all of a set of elements. Two elements are only considered the same is they
    /// * have the same identity (*i.e* they are the same object).
    /// *
    /// * @param expected  The elements the iterable must contain.
    /// * @param actual    The iterable that should contain the elements.
    /// * @param message   The message to supply if the assertion fails.
    /// * @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    /// *                              arguments is null.
    public static void assertContainsAllByIdentity(final Iterable<?> expected,
                                                   final Iterable<?> actual,
                                                   final @Nullable String message) {
        checkContainsAllByIdentity(expected, actual, message);
    }

    /// Test if an iterable contains all of a set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e*> they are the same object).
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsAllByIdentity(final Iterable<?> expected,
                                                   final Iterable<?> actual,
                                                   final @Nullable Supplier<String> messageSupplier) {
        checkContainsAllByIdentity(expected, actual, messageSupplier);
    }

    private static void checkContainsAllByIdentity(final Iterable<?> expected,
                                                   final Iterable<?> actual,
                                                   final @Nullable Object messageOrSupplier) {

        Set<?> s = IterableTestUtils.asSet(actual);
        Collection<Object> missing = new ArrayList<>();
        for (final Object e : expected) {
            if (!IterableTestUtils.containsByIdentity(s, e)) {
                missing.add(e);
            }
        }
        if (!missing.isEmpty()) {
            throw buildException(expected, actual, missing, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Iterable<?> elements,
                                                       final Iterable<?> actual,
                                                       final Iterable<?> missing,
                                                       final @Nullable Object messageOrSupplier) {
        return assertionFailure()
            .message(messageOrSupplier)
            .expected(elements)
            .actual(actual)
            .reason("missing elements: " + StringUtils.nullSafeToString(missing))
            .build();
    }
}
