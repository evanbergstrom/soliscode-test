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

import org.opentest4j.AssertionFailedError;
import org.soliscode.test.util.IterableTestOps;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that two collections contain the same elements by identity.
///
/// @author evanbergstromÒ
/// @since 1.0.0
public final class AssertContainsSameByIdentity extends IterableAssertion {

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
            final String message) {
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
            final Supplier<String> messageSupplier) {
        checkContainsSameByIdentity(expected, actual, messageSupplier);
    }

    private static void checkContainsSameByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final Object messageOrSupplier) {
        assertIterablesNotNull(expected, actual, messageOrSupplier);
        List<?> actualList = IterableTestOps.asList(actual);
        List<?> expectedList = IterableTestOps.asList(expected);

        if (actualList.size() != expectedList.size()) {
            throw buildException(expected, actual, messageOrSupplier);
        }
        while(!actualList.isEmpty() && !expectedList.isEmpty()) {
            Object o = actualList.getFirst();
            if (!IterableTestOps.containsByIdentity(expectedList, o)) {
                throw buildException(expected, actual, messageOrSupplier);
            }
            actualList.remove(o);
            expectedList.remove(o);
        }
        if (!expectedList.isEmpty() || !actualList.isEmpty()) {
            throw buildException(expected, actual, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Iterable<?> expected, final Iterable<?> actual,
            final Object messageOrSupplier) {
        return assertionFailure()
            .message(messageOrSupplier)
            .expected(expected)
            .actual(actual)
            .build();
    }
}
