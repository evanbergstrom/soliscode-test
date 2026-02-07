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
import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that an executable throws an exception that is not one of a
/// specified set of prohibited exception types. This class provides assertions for testing that code throws
/// any exception EXCEPT those specified in the prohibited types list. All methods in this class will throw an
/// {@link org.opentest4j.AssertionFailedError} if the assertion fails.
///
/// @author evanbergstrom
/// @see org.junit.jupiter.api.function.Executable
/// @see org.junit.jupiter.api.Assertions
/// @since 1.0
public final class AssertThrowsDifferent {

    private AssertThrowsDifferent() { }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// This method executes the provided executable and verifies that it throws an exception, but that the
    /// thrown exception is NOT an instance of the prohibited types. The assertion passes if any exception
    /// is thrown that is not an instance of the prohibited types. The assertion fails if no exception is thrown
    /// or if the thrown exception matches one of the prohibited types.
    /// ```java
    /// // Example with prohibited types - these pass
    /// Collection<Class<? extends Throwable>> prohibited = List.of(
    ///     IllegalArgumentException.class,
    ///     NullPointerException.class
    /// );
    ///
    /// assertThrowsDifferent(prohibited, () -> {
    ///     throw new IllegalStateException("Different exception");  // Passes
    /// });
    ///
    /// assertThrowsDifferent(prohibited, () -> {
    ///     throw new RuntimeException("Allowed exception");        // Passes
    /// });
    ///
    /// // Example with inheritance - subclasses are considered instances
    /// assertThrowsDifferent(List.of(RuntimeException.class), () -> {
    ///     throw new IOException("Checked exception");             // Passes (not a RuntimeException)
    /// });
    ///
    /// // Examples that would fail
    /// assertThrowsDifferent(prohibited, () -> {
    ///     throw new IllegalArgumentException("Prohibited");       // Fails: matches prohibited type
    /// });
    ///
    /// assertThrowsDifferent(prohibited, () -> {
    ///     // No exception thrown                                  // Fails: no exception
    /// });
    /// ```
    ///
    /// @param prohibitedTypes the collection of exception types that should NOT be thrown
    /// @param executable the executable to test
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of the prohibited types
    /// @throws NullPointerException if {@code prohibitedTypes} or {@code executable} is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0
    public static void assertThrowsDifferent(final @NonNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NonNull Executable executable) {
        checkThrowsDifferent(prohibitedTypes, executable, null);
    }

