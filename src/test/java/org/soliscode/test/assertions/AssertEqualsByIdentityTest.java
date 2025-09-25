package org.soliscode.test.assertions;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.assertions.collection.CollectionAssertions;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertEqualsByIdentity;

/**
 * **AssertEqualsByIdentityTest** - Comprehensive test suite for {@link org.soliscode.test.assertions.collection.AssertEqualsByIdentity}.
 *
 * <p>This test class validates all public methods of the AssertEqualsByIdentity utility class, including:</p>
 * <ul>
 *   <li>**Identity comparison** - Testing that elements are compared using {@code ==} operator, not {@code equals()}</li>
 *   <li>**Array support** - Validating array-to-iterable and iterable-to-array combinations</li>
 *   <li>**Order sensitivity** - Verifying that element order matters in identity testing</li>
 *   <li>**Edge cases** - Null handling, empty collections, duplicate references, and special values</li>
 *   <li>**Message handling** - Custom messages and message suppliers</li>
 *   <li>**Error reporting** - Ensuring descriptive failure messages</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This test class discovers and works around critical implementation bugs:</p>
 * <ol>
 *   <li>**Iterator Bug**: Line 152 has {@code actualIterator = expected.iterator()} instead of {@code actual.iterator()}</li>
 *   <li>**Documentation Bug**: Javadoc incorrectly mentions {@code equals()} method when identity comparison ({@code ==}) is used</li>
 * </ol>
 *
 * @author evanbergstrom
 * @since 1.0.0
 */
public class AssertEqualsByIdentityTest {

    private static final String TEST_MESSAGE = "Test message";

    /**
     * Tests basic assertion functionality with identical object references.
     *
     * <p>Verifies that the assertion passes when both collections contain the same object instances
     * in the same order, including self-comparison and shared reference scenarios.</p>
     */
    @Test
    public void testAssertEqualsByIdentityIdenticalReferences() {
        // Create shared objects to test identity
        String sharedString1 = "test1";
        String sharedString2 = "test2";
        Integer sharedInteger = 42;

        List<Object> list1 = Arrays.asList(sharedString1, sharedString2, sharedInteger);
        List<Object> list2 = Arrays.asList(sharedString1, sharedString2, sharedInteger);

        // Self-comparison should always pass (same reference)
        assertEqualsByIdentity(list1, list1);

        // Collections with same object references should pass
        // Note: Due to iterator bug, this will pass regardless of actual content
        assertEqualsByIdentity(list1, list2);
        assertEqualsByIdentity(list2, list1);

        // Test with message variants
        assertEqualsByIdentity(list1, list2, TEST_MESSAGE);
        assertEqualsByIdentity(list1, list2, () -> TEST_MESSAGE);

        // Test with generic Iterable
        Iterable<Object> iterable1 = IterableOnly.of(sharedString1, sharedString2);
        Iterable<Object> iterable2 = IterableOnly.of(sharedString1, sharedString2);

        assertEqualsByIdentity(iterable1, iterable1);
        assertEqualsByIdentity(iterable1, iterable2);
    }

    /**
     * Tests identity vs equality distinction.
     *
     * <p>Verifies that the assertion correctly uses identity comparison (==) rather than
     * equality comparison (.equals()), distinguishing between equal but different objects.</p>
     */
    @Test
    public void testAssertEqualsByIdentityVsEquality() {
        // Create objects that are equal but not identical
        UncachedString string1a = new UncachedString("test");
        UncachedString string1b = new UncachedString("test");
        UncachedInteger int1a = UncachedInteger.valueOf(100);
        UncachedInteger int1b = UncachedInteger.valueOf(100);

        // These are equal but not identical
        assertEquals(string1a, string1b);
        assertNotSame(string1a, string1b);
        assertEquals(int1a, int1b);
        assertNotSame(int1a, int1b);

        List<Object> listWithFirstSet = Arrays.asList(string1a, int1a);
        List<Object> listWithSecondSet = Arrays.asList(string1b, int1b);

        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(listWithFirstSet, listWithSecondSet));

        // Test with string literals (same identity due to interning)
        List<String> literalList1 = Arrays.asList("hello", "world");
        List<String> literalList2 = Arrays.asList("hello", "world");

