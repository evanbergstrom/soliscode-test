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

import org.assertj.core.util.Lists;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.util.IterableTestOps;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// `AssertContainsNone` is a collection of utility methods that support asserting that a collection does not
/// contain specific elements.
///
/// @author evanbergstrom
/// @since 1.0.0
final class AssertContainsNone extends IterableAssertion {

    private AssertContainsNone() {
    }

    /// Test if an iterable contains no element from a set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param excluded  The elements the iterable must not contain.
    /// @param actual    The iterable that should not contain the elements.
    /// @throws AssertionFailedError if the iterable contains any of the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsNone(final Iterable<?> excluded, final Iterable<?> actual) {
        checkContainsNone(excluded, actual, null);
    }


    /// Test if an iterable contains no element from a set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param excluded  The elements the iterable must not contain.
    /// @param actual    The iterable that should not contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable contains any of the elements or if either of the iterable
    ///                              arguments is null.
    ///
    public static void assertContainsNone(final Iterable<?> excluded, final Iterable<?> actual, final String message) {
        checkContainsNone(excluded, actual, message);
    }

    /// Test if an iterable contains no element from a set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param excluded  The elements the iterable must not contain.
    /// @param actual    The iterable that should not contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable contains any of the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsNone(final Iterable<?> excluded, final Iterable<?> actual,
            final Supplier<String> messageSupplier) {
        checkContainsNone(excluded, actual, messageSupplier);
    }

    private static void checkContainsNone(final Iterable<?> excluded, final Iterable<?> actual,
            final Object messageOrSupplier) {

        assertIterablesNotNull(excluded, actual, messageOrSupplier);
        Set<?> s = IterableTestOps.asSet(actual);
        Collection<Object> found = new ArrayList<>();
        for (Object e : excluded) {
            if (s.contains(e)) {
                found.add(e);
            }
        }
        if (!found.isEmpty()) {
            throw buildException(excluded, actual, found, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Iterable<?> excluded, final Iterable<?> actual,
            final Collection<?> found, final Object messageOrSupplier) {
        Collection<?> expected = Lists.newArrayList(actual);
        expected.removeAll(found);

        return assertionFailure()
            .message(messageOrSupplier)
            .expected(expected)
            .actual(actual)
            .reason("should not contain elements: " + StringUtils.nullSafeToString(found))
            .build();
    }
}
