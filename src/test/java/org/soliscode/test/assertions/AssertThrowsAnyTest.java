package org.soliscode.test.assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/// Test class for `assertThrowsAny` assertion methods.
/// This class provides comprehensive test coverage for the assertThrowsAny functionality,
/// which validates that executables throw ANY exception.
///
/// **Testing Strategy**: The tests verify that:
/// - Executables throwing any exception pass validation
/// - Executables that don't throw exceptions fail validation
/// - Custom error messages and message suppliers work correctly
///
/// @author evanbergstrom
/// @since 1.0.0
class AssertThrowsAnyTest {

    @Test
    @DisplayName("assertThrowsAny passes when executable throws any exception")
    void testAssertThrowsAnyPassesWithException() {
        Assertions.assertThrowsAny(() -> {
            throw new IllegalArgumentException("Test exception");
        });

        Assertions.assertThrowsAny(() -> {
            throw new IOException("Test IO exception");
        });

        Assertions.assertThrowsAny(() -> {
            throw new Exception("Test checked exception");
        });

        Assertions.assertThrowsAny(() -> {
            throw new Throwable("Test throwable");
        });
    }

    @Test
    @DisplayName("assertThrowsAny fails when executable does not throw any exception")
    void testAssertThrowsAnyFailsWhenNoExceptionThrown() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> 
                Assertions.assertThrowsAny(() -> {
                    // Do nothing
                }));
        
        assertTrue(error.getMessage().contains("Expected java.lang.Throwable to be thrown"));
    }

    @Test
    @DisplayName("assertThrowsAny(String) passes when executable throws any exception")
    void testAssertThrowsAnyWithMessagePassesWithException() {
        Assertions.assertThrowsAny(() -> {
            throw new RuntimeException("Test");
        }, "Custom message");
    }

    @Test
    @DisplayName("assertThrowsAny(String) fails with custom message when no exception is thrown")
    void testAssertThrowsAnyWithMessageFailsWhenNoExceptionThrown() {
        String customMessage = "Assertion failed with custom message";
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> 
                Assertions.assertThrowsAny(() -> {
                    // Do nothing
                }, customMessage));
        
        assertTrue(error.getMessage().contains(customMessage));
    }

    @Test
    @DisplayName("assertThrowsAny(Supplier<String>) passes when executable throws any exception")
    void testAssertThrowsAnyWithMessageSupplierPassesWithException() {
        Assertions.assertThrowsAny(() -> {
            throw new RuntimeException("Test");
        }, () -> "Lazy message");
    }

    @Test
    @DisplayName("assertThrowsAny(Supplier<String>) fails with lazy message when no exception is thrown")
    void testAssertThrowsAnyWithMessageSupplierFailsWhenNoExceptionThrown() {
        String customMessage = "Assertion failed with lazy message";
        AtomicInteger supplierCallCount = new AtomicInteger(0);
        
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> 
                Assertions.assertThrowsAny(() -> {
                    // Do nothing
                }, () -> {
                    supplierCallCount.incrementAndGet();
                    return customMessage;
                }));
        
        assertTrue(error.getMessage().contains(customMessage));
        assertEquals(1, supplierCallCount.get(), "Message supplier should be called exactly once");
    }
}
