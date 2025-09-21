package org.soliscode.test.assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.AssertLessThan.assertLessThan;

/**
 * Unit tests for the {@link AssertLessThan} class.
 *
 * ## Test Coverage
 *
 * This test class provides comprehensive coverage for all methods in the `AssertLessThan` class,
 * testing various scenarios including:
 *
 * - **Basic comparisons** with different `Comparable` types (Integer, String, Double, BigDecimal, LocalDate)
 * - **Method overloads** testing all three variants (no message, String message, Supplier message)
 * - **Success cases** where the first value is genuinely less than the second
 * - **Failure cases** where assertions should fail (equal values, first > second)
 * - **Edge cases** including null parameters and custom message evaluation
 * - **Message verification** ensuring proper error messages and lazy evaluation
 *
 * @author evanbergstrom
 * @see AssertLessThan
 */
@DisplayName("AssertLessThan Tests")
class AssertLessThanTest {

    /**
     * ## Test: Basic Integer Comparison - Success Case
     *
     * Verifies that `assertLessThan` passes when comparing two integers where the first
     * is genuinely less than the second. This tests the fundamental functionality of
     * the assertion method using the natural ordering of integers.
     *
     * **Test Scenario:**
     * - First value: `3`
     * - Second value: `5`
     * - Expected result: Assertion passes (3 < 5)
     */
    @Test
    @DisplayName("assertLessThan passes with integers when first < second")
    void testAssertLessThanIntegerSuccess() {
        assertDoesNotThrow(() -> assertLessThan(3, 5));
        assertDoesNotThrow(() -> assertLessThan(-10, -5));
        assertDoesNotThrow(() -> assertLessThan(0, 1));
    }

