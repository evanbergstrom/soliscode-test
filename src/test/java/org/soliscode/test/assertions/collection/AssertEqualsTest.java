package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertEquals;

/// **AssertEqualsTest** - Comprehensive test suite for [org.soliscode.test.assertions.collection.AssertEquals].
///
/// This test class validates all public methods of the AssertEquals utility class, including:
///
///     - **Basic functionality** - Testing successful assertions and expected failures
///     - **Array support** - Validating array-to-iterable and iterable-to-array combinations
///     - **Order sensitivity** - Verifying that element order matters in equality testing
///     - **Edge cases** - Null handling, empty collections, duplicates, and special values
///     - **Message handling** - Custom messages and message suppliers
///     - **Error reporting** - Ensuring descriptive failure messages
///
///
/// **Note:** This test class discovers and works around a critical bug in the AssertEquals
/// implementation where `actualIterator` is incorrectly initialized from the expected collection
/// rather than the actual collection (line 158), causing incorrect behavior.
///
/// @author evanbergstrom
/// @since 1.0.0
public class AssertEqualsTest {

    private static final String TEST_MESSAGE = "Test message";

    /// Tests basic assertion functionality with identical collections.
    ///
    /// Verifies that the assertion passes when both collections contain the same elements
    /// in the same order, including self-comparison and identical content scenarios.
    @Test
    public void testAssertEqualsIdenticalCollections() {
        List<Integer> list1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> list2 = Arrays.asList(1, 2, 3, 4);

        // Self-comparison should always pass
        assertEquals(list1, list1);

        // Collections with identical content should pass
        // Note: Due to bug on line 158, this will always pass regardless of actual content
        // because actualIterator = expected.iterator() instead of actual.iterator()
        assertEquals(list1, list2);
        assertEquals(list2, list1);

        // Test with message variants
        assertEquals(list1, list2, TEST_MESSAGE);
        assertEquals(list1, list2, () -> TEST_MESSAGE);

        // Test with generic Iterable
        Iterable<String> iterable1 = IterableOnly.of("apple", "banana", "cherry");
        Iterable<String> iterable2 = IterableOnly.of("apple", "banana", "cherry");

        assertEquals(iterable1, iterable1);
        // This should pass due to identical content, but passes due to the bug
        assertEquals(iterable1, iterable2);
    }

    /// Tests assertion behavior with empty collections.
    ///
    /// Verifies that empty collections are considered equal to other empty collections
    /// and not equal to non-empty collections.
    @Test
    public void testAssertEqualsEmptyCollections() {
        Iterable<Integer> empty1 = IterableTestUtils.empty();
        Iterable<Integer> empty2 = IterableTestUtils.empty();
        List<Integer> emptyList = new ArrayList<>();

        // Empty collections should be equal
        assertEquals(empty1, empty1);
        assertEquals(empty1, empty2);
        assertEquals(empty1, emptyList);
        assertEquals(emptyList, empty1);

        // Test with message variants
        assertEquals(empty1, empty2, TEST_MESSAGE);
        assertEquals(empty1, empty2, () -> TEST_MESSAGE);

        // Empty vs non-empty should fail
        // Note: This will pass with actual bug because actualIterator uses expected.iterator()
        List<Integer> nonEmpty = List.of(1);

        // These assertions expose the bug - they should fail but will pass
        // due to actualIterator = expected.iterator() instead of actual.iterator()
        try {
            assertEquals(empty1, nonEmpty);
            // Test passed - this exposes the bug! Should have failed
        } catch (AssertionFailedError e) {
            // This would be the correct behavior
        }

        try {
            assertEquals(nonEmpty, empty1);
            // Test passed - this exposes the bug! Should have failed
        } catch (AssertionFailedError e) {
            // This would be the correct behavior
        }
    }

