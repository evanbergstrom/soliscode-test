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

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.Assertions.assertNotInstanceOf;

/// Test class for `AssertNotInstanceOf` assertion methods.
/// This class provides comprehensive test coverage for the AssertNotInstanceOf functionality,
/// which validates that objects are NOT instances of specified types.
///
/// **Implementation Details**: The AssertNotInstanceOf implementation uses `Class.isInstance()`
/// which performs runtime type checking including inheritance hierarchy verification.
///
/// **Testing Strategy**: The tests verify that:
/// - Objects of different types pass validation (are not instances of unrelated types)
/// - Objects that ARE instances of specified types fail validation
/// - Null values are handled correctly (null is not an instance of any type)
/// - Inheritance relationships are properly detected
/// - Interface implementation relationships are properly detected
/// - Custom error messages and message suppliers work correctly
///
/// @author evanbergstrom
/// @since 1.0.0
@SuppressWarnings("DataFlowIssue")
class AssertNotInstanceOfTest {

    @Test
    @DisplayName("assertNotInstanceOf passes when object is not instance of specified type")
    void testAssertNotInstanceOfPasses() {
        assertNotInstanceOf(String.class, 42);
        assertNotInstanceOf(Integer.class, "hello");
        assertNotInstanceOf(List.class, new HashSet<>());
        assertNotInstanceOf(ArrayList.class, new LinkedList<>());
        assertNotInstanceOf(Number.class, "text");
    }

    @Test
    @DisplayName("assertNotInstanceOf passes with null object")
    void testAssertNotInstanceOfPassesWithNull() {
        assertNotInstanceOf(String.class, null);
        assertNotInstanceOf(Object.class, null);
        assertNotInstanceOf(List.class, null);
        assertNotInstanceOf(Integer.class, null);
    }

    @Test
    @DisplayName("assertNotInstanceOf fails when object is instance of specified type")
    void testAssertNotInstanceOfFailsForExactType() {
        AssertionFailedError error1 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(String.class, "hello"));
        assertNotNull(error1);

