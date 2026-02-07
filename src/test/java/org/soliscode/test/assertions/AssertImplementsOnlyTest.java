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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.Assertions.assertImplementsOnly;

/// Test class for `AssertImplementsOnly` assertion methods through the `Assertions` class.
/// This class provides comprehensive test coverage for the AssertImplementsOnly functionality,
/// which validates that objects implement only specified interfaces from their complete hierarchy.
///
/// **Implementation Details**: The AssertImplementsOnly implementation uses `ReflectionTestUtils.getAllInterfaces()`
/// which returns ALL interfaces in the complete class hierarchy, including both directly implemented interfaces
/// and inherited interfaces from parent classes and super-interfaces.
///
/// **Testing Strategy**: The tests verify that:
/// - Objects implementing only expected interfaces pass validation
/// - Objects implementing additional unexpected interfaces fail validation
/// - Interface hierarchy expansion works correctly for both expected and actual interfaces
/// - Custom error messages and message suppliers work as expected
/// - Edge cases with empty interfaces, null parameters, and complex hierarchies are handled properly
///
/// **Key Test Categories**:
/// - **Basic Interface Validation**: Single interfaces, collections of interfaces
/// - **Interface Hierarchy Testing**: Extended interfaces, complex inheritance chains
/// - **Real-World Collections**: ArrayList, LinkedList, HashSet with their complete interface sets
/// - **Error Scenarios**: Invalid parameters, interface mismatches, custom error messaging
/// - **Edge Cases**: Empty interfaces, primitive types, legacy method compatibility
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see AssertImplementsOnly
/// @see Assertions
/// @see org.soliscode.test.util.ReflectionTestUtils#getAllInterfaces(Class)
/// @see org.opentest4j.AssertionFailedError
@DisplayName("Tests for AssertImplementsOnly through Assertions class")
public class AssertImplementsOnlyTest {

    /// Test message used for custom error message validation.
    private static final String TEST_MESSAGE = "Test message for interface compliance";

    // Test classes and interfaces used throughout the tests

    /// Simple test class that implements no interfaces (except implicit Object inheritance)
    private static class TestClassWithNoInterfaces {
        // Empty class that implements no interfaces
    }

    /// Test class implementing a single standard interface
    private static class TestClassWithOneInterface implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    /// Test class implementing multiple standard interfaces
    private static class TestClassWithMultipleInterfaces implements Serializable, Cloneable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    /// Custom test interface for validation scenarios
    private interface TestInterface {
        // Test interface for validation
    }

    /// Interface that extends another interface to test hierarchy handling
    private interface ExtendedTestInterface extends TestInterface {
        // Interface that extends another interface
    }

    /// Test class implementing custom test interface
    private static class TestClassWithCustomInterface implements TestInterface {
        // Class implementing custom test interface
    }

    /// Test class implementing interface that extends another interface
    private static class TestClassWithExtendedInterface implements ExtendedTestInterface {
        // Class implementing interface that extends another interface
        // This includes BOTH ExtendedTestInterface AND TestInterface in hierarchy
    }

    /// Test class implementing both custom and standard interfaces
    private static class TestClassWithMixedInterfaces implements TestInterface, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    // Basic interface validation tests