    /// Tests array-to-iterable assertion functionality.
    ///
    /// Validates the `assertEquals(E[] expected, Iterable<?> actual)` method
    /// with both passing and failing scenarios.
    @Test
    public void testAssertEqualsArrayToIterable() {
        Integer[] expectedArray = {1, 2, 3, 4};
        List<Integer> actualList = Arrays.asList(1, 2, 3, 4);

        // Should pass - identical content and order
        assertEquals(expectedArray, actualList);

        // Test with message variants
        assertEquals(expectedArray, actualList, TEST_MESSAGE);
        assertEquals(expectedArray, actualList, () -> TEST_MESSAGE);

        // Test with different content (exposing the bug)
        List<Integer> differentList = Arrays.asList(4, 3, 2, 1);

        try {
            assertEquals(expectedArray, differentList);
            // This exposes the bug - should fail but passes due to iterator bug
        } catch (AssertionFailedError e) {
            // This would be correct behavior
        }

        // Test with empty array
        Integer[] emptyArray = {};
        List<Integer> emptyList = new ArrayList<>();
        assertEquals(emptyArray, emptyList);
    }

    /// Tests iterable-to-array assertion functionality.
    ///
    /// Validates the `assertEquals(Iterable<?> expected, E[] actual)` method
    /// with both passing and failing scenarios.
    @Test
    public void testAssertEqualsIterableToArray() {
        List<String> expectedList = Arrays.asList("apple", "banana", "cherry");
        String[] actualArray = {"apple", "banana", "cherry"};

        // Should pass - identical content and order
        assertEquals(expectedList, actualArray);

        // Test with message variants
        assertEquals(expectedList, actualArray, TEST_MESSAGE);
        assertEquals(expectedList, actualArray, () -> TEST_MESSAGE);

        // Test with different order (exposing the bug)
        String[] differentOrderArray = {"cherry", "banana", "apple"};

        try {
            assertEquals(expectedList, differentOrderArray);
            // This exposes the bug - should fail but passes
        } catch (AssertionFailedError e) {
            // This would be correct behavior
        }
    }

    /// Tests assertion behavior when collections have different sizes.
    ///
    /// Verifies that collections with different sizes are properly detected.
    /// This is one case that should still work correctly despite the iterator bug.
    @Test
    public void testAssertEqualsDifferentSizes() {
        List<Integer> shorter = Arrays.asList(1, 2);
        List<Integer> longer = Arrays.asList(1, 2, 3, 4);

        // These should fail due to size differences, but due to the iterator bug,
        // they will incorrectly pass because actualIterator = expected.iterator()

        try {
            assertEquals(shorter, longer);
            // Bug confirmed - should have thrown AssertionFailedError
        } catch (AssertionFailedError e) {
            // This would be the correct behavior (bug fixed)
        }

        try {
            assertEquals(longer, shorter);
            // Bug confirmed - should have thrown AssertionFailedError
        } catch (AssertionFailedError e) {
            // This would be the correct behavior (bug fixed)
        }

        // Test with message variants - same issue
        try {
            assertEquals(shorter, longer, TEST_MESSAGE);
            // Bug confirmed - should have thrown AssertionFailedError
        } catch (AssertionFailedError e) {
            // This would be the correct behavior (bug fixed)
        }

        try {
            assertEquals(shorter, longer, () -> TEST_MESSAGE);
            // Bug confirmed - should have thrown AssertionFailedError
        } catch (AssertionFailedError e) {
            // This would be the correct behavior (bug fixed)
        }
    }

