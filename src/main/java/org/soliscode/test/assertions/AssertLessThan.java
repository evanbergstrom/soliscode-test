package org.soliscode.test.assertions;

import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

public final class AssertLessThan {

    /// A collection of utility methods that support asserting that a value is less than another value.
    private AssertLessThan() {}

    public static <T extends Comparable<T>> void assertLessThan(T first, T second) {
        if (second.compareTo(first) >= 0) {
            failLessThan(first, second, expectedLessThanMessage(first, second));
        }
    }

    public static <T extends Comparable<T>> void assertLessThan(T first, T second, String message) {
        if (second.compareTo(first) >= 0) {
            failLessThan(first, second, message);
        }
    }

    public static <T extends Comparable<T>> void assertLessThan(T first, T second, final Supplier<String> messageSupplier) {
        if (second.compareTo(first) >= 0) {
            failLessThan(first, second, messageSupplier);
        }
    }

    private static String expectedLessThanMessage(Object first, Object second) {
        return "Expected " + second + " to be less than " + first;
    }

    private static void failLessThan(Object first, Object second, Object messageOrSupplier) {
        assertionFailure()
                .message(messageOrSupplier)
                .expected(first)
                .actual(second)
                .buildAndThrow();
    }
}
