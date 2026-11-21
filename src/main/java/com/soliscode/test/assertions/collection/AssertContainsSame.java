/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.assertions.collection;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.util.IterableTestUtils;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that two collections contain the same elements.
/// The iterators must have the same number of elements, and for each element in the iterator being tested
/// (actual), there must be an element in the comparison iterator (expected) that returns true when the
/// equals()` method is called.
///
/// This function should be called through the [CollectionAssertions] class:
/// ```java
///    Iterable<Integer> actual = ...
///    Iterable<Integer> expected = ...
///    CollectionAssertions.assertContainsSame(expected, actual);
/// ```
///
/// @author evanbergstrom
/// @see CollectionAssertions#assertContainsSame
/// @since 1.0.0
public final class AssertContainsSame {

    private AssertContainsSame() {
    }

    /// Test if two iterables contain the same set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSame(final Iterable<?> expected, final Iterable<?> actual) {
        checkContainsSame(expected, actual, null);
    }

    /// Test if two iterables contain the same set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSame(final Iterable<?> expected, final Iterable<?> actual,
            final @Nullable String message) {
        checkContainsSame(expected, actual, message);
    }

    /// Test if two iterables contain the same set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSame(final Iterable<?> expected, final Iterable<?> actual,
            final @Nullable Supplier<String> messageSupplier) {
        checkContainsSame(expected, actual, messageSupplier);
    }

    private static void checkContainsSame(final Iterable<?> expected, final Iterable<?> actual,
            final @Nullable Object messageOrSupplier) {
        List<?> actualList = IterableTestUtils.asList(actual);
        Set<?> expectedSet = IterableTestUtils.asSet(expected);

        if (actualList.size() != expectedSet.size()) {
            throw buildException(expected, actual, messageOrSupplier);
        }
        actualList.forEach((o) -> {
            if (!expectedSet.remove(o)) {
                throw buildException(expected, actual, messageOrSupplier);
            }
        });
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