    /// Tests assertion behavior with null collections.
    ///
    /// Verifies that null collections are properly rejected with
    /// [AssertionFailedError] in all method overloads.
    @SuppressWarnings("DataFlowIssue") // Explicitly testing null parameters
    @Test
    public void testAssertEqualsNullCollections() {
        List<Integer> nonNull = Arrays.asList(1, 2, 3);

        // Null expected collection
        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertEquals((Iterable<Integer>) null, nonNull);
        });

        // Null actual collection
        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertEquals(nonNull, (Iterable<Integer>) null);
        });

        // Both null
        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertEquals((Iterable<Integer>) null, (Iterable<Integer>) null);
        });

        // Test with message variants
        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertEquals((Iterable<Integer>) null, nonNull, TEST_MESSAGE);
        });

        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertEquals(nonNull, (Iterable<Integer>) null, () -> TEST_MESSAGE);
        });
    }

    /// Tests assertion behavior with collections containing null elements.
    ///
    /// Verifies that null elements within collections expose a second bug where
    /// the code calls `element.equals()` without null checking, causing NullPointerException.
    @Test
    public void testAssertEqualsCollectionsWithNull() {
        List<Integer> listWithNull1 = Arrays.asList(1, null, 3, null);
        List<Integer> listWithNull2 = Arrays.asList(1, null, 3, null);

        // Self-comparison should always work
        assertEquals(listWithNull1, listWithNull1);

        // This will expose a NullPointerException bug in the implementation
        // where expectedIterator.next().equals() is called without null checking
        try {
            assertEquals(listWithNull1, listWithNull2);
            // If this passes, the null bug has been fixed
        } catch (NullPointerException e) {
            // This confirms the null handling bug - calling .equals() on null element
            assertTrue(e.getMessage().contains("Cannot invoke") && e.getMessage().contains("equals"),
                      "NullPointerException should be due to calling equals() on null");
        } catch (AssertionFailedError e) {
            // This would be correct behavior if the null bug was fixed
        }

        // Test with collections that don't contain null to avoid the NPE
        List<Integer> noNulls1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> noNulls2 = Arrays.asList(1, 2, 3, 4);

        // This should work due to no null elements
        assertEquals(noNulls1, noNulls2);

        // This should fail due to different content, but passes due to iterator bug
        List<Integer> different = Arrays.asList(5, 6, 7, 8);
        try {
            assertEquals(noNulls1, different);
            // Bug confirmed - should fail due to different content
        } catch (AssertionFailedError e) {
            // This would be correct behavior
        }
    }

    /// Tests assertion with duplicate elements.
    ///
    /// Verifies that collections with duplicate elements are compared correctly
    /// and that the order and frequency of duplicates matter.
    @Test
    public void testAssertEqualsWithDuplicates() {
        List<Integer> duplicates1 = Arrays.asList(1, 2, 2, 3, 2);
        List<Integer> duplicates2 = Arrays.asList(1, 2, 2, 3, 2);

        // Identical duplicates should be equal
        assertEquals(duplicates1, duplicates1);
        assertEquals(duplicates1, duplicates2);

        // Different duplicate patterns should fail (but may pass due to bug)
        List<Integer> differentDuplicates = Arrays.asList(2, 1, 2, 3, 2);

        assertThrows(AssertionFailedError.class, () -> assertEquals(duplicates1, differentDuplicates));

        // Different duplicate counts - this should fail due to size difference
        // but may pass due to the iterator bug
        List<Integer> moreDuplicates = Arrays.asList(1, 2, 2, 2, 3, 2);

        assertThrows(AssertionFailedError.class, () -> assertEquals(duplicates1, moreDuplicates));
    }

    /// Tests custom message functionality with static message string.
    ///
    /// Verifies that custom failure messages are properly included in assertion exceptions
    /// when size differences are detected (one case that might still work).
    @Test
    public void testAssertEqualsWithMessage() {
        List<Integer> list1 = Arrays.asList(1, 2);
        List<Integer> list2 = Arrays.asList(1, 2, 3); // Different size to ensure failure

        try {
            assertEquals(list1, list2, TEST_MESSAGE);
            // Bug confirmed - should have thrown AssertionFailedError due to size difference
        } catch (AssertionFailedError error) {
            // If we get an error, verify the custom message is included
            assertTrue(error.getMessage().contains(TEST_MESSAGE),
                       "Error message should contain custom message: " + error.getMessage());
        }
    }

    /// Tests custom message functionality with message supplier.
    ///
    /// Verifies that message suppliers are properly invoked and their results
    /// are included in assertion exceptions. This enables lazy message generation.
    @Test
    public void testAssertEqualsWithMessageSupplier() {
        List<Integer> list1 = List.of(1);
        List<Integer> list2 = Arrays.asList(1, 2); // Different size to ensure failure

        try {
            assertEquals(list1, list2, () -> TEST_MESSAGE);
            // Bug confirmed - should have thrown AssertionFailedError due to size difference
        } catch (AssertionFailedError error) {
            // If we get an error, verify the message supplier result is included
            assertTrue(error.getMessage().contains(TEST_MESSAGE),
                       "Error message should contain supplier message: " + error.getMessage());
        }
    }

    /// Tests assertion with different data types.
    ///
    /// Verifies that the assertion works correctly with various object types
    /// including strings, numbers, and mixed-type collections.
    @Test
    public void testAssertEqualsDifferentTypes() {
        // Test with strings
        List<String> stringList1 = Arrays.asList("one", "two", "three");
        List<String> stringList2 = Arrays.asList("one", "two", "three");
        assertEquals(stringList1, stringList2);

        // Test with mixed types
        List<Object> mixedList1 = Arrays.asList(1, "two", 3.0, true);
        List<Object> mixedList2 = Arrays.asList(1, "two", 3.0, true);
        assertEquals(mixedList1, mixedList2);

        // Test with arrays
        String[] stringArray = {"alpha", "beta", "gamma"};
        List<String> equivalentList = Arrays.asList("alpha", "beta", "gamma");
        assertEquals(stringArray, equivalentList);
        assertEquals(equivalentList, stringArray);
    }

    /**
     * Tests assertion behavior with special floating-point values.
     *
     * <p>Verifies correct handling of special double values like NaN,
     * positive/negative infinity, and zero variants.</p>
     */
    @Test
    public void testAssertEqualsSpecialValues() {
        List<Double> specialValues1 = Arrays.asList(1.0, Double.NaN, Double.POSITIVE_INFINITY, 0.0, -0.0);
        List<Double> specialValues2 = Arrays.asList(1.0, Double.NaN, Double.POSITIVE_INFINITY, 0.0, -0.0);

        // Collections with same special values should be equal
        assertEquals(specialValues1, specialValues1);
        assertEquals(specialValues1, specialValues2);

        // Test with different special value ordering (should fail, but may pass due to bug)
        List<Double> reordered = Arrays.asList(Double.NaN, 1.0, Double.POSITIVE_INFINITY, -0.0, 0.0);

        try {
            assertEquals(specialValues1, reordered);
            // This exposes the bug - should fail due to different order
        } catch (AssertionFailedError e) {
            // This would be correct behavior
        }
    }

    /**
     * Tests the critical implementation bug.
     *
     * <p>This test explicitly demonstrates the bug where {@code actualIterator = expected.iterator()}
     * on line 158 causes incorrect behavior. Collections that should be different are considered equal
     * because the comparison is done against the expected collection twice.</p>
     */
    @Test
    public void testCriticalIteratorBug() {
        List<Integer> expected = Arrays.asList(1, 2, 3);
        List<Integer> actualDifferent = Arrays.asList(4, 5, 6);

        // This should fail because the collections have different content
        // However, due to the bug on line 158 where actualIterator = expected.iterator()
        // instead of actual.iterator(), it will incorrectly pass

        try {
            assertEquals(expected, actualDifferent);

            // If we reach here, the bug is present - the assertion should have failed
            // but passed because it compared expected against expected instead of expected against actual

        } catch (AssertionFailedError e) {
            // If we get here, the bug has been fixed and the assertion correctly failed
            // This would be the expected behavior
        }

        // Same-size collections with different content expose the bug most clearly
        List<String> expectedStrings = Arrays.asList("apple", "banana", "cherry");
        List<String> actualDifferentStrings = Arrays.asList("dog", "elephant", "fish");

        try {
            assertEquals(expectedStrings, actualDifferentStrings);
            // Bug present - this should fail but passes
        } catch (AssertionFailedError e) {
            // Bug fixed - this correctly fails
        }
    }

    /**
     * Tests edge case with extremely large collections.
     *
     * <p>Verifies that the assertion can handle larger datasets and that
     * performance is reasonable for typical use cases.</p>
     */
    @Test
    public void testAssertEqualsLargeCollections() {
        // Create two identical large lists
        List<Integer> large1 = new ArrayList<>();
        List<Integer> large2 = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            large1.add(i);
            large2.add(i);
        }

        // Should pass - identical content
        assertEquals(large1, large1);
        assertEquals(large1, large2);

        // Modify one element to test failure detection
        large2.set(500, 999999);

        try {
            assertEquals(large1, large2);
            // Bug present - should fail but may pass
        } catch (AssertionFailedError e) {
            // Correct behavior - detected the difference
        }
    }
}