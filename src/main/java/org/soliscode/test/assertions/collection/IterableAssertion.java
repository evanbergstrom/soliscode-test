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

import org.junit.jupiter.api.Assertions;
import org.soliscode.test.interfaces.IterableOnly;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A utility class for assertion classes that assert conditions on iterables.
/// This class provides common functionality for validating iterables in test assertions,
/// including null checking, error handling, and type conversions.
///
/// This is a utility class with only static methods and should not be instantiated
/// directly. It serves as a base class for other assertion utilities that work with
/// iterable collections.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see IterableOnly
public class IterableAssertion {

    /// Protected constructor to prevent direct instantiation.
    /// This class is intended to be used as a utility class with static methods,
    /// or as a base class for other assertion utilities.
    protected IterableAssertion() {
    }

    /// Asserts that the actual iterable parameter is not null.
    /// This method validates that the iterable being tested is not null, throwing
    /// an assertion failure with appropriate messaging if it is null.
    ///
    /// @param actual the iterable to check for null
    /// @param messageOrSupplier the message or message supplier to include in the assertion failure
    /// @throws org.opentest4j.AssertionFailedError if actual is null
    /// @see #failIterableIsNull(String, Object)
    protected static void assertIterablesNotNull(final Object actual, final Object messageOrSupplier) {

        if (actual == null) {
            failIterableIsNull("actual", messageOrSupplier);
        }
    }

    /// Asserts that both the expected and actual iterable parameters are not null.
    /// This method validates that both iterables being compared are not null, throwing
    /// an assertion failure with appropriate messaging if either is null.
    ///
    /// @param expected the expected iterable to check for null
    /// @param actual the actual iterable to check for null
    /// @param messageOrSupplier the message or message supplier to include in the assertion failure
    /// @throws org.opentest4j.AssertionFailedError if either expected or actual is null
    /// @see #failIterableIsNull(String, Object)
    protected static void assertIterablesNotNull(final Object expected, final Object actual,
        final Object messageOrSupplier) {

        if (expected == null) {
            failIterableIsNull("expected", messageOrSupplier);
        }
        if (actual == null) {
            failIterableIsNull("actual", messageOrSupplier);
        }
    }

    /// Throws an assertion failure indicating that an iterable parameter was null.
    /// This method constructs and throws an AssertionFailedError with a descriptive
    /// message indicating which parameter (expected or actual) was null.
    ///
    /// @param name the name of the parameter that was null (e.g., "expected" or "actual")
    /// @param messageOrSupplier the message or message supplier to include in the assertion failure
    /// @throws org.opentest4j.AssertionFailedError always - this method never returns normally
    /// @see org.junit.jupiter.api.AssertionFailureBuilder
    protected static void failIterableIsNull(final String name, final Object messageOrSupplier) {
        assertionFailure()
            .message(messageOrSupplier)
            .reason(name + " iterable was <null>")
            .buildAndThrow();
    }

    /// Converts an array of elements to an Iterable for use in assertions.
    /// This method wraps the array elements in an IterableOnly instance, which restricts
    /// access to only the Iterable interface methods. This is useful for testing code
    /// that should only depend on the Iterable interface.
    ///
    /// @param <E> the type of elements in the array
    /// @param elements the array of elements to convert to an iterable
    /// @return an Iterable containing the elements from the array
    /// @throws org.opentest4j.AssertionFailedError if elements is null
    /// @see IterableOnly
    /// @see Arrays#stream(Object[])
    protected static <E> Iterable<E> asIterable(final E[] elements) {
        Assertions.assertNotNull(elements);
        return new IterableOnly<>(Arrays.stream(elements).collect(Collectors.toList()));
    }
}
