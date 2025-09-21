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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.util.IterableTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.soliscode.test.util.IterableTestUtils.asIterable;

/// **AssertContainsAll** - Utility class for asserting that collections contain all expected elements.
///
/// This final utility class provides static methods for verifying that an [Iterable] or array
/// contains all elements from another collection. Elements are compared using their [#equals(Object)]
/// method for equality testing.
/// ## Core Features
/// - **Flexible Input Types**: Supports any combination of [Iterable] and array types
/// - **Null-Safe Operations**: Properly handles null validation with descriptive error messages
/// - **Comprehensive Messaging**: Provides detailed failure messages showing missing elements
/// - **Multiple Overloads**: Supports custom messages via [String] or [java.util.function.Supplier]
/// ## Usage Examples
/// ```java
/// // Basic usage with collections
/// List<Integer> expected = Arrays.asList(1, 2);
/// List<Integer> actual = Arrays.asList(1, 2, 3, 4);
/// assertContainsAll(expected, actual); // Passes - actual contains all of expected
/// // Using arrays
/// Integer[] expectedArray = {1, 2};
/// assertContainsAll(expectedArray, actual); // Passes
/// // With custom message
/// assertContainsAll(expected, actual, "Custom failure message");
/// // With message supplier (lazy evaluation)
/// assertContainsAll(expected, actual, () -> "Generated message");
/// ```
/// ## Assertion Behavior
/// - **Success**: When the actual collection contains ALL elements from the expected collection
/// - **Failure**: When one or more expected elements are missing from the actual collection
/// - **Null Handling**: Throws [AssertionFailedError] if either collection is null
/// - **Empty Collections**: Empty expected collection always passes (vacuous truth)
/// ## Implementation Notes
/// The class converts the actual collection to a [java.util.Set] for efficient `O(1)`
/// membership testing, then iterates through expected elements to identify any missing items.
/// This approach provides optimal performance for large collections.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see org.opentest4j.AssertionFailedError
final class AssertContainsAll {

    private AssertContainsAll() {
    }

    /// Tests if an [Iterable] contains all elements from another [Iterable].
    ///
    /// Elements are compared using their [#equals(Object)] method. The assertion
    /// **passes** when the actual iterable contains every element from the expected iterable.
    ///
    /// @param expected the elements that must be present in the actual iterable
    /// @param actual   the iterable to test for containing all expected elements
    /// @throws AssertionFailedError if the actual iterable does not contain all expected elements,
    ///                              or if either parameter is `null`
    public static void assertContainsAll(final @NonNull Iterable<?> expected, final @NonNull Iterable<?> actual) {
        checkContainsAll(expected, actual, null);
    }

    /// Tests if an [Iterable] contains all elements from an array.
    ///
    /// Elements are compared using their [#equals(Object)] method. The assertion
    /// **passes** when the actual iterable contains every element from the expected array.
    ///
    /// @param <E>      the type of elements in the array
    /// @param expected the array of elements that must be present in the actual iterable
    /// @param actual   the iterable to test for containing all expected elements
    /// @throws AssertionFailedError if the actual iterable does not contain all expected elements,
    ///                              or if either parameter is `null`
    public static <E> void assertContainsAll(final @NonNull E[] expected, final @NonNull Iterable<?> actual) {
        checkContainsAll(asIterable(expected), actual, null);
    }

    /// Tests if an array contains all elements from an [Iterable].
    ///
    /// Elements are compared using their [#equals(Object)] method. The assertion
    /// **passes** when the actual array contains every element from the expected iterable.
    ///
    /// @param <E>      the type of elements in the array
    /// @param expected the iterable of elements that must be present in the actual array
    /// @param actual   the array to test for containing all expected elements
    /// @throws AssertionFailedError if the actual array does not contain all expected elements,
    ///                              or if either parameter is `null`
    public static <E> void assertContainsAll(final @NonNull Iterable<?> expected, final @NonNull E[] actual) {
        checkContainsAll(expected, asIterable(actual), null);
    }

    /// Tests if an [Iterable] contains all elements from another [Iterable] with a custom message.
    ///
    /// Elements are compared using their [#equals(Object)] method. The assertion
    /// **passes** when the actual iterable contains every element from the expected iterable.
    ///
    /// @param expected the elements that must be present in the actual iterable
    /// @param actual   the iterable to test for containing all expected elements
    /// @param message  the custom message to include in the failure exception
    /// @throws AssertionFailedError if the actual iterable does not contain all expected elements,
    ///                              or if either parameter is `null`
    public static void assertContainsAll(final @NonNull Iterable<?> expected, final @NonNull Iterable<?> actual,
                                         final @Nullable String message) {
        checkContainsAll(expected, actual, message);
    }