    /**
     * ## Test: Basic Integer Comparison - Failure Case (Equal Values)
     *
     * Verifies that `assertLessThan` fails when comparing two equal integers.
     * The assertion should throw an `AssertionFailedError` since equal values
     * don't satisfy the "less than" requirement.
     *
     * **Test Scenario:**
     * - First value: `5`
     * - Second value: `5`
     * - Expected result: AssertionFailedError (5 is not < 5)
     */
    @Test
    @DisplayName("assertLessThan fails with equal integers")
    void testAssertLessThanIntegerEqualFails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThan(5, 5));

        assertTrue(error.getMessage().contains("Expected 5 to be less than 5"));
    }

    /**
     * ## Test: Basic Integer Comparison - Failure Case (First > Second)
     *
     * Verifies that `assertLessThan` fails when the first value is greater than
     * the second value. This tests the core failure condition of the assertion.
     *
     * **Test Scenario:**
     * - First value: `10`
     * - Second value: `5`
     * - Expected result: AssertionFailedError (10 is not < 5)
     */
    @Test
    @DisplayName("assertLessThan fails when first > second")
    void testAssertLessThanIntegerGreaterFails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThan(10, 5));

        assertTrue(error.getMessage().contains("Expected 10 to be less than 5"));
    }

    /**
     * ## Test: String Comparison - Success Case
     *
     * Verifies that `assertLessThan` works correctly with String objects using
     * their natural alphabetical ordering. This tests that the method works
     * with different types of `Comparable` objects beyond just numbers.
     *
     * **Test Scenarios:**
     * - "apple" < "banana" (alphabetical ordering)
     * - "a" < "b" (single character comparison)
     * - "" < "a" (empty string vs non-empty)
     */
    @Test
    @DisplayName("assertLessThan passes with strings in alphabetical order")
    void testAssertLessThanStringSuccess() {
        assertDoesNotThrow(() -> assertLessThan("apple", "banana"));
        assertDoesNotThrow(() -> assertLessThan("a", "b"));
        assertDoesNotThrow(() -> assertLessThan("", "a"));
    }

    /**
     * ## Test: String Comparison - Failure Case
     *
     * Verifies that `assertLessThan` properly fails when strings are not in
     * the expected alphabetical order.
     *
     * **Test Scenario:**
     * - First value: "zebra"
     * - Second value: "apple"
     * - Expected result: AssertionFailedError ("zebra" is not < "apple")
     */
    @Test
    @DisplayName("assertLessThan fails with strings in wrong order")
    void testAssertLessThanStringFails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThan("zebra", "apple"));

        assertTrue(error.getMessage().contains("Expected zebra to be less than apple"));
    }

    /**
     * ## Test: Double Precision Comparison
     *
     * Verifies that `assertLessThan` works correctly with floating-point numbers,
     * testing both success and failure cases with double precision values.
     *
     * **Test Scenarios:**
     * - 2.5 < 3.0 (success case)
     * - 3.14159 < 3.14160 (high precision comparison)
     * - 5.0 vs 5.0 (equal doubles should fail)
     */
    @Test
    @DisplayName("assertLessThan works with double precision numbers")
    void testAssertLessThanDouble() {
        assertDoesNotThrow(() -> assertLessThan(2.5, 3.0));
        assertDoesNotThrow(() -> assertLessThan(3.14159, 3.14160));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThan(5.0, 5.0));
        assertTrue(error.getMessage().contains("Expected 5.0 to be less than 5.0"));
    }

    /**
     * ## Test: BigDecimal High Precision Comparison
     *
     * Verifies that `assertLessThan` works with `BigDecimal` objects, which
     * implement `Comparable` and provide arbitrary precision arithmetic.
     * This ensures the method works with complex comparable types.
     *
     * **Test Scenarios:**
     * - 1.23 < 1.24 (success with decimal precision)
     * - Equal BigDecimal values should fail
     */
    @Test
    @DisplayName("assertLessThan works with BigDecimal objects")
    void testAssertLessThanBigDecimal() {
        BigDecimal smaller = new BigDecimal("1.23");
        BigDecimal larger = new BigDecimal("1.24");
        BigDecimal equal = new BigDecimal("1.23");

        assertDoesNotThrow(() -> assertLessThan(smaller, larger));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThan(smaller, equal));
        assertTrue(error.getMessage().contains("Expected 1.23 to be less than 1.23"));
    }

    /**
     * ## Test: LocalDate Temporal Comparison
     *
     * Verifies that `assertLessThan` works with temporal objects like `LocalDate`,
     * testing chronological ordering of dates.
     *
     * **Test Scenarios:**
     * - Earlier date < later date (2023-01-01 < 2023-01-02)
     * - Same date should fail the assertion
     */
    @Test
    @DisplayName("assertLessThan works with LocalDate objects")
    void testAssertLessThanLocalDate() {
        LocalDate earlier = LocalDate.of(2023, 1, 1);
        LocalDate later = LocalDate.of(2023, 1, 2);
        LocalDate same = LocalDate.of(2023, 1, 1);

        assertDoesNotThrow(() -> assertLessThan(earlier, later));

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThan(earlier, same));
        assertTrue(error.getMessage().contains("Expected 2023-01-01 to be less than 2023-01-01"));
    }

    /**
     * ## Test: Custom Error Message - String Overload
     *
     * Verifies that the `assertLessThan(T, T, String)` overload properly uses
     * the provided custom error message when the assertion fails. This tests
     * the second method variant that accepts a static error message.
     *
     * **Test Scenario:**
     * - Failing assertion with custom message: "Custom error message"
     * - Expected result: AssertionFailedError containing the custom message
     */
    @Test
    @DisplayName("assertLessThan with custom string message displays correct error")
    void testAssertLessThanWithStringMessage() {
        String customMessage = "Custom error message";

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThan(10, 5, customMessage));

        assertTrue(error.getMessage().contains(customMessage));
    }

    /**
     * ## Test: Custom Error Message - Supplier Overload
     *
     * Verifies that the `assertLessThan(T, T, Supplier<String>)` overload properly
     * uses the message supplier when the assertion fails. This tests the third
     * method variant that accepts a message supplier for lazy evaluation.
     *
     * **Test Scenario:**
     * - Failing assertion with message supplier
     * - Expected result: AssertionFailedError containing the supplied message
     */
    @Test
    @DisplayName("assertLessThan with message supplier displays correct error")
    void testAssertLessThanWithMessageSupplier() {
        String suppliedMessage = "Supplied error message";
        Supplier<String> messageSupplier = () -> suppliedMessage;

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThan(10, 5, messageSupplier));

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
    @DisplayName("assertLessThan message supplier is lazily evaluated")
    void testAssertLessThanMessageSupplierLazyEvaluation() {
        final boolean[] supplierCalled = {false};
        Supplier<String> messageSupplier = () -> {
            supplierCalled[0] = true;
            return "Lazy message";
        };

        // Passing assertion - supplier should not be called
        assertDoesNotThrow(() -> assertLessThan(3, 5, messageSupplier));
        assertFalse(supplierCalled[0], "Message supplier should not be called for passing assertion");

        // Failing assertion - supplier should be called
        assertThrows(AssertionFailedError.class,
            () -> assertLessThan(10, 5, messageSupplier));
        assertTrue(supplierCalled[0], "Message supplier should be called for failing assertion");
    }

    /**
     * ## Test: Null Parameter Handling - First Parameter
     *
     * Verifies that `assertLessThan` throws a `NullPointerException` when the
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
    @DisplayName("assertLessThan throws NullPointerException when first parameter is null")
    void testAssertLessThanNullFirstParameter() {
        assertThrows(NullPointerException.class,
            () -> assertLessThan(null, 5));
    }

    /**
     * ## Test: Null Parameter Handling - Second Parameter
     *
     * Verifies that `assertLessThan` throws a `NullPointerException` when the
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
    @DisplayName("assertLessThan throws NullPointerException when second parameter is null")
    void testAssertLessThanNullSecondParameter() {
        assertThrows(NullPointerException.class,
            () -> assertLessThan(5, null));
    }

    /**
     * ## Test: Null Parameter Handling - Both Parameters
     *
     * Verifies that `assertLessThan` throws a `NullPointerException` when both
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
    @DisplayName("assertLessThan throws NullPointerException when both parameters are null")
    void testAssertLessThanBothParametersNull() {
        assertThrows(NullPointerException.class,
            () -> assertLessThan(null, null));
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
     * - Verify message contains "Expected 5 to be less than 10"
     */
    @Test
    @DisplayName("assertLessThan error message follows expected format")
    void testAssertLessThanErrorMessageFormat() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThan(10, 5));

        String message = error.getMessage();
        assertTrue(message.contains("Expected 10 to be less than 5"),
            "Error message should follow format: 'Expected <first> to be less than <second>'");
    }

    /**
     * ## Test: Mixed Comparable Types - Same Interface
     *
     * Verifies that `assertLessThan` works when comparing different implementations
     * of the same `Comparable` interface. This tests type compatibility within
     * the bounds of the generic type parameter.
     *
     * **Test Scenario:**
     * - Compare different Integer objects (auto-boxing)
     * - Ensure the method handles object identity vs value comparison correctly
     */
    @Test
    @DisplayName("assertLessThan works with different instances of same Comparable type")
    void testAssertLessThanDifferentInstancesSameType() {
        Integer first = 3;
        Integer second = 5;

        assertDoesNotThrow(() -> assertLessThan(first, second));

        Integer equal1 = 100;
        Integer equal2 = 100;

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
            () -> assertLessThan(equal1, equal2));
        assertTrue(error.getMessage().contains("Expected 100 to be less than 100"));
    }

    /**
     * ## Test: All Method Overloads - Success Cases
     *
     * Comprehensive test ensuring all three method overloads work correctly
     * for success cases. This verifies that each variant properly delegates
     * to the core comparison logic.
     *
     * **Test Scenarios:**
     * - `assertLessThan(T, T)` - basic overload
     * - `assertLessThan(T, T, String)` - with string message
     * - `assertLessThan(T, T, Supplier<String>)` - with message supplier
     */
    @Test
    @DisplayName("All assertLessThan overloads work for success cases")
    void testAllOverloadsSuccessCases() {
        // Basic overload
        assertDoesNotThrow(() -> assertLessThan(1, 2));

        // String message overload
        assertDoesNotThrow(() -> assertLessThan(1, 2, "Should not fail"));

        // Message supplier overload
        assertDoesNotThrow(() -> assertLessThan(1, 2, () -> "Should not fail"));
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
    @DisplayName("All assertLessThan overloads fail appropriately")
    void testAllOverloadsFailureCases() {
        // Basic overload
        assertThrows(AssertionFailedError.class, () -> assertLessThan(5, 3));

        // String message overload
        assertThrows(AssertionFailedError.class, () -> assertLessThan(5, 3, "Custom message"));

        // Message supplier overload
        assertThrows(AssertionFailedError.class, () -> assertLessThan(5, 3, () -> "Supplier message"));
    }

    /**
     * ## Test: Integration with Main Assertions Class
     *
     * Verifies that the `assertLessThan` methods are properly integrated with the
     * main `Assertions` class and work identically to the direct `AssertLessThan` methods.
     * This ensures users can import from either location with consistent behavior.
     *
     * **Test Scenarios:**
     * - All three overloads accessible via the main Assertions class
     * - Behavior should be identical to direct AssertLessThan usage
     * - Success and failure cases work the same way
     */
    @Test
    @DisplayName("assertLessThan integrates properly with main Assertions class")
    void testAssertionsClassIntegration() {
        // Test success cases via main Assertions class
        assertDoesNotThrow(() -> Assertions.assertLessThan(1, 2));
        assertDoesNotThrow(() -> Assertions.assertLessThan(1, 2, "Should pass"));
        assertDoesNotThrow(() -> Assertions.assertLessThan(1, 2, () -> "Should pass"));

        // Test failure cases via main Assertions class
        assertThrows(AssertionFailedError.class, () -> Assertions.assertLessThan(5, 3));
        assertThrows(AssertionFailedError.class, () -> Assertions.assertLessThan(5, 3, "Should fail"));
        assertThrows(AssertionFailedError.class, () -> Assertions.assertLessThan(5, 3, () -> "Should fail"));

        // Verify error messages are consistent
        AssertionFailedError directError = assertThrows(AssertionFailedError.class,
            () -> assertLessThan(10, 5));
        AssertionFailedError assertionsError = assertThrows(AssertionFailedError.class,
            () -> Assertions.assertLessThan(10, 5));

        assertTrue(directError.getMessage().contains("Expected 10 to be less than 5"));
        assertTrue(assertionsError.getMessage().contains("Expected 10 to be less than 5"));
        assertEquals(directError.getMessage(), assertionsError.getMessage(),
            "Error messages should be identical when called via Assertions class");
    }
}