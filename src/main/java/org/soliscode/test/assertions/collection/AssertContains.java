/*
 * Copyright 2024 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.soliscode.test.assertions.collection;

import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

import java.util.Collection;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// `AssertContain` is a collection of utility methods that support asserting that a collection contains a specific
///  element.
///
/// @author evanbergstrom
/// @see CollectionAssertions
/// @since 1.0.0
final class AssertContains extends IterableAssertion {

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
    public static void assertContains(final Object expected, final Iterable<?> actual, final String message) {
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
            final Supplier<String> messageSupplier) {
        checkContains(expected, actual, messageSupplier);
    }

    private static void checkContains(final Object expected, final Iterable<?> actual,
            final Object messageOrSupplier) {

        assertIterablesNotNull(expected, actual, messageOrSupplier);
        if (actual instanceof Collection<?> collection) {
            if (!collection.contains(expected)) {
                throw buildException(expected, actual, messageOrSupplier);
            }
        } else {
            for (Object e : actual) {
                if (e.equals(expected)) {
                    return;
                }
            }
            throw buildException(expected, actual, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Object expected, final Iterable<?> actual,
            final Object messageOrSupplier) {
        return assertionFailure()
                .message(messageOrSupplier)
                .expected(expected)
                .actual(actual)
                .reason("missing element: " + StringUtils.nullSafeToString(expected))
                .build();
    }
}