    /// Tests if an [Iterable] contains all elements from from an array with a custom message.
    ///
    /// Elements are compared using their [#equals(Object)] method. The assertion
    /// **passes** when the actual iterable contains every element from the expected iterable.
    ///
    /// @param <E>      the type of elements in the array
    /// @param expected the array of elements that must be present in the actual iterable
    /// @param actual   the iterable to test for containing all expected elements
    /// @param message  the custom message to include in the failure exception
    /// @throws AssertionFailedError if the actual iterable does not contain all expected elements,
    ///                              or if either parameter is `null`
    public static <E> void assertContainsAll(final @NonNull E[] expected, final @NonNull Iterable<?> actual,
                                             final @Nullable String message) {
        checkContainsAll(asIterable(expected), actual, message);
    }

    /// Tests if an array contains all elements from another [Iterable] with a custom message.
    ///
    /// Elements are compared using their [#equals(Object)] method. The assertion
    /// **passes** when the actual array contains every element from the expected iterable.
    ///
    /// @param <E>      the type of elements in the array
    /// @param expected the elements that must be present in the actual iterable
    /// @param actual   the array to test for containing all expected elements
    /// @param message  the custom message to include in the failure exception
    /// @throws AssertionFailedError if the actual iterable does not contain all expected elements,
    ///                              or if either parameter is `null`
    public static <E> void assertContainsAll(final @NonNull Iterable<?> expected, final @NonNull E[] actual,
                                             final @Nullable String message) {
        checkContainsAll(expected, asIterable(actual), message);
    }

    /// Tests if an [Iterable] contains all elements from another [Iterable] with a message supplier.
    ///
    /// Elements are compared using their [#equals(Object)] method. The assertion
    /// **passes** when the actual iterable contains every element from the expected iterable.
    /// The message supplier is only called if the assertion fails, enabling lazy message generation.
    ///
    /// @param expected        the elements that must be present in the actual iterable
    /// @param actual          the iterable to test for containing all expected elements
    /// @param messageSupplier the supplier to generate a custom failure message
    /// @throws AssertionFailedError if the actual iterable does not contain all expected elements,
    ///                              or if either parameter is `null`
    public static void assertContainsAll(final @NonNull Iterable<?> expected, final @NonNull Iterable<?> actual,
                                         final @Nullable Supplier<String> messageSupplier) {
        checkContainsAll(expected, actual, messageSupplier);
    }

    /// Tests if an [Iterable] contains all elements from an array with a message supplier.
    ///
    /// Elements are compared using their [#equals(Object)] method. The assertion
    /// **passes** when the actual iterable contains every element from the expected array.
    /// The message supplier is only called if the assertion fails, enabling lazy message generation.
    ///
    /// @param <E>      the type of elements in the array
    /// @param expected        the array of elements that must be present in the actual iterable
    /// @param actual          the iterable to test for containing all expected elements
    /// @param messageSupplier the supplier to generate a custom failure message
    /// @throws AssertionFailedError if the actual array does not contain all expected elements,
    ///                              or if either parameter is `null`
    public static <E> void assertContainsAll(final @NonNull E[] expected, final @NonNull Iterable<?> actual,
                                             final @Nullable Supplier<String> messageSupplier) {
        checkContainsAll(asIterable(expected), actual, messageSupplier);
    }

    /// Tests if an array contains all elements from an [Iterable] with a message supplier.
    ///
    /// Elements are compared using their [#equals(Object)] method. The assertion
    /// **passes** when the actual array contains every element from the expected iterable.
    /// The message supplier is only called if the assertion fails, enabling lazy message generation.
    ///
    /// @param <E>      the type of elements in the array
    /// @param expected        the elements that must be present in the actual iterable
    /// @param actual          the array to test for containing all expected elements
    /// @param messageSupplier the supplier to generate a custom failure message
    /// @throws AssertionFailedError if the actual iterable does not contain all expected elements,
    ///                              or if either parameter is `null`
    public static <E> void assertContainsAll(final @NonNull Iterable<?> expected, final @NonNull E[] actual,
                                             final @Nullable Supplier<String> messageSupplier) {
        checkContainsAll(expected, asIterable(actual), messageSupplier);
    }

    private static void checkContainsAll(final @NonNull Iterable<?> expected, final @NonNull Iterable<?> actual,
            final Object messageOrSupplier) {

        Set<?> s = IterableTestUtils.asSet(actual);
        Collection<Object> missing = new ArrayList<>();
        for (Object e : expected) {
            if (!s.contains(e)) {
                missing.add(e);
            }
        }
        if (!missing.isEmpty()) {
            throw buildException(expected, actual, missing, messageOrSupplier);
        }
    }

    private static AssertionFailedError buildException(final @NonNull Iterable<?> expected,
                                                       final @NonNull Iterable<?> actual,
                                                       final @NonNull Iterable<?> missing,
                                                       final @Nullable Object messageOrSupplier) {
        return assertionFailure()
                .message(messageOrSupplier)
                .expected(expected)
                .actual(actual)
                .reason("missing elements: " + StringUtils.nullSafeToString(missing))
                .build();
    }
}
