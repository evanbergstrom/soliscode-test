package com.soliscode.test.assertions.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.interfaces.IterableOnly;
import com.soliscode.test.util.IterableTestUtils;

import static com.soliscode.test.assertions.collection.CollectionAssertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **AssertEqualsTest** - Comprehensive test suite for [com.soliscode.test.assertions.collection.AssertEquals].
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
/// @author evanbergstrom
/// @since 1.0.0
public class AssertEqualsTest {

    private static final String TEST_MESSAGE = "Test message";

    /// Default constructor.
    public AssertEqualsTest() {}

    /// Tests basic assertion functionality with identical collections.
    ///
    /// Verifies that the assertion passes when both collections contain the same elements
    /// in the same order, including self-comparison and identical content scenarios.
    @Test
    @DisplayName("assertEquals() passes for identical collections and for a collection compared with itself")
    public void testAssertEqualsIdenticalCollections() {
        List<Integer> list1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> list2 = Arrays.asList(1, 2, 3, 4);

        // Self-comparison should always pass
        assertEquals(list1, list1);

        // Collections with identical content should pass
        assertEquals(list1, list2);
        assertEquals(list2, list1);

        // Test with message variants
        assertEquals(list1, list2, TEST_MESSAGE);
        assertEquals(list1, list2, () -> TEST_MESSAGE);

        // Test with generic Iterable
        Iterable<String> iterable1 = IterableOnly.of("apple", "banana", "cherry");
        Iterable<String> iterable2 = IterableOnly.of("apple", "banana", "cherry");

        assertEquals(iterable1, iterable1);
        assertEquals(iterable1, iterable2);
    }

    /// Tests assertion behavior with empty collections.
    ///
    /// Verifies that empty iterables are equal to each other, and that an empty iterable is not equal to a
    /// non-empty one, in either order.
    @Test
    @DisplayName("assertEquals() passes for empty iterables and fails for an empty and a non-empty one")
    public void testAssertEqualsEmptyCollections() {
        Iterable<Integer> empty1 = IterableTestUtils.empty();
        Iterable<Integer> empty2 = IterableTestUtils.empty();
        List<Integer> emptyList = new ArrayList<>();

        assertDoesNotThrow(() -> assertEquals(empty1, empty1));
        assertDoesNotThrow(() -> assertEquals(empty1, empty2));
        assertDoesNotThrow(() -> assertEquals(empty1, emptyList));
        assertDoesNotThrow(() -> assertEquals(emptyList, empty1));
        assertDoesNotThrow(() -> assertEquals(empty1, empty2, TEST_MESSAGE));
        assertDoesNotThrow(() -> assertEquals(empty1, empty2, () -> TEST_MESSAGE));

        List<Integer> nonEmpty = List.of(1);
        assertThrows(AssertionFailedError.class, () -> assertEquals(empty1, nonEmpty));
        assertThrows(AssertionFailedError.class, () -> assertEquals(nonEmpty, empty1));
    }

    /// Tests the `assertEquals(E[] expected, Iterable<?> actual)` overloads.
    ///
    /// Verifies that an array and an iterable with the same elements in the same order are equal, and that the
    /// same elements in another order are not.
    @Test
    @DisplayName("assertEquals(E[], Iterable) passes for the same elements in order and fails for another order")
    public void testAssertEqualsArrayToIterable() {
        Integer[] expectedArray = {1, 2, 3, 4};
        List<Integer> actualList = Arrays.asList(1, 2, 3, 4);

        assertDoesNotThrow(() -> assertEquals(expectedArray, actualList));
        assertDoesNotThrow(() -> assertEquals(expectedArray, actualList, TEST_MESSAGE));
        assertDoesNotThrow(() -> assertEquals(expectedArray, actualList, () -> TEST_MESSAGE));

        List<Integer> differentList = Arrays.asList(4, 3, 2, 1);
        assertThrows(AssertionFailedError.class, () -> assertEquals(expectedArray, differentList));

        Integer[] emptyArray = {};
        List<Integer> emptyList = new ArrayList<>();
        assertDoesNotThrow(() -> assertEquals(emptyArray, emptyList));
    }