    /// Asserts that the executable will throw an exception that is not the prohibited exception type.
    /// This method executes the provided executable and verifies that it throws an exception, but that the
    /// thrown exception is NOT an instance of the prohibited type. The assertion passes if any exception
    /// is thrown that is not an instance of the prohibited type. The assertion fails if no exception is thrown
    /// or if the thrown exception matches the prohibited type.
    /// ```java
    /// // Example with single prohibited type - these pass
    /// assertThrowsDifferent(IllegalArgumentException.class, () -> {
    ///     throw new IllegalStateException("Different exception");  // Passes
    /// });
    ///
    /// assertThrowsDifferent(NullPointerException.class, () -> {
    ///     throw new IllegalArgumentException("Not null pointer");  // Passes
    /// });
    ///
    /// // Example with inheritance checking
    /// assertThrowsDifferent(RuntimeException.class, () -> {
    ///     throw new Exception("Checked exception");               // Passes (not a RuntimeException)
    /// });
    ///
    /// // Examples that would fail
    /// assertThrowsDifferent(IllegalStateException.class, () -> {
    ///     throw new IllegalStateException("Prohibited");          // Fails: matches prohibited type
    /// });
    /// ```
    ///
    /// @param prohibitedType the exception type that should NOT be thrown
    /// @param executable the executable to test
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of the prohibited type
    /// @throws NullPointerException if {@code prohibitedType} or {@code executable} is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0
    public static void assertThrowsDifferent(final @NonNull Class<? extends Throwable> prohibitedType,
                                             final @NonNull Executable executable) {
        checkThrowsDifferent(List.of(prohibitedType), executable, null);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// This method executes the provided executable and verifies that it throws an exception, but that the
    /// thrown exception is NOT an instance of the prohibited types. The assertion passes if any exception
    /// is thrown that is not an instance of the prohibited types. The assertion fails if no exception is thrown
    /// or if the thrown exception matches one of the prohibited types.
    /// ```java
    /// // Example with custom error message
    /// Collection<Class<? extends Throwable>> invalidExceptions = List.of(
    ///     UnsupportedOperationException.class,
    ///     IllegalStateException.class
    /// );
    ///
    /// assertThrowsDifferent(invalidExceptions, () -> {
    ///     performRiskyOperation();
    /// }, "Operation should fail but not with known invalid states");
    ///
    /// // Example that would fail with custom message
    /// assertThrowsDifferent(List.of(IOException.class), () -> {
    ///     throw new IOException("I/O error");
    /// }, "Expected non-I/O exception");
    /// // Throws AssertionFailedError with message: "Expected non-I/O exception"
    /// ```
    ///
    /// @param prohibitedTypes the collection of exception types that should NOT be thrown
    /// @param executable the executable to test
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of the prohibited types
    /// @throws NullPointerException if {@code prohibitedTypes} or {@code executable} is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0
    public static void assertThrowsDifferent(final @NonNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                       final @NonNull Executable executable, final String message) {
        checkThrowsDifferent(prohibitedTypes, executable, message);
    }

    /// Asserts that the executable will throw an exception that is not the prohibited exception type.
    /// This method executes the provided executable and verifies that it throws an exception, but that the
    /// thrown exception is NOT an instance of the prohibited type. The assertion passes if any exception
    /// is thrown that is not an instance of the prohibited type. The assertion fails if no exception is thrown
    /// or if the thrown exception matches the prohibited type.
    /// ```java
    /// // Example with custom error message for single prohibited type
    /// assertThrowsDifferent(SecurityException.class, () -> {
    ///     accessRestrictedResource();
    /// }, "Access should fail but not due to security restrictions");
    ///
    /// // Example that would fail with custom message
    /// assertThrowsDifferent(ValidationException.class, () -> {
    ///     throw new ValidationException("Invalid input");
    /// }, "Expected non-validation error");
    /// // Throws AssertionFailedError with message: "Expected non-validation error"
    /// ```
    ///
    /// @param prohibitedType the exception type that should NOT be thrown
    /// @param executable the executable to test
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of the prohibited type
    /// @throws NullPointerException if {@code prohibitedType} or {@code executable} is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0
    public static void assertThrowsDifferent(final @NonNull Class<? extends Throwable> prohibitedType,
                                             final @NonNull Executable executable, final String message) {
       checkThrowsDifferent(List.of(prohibitedType), executable, message);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// This method executes the provided executable and verifies that it throws an exception, but that the
    /// thrown exception is NOT an instance of the prohibited types. The assertion passes if any exception
    /// is thrown that is not an instance of the prohibited types. The assertion fails if no exception is thrown
    /// or if the thrown exception matches one of the prohibited types.
    /// ```java
    /// // Example with message supplier (lazy evaluation)
    /// Collection<Class<? extends Throwable>> knownBadExceptions = List.of(
    ///     OutOfMemoryError.class,
    ///     StackOverflowError.class,
    ///     ThreadDeath.class
    /// );
    ///
    /// assertThrowsDifferent(knownBadExceptions, () -> {
    ///     performComplexOperation();
    /// }, () -> "Complex operation failed with serious system error: " + knownBadExceptions);
    /// // Message is only computed if assertion fails
    ///
    /// // Example with expensive message computation
    /// assertThrowsDifferent(prohibitedExceptions, riskyCode,
    ///     () -> buildDetailedFailureAnalysis(prohibitedExceptions, operationContext));
    /// // Message computation is deferred until needed
    /// ```
    ///
    /// @param prohibitedTypes the collection of exception types that should NOT be thrown
    /// @param executable the executable to test
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of the prohibited types
    /// @throws NullPointerException if {@code prohibitedTypes} or {@code executable} is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0
    public static void assertThrowsDifferent(final @NonNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NonNull Executable executable,
                                             final Supplier<String> messageSupplier) {
        checkThrowsDifferent(prohibitedTypes, executable, messageSupplier);
    }

    /// Asserts that the executable will throw an exception that is not the prohibited exception type.
    /// This method executes the provided executable and verifies that it throws an exception, but that the
    /// thrown exception is NOT an instance of the prohibited type. The assertion passes if any exception
    /// is thrown that is not an instance of the prohibited type. The assertion fails if no exception is thrown
    /// or if the thrown exception matches the prohibited type.
    /// ```java
    /// // Example with message supplier for single prohibited type (lazy evaluation)
    /// assertThrowsDifferent(TimeoutException.class, () -> {
    ///     performTimeSensitiveOperation();
    /// }, () -> "Operation should fail but not due to timeout at " + System.currentTimeMillis());
    /// // Timestamp is only computed if assertion fails
    ///
    /// // Example with expensive message computation
    /// assertThrowsDifferent(ConcurrencyException.class, concurrentTask,
    ///     () -> analyzeThreadState(currentThread(), prohibitedType));
    /// // Thread analysis only happens if assertion fails
    /// ```
    ///
    /// @param prohibitedType the exception type that should NOT be thrown
    /// @param executable the executable to test
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of the prohibited type
    /// @throws NullPointerException if {@code prohibitedType} or {@code executable} is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0
    public static void assertThrowsDifferent(final @NonNull Class<? extends Throwable> prohibitedType,
                                             final @NonNull Executable executable,
                                             final Supplier<String> messageSupplier) {
        checkThrowsDifferent(List.of(prohibitedType), executable, messageSupplier);
    }

    private static void checkThrowsDifferent(final @NonNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NonNull Executable executable, final Object messageOrSupplier) {
        try {
            executable.execute();
        } catch (Throwable actual) {
            for (Class<?> expectedType : prohibitedTypes) {
                if (expectedType.isInstance(actual)) {
                    UnrecoverableExceptions.rethrowIfUnrecoverable(actual);
                    throw assertionFailure()
                            .message(messageOrSupplier)
                            .expected(prohibitedTypes)
                            .actual(actual.getClass())
                            .reason("Unexpected exception type thrown")
                            .cause(actual)
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
