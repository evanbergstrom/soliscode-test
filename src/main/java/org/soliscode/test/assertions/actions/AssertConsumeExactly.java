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
import org.soliscode.test.util.IterableTestOps;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A consumer that checks that is consumes a specified set of objects, consuming each of those objects once, and not
/// consuming any objects not in the set. This consumer requires that all of the objects in the set are consumed.
/// Instead of instantiating this class directly, the [AssertActions] utility class should be used:
/// ```java
/// Consumer<Integer> consumer = AssertActions.consumeExactly(Set.of(1, 3, 5, 7));
/// ```
/// This assertion action must be checked after the method it is passed to has completed, so the [#assertCheck()] method
/// will need to be explicitly called:
/// ```java
/// AssertConsumer<Integer> consumer = AssertActions.consumeExactly(Set.of(1, 3, 5, 7);
/// collection.forEach(consumer);
/// consumer.assertCheck();
/// ```
/// @param <T> element type
/// @author evanbergstrom
/// @since 1.0
/// @see AssertActions
/// @see Consumer
public class AssertConsumeExactly<T> implements AssertConsumer<T> {

    private final @NotNull Collection<T> expected;
    private final @Nullable Object messageOrSupplier;

    /// A consumer that checks that is consumes a specified set of objects.
    /// @param expected the set of objects that should be consumed.
    public AssertConsumeExactly(final @NotNull Iterable<T> expected) {
        this.expected = IterableTestOps.asCollection(expected);
        this.messageOrSupplier = null;
    }

    /// A consumer that checks that is consumes a specified set of objects. This constructor allows specification of
    /// a string to be included in the exception of the assertion fails.
    /// @param expected the set of objects that should be consumed.
    /// @param message the text to include in the exception.
    public AssertConsumeExactly(final @NotNull Iterable<T> expected, final @Nullable String message) {
        this.expected = IterableTestOps.asCollection(expected);
        this.messageOrSupplier = message;
    }

    /// A consumer that checks that is consumes a specified set of objects. This constructor allows specification of
    /// a supplier of a message to be included in the exception.
    /// @param expected the set of objects that should be consumed.
    /// @param messageSupplier the supplier of the text to include in the exception.
    public AssertConsumeExactly(final @NotNull Iterable<T> expected, final @Nullable Supplier<String> messageSupplier) {
        this.expected = IterableTestOps.asCollection(expected);
        this.messageOrSupplier = messageSupplier;
    }

    @Override
    public void accept(T e) {
        if (expected.contains(e)) {
            expected.remove(e);
        } else {
            throw assertionFailure()
                    .message(messageOrSupplier)
                    .expected(expected)
                    .actual(e)
                    .build();
        }
    }

    @Override
    public void assertCheck() {
        if (!expected.isEmpty()) {
            throw assertionFailure()
                    .message(messageOrSupplier)
                    .expected(expected)
                    .build();
        }
    }
}
