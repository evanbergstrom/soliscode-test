package org.soliscode.test.assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.AssertGreaterThanOrEqual.assertGreaterThanOrEqual;

/**
 * Unit tests for the {@link AssertGreaterThanOrEqual} class.
 *
 * ## Test Coverage
 *
 * This test class provides comprehensive coverage for all methods in the `AssertGreaterThanOrEqual` class,
 * testing various scenarios including:
 *
 * - **Basic comparisons** with different `Comparable` types (Integer, String, Double, BigDecimal, LocalDate)
 * - **Method overloads** testing all three variants (no message, String message, Supplier message)
 * - **Success cases** where the first value is greater than or equal to the second
 * - **Failure cases** where assertions should fail (first < second)
 * - **Edge cases** including null parameters and custom message evaluation
 * - **Message verification** ensuring proper error messages and lazy evaluation
 *
 * @author evanbergstrom
 * @see AssertGreaterThanOrEqual
 */
@DisplayName("AssertGreaterThanOrEqual Tests")
class AssertGreaterThanOrEqualTest {

    /**
     * ## Test: Basic Integer Comparison - Success Case (Greater Than)
     *
     * Verifies that `assertGreaterThanOrEqual` passes when comparing two integers where the first
     * is genuinely greater than the second. This tests the "greater than" part of the
     * assertion method using the natural ordering of integers.
     *
     * **Test Scenario:**
     * - First value: `5`
     * - Second value: `3`
     * - Expected result: Assertion passes (5 >= 3)
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual passes with integers when first > second")
    void testAssertGreaterThanOrEqualIntegerGreaterSuccess() {
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(5, 3));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(-5, -10));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(1, 0));
    }

    /**
     * ## Test: Basic Integer Comparison - Success Case (Equal)
     *
     * Verifies that `assertGreaterThanOrEqual` passes when comparing two equal integers.
     * This tests the "equal to" part of the assertion method, which should pass
     * since equal values satisfy the "greater than or equal to" requirement.
     *
     * **Test Scenario:**
     * - First value: `5`
     * - Second value: `5`
     * - Expected result: Assertion passes (5 >= 5)
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual passes with equal integers")
    void testAssertGreaterThanOrEqualIntegerEqualSuccess() {
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(5, 5));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(0, 0));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(-10, -10));
    }

    /**
     * ## Test: Basic Integer Comparison - Failure Case (First < Second)
     *
     * Verifies that `assertGreaterThanOrEqual` fails when the first value is less than
     * the second value. This tests the core failure condition of the assertion.
     *
     * **Test Scenario:**
     * - First value: `3`
     * - Second value: `10`
     * - Expected result: AssertionFailedError (3 is not >= 10)
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual fails when first < second")
    void testAssertGreaterThanOrEqualIntegerLessFails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual(3, 10));

        assertTrue(error.getMessage().contains("Expected 3 to be greater than or equal to 10"));
    }

    /**
     * ## Test: String Comparison - Success Case
     *
     * Verifies that `assertGreaterThanOrEqual` works correctly with String objects using
     * their natural alphabetical ordering. This tests that the method works
     * with different types of `Comparable` objects beyond just numbers.
     *
     * **Test Scenarios:**
     * - "banana" >= "apple" (reverse alphabetical ordering)
     * - "apple" >= "apple" (equal strings)
     * - "b" >= "a" (single character comparison)
     * - "a" >= "" (non-empty string vs empty)
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual passes with strings in reverse alphabetical order and equal")
    void testAssertGreaterThanOrEqualStringSuccess() {
        assertDoesNotThrow(() -> assertGreaterThanOrEqual("banana", "apple"));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual("apple", "apple"));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual("b", "a"));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual("a", ""));
    }

    /**
     * ## Test: String Comparison - Failure Case
     *
     * Verifies that `assertGreaterThanOrEqual` properly fails when strings are in
     * alphabetical order (first < second).
     *
     * **Test Scenario:**
     * - First value: "apple"
     * - Second value: "zebra"
     * - Expected result: AssertionFailedError ("apple" is not >= "zebra")
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual fails with strings in alphabetical order")
    void testAssertGreaterThanOrEqualStringFails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual("apple", "zebra"));

        assertTrue(error.getMessage().contains("Expected apple to be greater than or equal to zebra"));
    }

    /**
     * ## Test: Double Precision Comparison
     *
     * Verifies that `assertGreaterThanOrEqual` works correctly with floating-point numbers,
     * testing both success cases (greater than and equal) and failure cases with double precision values.
     *
     * **Test Scenarios:**
     * - 3.0 >= 2.5 (success case - greater than)
     * - 3.0 >= 3.0 (success case - equal)
     * - 3.14160 >= 3.14159 (high precision comparison)
     * - 2.5 >= 5.0 (failure case - less than)
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual works with double precision numbers")
    void testAssertGreaterThanOrEqualDouble() {
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(3.0, 2.5));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(3.0, 3.0));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(3.14160, 3.14159));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual(2.5, 5.0));
        assertTrue(error.getMessage().contains("Expected 2.5 to be greater than or equal to 5.0"));
    }

    /**
     * ## Test: BigDecimal High Precision Comparison
     *
     * Verifies that `assertGreaterThanOrEqual` works with `BigDecimal` objects, which
     * implement `Comparable` and provide arbitrary precision arithmetic.
     * This ensures the method works with complex comparable types.
     *
     * **Test Scenarios:**
     * - 1.24 >= 1.23 (success with decimal precision - greater than)
     * - 1.23 >= 1.23 (success with decimal precision - equal)
     * - 1.23 >= 1.24 (failure - less than)
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual works with BigDecimal objects")
    void testAssertGreaterThanOrEqualBigDecimal() {
        BigDecimal smaller = new BigDecimal("1.23");
        BigDecimal larger = new BigDecimal("1.24");
        BigDecimal equal = new BigDecimal("1.23");

        assertDoesNotThrow(() -> assertGreaterThanOrEqual(larger, smaller));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(smaller, equal));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual(smaller, larger));
        assertTrue(error.getMessage().contains("Expected 1.23 to be greater than or equal to 1.24"));
    }

    /**
     * ## Test: LocalDate Temporal Comparison
     *
     * Verifies that `assertGreaterThanOrEqual` works with temporal objects like `LocalDate`,
     * testing chronological ordering of dates.
     *
     * **Test Scenarios:**
     * - Later date >= earlier date (2023-01-02 >= 2023-01-01)
     * - Same date >= same date (2023-01-01 >= 2023-01-01)
     * - Earlier date >= later date (should fail)
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual works with LocalDate objects")
    void testAssertGreaterThanOrEqualLocalDate() {
        LocalDate earlier = LocalDate.of(2023, 1, 1);
        LocalDate later = LocalDate.of(2023, 1, 2);
        LocalDate same = LocalDate.of(2023, 1, 1);

        assertDoesNotThrow(() -> assertGreaterThanOrEqual(later, earlier));
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(earlier, same));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual(earlier, later));
        assertTrue(error.getMessage().contains("Expected 2023-01-01 to be greater than or equal to 2023-01-02"));
    }

    /**
     * ## Test: Custom Error Message - String Overload
     *
     * Verifies that the `assertGreaterThanOrEqual(T, T, String)` overload properly uses
     * the provided custom error message when the assertion fails. This tests
     * the second method variant that accepts a static error message.
     *
     * **Test Scenario:**
     * - Failing assertion with custom message: "Custom error message"
     * - Expected result: AssertionFailedError containing the custom message
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual with custom string message displays correct error")
    void testAssertGreaterThanOrEqualWithStringMessage() {
        String customMessage = "Custom error message";

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual(3, 10, customMessage));

        assertTrue(error.getMessage().contains(customMessage));
    }

    /**
     * ## Test: Custom Error Message - Supplier Overload
     *
     * Verifies that the `assertGreaterThanOrEqual(T, T, Supplier<String>)` overload properly
     * uses the message supplier when the assertion fails. This tests the third
     * method variant that accepts a message supplier for lazy evaluation.
     *
     * **Test Scenario:**
     * - Failing assertion with message supplier
     * - Expected result: AssertionFailedError containing the supplied message
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual with message supplier displays correct error")
    void testAssertGreaterThanOrEqualWithMessageSupplier() {
        String suppliedMessage = "Supplied error message";
        Supplier<String> messageSupplier = () -> suppliedMessage;

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual(3, 10, messageSupplier));

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
    @DisplayName("assertGreaterThanOrEqual message supplier is lazily evaluated")
    void testAssertGreaterThanOrEqualMessageSupplierLazyEvaluation() {
        final boolean[] supplierCalled = {false};
        Supplier<String> messageSupplier = () -> {
            supplierCalled[0] = true;
            return "Lazy message";
        };

        // Passing assertion - supplier should not be called
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(5, 3, messageSupplier));
        assertFalse(supplierCalled[0], "Message supplier should not be called for passing assertion");

        // Failing assertion - supplier should be called
        assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual(3, 10, messageSupplier));
        assertTrue(supplierCalled[0], "Message supplier should be called for failing assertion");
    }

    /**
     * ## Test: Null Parameter Handling - First Parameter
     *
     * Verifies that `assertGreaterThanOrEqual` throws a `NullPointerException` when the
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
    @DisplayName("assertGreaterThanOrEqual throws NullPointerException when first parameter is null")
    void testAssertGreaterThanOrEqualNullFirstParameter() {
        assertThrows(NullPointerException.class,
            () -> assertGreaterThanOrEqual(null, 5));
    }

    /**
     * ## Test: Null Parameter Handling - Second Parameter
     *
     * Verifies that `assertGreaterThanOrEqual` throws a `NullPointerException` when the
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
    @DisplayName("assertGreaterThanOrEqual throws NullPointerException when second parameter is null")
    void testAssertGreaterThanOrEqualNullSecondParameter() {
        assertThrows(NullPointerException.class,
            () -> assertGreaterThanOrEqual(5, null));
    }

    /**
     * ## Test: Null Parameter Handling - Both Parameters
     *
     * Verifies that `assertGreaterThanOrEqual` throws a `NullPointerException` when both
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
    @DisplayName("assertGreaterThanOrEqual throws NullPointerException when both parameters are null")
    void testAssertGreaterThanOrEqualBothParametersNull() {
        assertThrows(NullPointerException.class,
            () -> assertGreaterThanOrEqual(null, null));
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
     * - Verify message contains "Expected 3 to be greater than or equal to 10"
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual error message follows expected format")
    void testAssertGreaterThanOrEqualErrorMessageFormat() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual(3, 10));

        String message = error.getMessage();
        assertTrue(message.contains("Expected 3 to be greater than or equal to 10"),
            "Error message should follow format: 'Expected <first> to be greater than or equal to <second>'");
    }

    /**
     * ## Test: Mixed Comparable Types - Same Interface
     *
     * Verifies that `assertGreaterThanOrEqual` works when comparing different implementations
     * of the same `Comparable` interface. This tests type compatibility within
     * the bounds of the generic type parameter.
     *
     * **Test Scenario:**
     * - Compare different Integer objects (auto-boxing)
     * - Ensure the method handles object identity vs value comparison correctly
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual works with different instances of same Comparable type")
    void testAssertGreaterThanOrEqualDifferentInstancesSameType() {
        Integer first = 5;
        Integer second = 3;

        assertDoesNotThrow(() -> assertGreaterThanOrEqual(first, second));

        Integer equal1 = 100;
        Integer equal2 = 100;
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(equal1, equal2));

        Integer smaller = 100;
        Integer larger = 200;
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual(smaller, larger));
        assertTrue(error.getMessage().contains("Expected 100 to be greater than or equal to 200"));
    }

    /**
     * ## Test: All Method Overloads - Success Cases
     *
     * Comprehensive test ensuring all three method overloads work correctly
     * for success cases. This verifies that each variant properly delegates
     * to the core comparison logic.
     *
     * **Test Scenarios:**
     * - `assertGreaterThanOrEqual(T, T)` - basic overload
     * - `assertGreaterThanOrEqual(T, T, String)` - with string message
     * - `assertGreaterThanOrEqual(T, T, Supplier<String>)` - with message supplier
     */
    @Test
    @DisplayName("All assertGreaterThanOrEqual overloads work for success cases")
    void testAllOverloadsSuccessCases() {
        // Basic overload - greater than
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(2, 1));
        // Basic overload - equal
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(2, 2));

        // String message overload - greater than
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(2, 1, "Should not fail"));
        // String message overload - equal
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(2, 2, "Should not fail"));

        // Message supplier overload - greater than
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(2, 1, () -> "Should not fail"));
        // Message supplier overload - equal
        assertDoesNotThrow(() -> assertGreaterThanOrEqual(2, 2, () -> "Should not fail"));
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
    @DisplayName("All assertGreaterThanOrEqual overloads fail appropriately")
    void testAllOverloadsFailureCases() {
        // Basic overload
        assertThrows(AssertionFailedError.class, () -> assertGreaterThanOrEqual(3, 5));

        // String message overload
        assertThrows(AssertionFailedError.class, () -> assertGreaterThanOrEqual(3, 5, "Custom message"));

        // Message supplier overload
        assertThrows(AssertionFailedError.class, () -> assertGreaterThanOrEqual(3, 5, () -> "Supplier message"));
    }

    /**
     * ## Test: Integration with Main Assertions Class
     *
     * Verifies that the `assertGreaterThanOrEqual` methods are properly integrated with the
     * main `Assertions` class and work identically to the direct `AssertGreaterThanOrEqual` methods.
     * This ensures users can import from either location with consistent behavior.
     *
     * **Test Scenarios:**
     * - All three overloads accessible via the main Assertions class
     * - Behavior should be identical to direct AssertGreaterThanOrEqual usage
     * - Success and failure cases work the same way
     */
    @Test
    @DisplayName("assertGreaterThanOrEqual integrates properly with main Assertions class")
    void testAssertionsClassIntegration() {
        // Test success cases via main Assertions class
        assertDoesNotThrow(() -> Assertions.assertGreaterThanOrEqual(2, 1));
        assertDoesNotThrow(() -> Assertions.assertGreaterThanOrEqual(2, 2));
        assertDoesNotThrow(() -> Assertions.assertGreaterThanOrEqual(2, 1, "Should pass"));
        assertDoesNotThrow(() -> Assertions.assertGreaterThanOrEqual(2, 1, () -> "Should pass"));

        // Test failure cases via main Assertions class
        assertThrows(AssertionFailedError.class, () -> Assertions.assertGreaterThanOrEqual(3, 5));
        assertThrows(AssertionFailedError.class, () -> Assertions.assertGreaterThanOrEqual(3, 5, "Should fail"));
        assertThrows(AssertionFailedError.class, () -> Assertions.assertGreaterThanOrEqual(3, 5, () -> "Should fail"));

        // Verify error messages are consistent
        AssertionFailedError directError = assertThrows(AssertionFailedError.class,
            () -> assertGreaterThanOrEqual(3, 10));
        AssertionFailedError assertionsError = assertThrows(AssertionFailedError.class,
            () -> Assertions.assertGreaterThanOrEqual(3, 10));

        assertTrue(directError.getMessage().contains("Expected 3 to be greater than or equal to 10"));
        assertTrue(assertionsError.getMessage().contains("Expected 3 to be greater than or equal to 10"));
        assertEquals(directError.getMessage(), assertionsError.getMessage(),
            "Error messages should be identical when called via Assertions class");
    }
}