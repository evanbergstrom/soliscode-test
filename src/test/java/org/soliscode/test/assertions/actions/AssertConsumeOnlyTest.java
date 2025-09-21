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

package org.soliscode.test.assertions.actions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.util.UsesCollections;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/// Test class for {@link AssertConsumeOnly} assertion consumer.
/// This class provides comprehensive test coverage for the AssertConsumeOnly class,
/// which is used to assert that a consumer only accepts values from a specified set.
///
/// The tests cover all constructors, the core accept() functionality, edge cases,
/// error handling, and integration scenarios with various collection types.
///
/// @author evanbergstrom
/// @since 1.0
/// @see AssertConsumeOnly
/// @see AssertActions
@DisplayName("Tests for AssertConsumeOnly")
public class AssertConsumeOnlyTest implements UsesCollections {

    /// Test message used for custom error message validation.
    private static final String TEST_MESSAGE = "Test message";

    /// Standard set of expected integer values used across multiple tests.
    private static final List<Integer> EXPECTED_VALUES = List.of(1, 2, 3);

    /// Standard set of expected string values used for type diversity testing.
    private static final Set<String> EXPECTED_STRINGS = Set.of("a", "b", "c");

    /// Tests the basic constructor that takes only expected values.
    /// Verifies that the consumer correctly accepts values from the expected set
    /// and throws AssertionFailedError for values not in the expected set.
    @DisplayName("Test constructor with expected values only")
    @Test
    void testConstructorWithExpectedOnly() {
        AssertConsumeOnly<Integer> consumer = AssertActions.consumeOnly(EXPECTED_VALUES);

        // Should accept values from expected set
        assertDoesNotThrow(() -> consumer.accept(1));
        assertDoesNotThrow(() -> consumer.accept(2));
        assertDoesNotThrow(() -> consumer.accept(3));

        // Should throw for values not in expected set
        assertThrows(AssertionFailedError.class, () -> consumer.accept(4));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(0));
    }

    /// Tests the constructor that takes expected values and a custom error message.
    /// Verifies that the custom message appears in the exception when assertion fails.
    @DisplayName("Test constructor with expected values and message")
    @Test
    void testConstructorWithExpectedAndMessage() {
        AssertConsumeOnly<Integer> consumer = AssertActions.consumeOnly(EXPECTED_VALUES, TEST_MESSAGE);

        // Should accept values from expected set
        assertDoesNotThrow(() -> consumer.accept(1));

        // Should throw for values not in expected set with custom message
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> consumer.accept(4));
        assertTrue(error.getMessage().contains(TEST_MESSAGE));
    }

    /// Tests the constructor that takes expected values and a message supplier.
    /// Verifies that the supplied message appears in the exception when assertion fails,
    /// demonstrating lazy message evaluation.
    @DisplayName("Test constructor with expected values and message supplier")
    @Test
    void testConstructorWithExpectedAndMessageSupplier() {
        AssertConsumeOnly<Integer> consumer = AssertActions.consumeOnly(EXPECTED_VALUES, () -> TEST_MESSAGE);

        // Should accept values from expected set
        assertDoesNotThrow(() -> consumer.accept(2));

        // Should throw for values not in expected set with supplied message
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> consumer.accept(5));
        assertTrue(error.getMessage().contains(TEST_MESSAGE));
    }

    /// Tests the accept method with values that should be accepted.
    /// Verifies that all values in the expected set are accepted without throwing exceptions.
    @DisplayName("Test accept method with valid values")
    @Test
    void testAcceptWithValidValues() {
        AssertConsumeOnly<Integer> consumer = new AssertConsumeOnly<>(EXPECTED_VALUES);

        // All expected values should be accepted without throwing
        for (Integer value : EXPECTED_VALUES) {
            assertDoesNotThrow(() -> consumer.accept(value));
        }
    }

    /// Tests the accept method with values that should be rejected.
    /// Verifies that values not in the expected set cause AssertionFailedError to be thrown.
    @DisplayName("Test accept method with invalid values")
    @Test
    void testAcceptWithInvalidValues() {
        AssertConsumeOnly<Integer> consumer = new AssertConsumeOnly<>(EXPECTED_VALUES);

        // Values not in expected set should throw AssertionFailedError
        assertThrows(AssertionFailedError.class, () -> consumer.accept(4));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(-1));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(100));
    }

    /// Tests the consumer with different collection types as expected values.
    /// Verifies that the consumer works correctly with List, Set, and other Iterable implementations.
    @DisplayName("Test with different collection types")
    @Test
    void testWithDifferentCollectionTypes() {
        // Test with List
        AssertConsumeOnly<Integer> listConsumer = new AssertConsumeOnly<>(List.of(10, 20, 30));
        assertDoesNotThrow(() -> listConsumer.accept(10));
        assertThrows(AssertionFailedError.class, () -> listConsumer.accept(40));

        // Test with Set
        AssertConsumeOnly<String> setConsumer = new AssertConsumeOnly<>(Set.of("x", "y", "z"));
        assertDoesNotThrow(() -> setConsumer.accept("x"));
        assertThrows(AssertionFailedError.class, () -> setConsumer.accept("w"));
    }

    /// Tests behavior when null values are included in the expected set.
    /// Verifies that null values are properly accepted when they appear in the expected values.
    @DisplayName("Test with null values in expected set")
    @Test
    void testWithNullValuesInExpectedSet() {
        List<String> expectedWithNull = listOf("a", null, "b");
        AssertConsumeOnly<String> consumer = new AssertConsumeOnly<>(expectedWithNull);

        // Should accept null if it's in the expected set
        assertDoesNotThrow(() -> consumer.accept(null));
        assertDoesNotThrow(() -> consumer.accept("a"));
        assertDoesNotThrow(() -> consumer.accept("b"));

        // Should throw for values not in expected set
        assertThrows(AssertionFailedError.class, () -> consumer.accept("c"));
    }

    /// Tests behavior when the expected set is empty.
    /// Verifies that any value (including null) causes AssertionFailedError when no values are expected.
    @DisplayName("Test with empty expected set")
    @Test
    void testWithEmptyExpectedSet() {
        AssertConsumeOnly<Integer> consumer = new AssertConsumeOnly<>(listOf());

        // Should throw for any value when expected set is empty
        assertThrows(AssertionFailedError.class, () -> consumer.accept(1));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(null));
    }

    /// Tests behavior when duplicate values appear in the expected set.
    /// Verifies that values are still accepted correctly regardless of duplicates in the expected collection.
    @DisplayName("Test duplicate values in expected set")
    @Test
    void testDuplicateValuesInExpectedSet() {
        List<Integer> expectedWithDuplicates = listOf(1, 2, 2, 3, 1);
        AssertConsumeOnly<Integer> consumer = new AssertConsumeOnly<>(expectedWithDuplicates);

        // Should accept values that appear in the expected set (even if duplicated)
        assertDoesNotThrow(() -> consumer.accept(1));
        assertDoesNotThrow(() -> consumer.accept(2));
        assertDoesNotThrow(() -> consumer.accept(3));

        // Should throw for values not in expected set
        assertThrows(AssertionFailedError.class, () -> consumer.accept(4));
    }

    /// Tests that error messages contain meaningful information about expected and actual values.
    /// Verifies that AssertionFailedError messages include both expected and actual value information.
    @DisplayName("Test error message contains expected and actual values")
    @Test
    void testErrorMessageContent() {
        AssertConsumeOnly<Integer> consumer = new AssertConsumeOnly<>(EXPECTED_VALUES);

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> consumer.accept(99));

        // The error should contain information about expected and actual values
        String errorMessage = error.getMessage();
        assertTrue(errorMessage.contains("expected"), "Error message should contain 'expected'");
        assertTrue(errorMessage.contains("actual") || errorMessage.contains("99"),
                  "Error message should contain 'actual' or the actual value");
    }

    /// Tests the consumer with String values instead of Integer values.
    /// Verifies that the generic typing works correctly with different element types.
    @DisplayName("Test with string values")
    @Test
    void testWithStringValues() {
        AssertConsumeOnly<String> consumer = new AssertConsumeOnly<>(EXPECTED_STRINGS);

        // Should accept strings from expected set
        assertDoesNotThrow(() -> consumer.accept("a"));
        assertDoesNotThrow(() -> consumer.accept("b"));
        assertDoesNotThrow(() -> consumer.accept("c"));

        // Should throw for strings not in expected set
        assertThrows(AssertionFailedError.class, () -> consumer.accept("d"));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(""));
    }

    /// Tests that the same valid value can be accepted multiple times.
    /// Verifies that the consumer doesn't maintain state that prevents repeated acceptance of valid values.
    @DisplayName("Test multiple calls to accept with same valid value")
    @Test
    void testMultipleCallsWithSameValidValue() {
        AssertConsumeOnly<Integer> consumer = new AssertConsumeOnly<>(EXPECTED_VALUES);

        // Should be able to accept the same valid value multiple times
        assertDoesNotThrow(() -> consumer.accept(1));
        assertDoesNotThrow(() -> consumer.accept(1));
        assertDoesNotThrow(() -> consumer.accept(1));
    }

    /// Tests integration with real-world forEach operations.
    /// Verifies that the consumer works correctly when passed to collection forEach methods,
    /// both with collections containing only valid values and collections containing invalid values.
    @DisplayName("Test integration with forEach operation")
    @Test
    void testIntegrationWithForEach() {
        AssertConsumeOnly<Integer> consumer = new AssertConsumeOnly<>(EXPECTED_VALUES);

        // Should work correctly when used with forEach on valid values
        List<Integer> validValues = List.of(1, 2, 3, 1, 2);
        assertDoesNotThrow(() -> validValues.forEach(consumer));

        // Should throw when used with forEach containing invalid values
        List<Integer> invalidValues = List.of(1, 2, 4);
        assertThrows(AssertionFailedError.class, () -> invalidValues.forEach(consumer));
    }
}