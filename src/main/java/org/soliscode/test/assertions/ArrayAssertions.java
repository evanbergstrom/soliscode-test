package org.soliscode.test.assertions;

import org.junit.jupiter.api.Assertions;

/// A collection of utility methods that support asserting conditions on arrays in tests.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class ArrayAssertions {

    private ArrayAssertions() {}

    /// Assert that an array has an expected length.
    /// @param expected the expected length of the array
    /// @param actual the actual array
    public static void assertLengthEquals(final int expected, final Object[] actual) {
        Assertions.assertEquals(expected, actual.length);
    }
}
