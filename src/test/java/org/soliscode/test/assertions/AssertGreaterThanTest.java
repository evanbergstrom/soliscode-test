package org.soliscode.test.assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.AssertGreaterThan.assertGreaterThan;

/**
 * Unit tests for the {@link AssertGreaterThan} class.
 *
 * ## Test Coverage
 *
 * This test class provides comprehensive coverage for all methods in the `AssertGreaterThan` class,
 * testing various scenarios including:
 *
 * - **Basic comparisons** with different `Comparable` types (Integer, String, Double, BigDecimal, LocalDate)
 * - **Method overloads** testing all three variants (no message, String message, Supplier message)
 * - **Success cases** where the first value is genuinely greater than the second
 * - **Failure cases** where assertions should fail (equal values, first < second)
 * - **Edge cases** including null parameters and custom message evaluation
 * - **Message verification** ensuring proper error messages and lazy evaluation
 *
 * @author evanbergstrom
 * @see AssertGreaterThan
 */
@DisplayName("AssertGreaterThan Tests")
class AssertGreaterThanTest {

    /**
     * ## Test: Basic Integer Comparison - Success Case
     *
     * Verifies that `assertGreaterThan` passes when comparing two integers where the first
     * is genuinely greater than the second. This tests the fundamental functionality of
     * the assertion method using the natural ordering of integers.
     *
     * **Test Scenario:**
     * - First value: `5`
     * - Second value: `3`
     * - Expected result: Assertion passes (5 > 3)
     */
    @Test
    @DisplayName("assertGreaterThan passes with integers when first > second")
    void testAssertGreaterThanIntegerSuccess() {
        assertDoesNotThrow(() -> assertGreaterThan(5, 3));
        assertDoesNotThrow(() -> assertGreaterThan(-5, -10));
        assertDoesNotThrow(() -> assertGreaterThan(1, 0));
    }

