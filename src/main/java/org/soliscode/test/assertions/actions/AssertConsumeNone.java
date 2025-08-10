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

/// A consumer that expects to consume no objects. It will throw an exception if any object is consumed, including 'null'.
///  Instead of instantiating this class directly, the [AssertActions] utility class should be used:
/// ```java
/// Consumer<Integer> consumer = AssertActions.consumeNone();
/// ```
/// @param <T> element type
/// @author evanbergstrom
/// @since 1.0
/// @see AssertActions
/// @see Consumer
public class AssertConsumeNone<T> implements AssertConsumer<T> {

    private final @Nullable Object messageOrSupplier;

    /// Create a consumer that expect to consume no objects.
    public AssertConsumeNone() {
        this.messageOrSupplier = null;
    }

    /// Create a consumer that expect to consume no objects. This constructor allows specification of a string to be
    /// included in the exception of the assertion fails.
    /// @param message the text to include in the exception.
    public AssertConsumeNone(final @Nullable String message) {
        this.messageOrSupplier = message;
    }

    /// Create a consumer that expect to consume no objects. This constructor allows specification of a supplier of a
    /// message to be included in the exception.
    /// @param messageSupplier the supplier of the text to include in the exception.
    public AssertConsumeNone(final @Nullable Supplier<String> messageSupplier) {
        this.messageOrSupplier = messageSupplier;
    }

    @Override
    public void accept(T e) {
        throw assertionFailure()
                .message(messageOrSupplier)
                .build();
    }
}