    /// Tests the `assertEquals(Iterable<?> expected, E[] actual)` overloads.
    ///
    /// Verifies that an iterable and an array with the same elements in the same order are equal, and that the
    /// same elements in another order are not.
    @Test
    @DisplayName("assertEquals(Iterable, E[]) passes for the same elements in order and fails for another order")
    public void testAssertEqualsIterableToArray() {
        List<String> expectedList = Arrays.asList("apple", "banana", "cherry");
        String[] actualArray = {"apple", "banana", "cherry"};

        assertDoesNotThrow(() -> assertEquals(expectedList, actualArray));
        assertDoesNotThrow(() -> assertEquals(expectedList, actualArray, TEST_MESSAGE));
        assertDoesNotThrow(() -> assertEquals(expectedList, actualArray, () -> TEST_MESSAGE));

        String[] differentOrderArray = {"cherry", "banana", "apple"};
        assertThrows(AssertionFailedError.class, () -> assertEquals(expectedList, differentOrderArray));
    }

    /// Tests assertion behavior when the iterables have different sizes.
    ///
    /// Verifies that the assertion fails when either iterable is a prefix of the other, with each message variant.
    @Test
    @DisplayName("assertEquals() fails for iterables of different sizes")
    public void testAssertEqualsDifferentSizes() {
        List<Integer> shorter = Arrays.asList(1, 2);
        List<Integer> longer = Arrays.asList(1, 2, 3, 4);

        assertThrows(AssertionFailedError.class, () -> assertEquals(shorter, longer));
        assertThrows(AssertionFailedError.class, () -> assertEquals(longer, shorter));
        assertThrows(AssertionFailedError.class, () -> assertEquals(shorter, longer, TEST_MESSAGE));
        assertThrows(AssertionFailedError.class, () -> assertEquals(shorter, longer, () -> TEST_MESSAGE));
    }

    /// Tests assertion behavior with null collections.
    ///
    /// Verifies that null collections are rejected with
    /// [NullPointerException] in all method overloads.
    @SuppressWarnings("DataFlowIssue") // Explicitly testing null parameters
    @Test
    @DisplayName("assertEquals() throws NullPointerException for a null collection")
    public void testAssertEqualsNullCollections() {
        List<Integer> nonNull = Arrays.asList(1, 2, 3);

        // Null expected collection
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertEquals((Iterable<Integer>) null, nonNull));

