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

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A consumer that counts each time it consumes an object and asserts that it has consumed the expected number of
/// objects when done. Instead of instantiating this class directly, the [AssertActions] utility class should be used:
/// ```java
/// Consumer<Integer> consumer = AssertActions.consumeCount(3);
/// ```
/// This assertion action must be checked after the method it is passed to has completed, so the [#assertCheck()] method
/// will need to be explicitly called:
/// ```java
/// AssertConsumer<Integer> consumer = AssertActions.consumeCount(7);
/// collection.forEach(consumer);
/// consumer.assertCheck();
/// ```
/// @param <T>
/// @author evanbergstrom
/// @since 1.0
/// @see AssertActions
/// @see Consumer
public class AssertConsumeCount<T> implements AssertConsumer<T> {

    private final int expected;
    private int actual;
    private final @Nullable Object messageOrSupplier;

    /// Create a consumer that expect to consume a specified number of objects.
    /// @param expected the number of objects to expect.
    public AssertConsumeCount(final int expected) {
        this.expected = expected;
        this.messageOrSupplier = null;
    }

    /// Create a consumer that expect to consume a specified number of objects. This constructor allows specification of
    /// a string to be included in the exception of the assertion fails.
    /// @param expected the number of objects to expect.
    /// @param message the text to include in the exception.
    public AssertConsumeCount(final int expected, final @Nullable String message) {
        this.expected = expected;
        this.messageOrSupplier = message;
    }

    /// Create a consumer that expect to consume a specified number of objects. This constructor allows specification of
    /// a supplier of a message to be included in the exception.
    /// @param expected the number of objects to expect.
    /// @param messageSupplier the supplier of the text to include in the exception.
    public AssertConsumeCount(final int expected, final @Nullable Supplier<String> messageSupplier) {
        this.expected = expected;
        this.messageOrSupplier = messageSupplier;
    }

    /// Increments the count of the number of times the consumer has been used.
    /// @param obj the object that is being consumed.
    @Override
    public void accept(@Nullable T obj) {
        actual++;
    }

    /// Asserts that the consumer has been called the expected number of times.
    /// @throws org.opentest4j.AssertionFailedError if the consumer has not been called the expected number of times.
    @Override
    public void assertCheck() {
        if (actual != expected) {
            throw assertionFailure()
                    .message(messageOrSupplier)
                    .expected(expected)
                    .actual(actual)
                    .build();
        }
    }
}
