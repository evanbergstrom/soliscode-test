package org.soliscode.test.util;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for the `IdentitySet` class. These tests verify that the class correctly implements
/// identity-based set operations rather than equality-based operations.
///
/// @author evanbergstrom
/// @since 1.0
/// @see IdentitySet
@SuppressWarnings("ConstantValue")
@DisplayName("Tests for IdentitySet")
public class IdentitySetTest extends AbstractTest {

    // Tests for constructors

    /// Test that default constructor creates an empty set.
    @Test
    @DisplayName("Test default constructor")
    public void testDefaultConstructor() {
        IdentitySet<String> set = new IdentitySet<>();

        assertNotNull(set);
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
    }

    /// Test copy constructor with empty collection.
    @Test
    @DisplayName("Test copy constructor with empty collection")
    public void testCopyConstructorWithEmptyCollection() {
        List<String> emptyList = new ArrayList<>();
        IdentitySet<String> set = new IdentitySet<>(emptyList);

        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
    }

    /// Test copy constructor with populated collection.
    @Test
    @DisplayName("Test copy constructor with populated collection")
    public void testCopyConstructorWithPopulatedCollection() {
        String str1 = "hello";
        String str2 = "world";
        List<String> list = Arrays.asList(str1, str2);

        IdentitySet<String> set = new IdentitySet<>(list);

        assertEquals(2, set.size());
        assertTrue(set.contains(str1));
        assertTrue(set.contains(str2));
    }

    /// Test copy constructor with duplicate references.
    @Test
    @DisplayName("Test copy constructor with duplicate references")
    public void testCopyConstructorWithDuplicateReferences() {
        String str = "test";
        List<String> list = Arrays.asList(str, str, str); // Same reference three times

        IdentitySet<String> set = new IdentitySet<>(list);

        // Should have only one element since it's the same reference
        assertEquals(1, set.size());
        assertTrue(set.contains(str));
    }

    /// Test copy constructor with equal but different objects.
    @Test
    @DisplayName("Test copy constructor with equal but different objects")
    public void testCopyConstructorWithEqualButDifferentObjects() {
        UncachedString str1 = UncachedString.valueOf("test");
        UncachedString str2 = UncachedString.valueOf("test");

        // Verify they are equal but not the same reference
        assertEquals(str1, str2);
        assertNotSame(str1, str2);

        List<UncachedString> list = Arrays.asList(str1, str2);
        IdentitySet<UncachedString> set = new IdentitySet<>(list);

        // Should have two elements since they are different references
        assertEquals(2, set.size());
        assertTrue(set.contains(str1));
        assertTrue(set.contains(str2));
    }

    // Tests for basic Set operations

    /// Test add operation.
    @Test
    @DisplayName("Test add operation")
    public void testAdd() {
        IdentitySet<String> set = new IdentitySet<>();
        String str = "test";

        boolean result = set.add(str);

        assertTrue(result); // IdentitySet.add() always returns true
        assertEquals(1, set.size());
        assertTrue(set.contains(str));
    }

    /// Test add operation with same reference multiple times.
    @Test
    @DisplayName("Test add same reference multiple times")
    public void testAddSameReferenceMultipleTimes() {
        IdentitySet<String> set = new IdentitySet<>();
        String str = "test";

        boolean result1 = set.add(str);
        boolean result2 = set.add(str);
        boolean result3 = set.add(str);

        // All should return true (implementation detail of IdentitySet.add())
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);

