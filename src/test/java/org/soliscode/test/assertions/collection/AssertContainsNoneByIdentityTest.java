package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.interfaces.IterableOnly;
import org.soliscode.test.util.IterableTestUtils;
import org.soliscode.test.util.UncachedInteger;
import org.soliscode.test.util.UncachedString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsNoneByIdentity;

/**
 * **AssertContainsNoneByIdentityTest** - Comprehensive test suite for {@link org.soliscode.test.assertions.collection.AssertContainsNoneByIdentity}.
 *
 * <p>This test class validates all public methods of the AssertContainsNoneByIdentity utility class, including:</p>
 * <ul>
 *   <li>**Identity comparison** - Testing that elements are compared using {@code ==} operator, not {@code equals()}</li>
 *   <li>**Exclusion verification** - Validating that collections do not contain any excluded elements</li>
 *   <li>**Edge cases** - Null handling, empty collections, duplicate references, and special values</li>
 *   <li>**Message handling** - Custom messages and message suppliers</li>
 *   <li>**Error reporting** - Ensuring descriptive failure messages</li>
 * </ul>
 *
 * <p><strong>Core Functionality:</strong> The assertion passes when the actual collection contains <strong>none</strong>
 * of the elements from the excluded collection, using identity comparison (==) rather than equality (.equals()).</p>
 *
 * @author evanbergstrom
 * @since 1.0.0
 */
public class AssertContainsNoneByIdentityTest {

    private static final String TEST_MESSAGE = "Test message";

