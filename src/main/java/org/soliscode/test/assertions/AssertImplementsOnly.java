package org.soliscode.test.assertions;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.soliscode.test.util.CollectionTestUtils.toCSVString;

/// A collection of utility methods that support asserting that an object is an instance of a class that implements
/// interfaces only from a set of permitted interfaces.
///
/// This utility class provides static methods to validate that an object's class implements only
/// the interfaces that are explicitly expected and permitted. This is particularly useful in testing
/// scenarios where you want to ensure that implementations don't inadvertently implement additional
/// interfaces that could affect behavior or violate design constraints.
///
/// ## Purpose
/// The primary purpose of this class is to validate interface compliance in testing scenarios.
/// For example, when testing a collection implementation, you might want to ensure it only implements
/// the expected collection interfaces and doesn't accidentally implement additional marker interfaces
/// or other contracts that could change its behavior.
///
/// ## Key Features
/// - **Interface Validation** - Verifies that objects implement only expected interfaces
/// - **Custom Error Messages** - Supports custom error messages and message suppliers
/// - **Detailed Failure Information** - Provides clear information about unexpected interfaces
/// - **JUnit Integration** - Uses JUnit's assertion failure mechanism for proper test integration
///
/// ## Usage Examples
/// ```java
/// // Test that an ArrayList only implements expected Collection interfaces
/// List<String> list = new ArrayList<>();
/// Collection<Class<?>> expectedInterfaces = Set.of(
///     List.class,
///     Collection.class,
///     Iterable.class,
///     Serializable.class,
///     Cloneable.class,
///     RandomAccess.class
/// );
/// AssertImplementsOnly.assertImplementsOnly(expectedInterfaces, list);
///
/// // With custom error message
/// AssertImplementsOnly.assertImplementsOnly(
///     expectedInterfaces,
///     list,
///     "ArrayList should only implement standard collection interfaces"
/// );
/// ```
///
/// ## Error Handling
/// When an assertion fails, the methods throw an {@link org.opentest4j.AssertionFailedError}
/// with detailed information about:
/// - The expected interfaces that are permitted
/// - The unexpected interfaces that were found
/// - The actual object and its class
///
/// ## Thread Safety
/// This utility class is thread-safe as all methods are static and stateless.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see org.opentest4j.AssertionFailedError
/// @see java.lang.Class#getInterfaces()
/// @see org.junit.jupiter.api.Assertions
public final class AssertImplementsOnly {

    /// Private constructor to prevent instantiation of this utility class.
    private AssertImplementsOnly() { }


    static void assertImplementsOnly(final @NonNull Class<?> expected, final @NonNull Object actual) {
        Set<Class<?>> allExpected = ReflectionTestUtils.getAllInterfaces(expected);
        checkImplementsOnly(allExpected, actual, null);
    }

    /// Asserts that the given object's class implements only the interfaces from the expected collection.
    /// This method validates that the object's class does not implement any interfaces beyond those
    /// explicitly permitted in the expected collection.
    ///
    /// The assertion succeeds if and only if every interface implemented by the object's class
    /// is contained within the expected collection of interfaces. If the object implements any
    /// additional interfaces not present in the expected collection, an {@link org.opentest4j.AssertionFailedError}
    /// is thrown with detailed information about the unexpected interfaces.
    ///
    /// ### Example Usage
    /// ```java
    /// List<String> list = new ArrayList<>();
    /// Collection<Class<?>> expectedInterfaces = Set.of(
    ///     List.class, Collection.class, Iterable.class,
    ///     Serializable.class, Cloneable.class, RandomAccess.class
    /// );
    /// AssertImplementsOnly.assertImplementsOnly(expectedInterfaces, list);
    /// ```
    ///
    /// @param expected the collection of interface classes that are permitted to be implemented
    ///                 by the actual object's class. Must not be {@code null}.
    /// @param actual the object whose class will be checked for interface compliance.
    ///               Must not be {@code null}.
    /// @throws org.opentest4j.AssertionFailedError if the actual object's class implements
    ///         any interfaces not present in the expected collection
    /// @throws NullPointerException if either {@code expected} or {@code actual} is {@code null}
    /// @see #assertImplementsOnly(Collection, Object, String)
    /// @see #assertImplementsOnly(Collection, Object, Supplier)
    static void assertImplementsOnly(final Collection<Class<?>> expected, final Object actual) {
        Set<Class<?>> allExpected = ReflectionTestUtils.getAllInterfaces(expected);
        checkImplementsOnly(allExpected, actual, null);
    }

