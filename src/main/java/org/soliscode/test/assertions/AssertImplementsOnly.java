package org.soliscode.test.assertions;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.soliscode.test.util.CollectionTestOps.toCSVString;

/// A collection of utility methods that support asserting that an object is an instance of a class that implements
/// interfaces only from a set of permitted interfaces.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class AssertImplementsOnly {

    private AssertImplementsOnly() {}

    static void assertImplementsOnly(final Collection<Class<?>> expected, final Object actual) {
        checkImplementsOnly(expected, actual, null);
    }

    static void assertImplementsOnly(final Collection<Class<?>> expected, final Object actual,
                                            final String message) {
        checkImplementsOnly(expected, actual, message);
    }

    static void assertImplementsOnly(final Collection<Class<?>> expected, final Object actual,
                                            final Supplier<String> messageSupplier) {
        checkImplementsOnly(expected, actual, messageSupplier);
    }

    private static void checkImplementsOnly(final Collection<Class<?>> expected, final Object actual,
                                            final Object messageOrSupplier) {

        Class<?>[] interfaces = actual.getClass().getInterfaces();
        if (!expected.containsAll(Arrays.asList(interfaces))) {
            failImplementsOnly(expected, actual, messageOrSupplier);
        }
    }

    private static void failImplementsOnly(Collection<Class<?>> expected, Object actual, Object messageOrSupplier) {
        if (messageOrSupplier == null) {
            Collection<Class<?>> unexpectedInterfaces = new ArrayList<>(Arrays.asList(actual.getClass().getInterfaces()));
            unexpectedInterfaces.removeAll(expected);
            messageOrSupplier = actual.getClass() + " is expected to only be an instance of " + toCSVString(expected) +
                    " but is an instance of " + toCSVString(unexpectedInterfaces);
        }
        assertionFailure()
                .message(messageOrSupplier)
                .expected(expected)
                .actual(actual)
                .buildAndThrow();
    }
}