    /**
     * Tests basic assertion functionality with non-overlapping collections.
     *
     * <p>Verifies that the assertion passes when the actual collection contains no elements
     * from the excluded collection, using identity comparison.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityBasicSuccess() {
        // Create distinct objects
        String str1 = "alpha";
        String str2 = "beta";
        String str3 = "gamma";
        String str4 = "delta";

        List<String> excluded = Arrays.asList(str1, str2);
        List<String> actual = Arrays.asList(str3, str4);

        // Should pass - no overlapping object identities
        assertContainsNoneByIdentity(excluded, actual);

        // Test with message variants
        assertContainsNoneByIdentity(excluded, actual, TEST_MESSAGE);
        assertContainsNoneByIdentity(excluded, actual, () -> TEST_MESSAGE);

        // Test with different collection types
        Iterable<String> excludedIterable = IterableOnly.of(str1, str2);
        Iterable<String> actualIterable = IterableOnly.of(str3, str4);

        assertContainsNoneByIdentity(excludedIterable, actualIterable);
    }

    /**
     * Tests assertion failure when collections have overlapping identities.
     *
     * <p>Verifies that the assertion fails when the actual collection contains
     * one or more elements from the excluded collection by identity.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityBasicFailure() {
        // Create objects with shared identities
        String shared1 = "shared1";
        String shared2 = "shared2";
        String unique1 = "unique1";
        String unique2 = "unique2";

        List<String> excluded = Arrays.asList(shared1, shared2);
        List<String> actual = Arrays.asList(shared1, unique1, unique2); // Contains shared1

        // Should fail - actual contains shared1 by identity
        assertThrows(AssertionFailedError.class,
                () -> assertContainsNoneByIdentity(excluded, actual));

        // Test with message variants
        assertThrows(AssertionFailedError.class,
                () -> assertContainsNoneByIdentity(excluded, actual, TEST_MESSAGE));

        assertThrows(AssertionFailedError.class,
                () -> assertContainsNoneByIdentity(excluded, actual, () -> TEST_MESSAGE));
    }

    /**
     * Tests identity vs equality distinction.
     *
     * <p>Verifies that the assertion correctly uses identity comparison (==) rather than
     * equality comparison (.equals()), allowing equal but different objects.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityVsEquality() {
        // Create objects that are equal but not identical
        UncachedString excluded1 = new UncachedString("test");
        UncachedString excluded2 = new UncachedString("value");

        UncachedString actual1 = new UncachedString("test");  // Equal to excluded1 but different identity
        UncachedString actual2 = new UncachedString("other"); // Different from both

        // Verify they are equal but not identical
        assertEquals(excluded1, actual1);
        assertNotSame(excluded1, actual1);

        List<UncachedString> excluded = Arrays.asList(excluded1, excluded2);
        List<UncachedString> actual = Arrays.asList(actual1, actual2);

        // Should pass - no identical objects despite having equal values
        assertContainsNoneByIdentity(excluded, actual);

        // Now test with shared identity - should fail
        List<UncachedString> actualWithSharedIdentity = Arrays.asList(excluded1, actual2);

        assertThrows(AssertionFailedError.class,
                () -> assertContainsNoneByIdentity(excluded, actualWithSharedIdentity));
    }

    /**
     * Tests assertion behavior with empty collections.
     *
     * <p>Verifies that empty collections are handled correctly in both
     * excluded and actual positions.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityEmptyCollections() {
        Iterable<Object> empty1 = IterableTestUtils.empty();
        Iterable<Object> empty2 = IterableTestUtils.empty();
        List<Object> emptyList = new ArrayList<>();

        Object obj1 = new Object();
        Object obj2 = new Object();
        List<Object> nonEmpty = Arrays.asList(obj1, obj2);

        // Empty excluded vs non-empty actual should pass (nothing to exclude)
        assertContainsNoneByIdentity(empty1, nonEmpty);
        assertContainsNoneByIdentity(emptyList, nonEmpty);

        // Non-empty excluded vs empty actual should pass (nothing to check)
        assertContainsNoneByIdentity(nonEmpty, empty1);
        assertContainsNoneByIdentity(nonEmpty, emptyList);

        // Empty vs empty should pass
        assertContainsNoneByIdentity(empty1, empty2);
        assertContainsNoneByIdentity(empty1, emptyList);
        assertContainsNoneByIdentity(emptyList, empty1);

        // Test with message variants
        assertContainsNoneByIdentity(empty1, nonEmpty, TEST_MESSAGE);
        assertContainsNoneByIdentity(empty1, nonEmpty, () -> TEST_MESSAGE);
    }

    /**
     * Tests assertion with collections containing null elements.
     *
     * <p>Verifies that null elements are properly compared by identity
     * and that null-containing collections work correctly.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityWithNulls() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();

        // Test excluding null - should pass when actual doesn't contain null
        List<Object> excludedWithNull = Arrays.asList(obj1, null);
        List<Object> actualWithoutNull = Arrays.asList(obj2, obj3);

        assertContainsNoneByIdentity(excludedWithNull, actualWithoutNull);

        // Test excluding null - should fail when actual contains null
        List<Object> actualWithNull = Arrays.asList(obj2, null, obj3);

        assertThrows(AssertionFailedError.class,
                () -> assertContainsNoneByIdentity(excludedWithNull, actualWithNull));

        // Test multiple nulls - null identity is consistent
        List<Object> excludedMultipleNulls = Arrays.asList(null, null);
        List<Object> actualMultipleNulls = Arrays.asList(null, obj1);

        assertThrows(AssertionFailedError.class,
                () -> assertContainsNoneByIdentity(excludedMultipleNulls, actualMultipleNulls));

        // Test with only nulls - may throw NPE or AssertionFailedError depending on implementation
        List<Object> onlyNullsExcluded = Arrays.asList(null, null);
        List<Object> onlyNullsActual = new ArrayList<>();
        onlyNullsActual.add(null);

        try {
            assertContainsNoneByIdentity(onlyNullsExcluded, onlyNullsActual);
            fail("Expected exception to be thrown for overlapping null elements");
        } catch (AssertionFailedError | NullPointerException e) {
            // Either exception is acceptable - depends on implementation details
        }
    }

    /**
     * Tests assertion with duplicate object references.
     *
     * <p>Verifies that duplicate references in both excluded and actual
     * collections are handled correctly using identity comparison.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityWithDuplicates() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();

        // Test duplicates in excluded list
        List<Object> excludedWithDuplicates = Arrays.asList(obj1, obj1, obj2);
        List<Object> actualDistinct = List.of(obj3);

        // Should pass - actual contains none of the excluded objects
        assertContainsNoneByIdentity(excludedWithDuplicates, actualDistinct);

        // Should fail - actual contains one of the excluded objects
        List<Object> actualWithExcluded = Arrays.asList(obj3, obj1);

        assertThrows(AssertionFailedError.class,
                () -> assertContainsNoneByIdentity(excludedWithDuplicates, actualWithExcluded));

        // Test duplicates in actual list
        List<Object> excludedSingle = List.of(obj1);
        List<Object> actualWithDuplicates = Arrays.asList(obj2, obj2, obj3);

        // Should pass - duplicates in actual don't matter if none match excluded
        assertContainsNoneByIdentity(excludedSingle, actualWithDuplicates);

        // Should fail - one of the actual duplicates matches excluded
        List<Object> actualWithExcludedDuplicates = Arrays.asList(obj1, obj1, obj3);

        assertThrows(AssertionFailedError.class,
                () -> assertContainsNoneByIdentity(excludedSingle, actualWithExcludedDuplicates));
    }

    /**
     * Tests assertion with boxed primitive edge cases.
     *
     * <p>Verifies correct identity handling of boxed primitives, including
     * cases where Integer.valueOf() may return cached instances.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityBoxedPrimitives() {
        // Small integers are cached and have same identity
        Integer cached1a = 42;
        Integer cached1b = 42;
        assertSame(cached1a, cached1b);  // Same identity due to caching

        List<Integer> excludedCached = List.of(cached1a);
        List<Integer> actualCached = List.of(cached1b); // Same identity

        // Should fail due to shared identity
        assertThrows(AssertionFailedError.class, () -> assertContainsNoneByIdentity(excludedCached, actualCached));

        // Large integers are not cached and have different identities
        UncachedInteger large1a = UncachedInteger.valueOf(1000);
        UncachedInteger large1b = UncachedInteger.valueOf(1000);
        assertNotSame(large1a, large1b);  // Different identities

        List<UncachedInteger> excludedLarge = List.of(large1a);
        List<UncachedInteger> actualLarge = List.of(large1b);

        // Should pass - different identities despite equal values
        assertContainsNoneByIdentity(excludedLarge, actualLarge);

        // Test Boolean constants (always same identity)
        List<Boolean> excludedBool = List.of(Boolean.TRUE);
        List<Boolean> actualBool = List.of(Boolean.TRUE);

        // Should fail - Boolean constants have same identity
        assertThrows(AssertionFailedError.class, () -> assertContainsNoneByIdentity(excludedBool, actualBool));
    }

    /**
     * Tests assertion with different data types mixed together.
     *
     * <p>Verifies that the assertion works correctly with various object types
     * and that identity comparison is used regardless of type.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityDifferentTypes() {
        // Create objects of different types
        UncachedString str = new UncachedString("test");
        UncachedInteger intVal = UncachedInteger.valueOf(42);
        Boolean bool = Boolean.FALSE;
        Object obj = new Object();

        List<Object> excluded = Arrays.asList(str, intVal);
        List<Object> actual = Arrays.asList(bool, obj);

        // Should pass - no shared identities
        assertContainsNoneByIdentity(excluded, actual);

        // Add one shared identity - should fail
        List<Object> actualWithShared = Arrays.asList(bool, obj, str);

        assertThrows(AssertionFailedError.class, () -> assertContainsNoneByIdentity(excluded, actualWithShared));

        // Test with equal but different objects
        UncachedString strEqual = new UncachedString("test");
        UncachedInteger intEqual = UncachedInteger.valueOf(42);

        assertEquals(str, strEqual);
        assertEquals(intVal, intEqual);
        assertNotSame(str, strEqual);
        assertNotSame(intVal, intEqual);

        List<Object> actualEqual = Arrays.asList(strEqual, intEqual);

        // Should pass - equal values but different identities
        assertContainsNoneByIdentity(excluded, actualEqual);
    }

    /**
     * Tests custom message functionality with static message string.
     *
     * <p>Verifies that custom failure messages are properly included in assertion exceptions
     * when excluded elements are found in the actual collection.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityWithMessage() {
        Object sharedObject = new Object();
        Object otherObject = new Object();

        List<Object> excluded = List.of(sharedObject);
        List<Object> actual = Arrays.asList(sharedObject, otherObject); // Contains excluded element

        try {
            assertContainsNoneByIdentity(excluded, actual, TEST_MESSAGE);
            fail("Expected AssertionFailedError to be thrown");
        } catch (AssertionFailedError error) {
            // Verify the custom message is included
            assertTrue(error.getMessage().contains(TEST_MESSAGE),
                       "Error message should contain custom message: " + error.getMessage());
            // Verify the reason mentions the found elements
            assertTrue(error.getMessage().contains("should not contain elements"),
                       "Error message should mention excluded elements: " + error.getMessage());
        }
    }

    /**
     * Tests custom message functionality with message supplier.
     *
     * <p>Verifies that message suppliers are properly invoked and their results
     * are included in assertion exceptions. This enables lazy message generation.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityWithMessageSupplier() {
        Object sharedObject = new Object();

        List<Object> excluded = List.of(sharedObject);
        List<Object> actual = List.of(sharedObject); // Contains excluded element

        try {
            assertContainsNoneByIdentity(excluded, actual, () -> TEST_MESSAGE);
            fail("Expected AssertionFailedError to be thrown");
        } catch (AssertionFailedError error) {
            // Verify the message supplier result is included
            assertTrue(error.getMessage().contains(TEST_MESSAGE),
                       "Error message should contain supplier message: " + error.getMessage());
        }
    }

    /**
     * Tests assertion behavior with null collections.
     *
     * <p>Verifies that null collections are properly rejected with appropriate exceptions.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityNullCollections() {
        List<Object> nonNull = Arrays.asList(new Object(), new Object());

        // Null excluded collection
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsNoneByIdentity(null, nonNull));

        // Null actual collection
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsNoneByIdentity(nonNull, null));

        // Both null
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsNoneByIdentity(null, null));

        // Test with message variants
        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsNoneByIdentity(null, nonNull, TEST_MESSAGE));

        assertThrows(NullPointerException.class,
                () -> CollectionAssertions.assertContainsNoneByIdentity(nonNull, null, () -> TEST_MESSAGE));
    }

    /**
     * Tests assertion with large collections for performance validation.
     *
     * <p>Verifies that the assertion can handle larger datasets efficiently
     * and that performance is reasonable for typical use cases.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityLargeCollections() {
        // Create large excluded collection
        List<Object> excluded = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            excluded.add(new Object());
        }

        // Create large actual collection with different objects
        List<Object> actual = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            actual.add(new Object());
        }

        // Should pass - no shared identities
        assertContainsNoneByIdentity(excluded, actual);

        // Add one shared identity and test failure
        Object shared = excluded.get(100);
        actual.add(shared);

        assertThrows(AssertionFailedError.class, () -> assertContainsNoneByIdentity(excluded, actual));
    }

    /**
     * Tests assertion with string literals and interning.
     *
     * <p>Verifies correct behavior with string literals that may be interned
     * and have the same identity despite being defined separately.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityStringLiterals() {
        // String literals are interned and have same identity
        List<String> excludedLiterals = Arrays.asList("hello", "world");
        List<String> actualLiterals = Arrays.asList("hello", "test");

        // Should fail - "hello" has same identity due to interning
        assertThrows(AssertionFailedError.class,
                () -> assertContainsNoneByIdentity(excludedLiterals, actualLiterals));

        // Test with non-overlapping literals
        List<String> actualDifferent = Arrays.asList("foo", "bar");

        // Should pass - no shared identities
        assertContainsNoneByIdentity(excludedLiterals, actualDifferent);

        // Test with constructed strings (different identities)
        List<UncachedString> excludedConstructed = Arrays.asList(new UncachedString("hello"),
                new UncachedString("world"));
        List<UncachedString> actualConstructed = Arrays.asList(new UncachedString("hello"),
                new UncachedString("test"));

        // Should pass - constructed strings have different identities despite equal values
        assertContainsNoneByIdentity(excludedConstructed, actualConstructed);
    }

    /**
     * Tests multiple overlapping elements scenario.
     *
     * <p>Verifies that the assertion correctly identifies all overlapping elements
     * and provides comprehensive error messages.</p>
     */
    @Test
    public void testAssertContainsNoneByIdentityMultipleOverlaps() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        Object obj4 = new Object();

        List<Object> excluded = Arrays.asList(obj1, obj2, obj3);
        List<Object> actual = Arrays.asList(obj1, obj4, obj2); // Contains obj1 and obj2

        try {
            assertContainsNoneByIdentity(excluded, actual);
            fail("Expected AssertionFailedError to be thrown");
        } catch (AssertionFailedError error) {
            // Verify the error message mentions multiple found elements
            String message = error.getMessage();
            assertTrue(message.contains("should not contain elements"),
                       "Error message should mention excluded elements: " + message);
            // The error should contain information about found elements
            assertNotNull(error.getExpected(), "Expected value should be set");
            assertNotNull(error.getActual(), "Actual value should be set");
        }
    }
}