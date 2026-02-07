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

package org.soliscode.test.assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.Assertions.assertThrowsAnyOf;

/// Test class for `AssertThrowsAnyOf` assertion methods.
/// This class provides comprehensive test coverage for the AssertThrowsAnyOf functionality,
/// which validates that executables throw ANY of a specified set of exception types.
///
/// **Implementation Details**: The AssertThrowsAnyOf implementation uses `Class.isInstance()`
/// which performs runtime type checking including inheritance hierarchy verification.
///
/// **Testing Strategy**: The tests verify that:
/// - Executables throwing expected exception types pass validation
/// - Executables throwing unexpected exception types fail validation
/// - Executables that don't throw exceptions fail validation
/// - Inheritance relationships are properly detected
/// - Custom error messages and message suppliers work correctly
/// - Multiple exception types are properly handled
///
/// @author evanbergstrom
/// @since 1.0.0
class AssertThrowsAnyOfTest {

    @Test
    @DisplayName("assertThrowsAnyOf passes when executable throws one of expected exception types")
    void testAssertThrowsAnyPassesWithExpectedTypes() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class,
                IllegalStateException.class
        );

        Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalArgumentException("Test exception");
        });

        Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new NullPointerException("Test null pointer");
        });

        Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalStateException("Test illegal state");
        });
    }

    @Test
    @DisplayName("assertThrowsAnyOf passes when thrown exception is subclass of expected type")
    void testAssertThrowsAnyPassesWithInheritance() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                RuntimeException.class,
                Exception.class
        );

        // IllegalArgumentException is a subclass of RuntimeException
        Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalArgumentException("Subclass test");
        });

        // RuntimeException is a subclass of Exception
        Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new RuntimeException("Runtime exception test");
        });

        // IOException is a subclass of Exception
        Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new IOException("IO exception test");
        });
    }

    @Test
    @DisplayName("assertThrowsAnyOf fails when executable throws unexpected exception type")
    void testAssertThrowsAnyFailsWithUnexpectedType() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class
        );

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> Assertions.assertThrowsAnyOf(expectedTypes,
                () -> {
                    throw new IllegalStateException("Wrong exception type");
                }));

        assertNotNull(error);
        assertNotNull(error.getExpected());
        assertNotNull(error.getActual());
    }

    @Test
    @DisplayName("assertThrowsAnyOf fails when executable does not throw any exception")
    void testAssertThrowsAnyFailsWhenNoExceptionThrown() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                IllegalArgumentException.class,
                RuntimeException.class
        );

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            // No exception thrown
        }));

        assertNotNull(error);
        assertTrue(error.getMessage().contains("nothing was thrown"));
    }

    @Test
    @DisplayName("assertThrowsAnyOf with custom message passes when expected exception is thrown")
    void testAssertThrowsAnyWithMessagePasses() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class
        );

        Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalArgumentException("Test");
        }, "Custom validation message");

        Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new NullPointerException("Test null");
        }, "Custom validation message");
    }

    @Test
    @DisplayName("assertThrowsAnyOf with custom message fails and includes custom message")
    void testAssertThrowsAnyWithMessageFails() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class
        );
        String customMessage = "Expected validation exceptions only";

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalStateException("Wrong type");
        }, customMessage));

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith(customMessage));
    }

    @Test
    @DisplayName("assertThrowsAnyOf with null message works correctly")
    void testAssertThrowsAnyWithNullMessage() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                IllegalArgumentException.class
        );

        Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalArgumentException("Test");
        }, (String) null);

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalStateException("Wrong type");
        }, (String) null));
        assertNotNull(error);
    }

    @Test
    @DisplayName("assertThrowsAnyOf with message supplier passes when expected exception is thrown")
    void testAssertThrowsAnyWithMessageSupplierPasses() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                RuntimeException.class,
                IOException.class
        );

        assertThrowsAnyOf(expectedTypes, () -> {
            throw new RuntimeException("Test runtime");
        }, () -> "Should accept runtime exceptions");

        assertThrowsAnyOf(expectedTypes, () -> {
            throw new IOException("Test IO");
        }, () -> "Should accept IO exceptions");
    }

    @Test
    @DisplayName("assertThrowsAnyOf with message supplier fails and includes supplied message")
    void testAssertThrowsAnyWithMessageSupplierFails() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class
        );
        String suppliedMessage = "Expected specific validation exceptions";

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalStateException("Wrong type");
        }, () -> suppliedMessage));

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith(suppliedMessage));
    }

    @Test
    @DisplayName("assertThrowsAnyOf with message supplier only evaluates supplier on failure")
    void testAssertThrowsAnyMessageSupplierLazyEvaluation() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class
        );
        AtomicInteger callCount = new AtomicInteger(0);

        // Passing case - supplier should not be called
        assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalArgumentException("Expected type");
        }, () -> {
            callCount.incrementAndGet();
            return "Should not be called";
        });
        assertEquals(0, callCount.get());

        // Failing case - supplier should be called
        assertThrows(AssertionFailedError.class, () -> assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalStateException("Wrong type");
        }, () -> {
            callCount.incrementAndGet();
            return "Should be called";
        }));
        assertEquals(1, callCount.get());
    }

    @Test
    @DisplayName("assertThrowsAnyOf with null message supplier works correctly")
    void testAssertThrowsAnyWithNullMessageSupplier() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                RuntimeException.class
        );

        assertThrowsAnyOf(expectedTypes, () -> {
            throw new RuntimeException("Test");
        }, (java.util.function.Supplier<String>) null);

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThrowsAnyOf(expectedTypes, () -> {
            throw new IOException("Wrong type");
        }, (java.util.function.Supplier<String>) null));
        assertNotNull(error);
    }

    @SuppressWarnings("DataFlowIssue") // Explicitly testing passing null parameters
    @Test
    @DisplayName("assertThrowsAnyOf handles null expected types collection")
    void testAssertThrowsAnyWithNullExpectedTypes() {
        // The method will throw an NPE when trying to iterate the null collection
        assertThrows(NullPointerException.class, () -> Assertions.assertThrowsAnyOf(null, () -> {
            throw new RuntimeException();
        }));

        assertThrows(NullPointerException.class, () -> Assertions.assertThrowsAnyOf(null, () -> {
            throw new RuntimeException();
        }, "Custom message"));

        assertThrows(NullPointerException.class, () -> assertThrowsAnyOf(null, () -> {
            throw new RuntimeException();
        }, () -> "Supplied message"));
    }

    @Test
    @DisplayName("assertThrowsAnyOf works correctly with single expected type")
    void testAssertThrowsAnyWithSingleType() {
        Collection<Class<? extends Throwable>> singleType = List.of(
                IllegalArgumentException.class
        );

        Assertions.assertThrowsAnyOf(singleType, () -> {
            throw new IllegalArgumentException("Single type test");
        });

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> Assertions.assertThrowsAnyOf(singleType, () -> {
            throw new IllegalStateException("Wrong single type");
        }));
        assertNotNull(error);
    }

    @Test
    @DisplayName("assertThrowsAnyOf works correctly with empty expected types collection")
    void testAssertThrowsAnyWithEmptyExpectedTypes() {
        Collection<Class<? extends Throwable>> emptyTypes = List.of();

        // Any exception should fail when no types are expected
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> Assertions.assertThrowsAnyOf(emptyTypes, () -> {
            throw new RuntimeException("Any exception");
        }));
        assertNotNull(error);

        // No exception should also fail
        AssertionFailedError error2 = assertThrows(AssertionFailedError.class, () -> Assertions.assertThrowsAnyOf(emptyTypes, () -> {
            // No exception
        }));
        assertNotNull(error2);
    }

    @Test
    @DisplayName("assertThrowsAnyOf works correctly with Error subclasses")
    void testAssertThrowsAnyWithErrors() {
        Collection<Class<? extends Throwable>> errorTypes = List.of(
                Error.class,
                AssertionError.class
        );

        Assertions.assertThrowsAnyOf(errorTypes, () -> {
            throw new AssertionError("Test assertion error");
        });

        Assertions.assertThrowsAnyOf(errorTypes, () -> {
            throw new OutOfMemoryError("Test OOM error");
        });

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> Assertions.assertThrowsAnyOf(errorTypes, () -> {
            throw new RuntimeException("Not an error");
        }));
        assertNotNull(error);
    }

    @Test
    @DisplayName("assertThrowsAnyOf assertion error contains expected information")
    void testAssertThrowsAnyErrorDetails() {
        Collection<Class<? extends Throwable>> expectedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class
        );

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> Assertions.assertThrowsAnyOf(expectedTypes, () -> {
            throw new IllegalStateException("Wrong type");
        }, "Custom error message"));

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith("Custom error message"));
        assertNotNull(error.getExpected());
        assertNotNull(error.getActual());
        assertEquals(expectedTypes, error.getExpected().getValue());
        assertEquals(IllegalStateException.class, error.getActual().getValue());
    }
}