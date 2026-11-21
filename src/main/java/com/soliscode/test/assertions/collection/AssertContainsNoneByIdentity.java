/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.assertions.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.util.CollectionTestUtils;
import com.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that a collection does not contain specific elements by
/// identity.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class AssertContainsNoneByIdentity {

    private AssertContainsNoneByIdentity() {
    }

    /// Test if an iterable contains no element from a set of elements. Two elements are only considered the same is
    /// they have the same identity (*i.e* they are the same object).
    ///
    /// @param excluded  The elements the iterable must not contain.
    /// @param actual    The iterable that should not contain the elements.
    /// @throws AssertionFailedError if the iterable contains any of the elements or if either of the iterable
    ///    arguments is null.
    public static void assertContainsNoneByIdentity(final Iterable<?> excluded,
                                                    final Iterable<?> actual) {
        checkContainsNoneByIdentity(excluded, actual, null);
    }

    /// Test if an iterable contains no element from a set of elements. Two elements are only considered the same is
    /// they have the same identity (*i.e* they are the same object).
    ///
    /// @param excluded  The elements the iterable must not contain.
    /// @param actual    The iterable that should not contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsNoneByIdentity(final Iterable<?> excluded,
                                                    final Iterable<?> actual,
                                                    final @Nullable String message) {
        checkContainsNoneByIdentity(excluded, actual, message);
    }

    /// Test if an iterable contains no element from a set of elements. Two elements are only considered the same is
    /// they have the same identity (*i.e* they are the same object).
    ///
    /// @param excluded          The elements the iterable must not contain.
    /// @param actual            The iterable that should not contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsNoneByIdentity(final Iterable<?> excluded,
                                                    final Iterable<?> actual,
                                                    final @Nullable Supplier<String> messageSupplier) {
        checkContainsNoneByIdentity(excluded, actual, messageSupplier);
    }

    private static void checkContainsNoneByIdentity(final Iterable<?> excluded,
                                                    final Iterable<?> actual,
                                                    final @Nullable Object messageOrSupplier) {

        Collection<Object> found = new ArrayList<>();
        for (final Object e : excluded) {
            if (IterableTestUtils.containsByIdentity(actual, e)) {
                found.add(e);
            }
        }
        if (!found.isEmpty()) {
            throw buildException(actual, found, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Iterable<?> actual,
                                                       final Collection<?> found,
                                                       final @Nullable Object messageOrSupplier) {
        Collection<Object> expected = new ArrayList<>();
        actual.forEach(expected::add);
        CollectionTestUtils.removeAllByIdentity(expected, found);

        return assertionFailure()
            .message(messageOrSupplier)
            .expected(expected)
            .actual(actual)
            .reason("should not contain elements: " + StringUtils.nullSafeToString(found))
            .build();
    }
}
