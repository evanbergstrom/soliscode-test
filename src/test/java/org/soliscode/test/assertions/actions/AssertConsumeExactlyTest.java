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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.Assertions.assertStringContains;
import static org.soliscode.test.assertions.Assertions.assertStringContainsInOrderIgnoreCase;

/// Test class for {@link AssertConsumeExactly} assertion consumer.
/// This class provides comprehensive test coverage for the AssertConsumeExactly class,
/// which is used to assert that a consumer consumes exactly a specified set of values,
/// consuming each value exactly once and rejecting any values not in the expected set.
///
/// The tests cover all constructors, the accept() method, the assertCheck() method,
/// edge cases, error handling, and integration scenarios with various collection operations.
///
/// @author evanbergstrom
/// @since 1.0
/// @see AssertConsumeExactly
/// @see AssertActions
@DisplayName("Tests for AssertConsumeExactly")
public class AssertConsumeExactlyTest implements UsesCollections {

    /// Test message used for custom error message validation.
    private static final String TEST_MESSAGE = "Test message";

    /// Standard list of expected test values used across multiple tests.
    private static final List<Integer> EXPECTED_VALUES = List.of(1, 2, 3);

    /// Standard set of expected string values used for type diversity testing.
    private static final Set<String> EXPECTED_STRINGS = Set.of("a", "b", "c");

    /// Tests the basic constructor that takes only expected values.
    /// Verifies that the consumer correctly accepts expected values and tracks consumption.
    @DisplayName("Test constructor with expected values only")
    @Test
    void testConstructorWithExpectedOnly() {
        AssertConsumeExactly<Integer> consumer = AssertActions.consumeExactly(EXPECTED_VALUES);

        // Should accept all expected values
        assertDoesNotThrow(() -> consumer.accept(1));
        assertDoesNotThrow(() -> consumer.accept(2));
        assertDoesNotThrow(() -> consumer.accept(3));

        // Should pass when all values consumed
        assertDoesNotThrow(consumer::assertCheck);
    }

