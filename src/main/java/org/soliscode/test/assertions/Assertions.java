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
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.function.Executable;
import org.soliscode.test.assertions.string.AssertStringContains;
import org.soliscode.test.assertions.string.AssertStringContainsInOrder;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/// **Specialized Test Assertions for Advanced Testing Scenarios**
///
/// This utility class provides a comprehensive set of assertion methods that extend and complement
/// the standard JUnit Jupiter assertions. These specialized assertions fill gaps in common testing
/// scenarios, particularly for exception handling, type checking, interface validation, and
/// comparative operations.
///
/// ## Purpose and Design Philosophy
///
/// The assertions in this class follow these design principles:
/// - **Complement JUnit**: Extend rather than replace standard JUnit assertions
/// - **Type Safety**: Leverage Java's type system for compile-time safety
/// - **Consistent API**: Follow JUnit's pattern of method overloading for messages and suppliers
/// - **Clear Semantics**: Each assertion has a single, well-defined purpose
/// - **Performance**: Designed for minimal overhead in test execution
///
/// ## Assertion Categories
///
/// ### Exception Assertions
/// - **`assertThrowsAnyOf`**: Verifies an exception from a specified set is thrown
/// - **`assertThrowsDifferent`**: Verifies an exception other than prohibited types is thrown
///
/// ### Type and Interface Assertions
/// - **`assertNotInstanceOf`**: Verifies an object is NOT an instance of a specific type
/// - **`assertImplementsOnly`**: Verifies an object implements only permitted interfaces
///
/// ### Comparison Assertions
/// - **`assertLessThan`**: Verifies first value is less than second
/// - **`assertGreaterThan`**: Verifies first value is greater than second
/// - **`assertLessThanOrEqual`**: Verifies first value is less than or equal to second
/// - **`assertGreaterThanOrEqual`**: Verifies first value is greater than or equal to second
///
/// ### String Content Assertions
/// - **`assertStringContains`**: Verifies string contains specified substrings
/// - **`assertStringContainsInOrder`**: Verifies string contains substrings in specified order
///
/// ## Usage Examples
///
/// ### Exception Testing
/// ```java
/// import static org.soliscode.test.assertions.Assertions.*;
///
/// // Test that method throws one of several acceptable exceptions
/// assertThrowsAnyOf(
///     List.of(IllegalArgumentException.class, NullPointerException.class),
///     () -> riskyMethod(null)
/// );
///
/// // Test that method throws something other than a specific exception
/// assertThrowsDifferent(
///     UnsupportedOperationException.class,
///     () -> methodThatShouldNotThrowUOE()
/// );
/// ```
///
/// ### Type and Interface Validation
/// ```java
/// // Verify object is not an instance of deprecated type
/// assertNotInstanceOf(DeprecatedClass.class, actualObject);
///
/// // Verify implementation only uses permitted interfaces
/// assertImplementsOnly(
///     List.of(Serializable.class, Comparable.class),
///     myCustomObject
/// );
/// ```
///
/// ### Comparative Assertions
/// ```java
/// // Natural ordering comparisons
/// assertLessThan(startTime, endTime);
/// assertGreaterThan(actualScore, minimumScore);
/// assertLessThanOrEqual(memoryUsed, memoryLimit);
/// assertGreaterThanOrEqual(performanceIndex, baseline);
///
/// // Works with any Comparable type
/// assertLessThan("apple", "banana");  // Alphabetical order
/// assertGreaterThan(BigDecimal.valueOf(100), BigDecimal.valueOf(50));
/// ```
///
/// ### String Content Validation
/// ```java
/// // Verify string contains expected content
/// assertStringContains("error", logOutput);
/// assertStringContains(List.of("user", "authenticated"), securityLog);
///
/// // Verify content appears in correct order
/// assertStringContainsInOrder(
///     List.of("startup", "initialized", "ready"),
///     applicationLog
/// );
/// ```
///
/// ## Integration with JUnit
///
/// These assertions integrate seamlessly with JUnit 5 and follow the same patterns:
///
/// ```java
/// @Test
/// void testComplexScenario() {
///     // Standard JUnit assertions
///     assertNotNull(result);
///     assertEquals(expectedValue, result.getValue());
///
///     // SolisCode specialized assertions
///     assertImplementsOnly(List.of(MyInterface.class), result);
///     assertGreaterThan(result.getScore(), minimumThreshold);
///     assertThrowsAnyOf(
///         List.of(ValidationException.class, ProcessingException.class),
///         () -> result.process()
///     );
/// }
/// ```
///
/// ## Error Messages and Debugging
///
/// All assertions support:
/// - **Default error messages**: Clear, descriptive failure messages
/// - **Custom messages**: String-based custom error messages
/// - **Lazy messages**: Supplier-based messages for expensive string construction
///
/// ```java
/// // Default message
/// assertGreaterThan(actual, expected);
///
/// // Custom message
/// assertGreaterThan(actual, expected, "Performance below threshold");
///
/// // Lazy message (computed only on failure)
/// assertGreaterThan(actual, expected, () ->
///     "Expected " + actual + " > " + expected + " but got difference of " + (expected - actual)
/// );
/// ```
///
/// ## Performance Considerations
///
/// - **Zero overhead on success**: Assertions only create objects/strings on failure
/// - **Lazy evaluation**: Supplier-based messages computed only when needed
/// - **Minimal reflection**: Type checking uses efficient instanceof operations
/// - **Direct delegation**: Most operations delegate to optimized implementations
///
/// ## Thread Safety
///
/// This class is thread-safe. All methods are static and stateless, making concurrent
/// access safe from multiple threads. No synchronization is required when calling
/// these assertion methods from different threads.
///
/// @author evanbergstrom
/// @since 1.0
/// @see org.junit.jupiter.api.Assertions
/// @see org.opentest4j.AssertionFailedError
public final class Assertions {