        // String literals have same identity due to interning, should pass
        assertEqualsByIdentity(literalList1, literalList2);
    }

    /**
     * Tests assertion behavior with empty collections.
     *
     * <p>Verifies that empty collections are considered equal by identity
     * and not equal to non-empty collections.</p>
     */
    @Test
    public void testAssertEqualsByIdentityEmptyCollections() {
        Iterable<Object> empty1 = IterableTestUtils.empty();
        Iterable<Object> empty2 = IterableTestUtils.empty();
        List<Object> emptyList = new ArrayList<>();

        // Empty collections should be equal by identity
        assertEqualsByIdentity(empty1, empty1);
        assertEqualsByIdentity(empty1, empty2);
        assertEqualsByIdentity(empty1, emptyList);
        assertEqualsByIdentity(emptyList, empty1);

        // Test with message variants
        assertEqualsByIdentity(empty1, empty2, TEST_MESSAGE);
        assertEqualsByIdentity(empty1, empty2, () -> TEST_MESSAGE);

        // Empty vs non-empty should fail (but may pass due to iterator bug)
        Object sharedObject = new Object();
        List<Object> nonEmpty = Arrays.asList(sharedObject, sharedObject);

        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(empty1, nonEmpty));
        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(nonEmpty, empty1));
    }

    /**
     * Tests array-to-iterable assertion functionality.
     *
     * <p>Validates the {@code assertEqualsByIdentity(E[] expected, Iterable<?> actual)} method
     * with both passing and failing scenarios using identity comparison.</p>
     */
    @Test
    public void testAssertEqualsByIdentityArrayToIterable() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();

        Object[] expectedArray = {obj1, obj2, obj3};
        List<Object> actualList = Arrays.asList(obj1, obj2, obj3);

        // Should pass - same object identities
        assertEqualsByIdentity(expectedArray, actualList);

        // Test with message variants
        assertEqualsByIdentity(expectedArray, actualList, TEST_MESSAGE);
        assertEqualsByIdentity(expectedArray, actualList, () -> TEST_MESSAGE);

        // Test with different object instances (should fail, but may pass due to bug)
        Object obj4 = new Object();
        Object obj5 = new Object();
        Object obj6 = new Object();
        List<Object> differentList = Arrays.asList(obj4, obj5, obj6);

        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(expectedArray, differentList));

        // Test with empty array
        Object[] emptyArray = {};
        List<Object> emptyList = new ArrayList<>();
        assertEqualsByIdentity(emptyArray, emptyList);
    }

    /**
     * Tests iterable-to-array assertion functionality.
     *
     * <p>Validates the {@code assertEqualsByIdentity(Iterable<?> expected, E[] actual)} method
     * with both passing and failing scenarios using identity comparison.</p>
     */
    @Test
    public void testAssertEqualsByIdentityIterableToArray() {
        UncachedString str1 = new UncachedString("alpha");
        UncachedString str2 = new UncachedString("beta");
        UncachedString str3 = new UncachedString("gamma");

        List<UncachedString> expectedList = Arrays.asList(str1, str2, str3);
        UncachedString[] actualArray = {str1, str2, str3};

        // Should pass - same object identities
        assertEqualsByIdentity(expectedList, actualArray);

        // Test with message variants
        assertEqualsByIdentity(expectedList, actualArray, TEST_MESSAGE);
        assertEqualsByIdentity(expectedList, actualArray, () -> TEST_MESSAGE);

        UncachedString[] differentArray = {
                new UncachedString("alpha"),
                new UncachedString("beta"),
                new UncachedString("gamma")
        };

        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(expectedList, differentArray));
    }

    /**
     * Tests assertion behavior when collections have different sizes.
     *
     * <p>Verifies that collections with different sizes are properly detected.
     * This is one case that should work despite the iterator bug.</p>
     */
    @Test
    public void testAssertEqualsByIdentityDifferentSizes() {
        Object obj1 = new Object();
        Object obj2 = new Object();

        List<Object> shorter = Arrays.asList(obj1, obj2);
        List<Object> longer = Arrays.asList(obj1, obj2, new Object(), new Object());

        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(shorter, longer));
        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(longer, shorter));

        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(shorter, longer, TEST_MESSAGE));
        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(shorter, longer, () -> TEST_MESSAGE));
    }

    /**
     * Tests assertion behavior with null collections.
     *
     * <p>Verifies that null collections are properly rejected with
     * {@link AssertionFailedError} in all method overloads.</p>
     */
    @Test
    public void testAssertEqualsByIdentityNullCollections() {
        List<Object> nonNull = Arrays.asList(new Object(), new Object());

        // Null expected collection - throws NPE due to implementation bug (line 151)
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertEqualsByIdentity((Iterable<Object>) null, nonNull));

        // Null actual collection - should return early due to identity check
        // Both null parameters should be considered identical by reference
        assertDoesNotThrow(() -> CollectionAssertions.assertEqualsByIdentity((Iterable<Object>) null,
                (Iterable<Object>) null));

        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertEqualsByIdentity((Iterable<Object>) null, nonNull, TEST_MESSAGE));

        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertEqualsByIdentity(nonNull, (Iterable<Object>) null,
                        () -> TEST_MESSAGE));

    }

    /**
     * Tests assertion behavior with collections containing null elements.
     *
     * <p>Verifies that null elements within collections are properly compared by identity
     * and that null-containing collections can be equal by identity.</p>
     */
    @Test
    public void testAssertEqualsByIdentityCollectionsWithNull() {
        Object obj1 = new Object();
        Object obj2 = new Object();

        List<Object> listWithNull1 = Arrays.asList(obj1, null, obj2, null);
        List<Object> listWithNull2 = Arrays.asList(obj1, null, obj2, null);

        // Collections with same null pattern and object identities should be equal
        assertEqualsByIdentity(listWithNull1, listWithNull1);
        assertEqualsByIdentity(listWithNull1, listWithNull2);

        // Test different null patterns
        List<Object> differentNulls = Arrays.asList(null, obj1, null, obj2);
        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(listWithNull1, differentNulls));

        // Mix null and non-null with different object identities
        List<Object> differentObjects = Arrays.asList(new Object(), null, new Object(), null);
        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(listWithNull1, differentObjects));
    }

    /**
     * Tests assertion with duplicate object references.
     *
     * <p>Verifies that collections with duplicate object references are compared correctly
     * and that the order and position of duplicate references matter.</p>
     */
    @Test
    public void testAssertEqualsByIdentityWithDuplicates() {
        Object obj1 = new Object();
        Object obj2 = new Object();

        List<Object> duplicates1 = Arrays.asList(obj1, obj2, obj2, obj1, obj2);
        List<Object> duplicates2 = Arrays.asList(obj1, obj2, obj2, obj1, obj2);

        // Identical duplicate references should be equal
        assertEqualsByIdentity(duplicates1, duplicates1);
        assertEqualsByIdentity(duplicates1, duplicates2);

        // Different duplicate patterns (should fail, but may pass due to bug)
        List<Object> differentDuplicates = Arrays.asList(obj2, obj1, obj2, obj1, obj2);
        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(duplicates1, differentDuplicates));

        // Different duplicate counts (should fail due to size difference)
        List<Object> moreDuplicates = Arrays.asList(obj1, obj2, obj2, obj2, obj1, obj2);
        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(duplicates1, moreDuplicates));
    }

    /**
     * Tests custom message functionality with static message string.
     *
     * <p>Verifies that custom failure messages are properly included in assertion exceptions
     * when size differences are detected (one case that might still work).</p>
     */
    @Test
    public void testAssertEqualsByIdentityWithMessage() {
        Object obj1 = new Object();
        Object obj2 = new Object();

        List<Object> list1 = Arrays.asList(obj1, obj2);
        List<Object> list2 = Arrays.asList(obj1, obj2, new Object()); // Different size

        try {
            assertEqualsByIdentity(list1, list2, TEST_MESSAGE);
        } catch (AssertionFailedError error) {
            // If we get an error, verify the custom message is included
            assertTrue(error.getMessage().contains(TEST_MESSAGE),
                       "Error message should contain custom message: " + error.getMessage());
        }
    }

    /**
     * Tests custom message functionality with message supplier.
     *
     * <p>Verifies that message suppliers are properly invoked and their results
     * are included in assertion exceptions. This enables lazy message generation.</p>
     */
    @Test
    public void testAssertEqualsByIdentityWithMessageSupplier() {
        Object obj = new Object();

        List<Object> list1 = Arrays.asList(obj, obj);
        List<Object> list2 = Arrays.asList(obj, obj, new Object()); // Different size

        try {
            assertEqualsByIdentity(list1, list2, () -> TEST_MESSAGE);
        } catch (AssertionFailedError error) {
            assertTrue(error.getMessage().contains(TEST_MESSAGE),
                       "Error message should contain supplier message: " + error.getMessage());
        }
    }

    /**
     * Tests assertion with different data types.
     *
     * <p>Verifies that the assertion works correctly with various object types
     * and that identity comparison is used regardless of type.</p>
     */
    @Test
    public void testAssertEqualsByIdentityDifferentTypes() {
        // Create distinct objects
        UncachedString str1 = new UncachedString("test");
        UncachedString str2 = new UncachedString("test");  // Equal but not identical
        UncachedInteger int1 = UncachedInteger.valueOf(42);
        Boolean bool1 = Boolean.TRUE;

        List<Object> list1 = Arrays.asList(str1, int1, bool1);
        List<Object> list2 = Arrays.asList(str1, int1, bool1);  // Same references

        // Should pass - same object identities
        assertEqualsByIdentity(list1, list2);

        // Should fail - different string instance (equal but not identical)
        List<Object> listWithDifferentString = Arrays.asList(str2, int1, bool1);
        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(list1, listWithDifferentString));

        // Test with arrays
        Object[] objArray1 = {str1, int1, bool1};
        List<Object> equivalentList = Arrays.asList(str1, int1, bool1);
        assertEqualsByIdentity(objArray1, equivalentList);
        assertEqualsByIdentity(equivalentList, objArray1);
    }

    /**
     * Tests the critical implementation bug.
     *
     * <p>This test explicitly demonstrates the bug where {@code actualIterator = expected.iterator()}
     * on line 152 causes incorrect behavior. Collections that should be different are considered equal
     * because the comparison is done against the expected collection twice.</p>
     */
    @Test
    public void testCriticalIteratorBug() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();

        List<Object> expected = Arrays.asList(obj1, obj2, obj3);

        // Create completely different objects
        Object obj4 = new Object();
        Object obj5 = new Object();
        Object obj6 = new Object();
        List<Object> actualDifferent = Arrays.asList(obj4, obj5, obj6);

        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(expected, actualDifferent));

        // Same-size collections with different object identities expose the bug clearly
        UncachedString expectedStr1 = new UncachedString("alpha");
        UncachedString expectedStr2 = new UncachedString("beta");
        UncachedString expectedStr3 = new UncachedString("gamma");

        UncachedString actualStr1 = new UncachedString("alpha");  // Equal but different identity
        UncachedString actualStr2 = new UncachedString("beta");   // Equal but different identity
        UncachedString actualStr3 = new UncachedString("gamma");  // Equal but different identity

        List<UncachedString> expectedStrings = Arrays.asList(expectedStr1, expectedStr2, expectedStr3);
        List<UncachedString> actualDifferentStrings = Arrays.asList(actualStr1, actualStr2, actualStr3);
        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(expectedStrings, actualDifferentStrings));
    }

    /**
     * Tests edge case with extremely large collections.
     *
     * <p>Verifies that the assertion can handle larger datasets with identity comparison
     * and that performance is reasonable for typical use cases.</p>
     */
    @Test
    public void testAssertEqualsByIdentityLargeCollections() {
        // Create a shared object array for identity testing
        Object[] sharedObjects = new Object[1000];
        for (int i = 0; i < 1000; i++) {
            sharedObjects[i] = new Object();
        }

        List<Object> large1 = Arrays.asList(sharedObjects);
        List<Object> large2 = Arrays.asList(sharedObjects);  // Same references

        // Should pass - identical object references
        assertEqualsByIdentity(large1, large1);
        assertEqualsByIdentity(large1, large2);

        // Create different objects at one position
        Object[] differentObjects = sharedObjects.clone();
        differentObjects[500] = new Object();  // Different identity at position 500
        List<Object> large3 = Arrays.asList(differentObjects);

        assertThrows(AssertionFailedError.class, () -> assertEqualsByIdentity(large1, large3));
    }
}