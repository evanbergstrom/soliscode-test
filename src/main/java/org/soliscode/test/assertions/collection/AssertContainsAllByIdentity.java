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
import org.soliscode.test.util.IterableTestOps;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// `AssertContains` is a collection of utility methods that support asserting that a collection contains specific
/// elements.
///
/// @author evanbergstrom
/// @since 1.0.0
final class AssertContainsAllByIdentity extends IterableAssertion {

    private AssertContainsAllByIdentity() {
    }

    /// Test if an iterable contains all of a set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e.* they are the same object).
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsAllByIdentity(final Iterable<?> expected, final Iterable<?> actual) {
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
    public static void assertContainsAllByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final String message) {
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
    public static void assertContainsAllByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final Supplier<String> messageSupplier) {
        checkContainsAllByIdentity(expected, actual, messageSupplier);
    }

    private static void checkContainsAllByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final Object messageOrSupplier) {

        assertIterablesNotNull(expected, actual, messageOrSupplier);
        Set<?> s = IterableTestOps.asSet(actual);
        Collection<Object> missing = new ArrayList<>();
        for (Object e : expected) {
            if (!IterableTestOps.containsByIdentity(s, e)) {
                missing.add(e);
            }
        }
        if (!missing.isEmpty()) {
            throw buildException(expected, actual, missing, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Iterable<?> elements, final Iterable<?> actual,
            final Iterable<?> missing, final Object messageOrSupplier) {
        return assertionFailure()
            .message(messageOrSupplier)
            .expected(elements)
            .actual(actual)
            .reason("missing elements: " + StringUtils.nullSafeToString(missing))
            .build();
    }
}