    /// Private constructor to prevent instantiation of this utility class.
    ///
    /// This class is designed to be used as a static utility and should never be instantiated.
    /// All methods are static and the class is final to prevent subclassing.
    private Assertions() { }

    /// Asserts that the executable will throw a {@link Throwable} of any type.
    ///
    /// This is equivalent to {@code assertThrows(Throwable.class, executable)} in JUnit 5.
    ///
    /// @param executable the executable to test.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown.
    /// @see org.junit.jupiter.api.Assertions#assertThrows(Class, Executable)
    /// @since 1.0.0
    public static void assertThrowsAny(final @NonNull Executable executable) {
        org.junit.jupiter.api.Assertions.assertThrows(Throwable.class, executable);
    }

    /// Asserts that the executable will throw a {@link Throwable} of any type with a custom error message.
    ///
    /// @param executable the executable to test.
    /// @param message the custom message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown.
    /// @see #assertThrowsAny(Executable)
    /// @since 1.0.0
    public static void assertThrowsAny(final @NonNull Executable executable, final String message) {
        org.junit.jupiter.api.Assertions.assertThrows(Throwable.class, executable, message);
    }

    /// Asserts that the executable will throw a {@link Throwable} of any type with a lazily-evaluated error message.
    ///
    /// @param executable the executable to test.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown.
    /// @see #assertThrowsAny(Executable)
    /// @since 1.0.0
    public static void assertThrowsAny(final @NonNull Executable executable,
                                       final Supplier<String> messageSupplier) {
        org.junit.jupiter.api.Assertions.assertThrows(Throwable.class, executable, messageSupplier);
    }

    /// Asserts that the executable will throw one of a list of possible exception types.
    ///
    /// @param expectedTypes the exception types that the executable should throw.
    /// @param executable the executable to test.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is not an instance of one of the expected types.
    /// @since 1.0.0
    public static void assertThrowsAnyOf(final @NonNull Collection<Class<? extends Throwable>> expectedTypes,
                                         final @NonNull Executable executable) {
        AssertThrowsAnyOf.assertThrowsAnyOf(expectedTypes, executable);
    }

    /// Asserts that the executable will throw one of a list of possible exception types with a custom error message.
    ///
    /// @param expectedTypes the exception types that the executable should throw.
    /// @param executable the executable to test.
    /// @param message the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is not an instance of one of the expected types.
    /// @since 1.0.0
    public static void assertThrowsAnyOf(final @NonNull Collection<Class<? extends Throwable>> expectedTypes,
                                         final @NonNull Executable executable, final @Nullable String message) {
        AssertThrowsAnyOf.assertThrowsAnyOf(expectedTypes, executable, message);
    }

    /// Asserts that the executable will throw one of a list of possible exception types with a lazily-evaluated error
    /// message.
    ///
    /// @param expectedTypes the exception types that the executable should throw.
    /// @param executable the executable to test.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is not an instance of one of the expected types.
    /// @since 1.0.0
    public static void assertThrowsAnyOf(final @NonNull Collection<Class<? extends Throwable>> expectedTypes,
                                         final @NonNull Executable executable,
                                         final @Nullable Supplier<String> messageSupplier) {
        AssertThrowsAnyOf.assertThrowsAnyOf(expectedTypes, executable, messageSupplier);
    }

