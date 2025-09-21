package org.soliscode.test.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.soliscode.test.AbstractTest;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for the `CountingConsumer` class. These tests verify that the consumer correctly counts
/// the number of times the accept method is called, regardless of the parameter values.
///
/// @author evanbergstrom
/// @since 1.0
/// @see CountingConsumer
@DisplayName("Tests for CountingConsumer")
public class CountingConsumerTest extends AbstractTest {

    // Tests for basic functionality

    /// Test that default constructor creates a consumer with zero count.
    @Test
    @DisplayName("Test default constructor creates zero count")
    public void testDefaultConstructor() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        // Verify initial count is zero
        assertEquals(0, consumer.count());
    }

    /// Test that CountingConsumer implements Consumer interface correctly.
    @Test
    @DisplayName("Test implements Consumer interface")
    public void testImplementsConsumerInterface() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        // Test that it can be used as a Consumer
        Consumer<String> consumerInterface = consumer;
        assertNotNull(consumerInterface);
        
        // Test that Consumer methods work
        consumerInterface.accept("test");
        assertEquals(1, consumer.count());
    }

    // Tests for accept method

    /// Test accepting a single element increments count by one.
    @Test
    @DisplayName("Test accepting single element")
    public void testAcceptSingleElement() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        assertEquals(0, consumer.count());
        
        consumer.accept("hello");
        
        assertEquals(1, consumer.count());
    }

    /// Test accepting multiple elements increments count correctly.
    @Test
    @DisplayName("Test accepting multiple elements")
    public void testAcceptMultipleElements() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        assertEquals(0, consumer.count());
        
        consumer.accept("first");
        assertEquals(1, consumer.count());
        
        consumer.accept("second");
        assertEquals(2, consumer.count());
        
        consumer.accept("third");
        assertEquals(3, consumer.count());
    }

    /// Test that parameter values don't affect counting.
    @Test
    @DisplayName("Test parameter values don't affect counting")
    public void testParameterValuesIgnored() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        // Test various parameter values
        consumer.accept("hello");
        assertEquals(1, consumer.count());
        
        consumer.accept("");
        assertEquals(2, consumer.count());
        
        consumer.accept("very long string with special characters !@#$%^&*()");
        assertEquals(3, consumer.count());
        
        consumer.accept("UPPERCASE");
        assertEquals(4, consumer.count());
        
        consumer.accept("123456");
        assertEquals(5, consumer.count());
    }

    /// Test accepting null values.
    @Test
    @DisplayName("Test accepting null values")
    public void testAcceptNullValues() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        assertEquals(0, consumer.count());
        
        consumer.accept(null);
        assertEquals(1, consumer.count());
        
        consumer.accept("not null");
        assertEquals(2, consumer.count());
        
        consumer.accept(null);
        assertEquals(3, consumer.count());
        
        consumer.accept(null);
        assertEquals(4, consumer.count());
    }

    /// Test accepting duplicate values.
    @Test
    @DisplayName("Test accepting duplicate values")
    public void testAcceptDuplicateValues() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        String value = "duplicate";
        
        consumer.accept(value);
        assertEquals(1, consumer.count());
        
        consumer.accept(value);
        assertEquals(2, consumer.count());
        
        consumer.accept(value);
        assertEquals(3, consumer.count());
        
        // Even with the same reference, each call should increment
        String sameReference = value;
        consumer.accept(sameReference);
        assertEquals(4, consumer.count());
    }

    // Tests for count method

    /// Test that count method returns correct values.
    @Test
    @DisplayName("Test count method returns correct values")
    public void testCountMethodReturnsCorrectValues() {
        CountingConsumer<Integer> consumer = new CountingConsumer<>();
        
        // Test initial count
        assertEquals(0, consumer.count());
        
        // Test incremental counts
        for (int i = 1; i <= 10; i++) {
            consumer.accept(i);
            assertEquals(i, consumer.count());
        }
    }

    /// Test that count method doesn't modify state.
    @Test
    @DisplayName("Test count method doesn't modify state")
    public void testCountMethodDoesntModifyState() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        consumer.accept("test");
        
        // Multiple calls to count() should return the same value
        assertEquals(1, consumer.count());
        assertEquals(1, consumer.count());
        assertEquals(1, consumer.count());
        
        consumer.accept("another");
        
        assertEquals(2, consumer.count());
        assertEquals(2, consumer.count());
    }

    // Tests with different data types

    /// Test with Integer type.
    @Test
    @DisplayName("Test with Integer type")
    public void testWithIntegerType() {
        CountingConsumer<Integer> consumer = new CountingConsumer<>();
        
        consumer.accept(1);
        consumer.accept(2);
        consumer.accept(1); // duplicate
        consumer.accept(null);
        consumer.accept(Integer.MAX_VALUE);
        consumer.accept(Integer.MIN_VALUE);
        
        assertEquals(6, consumer.count());
    }

    /// Test with Boolean type.
    @Test
    @DisplayName("Test with Boolean type")
    public void testWithBooleanType() {
        CountingConsumer<Boolean> consumer = new CountingConsumer<>();
        
        consumer.accept(true);
        consumer.accept(false);
        consumer.accept(true);
        consumer.accept(Boolean.TRUE);
        consumer.accept(Boolean.FALSE);
        consumer.accept(null);
        
        assertEquals(6, consumer.count());
    }

    /// Test with custom object type.
    @Test
    @DisplayName("Test with custom object type")
    public void testWithCustomObjectType() {
        CountingConsumer<TestObject> consumer = new CountingConsumer<>();
        
        TestObject obj1 = new TestObject("a");
        TestObject obj2 = new TestObject("b");
        TestObject obj3 = new TestObject("a"); // equal to obj1 but different instance
        
        consumer.accept(obj1);
        assertEquals(1, consumer.count());
        
        consumer.accept(obj2);
        assertEquals(2, consumer.count());
        
        consumer.accept(obj3);
        assertEquals(3, consumer.count());
        
        consumer.accept(null);
        assertEquals(4, consumer.count());
    }

    // Tests for large datasets

    /// Test with large number of elements.
    @ParameterizedTest
    @ValueSource(ints = {100, 1000, 10000})
    @DisplayName("Test with large datasets")
    public void testWithLargeDatasets(int size) {
        CountingConsumer<Integer> consumer = new CountingConsumer<>();
        
        assertEquals(0, consumer.count());
        
        // Add elements 0 to size-1
        for (int i = 0; i < size; i++) {
            consumer.accept(i);
            assertEquals(i + 1, consumer.count());
        }
        
        assertEquals(size, consumer.count());
    }

    /// Test performance with very large number of accepts.
    @Test
    @DisplayName("Test performance with very large dataset")
    public void testPerformanceWithVeryLargeDataset() {
        CountingConsumer<Integer> consumer = new CountingConsumer<>();
        
        final int LARGE_SIZE = 1_000_000;
        
        // Perform many accepts
        for (int i = 0; i < LARGE_SIZE; i++) {
            consumer.accept(i);
        }
        
        assertEquals(LARGE_SIZE, consumer.count());
    }

    // Tests for functional programming integration

    /// Test usage with Stream API.
    @Test
    @DisplayName("Test usage with Stream API")
    public void testUsageWithStreamAPI() {
        CountingConsumer<Integer> consumer = new CountingConsumer<>();
        
        assertEquals(0, consumer.count());
        
        // Use with stream forEach
        IntStream.range(1, 6).boxed().forEach(consumer);
        
        assertEquals(5, consumer.count());
    }

    /// Test usage with Collection forEach.
    @Test
    @DisplayName("Test usage with Collection forEach")
    public void testUsageWithCollectionForEach() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        List<String> items = List.of("apple", "banana", "cherry", "date");
        
        assertEquals(0, consumer.count());
        
        items.forEach(consumer);
        
        assertEquals(4, consumer.count());
    }

    /// Test chaining with other consumers.
    @Test
    @DisplayName("Test chaining with other consumers")
    public void testChainingWithOtherConsumers() {
        CountingConsumer<String> counter = new CountingConsumer<>();
        StringBuilder builder = new StringBuilder();
        
        // Create a chained consumer
        Consumer<String> chainedConsumer = counter.andThen(builder::append);
        
        assertEquals(0, counter.count());
        
        chainedConsumer.accept("Hello");
        chainedConsumer.accept(" ");
        chainedConsumer.accept("World");
        
        // Verify counter received all calls
        assertEquals(3, counter.count());
        // Verify builder also received all values
        assertEquals("Hello World", builder.toString());
    }

    /// Test composing with other consumers using Consumer.andThen.
    @Test
    @DisplayName("Test Consumer.andThen composition")
    public void testConsumerAndThenComposition() {
        CountingConsumer<Integer> counter = new CountingConsumer<>();
        CollectingConsumer<Integer> collector = new CollectingConsumer<>();
        
        // Chain counter then collector
        Consumer<Integer> composed = counter.andThen(collector);
        
        composed.accept(1);
        composed.accept(2);
        composed.accept(3);
        
        assertEquals(3, counter.count());
        assertEquals(List.of(1, 2, 3), collector.toList());
    }

    // Tests for edge cases and error conditions

    /// Test behavior with maximum integer values near overflow.
    @Test
    @DisplayName("Test behavior near integer overflow")
    public void testBehaviorNearIntegerOverflow() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        // This test would be impractical to run to actual overflow,
        // but we can test the principle with a reasonable number
        final int LARGE_COUNT = 100_000;
        
        for (int i = 0; i < LARGE_COUNT; i++) {
            consumer.accept("item");
        }
        
        assertEquals(LARGE_COUNT, consumer.count());
        
        // Add a few more to ensure it still works
        consumer.accept("extra1");
        consumer.accept("extra2");
        
        assertEquals(LARGE_COUNT + 2, consumer.count());
    }

    /// Test thread safety considerations (single-threaded behavior).
    @Test
    @DisplayName("Test single-threaded behavior")
    public void testSingleThreadedBehavior() {
        CountingConsumer<Integer> consumer = new CountingConsumer<>();
        
        // Simulate concurrent-like operations in single thread
        for (int i = 0; i < 1000; i++) {
            consumer.accept(i);
            int currentCount = consumer.count();
            assertEquals(i + 1, currentCount);
        }
        
        assertEquals(1000, consumer.count());
    }

    /// Test that consumer works correctly after many operations.
    @Test
    @DisplayName("Test behavior after many operations")
    public void testBehaviorAfterManyOperations() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        // Perform many operations with count checks
        for (int i = 0; i < 1000; i++) {
            consumer.accept("item" + i);
            
            // Intermittently check count
            if (i % 100 == 0) {
                assertEquals(i + 1, consumer.count());
            }
        }
        
        // Final verification
        assertEquals(1000, consumer.count());
        
        // Additional operations
        consumer.accept(null);
        consumer.accept("");
        consumer.accept("final");
        
        assertEquals(1003, consumer.count());
    }

    /// Test generic type safety.
    @Test
    @DisplayName("Test generic type safety")
    public void testGenericTypeSafety() {
        // Test with different generic types to ensure type safety
        CountingConsumer<String> stringConsumer = new CountingConsumer<>();
        CountingConsumer<Integer> intConsumer = new CountingConsumer<>();
        CountingConsumer<Object> objectConsumer = new CountingConsumer<>();
        
        stringConsumer.accept("string");
        intConsumer.accept(42);
        objectConsumer.accept(new Object());
        
        assertEquals(1, stringConsumer.count());
        assertEquals(1, intConsumer.count());
        assertEquals(1, objectConsumer.count());
        
        // Verify they're independent
        stringConsumer.accept("another");
        assertEquals(2, stringConsumer.count());
        assertEquals(1, intConsumer.count());
        assertEquals(1, objectConsumer.count());
    }

    // Tests for consistency and invariants

    /// Test that count always increases by exactly one per accept.
    @Test
    @DisplayName("Test count increases by exactly one per accept")
    public void testCountIncreasesExactlyOnePerAccept() {
        CountingConsumer<Object> consumer = new CountingConsumer<>();
        
        int previousCount = consumer.count();
        assertEquals(0, previousCount);
        
        for (int i = 0; i < 50; i++) {
            consumer.accept(new Object());
            int newCount = consumer.count();
            assertEquals(previousCount + 1, newCount);
            previousCount = newCount;
        }
        
        assertEquals(50, consumer.count());
    }

    /// Test that count never decreases.
    @Test
    @DisplayName("Test count never decreases")
    public void testCountNeverDecreases() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        int lastCount = consumer.count();
        
        for (int i = 0; i < 100; i++) {
            consumer.accept("test" + i);
            int currentCount = consumer.count();
            assertTrue(currentCount >= lastCount);
            lastCount = currentCount;
        }
    }

    // Tests for practical usage scenarios

    /// Test practical usage for counting stream operations.
    @Test
    @DisplayName("Test practical usage for counting stream operations")
    public void testPracticalUsageForCountingStreamOperations() {
        CountingConsumer<String> consumer = new CountingConsumer<>();
        
        List<String> words = List.of("hello", "world", "test", "java", "stream");
        
        // Count how many words are processed
        words.stream()
             .filter(word -> word.length() > 3)
             .forEach(consumer);
        
        assertEquals(5, consumer.count()); // "hello", "world", "test", "java", "stream"
    }

    /// Test usage for counting filtered operations.
    @Test
    @DisplayName("Test usage for counting filtered operations")
    public void testUsageForCountingFilteredOperations() {
        CountingConsumer<Integer> evenCounter = new CountingConsumer<>();
        CountingConsumer<Integer> oddCounter = new CountingConsumer<>();
        
        IntStream.range(1, 11)
                .boxed()
                .forEach(n -> {
                    if (n % 2 == 0) {
                        evenCounter.accept(n);
                    } else {
                        oddCounter.accept(n);
                    }
                });
        
        assertEquals(5, evenCounter.count()); // 2, 4, 6, 8, 10
        assertEquals(5, oddCounter.count());  // 1, 3, 5, 7, 9
    }

    /// Test usage for debugging and monitoring.
    @Test
    @DisplayName("Test usage for debugging and monitoring")
    public void testUsageForDebuggingAndMonitoring() {
        CountingConsumer<String> monitor = new CountingConsumer<>();
        
        // Simulate processing with monitoring
        List<String> data = List.of("item1", "item2", "item3", "item4", "item5");
        
        data.forEach(item -> {
            // Process item (simulated)
            monitor.accept(item);
            // Could add logging here: "Processed item: " + item
        });
        
        // Verify all items were processed
        assertEquals(data.size(), monitor.count());
        assertEquals(5, monitor.count());
    }

    // Helper class for testing custom objects
    private static class TestObject {
        private final String value;
        
        TestObject(String value) {
            this.value = value;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestObject)) return false;
            TestObject other = (TestObject) obj;
            return value != null ? value.equals(other.value) : other.value == null;
        }
        
        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
        
        @Override
        public String toString() {
            return "TestObject{" + value + "}";
        }
    }
}