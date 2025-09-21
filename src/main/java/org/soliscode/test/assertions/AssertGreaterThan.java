package org.soliscode.test.assertions;

import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that a value is greater than another value.
/// This class provides assertions for comparing {@link Comparable} objects using their natural ordering.
/// All methods in this class will throw an {@link org.opentest4j.AssertionFailedError} if the assertion fails.
///
/// @author evanbergstrom
/// @see java.lang.Comparable
/// @see org.junit.jupiter.api.Assertions
/// @since 1.0.0
public final class AssertGreaterThan {

    private AssertGreaterThan() { }

    /// Asserts that the first comparable value is greater than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking {@code first.compareTo(second)}. The assertion fails if the first value
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
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be greater than the second value
    /// @param second the value that the first value should be greater than
    /// @throws org.opentest4j.AssertionFailedError if {@code first <= second}
    /// @throws NullPointerException if either parameter is null
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertGreaterThan(final @NonNull T first, final @NonNull T second) {
        if (first.compareTo(second) <= 0) {
            failGreaterThan(first, second, expectedGreaterThanMessage(first, second));
        }
    }

    /// Asserts that the first comparable value is greater than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking {@code first.compareTo(second)}. The assertion fails if the first value
    /// is less than or equal to the second value.
    /// ```java
    /// // Example with custom error message
    /// assertGreaterThan(5, 10, "Value should be larger");
    /// // Throws AssertionFailedError with message: "Value should be larger"
    ///
    /// // Example that passes
    /// assertGreaterThan(2, 1, "First must be greater than second"); // Passes
    /// ```
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be greater than the second value
    /// @param second the value that the first value should be greater than
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if {@code first <= second}
    /// @throws NullPointerException if either comparable parameter is null
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertGreaterThan(final @NonNull T first, final @NonNull T second,
                                                                   final String message) {
        if (first.compareTo(second) <= 0) {
            failGreaterThan(first, second, message);
        }
    }

    /// Asserts that the first comparable value is greater than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking {@code first.compareTo(second)}. The assertion fails if the first value
    /// is less than or equal to the second value.
    /// ```java
    /// // Example with message supplier (lazy evaluation)
    /// assertGreaterThan(5, 10, () -> "Expected " + 5 + " > " + 10);
    /// // Throws AssertionFailedError with dynamically generated message
    ///
    /// // Example with expensive message computation
    /// assertGreaterThan(score, threshold, () -> buildDetailedErrorMessage(score, threshold));
    /// // Message is only computed if assertion fails
    /// ```
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be greater than the second value
    /// @param second the value that the first value should be greater than
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if {@code first <= second}
    /// @throws NullPointerException if either comparable parameter is null
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertGreaterThan(final @NonNull T first, final @NonNull T second,
                                                                   final Supplier<String> messageSupplier) {
        if (first.compareTo(second) <= 0) {
            failGreaterThan(first, second, messageSupplier);
        }
    }

    private static String expectedGreaterThanMessage(final Object first, final Object second) {
        return "Expected " + first + " to be greater than " + second;
    }

    private static void failGreaterThan(final Object first, final Object second, final Object messageOrSupplier) {
        assertionFailure()
                .message(messageOrSupplier)
                .expected(first)
                .actual(second)
                .buildAndThrow();
    }
}
