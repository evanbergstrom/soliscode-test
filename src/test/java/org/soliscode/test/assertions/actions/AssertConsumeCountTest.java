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
import org.soliscode.test.assertions.Assertions;
import org.soliscode.test.util.UsesCollections;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.string.AssertStringContainsInOrder.assertStringContainsInOrderIgnoreCase;

/// Test class for {@link AssertConsumeCount} assertion consumer.
/// This class provides comprehensive test coverage for the AssertConsumeCount class,
/// which is used to assert that a consumer is called exactly a specified number of times.
///
/// The tests cover all constructors, the accept() method, the assertCheck() method,
/// edge cases, error handling, and integration scenarios with various collection operations.
///
/// @author evanbergstrom
/// @since 1.0
/// @see AssertConsumeCount
/// @see AssertActions
@DisplayName("Tests for AssertConsumeCount")
public class AssertConsumeCountTest implements UsesCollections {

    /// Test message used for custom error message validation.
    private static final String TEST_MESSAGE = "Test message";

    /// Standard list of test values used for consumption testing.
    private static final List<Integer> TEST_VALUES = List.of(1, 2, 3, 4, 5);

    /// Tests the basic constructor that takes only the expected count.
    /// Verifies that the consumer correctly counts accepted values and validates the count.
    @DisplayName("Test constructor with expected count only")
    @Test
    void testConstructorWithExpectedCountOnly() {
        AssertConsumeCount<Integer> consumer = AssertActions.consumeCount(3);

        // Accept only 1 value (less than expected)
        consumer.accept(1);

        // Should fail when count is less than expected
        AssertionFailedError lessError = assertThrows(AssertionFailedError.class, consumer::assertCheck);

        // Accept exactly 3 values
        consumer.accept(2);
        consumer.accept(3);

        // Should pass when count matches expected
        assertDoesNotThrow(consumer::assertCheck);

        // Accept four values (more than expected)
        consumer.accept(4);

        // Should fail when count is more than expected
        AssertionFailedError moreError = assertThrows(AssertionFailedError.class, consumer::assertCheck);
    }

    /// Tests the constructor that takes expected count and a custom error message.
    /// Verifies that the custom message appears in the exception when assertion fails.
    @DisplayName("Test constructor with expected count and message")
    @Test
    void testConstructorWithExpectedCountAndMessage() {
        AssertConsumeCount<Integer> consumer = AssertActions.consumeCount(2, TEST_MESSAGE);

        // Accept only 1 value (less than expected)
        consumer.accept(1);

        // Should throw with custom message
        AssertionFailedError lessError = assertThrows(AssertionFailedError.class, consumer::assertCheck);
        Assertions.assertStringContains(TEST_MESSAGE, lessError.getMessage());

        consumer.accept(2);
        consumer.accept(3);
        consumer.accept(4);

        AssertionFailedError moreError = assertThrows(AssertionFailedError.class, consumer::assertCheck);
        Assertions.assertStringContains(TEST_MESSAGE, moreError.getMessage());
    }

    /// Tests the constructor that takes expected count and a message supplier.
    /// Verifies that the supplied message appears in the exception when assertion fails,
    /// demonstrating lazy message evaluation.
    @DisplayName("Test constructor with expected count and message supplier")
    @Test
    void testConstructorWithExpectedCountAndMessageSupplier() {
        AssertConsumeCount<Integer> consumer = AssertActions.consumeCount(2, () -> TEST_MESSAGE);

        // Accept more values than expected
        consumer.accept(1);
        AssertionFailedError lessError = assertThrows(AssertionFailedError.class, consumer::assertCheck);
        Assertions.assertStringContains(TEST_MESSAGE, lessError.getMessage());

        consumer.accept(2);
        consumer.accept(3);

        // Should throw with supplied message
        AssertionFailedError moreError = assertThrows(AssertionFailedError.class, consumer::assertCheck);
        Assertions.assertStringContains(TEST_MESSAGE, moreError.getMessage());
    }

    /// Tests the accept method counting behavior.
    /// Verifies that each call to accept increments the internal counter.
    @DisplayName("Test accept method increments count")
    @Test
    void testAcceptIncrementsCount() {
        AssertConsumeCount<Integer> consumer = new AssertConsumeCount<>(0);
        assertEquals(0, consumer.accepted());

        // Initially should pass with zero count
        assertDoesNotThrow(consumer::assertCheck);

        // After one accept, should expect 1
        consumer.accept(42);
        assertEquals(1, consumer.accepted());

        AssertConsumeCount<Integer> consumer1 = new AssertConsumeCount<>(1);
        consumer1.accept(42);
        assertEquals(1, consumer1.accepted());
        assertDoesNotThrow(consumer1::assertCheck);
    }

    /// Tests assertCheck when the actual count matches expected count.
    /// Verifies that no exception is thrown when counts match exactly.
    @DisplayName("Test assertCheck passes when counts match")
    @Test
    void testAssertCheckPassesWhenCountsMatch() {
        // Test with zero count
        AssertConsumeCount<String> zeroConsumer = new AssertConsumeCount<>(0);
        assertDoesNotThrow(zeroConsumer::assertCheck);

        // Test with positive count
        AssertConsumeCount<String> positiveConsumer = new AssertConsumeCount<>(3);
        positiveConsumer.accept("a");
        positiveConsumer.accept("b");
        positiveConsumer.accept("c");

        assertDoesNotThrow(positiveConsumer::assertCheck);
    }

