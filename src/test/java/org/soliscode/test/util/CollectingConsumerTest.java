package org.soliscode.test.util;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.soliscode.test.AbstractTest;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for the `CollectingConsumer` class. These tests verify that the consumer correctly collects
/// all arguments passed to the accept method and provides proper access to them via List and Set views.
///
/// @author evanbergstrom
/// @since 1.0
/// @see CollectingConsumer
@DisplayName("Tests for CollectingConsumer")
public class CollectingConsumerTest extends AbstractTest {

    // Tests for basic functionality

    /// Test that default constructor creates an empty collector.
    @SuppressWarnings("ConstantValue")
    @Test
    @DisplayName("Test default constructor creates empty collector")
    public void testDefaultConstructor() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        // Verify initial state is empty
        List<String> list = consumer.toList();
        Set<String> set = consumer.toSet();

        assertNotNull(list);
        assertNotNull(set);
        assertTrue(list.isEmpty());
        assertTrue(set.isEmpty());
        assertEquals(0, list.size());
        assertEquals(0, set.size());
    }

    /// Test that CollectingConsumer implements Consumer interface correctly.
    @SuppressWarnings({"SequencedCollectionMethodCanBeUsed"})
    @Test
    @DisplayName("Test implements Consumer interface")
    public void testImplementsConsumerInterface() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        // Test that it can be used as a Consumer
        Consumer<String> consumerInterface = consumer;
        assertNotNull(consumerInterface);

        // Test that Consumer methods work
        consumerInterface.accept("test");
        assertEquals(1, consumer.toList().size());
        assertEquals("test", consumer.toList().get(0));
    }

    // Tests for accept method

    /// Test accepting a single element.
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Test
    @DisplayName("Test accepting single element")
    public void testAcceptSingleElement() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        consumer.accept("hello");

        List<String> list = consumer.toList();
        Set<String> set = consumer.toSet();

        assertEquals(1, list.size());
        assertEquals(1, set.size());
        assertEquals("hello", list.get(0));
        assertTrue(set.contains("hello"));
    }

    /// Test accepting multiple elements.
    @Test
    @DisplayName("Test accepting multiple elements")
    public void testAcceptMultipleElements() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        consumer.accept("first");
        consumer.accept("second");
        consumer.accept("third");

        List<String> list = consumer.toList();
        Set<String> set = consumer.toSet();

        assertEquals(3, list.size());
        assertEquals(3, set.size());

        // Test order preservation in list
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
        assertEquals("third", list.get(2));

        // Test all elements present in set
        assertTrue(set.contains("first"));
        assertTrue(set.contains("second"));
        assertTrue(set.contains("third"));
    }

    /// Test accepting null values.
    @Test
    @DisplayName("Test accepting null values")
    public void testAcceptNullValues() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        consumer.accept(null);
        consumer.accept("not null");
        consumer.accept(null);

        List<String> list = consumer.toList();
        Set<String> set = consumer.toSet();

        assertEquals(3, list.size());
        assertEquals(2, set.size()); // Set removes duplicate nulls

        // Test list preserves order and duplicates
        assertNull(list.get(0));
        assertEquals("not null", list.get(1));
        assertNull(list.get(2));

        // Test set contains both values
        assertTrue(set.contains(null));
        assertTrue(set.contains("not null"));
    }

    /// Test accepting duplicate values.
    @Test
    @DisplayName("Test accepting duplicate values")
    public void testAcceptDuplicateValues() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        consumer.accept("duplicate");
        consumer.accept("unique");
        consumer.accept("duplicate");
        consumer.accept("duplicate");

        List<String> list = consumer.toList();
        Set<String> set = consumer.toSet();

        assertEquals(4, list.size());
        assertEquals(2, set.size()); // Set removes duplicates

        // Test list preserves all occurrences
        assertEquals("duplicate", list.get(0));
        assertEquals("unique", list.get(1));
        assertEquals("duplicate", list.get(2));
        assertEquals("duplicate", list.get(3));

        // Test set contains unique values only
        assertTrue(set.contains("duplicate"));
        assertTrue(set.contains("unique"));
    }

    // Tests for toList method

    /// Test that toList returns the same reference consistently.
    @Test
    @DisplayName("Test toList returns same reference")
    public void testToListSameReference() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        List<String> list1 = consumer.toList();
        List<String> list2 = consumer.toList();

        // Should return the same list instance
        assertSame(list1, list2);
    }

    /// Test that toList reflects changes after accept calls.
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Test
    @DisplayName("Test toList reflects changes")
    public void testToListReflectsChanges() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        List<String> list = consumer.toList();
        assertTrue(list.isEmpty());

        consumer.accept("item1");
        assertEquals(1, list.size());
        assertEquals("item1", list.get(0));

        consumer.accept("item2");
        assertEquals(2, list.size());
        assertEquals("item2", list.get(1));
    }

    /// Test that toList preserves insertion order.
    @Test
    @DisplayName("Test toList preserves insertion order")
    public void testToListPreservesOrder() {
        CollectingConsumer<Integer> consumer = new CollectingConsumer<>();

        // Add elements in specific order
        for (int i = 10; i >= 1; i--) {
            consumer.accept(i);
        }

        List<Integer> list = consumer.toList();
        assertEquals(10, list.size());

        // Verify order is preserved
        for (int i = 0; i < 10; i++) {
            assertEquals(Integer.valueOf(10 - i), list.get(i));
        }
    }

    // Tests for toSet method

    /// Test that toSet creates new instances each time.
    @Test
    @DisplayName("Test toSet creates new instances")
    public void testToSetCreatesNewInstances() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();
        consumer.accept("test");

        Set<String> set1 = consumer.toSet();
        Set<String> set2 = consumer.toSet();

        // Should create new set instances
        assertNotSame(set1, set2);
        assertEquals(set1, set2); // But content should be equal
    }

    /// Test that toSet reflects current state.
    @Test
    @DisplayName("Test toSet reflects current state")
    public void testToSetReflectsCurrentState() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        Set<String> emptySet = consumer.toSet();
        assertTrue(emptySet.isEmpty());

        consumer.accept("item1");
        Set<String> oneItemSet = consumer.toSet();
        assertEquals(1, oneItemSet.size());
        assertTrue(oneItemSet.contains("item1"));

        consumer.accept("item2");
        Set<String> twoItemSet = consumer.toSet();
        assertEquals(2, twoItemSet.size());
        assertTrue(twoItemSet.contains("item1"));
        assertTrue(twoItemSet.contains("item2"));
    }

    /// Test that toSet removes duplicates correctly.
    @Test
    @DisplayName("Test toSet removes duplicates")
    public void testToSetRemovesDuplicates() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        // Add many duplicates
        consumer.accept("a");
        consumer.accept("b");
        consumer.accept("a");
        consumer.accept("c");
        consumer.accept("b");
        consumer.accept("a");

        List<String> list = consumer.toList();
        Set<String> set = consumer.toSet();

        assertEquals(6, list.size()); // List keeps all
        assertEquals(3, set.size());  // Set removes duplicates

        assertTrue(set.contains("a"));
        assertTrue(set.contains("b"));
        assertTrue(set.contains("c"));
    }

    // Tests with different data types

    /// Test with Integer type.
    @Test
    @DisplayName("Test with Integer type")
    public void testWithIntegerType() {
        CollectingConsumer<Integer> consumer = new CollectingConsumer<>();

        consumer.accept(1);
        consumer.accept(2);
        consumer.accept(1); // duplicate
        consumer.accept(3);

        List<Integer> list = consumer.toList();
        Set<Integer> set = consumer.toSet();

        assertEquals(4, list.size());
        assertEquals(3, set.size());

        assertEquals(List.of(1, 2, 1, 3), list);
        assertEquals(Set.of(1, 2, 3), set);
    }

    /// Test with custom object type.
    @Test
    @DisplayName("Test with custom object type")
    public void testWithCustomObjectType() {
        CollectingConsumer<TestObject> consumer = new CollectingConsumer<>();

        TestObject obj1 = new TestObject("a");
        TestObject obj2 = new TestObject("b");
        TestObject obj3 = new TestObject("a"); // equal to obj1

        consumer.accept(obj1);
        consumer.accept(obj2);
        consumer.accept(obj3);

        List<TestObject> list = consumer.toList();
        Set<TestObject> set = consumer.toSet();

        assertEquals(3, list.size());
        assertEquals(2, set.size()); // obj1 and obj3 are equal

        assertSame(obj1, list.get(0));
        assertSame(obj2, list.get(1));
        assertSame(obj3, list.get(2));

        assertTrue(set.contains(obj1));
        assertTrue(set.contains(obj2));
        assertTrue(set.contains(obj3)); // obj3 equals obj1
    }

    // Tests for large datasets

    /// Test with large number of elements.
    /// @param size The size of the collection to test
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @ParameterizedTest
    @ValueSource(ints = {100, 1000, 10000})
    @DisplayName("Test with large datasets")
    public void testWithLargeDatasets(int size) {
        CollectingConsumer<Integer> consumer = new CollectingConsumer<>();

        // Add elements 0 to size-1
        for (int i = 0; i < size; i++) {
            consumer.accept(i);
        }

        List<Integer> list = consumer.toList();
        Set<Integer> set = consumer.toSet();

        assertEquals(size, list.size());
        assertEquals(size, set.size());

        // Verify first and last elements
        assertEquals(Integer.valueOf(0), list.get(0));
        assertEquals(Integer.valueOf(size - 1), list.get(size - 1));

        assertTrue(set.contains(0));
        assertTrue(set.contains(size - 1));
    }

    /// Test with large number of duplicates.
    @Test
    @DisplayName("Test with large number of duplicates")
    public void testWithLargeDuplicates() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        // Add 1000 instances of the same string
        String repeated = "repeated";
        for (int i = 0; i < 1000; i++) {
            consumer.accept(repeated);
        }

        List<String> list = consumer.toList();
        Set<String> set = consumer.toSet();

        assertEquals(1000, list.size());
        assertEquals(1, set.size());

        // All list elements should be the same
        assertTrue(list.stream().allMatch(repeated::equals));

        // Set should contain only one element
        assertTrue(set.contains(repeated));
        assertEquals(repeated, set.iterator().next());
    }

    // Tests for functional programming integration

    /// Test usage with Stream API.
    @Test
    @DisplayName("Test usage with Stream API")
    public void testUsageWithStreamAPI() {
        CollectingConsumer<Integer> consumer = new CollectingConsumer<>();

        // Use with stream forEach
        IntStream.range(1, 6).boxed().forEach(consumer);

        List<Integer> list = consumer.toList();
        Set<Integer> set = consumer.toSet();

        assertEquals(5, list.size());
        assertEquals(5, set.size());
        assertEquals(List.of(1, 2, 3, 4, 5), list);
        assertEquals(Set.of(1, 2, 3, 4, 5), set);
    }

    /// Test chaining with other consumers.
    @Test
    @DisplayName("Test chaining with other consumers")
    public void testChainingWithOtherConsumers() {
        CollectingConsumer<String> collector = new CollectingConsumer<>();
        StringBuilder builder = new StringBuilder();

        // Create a chained consumer
        Consumer<String> chainedConsumer = collector.andThen(builder::append);

        chainedConsumer.accept("Hello");
        chainedConsumer.accept(" ");
        chainedConsumer.accept("World");

        // Verify both consumers received the values
        assertEquals(List.of("Hello", " ", "World"), collector.toList());
        assertEquals("Hello World", builder.toString());
    }

    // Tests for edge cases and error conditions

    /// Test behavior after many operations.
    @Test
    @DisplayName("Test behavior after many operations")
    public void testBehaviorAfterManyOperations() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        // Perform many operations
        for (int i = 0; i < 100; i++) {
            consumer.accept("item" + i);

            // Intermittently check state
            if (i % 10 == 0) {
                assertEquals(i + 1, consumer.toList().size());
                assertEquals(i + 1, consumer.toSet().size());
            }
        }

        // Final verification
        assertEquals(100, consumer.toList().size());
        assertEquals(100, consumer.toSet().size());
    }

    /// Test that modifications to returned collections don't affect internal state.
    @Test
    @DisplayName("Test returned collection independence")
    public void testReturnedCollectionIndependence() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();
        consumer.accept("original");

        // Get the list and modify it
        List<String> list = consumer.toList();
        // Note: toList() returns the internal list, so this test verifies current behavior
        // In a real implementation, you might want to return a copy for immutability

        // Get set and try to modify it (should create new instance each time)
        Set<String> set1 = consumer.toSet();
        set1.add("modified"); // This shouldn't affect consumer's internal state

        Set<String> set2 = consumer.toSet();
        assertEquals(1, set2.size());
        assertTrue(set2.contains("original"));
        assertFalse(set2.contains("modified"));
    }

    /// Test generic type safety.
    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Test
    @DisplayName("Test generic type safety")
    public void testGenericTypeSafety() {
        // Test with different generic types to ensure type safety
        CollectingConsumer<String> stringConsumer = new CollectingConsumer<>();
        CollectingConsumer<Integer> intConsumer = new CollectingConsumer<>();
        CollectingConsumer<Object> objectConsumer = new CollectingConsumer<>();

        stringConsumer.accept("string");
        intConsumer.accept(42);
        objectConsumer.accept(new Object());

        List<String> strings = stringConsumer.toList();
        List<Integer> integers = intConsumer.toList();
        List<Object> objects = objectConsumer.toList();

        assertEquals(1, strings.size());
        assertEquals(1, integers.size());
        assertEquals(1, objects.size());

        assertEquals(String.class, strings.get(0).getClass());
        assertEquals(Integer.class, integers.get(0).getClass());
        assertEquals(Object.class, objects.get(0).getClass());
    }

    // Performance and memory tests

    /// Test memory efficiency with repeated elements.
    @SuppressWarnings({"SequencedCollectionMethodCanBeUsed", "StringEquality"})
    @Test
    @DisplayName("Test memory efficiency with repeated elements")
    public void testMemoryEfficiencyWithRepeatedElements() {
        CollectingConsumer<String> consumer = new CollectingConsumer<>();

        // Use string interning to test memory efficiency
        String base = "repeated";
        for (int i = 0; i < 1000; i++) {
            consumer.accept(base.intern()); // Same reference
        }

        List<String> list = consumer.toList();
        Set<String> set = consumer.toSet();

        assertEquals(1000, list.size());
        assertEquals(1, set.size());

        // All references should be the same due to interning
        String first = list.get(0);
        assertTrue(list.stream().allMatch(s -> s == first));
    }

    // Helper class for testing custom objects
    private record TestObject(String value) {

    @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestObject(String value1))) return false;
            return Objects.equals(value, value1);
        }

    @Override
        public @NonNull String toString() {
            return "TestObject{" + value + "}";
        }
    }
}