    /// Tests the constructor that takes expected values and a custom error message.
    /// Verifies that the custom message appears in the exception when assertion fails.
    @DisplayName("Test constructor with expected values and message")
    @Test
    void testConstructorWithExpectedAndMessage() {
        AssertConsumeExactly<Integer> consumer = AssertActions.consumeExactly(EXPECTED_VALUES, TEST_MESSAGE);

        // Accept an unexpected value
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> consumer.accept(99));
        assertStringContains(TEST_MESSAGE, error.getMessage());
    }

    /// Tests the constructor that takes expected values and a message supplier.
    /// Verifies that the supplied message appears in the exception when assertion fails,
    /// demonstrating lazy message evaluation.
    @DisplayName("Test constructor with expected values and message supplier")
    @Test
    void testConstructorWithExpectedAndMessageSupplier() {
        AssertConsumeExactly<Integer> consumer = AssertActions.consumeExactly(EXPECTED_VALUES, () -> TEST_MESSAGE);

        // Accept some but not all expected values, then check
        consumer.accept(1);
        AssertionFailedError error = assertThrows(AssertionFailedError.class, consumer::assertCheck);
        assertStringContains(TEST_MESSAGE, error.getMessage());
    }

    /// Tests the accept method with valid expected values.
    /// Verifies that expected values are accepted and removed from the internal collection.
    @DisplayName("Test accept method with valid values")
    @Test
    void testAcceptWithValidValues() {
        AssertConsumeExactly<Integer> consumer = new AssertConsumeExactly<>(EXPECTED_VALUES);

        // All expected values should be accepted
        for (Integer value : EXPECTED_VALUES) {
            assertDoesNotThrow(() -> consumer.accept(value));
        }

        // Should pass when all expected values have been consumed
        assertDoesNotThrow(consumer::assertCheck);
    }

    /// Tests the accept method with invalid/unexpected values.
    /// Verifies that unexpected values cause immediate AssertionFailedError.
    @DisplayName("Test accept method with invalid values")
    @Test
    void testAcceptWithInvalidValues() {
        AssertConsumeExactly<Integer> consumer = new AssertConsumeExactly<>(EXPECTED_VALUES);

        // Unexpected values should throw immediately
        assertThrows(AssertionFailedError.class, () -> consumer.accept(99));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(0));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(-1));
    }

    /// Tests assertCheck when all expected values have been consumed exactly once.
    /// Verifies that no exception is thrown when the consumption is complete and exact.
    @DisplayName("Test assertCheck passes when all values consumed exactly")
    @Test
    void testAssertCheckPassesWhenComplete() {
        AssertConsumeExactly<String> consumer = new AssertConsumeExactly<>(EXPECTED_STRINGS);

        // Consume all expected values
        consumer.accept("a");
        consumer.accept("b");
        consumer.accept("c");

        // Should pass
        assertDoesNotThrow(consumer::assertCheck);
    }

    /// Tests assertCheck when not all expected values have been consumed.
    /// Verifies that AssertionFailedError is thrown when consumption is incomplete.
    @DisplayName("Test assertCheck fails when values missing")
    @Test
    void testAssertCheckFailsWhenIncomplete() {
        AssertConsumeExactly<Integer> consumer = new AssertConsumeExactly<>(EXPECTED_VALUES);

        // Consume only some values
        consumer.accept(1);
        consumer.accept(2);
        // Missing value 3

        // Should fail
        assertThrows(AssertionFailedError.class, consumer::assertCheck);
    }

    /// Tests behavior when the same value is consumed twice.
    /// Verifies that consuming a value twice causes failure on the second consumption.
    @DisplayName("Test consuming same value twice fails")
    @Test
    void testConsumeSameValueTwiceFails() {
        AssertConsumeExactly<Integer> consumer = new AssertConsumeExactly<>(EXPECTED_VALUES);

        // First consumption should succeed
        assertDoesNotThrow(() -> consumer.accept(1));

        // Second consumption of same value should fail
        assertThrows(AssertionFailedError.class, () -> consumer.accept(1));
    }

    /// Tests behavior with null values in the expected set.
    /// Verifies that null values are properly handled when they are expected.
    @DisplayName("Test with null values in expected set")
    @Test
    void testWithNullValuesInExpectedSet() {
        List<String> expectedWithNull = listOf("a", null, "b");
        AssertConsumeExactly<String> consumer = new AssertConsumeExactly<>(expectedWithNull);

        // Should accept null if it's expected
        assertDoesNotThrow(() -> consumer.accept(null));
        assertDoesNotThrow(() -> consumer.accept("a"));
        assertDoesNotThrow(() -> consumer.accept("b"));

        // Should pass when all expected values (including null) are consumed
        assertDoesNotThrow(consumer::assertCheck);
    }

    /// Tests behavior with empty expected set.
    /// Verifies that any consumption fails when no values are expected.
    @DisplayName("Test with empty expected set")
    @Test
    void testWithEmptyExpectedSet() {
        AssertConsumeExactly<Integer> consumer = new AssertConsumeExactly<>(listOf());

        // Should pass immediately when no values are expected
        assertDoesNotThrow(consumer::assertCheck);

        // Any consumption should fail
        assertThrows(AssertionFailedError.class, () -> consumer.accept(1));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(null));
    }

    /// Tests behavior with duplicate values in the expected set.
    /// Verifies that duplicates in the expected set allow multiple consumptions of the same value.
    @DisplayName("Test with duplicate values in expected set")
    @Test
    void testWithDuplicateValuesInExpectedSet() {
        List<Integer> expectedWithDuplicates = listOf(1, 2, 2, 3);
        AssertConsumeExactly<Integer> consumer = new AssertConsumeExactly<>(expectedWithDuplicates);

        // Should be able to consume value 2 twice
        assertDoesNotThrow(() -> consumer.accept(1));
        assertDoesNotThrow(() -> consumer.accept(2));
        assertDoesNotThrow(() -> consumer.accept(2)); // Second 2
        assertDoesNotThrow(() -> consumer.accept(3));

        // Should pass when all expected values consumed
        assertDoesNotThrow(consumer::assertCheck);
    }

    /// Tests the consumer with different collection types as expected values.
    /// Verifies that the consumer works correctly with List, Set, and other Iterable implementations.
    @DisplayName("Test with different collection types")
    @Test
    void testWithDifferentCollectionTypes() {
        // Test with Set
        AssertConsumeExactly<String> setConsumer = new AssertConsumeExactly<>(Set.of("x", "y"));
        assertDoesNotThrow(() -> setConsumer.accept("x"));
        assertDoesNotThrow(() -> setConsumer.accept("y"));
        assertDoesNotThrow(setConsumer::assertCheck);

        // Test with List
        AssertConsumeExactly<Integer> listConsumer = new AssertConsumeExactly<>(List.of(10, 20));
        assertDoesNotThrow(() -> listConsumer.accept(10));
        assertDoesNotThrow(() -> listConsumer.accept(20));
        assertDoesNotThrow(listConsumer::assertCheck);
    }

    /// Tests that error messages contain meaningful information about expected and actual values.
    /// Verifies that AssertionFailedError messages include relevant expected and actual information.
    @DisplayName("Test error message contains expected and actual values")
    @Test
    void testErrorMessageContent() {
        AssertConsumeExactly<Integer> consumer = new AssertConsumeExactly<>(EXPECTED_VALUES);

        // Test error message for unexpected value
        AssertionFailedError acceptError = assertThrows(AssertionFailedError.class, () -> consumer.accept(99));
        List<String> expectedStrings = List.of("expected", "1", "2", "99");
        assertStringContainsInOrderIgnoreCase(expectedStrings, acceptError.getMessage());

        // Test error message for incomplete consumption
        AssertConsumeExactly<Integer> incompleteConsumer = new AssertConsumeExactly<>(EXPECTED_VALUES);
        incompleteConsumer.accept(1); // Only consume one value
        AssertionFailedError checkError = assertThrows(AssertionFailedError.class, incompleteConsumer::assertCheck);

        List<String> expectedStrings2 = List.of("expected", "2", "3");
        assertStringContainsInOrderIgnoreCase(expectedStrings2, acceptError.getMessage());
    }

    /// Tests the consumer with String values instead of Integer values.
    /// Verifies that the generic typing works correctly with different element types.
    @DisplayName("Test with string values")
    @Test
    void testWithStringValues() {
        AssertConsumeExactly<String> consumer = new AssertConsumeExactly<>(EXPECTED_STRINGS);

        // Should accept all expected strings
        assertDoesNotThrow(() -> consumer.accept("a"));
        assertDoesNotThrow(() -> consumer.accept("b"));
        assertDoesNotThrow(() -> consumer.accept("c"));

        // Should pass when all consumed
        assertDoesNotThrow(consumer::assertCheck);

        // Test with unexpected string should fail
        AssertConsumeExactly<String> failConsumer = new AssertConsumeExactly<>(EXPECTED_STRINGS);
        assertThrows(AssertionFailedError.class, () -> failConsumer.accept("d"));
    }

    /// Tests integration with real-world forEach operations.
    /// Verifies that the consumer works correctly when passed to collection forEach methods.
    @DisplayName("Test integration with forEach operation")
    @Test
    void testIntegrationWithForEach() {
        // Test with matching values
        AssertConsumeExactly<Integer> consumer = new AssertConsumeExactly<>(EXPECTED_VALUES);
        List<Integer> matchingValues = List.of(1, 2, 3); // Same values, possibly different order
        assertDoesNotThrow(() -> matchingValues.forEach(consumer));
        assertDoesNotThrow(consumer::assertCheck);

        // Test with extra values should fail during forEach
        AssertConsumeExactly<Integer> extraConsumer = new AssertConsumeExactly<>(EXPECTED_VALUES);
        List<Integer> extraValues = List.of(1, 2, 3, 4);
        assertThrows(AssertionFailedError.class, () -> extraValues.forEach(extraConsumer));

        // Test with missing values should fail during assertCheck
        AssertConsumeExactly<Integer> missingConsumer = new AssertConsumeExactly<>(EXPECTED_VALUES);
        List<Integer> missingValues = List.of(1, 2); // Missing 3
        assertDoesNotThrow(() -> missingValues.forEach(missingConsumer));
        assertThrows(AssertionFailedError.class, missingConsumer::assertCheck);
    }

    /// Tests multiple calls to assertCheck with consistent results.
    /// Verifies that assertCheck can be called multiple times after successful consumption.
    @DisplayName("Test multiple calls to assertCheck")
    @Test
    void testMultipleCallsToAssertCheck() {
        AssertConsumeExactly<Integer> consumer = new AssertConsumeExactly<>(EXPECTED_VALUES);

        // Consume all expected values
        consumer.accept(1);
        consumer.accept(2);
        consumer.accept(3);

        // Multiple calls should all pass
        assertDoesNotThrow(consumer::assertCheck);
        assertDoesNotThrow(consumer::assertCheck);
        assertDoesNotThrow(consumer::assertCheck);
    }

    /// Tests behavior in partial consumption scenarios.
    /// Verifies that assertCheck fails appropriately when only some values are consumed.
    @DisplayName("Test partial consumption scenarios")
    @Test
    void testPartialConsumptionScenarios() {
        // Test consuming first half
        AssertConsumeExactly<Integer> consumer1 = new AssertConsumeExactly<>(List.of(1, 2, 3, 4));
        consumer1.accept(1);
        consumer1.accept(2);
        assertThrows(AssertionFailedError.class, consumer1::assertCheck);

        // Test consuming last half
        AssertConsumeExactly<Integer> consumer2 = new AssertConsumeExactly<>(List.of(1, 2, 3, 4));
        consumer2.accept(3);
        consumer2.accept(4);
        assertThrows(AssertionFailedError.class, consumer2::assertCheck);
    }
}