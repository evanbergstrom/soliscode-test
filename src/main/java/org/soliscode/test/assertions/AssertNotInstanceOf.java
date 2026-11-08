package org.soliscode.test.assertions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that an object is not an instance of a class.
/// This class provides assertions for type checking that verify an object does NOT belong to a specific class
/// or implement a specific interface. All methods in this class will throw an
/// {@link org.opentest4j.AssertionFailedError}
/// if the assertion fails (i.e., if the object IS an instance of the specified type).
///
/// @author evanbergstrom
/// @see java.lang.Class#isInstance(Object)
/// @see org.junit.jupiter.api.Assertions
/// @since 1.0.0
public final class AssertNotInstanceOf {

    private AssertNotInstanceOf() { }

    /// Asserts that the given object is not an instance of the specified class or interface.
    /// This method uses {@link Class#isInstance(Object)} to perform the type check.
    /// The assertion fails if the object IS an instance of the expected type.
    /// ```java
    /// // Example with different types - these pass
    /// assertNotInstanceOf(String.class, 42);           // Integer is not a String
    /// assertNotInstanceOf(List.class, "hello");        // String is not a List
    /// assertNotInstanceOf(Number.class, "text");       // String is not a Number
    ///
    /// // Example with null - this passes (null is not an instance of any class)
    /// assertNotInstanceOf(String.class, null);         // null is not a String
    ///
    /// // Example with inheritance hierarchy
    /// Object obj = new ArrayList<>();
    /// assertNotInstanceOf(HashSet.class, obj);         // ArrayList is not a HashSet
    ///
    /// // Examples that would fail
    /// assertNotInstanceOf(String.class, "hello");      // Throws AssertionFailedError: String IS a String
    /// assertNotInstanceOf(Number.class, 42);           // Throws AssertionFailedError: Integer IS a Number
    /// assertNotInstanceOf(Collection.class, new ArrayList<>()); // ArrayList IS a Collection
    /// ```
    ///
    /// @param expectedType the type that the object should NOT be an instance of
    /// @param actual the object to check
    /// @throws org.opentest4j.AssertionFailedError if {@code actual} is an instance of {@code expectedType}
    /// @throws NullPointerException if {@code expectedType} is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0.0
    static void assertNotInstanceOf(final @NonNull Class<?> expectedType, final @NonNull Object actual) {
        assertNotInstanceOf(expectedType, actual, (Object) null);
    }

    /// Asserts that the given object is not an instance of the specified class or interface.
    /// This method uses {@link Class#isInstance(Object)} to perform the type check.
    /// The assertion fails if the object IS an instance of the expected type.
    /// ```java
    /// // Example with custom error message
    /// assertNotInstanceOf(String.class, 42, "Expected a non-string value");
    /// // Passes: Integer is not a String
    ///
    /// // Example that would fail with custom message
    /// assertNotInstanceOf(Number.class, 42, "Value should not be numeric");
    /// // Throws AssertionFailedError with message: "Value should not be numeric"
    ///
    /// // Example with inheritance checking
    /// Object list = new ArrayList<>();
    /// assertNotInstanceOf(HashSet.class, list, "List should not be a HashSet");
    /// // Passes: ArrayList is not a HashSet
    /// ```
    ///
    /// @param expectedType the type that the object should NOT be an instance of
    /// @param actual the object to check
    /// @param message the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if {@code actual} is an instance of {@code expectedType}
    /// @throws NullPointerException if {@code expectedType} is null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0.0
    static void assertNotInstanceOf(final @NonNull Class<?> expectedType, @NonNull final Object actual,
                                    final @Nullable String message) {
        assertNotInstanceOf(expectedType, actual, (Object) message);
    }

    /// Asserts that the given object is not an instance of the specified class or interface.
    /// This method uses {@link Class#isInstance(Object)} to perform the type check.
    /// The assertion fails if the object IS an instance of the expected type.
    /// ```java
    /// // Example with message supplier (lazy evaluation)
    /// assertNotInstanceOf(String.class, someObject,
    ///     () -> "Object " + someObject + " should not be a String");
    /// // Message is only computed if assertion fails
    ///
    /// // Example with expensive message computation
    /// assertNotInstanceOf(ExpensiveType.class, result,
    ///     () -> buildDetailedTypeErrorMessage(result));
    /// // Message computation is deferred until needed
    ///
    /// // Example that demonstrates lazy evaluation benefit
    /// Object value = getValue();
    /// assertNotInstanceOf(String.class, value,
    ///     () -> "Expected non-string but got: " + value.getClass().getSimpleName());
    /// // Class name lookup only happens if assertion fails
    /// ```
    ///
    /// @param expectedType the type that the object should NOT be an instance of
    /// @param actual the object to check
    /// @param messageSupplier the supplier of the message to include in the exception if the assertion fails
    /// @throws org.opentest4j.AssertionFailedError if {@code actual} is an instance of {@code expectedType}
    /// @throws NullPointerException if {@code expectedType} s null
    ///
    /// @see java.lang.Class#isInstance(Object)
    /// @since 1.0.0
    static void assertNotInstanceOf(final @NonNull Class<?> expectedType, final @NonNull Object actual,
                                    final @Nullable Supplier<String> messageSupplier) {
        assertNotInstanceOf(expectedType, actual, (Object) messageSupplier);
    }

    private static void assertNotInstanceOf(final Class<?> expectedType, final Object actual,
                                            final Object messageOrSupplier) {
        if (expectedType.isInstance(actual)) {
            assertionFailure()
                    .message(messageOrSupplier)
                    .reason("Unexpected type")
                    .expected(expectedType)
                    .actual(actual.getClass()) //
                    .buildAndThrow();
        }
    }
}
