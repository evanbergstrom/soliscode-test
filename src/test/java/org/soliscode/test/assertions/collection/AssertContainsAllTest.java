package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.soliscode.test.assertions.collection.CollectionAssertions.*;

/// **AssertContainsAllTest** - Comprehensive test suite for [AssertContainsAll][org.soliscode.test.assertions.collection.AssertContainsAll].
///
/// This test class validates all public methods of the AssertContainsAll utility class, including:
///
///     - **Basic functionality** - Testing successful assertions and expected failures
///     - **Array support** - Validating array-to-iterable and iterable-to-array combinations
///     - **Edge cases** - Null handling, empty collections, duplicates, and special values
///     - **Message handling** - Custom messages and message suppliers
///     - **Error reporting** - Ensuring descriptive failure messages
///
/// @author evanbergstrom
/// @since 1.0.0
public class AssertContainsAllTest {

    private static final String TEST_MESSAGE = "Test message";

    /// Tests assertion behavior with empty collections.
    ///
    /// Verifies that empty expected collections always pass (vacuous truth) and that
    /// non-empty expected collections fail when testing against empty actual collections.
    @Test
    public void testAssertContainsAllOnEmptyCollection() {
        Iterable<Integer> empty1 = IterableTestUtils.empty();
        assertContainsAll(empty1, empty1);
        assertContainsAll(empty1, empty1, TEST_MESSAGE);
        assertContainsAll(empty1, empty1, () -> TEST_MESSAGE);

        Iterable<Integer> empty2 = IterableTestUtils.empty();
        assertContainsAll(empty1, empty2);
        assertContainsAll(empty1, empty2, TEST_MESSAGE);
        assertContainsAll(empty1, empty2, () -> TEST_MESSAGE);

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(nonEmpty, empty1);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(nonEmpty, empty1, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(nonEmpty, empty1, () -> TEST_MESSAGE);
        });
    }

    /// Tests assertion behavior with null collections.
    ///
    /// Verifies that `null` collections are properly rejected with
    /// [NullPointerException] in all parameter combinations.
    @SuppressWarnings("DataFlowIssue") // Explicitly testing null parameters
    @Test
    public void testAssertContainsAllOnNullCollection() {

        Iterable<Integer> nonEmpty = IterableOnly.of(1);
        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertContainsAll(nonEmpty, (Iterable<?>)null);
        });

        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertContainsAll((Iterable<?>)null, nonEmpty);
        });

        assertThrows(NullPointerException.class, () -> {
            CollectionAssertions.assertContainsAll((Iterable<?>)null, (Iterable<?>)null);
        });
    }

    /// Tests basic assertion functionality with non-empty collections.
    ///
    /// Validates that subset-superset relationships work correctly and that
    /// attempting to assert superset-subset relationships fail appropriately.
    @Test
    public void testAssertContainsAllOnCollectionWithElements() {
        Iterable<Integer> superset = IterableOnly.of(1, 2, 3, 4);
        Iterable<Integer> subset = IterableOnly.of(2, 3);
        assertContainsAll(subset, superset);

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset, () -> TEST_MESSAGE);
        });
    }

    /// Tests basic assertion functionality with non-empty array and non-empty collection.
    ///
    /// Validates that subset-superset relationships work correctly and that
    /// attempting to assert superset-subset relationships fail appropriately.
    @Test
    public void testAssertContainsAllOnExpectedArrayWithElements() {
        Integer[] superset = new Integer[] {1, 2, 3, 4};
        Iterable<Integer> subset = IterableOnly.of(2, 3);
        assertContainsAll(subset, superset);

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset, () -> TEST_MESSAGE);
        });
    }

    /// Tests basic assertion functionality with non-empty array and non-empty collection.
    ///
    /// Validates that subset-superset relationships work correctly and that
    /// attempting to assert superset-subset relationships fail appropriately.
    @Test
    public void testAssertContainsAllOnActualArrayWithElements() {
        Iterable<Integer> superset = IterableOnly.of(1, 2, 3, 4);
        Integer[] subset = new Integer[]{2, 3};
        assertContainsAll(subset, superset);

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset, TEST_MESSAGE);
        });

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(superset, subset, () -> TEST_MESSAGE);
        });
    }

    /// Tests custom message functionality with static message string.
    ///
    /// Verifies that custom failure messages are properly included in assertion exceptions.
    @Test
    public void testAssertContainsAllWithMessage() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(IterableOnly.of(1, 2), IterableOnly.of(1), TEST_MESSAGE);
        }, TEST_MESSAGE);
    }

    /// Tests custom message functionality with message supplier.
    ///
    /// Verifies that message suppliers are properly invoked and their results
    /// are included in assertion exceptions. This enables lazy message generation.
    @Test
    public void testAssertContainsAllWithMessageSupplier() {
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(IterableOnly.of(1, 2), IterableOnly.of(1), () -> TEST_MESSAGE);
        }, TEST_MESSAGE);
    }

    /// Tests array-based collections with iterable assertions.
    ///
    /// Validates assertion functionality when one operand is an array converted to
    /// an iterable, testing both passing and failing scenarios.
    @Test
    public void testAssertContainsAllArrayBasedCollections() {
        List<Integer> expectedList = Arrays.asList(1, 2);
        List<Integer> actualList = Arrays.asList(1, 2, 3, 4);

        // Should pass - actualList contains all elements from expectedList
        assertContainsAll(expectedList, actualList);

        // Should fail - actualList doesn't contain all elements from a larger list
        List<Integer> largerList = Arrays.asList(1, 2, 3, 4, 5);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(largerList, actualList);
        });

        // Test with empty array as list - should always pass (vacuous truth)
        List<Integer> emptyList = Arrays.asList();
        assertContainsAll(emptyList, actualList);
        assertContainsAll(emptyList, IterableTestUtils.empty());
    }

    /// Tests different collection types with iterable assertions.
    ///
    /// Validates assertion functionality when using different concrete collection
    /// implementations, testing both passing and failing scenarios.
    @Test
    public void testAssertContainsAllDifferentCollectionTypes() {
        List<Integer> expectedList = Arrays.asList(2, 3);
        List<Integer> actualArray = Arrays.asList(1, 2, 3, 4);

        // Should pass - actualArray contains all elements from expectedList
        assertContainsAll(expectedList, actualArray);

        // Should fail - smaller array doesn't contain all expected elements
        List<Integer> smallerArray = Arrays.asList(1, 2);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(expectedList, smallerArray);
        });

        // Test with empty iterable - should always pass (vacuous truth)
        Iterable<Integer> emptyIterable = IterableTestUtils.empty();
        assertContainsAll(emptyIterable, actualArray);
        assertContainsAll(emptyIterable, Arrays.asList());
    }

    /// Tests assertion behavior with duplicate elements.
    ///
    /// Verifies that duplicate elements in either expected or actual collections
    /// are handled correctly using set-based membership testing.
    @Test
    public void testAssertContainsAllWithDuplicates() {
        // Expected has duplicates, actual doesn't - should pass
        List<Integer> expectedWithDuplicates = Arrays.asList(1, 1, 2, 2, 3);
        List<Integer> actualWithoutDuplicates = Arrays.asList(1, 2, 3, 4);
        assertContainsAll(expectedWithDuplicates, actualWithoutDuplicates);

        // Expected has unique elements, actual has duplicates - should pass
        List<Integer> expectedUnique = Arrays.asList(1, 2);
        List<Integer> actualWithDuplicates = Arrays.asList(1, 1, 2, 2, 3, 3);
        assertContainsAll(expectedUnique, actualWithDuplicates);

        // Both have duplicates - should pass if all unique elements are present
        assertContainsAll(expectedWithDuplicates, actualWithDuplicates);
    }

    /// Tests assertion behavior with null elements in collections.
    ///
    /// Verifies that collections containing `null` elements are handled
    /// correctly and that `null` elements participate in equality testing.
    @Test
    public void testAssertContainsAllWithNullElements() {
        List<Integer> expectedWithNull = Arrays.asList(1, null, 3);
        List<Integer> actualWithNull = Arrays.asList(1, 2, null, 3, 4);

        // Should pass - actualWithNull contains all elements including null
        assertContainsAll(expectedWithNull, actualWithNull);

        // Should fail - actual doesn't contain null
        List<Integer> actualWithoutNull = Arrays.asList(1, 2, 3, 4);
        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(expectedWithNull, actualWithoutNull);
        });

        // Test arrays with null elements converted to lists
        List<Integer> arrayWithNull = Arrays.asList(1, null, 3);
        assertContainsAll(arrayWithNull, actualWithNull);

        assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(arrayWithNull, actualWithoutNull);
        });
    }

    /// Tests error message content and structure.
    ///
    /// Validates that assertion failures provide descriptive error messages
    /// that include information about missing elements.
    @Test
    public void testAssertContainsAllErrorMessageContent() {
        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> actual = Arrays.asList(1, 3, 5);

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
            assertContainsAll(expected, actual);
        });

        // Verify the error message contains information about missing elements
        String errorMessage = error.getMessage();
        assertTrue(errorMessage.contains("missing elements"),
                   "Error message should mention 'missing elements': " + errorMessage);

        // The missing elements should be 2 and 4
        assertTrue(errorMessage.contains("2"),
                   "Error message should contain missing element '2': " + errorMessage);
        assertTrue(errorMessage.contains("4"),
                   "Error message should contain missing element '4': " + errorMessage);
    }

    /// Tests assertion with identical collections.
    ///
    /// Verifies that asserting a collection contains all elements of itself
    /// always passes, including edge cases with various collection types.
    @Test
    public void testAssertContainsAllIdenticalCollections() {
        List<String> stringList = Arrays.asList("apple", "banana", "cherry");
        assertContainsAll(stringList, stringList);

        List<String> stringArray = Arrays.asList("apple", "banana", "cherry");
        assertContainsAll(stringArray, stringArray);

        // Test with mixed types (different list instances with same elements)
        assertContainsAll(stringArray, stringList);
        assertContainsAll(stringList, stringArray);
    }
}
