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

/// A collection of utility methods that support asserting that two collections are the same size.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class AssertSameSize extends IterableAssertion {

    private AssertSameSize() {
    }

    /**
     * <em>Assert</em> that the collections are the same size.
     * @param expected the collection with the expected size
     * @param actual the collection with the actual size
     * @throws AssertionFailedError if the collection is not empty.
     * @throws NullPointerException is either of the collections are null.
     */
    public static void assertSameSize(final Iterable<?> expected, final Iterable<?> actual) {
        checkSameSize(expected, actual, null);
    }

    /**
     * <em>Assert</em> that the collections are the same size.
     * @param expected the collection with the expected size
     * @param actual the collection with the actual size
     * @param message The message to supply if the assertion fails.
     * @throws AssertionFailedError if the collection is not empty.
     * @throws NullPointerException is either of the collections are null.
     */
    public static void assertSameSize(final Iterable<?> expected, final Iterable<?> actual, final String message) {
        checkSameSize(expected, actual, message);
    }

    /**
     * <em>Assert</em> that the collections are the same size.
     * @param expected the collection with the expected size
     * @param actual the collection with the actual size
     * @param messageSupplier The supplier to call top generate  message to supply if the assertion fails.
     * @throws AssertionFailedError if the collection is not empty.
     * @throws NullPointerException is either of the collections are null.
     */
    public static void assertSameSize(final Iterable<?> expected, final Iterable<?> actual,
            final Supplier<String> messageSupplier) {
        checkSameSize(expected, actual, messageSupplier);
    }

    static void checkSameSize(final Iterable<?> expected, final Iterable<?> actual,
            final Object messageOrSupplier) {
        assertIterablesNotNull(expected, actual, messageOrSupplier);
        if (IterableTestOps.size(expected) != IterableTestOps.size(actual)) {
            throw buildException(expected, actual, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final Iterable<?> expected, final Iterable<?> actual,
            final  Object messageOrSupplier) {
        return assertionFailure()
            .message(messageOrSupplier)
            .expected(expected)
            .actual(actual)
            .build();
    }
}
