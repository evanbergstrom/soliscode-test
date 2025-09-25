package org.soliscode.test.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.soliscode.test.AbstractTest;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for the `AlwaysThrows` class. These tests verify that the class correctly throws exceptions
/// for all method calls as designed for testing purposes.
///
/// @author evanbergstrom
/// @since 1.0
/// @see AlwaysThrows
@DisplayName("Tests for AlwaysThrows")
public class AlwaysThrowsTest extends AbstractTest {

    // Tests for default constructor behavior

    /// Test that the default constructor creates an instance that throws UnsupportedOperationException.
    @Test
    @DisplayName("Test default constructor throws UnsupportedOperationException")
    public void testDefaultConstructor() {
        AlwaysThrows alwaysThrows = new AlwaysThrows();

        // Test that the instance was created successfully
        assertNotNull(alwaysThrows);

        // Test that all methods throw UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.equals(new Object()));

        assertThrows(UnsupportedOperationException.class, alwaysThrows::hashCode);

        assertThrows(UnsupportedOperationException.class, alwaysThrows::toString);

        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.compareTo(new AlwaysThrows()));
    }

    // Tests for custom exception supplier constructor

    /// Test that custom exception supplier constructor works correctly.
    @Test
    @DisplayName("Test custom exception supplier constructor")
    public void testCustomExceptionSupplierConstructor() {
        Supplier<RuntimeException> customSupplier = () -> new IllegalArgumentException("Custom exception");
        AlwaysThrows alwaysThrows = new AlwaysThrows(customSupplier);

        // Test that the instance was created successfully
        assertNotNull(alwaysThrows);

        // Test that all methods throw the custom exception
        IllegalArgumentException equalsException = assertThrows(IllegalArgumentException.class,
                () -> alwaysThrows.equals(new Object()));
        assertEquals("Custom exception", equalsException.getMessage());

        IllegalArgumentException hashCodeException = assertThrows(IllegalArgumentException.class,
                alwaysThrows::hashCode);
        assertEquals("Custom exception", hashCodeException.getMessage());

        IllegalArgumentException toStringException = assertThrows(IllegalArgumentException.class,
                alwaysThrows::toString);
        assertEquals("Custom exception", toStringException.getMessage());

        IllegalArgumentException compareToException = assertThrows(IllegalArgumentException.class,
                () -> alwaysThrows.compareTo(new AlwaysThrows()));
        assertEquals("Custom exception", compareToException.getMessage());
    }

    // Tests for equals method

    /// Test that equals method throws exceptions with various inputs.
    @SuppressWarnings({"EqualsWithItself", "EqualsBetweenInconvertibleTypes", "UnnecessaryBoxing"})
    @Test
    @DisplayName("Test equals method throws exceptions with various inputs")
    public void testEqualsMethodThrows() {
        AlwaysThrows alwaysThrows = new AlwaysThrows();

        // Test with null
        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.equals(null));

        // Test with same instance
        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.equals(alwaysThrows));

        // Test with different AlwaysThrows instance
        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.equals(new AlwaysThrows()));

        // Test with different object types
        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.equals("string"));

        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.equals(Integer.valueOf(42)));

        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.equals(new Object()));
    }

    /// Test that equals method throws custom exceptions when using custom supplier.
    @Test
    @DisplayName("Test equals method throws custom exceptions")
    public void testEqualsMethodThrowsCustomException() {
        Supplier<RuntimeException> customSupplier = () -> new RuntimeException("Custom equals exception");
        AlwaysThrows alwaysThrows = new AlwaysThrows(customSupplier);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> alwaysThrows.equals(new Object()));
        assertEquals("Custom equals exception", exception.getMessage());
    }

    // Tests for hashCode method

    /// Test that hashCode method always throws exceptions.
    @Test
    @DisplayName("Test hashCode method throws exceptions")
    public void testHashCodeMethodThrows() {
        AlwaysThrows alwaysThrows = new AlwaysThrows();

        // Test multiple calls to hashCode
        assertThrows(UnsupportedOperationException.class, alwaysThrows::hashCode);

        assertThrows(UnsupportedOperationException.class, alwaysThrows::hashCode);

        assertThrows(UnsupportedOperationException.class, alwaysThrows::hashCode);
    }

    /// Test that hashCode method throws custom exceptions when using custom supplier.
    @Test
    @DisplayName("Test hashCode method throws custom exceptions")
    public void testHashCodeMethodThrowsCustomException() {
        Supplier<RuntimeException> customSupplier = () -> new IllegalStateException("Custom hashCode exception");
        AlwaysThrows alwaysThrows = new AlwaysThrows(customSupplier);

        IllegalStateException exception = assertThrows(IllegalStateException.class, alwaysThrows::hashCode);
        assertEquals("Custom hashCode exception", exception.getMessage());
    }

    // Tests for toString method

    /// Test that toString method always throws exceptions.
    @Test
    @DisplayName("Test toString method throws exceptions")
    public void testToStringMethodThrows() {
        AlwaysThrows alwaysThrows = new AlwaysThrows();

        // Test multiple calls to toString
        assertThrows(UnsupportedOperationException.class, alwaysThrows::toString);

        assertThrows(UnsupportedOperationException.class, alwaysThrows::toString);
    }

    /// Test that toString method throws custom exceptions when using custom supplier.
    @Test
    @DisplayName("Test toString method throws custom exceptions")
    public void testToStringMethodThrowsCustomException() {
        Supplier<RuntimeException> customSupplier = () -> new RuntimeException("Custom toString exception");
        AlwaysThrows alwaysThrows = new AlwaysThrows(customSupplier);

        RuntimeException exception = assertThrows(RuntimeException.class, alwaysThrows::toString);
        assertEquals("Custom toString exception", exception.getMessage());
    }

    // Tests for compareTo method

    /// Test that compareTo method throws exceptions with various inputs.
    @SuppressWarnings("EqualsWithItself")
    @Test
    @DisplayName("Test compareTo method throws exceptions with various inputs")
    public void testCompareToMethodThrows() {
        AlwaysThrows alwaysThrows = new AlwaysThrows();

        // Test with same instance
        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.compareTo(alwaysThrows));

        // Test with different AlwaysThrows instances
        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.compareTo(new AlwaysThrows()));

        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.compareTo(new AlwaysThrows(
                () -> new RuntimeException("Other exception"))));
    }

    /// Test that compareTo method throws custom exceptions when using custom supplier.
    @Test
    @DisplayName("Test compareTo method throws custom exceptions")
    public void testCompareToMethodThrowsCustomException() {
        Supplier<RuntimeException> customSupplier = () -> new IllegalArgumentException("Custom compareTo exception");
        AlwaysThrows alwaysThrows = new AlwaysThrows(customSupplier);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> alwaysThrows.compareTo(new AlwaysThrows()));
        assertEquals("Custom compareTo exception", exception.getMessage());
    }

    /// Test that compareTo method with null throws custom exception (not NullPointerException).
    @SuppressWarnings("DataFlowIssue") // explicitly testing passing a null parameter
    @Test
    @DisplayName("Test compareTo method with null parameter")
    public void testCompareToWithNull() {
        AlwaysThrows alwaysThrows = new AlwaysThrows();

        // Note: compareTo should throw the custom exception, not a NullPointerException
        // because the method throws before parameter validation
        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.compareTo(null));
    }

    // Tests for multiple exception types

    /// Test various exception types that can be supplied.
    @Test
    @DisplayName("Test various exception types")
    public void testVariousExceptionTypes() {
        // Test with IllegalStateException
        AlwaysThrows illegalState = new AlwaysThrows(() -> new IllegalStateException("Illegal state"));
        assertThrows(IllegalStateException.class, () -> illegalState.equals(null));

        // Test with NullPointerException
        AlwaysThrows nullPointer = new AlwaysThrows(() -> new NullPointerException("Null pointer"));
        assertThrows(NullPointerException.class, nullPointer::hashCode);

        // Test with RuntimeException
        AlwaysThrows runtime = new AlwaysThrows(() -> new RuntimeException("Runtime"));
        assertThrows(RuntimeException.class, runtime::toString);

        // Test with custom RuntimeException subclass
        AlwaysThrows custom = new AlwaysThrows(() -> new SecurityException("Security"));
        assertThrows(SecurityException.class, () -> custom.compareTo(new AlwaysThrows()));
    }

    // Tests for exception supplier behavior

    /// Test that exception supplier is called each time a method is invoked.
    @Test
    @DisplayName("Test exception supplier called each time")
    public void testExceptionSupplierCalledEachTime() {
        // Use a supplier that creates exceptions with different messages each time
        final int[] counter = {0};
        Supplier<RuntimeException> countingSupplier = () -> {
            counter[0]++;
            return new RuntimeException("Exception #" + counter[0]);
        };

        AlwaysThrows alwaysThrows = new AlwaysThrows(countingSupplier);

        // Call equals
        RuntimeException exception1 = assertThrows(RuntimeException.class, () -> alwaysThrows.equals(null));
        assertEquals("Exception #1", exception1.getMessage());

        // Call hashCode
        RuntimeException exception2 = assertThrows(RuntimeException.class, alwaysThrows::hashCode);
        assertEquals("Exception #2", exception2.getMessage());

        // Call toString
        RuntimeException exception3 = assertThrows(RuntimeException.class, alwaysThrows::toString);
        assertEquals("Exception #3", exception3.getMessage());

        // Call compareTo
        RuntimeException exception4 = assertThrows(RuntimeException.class,
                () -> alwaysThrows.compareTo(new AlwaysThrows()));
        assertEquals("Exception #4", exception4.getMessage());

        // Verify counter was incremented for each call
        assertEquals(4, counter[0]);
    }

    /// Test that supplier can return null (which will cause NullPointerException when thrown).
    @Test
    @DisplayName("Test supplier returning null")
    public void testSupplierReturningNull() {
        Supplier<RuntimeException> nullSupplier = () -> null;
        AlwaysThrows alwaysThrows = new AlwaysThrows(nullSupplier);

        // When the supplier returns null, trying to throw null should cause a NullPointerException
        assertThrows(NullPointerException.class, () -> alwaysThrows.equals(null));

        assertThrows(NullPointerException.class, alwaysThrows::hashCode);

        assertThrows(NullPointerException.class, alwaysThrows::toString);

        assertThrows(NullPointerException.class, () -> alwaysThrows.compareTo(new AlwaysThrows()));
    }

    // Tests for interaction with Java collections and frameworks

    /// Test behavior when used in collections that call equals/hashCode.
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    @DisplayName("Test behavior in collections")
    public void testBehaviorInCollections() {
        AlwaysThrows alwaysThrows = new AlwaysThrows();

        // Test that HashSet operations fail due to hashCode() throwing
        assertThrows(UnsupportedOperationException.class, () -> {
            java.util.Set<AlwaysThrows> set = new java.util.HashSet<>();
            set.add(alwaysThrows); // This should trigger hashCode()
        });

        // Test that contains operations fail due to equals() throwing
        java.util.List<AlwaysThrows> list = new java.util.ArrayList<>();
        list.add(alwaysThrows); // ArrayList.add() doesn't call equals/hashCode

        assertThrows(UnsupportedOperationException.class, () -> {
            list.contains(alwaysThrows); // This should trigger equals()
        });
    }

    /// Test behavior when used with sorting that calls compareTo.
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    @DisplayName("Test behavior with sorting")
    public void testBehaviorWithSorting() {
        AlwaysThrows alwaysThrows1 = new AlwaysThrows();
        AlwaysThrows alwaysThrows2 = new AlwaysThrows();

        java.util.List<AlwaysThrows> list = new java.util.ArrayList<>();
        list.add(alwaysThrows1);
        list.add(alwaysThrows2);

        // Test that sorting fails due to compareTo() throwing
        assertThrows(UnsupportedOperationException.class, () -> java.util.Collections.sort(list));
    }

    // Tests for edge cases and error conditions

    /// Test constructor with null supplier.
    @Test
    @DisplayName("Test constructor with null supplier")
    public void testConstructorWithNullSupplier() {
        // This should cause a NullPointerException when the constructor tries to store the null supplier
        // or when methods are called and try to invoke supplier.get()
        assertThrows(NullPointerException.class, () -> {
            AlwaysThrows alwaysThrows = new AlwaysThrows(null);
            alwaysThrows.equals(null); // This will cause NPE when trying to call null.get()
        });
    }

    /// Test that exception messages are preserved correctly.
    /// @param message the exception message
    @ParameterizedTest
    @ValueSource(strings = {"", "Simple message", "Message with special chars: !@#$%^&*()",
            "Very long message that contains multiple words and punctuation marks to test message preservation."})
    @DisplayName("Test exception message preservation")
    public void testExceptionMessagePreservation(String message) {
        Supplier<RuntimeException> messageSupplier = () -> new RuntimeException(message);
        AlwaysThrows alwaysThrows = new AlwaysThrows(messageSupplier);

        // Test that message is preserved in equals
        RuntimeException exception = assertThrows(RuntimeException.class, () -> alwaysThrows.equals(null));
        assertEquals(message, exception.getMessage());
    }

    /// Test that all method overrides are consistent in throwing behavior.
    @Test
    @DisplayName("Test consistency across all overridden methods")
    public void testConsistencyAcrossAllMethods() {
        String expectedMessage = "Consistent exception";
        Supplier<RuntimeException> consistentSupplier = () -> new RuntimeException(expectedMessage);
        AlwaysThrows alwaysThrows = new AlwaysThrows(consistentSupplier);

        // All methods should throw the same type of exception with the same message
        RuntimeException equalsException = assertThrows(RuntimeException.class, () -> alwaysThrows.equals(new Object()));
        assertEquals(expectedMessage, equalsException.getMessage());

        RuntimeException hashCodeException = assertThrows(RuntimeException.class, alwaysThrows::hashCode);
        assertEquals(expectedMessage, hashCodeException.getMessage());

        RuntimeException toStringException = assertThrows(RuntimeException.class, alwaysThrows::toString);
        assertEquals(expectedMessage, toStringException.getMessage());

        RuntimeException compareToException = assertThrows(RuntimeException.class,
                () -> alwaysThrows.compareTo(new AlwaysThrows()));
        assertEquals(expectedMessage, compareToException.getMessage());
    }

    // Tests for practical usage scenarios

    /// Test usage scenario for testing exception handling in code that uses Object methods.
    @Test
    @DisplayName("Test practical usage for testing exception handling")
    public void testPracticalUsageForTesting() {
        AlwaysThrows problematicObject = new AlwaysThrows(() -> new RuntimeException("Simulated failure"));

        // Test that code can catch exceptions from equals
        try {
            boolean result = problematicObject.equals(new Object());
            fail("Should have thrown an exception");
        } catch (RuntimeException e) {
            assertEquals("Simulated failure", e.getMessage());
        }

        // Test that code can catch exceptions from toString
        try {
            String result = problematicObject.toString();
            fail("Should have thrown an exception");
        } catch (RuntimeException e) {
            assertEquals("Simulated failure", e.getMessage());
        }
    }

    /// Test that AlwaysThrows can be used to test defensive programming.
    @Test
    @DisplayName("Test defensive programming scenarios")
    public void testDefensiveProgrammingScenarios() {
        AlwaysThrows problematicObject = new AlwaysThrows();

        // Example of defensive code that should handle exceptions gracefully
        String safeToString = getSafeToString(problematicObject);
        assertEquals("toString() failed", safeToString);

        boolean safeEquals = getSafeEquals(problematicObject, new Object());
        assertFalse(safeEquals);
    }

    // Helper methods for defensive programming test
    private String getSafeToString(Object obj) {
        try {
            return obj.toString();
        } catch (Exception e) {
            return "toString() failed";
        }
    }

    private boolean getSafeEquals(Object obj1, Object obj2) {
        try {
            return obj1.equals(obj2);
        } catch (Exception e) {
            return false;
        }
    }
}