    static void assertImplementsOnly(final @NonNull Class<?> expected, final @NonNull Object actual,
                                     final String message) {
        Set<Class<?>> allExpected = ReflectionTestUtils.getAllInterfaces(expected);
        checkImplementsOnly(allExpected, actual, message);
    }

    /// Asserts that the given object's class implements only the interfaces from the expected collection,
    /// with a custom error message.
    /// This method validates that the object's class does not implement any interfaces beyond those
    /// explicitly permitted in the expected collection, using the provided message when the assertion fails.
    ///
    /// The assertion succeeds if and only if every interface implemented by the object's class
    /// is contained within the expected collection of interfaces. If the object implements any
    /// additional interfaces not present in the expected collection, an {@link org.opentest4j.AssertionFailedError}
    /// is thrown using the provided custom message.
    ///
    /// ### Example Usage
    /// ```java
    /// List<String> list = new ArrayList<>();
    /// Collection<Class<?>> expectedInterfaces = Set.of(
    ///     List.class, Collection.class, Iterable.class
    /// );
    /// AssertImplementsOnly.assertImplementsOnly(
    ///     expectedInterfaces,
    ///     list,
    ///     "ArrayList should only implement core collection interfaces"
    /// );
    /// ```
    ///
    /// @param expected the collection of interface classes that are permitted to be implemented
    ///                 by the actual object's class. Must not be {@code null}.
    /// @param actual the object whose class will be checked for interface compliance.
    ///               Must not be {@code null}.
    /// @param message the custom error message to use when the assertion fails.
    ///                May be {@code null}, in which case a default message is generated.
    /// @throws org.opentest4j.AssertionFailedError if the actual object's class implements
    ///         any interfaces not present in the expected collection
    /// @throws NullPointerException if either {@code expected} or {@code actual} is {@code null}
    /// @see #assertImplementsOnly(Collection, Object)
    /// @see #assertImplementsOnly(Collection, Object, Supplier)
    static void assertImplementsOnly(final @NonNull Collection<Class<?>> expected, final @NonNull Object actual,
                                            final String message) {
        Set<Class<?>> allExpected = ReflectionTestUtils.getAllInterfaces(expected);
        checkImplementsOnly(allExpected, actual, message);
    }

    static void assertImplementsOnly(final @NonNull Class<?> expected, final @NonNull Object actual,
                                     final Supplier<String> messageSupplier) {
        Set<Class<?>> allExpected = ReflectionTestUtils.getAllInterfaces(expected);
        checkImplementsOnly(allExpected, actual, messageSupplier);
    }

    /// Asserts that the given object's class implements only the interfaces from the expected collection,
    /// with a lazy-evaluated custom error message.
    /// This method validates that the object's class does not implement any interfaces beyond those
    /// explicitly permitted in the expected collection, using a message supplier for lazy message evaluation.
    ///
    /// The assertion succeeds if and only if every interface implemented by the object's class
    /// is contained within the expected collection of interfaces. If the object implements any
    /// additional interfaces not present in the expected collection, an {@link org.opentest4j.AssertionFailedError}
    /// is thrown using the message provided by the supplier.
    ///
    /// ### Lazy Message Evaluation
    /// The message supplier is only called if the assertion fails, which can be more efficient
    /// when message generation is expensive. This is particularly useful when the error message
    /// needs to include runtime information or computationally expensive details.
    ///
    /// ### Example Usage
    /// ```java
    /// List<String> list = new ArrayList<>();
    /// Collection<Class<?>> expectedInterfaces = Set.of(
    ///     List.class, Collection.class, Iterable.class
    /// );
    /// AssertImplementsOnly.assertImplementsOnly(
    ///     expectedInterfaces,
    ///     list,
    ///     () -> "Expected " + list.getClass().getSimpleName() +
    ///           " to implement only: " + expectedInterfaces
    /// );
    /// ```
    ///
    /// @param expected the collection of interface classes that are permitted to be implemented
    ///                 by the actual object's class. Must not be {@code null}.
    /// @param actual the object whose class will be checked for interface compliance.
    ///               Must not be {@code null}.
    /// @param messageSupplier the supplier that provides the custom error message when the assertion fails.
    ///                        May be {@code null}, in which case a default message is generated.
    ///                        The supplier is only called if the assertion fails.
    /// @throws org.opentest4j.AssertionFailedError if the actual object's class implements
    ///         any interfaces not present in the expected collection
    /// @throws NullPointerException if either {@code expected} or {@code actual} is {@code null}
    /// @see #assertImplementsOnly(Collection, Object)
    /// @see #assertImplementsOnly(Collection, Object, String)
    static void assertImplementsOnly(final Collection<Class<?>> expected, final Object actual,
                                            final Supplier<String> messageSupplier) {
        Set<Class<?>> allExpected = ReflectionTestUtils.getAllInterfaces(expected);
        checkImplementsOnly(allExpected, actual, messageSupplier);
    }