    /// Asserts that the executable will throw an exception that is not the prohibited exception type.
    ///
    /// @param prohibitedType the exception type that the executable should not throw.
    /// @param executable the executable to test.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of the prohibited type.
    /// @since 1.0.0
    public static void assertThrowsDifferent(final @NonNull Class<? extends Throwable> prohibitedType,
                                             final @NonNull Executable executable) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedType, executable);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    ///
    /// @param prohibitedTypes the exception types that the executable should not throw.
    /// @param executable the executable to test.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of one of the prohibited types.
    /// @since 1.0.0
    public static void assertThrowsDifferent(final @NonNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NonNull Executable executable) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedTypes, executable);
    }

    /// Asserts that the executable will throw an exception that is not the prohibited exception type with a custom
    /// error message.
    ///
    /// @param prohibitedType the exception type that the executable should not throw.
    /// @param executable the executable to test.
    /// @param message the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of the prohibited type.
    /// @since 1.0.0
    public static void assertThrowsDifferent(final @NonNull Class<? extends Throwable> prohibitedType,
                                             final @NonNull Executable executable,
                                             final @Nullable String message) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedType, executable, message);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types
    /// with a custom error message.
    ///
    /// @param prohibitedTypes the exception types that the executable should not throw.
    /// @param executable the executable to test.
    /// @param message the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of one of the prohibited types.
    /// @since 1.0.0
    public static void assertThrowsDifferent(final @NonNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NonNull Executable executable,
                                             final @Nullable String message) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedTypes, executable, message);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types
    /// with a lazily-evaluated error message.
    ///
    /// @param prohibitedTypes the exception types that the executable should not throw.
    /// @param executable the executable to test.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of one of the prohibited types.
    /// @since 1.0.0
    public static void assertThrowsDifferent(final @NonNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NonNull Executable executable,
                                             final @Nullable Supplier<String> messageSupplier) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedTypes, executable, messageSupplier);
    }

    /// Asserts that the executable will throw an exception that is not the prohibited exception type with a
    /// lazily-evaluated error message.
    ///
    /// @param prohibitedType the exception type that the executable should not throw.
    /// @param executable the executable to test.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if no exception is thrown or if the thrown exception
    ///         is an instance of the prohibited type.
    /// @since 1.0.0
    public static void assertThrowsDifferent(final @NonNull Class<? extends Throwable> prohibitedType,
                                             final @NonNull Executable executable,
                                             final @Nullable Supplier<String> messageSupplier) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedType, executable, messageSupplier);
    }


    /// Asserts that the object is not an instance of a class.
    ///
    /// @param expectedType The type that the object being tested is expected *not* to be.
    /// @param actual The object being tested.
    /// @param <T> The type that is not expected.
    /// @throws org.opentest4j.AssertionFailedError if the object is an instance of the expected type.
    /// @since 1.0.0
    public static <T> void assertNotInstanceOf(final @NonNull Class<T> expectedType, final @NonNull Object actual) {
        AssertNotInstanceOf.assertNotInstanceOf(expectedType, actual);
    }

    /// Asserts that the object is not an instance of a class with a custom error message.
    ///
    /// @param expectedType The type that the object being tested is expected *not* to be.
    /// @param actual The object being tested.
    /// @param message the message to include in the exception if the assertion fails.
    /// @param <T> The type that is not expected.
    /// @throws org.opentest4j.AssertionFailedError if the object is an instance of the expected type.
    /// @since 1.0.0
    public static <T> void assertNotInstanceOf(final @NonNull Class<T> expectedType, final @NonNull Object actual,
                                               final @Nullable String message) {
        AssertNotInstanceOf.assertNotInstanceOf(expectedType, actual, message);
    }

    /// Asserts that the object is not an instance of a class with a lazily-evaluated error message.
    ///
    /// @param expectedType The type that the object being tested is expected *not* to be.
    /// @param actual The object being tested.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails.
    /// @param <T> The type that is not expected.
    /// @throws org.opentest4j.AssertionFailedError if the object is an instance of the expected type.
    /// @since 1.0.0
    public static <T> void assertNotInstanceOf(final @NonNull Class<T> expectedType, final @NonNull Object actual,
                                               final @Nullable Supplier<String> messageSupplier) {
        AssertNotInstanceOf.assertNotInstanceOf(expectedType, actual, messageSupplier);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly.
    ///
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    /// @throws org.opentest4j.AssertionFailedError if the object implements interfaces not in the permitted set.
    /// @since 1.0.0
    public static void assertImplementsOnly(final @NonNull Class<?> expected, final @NonNull Object actual) {
        AssertImplementsOnly.assertImplementsOnly(expected, actual);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly.
    ///
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    /// @throws org.opentest4j.AssertionFailedError if the object implements interfaces not in the permitted set.
    /// @since 1.0.0
    public static void assertImplementsOnly(final @NonNull Collection<Class<?>> expected,
                                            final @NonNull Object actual) {
        AssertImplementsOnly.assertImplementsOnly(expected, actual);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly, with a custom
    /// error message.
    ///
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    /// @param message the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if the object implements interfaces not in the permitted set.
    /// @since 1.0.0
    public static void assertImplementsOnly(final @NonNull Class<?> expected, final @NonNull Object actual,
                                            final @Nullable String message) {
        AssertImplementsOnly.assertImplementsOnly(expected, actual, message);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly, with a custom
    /// error message.
    ///
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    /// @param message the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if the object implements interfaces not in the permitted set.
    /// @since 1.0.0
    public static void assertImplementsOnly(final @NonNull Collection<Class<?>> expected, final @NonNull Object actual,
                                            final String message) {
        AssertImplementsOnly.assertImplementsOnly(expected, actual, message);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly, with a
    /// lazily-evaluated error message.
    ///
    /// @param expected the class or interface that the object can be an instance of.
    /// @param actual The object being tested.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if the object implements interfaces not in the permitted set.
    /// @since 1.0.0
    public static void assertImplementsOnly(final @NonNull Class<?> expected, final @NonNull Object actual,
                                            final @Nullable Supplier<String> messageSupplier) {
        AssertImplementsOnly.assertImplementsOnly(expected, actual, messageSupplier);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly, with a
    /// lazily-evaluated error message.
    ///
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if the object implements interfaces not in the permitted set.
    /// @since 1.0.0
    public static void assertImplementsOnly(final @NonNull Collection<Class<?>> expected, final @NonNull Object actual,
                                            final @Nullable Supplier<String> messageSupplier) {
        AssertImplementsOnly.assertImplementsOnly(expected, actual, messageSupplier);
    }

    /// Asserts that the first comparable value is less than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `second.compareTo(first)`. The assertion fails if the first value
    /// is greater than or equal to the second value.
    /// ```java
    /// // Example with integers
    /// assertLessThan(3, 5); // Passes: 3 < 5
    ///
    /// // Example with strings (alphabetical ordering)
    /// assertLessThan("apple", "banana"); // Passes: "apple" < "banana"
    ///
    /// // Example with doubles
    /// assertLessThan(2.5, 3.0); // Passes: 2.5 < 3.0
    ///
    /// // Example that would fail
    /// assertLessThan(10, 5); // Throws AssertionFailedError: 10 >= 5
    /// ```
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than the second value
    /// @param second the value that the first value should be less than
    /// @throws org.opentest4j.AssertionFailedError if `first >= second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThan(final @NonNull T first, final @NonNull T second) {
        AssertLessThan.assertLessThan(first, second);
    }

    /// Asserts that the first comparable value is less than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `second.compareTo(first)`. The assertion fails if the first value
    /// is greater than or equal to the second value.
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than the second value
    /// @param second the value that the first value should be less than
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if `first >= second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThan(final @NonNull T first, final @NonNull T second,
                                                                final @Nullable String message) {
        AssertLessThan.assertLessThan(first, second, message);
    }

    /// Asserts that the first comparable value is less than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `second.compareTo(first)`. The assertion fails if the first value
    /// is greater than or equal to the second value.
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than the second value
    /// @param second the value that the first value should be less than
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if `first >= second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThan(final @NonNull T first, final @NonNull T second,
                                                                final @Nullable Supplier<String> messageSupplier) {
        AssertLessThan.assertLessThan(first, second, messageSupplier);
    }

    /// Asserts that the first comparable value is greater than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `first.compareTo(second)`. The assertion fails if the first value
    /// is less than or equal to the second value.
    /// ```java
    /// // Example with integers
    /// assertGreaterThan(5, 3); // Passes: 5 > 3
    ///
    /// // Example with strings (alphabetical ordering)
    /// assertGreaterThan("banana", "apple"); // Passes: "banana" > "apple"
    ///
    /// // Example with doubles
    /// assertGreaterThan(3.0, 2.5); // Passes: 3.0 > 2.5
    ///
    /// // Example that would fail
    /// assertGreaterThan(5, 10); // Throws AssertionFailedError: 5 <= 10
    /// ```
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be greater than the second value
    /// @param second the value that the first value should be greater than
    /// @throws org.opentest4j.AssertionFailedError if `first <= second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertGreaterThan(final @NonNull T first, final @NonNull T second) {
        AssertGreaterThan.assertGreaterThan(first, second);
    }

    /// Asserts that the first comparable value is greater than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `first.compareTo(second)`. The assertion fails if the first value
    /// is less than or equal to the second value.
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be greater than the second value
    /// @param second the value that the first value should be greater than
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if `first <= second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertGreaterThan(final @NonNull T first, final @NonNull T second,
                                                                   final @Nullable String message) {
        AssertGreaterThan.assertGreaterThan(first, second, message);
    }

    /// Asserts that the first comparable value is greater than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `first.compareTo(second)`. The assertion fails if the first value
    /// is less than or equal to the second value.
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be greater than the second value
    /// @param second the value that the first value should be greater than
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if `first <= second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertGreaterThan(final @NonNull T first, final @NonNull T second,
                                                                   final @Nullable Supplier<String> messageSupplier) {
        AssertGreaterThan.assertGreaterThan(first, second, messageSupplier);
    }

    /// Asserts that the first comparable value is less than or equal to the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `first.compareTo(second)`. The assertion fails if the first value
    /// is greater than the second value.
    /// ```java
    /// // Example with integers
    /// assertLessThanOrEqual(3, 5); // Passes: 3 <= 5
    /// assertLessThanOrEqual(5, 5); // Passes: 5 <= 5
    ///
    /// // Example with strings (alphabetical ordering)
    /// assertLessThanOrEqual("apple", "banana"); // Passes: "apple" <= "banana"
    /// assertLessThanOrEqual("apple", "apple"); // Passes: "apple" <= "apple"
    ///
    /// // Example that would fail
    /// assertLessThanOrEqual(10, 5); // Throws AssertionFailedError: 10 > 5
    /// ```
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than or equal to the second value
    /// @param second the value that the first value should be less than or equal to
    /// @throws org.opentest4j.AssertionFailedError if `first > second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThanOrEqual(final @NonNull T first,
                                                                       final @NonNull T second) {
        AssertLessThanOrEqual.assertLessThanOrEqual(first, second);
    }

    /// Asserts that the first comparable value is less than or equal to the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `first.compareTo(second)`. The assertion fails if the first value
    /// is greater than the second value.
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than or equal to the second value
    /// @param second the value that the first value should be less than or equal to
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if `first > second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThanOrEqual(final @NonNull T first, final @NonNull T second,
                                                                       final @Nullable String message) {
        AssertLessThanOrEqual.assertLessThanOrEqual(first, second, message);
    }

    /// Asserts that the first comparable value is less than or equal to the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `first.compareTo(second)`. The assertion fails if the first value
    /// is greater than the second value.
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than or equal to the second value
    /// @param second the value that the first value should be less than or equal to
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if `first > second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThanOrEqual(final @NonNull T first, final @NonNull T second,
                                                                   final @Nullable Supplier<String> messageSupplier) {
        AssertLessThanOrEqual.assertLessThanOrEqual(first, second, messageSupplier);
    }

    /// Asserts that the first comparable value is greater than or equal to the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `first.compareTo(second)`. The assertion fails if the first value
    /// is less than the second value.
    /// ```java
    /// // Example with integers
    /// assertGreaterThanOrEqual(5, 3); // Passes: 5 >= 3
    /// assertGreaterThanOrEqual(5, 5); // Passes: 5 >= 5
    ///
    /// // Example with strings (alphabetical ordering)
    /// assertGreaterThanOrEqual("banana", "apple"); // Passes: "banana" >= "apple"
    /// assertGreaterThanOrEqual("apple", "apple"); // Passes: "apple" >= "apple"
    ///
    /// // Example that would fail
    /// assertGreaterThanOrEqual(5, 10); // Throws AssertionFailedError: 5 < 10
    /// ```
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be greater than or equal to the second value
    /// @param second the value that the first value should be greater than or equal to
    /// @throws org.opentest4j.AssertionFailedError if `first < second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertGreaterThanOrEqual(final @NonNull T first,
                                                                          final @NonNull T second) {
        AssertGreaterThanOrEqual.assertGreaterThanOrEqual(first, second);
    }

    /// Asserts that the first comparable value is greater than or equal to the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `first.compareTo(second)`. The assertion fails if the first value
    /// is less than the second value.
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be greater than or equal to the second value
    /// @param second the value that the first value should be greater than or equal to
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if `first < second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertGreaterThanOrEqual(final @NonNull T first,
                                                                          final @NonNull T second,
                                                                          final @Nullable String message) {
        AssertGreaterThanOrEqual.assertGreaterThanOrEqual(first, second, message);
    }

    /// Asserts that the first comparable value is greater than or equal to the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking `first.compareTo(second)`. The assertion fails if the first value
    /// is less than the second value.
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be greater than or equal to the second value
    /// @param second the value that the first value should be greater than or equal to
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if `first < second`
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertGreaterThanOrEqual(final @NonNull T first,
                                                                          final @NonNull T second,
                                                                     final @Nullable Supplier<String> messageSupplier) {
        AssertGreaterThanOrEqual.assertGreaterThanOrEqual(first, second, messageSupplier);
    }

    /// Asserts that the actual string contains the expected substring.
    ///
    /// This method verifies that the specified substring appears anywhere within the actual string.
    /// The search is case-sensitive and uses standard string containment checking.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Basic substring checking
    /// assertStringContains("error", "An error occurred");        // Passes
    /// assertStringContains("Error", "An error occurred");        // Fails (case-sensitive)
    /// assertStringContains("world", "Hello world!");             // Passes
    ///
    /// // Log file validation
    /// String logOutput = "2024-01-01 ERROR: Database connection failed";
    /// assertStringContains("ERROR", logOutput);                  // Passes
    /// assertStringContains("Database", logOutput);               // Passes
    /// ```
    ///
    /// @param expected the substring that must be present in the actual string
    /// @param actual the string to search within
    /// @throws org.opentest4j.AssertionFailedError if the expected substring is not found
    /// @see #assertStringContains(List, String)
    /// @since 1.0.0
    public static void assertStringContains(final @NonNull String expected, final @NonNull String actual) {
        AssertStringContains.assertStringContains(List.of(expected), actual);
    }

    /// Asserts that the actual string contains the expected substring, ignoring case differences.
    ///
    /// This method verifies that the specified substring appears anywhere within the actual string,
    /// performing a case-insensitive search. This is particularly useful for validating content
    /// where case variations are expected or irrelevant.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Case-insensitive substring checking
    /// assertStringContainsIgnoreCase("error", "An ERROR occurred");       // Passes
    /// assertStringContainsIgnoreCase("ERROR", "An error occurred");       // Passes
    /// assertStringContainsIgnoreCase("ErRoR", "An error occurred");       // Passes
    /// assertStringContainsIgnoreCase("world", "Hello WORLD!");            // Passes
    ///
    /// // Log file validation with mixed case
    /// String logOutput = "2024-01-01 error: Database Connection Failed";
    /// assertStringContainsIgnoreCase("ERROR", logOutput);                 // Passes
    /// assertStringContainsIgnoreCase("database", logOutput);              // Passes
    /// assertStringContainsIgnoreCase("FAILED", logOutput);                // Passes
    /// ```
    ///
    /// @param expected the substring that must be present in the actual string (case-insensitive)
    /// @param actual the string to search within
    /// @throws org.opentest4j.AssertionFailedError if the expected substring is not found
    /// @see #assertStringContains(String, String)
    /// @since 1.0.0
    public static void assertStringContainsIgnoreCase(final @NonNull String expected, final @NonNull String actual) {
        AssertStringContains.assertStringContainsIgnoreCase(List.of(expected), actual);
    }

    /// Asserts that the actual string contains the expected substring with a custom error message.
    ///
    /// @param expected the substring that must be present in the actual string
    /// @param actual the string to search within
    /// @param message the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if the expected substring is not found
    /// @see #assertStringContains(String, String)
    /// @since 1.0.0
    public static void assertStringContains(final @NonNull String expected, final @NonNull String actual,
                                            final String message) {
        AssertStringContains.assertStringContains(List.of(expected), actual, message);
    }

    /// Asserts that the actual string contains the expected substring with a custom error message, ignoring case
    /// differences.
    ///
    /// @param expected the substring that must be present in the actual string (case-insensitive)
    /// @param actual the string to search within
    /// @param message the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if the expected substring is not found
    /// @see #assertStringContainsIgnoreCase(String, String)
    /// @since 1.0.0
    public static void assertStringContainsIgnoreCase(final @NonNull String expected, final @NonNull String actual,
                                                      final String message) {
        AssertStringContains.assertStringContainsIgnoreCase(List.of(expected), actual, message);
    }

    /// Asserts that the actual string contains the expected substring with a lazily-evaluated error message.
    ///
    /// @param expected the substring that must be present in the actual string
    /// @param actual the string to search within
    /// @param supplier the supplier of the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if the expected substring is not found
    /// @see #assertStringContains(String, String)
    /// @since 1.0.0
    public static void assertStringContains(final @NonNull String expected,
                                            final @NonNull String actual,
                                            final @NonNull Supplier<String> supplier) {
        AssertStringContains.assertStringContains(List.of(expected), actual, supplier);
    }

    /// Asserts that the actual string contains the expected substring with a lazily-evaluated error message, ignoring
    /// case differences.
    ///
    /// @param expected the substring that must be present in the actual string (case-insensitive)
    /// @param actual the string to search within
    /// @param supplier the supplier of the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if the expected substring is not found
    /// @see #assertStringContainsIgnoreCase(String, String)
    /// @since 1.0.0
    public static void assertStringContainsIgnoreCase(final @NonNull String expected,
                                                      final @NonNull String actual,
                                                      final @NonNull Supplier<String> supplier) {
        AssertStringContains.assertStringContainsIgnoreCase(List.of(expected), actual, supplier);
    }
    /// Asserts that the actual string contains all of the expected substrings.
    ///
    /// This method verifies that every substring in the expected list appears somewhere within
    /// the actual string. The order of appearance is not checked - use `assertStringContainsInOrder`
    /// for order-sensitive validation.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Multiple substring validation
    /// String response = "User john.doe authenticated successfully";
    /// assertStringContains(
    ///     List.of("User", "authenticated", "successfully"),
    ///     response
    /// );  // Passes - all substrings present
    ///
    /// // API response validation
    /// String jsonResponse = "{\"status\":\"success\",\"user\":\"admin\",\"timestamp\":\"2024-01-01\"}";
    /// assertStringContains(
    ///     List.of("status", "success", "user", "admin"),
    ///     jsonResponse
    /// );  // Passes
    ///
    /// // Missing substring example
    /// assertStringContains(
    ///     List.of("error", "failure"),
    ///     "Operation completed successfully"
    /// );  // Fails - neither "error" nor "failure" found
    /// ```
    ///
    /// @param expected the list of substrings that must all be present in the actual string
    /// @param actual the string to search within
    /// @throws org.opentest4j.AssertionFailedError if any expected substring is not found
    /// @see #assertStringContainsInOrder(List, String)
    /// @since 1.0.0
    public static void assertStringContains(final @NonNull List<String> expected, final @NonNull String actual) {
        AssertStringContains.assertStringContains(expected, actual);
    }

    /// Asserts that the actual string contains all of the expected substrings, ignoring case differences.
    ///
    /// This method verifies that every substring in the expected list appears somewhere within
    /// the actual string, performing case-insensitive matching. The order of appearance is not
    /// checked - use `assertStringContainsInOrderIgnoreCase` for order-sensitive validation.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Multiple case-insensitive substring validation
    /// String response = "User JOHN.DOE authenticated successfully";
    /// assertStringContainsIgnoreCase(
    ///     List.of("user", "AUTHENTICATED", "Successfully"),
    ///     response
    /// );  // Passes - all substrings present regardless of case
    ///
    /// // API response validation with mixed case
    /// String jsonResponse = "{\"STATUS\":\"Success\",\"USER\":\"admin\",\"timestamp\":\"2024-01-01\"}";
    /// assertStringContainsIgnoreCase(
    ///     List.of("status", "SUCCESS", "User", "ADMIN"),
    ///     jsonResponse
    /// );  // Passes - case variations handled
    /// ```
    ///
    /// @param expected the list of substrings that must all be present in the actual string (case-insensitive)
    /// @param actual the string to search within
    /// @throws org.opentest4j.AssertionFailedError if any expected substring is not found
    /// @see #assertStringContainsIgnoreCase(String, String)
    /// @see #assertStringContainsInOrderIgnoreCase(List, String)
    /// @since 1.0.0
    public static void assertStringContainsIgnoreCase(final @NonNull List<String> expected,
                                                      final @NonNull String actual) {
        AssertStringContains.assertStringContainsIgnoreCase(expected, actual);
    }

    /// Asserts that the actual string contains all of the expected substrings with a custom error message.
    ///
    /// @param expected the list of substrings that must all be present in the actual string
    /// @param actual the string to search within
    /// @param message the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if any expected substring is not found
    /// @see #assertStringContains(List, String)
    /// @since 1.0.0
    public static void assertStringContains(final @NonNull List<String> expected, final @NonNull String actual,
                                            final String message) {
        AssertStringContains.assertStringContains(expected, actual, message);
    }

    /// Asserts that the actual string contains all of the expected substrings with a custom error message, ignoring
    /// case differences.
    ///
    /// @param expected the list of substrings that must all be present in the actual string (case-insensitive)
    /// @param actual the string to search within
    /// @param message the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if any expected substring is not found
    /// @see #assertStringContainsIgnoreCase(List, String)
    /// @since 1.0.0
    public static void assertStringContainsIgnoreCase(final @NonNull List<String> expected,
                                                      final @NonNull String actual,
                                                      final String message) {
        AssertStringContains.assertStringContainsIgnoreCase(expected, actual, message);
    }

    /// Asserts that the actual string contains all of the expected substrings with a lazily-evaluated error message.
    ///
    /// @param expected the list of substrings that must all be present in the actual string
    /// @param actual the string to search within
    /// @param supplier the supplier of the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if any expected substring is not found
    /// @see #assertStringContains(List, String)
    /// @since 1.0.0
    public static void assertStringContains(final @NonNull List<String> expected,
                                            final @NonNull String actual,
                                            final @NonNull Supplier<String> supplier) {
        AssertStringContains.assertStringContains(expected, actual, supplier);
    }

    /// Asserts that the actual string contains all of the expected substrings with a lazily-evaluated error message,
    /// ignoring case differences.
    ///
    /// @param expected the list of substrings that must all be present in the actual string (case-insensitive)
    /// @param actual the string to search within
    /// @param supplier the supplier of the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if any expected substring is not found
    /// @see #assertStringContainsIgnoreCase(List, String)
    /// @since 1.0.0
    public static void assertStringContainsIgnoreCase(final @NonNull List<String> expected,
                                            final @NonNull String actual,
                                            final @NonNull Supplier<String> supplier) {
        AssertStringContains.assertStringContainsIgnoreCase(expected, actual, supplier);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order.
    ///
    /// This method verifies that all substrings in the expected list appear in the actual string
    /// in the same relative order they appear in the list. The substrings do not need to be
    /// adjacent - other content can appear between them.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Process flow validation
    /// String log = "System startup -> Configuration loaded -> Database connected -> Server ready";
    /// assertStringContainsInOrder(
    ///     List.of("startup", "Configuration", "Database", "ready"),
    ///     log
    /// );  // Passes - all appear in correct order
    ///
    /// // Workflow state transitions
    /// String stateLog = "PENDING -> PROCESSING -> VALIDATING -> COMPLETED";
    /// assertStringContainsInOrder(
    ///     List.of("PENDING", "PROCESSING", "COMPLETED"),
    ///     stateLog
    /// );  // Passes - order preserved (VALIDATING can be skipped)
    ///
    /// // Order violation example
    /// assertStringContainsInOrder(
    ///     List.of("end", "start", "middle"),
    ///     "start processing middle section end result"
    /// );  // Fails - "end" appears after "start" and "middle", not before
    /// ```
    ///
    /// @param expected the list of substrings that must appear in order within the actual string
    /// @param actual the string to search within
    /// @throws org.opentest4j.AssertionFailedError if substrings are not found in the specified order
    /// @see #assertStringContains(List, String)
    /// @since 1.0.0
    public static void assertStringContainsInOrder(final @NonNull List<String> expected, final @NonNull String actual) {
        AssertStringContainsInOrder.assertStringContainsInOrder(expected, actual);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order, ignoring case
    /// differences.
    ///
    /// This method verifies that all substrings in the expected list appear in the actual string
    /// in the same relative order they appear in the list, performing case-insensitive matching.
    /// The substrings do not need to be adjacent - other content can appear between them.
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Process flow validation with mixed case
    /// String log = "SYSTEM startup -> configuration LOADED -> Database Connected -> SERVER ready";
    /// assertStringContainsInOrderIgnoreCase(
    ///     List.of("startup", "CONFIGURATION", "database", "READY"),
    ///     log
    /// );  // Passes - all appear in correct order regardless of case
    ///
    /// // Workflow state transitions with case variations
    /// String stateLog = "pending -> PROCESSING -> validating -> COMPLETED";
    /// assertStringContainsInOrderIgnoreCase(
    ///     List.of("PENDING", "processing", "COMPLETED"),
    ///     stateLog
    /// );  // Passes - order preserved, case ignored
    /// ```
    ///
    /// @param expected the list of substrings that must appear in order within the actual string (case-insensitive)
    /// @param actual the string to search within
    /// @throws org.opentest4j.AssertionFailedError if substrings are not found in the specified order
    /// @see #assertStringContainsInOrder(List, String)
    /// @see #assertStringContainsIgnoreCase(List, String)
    /// @since 1.0.0
    public static void assertStringContainsInOrderIgnoreCase(final @NonNull List<String> expected,
                                                             final @NonNull String actual) {
        AssertStringContainsInOrder.assertStringContainsInOrderIgnoreCase(expected, actual);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order with a custom error
    /// message.
    ///
    /// @param expected the list of substrings that must appear in order within the actual string
    /// @param actual the string to search within
    /// @param message the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if substrings are not found in the specified order
    /// @see #assertStringContainsInOrder(List, String)
    /// @since 1.0.0
    public static void assertStringContainsInOrder(final @NonNull List<String> expected, final @NonNull String actual,
                                            final String message) {
        AssertStringContainsInOrder.assertStringContainsInOrder(expected, actual, message);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order, ignoring case
    /// differences, with a custom error message.
    ///
    /// This method verifies that all substrings from the expected list appear in the actual string
    /// in the exact order specified, performing case-insensitive matching. If the assertion fails,
    /// the provided custom message is included in the assertion error.
    ///
    /// ## Order Requirements
    ///
    /// The substrings must appear in the actual string in the same sequential order as they appear
    /// in the expected list. However:
    /// - Substrings don't need to be adjacent to each other
    /// - Other text can appear between the expected substrings
    /// - Case differences are ignored during matching
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Workflow validation with case-insensitive matching and custom message
    /// List<String> workflowSteps = List.of("start", "process", "complete");
    /// String log = "START: Beginning workflow\nPROCESS: Working on data\nCOMPLETE: Task finished";
    /// assertStringContainsInOrderIgnoreCase(
    ///     workflowSteps,
    ///     log,
    ///     "Workflow steps not found in expected order"
    /// );  // Passes - all steps found in order with case ignored
    ///
    /// // Mixed case validation with descriptive error message
    /// List<String> httpSteps = List.of("request", "authenticate", "response");
    /// String httpLog = "REQUEST received\nAUTHENTICATE user\nRESPONSE sent";
    /// assertStringContainsInOrderIgnoreCase(
    ///     httpSteps,
    ///     httpLog,
    ///     "HTTP request flow validation failed"
    /// );  // Passes - ignores case differences
    /// ```
    ///
    /// @param expected the list of substrings that must appear in order within the actual string (case-insensitive)
    /// @param actual the string to search within
    /// @param message the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if substrings are not found in the specified order
    /// @see #assertStringContainsInOrderIgnoreCase(List, String)
    /// @see #assertStringContainsInOrder(List, String, String)
    /// @since 1.0.0
    public static void assertStringContainsInOrderIgnoreCase(final @NonNull List<String> expected,
                                                             final @NonNull String actual,
                                                             final String message) {
        AssertStringContainsInOrder.assertStringContainsInOrderIgnoreCase(expected, actual, message);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order with a lazily-evaluated
    /// error message.
    ///
    /// @param expected the list of substrings that must appear in order within the actual string
    /// @param actual the string to search within
    /// @param supplier the supplier of the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if substrings are not found in the specified order
    /// @see #assertStringContainsInOrder(List, String)
    /// @since 1.0.0
    public static void assertStringContainsInOrder(final @NonNull List<String> expected,
                                            final @NonNull String actual,
                                            final @NonNull Supplier<String> supplier) {
        AssertStringContainsInOrder.assertStringContainsInOrder(expected, actual, supplier);
    }

    /// Asserts that the actual string contains all expected substrings in the specified order, ignoring case
    /// differences,
    /// with a lazily-evaluated error message.
    ///
    /// This method verifies that all substrings from the expected list appear in the actual string
    /// in the exact order specified, performing case-insensitive matching. If the assertion fails,
    /// the error message is generated by invoking the provided supplier, which allows for expensive
    /// message construction to be deferred until actually needed.
    ///
    /// ## Order Requirements
    ///
    /// The substrings must appear in the actual string in the same sequential order as they appear
    /// in the expected list. However:
    /// - Substrings don't need to be adjacent to each other
    /// - Other text can appear between the expected substrings
    /// - Case differences are ignored during matching
    ///
    /// ## Examples
    ///
    /// ```java
    /// // Complex workflow validation with expensive debugging information
    /// List<String> processSteps = List.of("init", "validate", "execute", "cleanup");
    /// String executionLog = getComplexExecutionLog();  // Expensive operation
    /// assertStringContainsInOrderIgnoreCase(
    ///     processSteps,
    ///     executionLog,
    ///     () -> "Process execution failed. Expected steps: " + processSteps +
    ///           ", Full log: " + executionLog + ", System state: " + getSystemState()
    /// );  // Supplier only called if assertion fails
    ///
    /// // Service communication validation with case-insensitive matching
    /// List<String> serviceFlow = List.of("REQUEST", "auth", "PROCESS", "response");
    /// String serviceLog = "request started\nAUTH validated\process completed\nRESPONSE sent";
    /// assertStringContainsInOrderIgnoreCase(
    ///     serviceFlow,
    ///     serviceLog,
    ///     () -> "Service communication flow incorrect for: " + getCurrentServiceContext()
    /// );  // Passes - ignores case differences
    /// ```
    ///
    /// @param expected the list of substrings that must appear in order within the actual string (case-insensitive)
    /// @param actual the string to search within
    /// @param supplier the supplier of the custom message to include if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if substrings are not found in the specified order
    /// @see #assertStringContainsInOrderIgnoreCase(List, String)
    /// @see #assertStringContainsInOrder(List, String, Supplier)
    /// @since 1.0.0
    public static void assertStringContainsInOrderIgnoreCase(final @NonNull List<String> expected,
                                                   final @NonNull String actual,
                                                   final @NonNull Supplier<String> supplier) {
        AssertStringContainsInOrder.assertStringContainsInOrderIgnoreCase(expected, actual, supplier);
    }

    /// Enables deadlock detection.
    ///
    /// When enabled, certain assertions can check for deadlocked threads in the JVM.
    ///
    /// @see #assertNoDeadlocks()
    /// @since 1.0.0
    public static void enableDeadlockDetection() {
        AssertNoDeadlocks.enableDeadlockDetection();
    }

    /// Asserts that no threads are currently deadlocked.
    ///
    /// @throws AssertionError if one or more deadlocked threads are detected.
    /// @see AssertNoDeadlocks#assertNoDeadlocks()
    public static void assertNoDeadlocks() {
        AssertNoDeadlocks.assertNoDeadlocks();
    }

    /// Asserts that no threads are currently deadlocked, with a custom error message.
    ///
    /// @param message the detail message for the [AssertionError]; may be null
    /// @throws AssertionError if one or more deadlocked threads are detected.
    /// @see AssertNoDeadlocks#assertNoDeadlocks(String)
    public static void assertNoDeadlocks(final String message) {
        AssertNoDeadlocks.assertNoDeadlocks(message);
    }

    /// Asserts that no threads are currently deadlocked, with a lazily-supplied error message.
    ///
    /// @param messageSupplier the supplier for the detail message of the [AssertionError];
    ///                        must not be null
    /// @throws AssertionError if one or more deadlocked threads are detected.
    /// @throws NullPointerException if messageSupplier is null
    /// @see AssertNoDeadlocks#assertNoDeadlocks(Supplier)
    public static void assertNoDeadlocks(final @NonNull Supplier<String> messageSupplier) {
        AssertNoDeadlocks.assertNoDeadlocks(messageSupplier);
    }
}
