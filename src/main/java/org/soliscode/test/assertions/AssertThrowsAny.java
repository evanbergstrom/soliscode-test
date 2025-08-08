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

package org.soliscode.test.assertions;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.function.Executable;

import java.util.Collection;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that an executable throws one of a list of exception types.
///
/// @author evanbergstrom
/// @since 1.0
public final class AssertThrowsAny {

    private AssertThrowsAny() {}

    /// Asserts that the executable will throw one of a list of possible exception types.
    /// @param expectedTypes the exception types that the executable should throw.
    /// @param executable the executable to test.
    public static void assertThrowsAny(final @NotNull Collection<Class<? extends Throwable>> expectedTypes,
                                       final @NotNull Executable executable) {
        checkThrowsAny(expectedTypes, executable, null);
    }

    /// Asserts that the executable will throw one of a list of possible exception types.
    /// @param expectedTypes the exception types that the executable should throw.
    /// @param executable the executable to test.
    /// @param message the message to include in the exception if the assertions fails.
    public static void assertThrowsAny(final @NotNull Collection<Class<? extends Throwable>> expectedTypes,
                                       final @NotNull Executable executable, final String message) {
        checkThrowsAny(expectedTypes, executable, message);
    }

    /// Asserts that the executable will throw one of a list of possible exception types.
    /// @param expectedTypes the exception types that the executable should throw.
    /// @param executable the executable to test.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertions fails.
    public static void assertThrowsAny(final @NotNull Collection<Class<? extends Throwable>> expectedTypes,
                                       final @NotNull Executable executable, final Supplier<String> messageSupplier) {
        checkThrowsAny(expectedTypes, executable, messageSupplier);
    }

    private static void checkThrowsAny(final @NotNull Collection<Class<? extends Throwable>> expectedTypes,
                                       final @NotNull Executable executable, final Object messageOrSupplier) {
        try {
            executable.execute();
        }
        catch (Throwable actualException) {
            for (Class<?> expectedType : expectedTypes) {
                if (expectedType.isInstance(actualException)) {
                    return;
                }
            }
            throw assertionFailure()
                    .message(messageOrSupplier)
                    .expected(expectedTypes)
                    .actual(actualException.getClass())
                    .build();
        }
        throw assertionFailure()
                .message(messageOrSupplier)
                .reason(format("Expected one of %s to be thrown, but nothing was thrown.",
                        expectedTypes.stream().map(Class::getCanonicalName).toList()))
                .build();
    }
}