    /// Tests assertCheck when the actual count is less than expected count.
    /// Verifies that AssertionFailedError is thrown when not enough values are consumed.
    @DisplayName("Test assertCheck fails when actual count is less than expected")
    @Test
    void testAssertCheckFailsWhenCountTooLow() {
        AssertConsumeCount<Integer> consumer = new AssertConsumeCount<>(5);

        // Accept only 3 values when expecting 5
        consumer.accept(1);
        consumer.accept(2);
        consumer.accept(3);

        AssertionFailedError error = assertThrows(AssertionFailedError.class, consumer::assertCheck);
    }

    /// Tests assertCheck when the actual count is greater than expected count.
    /// Verifies that AssertionFailedError is thrown when too many values are consumed.
    @DisplayName("Test assertCheck fails when actual count is greater than expected")
    @Test
    void testAssertCheckFailsWhenCountTooHigh() {
        AssertConsumeCount<Integer> consumer = new AssertConsumeCount<>(2);

        // Accept 4 values when expecting 2
        consumer.accept(1);
        consumer.accept(2);
        consumer.accept(3);
        consumer.accept(4);

        AssertionFailedError error = assertThrows(AssertionFailedError.class, consumer::assertCheck);
    }

    /// Tests behavior with null values being consumed.
    /// Verifies that null values are counted just like any other value.
    @DisplayName("Test with null values")
    @Test
    void testWithNullValues() {
        AssertConsumeCount<String> consumer = new AssertConsumeCount<>(3);

        consumer.accept("test");
        consumer.accept(null);
        consumer.accept("another");

        // Should pass - null counts as a consumed value
        assertDoesNotThrow(consumer::assertCheck);
    }

    /// Tests behavior with zero expected count.
    /// Verifies that the consumer works correctly when expecting no consumption.
    @DisplayName("Test with zero expected count")
    @Test
    void testWithZeroExpectedCount() {
        AssertConsumeCount<Integer> consumer = new AssertConsumeCount<>(0);

        // Should pass when no values are consumed
        assertDoesNotThrow(consumer::assertCheck);

        // Should fail when any value is consumed
        consumer.accept(1);
        assertThrows(AssertionFailedError.class, consumer::assertCheck);
    }

    /// Tests behavior with large expected counts.
    /// Verifies that the consumer works correctly with larger numbers.
    @DisplayName("Test with large expected count")
    @Test
    void testWithLargeExpectedCount() {
        AssertConsumeCount<Integer> consumer = new AssertConsumeCount<>(100);

        // Consume exactly 100 values
        for (int i = 0; i < 100; i++) {
            consumer.accept(i);
        }

        // Should pass with exact count
        assertDoesNotThrow(consumer::assertCheck);

        // Consume one more
        consumer.accept(100);

        // Should now fail
        assertThrows(AssertionFailedError.class, consumer::assertCheck);
    }

    /// Tests multiple calls to assertCheck.
    /// Verifies that assertCheck can be called multiple times with consistent results.
    @DisplayName("Test multiple calls to assertCheck")
    @Test
    void testMultipleCallsToAssertCheck() {
        AssertConsumeCount<Integer> consumer = new AssertConsumeCount<>(2);
        consumer.accept(1);
        consumer.accept(2);

        // Multiple calls should all pass
        assertDoesNotThrow(consumer::assertCheck);
        assertDoesNotThrow(consumer::assertCheck);
        assertDoesNotThrow(consumer::assertCheck);
    }

    /// Tests integration with real-world forEach operations.
    /// Verifies that the consumer works correctly when passed to collection forEach methods.
    @DisplayName("Test integration with forEach operation")
    @Test
    void testIntegrationWithForEach() {
        // Test with matching count
        AssertConsumeCount<Integer> consumer = new AssertConsumeCount<>(TEST_VALUES.size());
        TEST_VALUES.forEach(consumer);
        assertDoesNotThrow(consumer::assertCheck);

        // Test with mismatched count
        AssertConsumeCount<Integer> mismatchConsumer = new AssertConsumeCount<>(10);
        TEST_VALUES.forEach(mismatchConsumer);
        assertThrows(AssertionFailedError.class, mismatchConsumer::assertCheck);
    }

    /// Tests that error messages contain meaningful information about expected and actual counts.
    /// Verifies that AssertionFailedError messages include both expected and actual count information.
    @DisplayName("Test error message contains expected and actual counts")
    @Test
    void testErrorMessageContent() {
        AssertConsumeCount<Integer> consumer = new AssertConsumeCount<>(5);
        consumer.accept(1);
        consumer.accept(2);
        // Expected 5, actual 2

        AssertionFailedError error = assertThrows(AssertionFailedError.class, consumer::assertCheck);
        String errorMessage = error.getMessage();

        // Should contain expected and actual information
        assertStringContainsInOrderIgnoreCase(List.of("expected", "5", "actual", "2"), error.getMessage());
    }

    /// Tests the consumer with different generic types.
    /// Verifies that the generic typing works correctly with different element types.
    @DisplayName("Test with different types")
    @Test
    void testWithDifferentTypes() {
        // Test with String
        AssertConsumeCount<String> stringConsumer = new AssertConsumeCount<>(2);
        stringConsumer.accept("hello");
        stringConsumer.accept("world");
        assertDoesNotThrow(stringConsumer::assertCheck);

        // Test with Double
        AssertConsumeCount<Double> doubleConsumer = new AssertConsumeCount<>(1);
        doubleConsumer.accept(3.14);
        assertDoesNotThrow(doubleConsumer::assertCheck);
    }
}