    /// Core implementation method that performs the interface compliance check.
    /// This method inspects the actual object's class to determine what interfaces it implements
    /// and verifies that all implemented interfaces are present in the expected collection.
    ///
    /// The method uses {@link Class#getInterfaces()} to obtain the direct interfaces
    /// implemented by the object's class, then checks if the expected collection contains
    /// all of these interfaces. If any interface is not found in the expected collection,
    /// the method delegates to {@link #failImplementsOnly} to generate and throw an appropriate
    /// assertion failure.
    ///
    /// @param expected the collection of permitted interface classes
    /// @param actual the object whose class interfaces will be validated
    /// @param messageOrSupplier either a String message, a Supplier<String>, or null for default messaging
    /// @see Class#getInterfaces()
    /// @see #failImplementsOnly(Collection, Object, Object)
    private static void checkImplementsOnly(final @NonNull Collection<Class<?>> expected, final @NonNull Object actual,
                                            final Object messageOrSupplier) {

        Set<Class<?>> interfaces = ReflectionTestUtils.getAllInterfaces(actual.getClass());
        if (!expected.containsAll(interfaces)) {
            failImplementsOnly(expected, actual, messageOrSupplier);
        }
    }

    /// Constructs and throws an AssertionFailedError when interface compliance validation fails.
    /// This method handles the creation of detailed error messages and the proper construction
    /// of JUnit's AssertionFailedError with expected and actual values.
    ///
    /// The method generates comprehensive error messages that include:
    /// - The class name of the object being tested
    /// - The list of expected (permitted) interfaces
    /// - The list of unexpected interfaces that were found
    ///
    /// If a custom message or message supplier is provided, it will be used instead of
    /// the generated default message. The method handles both String messages and Supplier<String>
    /// instances, calling the supplier only when needed for lazy evaluation.
    ///
    /// @param expected the collection of permitted interface classes for context in the error
    /// @param actual the object that failed interface compliance for context in the error
    /// @param messageOrSupplier either a custom String message, a Supplier<String> for lazy evaluation,
    ///                          or null to generate a default descriptive message
    /// @throws org.opentest4j.AssertionFailedError always, with detailed information about the failure
    /// @see org.junit.jupiter.api.AssertionFailureBuilder
    /// @see org.soliscode.test.util.CollectionTestUtils#toCSVString(Collection)
    private static void failImplementsOnly(final @NonNull Collection<Class<?>> expected, final @NonNull Object actual,
                                           final Object messageOrSupplier) {
        Object message = messageOrSupplier;
        if (messageOrSupplier == null) {
            Set<Class<?>> unexpectedInterfaces = ReflectionTestUtils.getAllInterfaces(actual.getClass());
            unexpectedInterfaces.removeAll(expected);
            message = actual.getClass() + " is expected to only be an instance of " + toCSVString(expected)
                    + " but is an instance of " + toCSVString(unexpectedInterfaces);
        }
        assertionFailure()
                .message(message)
                .expected(expected)
                .actual(actual)
                .buildAndThrow();
    }
}
