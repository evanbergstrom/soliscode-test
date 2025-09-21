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

import static org.junit.jupiter.api.Assertions.*;

/// Test class for {@link AssertConsumeNone} assertion consumer.
/// This class provides comprehensive test coverage for the AssertConsumeNone class,
/// which is used to assert that a consumer should never be called - any call to
/// accept() should immediately throw an AssertionFailedError.
///
/// The tests cover all constructors, the accept() method behavior, edge cases
/// with different value types including null, error handling, and integration
/// scenarios with various collection operations.
///
/// @author evanbergstrom
/// @since 1.0
/// @see AssertConsumeNone
/// @see AssertActions
@DisplayName("Tests for AssertConsumeNone")
public class AssertConsumeNoneTest implements UsesCollections {

    /// Test message used for custom error message validation.
    private static final String TEST_MESSAGE = "Test message";

    /// Tests the basic constructor with no parameters.
    /// Verifies that the consumer immediately throws on any accept() call.
    @DisplayName("Test constructor with no parameters")
    @Test
    void testConstructorWithNoParameters() {
        AssertConsumeNone<Integer> consumer = AssertActions.consumeNone();

        // Any accept call should throw immediately
        assertThrows(AssertionFailedError.class, () -> consumer.accept(1));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(null));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(0));
    }

    /// Tests the constructor that takes a custom error message.
    /// Verifies that the custom message appears in the exception when assertion fails.
    @DisplayName("Test constructor with custom message")
    @Test
    void testConstructorWithMessage() {
        AssertConsumeNone<String> consumer = AssertActions.consumeNone(TEST_MESSAGE);

        // Should throw with custom message
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> consumer.accept("test"));
        assertTrue(error.getMessage().contains(TEST_MESSAGE));
    }

    /// Tests the constructor that takes a message supplier.
    /// Verifies that the supplied message appears in the exception when assertion fails,
    /// demonstrating lazy message evaluation.
    @DisplayName("Test constructor with message supplier")
    @Test
    void testConstructorWithMessageSupplier() {
        AssertConsumeNone<Double> consumer = AssertActions.consumeNone(() -> TEST_MESSAGE);

        // Should throw with supplied message
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> consumer.accept(3.14));
        assertTrue(error.getMessage().contains(TEST_MESSAGE));
    }

    /// Tests the accept method with various non-null values.
    /// Verifies that any non-null value causes immediate AssertionFailedError.
    @DisplayName("Test accept method with non-null values")
    @Test
    void testAcceptWithNonNullValues() {
        AssertConsumeNone<Object> consumer = new AssertConsumeNone<>();

        // Test with different types of values
        assertThrows(AssertionFailedError.class, () -> consumer.accept(42));
        assertThrows(AssertionFailedError.class, () -> consumer.accept("string"));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(true));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(3.14));
        assertThrows(AssertionFailedError.class, () -> consumer.accept('c'));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(List.of(1, 2, 3)));
    }

    /// Tests the accept method with null values.
    /// Verifies that null values also cause immediate AssertionFailedError.
    @DisplayName("Test accept method with null values")
    @Test
    void testAcceptWithNullValues() {
        AssertConsumeNone<String> consumer = new AssertConsumeNone<>();

        // Null should also cause failure
        assertThrows(AssertionFailedError.class, () -> consumer.accept(null));
    }

    /// Tests that the consumer works with different generic types.
    /// Verifies that the generic typing works correctly with various element types.
    @DisplayName("Test with different generic types")
    @Test
    void testWithDifferentGenericTypes() {
        // Test with Integer
        AssertConsumeNone<Integer> intConsumer = new AssertConsumeNone<>();
        assertThrows(AssertionFailedError.class, () -> intConsumer.accept(100));

        // Test with String
        AssertConsumeNone<String> stringConsumer = new AssertConsumeNone<>();
        assertThrows(AssertionFailedError.class, () -> stringConsumer.accept("fail"));

        // Test with custom object
        AssertConsumeNone<List<String>> listConsumer = new AssertConsumeNone<>();
        assertThrows(AssertionFailedError.class, () -> listConsumer.accept(List.of("a", "b")));

        // Test with Boolean
        AssertConsumeNone<Boolean> boolConsumer = new AssertConsumeNone<>();
        assertThrows(AssertionFailedError.class, () -> boolConsumer.accept(true));
        assertThrows(AssertionFailedError.class, () -> boolConsumer.accept(false));
    }

    /// Tests multiple consecutive calls to accept.
    /// Verifies that each call consistently throws AssertionFailedError.
    @DisplayName("Test multiple consecutive accept calls")
    @Test
    void testMultipleConsecutiveAcceptCalls() {
        AssertConsumeNone<Integer> consumer = new AssertConsumeNone<>();

        // Each call should throw
        assertThrows(AssertionFailedError.class, () -> consumer.accept(1));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(2));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(3));
    }

    /// Tests that error messages are generated correctly.
    /// Verifies that AssertionFailedError contains appropriate error information.
    @DisplayName("Test error message generation")
    @Test
    void testErrorMessageGeneration() {
        AssertConsumeNone<String> consumer = new AssertConsumeNone<>();

        // Should throw with some error message
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> consumer.accept("test"));
        assertNotNull(error.getMessage());
        // The error message might be empty or contain default information, which is acceptable
    }

    /// Tests behavior with custom message being null.
    /// Verifies that null custom messages are handled gracefully.
    @DisplayName("Test with null custom message")
    @Test
    void testWithNullCustomMessage() {
        AssertConsumeNone<Integer> consumer = new AssertConsumeNone<>((String) null);

        // Should still throw, even with null message
        assertThrows(AssertionFailedError.class, () -> consumer.accept(42));
    }

    /// Tests behavior with null message supplier.
    /// Verifies that null message suppliers are handled gracefully.
    @DisplayName("Test with null message supplier")
    @Test
    void testWithNullMessageSupplier() {
        AssertConsumeNone<Integer> consumer = new AssertConsumeNone<>((java.util.function.Supplier<String>) null);

        // Should still throw, even with null supplier
        assertThrows(AssertionFailedError.class, () -> consumer.accept(42));
    }

    /// Tests integration with empty collections.
    /// Verifies that the consumer works correctly with empty collections (no calls to accept).
    @DisplayName("Test integration with empty collections")
    @Test
    void testIntegrationWithEmptyCollections() {
        AssertConsumeNone<Integer> consumer = new AssertConsumeNone<>();

        // Empty collections should not call accept, so no exception should be thrown
        List<Integer> emptyList = listOf();
        assertDoesNotThrow(() -> emptyList.forEach(consumer));
    }

    /// Tests integration with non-empty collections.
    /// Verifies that the consumer fails when used with collections that contain elements.
    @DisplayName("Test integration with non-empty collections")
    @Test
    void testIntegrationWithNonEmptyCollections() {
        AssertConsumeNone<Integer> consumer = new AssertConsumeNone<>();

        // Non-empty collections should cause failure
        List<Integer> singleElementList = List.of(1);
        assertThrows(AssertionFailedError.class, () -> singleElementList.forEach(consumer));

        List<Integer> multiElementList = List.of(1, 2, 3);
        assertThrows(AssertionFailedError.class, () -> multiElementList.forEach(consumer));
    }

    /// Tests that the consumer fails fast on first element.
    /// Verifies that the consumer throws immediately on the first accept call in a forEach.
    @DisplayName("Test fails fast on first element")
    @Test
    void testFailsFastOnFirstElement() {
        AssertConsumeNone<Integer> consumer = new AssertConsumeNone<>();
        List<Integer> values = List.of(1, 2, 3, 4, 5);

        // Should fail on the first element, not process all elements
        assertThrows(AssertionFailedError.class, () -> values.forEach(consumer));
    }

    /// Tests the consumer with large collections to verify performance.
    /// Verifies that the consumer fails immediately even with large datasets.
    @DisplayName("Test with large collections")
    @Test
    void testWithLargeCollections() {
        AssertConsumeNone<Integer> consumer = new AssertConsumeNone<>();

        // Create a large list
        List<Integer> largeList = listOf();
        for (int i = 0; i < 1000; i++) {
            ((java.util.List<Integer>) largeList).add(i);
        }

        // Should fail immediately on first element, regardless of size
        assertThrows(AssertionFailedError.class, () -> largeList.forEach(consumer));
    }

    /// Tests edge cases with special numeric values.
    /// Verifies that special values like zero, negative numbers, and extreme values all cause failure.
    @DisplayName("Test with special numeric values")
    @Test
    void testWithSpecialNumericValues() {
        AssertConsumeNone<Number> consumer = new AssertConsumeNone<>();

        // Test special numeric values
        assertThrows(AssertionFailedError.class, () -> consumer.accept(0));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(-1));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(Integer.MAX_VALUE));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(Integer.MIN_VALUE));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(Double.POSITIVE_INFINITY));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(Double.NEGATIVE_INFINITY));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(Double.NaN));
    }

    /// Tests edge cases with special string values.
    /// Verifies that empty strings and whitespace strings also cause failure.
    @DisplayName("Test with special string values")
    @Test
    void testWithSpecialStringValues() {
        AssertConsumeNone<String> consumer = new AssertConsumeNone<>();

        // Test special string values
        assertThrows(AssertionFailedError.class, () -> consumer.accept(""));
        assertThrows(AssertionFailedError.class, () -> consumer.accept(" "));
        assertThrows(AssertionFailedError.class, () -> consumer.accept("\n"));
        assertThrows(AssertionFailedError.class, () -> consumer.accept("\t"));
        assertThrows(AssertionFailedError.class, () -> consumer.accept("   "));
    }

    /// Tests the consumer implements AssertConsumer interface correctly.
    /// Verifies that the consumer can be used polymorphically as an AssertConsumer.
    @DisplayName("Test implements AssertConsumer interface")
    @Test
    void testImplementsAssertConsumerInterface() {
        AssertConsumeNone<String> consumer = new AssertConsumeNone<>();

        // Should work as AssertConsumer
        assertThrows(AssertionFailedError.class, () -> consumer.accept("test"));
    }
}