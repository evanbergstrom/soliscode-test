package org.soliscode.test.assertions;

import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A collection of utility methods that support asserting that an object is not an instance of a class.
///
/// @author evanbergstrom
/// @since 1.0.0
public class AssertNotInstanceOf {

    private AssertNotInstanceOf() {}

    static void assertNotInstanceOf(final Class<?> expectedType, final Object actual) {
        assertNotInstanceOf(expectedType, actual, (Object) null);
    }

    static void assertNotInstanceOf(final Class<?> expectedType, final Object actual, final String message) {
        assertNotInstanceOf(expectedType, actual, (Object) message);
    }

    static void assertNotInstanceOf(final Class<?> expectedType, final Object actual, final Supplier<String> messageSupplier) {
        assertNotInstanceOf(expectedType, actual, (Object) messageSupplier);
    }

    private static void assertNotInstanceOf(final Class<?> expectedType, final Object actual, final Object messageOrSupplier) {
        if (expectedType.isInstance(actual)) {
            assertionFailure()
                    .message(messageOrSupplier)
                    .reason(actual == null ? "Unexpected null value" : "Unexpected type")
                    .expected(expectedType)
                    .actual(actual == null ? null : actual.getClass()) //
                    .buildAndThrow();
        }
    }
}
