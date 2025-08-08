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

package org.soliscode.test.assertions.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/// Utility class for creating assertion actions to provide to methods that are being tested. Three versions of each
/// method are provided, with two versions allowing the provision of additional message text.
///
/// @author evanbergstrom
/// @since 1.o
public final class AssertActions {

    private AssertActions() {}

    /// Creates a consumer that checks that it only consumes objects from a specified set of objects.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @return a consumer that expects a specific set of objects.
    /// @see AssertConsumeOnly
    public static <T> @NotNull AssertConsumer<T> consumeOnly(final @NotNull Iterable<T> expected) {
        return new AssertConsumeOnly<>(expected);
    }

    /// Creates a consumer that checks that it only consumes objects from a specified set of objects. This method
    /// allows specification of a string to be included in the exception of the assertion fails.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @param message the text to include in the exception.
    /// @return a consumer that expects a specific set of objects.
    public static <T> @NotNull AssertConsumer<T> consumeOnly(final @NotNull Iterable<T> expected,
                                                             final @Nullable String message) {
        return new AssertConsumeOnly<>(expected, message);
    }

    /// Creates a consumer that checks that it only consumes objects from a specified set of objects. This method
    /// allows specification of a supplier of a message to be included in the exception.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @param messageSupplier the supplier of the text to include in the exception.
    /// @return a consumer that expects a specific set of objects.
    public static <T> @NotNull AssertConsumer<T> consumeOnly(final @NotNull Iterable<T> expected,
                                                             final @Nullable Supplier<String> messageSupplier) {
        return new AssertConsumeOnly<>(expected, messageSupplier);
    }

    /// A consumer that checks that is consumes a specified set of objects.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @return a consumer that expects a specific set of objects.
    /// @see AssertConsumeExactly
    public static <T> AssertConsumer<T> consumeExactly(final @NotNull Iterable<T> expected) {
        return new AssertConsumeExactly<>(expected);
    }

    /// A consumer that checks that is consumes a specified set of objects. This method allows specification of
    /// a string to be included in the exception of the assertion fails.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @param message the text to include in the exception.
    /// @return a consumer that expects a specific set of objects.
    /// @see AssertConsumeExactly
    public static <T> AssertConsumer<T> consumeExactly(final @NotNull Iterable<T> expected,
                                                       final @Nullable String message) {
        return new AssertConsumeExactly<>(expected, message);
    }

    /// A consumer that checks that is consumes a specified set of objects. This method allows specification of
    /// a supplier of a message to be included in the exception.
    /// @param <T> the type of object being consumed.
    /// @param expected the set of objects that should be consumed.
    /// @param messageSupplier the supplier of the text to include in the exception.
    /// @return a consumer that expects a specific set of objects.
    /// @see AssertConsumeExactly
    public static <T> AssertConsumer<T> consumeExactly(final @NotNull Iterable<T> expected,
                                                       final @Nullable Supplier<String> messageSupplier) {
        return new AssertConsumeExactly<>(expected, messageSupplier);
    }

    /// Create a consumer that expect to consume a specified number of objects.
    /// @param <T> the type of object being consumed.
    /// @param expected the number of objects to expect.
    /// @return a consumer that expects the specified number of objects.
    /// @see AssertConsumeCount
    public static <T> AssertConsumer<T> consumeCount(final int expected) {
        return new AssertConsumeCount<>(expected);
    }

    /// Create a consumer that expect to consume a specified number of objects. This method allows specification of
    /// a string to be included in the exception of the assertion fails.
    /// @param <T> the type of object being consumed.
    /// @param expected the number of objects to expect.
    /// @param message the text to include in the exception.
    /// @return a consumer that expects the specified number of objects.
    /// @see AssertConsumeCount
    public static <T> AssertConsumer<T> consumeCount(final int expected,
                                                     final @Nullable String message) {
        return new AssertConsumeCount<>(expected, message);
    }

    /// Create a consumer that expect to consume a specified number of objects. This method allows specification of
    /// a supplier of a message to be included in the exception.
    /// @param <T> the type of object being consumed.
    /// @param expected the number of objects to expect.
    /// @param messageSupplier the supplier of the text to include in the exception.
    /// @return a consumer that expects the specified number of objects.
    /// @see AssertConsumeCount
    public static <T> AssertConsumer<T> consumeCount(final int expected,
                                                     final @Nullable Supplier<String> messageSupplier) {
        return new AssertConsumeCount<>(expected, messageSupplier);
    }

    /// Create a consumer that expect to consume no objects.
    /// @param <T> the type of object being consumed.
    /// @return a consumer that expects no objects.
    /// @see AssertConsumeNone
    public static <T> AssertConsumer<T> assertConsumeNone() {
        return new AssertConsumeNone<>();
    }

    /// Create a consumer that expect to consume no objects. This method allows specification of a string to be
    /// included in the exception of the assertion fails.
    /// @param <T> the type of object being consumed.
    /// @param message the text to include in the exception.
    /// @return a consumer that expects no objects.
    /// @see AssertConsumeNone
    public static <T> AssertConsumer<T> assertConsumeNone(final @Nullable String message) {
        return new AssertConsumeNone<>(message);
    }

    /// Create a consumer that expect to consume no objects. This method allows specification of a supplier of a
    /// message to be included in the exception.
    /// @param <T> the type of object being consumed.
    /// @param messageSupplier the supplier of the text to include in the exception.
    /// @return a consumer that expects no objects.
    /// @see AssertConsumeNone
    public static <T> AssertConsumer<T> assertConsumeNone(final @Nullable Supplier<String> messageSupplier) {
        return new AssertConsumeNone<>(messageSupplier);
    }
}
