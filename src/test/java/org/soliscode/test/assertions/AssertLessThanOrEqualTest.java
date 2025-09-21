package org.soliscode.test.assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.AssertLessThanOrEqual.assertLessThanOrEqual;

/**
 * Unit tests for the {@link AssertLessThanOrEqual} class.
 *
 * ## Test Coverage
 *
 * This test class provides comprehensive coverage for all methods in the `AssertLessThanOrEqual` class,
 * testing various scenarios including:
 *
 * - **Basic comparisons** with different `Comparable` types (Integer, String, Double, BigDecimal, LocalDate)
 * - **Method overloads** testing all three variants (no message, String message, Supplier message)
 * - **Success cases** where the first value is less than or equal to the second
 * - **Failure cases** where assertions should fail (first > second)
 * - **Edge cases** including null parameters and custom message evaluation
 * - **Message verification** ensuring proper error messages and lazy evaluation
 *
 * @author evanbergstrom
 * @see AssertLessThanOrEqual
 */
@DisplayName("AssertLessThanOrEqual Tests")
class AssertLessThanOrEqualTest {

    /**
     * ## Test: Basic Integer Comparison - Success Case (Less Than)
     *
     * Verifies that `assertLessThanOrEqual` passes when comparing two integers where the first
     * is genuinely less than the second. This tests the "less than" part of the
     * assertion method using the natural ordering of integers.
     *
     * **Test Scenario:**
     * - First value: `3`
     * - Second value: `5`
     * - Expected result: Assertion passes (3 <= 5)
     */
    @Test
    @DisplayName("assertLessThanOrEqual passes with integers when first < second")
    void testAssertLessThanOrEqualIntegerLessSuccess() {
        assertDoesNotThrow(() -> assertLessThanOrEqual(3, 5));
        assertDoesNotThrow(() -> assertLessThanOrEqual(-10, -5));
        assertDoesNotThrow(() -> assertLessThanOrEqual(0, 1));
    }

    /**
     * ## Test: Basic Integer Comparison - Success Case (Equal)
     *
     * Verifies that `assertLessThanOrEqual` passes when comparing two equal integers.
     * This tests the "equal to" part of the assertion method, which should pass
     * since equal values satisfy the "less than or equal to" requirement.
     *
     * **Test Scenario:**
     * - First value: `5`
     * - Second value: `5`
     * - Expected result: Assertion passes (5 <= 5)
     */
    @Test
    @DisplayName("assertLessThanOrEqual passes with equal integers")
    void testAssertLessThanOrEqualIntegerEqualSuccess() {
        assertDoesNotThrow(() -> assertLessThanOrEqual(5, 5));
        assertDoesNotThrow(() -> assertLessThanOrEqual(0, 0));
        assertDoesNotThrow(() -> assertLessThanOrEqual(-10, -10));
    }