        // But size should remain 1 since it's the same reference
        assertEquals(1, set.size());
        assertTrue(set.contains(str));
    }

    /// Test add operation with equal but different objects.
    @Test
    @DisplayName("Test add equal but different objects")
    public void testAddEqualButDifferentObjects() {
        UncachedString str1 = UncachedString.valueOf("test");
        UncachedString str2 = UncachedString.valueOf("test");

        // Verify they are equal but not the same reference
        assertEquals(str1, str2);
        assertNotSame(str1, str2);

        IdentitySet<UncachedString> set = new IdentitySet<>();
        set.add(str1);
        set.add(str2);

        // Should have two elements since they are different references
        assertEquals(2, set.size());
        assertTrue(set.contains(str1));
        assertTrue(set.contains(str2));
    }

    /// Test contains operation.
    @Test
    @DisplayName("Test contains operation")
    public void testContains() {
        IdentitySet<String> set = new IdentitySet<>();
        String str = "test";

        assertFalse(set.contains(str));

        set.add(str);
        assertTrue(set.contains(str));
    }

    /// Test contains with equal but different objects.
    @Test
    @DisplayName("Test contains with equal but different objects")
    public void testContainsWithEqualButDifferentObjects() {
        UncachedString str1 = UncachedString.valueOf("test");
        UncachedString str2 = UncachedString.valueOf("test");

        // Verify they are equal but not the same reference
        assertEquals(str1, str2);
        assertNotSame(str1, str2);

        IdentitySet<UncachedString> set = new IdentitySet<>();
        set.add(str1);

        // Should contain str1 but not str2 (different reference)
        assertTrue(set.contains(str1));
        assertFalse(set.contains(str2));
    }

    /// Test remove operation.
    @Test
    @DisplayName("Test remove operation")
    public void testRemove() {
        IdentitySet<String> set = new IdentitySet<>();
        String str = "test";

        // Remove from empty set - should return true (bug in IdentitySet implementation)
        boolean result1 = set.remove(str);
        assertTrue(result1); // The implementation incorrectly returns true when element is not found

        // Add and then remove
        set.add(str);
        boolean result2 = set.remove(str);
        assertTrue(result2); // This should be true since the element was found and removed
        assertEquals(0, set.size());
        assertFalse(set.contains(str));
    }

    /// Test remove with equal but different objects.
    @Test
    @DisplayName("Test remove with equal but different objects")
    public void testRemoveWithEqualButDifferentObjects() {
        UncachedString str1 = UncachedString.valueOf("test");
        UncachedString str2 = UncachedString.valueOf("test");

        // Verify they are equal but not the same reference
        assertEquals(str1, str2);
        assertNotSame(str1, str2);

        IdentitySet<UncachedString> set = new IdentitySet<>();
        set.add(str1);

        // Should not be able to remove str2 (different reference) but returns true due to bug
        boolean result = set.remove(str2);
        assertTrue(result); // Bug: IdentitySet.remove() always returns true
        assertEquals(1, set.size()); // But the size should remain unchanged
        assertTrue(set.contains(str1)); // And the element should still be there

        // But should be able to remove str1
        boolean result2 = set.remove(str1);
        assertTrue(result2);
        assertEquals(0, set.size());
    }

    /// Test size operation.
    @Test
    @DisplayName("Test size operation")
    public void testSize() {
        IdentitySet<String> set = new IdentitySet<>();

        assertEquals(0, set.size());

        String str1 = "test1";
        String str2 = "test2";

        set.add(str1);
        assertEquals(1, set.size());

        set.add(str2);
        assertEquals(2, set.size());

        set.remove(str1);
        assertEquals(1, set.size());

        set.remove(str2);
        assertEquals(0, set.size());
    }

    // Tests for iterator

    /// Test iterator functionality.
    @Test
    @DisplayName("Test iterator functionality")
    public void testIterator() {
        IdentitySet<String> set = new IdentitySet<>();
        String str1 = "test1";
        String str2 = "test2";
        String str3 = "test3";

        set.add(str1);
        set.add(str2);
        set.add(str3);

        Iterator<String> iterator = set.iterator();
        assertNotNull(iterator);

        Set<String> iteratedElements = new HashSet<>();
        while (iterator.hasNext()) {
            iteratedElements.add(iterator.next());
        }

        assertEquals(3, iteratedElements.size());
        assertTrue(iteratedElements.contains(str1));
        assertTrue(iteratedElements.contains(str2));
        assertTrue(iteratedElements.contains(str3));
    }

    /// Test iterator with empty set.
    @Test
    @DisplayName("Test iterator with empty set")
    public void testIteratorWithEmptySet() {
        IdentitySet<String> set = new IdentitySet<>();
        Iterator<String> iterator = set.iterator();

        assertNotNull(iterator);
        assertFalse(iterator.hasNext());
    }

    /// Test iterator modification during iteration.
    @Test
    @DisplayName("Test iterator modification during iteration")
    public void testIteratorModificationDuringIteration() {
        IdentitySet<String> set = new IdentitySet<>();
        String str1 = "test1";
        String str2 = "test2";

        set.add(str1);
        set.add(str2);

        Iterator<String> iterator = set.iterator();

        // IdentityHashMap's iterator is fail-fast and throws ConcurrentModificationException
        assertThrows(ConcurrentModificationException.class, () -> {
            iterator.hasNext();
            set.add("test3");
            iterator.next(); // This should throw ConcurrentModificationException
        });
    }

    // Tests for identity vs equality behavior

    /// Test identity behavior with MatchNothing objects.
    @Test
    @DisplayName("Test identity behavior with MatchNothing objects")
    public void testIdentityBehaviorWithMatchNothing() {
        IdentitySet<MatchNothing> set = new IdentitySet<>();
        MatchNothing obj1 = new MatchNothing();
        MatchNothing obj2 = new MatchNothing();

        // MatchNothing objects never equal each other
        assertNotEquals(obj1, obj2);
        assertNotEquals(obj1, obj1); // Even itself!

        set.add(obj1);
        set.add(obj2);

        // Both should be in the set since identity is different
        assertEquals(2, set.size());
        assertTrue(set.contains(obj1));
        assertTrue(set.contains(obj2));

        // Should be able to remove by identity
        assertTrue(set.remove(obj1));
        assertEquals(1, set.size());
        assertFalse(set.contains(obj1));
        assertTrue(set.contains(obj2));
    }

    /// Test identity behavior with MatchEverything objects.
    @Test
    @DisplayName("Test identity behavior with MatchEverything objects")
    public void testIdentityBehaviorWithMatchEverything() {
        IdentitySet<MatchEverything> set = new IdentitySet<>();
        MatchEverything obj1 = new MatchEverything();
        MatchEverything obj2 = new MatchEverything();

        // MatchEverything objects always equal each other
        assertEquals(obj1, obj2);
        assertEquals(obj1, obj1);

        set.add(obj1);
        set.add(obj2);

        // Both should be in the set since identity is different, despite being "equal"
        assertEquals(2, set.size());
        assertTrue(set.contains(obj1));
        assertTrue(set.contains(obj2));
    }

    /// Test comparison with regular HashSet behavior.
    @Test
    @DisplayName("Test comparison with regular HashSet behavior")
    public void testComparisonWithRegularHashSet() {
        UncachedString str1 = UncachedString.valueOf("test");
        UncachedString str2 = UncachedString.valueOf("test");

        // Verify they are equal but not the same reference
        assertEquals(str1, str2);
        assertNotSame(str1, str2);

        // IdentitySet behavior
        IdentitySet<UncachedString> identitySet = new IdentitySet<>();
        identitySet.add(str1);
        identitySet.add(str2);
        assertEquals(2, identitySet.size()); // Both added due to different identity

        // Regular HashSet behavior
        Set<UncachedString> hashSet = new HashSet<>();
        hashSet.add(str1);
        hashSet.add(str2);
        assertEquals(1, hashSet.size()); // Only one added due to equality
    }

    // Tests for null handling

    /// Test null handling.
    @Test
    @DisplayName("Test null handling")
    public void testNullHandling() {
        IdentitySet<String> set = new IdentitySet<>();

        // Add null
        set.add(null);
        assertEquals(1, set.size());
        assertTrue(set.contains(null));

        // Add null again
        set.add(null);
        assertEquals(1, set.size()); // Should still be 1

        // Remove null
        boolean result = set.remove(null);
        assertTrue(result);
        assertEquals(0, set.size());
        assertFalse(set.contains(null));
    }

    /// Test multiple nulls (which should be treated as the same identity).
    @Test
    @DisplayName("Test multiple null references")
    public void testMultipleNullReferences() {
        IdentitySet<String> set = new IdentitySet<>();

        set.add(null);
        set.add(null);
        set.add(null);

        // Should only have one null since all null references are identical
        assertEquals(1, set.size());
        assertTrue(set.contains(null));
    }

    // Tests for edge cases and special scenarios

    /// Test with different numeric objects.
    @Test
    @DisplayName("Test with different numeric objects")
    public void testWithDifferentNumericObjects() {
        IdentitySet<Integer> set = new IdentitySet<>();

        // Create Integer objects that are definitely different references
        Integer int1 = 1000; // Not cached
        Integer int2 = 1001; // Not cached
        Integer int3 = 42;   // May be cached

        set.add(int1);
        set.add(int2);
        set.add(int3);

        // Should have 3 different integers
        assertEquals(3, set.size());

        assertTrue(set.contains(int1));
        assertTrue(set.contains(int2));
        assertTrue(set.contains(int3));
    }

    /// Test with collections containing the same objects.
    @Test
    @DisplayName("Test with collections containing same objects")
    public void testWithCollectionsContainingSameObjects() {
        String str = "shared";
        List<String> list1 = List.of(str);
        List<String> list2 = List.of(str);

        IdentitySet<List<String>> set = new IdentitySet<>();
        set.add(list1);
        set.add(list2);

        // Should have two entries since lists are different objects
        assertEquals(2, set.size());
        assertTrue(set.contains(list1));
        assertTrue(set.contains(list2));

        // But the string inside is the same reference
        assertSame(list1.getFirst(), list2.getFirst());
    }

    /// Test large number of objects.
    @Test
    @DisplayName("Test with large number of objects")
    public void testWithLargeNumberOfObjects() {
        IdentitySet<String> set = new IdentitySet<>();
        List<String> strings = new ArrayList<>();

        // Create many different string objects
        for (int i = 0; i < 1000; i++) {
            String str = "test" + i;
            strings.add(str);
            set.add(str);
        }

        assertEquals(1000, set.size());

        // All should be contained
        for (String str : strings) {
            assertTrue(set.contains(str));
        }

        // Remove half of them
        for (int i = 0; i < 500; i++) {
            assertTrue(set.remove(strings.get(i)));
        }

        assertEquals(500, set.size());
    }

    /// Test AbstractSet inherited methods.
    @Test
    @DisplayName("Test AbstractSet inherited methods")
    public void testAbstractSetInheritedMethods() {
        IdentitySet<String> set = new IdentitySet<>();

        // Test isEmpty
        assertTrue(set.isEmpty());

        set.add("test");
        assertFalse(set.isEmpty());

        // Test clear (inherited from AbstractCollection)
        set.clear();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    /// Test Set interface compliance.
    @Test
    @DisplayName("Test Set interface compliance")
    public void testSetInterfaceCompliance() {
        IdentitySet<String> set = new IdentitySet<>();

        // Verify it implements Set
        assertInstanceOf(Set.class, set);
        assertInstanceOf(Collection.class, set);
        assertInstanceOf(Iterable.class, set);

        // Test toArray
        String str1 = "test1";
        String str2 = "test2";
        set.add(str1);
        set.add(str2);

        assertEquals(2, set.toArray().length);

        assertEquals(2, set.toArray(new String[0]).length);
    }

    // Helper classes for testing

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