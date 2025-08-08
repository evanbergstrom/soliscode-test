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
import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that an executable throws an exception that is not one of a
/// specified set of prohibited exception types.
///
/// @author evanbergstrom
/// @since 1.0
public class AssertThrowsDifferent {

    private AssertThrowsDifferent() {}

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// @param prohibitedTypes the exception types that should not be thrown.
    /// @param executable the executable to test.
    public static void assertThrowsDifferent(final @NotNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NotNull Executable executable) {
        checkThrowsDifferent(prohibitedTypes, executable, null);
    }

    /// Asserts that the executable will throw an exception that is the prohibited exception type.
    /// @param prohibitedType the exception type that should not be thrown.
    /// @param executable the executable to test.
    public static void assertThrowsDifferent(final @NotNull Class<? extends Throwable> prohibitedType,
                                             final @NotNull Executable executable) {
        checkThrowsDifferent(List.of(prohibitedType), executable, null);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// @param prohibitedTypes the exception types that the executable should throw.
    /// @param executable the executable to test.
    /// @param message the message to include in the exception if the assertions fails.
    public static void assertThrowsDifferent(final @NotNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                       final @NotNull Executable executable, final String message) {
        checkThrowsDifferent(prohibitedTypes, executable, message);
    }

    /// Asserts that the executable will throw an exception that is the prohibited exception type.
    /// @param prohibitedType the exception type that should not be thrown.
    /// @param executable the executable to test.
    /// @param message the message to include in the exception if the assertions fails.
    public static void assertThrowsDifferent(final @NotNull Class<? extends Throwable> prohibitedType,
                                             final @NotNull Executable executable, final String message) {
        checkThrowsDifferent(List.of(prohibitedType), executable, message);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// @param prohibitedTypes the exception types that should not be thrown.
    /// @param executable the executable to test.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertions fails.
    public static void assertThrowsDifferent(final @NotNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NotNull Executable executable,
                                             final Supplier<String> messageSupplier) {
        checkThrowsDifferent(prohibitedTypes, executable, messageSupplier);
    }

    /// Asserts that the executable will throw an exception that is the prohibited exception type.
    /// @param prohibitedType the exception type that should not be thrown.
    /// @param executable the executable to test.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertions fails.
    public static void assertThrowsDifferent(final @NotNull Class<? extends Throwable> prohibitedType,
                                             final @NotNull Executable executable,
                                             final Supplier<String> messageSupplier) {
        checkThrowsDifferent(List.of(prohibitedType), executable, messageSupplier);
    }

    private static void checkThrowsDifferent(final @NotNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NotNull Executable executable, final Object messageOrSupplier) {
        try {
            executable.execute();
        }
        catch (Throwable actualException) {
            for (Class<?> expectedType : prohibitedTypes) {
                if (expectedType.isInstance(actualException)) {
                    throw assertionFailure()
                            .message(messageOrSupplier)
                            .expected(prohibitedTypes)
                            .actual(actualException.getClass())
                            .build();
                }
            }
            return;
        }
        throw assertionFailure()
                .message(messageOrSupplier)
                .reason(format("Should have thrown an exception other than any of %s, but nothing was thrown.",
                        prohibitedTypes.stream().map(Class::getCanonicalName).toList()))
                .build();
    }
}