        AssertionFailedError error2 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(Integer.class, 42));
        assertNotNull(error2);

        AssertionFailedError error3 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(ArrayList.class, new ArrayList<>()));
        assertNotNull(error3);
    }

    @Test
    @DisplayName("assertNotInstanceOf fails when object implements specified interface")
    void testAssertNotInstanceOfFailsForInterface() {
        ArrayList<String> list = new ArrayList<>();

        AssertionFailedError error1 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(List.class, list));
        assertNotNull(error1);

        AssertionFailedError error2 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(Collection.class, list));
        assertNotNull(error2);

        AssertionFailedError error3 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(Iterable.class, list));
        assertNotNull(error3);
    }

    @Test
    @DisplayName("assertNotInstanceOf fails when object extends specified superclass")
    void testAssertNotInstanceOfFailsForSuperclass() {
        ArrayList<String> list = new ArrayList<>();

        AssertionFailedError error1 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(AbstractList.class, list));
        assertNotNull(error1);

        AssertionFailedError error2 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(AbstractCollection.class, list));
        assertNotNull(error2);

        AssertionFailedError error3 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(Object.class, list));
        assertNotNull(error3);
    }

    @Test
    @DisplayName("assertNotInstanceOf with custom message passes when object is not instance")
    void testAssertNotInstanceOfWithMessagePasses() {
        assertNotInstanceOf(String.class, 42, "Should not be a String");
        assertNotInstanceOf(List.class, "hello", "Should not be a List");
        assertNotInstanceOf(Number.class, new Object(), "Should not be a Number");
    }

    @Test
    @DisplayName("assertNotInstanceOf with custom message fails and includes custom message")
    void testAssertNotInstanceOfWithMessageFails() {
        String customMessage = "Expected non-string value";
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(String.class, "hello", customMessage));

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith(customMessage));
    }

    @Test
    @DisplayName("assertNotInstanceOf with null message works correctly")
    void testAssertNotInstanceOfWithNullMessage() {
        assertNotInstanceOf(String.class, 42, (String) null);

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(String.class, "hello", (String) null));
        assertNotNull(error);
    }

    @Test
    @DisplayName("assertNotInstanceOf with message supplier passes when object is not instance")
    void testAssertNotInstanceOfWithMessageSupplierPasses() {
        assertNotInstanceOf(String.class, 42, () -> "Should not be a String");
        assertNotInstanceOf(List.class, "hello", () -> "Should not be a List");
        assertNotInstanceOf(Number.class, new Object(), () -> "Should not be a Number");
    }

    @Test
    @DisplayName("assertNotInstanceOf with message supplier fails and includes supplied message")
    void testAssertNotInstanceOfWithMessageSupplierFails() {
        String suppliedMessage = "Object should not be a String type";
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(String.class, "hello", () -> suppliedMessage));

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith(suppliedMessage));
    }

    @Test
    @DisplayName("assertNotInstanceOf with message supplier only evaluates supplier on failure")
    void testAssertNotInstanceOfMessageSupplierLazyEvaluation() {
        AtomicInteger callCount = new AtomicInteger(0);

        // Passing case - supplier should not be called
        assertNotInstanceOf(String.class, 42, () -> {
            callCount.incrementAndGet();
            return "Should not be called";
        });
        assertEquals(0, callCount.get());

        // Failing case - supplier should be called
        assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(String.class, "hello", () -> {
                    callCount.incrementAndGet();
                    return "Should be called";
                }));
        assertEquals(1, callCount.get());
    }

    @Test
    @DisplayName("assertNotInstanceOf with null message supplier works correctly")
    void testAssertNotInstanceOfWithNullMessageSupplier() {
        assertNotInstanceOf(String.class, 42, (java.util.function.Supplier<String>) null);

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(String.class, "hello", (java.util.function.Supplier<String>) null));
        assertNotNull(error);
    }

    @Test
    @DisplayName("assertNotInstanceOf throws NullPointerException for null expected type")
    void testAssertNotInstanceOfWithNullExpectedType() {
        assertThrows(NullPointerException.class,
                () -> assertNotInstanceOf(null, "hello"));

        assertThrows(NullPointerException.class,
                () -> assertNotInstanceOf(null, "hello", "Custom message"));

        assertThrows(NullPointerException.class,
                () -> assertNotInstanceOf(null, "hello", () -> "Supplied message"));
    }

    @Test
    @DisplayName("assertNotInstanceOf works correctly with primitive wrapper types")
    void testAssertNotInstanceOfWithPrimitiveWrappers() {
        // Different primitive wrapper types should not be instances of each other
        assertNotInstanceOf(Integer.class, 3.14);
        assertNotInstanceOf(Double.class, 42);
        assertNotInstanceOf(Boolean.class, "true");
        assertNotInstanceOf(Character.class, 65);

        // But they should fail when they are instances of Number
        AssertionFailedError error1 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(Number.class, 42));
        assertNotNull(error1);

        AssertionFailedError error2 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(Number.class, 3.14));
        assertNotNull(error2);
    }

    @Test
    @DisplayName("assertNotInstanceOf works correctly with collection hierarchy")
    void testAssertNotInstanceOfWithCollectionHierarchy() {
        LinkedList<String> linkedList = new LinkedList<>();
        HashSet<String> hashSet = new HashSet<>();

        // Different collection implementations should not be instances of each other's concrete types
        assertNotInstanceOf(ArrayList.class, linkedList);
        assertNotInstanceOf(LinkedList.class, new ArrayList<>());
        assertNotInstanceOf(HashSet.class, new TreeSet<>());
        assertNotInstanceOf(TreeSet.class, hashSet);

        // But they should fail for common interfaces they implement
        AssertionFailedError error1 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(Collection.class, linkedList));
        assertNotNull(error1);

        AssertionFailedError error2 = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(Set.class, hashSet));
        assertNotNull(error2);
    }

    @Test
    @DisplayName("assertNotInstanceOf assertion error contains expected information")
    void testAssertNotInstanceOfErrorDetails() {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertNotInstanceOf(String.class, "hello", "Custom error message"));

        assertNotNull(error);
        assertTrue(error.getMessage().startsWith("Custom error message"));
        assertNotNull(error.getExpected());
        assertNotNull(error.getActual());
    }
}