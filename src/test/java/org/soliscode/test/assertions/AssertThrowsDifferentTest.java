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
import static org.soliscode.test.assertions.Assertions.*;

/// Test class for `AssertThrowsDifferent` assertion methods.
/// This class provides comprehensive test coverage for the AssertThrowsDifferent functionality,
/// which validates that executables throw exceptions that are NOT any of a specified set of prohibited types.
///
/// **Implementation Details**: The AssertThrowsDifferent implementation uses `Class.isInstance()`
/// which performs runtime type checking including inheritance hierarchy verification.
///
/// **Testing Strategy**: The tests verify that:
/// - Executables throwing non-prohibited exception types pass validation
/// - Executables throwing prohibited exception types fail validation
/// - Executables that don't throw exceptions fail validation
/// - Inheritance relationships are properly detected
/// - Custom error messages and message suppliers work correctly
/// - Both single prohibited types and collections of prohibited types work correctly
///
/// @author evanbergstrom
/// @since 1.0.0
class AssertThrowsDifferentTest {

    @Test
    @DisplayName("assertThrowsDifferent with collection passes when executable throws non-prohibited exception")
    void testAssertThrowsDifferentCollectionPassesWithAllowedException() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class
        );

        assertThrowsDifferent(prohibitedTypes, () -> {
            throw new IllegalStateException("Allowed exception");
        });

        assertThrowsDifferent(prohibitedTypes, () -> {
            throw new RuntimeException("Also allowed");
        });

        assertThrowsDifferent(prohibitedTypes, () -> {
            throw new IOException("Different exception type");
        });
    }

    @Test
    @DisplayName("assertThrowsDifferent with single type passes when executable throws non-prohibited exception")
    void testAssertThrowsDifferentSingleTypePassesWithAllowedException() {
        assertThrowsDifferent(IllegalArgumentException.class, () -> {
            throw new IllegalStateException("Different exception");
        });

        assertThrowsDifferent(NullPointerException.class, () -> {
            throw new IllegalArgumentException("Not null pointer");
        });

        assertThrowsDifferent(RuntimeException.class, () -> {
            throw new IOException("Checked exception");
        });
    }

    @Test
    @DisplayName("assertThrowsDifferent fails when executable throws prohibited exception type")
    void testAssertThrowsDifferentFailsWithProhibitedType() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class
        );

        AssertionFailedError error1 = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(prohibitedTypes, () -> {
                throw new IllegalArgumentException("Prohibited exception");
            });
        });
        assertNotNull(error1);

        AssertionFailedError error2 = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(prohibitedTypes, () -> {
                throw new NullPointerException("Also prohibited");
            });
        });
        assertNotNull(error2);
    }

    @Test
    @DisplayName("assertThrowsDifferent with single type fails when executable throws prohibited exception")
    void testAssertThrowsDifferentSingleTypeFailsWithProhibitedType() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(IllegalStateException.class, () -> {
                throw new IllegalStateException("Prohibited exception");
            });
        });
        assertNotNull(error);
    }

    @Test
    @DisplayName("assertThrowsDifferent fails when executable throws subclass of prohibited type")
    void testAssertThrowsDifferentFailsWithInheritance() {
        // RuntimeException is prohibited, so IllegalArgumentException (subclass) should also fail
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(RuntimeException.class, () -> {
                throw new IllegalArgumentException("Subclass of prohibited type");
            });
        });
        assertNotNull(error);

        // Exception is prohibited, so RuntimeException (subclass) should also fail
        AssertionFailedError error2 = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(Exception.class, () -> {
                throw new RuntimeException("Also subclass of prohibited type");
            });
        });
        assertNotNull(error2);
    }

    @Test
    @DisplayName("assertThrowsDifferent passes when thrown exception is not subclass of prohibited type")
    void testAssertThrowsDifferentPassesWithNonSubclass() {
        // IOException is not a subclass of RuntimeException
        assertThrowsDifferent(RuntimeException.class, () -> {
            throw new IOException("Checked exception, not runtime");
        });

        // RuntimeException is not a subclass of IOException
        assertThrowsDifferent(IOException.class, () -> {
            throw new RuntimeException("Runtime exception, not IO");
        });
    }

    @Test
    @DisplayName("assertThrowsDifferent fails when executable does not throw any exception")
    void testAssertThrowsDifferentFailsWhenNoExceptionThrown() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                IllegalArgumentException.class
        );

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(prohibitedTypes, () -> {
                // No exception thrown
            });
        });

        assertNotNull(error);
        assertTrue(error.getMessage().contains("nothing was thrown"));
    }

    @Test
    @DisplayName("assertThrowsDifferent with single type fails when no exception thrown")
    void testAssertThrowsDifferentSingleTypeFailsWhenNoExceptionThrown() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(IllegalArgumentException.class, () -> {
                // No exception thrown
            });
        });

        assertNotNull(error);
        assertTrue(error.getMessage().contains("nothing was thrown"));
    }

    @Test
    @DisplayName("assertThrowsDifferent with custom message passes when allowed exception is thrown")
    void testAssertThrowsDifferentWithMessagePasses() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class
        );

        assertThrowsDifferent(prohibitedTypes, () -> {
            throw new IllegalStateException("Allowed");
        }, "Custom validation message");

        assertThrowsDifferent(IllegalArgumentException.class, () -> {
            throw new IllegalStateException("Different type");
        }, "Single type validation message");
    }

    @Test
    @DisplayName("assertThrowsDifferent with custom message fails and includes custom message")
    void testAssertThrowsDifferentWithMessageFails() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                IllegalArgumentException.class
        );
        String customMessage = "Expected different exception type";

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(prohibitedTypes, () -> {
                throw new IllegalArgumentException("Prohibited");
            }, customMessage);
        });

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith(customMessage));
    }

    @Test
    @DisplayName("assertThrowsDifferent single type with custom message fails and includes custom message")
    void testAssertThrowsDifferentSingleTypeWithMessageFails() {
        String customMessage = "Expected non-validation error";

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(IllegalArgumentException.class, () -> {
                throw new IllegalArgumentException("Prohibited");
            }, customMessage);
        });

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith(customMessage));
    }

    @Test
    @DisplayName("assertThrowsDifferent with null message works correctly")
    void testAssertThrowsDifferentWithNullMessage() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                IllegalArgumentException.class
        );

        assertThrowsDifferent(prohibitedTypes, () -> {
            throw new IllegalStateException("Allowed");
        }, (String) null);

        assertThrowsDifferent(IllegalArgumentException.class, () -> {
            throw new IllegalStateException("Allowed");
        }, (String) null);

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(prohibitedTypes, () -> {
                throw new IllegalArgumentException("Prohibited");
            }, (String) null);
        });
        assertNotNull(error);
    }

    @Test
    @DisplayName("assertThrowsDifferent with message supplier passes when allowed exception is thrown")
    void testAssertThrowsDifferentWithMessageSupplierPasses() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                RuntimeException.class
        );

        assertThrowsDifferent(prohibitedTypes, () -> {
            throw new IOException("IO error");
        }, () -> "Should accept IO exceptions");

        assertThrowsDifferent(IllegalArgumentException.class, () -> {
            throw new IllegalStateException("State error");
        }, () -> "Should accept state exceptions");
    }

    @Test
    @DisplayName("assertThrowsDifferent with message supplier fails and includes supplied message")
    void testAssertThrowsDifferentWithMessageSupplierFails() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                IllegalArgumentException.class
        );
        String suppliedMessage = "Expected non-argument exception";

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(prohibitedTypes, () -> {
                throw new IllegalArgumentException("Prohibited");
            }, () -> suppliedMessage);
        });

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith(suppliedMessage));
    }

    @Test
    @DisplayName("assertThrowsDifferent single type with message supplier fails and includes supplied message")
    void testAssertThrowsDifferentSingleTypeWithMessageSupplierFails() {
        String suppliedMessage = "Expected non-timeout exception";

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(IllegalStateException.class, () -> {
                throw new IllegalStateException("Prohibited");
            }, () -> suppliedMessage);
        });

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith(suppliedMessage));
    }

    @Test
    @DisplayName("assertThrowsDifferent with message supplier only evaluates supplier on failure")
    void testAssertThrowsDifferentMessageSupplierLazyEvaluation() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                IllegalArgumentException.class
        );
        AtomicInteger callCount = new AtomicInteger(0);

        // Passing case - supplier should not be called
        assertThrowsDifferent(prohibitedTypes, () -> {
            throw new IllegalStateException("Allowed type");
        }, () -> {
            callCount.incrementAndGet();
            return "Should not be called";
        });
        assertEquals(0, callCount.get());

        // Failing case - supplier should be called
        assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(prohibitedTypes, () -> {
                throw new IllegalArgumentException("Prohibited type");
            }, () -> {
                callCount.incrementAndGet();
                return "Should be called";
            });
        });
        assertEquals(1, callCount.get());
    }

    @Test
    @DisplayName("assertThrowsDifferent with null message supplier works correctly")
    void testAssertThrowsDifferentWithNullMessageSupplier() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                RuntimeException.class
        );

        assertThrowsDifferent(prohibitedTypes, () -> {
            throw new IOException("Allowed");
        }, (java.util.function.Supplier<String>) null);

        assertThrowsDifferent(IllegalArgumentException.class, () -> {
            throw new IOException("Also allowed");
        }, (java.util.function.Supplier<String>) null);

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(prohibitedTypes, () -> {
                throw new RuntimeException("Prohibited");
            }, (java.util.function.Supplier<String>) null);
        });
        assertNotNull(error);
    }

    @SuppressWarnings("DataFlowIssue") // Explicitly testing passing null parameters
    @Test
    @DisplayName("assertThrowsDifferent handles null prohibited types")
    void testAssertThrowsDifferentWithNullProhibitedTypes() {
        // Null collection will cause NPE when iterating
        assertThrows(NullPointerException.class, () -> {
            assertThrowsDifferent((Collection<Class<? extends Throwable>>) null, () -> {
                throw new RuntimeException();
            });
        });

        // Null single type will cause NPE when creating the List.of()
        assertThrows(NullPointerException.class, () -> {
            assertThrowsDifferent((Class<? extends Throwable>) null, () -> {
                throw new RuntimeException();
            });
        });
    }

    @Test
    @DisplayName("assertThrowsDifferent works correctly with empty prohibited types collection")
    void testAssertThrowsDifferentWithEmptyProhibitedTypes() {
        Collection<Class<? extends Throwable>> emptyTypes = List.of();

        // Any exception should pass when no types are prohibited
        assertThrowsDifferent(emptyTypes, () -> {
            throw new RuntimeException("Any exception should pass");
        });

        assertThrowsDifferent(emptyTypes, () -> {
            throw new IOException("IO exception should pass");
        });

        // No exception should still fail
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(emptyTypes, () -> {
                // No exception
            });
        });
        assertNotNull(error);
    }

    @Test
    @DisplayName("assertThrowsDifferent works correctly with Error subclasses")
    void testAssertThrowsDifferentWithErrors() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                RuntimeException.class
        );

        // Errors should be allowed when RuntimeException is prohibited
        assertThrowsDifferent(prohibitedTypes, () -> {
            throw new AssertionError("Assertion errors are not RuntimeExceptions");
        });

        assertThrowsDifferent(prohibitedTypes, () -> {
            throw new OutOfMemoryError("OOM errors are not RuntimeExceptions");
        });

        // But Error itself being prohibited should fail
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(Error.class, () -> {
                throw new AssertionError("This is an Error");
            });
        });
        assertNotNull(error);
    }

    @Test
    @DisplayName("assertThrowsDifferent assertion error contains expected information")
    void testAssertThrowsDifferentErrorDetails() {
        Collection<Class<? extends Throwable>> prohibitedTypes = List.of(
                IllegalArgumentException.class,
                NullPointerException.class
        );

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertThrowsDifferent(prohibitedTypes, () -> {
                throw new IllegalArgumentException("Prohibited type");
            }, "Custom error message");
        });

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith("Custom error message"));
        assertNotNull(error.getExpected());
        assertNotNull(error.getActual());
        assertEquals(prohibitedTypes, error.getExpected().getValue());
        assertEquals(IllegalArgumentException.class, error.getActual().getValue());
    }
}