    /**
     * ## Test: Basic Integer Comparison - Failure Case (Equal Values)
     *
     * Verifies that `assertGreaterThan` fails when comparing two equal integers.
     * The assertion should throw an `AssertionFailedError` since equal values
     * don't satisfy the "greater than" requirement.
     *
     * **Test Scenario:**
     * - First value: `5`
     * - Second value: `5`
     * - Expected result: AssertionFailedError (5 is not > 5)
     */
    @Test
    @DisplayName("assertGreaterThan fails with equal integers")
    void testAssertGreaterThanIntegerEqualFails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(5, 5));

        assertTrue(error.getMessage().contains("Expected 5 to be greater than 5"));
    }

    /**
     * ## Test: Basic Integer Comparison - Failure Case (First < Second)
     *
     * Verifies that `assertGreaterThan` fails when the first value is less than
     * the second value. This tests the core failure condition of the assertion.
     *
     * **Test Scenario:**
     * - First value: `3`
     * - Second value: `10`
     * - Expected result: AssertionFailedError (3 is not > 10)
     */
    @Test
    @DisplayName("assertGreaterThan fails when first < second")
    void testAssertGreaterThanIntegerLessFails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(3, 10));

        assertTrue(error.getMessage().contains("Expected 3 to be greater than 10"));
    }

    /**
     * ## Test: String Comparison - Success Case
     *
     * Verifies that `assertGreaterThan` works correctly with String objects using
     * their natural alphabetical ordering. This tests that the method works
     * with different types of `Comparable` objects beyond just numbers.
     *
     * **Test Scenarios:**
     * - "banana" > "apple" (alphabetical ordering)
     * - "b" > "a" (single character comparison)
     * - "a" > "" (non-empty string vs empty)
     */
    @Test
    @DisplayName("assertGreaterThan passes with strings in reverse alphabetical order")
    void testAssertGreaterThanStringSuccess() {
        assertDoesNotThrow(() -> assertGreaterThan("banana", "apple"));
        assertDoesNotThrow(() -> assertGreaterThan("b", "a"));
        assertDoesNotThrow(() -> assertGreaterThan("a", ""));
    }

    /**
     * ## Test: String Comparison - Failure Case
     *
     * Verifies that `assertGreaterThan` properly fails when strings are not in
     * the expected reverse alphabetical order.
     *
     * **Test Scenario:**
     * - First value: "apple"
     * - Second value: "zebra"
     * - Expected result: AssertionFailedError ("apple" is not > "zebra")
     */
    @Test
    @DisplayName("assertGreaterThan fails with strings in alphabetical order")
    void testAssertGreaterThanStringFails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan("apple", "zebra"));

        assertTrue(error.getMessage().contains("Expected apple to be greater than zebra"));
    }

    /**
     * ## Test: Double Precision Comparison
     *
     * Verifies that `assertGreaterThan` works correctly with floating-point numbers,
     * testing both success and failure cases with double precision values.
     *
     * **Test Scenarios:**
     * - 3.0 > 2.5 (success case)
     * - 3.14160 > 3.14159 (high precision comparison)
     * - 5.0 vs 5.0 (equal doubles should fail)
     */
    @Test
    @DisplayName("assertGreaterThan works with double precision numbers")
    void testAssertGreaterThanDouble() {
        assertDoesNotThrow(() -> assertGreaterThan(3.0, 2.5));
        assertDoesNotThrow(() -> assertGreaterThan(3.14160, 3.14159));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(5.0, 5.0));
        assertTrue(error.getMessage().contains("Expected 5.0 to be greater than 5.0"));
    }

    /**
     * ## Test: BigDecimal High Precision Comparison
     *
     * Verifies that `assertGreaterThan` works with `BigDecimal` objects, which
     * implement `Comparable` and provide arbitrary precision arithmetic.
     * This ensures the method works with complex comparable types.
     *
     * **Test Scenarios:**
     * - 1.24 > 1.23 (success with decimal precision)
     * - Equal BigDecimal values should fail
     */
    @Test
    @DisplayName("assertGreaterThan works with BigDecimal objects")
    void testAssertGreaterThanBigDecimal() {
        BigDecimal larger = new BigDecimal("1.24");
        BigDecimal smaller = new BigDecimal("1.23");
        BigDecimal equal = new BigDecimal("1.23");

        assertDoesNotThrow(() -> assertGreaterThan(larger, smaller));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(smaller, equal));
        assertTrue(error.getMessage().contains("Expected 1.23 to be greater than 1.23"));
    }

    /**
     * ## Test: LocalDate Temporal Comparison
     *
     * Verifies that `assertGreaterThan` works with temporal objects like `LocalDate`,
     * testing chronological ordering of dates.
     *
     * **Test Scenarios:**
     * - Later date > earlier date (2023-01-02 > 2023-01-01)
     * - Same date should fail the assertion
     */
    @Test
    @DisplayName("assertGreaterThan works with LocalDate objects")
    void testAssertGreaterThanLocalDate() {
        LocalDate earlier = LocalDate.of(2023, 1, 1);
        LocalDate later = LocalDate.of(2023, 1, 2);
        LocalDate same = LocalDate.of(2023, 1, 1);

        assertDoesNotThrow(() -> assertGreaterThan(later, earlier));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(earlier, same));
        assertTrue(error.getMessage().contains("Expected 2023-01-01 to be greater than 2023-01-01"));
    }

    /**
     * ## Test: Custom Error Message - String Overload
     *
     * Verifies that the `assertGreaterThan(T, T, String)` overload properly uses
     * the provided custom error message when the assertion fails. This tests
     * the second method variant that accepts a static error message.
     *
     * **Test Scenario:**
     * - Failing assertion with custom message: "Custom error message"
     * - Expected result: AssertionFailedError containing the custom message
     */
    @Test
    @DisplayName("assertGreaterThan with custom string message displays correct error")
    void testAssertGreaterThanWithStringMessage() {
        String customMessage = "Custom error message";

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(3, 10, customMessage));

        assertTrue(error.getMessage().contains(customMessage));
    }

    /**
     * ## Test: Custom Error Message - Supplier Overload
     *
     * Verifies that the `assertGreaterThan(T, T, Supplier<String>)` overload properly
     * uses the message supplier when the assertion fails. This tests the third
     * method variant that accepts a message supplier for lazy evaluation.
     *
     * **Test Scenario:**
     * - Failing assertion with message supplier
     * - Expected result: AssertionFailedError containing the supplied message
     */
    @Test
    @DisplayName("assertGreaterThan with message supplier displays correct error")
    void testAssertGreaterThanWithMessageSupplier() {
        String suppliedMessage = "Supplied error message";
        Supplier<String> messageSupplier = () -> suppliedMessage;

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(5, 10, messageSupplier));

        assertTrue(error.getMessage().contains(suppliedMessage));
    }

    /**
     * ## Test: Message Supplier Lazy Evaluation
     *
     * Verifies that the message supplier is only called when the assertion fails,
     * demonstrating lazy evaluation. When the assertion passes, the supplier
     * should never be invoked, making it efficient for expensive message generation.
     *
     * **Test Scenarios:**
     * - Passing assertion: Supplier should not be called
     * - Failing assertion: Supplier should be called exactly once
     */
    @Test
    @DisplayName("assertGreaterThan message supplier is lazily evaluated")
    void testAssertGreaterThanMessageSupplierLazyEvaluation() {
        final boolean[] supplierCalled = {false};
        Supplier<String> messageSupplier = () -> {
            supplierCalled[0] = true;
            return "Lazy message";
        };

        // Passing assertion - supplier should not be called
        assertDoesNotThrow(() -> assertGreaterThan(5, 3, messageSupplier));
        assertFalse(supplierCalled[0], "Message supplier should not be called for passing assertion");

        // Failing assertion - supplier should be called
        assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(3, 10, messageSupplier));
        assertTrue(supplierCalled[0], "Message supplier should be called for failing assertion");
    }

    /**
     * ## Test: Null Parameter Handling - First Parameter
     *
     * Verifies that `assertGreaterThan` throws a `NullPointerException` when the
     * first parameter is null. This tests the method's adherence to its contract
     * that requires non-null parameters annotated with `@NonNull`.
     *
     * **Test Scenario:**
     * - First parameter: null
     * - Second parameter: valid Integer
     * - Expected result: NullPointerException
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("assertGreaterThan throws NullPointerException when first parameter is null")
    void testAssertGreaterThanNullFirstParameter() {
        assertThrows(NullPointerException.class,
            () -> assertGreaterThan(null, 5));
    }

    /**
     * ## Test: Null Parameter Handling - Second Parameter
     *
     * Verifies that `assertGreaterThan` throws a `NullPointerException` when the
     * second parameter is null. This complements the first null parameter test
     * and ensures both parameters are properly validated.
     *
     * **Test Scenario:**
     * - First parameter: valid Integer
     * - Second parameter: null
     * - Expected result: NullPointerException
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("assertGreaterThan throws NullPointerException when second parameter is null")
    void testAssertGreaterThanNullSecondParameter() {
        assertThrows(NullPointerException.class,
            () -> assertGreaterThan(5, null));
    }

    /**
     * ## Test: Null Parameter Handling - Both Parameters
     *
     * Verifies that `assertGreaterThan` throws a `NullPointerException` when both
     * parameters are null. This tests the edge case where both required
     * parameters are missing.
     *
     * **Test Scenario:**
     * - First parameter: null
     * - Second parameter: null
     * - Expected result: NullPointerException
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("assertGreaterThan throws NullPointerException when both parameters are null")
    void testAssertGreaterThanBothParametersNull() {
        assertThrows(NullPointerException.class,
            () -> assertGreaterThan(null, null));
    }

    /**
     * ## Test: Error Message Content Verification
     *
     * Verifies that the default error message format matches the expected pattern
     * shown in the method's Javadoc. This ensures consistency between documented
     * behavior and actual implementation.
     *
     * **Test Scenario:**
     * - Compare 3 vs 10 (should fail)
     * - Verify message contains "Expected 3 to be greater than 10"
     */
    @Test
    @DisplayName("assertGreaterThan error message follows expected format")
    void testAssertGreaterThanErrorMessageFormat() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(3, 10));

        String message = error.getMessage();
        assertTrue(message.contains("Expected 3 to be greater than 10"),
            "Error message should follow format: 'Expected <first> to be greater than <second>'");
    }

    /**
     * ## Test: Mixed Comparable Types - Same Interface
     *
     * Verifies that `assertGreaterThan` works when comparing different implementations
     * of the same `Comparable` interface. This tests type compatibility within
     * the bounds of the generic type parameter.
     *
     * **Test Scenario:**
     * - Compare different Integer objects (auto-boxing)
     * - Ensure the method handles object identity vs value comparison correctly
     */
    @Test
    @DisplayName("assertGreaterThan works with different instances of same Comparable type")
    void testAssertGreaterThanDifferentInstancesSameType() {
        Integer first = 5;
        Integer second = 3;

        assertDoesNotThrow(() -> assertGreaterThan(first, second));

        Integer equal1 = 100;
        Integer equal2 = 100;

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(equal1, equal2));
        assertTrue(error.getMessage().contains("Expected 100 to be greater than 100"));
    }

    /**
     * ## Test: All Method Overloads - Success Cases
     *
     * Comprehensive test ensuring all three method overloads work correctly
     * for success cases. This verifies that each variant properly delegates
     * to the core comparison logic.
     *
     * **Test Scenarios:**
     * - `assertGreaterThan(T, T)` - basic overload
     * - `assertGreaterThan(T, T, String)` - with string message
     * - `assertGreaterThan(T, T, Supplier<String>)` - with message supplier
     */
    @Test
    @DisplayName("All assertGreaterThan overloads work for success cases")
    void testAllOverloadsSuccessCases() {
        // Basic overload
        assertDoesNotThrow(() -> assertGreaterThan(2, 1));

        // String message overload
        assertDoesNotThrow(() -> assertGreaterThan(2, 1, "Should not fail"));

        // Message supplier overload
        assertDoesNotThrow(() -> assertGreaterThan(2, 1, () -> "Should not fail"));
    }

    /**
     * ## Test: All Method Overloads - Failure Cases
     *
     * Comprehensive test ensuring all three method overloads properly fail
     * and throw `AssertionFailedError` when the comparison condition is not met.
     * This verifies consistent failure behavior across all variants.
     *
     * **Test Scenarios:**
     * - All three overloads should throw AssertionFailedError
     * - Each should handle their respective message parameter correctly
     */
    @Test
    @DisplayName("All assertGreaterThan overloads fail appropriately")
    void testAllOverloadsFailureCases() {
        // Basic overload
        assertThrows(AssertionFailedError.class, () -> assertGreaterThan(3, 5));

        // String message overload
        assertThrows(AssertionFailedError.class, () -> assertGreaterThan(3, 5, "Custom message"));

        // Message supplier overload
        assertThrows(AssertionFailedError.class, () -> assertGreaterThan(3, 5, () -> "Supplier message"));
    }

    /**
     * ## Test: Integration with Main Assertions Class
     *
     * Verifies that the `assertGreaterThan` methods are properly integrated with the
     * main `Assertions` class and work identically to the direct `AssertGreaterThan` methods.
     * This ensures users can import from either location with consistent behavior.
     *
     * **Test Scenarios:**
     * - All three overloads accessible via the main Assertions class
     * - Behavior should be identical to direct AssertGreaterThan usage
     * - Success and failure cases work the same way
     */
    @Test
    @DisplayName("assertGreaterThan integrates properly with main Assertions class")
    void testAssertionsClassIntegration() {
        // Test success cases via main Assertions class
        assertDoesNotThrow(() -> Assertions.assertGreaterThan(2, 1));
        assertDoesNotThrow(() -> Assertions.assertGreaterThan(2, 1, "Should pass"));
        assertDoesNotThrow(() -> Assertions.assertGreaterThan(2, 1, () -> "Should pass"));

        // Test failure cases via main Assertions class
        assertThrows(AssertionFailedError.class, () -> Assertions.assertGreaterThan(3, 5));
        assertThrows(AssertionFailedError.class, () -> Assertions.assertGreaterThan(3, 5, "Should fail"));
        assertThrows(AssertionFailedError.class, () -> Assertions.assertGreaterThan(3, 5, () -> "Should fail"));

        // Verify error messages are consistent
        AssertionFailedError directError = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThan(3, 10));
        AssertionFailedError assertionsError = assertThrows(AssertionFailedError.class,
            () -> Assertions.assertGreaterThan(3, 10));

        assertTrue(directError.getMessage().contains("Expected 3 to be greater than 10"));
        assertTrue(assertionsError.getMessage().contains("Expected 3 to be greater than 10"));
        assertEquals(directError.getMessage(), assertionsError.getMessage(),
            "Error messages should be identical when called via Assertions class");
    }
}