    /// Tests `assertImplementsOnly` with single expected interface using basic method.
    /// Verifies that objects implementing only the expected interface pass validation,
    /// and objects implementing different interfaces fail validation.
    ///
    /// This test demonstrates the corrected behavior where the implementation properly
    /// handles single interface specifications and their complete hierarchies.
    @DisplayName("Test assertImplementsOnly with single expected interface")
    @Test
    void testAssertImplementsOnlyWithSingleExpectedInterface() {
        TestClassWithOneInterface obj = new TestClassWithOneInterface();

        // Should pass - object implements Serializable, expected interface is Serializable
        // Both expand to the same set: {Serializable}
        assertDoesNotThrow(() -> assertImplementsOnly(Serializable.class, obj));

        // Should fail - object implements Serializable but we expect TestInterface
        // Object: {Serializable}, Expected: {TestInterface} - no match
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(TestInterface.class, obj));
    }

    /// Tests `assertImplementsOnly` with single expected interface and custom message.
    /// Verifies that custom error messages appear correctly in assertion failures
    /// and that successful assertions work with custom messages without issues.
    @DisplayName("Test assertImplementsOnly with single expected interface and custom message")
    @Test
    void testAssertImplementsOnlyWithSingleExpectedInterfaceAndMessage() {
        TestClassWithOneInterface obj = new TestClassWithOneInterface();

        // Should pass with custom message - implementation handles message parameter correctly
        assertDoesNotThrow(() -> assertImplementsOnly(Serializable.class, obj, TEST_MESSAGE));

        // Should fail with custom message appearing in the error
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(TestInterface.class, obj, TEST_MESSAGE));
        assertTrue(error.getMessage().contains(TEST_MESSAGE));
    }

    /// Tests `assertImplementsOnly` with message supplier for lazy evaluation.
    /// Verifies that message suppliers are called only when assertions fail
    /// and that they provide the expected custom messages in error scenarios.
    @DisplayName("Test assertImplementsOnly with message supplier")
    @Test
    void testAssertImplementsOnlyWithMessageSupplier() {
        TestClassWithOneInterface obj = new TestClassWithOneInterface();

        // Should pass with message supplier - supplier should not be called
        assertDoesNotThrow(() -> assertImplementsOnly(Serializable.class, obj, () -> TEST_MESSAGE));

        // Should fail with message supplier - supplier should be called and message included
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(TestInterface.class, obj, () -> TEST_MESSAGE));
        assertTrue(error.getMessage().contains(TEST_MESSAGE));
    }

    // Collection-based interface validation tests

    /// Tests `assertImplementsOnly` with collection of expected interfaces.
    /// Verifies that objects implementing multiple interfaces are validated correctly
    /// against collections of expected interfaces, including proper hierarchy expansion.
    @DisplayName("Test assertImplementsOnly with collection of expected interfaces")
    @Test
    void testAssertImplementsOnlyWithCollectionOfExpectedInterfaces() {
        TestClassWithMultipleInterfaces obj = new TestClassWithMultipleInterfaces();
        Collection<Class<?>> expectedInterfaces = Set.of(Serializable.class, Cloneable.class);

        // Should pass - object implements exactly the expected interfaces
        // Object: {Serializable, Cloneable}, Expected: {Serializable, Cloneable} - match
        assertDoesNotThrow(() -> assertImplementsOnly(expectedInterfaces, obj));

        // Should fail with partial expectation (subset of implemented interfaces)
        Collection<Class<?>> subsetInterfaces = Set.of(Serializable.class);
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(subsetInterfaces, obj));
    }

    /// Tests `assertImplementsOnly` with collection of expected interfaces and custom message.
    /// Verifies that custom messages work correctly with collection-based validation
    /// and appear properly in assertion failure scenarios.
    @DisplayName("Test assertImplementsOnly with collection and custom message")
    @Test
    void testAssertImplementsOnlyWithCollectionAndMessage() {
        TestClassWithMultipleInterfaces obj = new TestClassWithMultipleInterfaces();
        Collection<Class<?>> expectedInterfaces = Set.of(Serializable.class);

        // Should fail with custom message - object implements more interfaces than expected
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(expectedInterfaces, obj, TEST_MESSAGE));
        assertTrue(error.getMessage().contains(TEST_MESSAGE));
    }

    /// Tests `assertImplementsOnly` with collection of expected interfaces and custom message supplier.
    /// Verifies that custom messages work correctly with collection-based validation
    /// and appear properly in assertion failure scenarios.
    @DisplayName("Test assertImplementsOnly with collection and custom message")
    @Test
    void testAssertImplementsOnlyWithCollectionAndMessageSupplier() {
        TestClassWithMultipleInterfaces obj = new TestClassWithMultipleInterfaces();
        Collection<Class<?>> expectedInterfaces = Set.of(Serializable.class);

        // Should fail with custom message - object implements more interfaces than expected
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertImplementsOnly(expectedInterfaces, obj, () -> TEST_MESSAGE));
        assertTrue(error.getMessage().contains(TEST_MESSAGE));
    }

    // Edge cases and special scenarios

    /// Tests `assertImplementsOnly` with objects that implement no interfaces.
    /// Verifies that classes with no interfaces are handled correctly and that
    /// empty interface expectations work as intended. This is an important edge case
    /// since many classes implement no interfaces beyond the implicit Object inheritance.
    @DisplayName("Test assertImplementsOnly with object implementing no interfaces")
    @Test
    void testAssertImplementsOnlyWithNoInterfaces() {
        TestClassWithNoInterfaces obj = new TestClassWithNoInterfaces();

        // Should pass - object implements no interfaces, expecting empty collection
        assertDoesNotThrow(() -> assertImplementsOnly(Collections.emptyList(), obj));

        // Should pass - object implements no interfaces, which is a subset of {Serializable}
        // The object implements ONLY interfaces from the allowed set (none), so it passes
        assertDoesNotThrow(() -> assertImplementsOnly(Serializable.class, obj));

        // Should pass - object implements no interfaces, which is a subset of {TestInterface}
        assertDoesNotThrow(() -> assertImplementsOnly(TestInterface.class, obj));
    }

    // Custom interface hierarchy tests

    /// Tests `assertImplementsOnly` with custom user-defined interfaces.
    /// Verifies that user-defined interfaces work correctly with the assertion framework
    /// and that both matching and non-matching scenarios are handled properly.
    @DisplayName("Test assertImplementsOnly with custom interfaces")
    @Test
    void testAssertImplementsOnlyWithCustomInterfaces() {
        TestClassWithCustomInterface obj = new TestClassWithCustomInterface();

        // Should pass - object implements exactly TestInterface
        assertDoesNotThrow(() -> assertImplementsOnly(TestInterface.class, obj));

        // Should fail - object implements TestInterface but we expect Serializable
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(Serializable.class, obj));

        // Should fail - object implements TestInterface but we expect different interface collection
        Collection<Class<?>> differentInterfaces = Set.of(Cloneable.class);
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(differentInterfaces, obj));
    }

    /// Tests `assertImplementsOnly` with extended interface hierarchy.
    /// Verifies that interface inheritance is properly handled - when a class implements
    /// an interface that extends another, BOTH interfaces are part of the complete hierarchy
    /// and must be included in the expected set for assertions to pass.
    @DisplayName("Test assertImplementsOnly with extended interface hierarchy")
    @Test
    void testAssertImplementsOnlyWithExtendedInterface() {
        TestClassWithExtendedInterface obj = new TestClassWithExtendedInterface();

        // This class implements ExtendedTestInterface, which extends TestInterface
        // So the complete hierarchy includes BOTH interfaces: {ExtendedTestInterface, TestInterface}

        // Should pass - we specify both interfaces in the hierarchy
        Collection<Class<?>> completeHierarchy = Set.of(ExtendedTestInterface.class, TestInterface.class);
        assertDoesNotThrow(() -> assertImplementsOnly(completeHierarchy, obj));

        // Should pass - ExtendedTestInterface expands to include TestInterface
        assertDoesNotThrow(() -> assertImplementsOnly(ExtendedTestInterface.class, obj));

        // Should fail - we only specify the parent interface, missing the direct implementation
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(TestInterface.class, obj));
    }

    /// Tests `assertImplementsOnly` with mixed standard and custom interfaces.
    /// Verifies that combinations of different interface types work correctly,
    /// including scenarios where objects implement both JDK interfaces and custom interfaces.
    @DisplayName("Test assertImplementsOnly with mixed interfaces")
    @Test
    void testAssertImplementsOnlyWithMixedInterfaces() {
        TestClassWithMixedInterfaces obj = new TestClassWithMixedInterfaces();
        Collection<Class<?>> expectedInterfaces = Set.of(TestInterface.class, Serializable.class);

        // Should pass - object implements exactly the expected interfaces
        assertDoesNotThrow(() -> assertImplementsOnly(expectedInterfaces, obj));

        // Should fail - missing one expected interface
        Collection<Class<?>> incompleteInterfaces = Set.of(TestInterface.class);
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(incompleteInterfaces, obj));

        // Should fail - different interface combination
        Collection<Class<?>> differentInterfaces = Set.of(Serializable.class, Cloneable.class);
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(differentInterfaces, obj));
    }

    // Real-world collection class tests

    /// Tests `assertImplementsOnly` with ArrayList to verify real-world usage.
    /// Verifies that the assertion works correctly with commonly used collection classes
    /// that have complex interface hierarchies including both direct and inherited interfaces.
    ///
    /// ArrayList implements: `List`, `RandomAccess`, `Cloneable`, `Serializable`
    /// Through inheritance it also implements: `Collection`, `SequencedCollection`, `Iterable`
    @DisplayName("Test assertImplementsOnly with ArrayList")
    @Test
    void testAssertImplementsOnlyWithArrayList() {
        ArrayList<String> arrayList = new ArrayList<>();

        // ArrayList implements these interfaces directly: List, RandomAccess, Cloneable, Serializable
        // Through List inheritance: Collection, SequencedCollection, Iterable
        Collection<Class<?>> arrayListInterfaces = Set.of(
            List.class, RandomAccess.class, Cloneable.class, Serializable.class
        );

        // Should pass - getAllInterfaces() expands to complete hierarchy matching ArrayList's interfaces
        assertDoesNotThrow(() -> assertImplementsOnly(arrayListInterfaces, arrayList));

        // Should fail - missing some directly implemented interfaces
        Collection<Class<?>> subset = Set.of(List.class, Serializable.class);
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(subset, arrayList));

        // Should pass - including extra allowed interfaces that ArrayList doesn't implement is fine
        // ArrayList implements only interfaces from this allowed set, even though it doesn't implement Map
        Collection<Class<?>> withExtra = Set.of(
            List.class, RandomAccess.class, Cloneable.class, Serializable.class,
            Map.class // ArrayList doesn't implement Map, but that's allowed
        );
        assertDoesNotThrow(() -> assertImplementsOnly(withExtra, arrayList));
    }

    /// Tests `assertImplementsOnly` with LinkedList to verify different collection implementation.
    /// Verifies that different collection implementations have different interface hierarchies
    /// and that the assertion correctly distinguishes between them.
    ///
    /// LinkedList implements: `List`, `Deque`, `Cloneable`, `Serializable`
    /// Through inheritance: `Collection`, `SequencedCollection`, `Iterable`, `Queue`
    @DisplayName("Test assertImplementsOnly with LinkedList")
    @Test
    void testAssertImplementsOnlyWithLinkedList() {
        LinkedList<String> linkedList = new LinkedList<>();

        // LinkedList directly implements: List, Deque, Cloneable, Serializable
        // Through inheritance: Collection, SequencedCollection, Iterable, Queue
        Collection<Class<?>> linkedListInterfaces = Set.of(
            List.class, Deque.class, Cloneable.class, Serializable.class
        );

        // Should pass - correct interface set for LinkedList
        assertDoesNotThrow(() -> assertImplementsOnly(linkedListInterfaces, linkedList));

        // Should fail - using ArrayList's interfaces (missing Deque, has extra RandomAccess)
        Collection<Class<?>> arrayListInterfaces = Set.of(
            List.class, RandomAccess.class, Cloneable.class, Serializable.class
        );
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(arrayListInterfaces, linkedList));

        // Should fail - missing Deque which LinkedList implements
        Collection<Class<?>> missingDeque = Set.of(
            List.class, Cloneable.class, Serializable.class
        );
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(missingDeque, linkedList));
    }

    /// Tests `assertImplementsOnly` with HashSet to verify Set implementation interfaces.
    /// Verifies that Set implementations have different interface patterns compared to
    /// List implementations and that the assertion handles these differences correctly.
    ///
    /// HashSet implements: `Set`, `Cloneable`, `Serializable`
    /// Through inheritance: `Collection`, `Iterable`
    @DisplayName("Test assertImplementsOnly with HashSet")
    @Test
    void testAssertImplementsOnlyWithHashSet() {
        HashSet<String> hashSet = new HashSet<>();

        // HashSet directly implements: Set, Cloneable, Serializable
        // Through inheritance: Collection, Iterable
        Collection<Class<?>> hashSetInterfaces = Set.of(
            Set.class, Cloneable.class, Serializable.class
        );

        // Should pass - correct interface set for HashSet
        assertDoesNotThrow(() -> assertImplementsOnly(hashSetInterfaces, hashSet));

        // Should fail - missing Set interface (using only inherited interfaces)
        Collection<Class<?>> inheritedOnly = Set.of(Collection.class, Iterable.class);
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(inheritedOnly, hashSet));

        // Should fail - using List-specific interfaces
        Collection<Class<?>> listInterfaces = Set.of(
            List.class, Cloneable.class, Serializable.class
        );
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(listInterfaces, hashSet));
    }

    // Error handling and validation tests

    /// Tests error message content and structure in assertion failures.
    /// Verifies that assertion failures contain meaningful information about
    /// the expected vs actual interface sets and object details.
    @DisplayName("Test error message content")
    @Test
    void testErrorMessageContent() {
        TestClassWithOneInterface obj = new TestClassWithOneInterface();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(TestInterface.class, obj));

        String errorMessage = error.getMessage();
        // Should contain class name information
        assertTrue(errorMessage.contains("TestClassWithOneInterface"));
        // Should contain interface information
        assertTrue(errorMessage.contains("expected") || errorMessage.contains("instance"));
    }

    /// Tests `assertImplementsOnly` with null object parameter.
    /// Verifies that null objects are handled appropriately with proper exception types
    /// across all method overloads.
    @SuppressWarnings("DataFlowIssue") // Explicitly test passing null parameters
    @DisplayName("Test assertImplementsOnly with null object")
    @Test
    void testAssertImplementsOnlyWithNullObject() {
        // Should throw NullPointerException for null object across all method overloads
        assertThrows(NullPointerException.class,
                    () -> assertImplementsOnly(Serializable.class, null));

        assertThrows(NullPointerException.class,
                    () -> assertImplementsOnly(Collections.singletonList(Serializable.class), null));

        assertThrows(NullPointerException.class,
                    () -> assertImplementsOnly(Serializable.class, null, TEST_MESSAGE));

        assertThrows(NullPointerException.class,
                    () -> assertImplementsOnly(Serializable.class, null, () -> TEST_MESSAGE));
    }

    /// Tests `assertImplementsOnly` with empty expected interfaces collection.
    /// Verifies that empty expectations work correctly and only pass for objects
    /// that truly implement no interfaces.
    @DisplayName("Test assertImplementsOnly with empty expected interfaces")
    @Test
    void testAssertImplementsOnlyWithEmptyExpectedInterfaces() {
        TestClassWithNoInterfaces objWithNoInterfaces = new TestClassWithNoInterfaces();
        TestClassWithOneInterface objWithInterface = new TestClassWithOneInterface();

        // Should pass - object has no interfaces, expecting none
        assertDoesNotThrow(() -> assertImplementsOnly(Collections.emptyList(), objWithNoInterfaces));

        // Should fail - object has interfaces but expecting none
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(Collections.emptyList(), objWithInterface));

        // Should fail - empty set expectation with object that has interfaces
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(Collections.emptySet(), objWithInterface));
    }

    /// Tests performance and correctness with large interface sets.
    /// Verifies that the assertion handles complex interface hierarchies efficiently
    /// and maintains accuracy even with extensive interface collections.
    @DisplayName("Test assertImplementsOnly with large interface sets")
    @Test
    void testAssertImplementsOnlyWithLargeInterfaceSets() {
        ArrayList<String> arrayList = new ArrayList<>();

        // Create comprehensive interface set
        Set<Class<?>> completeInterfaceSet = Set.of(
            List.class, RandomAccess.class, Cloneable.class, Serializable.class
        );

        // Should complete efficiently and pass
        assertDoesNotThrow(() -> assertImplementsOnly(completeInterfaceSet, arrayList));

        // Add interfaces that ArrayList doesn't implement - should still pass since ArrayList
        // implements only interfaces from the allowed set
        Set<Class<?>> extendedSet = new HashSet<>(completeInterfaceSet);
        extendedSet.add(Map.class);        // ArrayList doesn't implement Map
        extendedSet.add(Deque.class);      // ArrayList doesn't implement Deque

        assertDoesNotThrow(() -> assertImplementsOnly(extendedSet, arrayList));
    }

    /// Tests `assertImplementsOnly` with primitive wrapper objects.
    /// Verifies behavior with basic Java types and demonstrates handling
    /// of objects that may implement various interfaces including version-specific ones.
    @DisplayName("Test assertImplementsOnly with primitive wrappers")
    @Test
    void testAssertImplementsOnlyWithPrimitiveWrappers() {
        String stringObj = "test";

        // String implements several interfaces, but the exact set may vary by Java version
        // We'll test with a conservative known set
        Collection<Class<?>> basicStringInterfaces = Set.of(
            Serializable.class,
            Comparable.class,
            CharSequence.class
        );

        // This will likely fail because String implements additional interfaces in modern Java
        // (such as Constable, ConstantDesc in Java 12+), but we test the mechanism
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(basicStringInterfaces, stringObj));

        // Test with Integer wrapper
        Integer integerObj = 42;
        Collection<Class<?>> integerInterfaces = Set.of(
            Serializable.class,
            Comparable.class
        );

        // This will also likely fail due to additional interfaces in modern Java
        assertThrows(AssertionFailedError.class,
                    () -> assertImplementsOnly(integerInterfaces, integerObj));
    }
}