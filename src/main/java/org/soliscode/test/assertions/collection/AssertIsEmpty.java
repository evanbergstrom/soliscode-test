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

import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that a collection is empty.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class AssertIsEmpty {

    private AssertIsEmpty() {
    }

    /// Assert that the collection is empty.
    ///
    /// @param actual The iterable that should be empty.
    /// @throws AssertionFailedError if the iterable is not empty or the iterable is null.
    public static void assertIsEmpty(final Iterable<?> actual) {
        checkIsEmpty(actual, null);
    }

    /// Assert that the collection is empty.
    ///
    /// @param actual The iterable that should be empty.
    /// @param message The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable is not empty or the iterable is null.
    public static void assertIsEmpty(final Iterable<?> actual, final String message) {
        checkIsEmpty(actual, message);
    }

    /// Assert that the iterable is empty.
    ///
    /// @param actual The iterable that should be empty.
    /// @param messageSupplier The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable is not empty or the iterable is null.
    public static void assertIsEmpty(final Iterable<?> actual, final Supplier<String> messageSupplier) {
        checkIsEmpty(actual, messageSupplier);
    }

    private static void checkIsEmpty(final Iterable<?> actual, final Object messageOrSupplier) {
        if (!IterableTestOps.isEmpty(actual)) {
            throw buildException(actual, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Iterable<?> actual, final Object messageOrSupplier) {
        return assertionFailure()
            .message(messageOrSupplier)
            .expected(IterableTestOps.empty())
            .actual(actual)
            .build();
    }
}