    /**
     * ## Test: Basic Integer Comparison - Failure Case (First > Second)
     *
     * Verifies that `assertLessThanOrEqual` fails when the first value is greater than
     * the second value. This tests the core failure condition of the assertion.
     *
     * **Test Scenario:**
     * - First value: `10`
     * - Second value: `5`
     * - Expected result: AssertionFailedError (10 is not <= 5)
     */
    @Test
    @DisplayName("assertLessThanOrEqual fails when first > second")
    void testAssertLessThanOrEqualIntegerGreaterFails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual(10, 5));

        assertTrue(error.getMessage().contains("Expected 10 to be less than or equal to 5"));
    }

    /**
     * ## Test: String Comparison - Success Case
     *
     * Verifies that `assertLessThanOrEqual` works correctly with String objects using
     * their natural alphabetical ordering. This tests that the method works
     * with different types of `Comparable` objects beyond just numbers.
     *
     * **Test Scenarios:**
     * - "apple" <= "banana" (alphabetical ordering)
     * - "apple" <= "apple" (equal strings)
     * - "a" <= "b" (single character comparison)
     * - "" <= "a" (empty string vs non-empty)
     */
    @Test
    @DisplayName("assertLessThanOrEqual passes with strings in alphabetical order and equal")
    void testAssertLessThanOrEqualStringSuccess() {
        assertDoesNotThrow(() -> assertLessThanOrEqual("apple", "banana"));
        assertDoesNotThrow(() -> assertLessThanOrEqual("apple", "apple"));
        assertDoesNotThrow(() -> assertLessThanOrEqual("a", "b"));
        assertDoesNotThrow(() -> assertLessThanOrEqual("", "a"));
    }

    /**
     * ## Test: String Comparison - Failure Case
     *
     * Verifies that `assertLessThanOrEqual` properly fails when strings are not in
     * the expected alphabetical order or equal.
     *
     * **Test Scenario:**
     * - First value: "zebra"
     * - Second value: "apple"
     * - Expected result: AssertionFailedError ("zebra" is not <= "apple")
     */
    @Test
    @DisplayName("assertLessThanOrEqual fails with strings in reverse alphabetical order")
    void testAssertLessThanOrEqualStringFails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual("zebra", "apple"));

        assertTrue(error.getMessage().contains("Expected zebra to be less than or equal to apple"));
    }

    /**
     * ## Test: Double Precision Comparison
     *
     * Verifies that `assertLessThanOrEqual` works correctly with floating-point numbers,
     * testing both success cases (less than and equal) and failure cases with double precision values.
     *
     * **Test Scenarios:**
     * - 2.5 <= 3.0 (success case - less than)
     * - 3.0 <= 3.0 (success case - equal)
     * - 3.14159 <= 3.14160 (high precision comparison)
     * - 5.0 <= 3.0 (failure case - greater than)
     */
    @Test
    @DisplayName("assertLessThanOrEqual works with double precision numbers")
    void testAssertLessThanOrEqualDouble() {
        assertDoesNotThrow(() -> assertLessThanOrEqual(2.5, 3.0));
        assertDoesNotThrow(() -> assertLessThanOrEqual(3.0, 3.0));
        assertDoesNotThrow(() -> assertLessThanOrEqual(3.14159, 3.14160));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual(5.0, 3.0));
        assertTrue(error.getMessage().contains("Expected 5.0 to be less than or equal to 3.0"));
    }

    /**
     * ## Test: BigDecimal High Precision Comparison
     *
     * Verifies that `assertLessThanOrEqual` works with `BigDecimal` objects, which
     * implement `Comparable` and provide arbitrary precision arithmetic.
     * This ensures the method works with complex comparable types.
     *
     * **Test Scenarios:**
     * - 1.23 <= 1.24 (success with decimal precision - less than)
     * - 1.23 <= 1.23 (success with decimal precision - equal)
     * - 1.24 <= 1.23 (failure - greater than)
     */
    @Test
    @DisplayName("assertLessThanOrEqual works with BigDecimal objects")
    void testAssertLessThanOrEqualBigDecimal() {
        BigDecimal smaller = new BigDecimal("1.23");
        BigDecimal larger = new BigDecimal("1.24");
        BigDecimal equal = new BigDecimal("1.23");

        assertDoesNotThrow(() -> assertLessThanOrEqual(smaller, larger));
        assertDoesNotThrow(() -> assertLessThanOrEqual(smaller, equal));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual(larger, smaller));
        assertTrue(error.getMessage().contains("Expected 1.24 to be less than or equal to 1.23"));
    }

    /**
     * ## Test: LocalDate Temporal Comparison
     *
     * Verifies that `assertLessThanOrEqual` works with temporal objects like `LocalDate`,
     * testing chronological ordering of dates.
     *
     * **Test Scenarios:**
     * - Earlier date <= later date (2023-01-01 <= 2023-01-02)
     * - Same date <= same date (2023-01-01 <= 2023-01-01)
     * - Later date <= earlier date (should fail)
     */
    @Test
    @DisplayName("assertLessThanOrEqual works with LocalDate objects")
    void testAssertLessThanOrEqualLocalDate() {
        LocalDate earlier = LocalDate.of(2023, 1, 1);
        LocalDate later = LocalDate.of(2023, 1, 2);
        LocalDate same = LocalDate.of(2023, 1, 1);

        assertDoesNotThrow(() -> assertLessThanOrEqual(earlier, later));
        assertDoesNotThrow(() -> assertLessThanOrEqual(earlier, same));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual(later, earlier));
        assertTrue(error.getMessage().contains("Expected 2023-01-02 to be less than or equal to 2023-01-01"));
    }

    /**
     * ## Test: Custom Error Message - String Overload
     *
     * Verifies that the `assertLessThanOrEqual(T, T, String)` overload properly uses
     * the provided custom error message when the assertion fails. This tests
     * the second method variant that accepts a static error message.
     *
     * **Test Scenario:**
     * - Failing assertion with custom message: "Custom error message"
     * - Expected result: AssertionFailedError containing the custom message
     */
    @Test
    @DisplayName("assertLessThanOrEqual with custom string message displays correct error")
    void testAssertLessThanOrEqualWithStringMessage() {
        String customMessage = "Custom error message";

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual(10, 5, customMessage));

        assertTrue(error.getMessage().contains(customMessage));
    }

    /**
     * ## Test: Custom Error Message - Supplier Overload
     *
     * Verifies that the `assertLessThanOrEqual(T, T, Supplier<String>)` overload properly
     * uses the message supplier when the assertion fails. This tests the third
     * method variant that accepts a message supplier for lazy evaluation.
     *
     * **Test Scenario:**
     * - Failing assertion with message supplier
     * - Expected result: AssertionFailedError containing the supplied message
     */
    @Test
    @DisplayName("assertLessThanOrEqual with message supplier displays correct error")
    void testAssertLessThanOrEqualWithMessageSupplier() {
        String suppliedMessage = "Supplied error message";
        Supplier<String> messageSupplier = () -> suppliedMessage;

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual(10, 5, messageSupplier));

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
    @DisplayName("assertLessThanOrEqual message supplier is lazily evaluated")
    void testAssertLessThanOrEqualMessageSupplierLazyEvaluation() {
        final boolean[] supplierCalled = {false};
        Supplier<String> messageSupplier = () -> {
            supplierCalled[0] = true;
            return "Lazy message";
        };

        // Passing assertion - supplier should not be called
        assertDoesNotThrow(() -> assertLessThanOrEqual(3, 5, messageSupplier));
        assertFalse(supplierCalled[0], "Message supplier should not be called for passing assertion");

        // Failing assertion - supplier should be called
        assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual(10, 5, messageSupplier));
        assertTrue(supplierCalled[0], "Message supplier should be called for failing assertion");
    }

    /**
     * ## Test: Null Parameter Handling - First Parameter
     *
     * Verifies that `assertLessThanOrEqual` throws a `NullPointerException` when the
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
    @DisplayName("assertLessThanOrEqual throws NullPointerException when first parameter is null")
    void testAssertLessThanOrEqualNullFirstParameter() {
        assertThrows(NullPointerException.class,
            () -> assertLessThanOrEqual(null, 5));
    }

    /**
     * ## Test: Null Parameter Handling - Second Parameter
     *
     * Verifies that `assertLessThanOrEqual` throws a `NullPointerException` when the
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
    @DisplayName("assertLessThanOrEqual throws NullPointerException when second parameter is null")
    void testAssertLessThanOrEqualNullSecondParameter() {
        assertThrows(NullPointerException.class,
            () -> assertLessThanOrEqual(5, null));
    }

    /**
     * ## Test: Null Parameter Handling - Both Parameters
     *
     * Verifies that `assertLessThanOrEqual` throws a `NullPointerException` when both
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
    @DisplayName("assertLessThanOrEqual throws NullPointerException when both parameters are null")
    void testAssertLessThanOrEqualBothParametersNull() {
        assertThrows(NullPointerException.class,
            () -> assertLessThanOrEqual(null, null));
    }

    /**
     * ## Test: Error Message Content Verification
     *
     * Verifies that the default error message format matches the expected pattern
     * shown in the method's Javadoc. This ensures consistency between documented
     * behavior and actual implementation.
     *
     * **Test Scenario:**
     * - Compare 10 vs 5 (should fail)
     * - Verify message contains "Expected 10 to be less than or equal to 5"
     */
    @Test
    @DisplayName("assertLessThanOrEqual error message follows expected format")
    void testAssertLessThanOrEqualErrorMessageFormat() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual(10, 5));

        String message = error.getMessage();
        assertTrue(message.contains("Expected 10 to be less than or equal to 5"),
            "Error message should follow format: 'Expected <first> to be less than or equal to <second>'");
    }

    /**
     * ## Test: Mixed Comparable Types - Same Interface
     *
     * Verifies that `assertLessThanOrEqual` works when comparing different implementations
     * of the same `Comparable` interface. This tests type compatibility within
     * the bounds of the generic type parameter.
     *
     * **Test Scenario:**
     * - Compare different Integer objects (auto-boxing)
     * - Ensure the method handles object identity vs value comparison correctly
     */
    @Test
    @DisplayName("assertLessThanOrEqual works with different instances of same Comparable type")
    void testAssertLessThanOrEqualDifferentInstancesSameType() {
        Integer first = 3;
        Integer second = 5;

        assertDoesNotThrow(() -> assertLessThanOrEqual(first, second));

        Integer equal1 = 100;
        Integer equal2 = 100;
        assertDoesNotThrow(() -> assertLessThanOrEqual(equal1, equal2));

        Integer larger = 200;
        Integer smaller = 100;
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual(larger, smaller));
        assertTrue(error.getMessage().contains("Expected 200 to be less than or equal to 100"));
    }

    /**
     * ## Test: All Method Overloads - Success Cases
     *
     * Comprehensive test ensuring all three method overloads work correctly
     * for success cases. This verifies that each variant properly delegates
     * to the core comparison logic.
     *
     * **Test Scenarios:**
     * - `assertLessThanOrEqual(T, T)` - basic overload
     * - `assertLessThanOrEqual(T, T, String)` - with string message
     * - `assertLessThanOrEqual(T, T, Supplier<String>)` - with message supplier
     */
    @Test
    @DisplayName("All assertLessThanOrEqual overloads work for success cases")
    void testAllOverloadsSuccessCases() {
        // Basic overload - less than
        assertDoesNotThrow(() -> assertLessThanOrEqual(1, 2));
        // Basic overload - equal
        assertDoesNotThrow(() -> assertLessThanOrEqual(2, 2));

        // String message overload - less than
        assertDoesNotThrow(() -> assertLessThanOrEqual(1, 2, "Should not fail"));
        // String message overload - equal
        assertDoesNotThrow(() -> assertLessThanOrEqual(2, 2, "Should not fail"));

        // Message supplier overload - less than
        assertDoesNotThrow(() -> assertLessThanOrEqual(1, 2, () -> "Should not fail"));
        // Message supplier overload - equal
        assertDoesNotThrow(() -> assertLessThanOrEqual(2, 2, () -> "Should not fail"));
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
    @DisplayName("All assertLessThanOrEqual overloads fail appropriately")
    void testAllOverloadsFailureCases() {
        // Basic overload
        assertThrows(AssertionFailedError.class, () -> assertLessThanOrEqual(5, 3));

        // String message overload
        assertThrows(AssertionFailedError.class, () -> assertLessThanOrEqual(5, 3, "Custom message"));

        // Message supplier overload
        assertThrows(AssertionFailedError.class, () -> assertLessThanOrEqual(5, 3, () -> "Supplier message"));
    }

    /**
     * ## Test: Integration with Main Assertions Class
     *
     * Verifies that the `assertLessThanOrEqual` methods are properly integrated with the
     * main `Assertions` class and work identically to the direct `AssertLessThanOrEqual` methods.
     * This ensures users can import from either location with consistent behavior.
     *
     * **Test Scenarios:**
     * - All three overloads accessible via the main Assertions class
     * - Behavior should be identical to direct AssertLessThanOrEqual usage
     * - Success and failure cases work the same way
     */
    @Test
    @DisplayName("assertLessThanOrEqual integrates properly with main Assertions class")
    void testAssertionsClassIntegration() {
        // Test success cases via main Assertions class
        assertDoesNotThrow(() -> Assertions.assertLessThanOrEqual(1, 2));
        assertDoesNotThrow(() -> Assertions.assertLessThanOrEqual(2, 2));
        assertDoesNotThrow(() -> Assertions.assertLessThanOrEqual(1, 2, "Should pass"));
        assertDoesNotThrow(() -> Assertions.assertLessThanOrEqual(1, 2, () -> "Should pass"));

        // Test failure cases via main Assertions class
        assertThrows(AssertionFailedError.class, () -> Assertions.assertLessThanOrEqual(5, 3));
        assertThrows(AssertionFailedError.class, () -> Assertions.assertLessThanOrEqual(5, 3, "Should fail"));
        assertThrows(AssertionFailedError.class, () -> Assertions.assertLessThanOrEqual(5, 3, () -> "Should fail"));

        // Verify error messages are consistent
        AssertionFailedError directError = assertThrows(AssertionFailedError.class,
            () -> assertLessThanOrEqual(10, 5));
        AssertionFailedError assertionsError = assertThrows(AssertionFailedError.class,
            () -> Assertions.assertLessThanOrEqual(10, 5));

        assertTrue(directError.getMessage().contains("Expected 10 to be less than or equal to 5"));
        assertTrue(assertionsError.getMessage().contains("Expected 10 to be less than or equal to 5"));
        assertEquals(directError.getMessage(), assertionsError.getMessage(),
            "Error messages should be identical when called via Assertions class");
    }
}