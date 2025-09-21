package org.soliscode.test.assertions;

import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that a value is less than another value.
/// This class provides assertions for comparing {@link Comparable} objects using their natural ordering.
/// All methods in this class will throw an {@link org.opentest4j.AssertionFailedError} if the assertion fails.
///
/// @author evanbergstrom
/// @see java.lang.Comparable
/// @see org.junit.jupiter.api.Assertions
/// @since 1.0.0
public final class AssertLessThan {

    private AssertLessThan() { }

    /// Asserts that the first comparable value is less than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking {@code second.compareTo(first)}. The assertion fails if the first value
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
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than the second value
    /// @param second the value that the first value should be less than
    /// @throws org.opentest4j.AssertionFailedError if {@code first >= second}
    /// @throws NullPointerException if either parameter is null
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThan(final @NonNull T first, final @NonNull T second) {
        if (first.compareTo(second) >= 0) {
            failLessThan(first, second, expectedLessThanMessage(first, second));
        }
    }

    /// Asserts that the first comparable value is less than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking {@code second.compareTo(first)}. The assertion fails if the first value
    /// is greater than or equal to the second value.
    /// ```java
    /// // Example with custom error message
    /// assertLessThan(10, 5, "Value should be smaller");
    /// // Throws AssertionFailedError with message: "Value should be smaller"
    ///
    /// // Example that passes
    /// assertLessThan(1, 2, "First must be less than second"); // Passes
    /// ```
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than the second value
    /// @param second the value that the first value should be less than
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if {@code first >= second}
    /// @throws NullPointerException if either comparable parameter is null
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThan(final @NonNull T first, final @NonNull T second,
                                                                final String message) {
        if (first.compareTo(second) >= 0) {
            failLessThan(first, second, message);
        }
    }

    /// Asserts that the first comparable value is less than the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking {@code second.compareTo(first)}. The assertion fails if the first value
    /// is greater than or equal to the second value.
    /// ```java
    /// // Example with message supplier (lazy evaluation)
    /// assertLessThan(10, 5, () -> "Expected " + 10 + " < " + 5);
    /// // Throws AssertionFailedError with dynamically generated message
    ///
    /// // Example with expensive message computation
    /// assertLessThan(score, threshold, () -> buildDetailedErrorMessage(score, threshold));
    /// // Message is only computed if assertion fails
    /// ```
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than the second value
    /// @param second the value that the first value should be less than
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if {@code first >= second}
    /// @throws NullPointerException if either comparable parameter is null
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThan(final @NonNull T first, final @NonNull T second,
                                                                final Supplier<String> messageSupplier) {
        if (first.compareTo(second) >= 0) {
            failLessThan(first, second, messageSupplier);
        }
    }

    private static String expectedLessThanMessage(final Object first, final Object second) {
        return "Expected " + first + " to be less than " + second;
    }

    private static void failLessThan(final Object first, final Object second, final Object messageOrSupplier) {
        assertionFailure()
                .message(messageOrSupplier)
                .expected(first)
                .actual(second)
                .buildAndThrow();
    }
}
