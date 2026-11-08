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

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.UnrecoverableExceptions;

import java.util.Collection;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that an executable throws one of a list of exception types.
/// This class provides assertions for testing that code throws ANY of a specified set of exception types,
/// useful when multiple exception types are acceptable outcomes. All methods in this class will throw an
/// [AssertionFailedError][org.opentest4j.AssertionFailedError] if the assertion fails.
///
/// @author evanbergstrom
/// @see org.junit.jupiter.api.function.Executable
/// @see org.junit.jupiter.api.Assertions
/// @since 1.0
public final class AssertThrowsAnyOf {

    private AssertThrowsAnyOf() { }

    /// Asserts that the executable will throw one of a list of possible exception types.
    /// This method executes the provided executable and verifies that ANY of the expected exception types
    /// is thrown. The assertion passes if any exception that is an instance of the expected types
    /// is thrown. The assertion fails if no exception is thrown or if a different type of exception is thrown.
    /// ```java
    /// // Example with multiple acceptable exception types - these pass
    /// Collection<Class<? extends Throwable>> exceptions = List.of(
    ///     IllegalArgumentException.class,
    ///     NullPointerException.class
    /// );
    ///
    /// assertThrowsAnyOf(exceptions, () -> {
    ///     throw new IllegalArgumentException("Invalid input");  // Passes
    /// });
    ///
    /// assertThrowsAnyOf(exceptions, () -> {
    ///     throw new NullPointerException("Null value");        // Passes
    /// });
    ///
    /// // Example with inheritance - subclasses match
    /// assertThrowsAnyOf(List.of(RuntimeException.class), () -> {
    ///     throw new IllegalArgumentException();                // Passes (subclass of RuntimeException)
    /// });
    ///
    /// // Examples that would fail
    /// assertThrowsAnyOf(List.of(IllegalStateException.class), () -> {
    ///     throw new IllegalArgumentException();                // Fails: wrong exception type
    /// });
    ///
    /// assertThrowsAnyOf(List.of(IllegalStateException.class), () -> {
    ///     // No exception thrown                               // Fails: no exception
    /// });
    /// ```
    ///
    /// @param expectedTypes the collection of exception types that the executable should throw (any one of them)
    /// @param executable the executable to test
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is not an instance of the expected types
    /// @throws NullPointerException if `expectedTypes` or `executable` is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0
    public static void assertThrowsAnyOf(final @NonNull Collection<Class<? extends Throwable>> expectedTypes,
                                              final @NonNull Executable executable) {
        checkThrowsAnyOf(expectedTypes, executable, null);
    }

    /// Asserts that the executable will throw one of a list of possible exception types.
    /// This method executes the provided executable and verifies that ANY of the expected exception types
    /// is thrown. The assertion passes if any exception that is an instance of the expected types
    /// is thrown. The assertion fails if no exception is thrown or if a different type of exception is thrown.
    /// ```java
    /// // Example with custom error message
    /// Collection<Class<? extends Throwable>> validationErrors = List.of(
    ///     IllegalArgumentException.class,
    ///     ValidationException.class
    /// );
    ///
    /// assertThrowsAnyOf(validationErrors, () -> {
    ///     validateInput(null);
    /// }, "Input validation should throw appropriate exception");
    ///
    /// // Example that would fail with custom message
    /// assertThrowsAnyOf(List.of(IOException.class), () -> {
    ///     throw new IllegalStateException();
    /// }, "Expected I/O related exception");
    /// // Throws AssertionFailedError with message: "Expected I/O related exception"
    /// ```
    ///
    /// @param expectedTypes the collection of exception types that the executable should throw (any one of them)
    /// @param executable the executable to test
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is not an instance of the expected types
    /// @throws NullPointerException if {@code expectedTypes} or {@code executable} is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0
    public static void assertThrowsAnyOf(final @NonNull Collection<Class<? extends Throwable>> expectedTypes,
                                              final @NonNull Executable executable,
                                              final String message) {
        checkThrowsAnyOf(expectedTypes, executable, message);
    }

    /// Asserts that the executable will throw one of a list of possible exception types.
    /// This method executes the provided executable and verifies that ANY of the expected exception types
    /// is thrown. The assertion passes if any exception that is an instance of the expected types
    /// is thrown. The assertion fails if no exception is thrown or if a different type of exception is thrown.
    /// ```java
    /// // Example with message supplier (lazy evaluation)
    /// Collection<Class<? extends Throwable>> networkErrors = List.of(
    ///     ConnectException.class,
    ///     SocketTimeoutException.class,
    ///     UnknownHostException.class
    /// );
    ///
    /// assertThrowsAnyOf(networkErrors, () -> {
    ///     connectToServer();
    /// }, () -> "Network operation should fail with: " + networkErrors);
    /// // Message is only computed if assertion fails
    ///
    /// // Example with expensive message computation
    /// assertThrowsAnyOf(expectedExceptions, risky Operation,
    ///     () -> buildDetailedErrorReport(expectedExceptions, actualContext));
    /// // Message computation is deferred until needed
    /// ```
    ///
    /// @param expectedTypes the collection of exception types that the executable should throw (any one of them)
    /// @param executable the executable to test
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is not an instance of the expected types
    /// @throws NullPointerException if {@code expectedTypes} or {@code executable} is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0
    public static void assertThrowsAnyOf(final @NonNull Collection<Class<? extends Throwable>> expectedTypes,
                                              final @NonNull Executable executable,
                                              final Supplier<String> messageSupplier) {
        checkThrowsAnyOf(expectedTypes, executable, messageSupplier);
    }

    private static void checkThrowsAnyOf(final @NonNull Collection<Class<? extends Throwable>> expectedTypes,
                                              final @NonNull Executable executable,
                                              final Object messageOrSupplier) {

        try {
            executable.execute();
        }  catch (Throwable actual) {
            for (Class<?> expectedType : expectedTypes) {
                if (expectedType.isInstance(actual)) {
                    return;
                }
            }
            UnrecoverableExceptions.rethrowIfUnrecoverable(actual);
            throw assertionFailure()
                    .message(messageOrSupplier)
                    .expected(expectedTypes)
                    .actual(actual.getClass())
                    .reason("Unexpected exception type thrown")
                    .cause(actual)
                    .build();
        }
        throw assertionFailure()
                .message(messageOrSupplier)
                .reason(format("Expected one of %s to be thrown, but nothing was thrown.",
                        expectedTypes.stream().map(Class::getCanonicalName).toList()))
                .build();
    }
}