        // Null actual collection
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertEquals(nonNull, (Iterable<Integer>) null));

        // Both null
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertEquals((Iterable<Integer>) null, (Iterable<Integer>) null));

        // Test with message variants
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertEquals((Iterable<Integer>) null, nonNull, TEST_MESSAGE));

        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertEquals(nonNull, (Iterable<Integer>) null, () -> TEST_MESSAGE));
    }

    /// Tests assertion behavior with iterables that contain null elements.
    ///
    /// Verifies that null elements are compared as `Objects.equals` compares them, as JUnit's
    /// `assertIterableEquals` does: a null equals a null and nothing else.
    @Test
    @DisplayName("assertEquals() treats null elements as equal to null only")
    public void testAssertEqualsCollectionsWithNull() {
        List<Integer> listWithNull1 = Arrays.asList(1, null, 3, null);
        List<Integer> listWithNull2 = Arrays.asList(1, null, 3, null);

        assertDoesNotThrow(() -> assertEquals(listWithNull1, listWithNull1));
        assertDoesNotThrow(() -> assertEquals(listWithNull1, listWithNull2));

        List<Integer> nullFirst = Arrays.asList(null, 2);
        List<Integer> valueFirst = Arrays.asList(1, 2);
        assertThrows(AssertionFailedError.class, () -> assertEquals(nullFirst, valueFirst));
        assertThrows(AssertionFailedError.class, () -> assertEquals(valueFirst, nullFirst));

        List<Integer> noNulls1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> noNulls2 = Arrays.asList(1, 2, 3, 4);
        assertDoesNotThrow(() -> assertEquals(noNulls1, noNulls2));

        List<Integer> different = Arrays.asList(5, 6, 7, 8);
        assertThrows(AssertionFailedError.class, () -> assertEquals(noNulls1, different));
    }

    /// Tests assertion with duplicate elements.
    ///
    /// Verifies that collections with duplicate elements are compared correctly
    /// and that the order and frequency of duplicates matter.
    @Test
    @DisplayName("assertEquals() fails when duplicates differ in order or count")
    public void testAssertEqualsWithDuplicates() {
        List<Integer> duplicates1 = Arrays.asList(1, 2, 2, 3, 2);
        List<Integer> duplicates2 = Arrays.asList(1, 2, 2, 3, 2);

        // Identical duplicates should be equal
        assertEquals(duplicates1, duplicates1);
        assertEquals(duplicates1, duplicates2);

        // Different duplicate patterns should fail
        List<Integer> differentDuplicates = Arrays.asList(2, 1, 2, 3, 2);

        assertThrows(AssertionFailedError.class, () -> assertEquals(duplicates1, differentDuplicates));

        // Different duplicate counts should fail due to the size difference
        List<Integer> moreDuplicates = Arrays.asList(1, 2, 2, 2, 3, 2);

        assertThrows(AssertionFailedError.class, () -> assertEquals(duplicates1, moreDuplicates));
    }

    /// Tests the overload with a message.
    ///
    /// Verifies that the assertion fails for iterables of different sizes, and that the failure's message
    /// contains the given message.
    @Test
    @DisplayName("assertEquals() with a message fails with that message")
    public void testAssertEqualsWithMessage() {
        List<Integer> list1 = Arrays.asList(1, 2);
        List<Integer> list2 = Arrays.asList(1, 2, 3);

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertEquals(list1, list2, TEST_MESSAGE));
        assertTrue(String.valueOf(error.getMessage()).contains(TEST_MESSAGE),
                () -> "the failure's message should contain the given message: " + error.getMessage());
    }

    /// Tests the overload with a message supplier.
    ///
    /// Verifies that the assertion fails for iterables of different sizes, and that the failure's message
    /// contains the supplier's result.
    @Test
    @DisplayName("assertEquals() with a message supplier fails with the supplied message")
    public void testAssertEqualsWithMessageSupplier() {
        List<Integer> list1 = List.of(1);
        List<Integer> list2 = Arrays.asList(1, 2);

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertEquals(list1, list2, () -> TEST_MESSAGE));
        assertTrue(String.valueOf(error.getMessage()).contains(TEST_MESSAGE),
                () -> "the failure's message should contain the supplied message: " + error.getMessage());
    }

    /// Tests assertion with different data types.
    ///
    /// Verifies that the assertion works correctly with various object types
    /// including strings, numbers, and mixed-type collections.
    @Test
    @DisplayName("assertEquals() passes for equal collections of strings, mixed types and arrays")
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

    /// Tests assertion behavior with special floating-point values.
    ///
    /// Verifies that `Double.equals` decides: NaN equals NaN, `0.0` does not equal `-0.0`, so the same
    /// values are equal and the values reordered are not.
    @Test
    @DisplayName("assertEquals() passes for the same special floating-point values and fails for them reordered")
    public void testAssertEqualsSpecialValues() {
        List<Double> specialValues1 = Arrays.asList(1.0, Double.NaN, Double.POSITIVE_INFINITY, 0.0, -0.0);
        List<Double> specialValues2 = Arrays.asList(1.0, Double.NaN, Double.POSITIVE_INFINITY, 0.0, -0.0);

        assertDoesNotThrow(() -> assertEquals(specialValues1, specialValues1));
        assertDoesNotThrow(() -> assertEquals(specialValues1, specialValues2));

        List<Double> reordered = Arrays.asList(Double.NaN, 1.0, Double.POSITIVE_INFINITY, -0.0, 0.0);
        assertThrows(AssertionFailedError.class, () -> assertEquals(specialValues1, reordered));
    }

    /// Tests iterables of the same size with different elements.
    ///
    /// Verifies that the assertion compares the expected elements with the actual ones (not with themselves),
    /// so iterables of the same size with different elements are not equal.
    @Test
    @DisplayName("assertEquals() fails for iterables of the same size with different elements")
    public void testAssertEqualsSameSizeDifferentElements() {
        List<Integer> expected = Arrays.asList(1, 2, 3);
        List<Integer> actualDifferent = Arrays.asList(4, 5, 6);
        assertThrows(AssertionFailedError.class, () -> assertEquals(expected, actualDifferent));

        List<String> expectedStrings = Arrays.asList("apple", "banana", "cherry");
        List<String> actualDifferentStrings = Arrays.asList("dog", "elephant", "fish");
        assertThrows(AssertionFailedError.class, () -> assertEquals(expectedStrings, actualDifferentStrings));
    }

    /// Tests assertion behavior with large collections.
    ///
    /// Verifies that two equal lists of 1000 elements are equal, and that they are not once one element differs.
    @Test
    @DisplayName("assertEquals() passes for equal large collections and fails when one element differs")
    public void testAssertEqualsLargeCollections() {
        List<Integer> large1 = new ArrayList<>();
        List<Integer> large2 = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            large1.add(i);
            large2.add(i);
        }

        assertDoesNotThrow(() -> assertEquals(large1, large1));
        assertDoesNotThrow(() -> assertEquals(large1, large2));

        large2.set(500, 999999);
        assertThrows(AssertionFailedError.class, () -> assertEquals(large1, large2));
    }
}
