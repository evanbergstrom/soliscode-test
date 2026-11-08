package org.soliscode.test.assertions;

import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that a value is less than or equal to another value.
/// This class provides assertions for comparing {@link Comparable} objects using their natural ordering.
/// All methods in this class will throw an {@link org.opentest4j.AssertionFailedError} if the assertion fails.
///
/// @author evanbergstrom
/// @see java.lang.Comparable
/// @see org.junit.jupiter.api.Assertions
/// @since 1.0.0
public final class AssertLessThanOrEqual {

    private AssertLessThanOrEqual() { }

    /// Asserts that the first comparable value is less than or equal to the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking {@code first.compareTo(second)}. The assertion fails if the first value
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
    /// // Example with doubles
    /// assertLessThanOrEqual(2.5, 3.0); // Passes: 2.5 <= 3.0
    /// assertLessThanOrEqual(3.0, 3.0); // Passes: 3.0 <= 3.0
    ///
    /// // Example that would fail
    /// assertLessThanOrEqual(10, 5); // Throws AssertionFailedError: 10 > 5
    /// ```
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than or equal to the second value
    /// @param second the value that the first value should be less than or equal to
    /// @throws org.opentest4j.AssertionFailedError if {@code first > second}
    /// @throws NullPointerException if either parameter is null
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThanOrEqual(final @NonNull T first,
                                                                       final @NonNull T second) {
        if (first.compareTo(second) > 0) {
            failLessThanOrEqual(first, second, expectedLessThanOrEqualMessage(first, second));
        }
    }

    /// Asserts that the first comparable value is less than or equal to the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking {@code first.compareTo(second)}. The assertion fails if the first value
    /// is greater than the second value.
    /// ```java
    /// // Example with custom error message
    /// assertLessThanOrEqual(10, 5, "Value should be smaller or equal");
    /// // Throws AssertionFailedError with message: "Value should be smaller or equal"
    ///
    /// // Example that passes
    /// assertLessThanOrEqual(1, 2, "First must be less than or equal to second"); // Passes
    /// assertLessThanOrEqual(2, 2, "First must be less than or equal to second"); // Passes
    /// ```
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than or equal to the second value
    /// @param second the value that the first value should be less than or equal to
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if {@code first > second}
    /// @throws NullPointerException if either comparable parameter is null
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThanOrEqual(final @NonNull T first, final @NonNull T second,
                                                                       final String message) {
        if (first.compareTo(second) > 0) {
            failLessThanOrEqual(first, second, message);
        }
    }

    /// Asserts that the first comparable value is less than or equal to the second comparable value.
    /// This method performs a comparison using the natural ordering of the comparable objects
    /// by invoking {@code first.compareTo(second)}. The assertion fails if the first value
    /// is greater than the second value.
    /// ```java
    /// // Example with message supplier (lazy evaluation)
    /// assertLessThanOrEqual(10, 5, () -> "Expected " + 10 + " <= " + 5);
    /// // Throws AssertionFailedError with dynamically generated message
    ///
    /// // Example with expensive message computation
    /// assertLessThanOrEqual(score, threshold, () -> buildDetailedErrorMessage(score, threshold));
    /// // Message is only computed if assertion fails
    /// ```
    ///
    /// @param <T> the type of comparable values being compared
    /// @param first the value that should be less than or equal to the second value
    /// @param second the value that the first value should be less than or equal to
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if {@code first > second}
    /// @throws NullPointerException if either comparable parameter is null
    ///
    /// @see java.lang.Comparable#compareTo(Object)
    /// @since 1.0.0
    public static <T extends Comparable<T>> void assertLessThanOrEqual(final @NonNull T first, final @NonNull T second,
                                                                       final Supplier<String> messageSupplier) {
        if (first.compareTo(second) > 0) {
            failLessThanOrEqual(first, second, messageSupplier);
        }
    }

    private static String expectedLessThanOrEqualMessage(final Object first, final Object second) {
        return "Expected " + first + " to be less than or equal to " + second;
    }

    private static void failLessThanOrEqual(final Object first, final Object second, final Object messageOrSupplier) {
        assertionFailure()
                .message(messageOrSupplier)
                .expected(first)
                .actual(second)
                .buildAndThrow();
    }
}

