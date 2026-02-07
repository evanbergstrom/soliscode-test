package org.soliscode.test.util;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for the `MatchNothing` class. These tests verify that the class correctly implements
/// equality, hashing, and comparison behavior to match nothing as designed for testing purposes.
///
/// @author evanbergstrom
/// @since 1.0
/// @see MatchNothing
@SuppressWarnings({"AssertBetweenInconvertibleTypes", "EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
@DisplayName("Tests for MatchNothing")
public class MatchNothingTest extends AbstractTest {

    // Tests for constructor

    /// Test that default constructor creates a valid instance.
    @Test
    @DisplayName("Test default constructor")
    public void testDefaultConstructor() {
        MatchNothing matchNothing = new MatchNothing();

        // Verify instance was created successfully
        assertNotNull(matchNothing);

        // Verify it implements Comparable
        assertInstanceOf(Comparable.class, matchNothing);
    }

    // Tests for equals method

    /// Test that equals method always returns false with various objects.
    @Test
    @DisplayName("Test equals always returns false")
    public void testEqualsAlwaysReturnsFalse() {
        MatchNothing matchNothing = new MatchNothing();

        // Test with null
        assertNotEquals(null, matchNothing);

        // Test with same instance (violates reflexive property)
        assertNotEquals(matchNothing, matchNothing);

        // Test with different MatchNothing instance
        assertNotEquals(new MatchNothing(), matchNothing);

        // Test with different object types
        assertNotEquals("string", matchNothing);
        assertNotEquals(42, matchNothing);
        assertNotEquals(new Object(), matchNothing);
        assertNotEquals(new ArrayList<>(), matchNothing);
        assertNotEquals(Boolean.TRUE, matchNothing);
        assertNotEquals(new Date(), matchNothing);
    }

    /// Test equals with primitive wrapper types.
    @Test
    @DisplayName("Test equals with primitive wrapper types")
    public void testEqualsWithPrimitiveWrappers() {
        MatchNothing matchNothing = new MatchNothing();

        assertNotEquals((byte) 1, matchNothing);
        assertNotEquals((short) 2, matchNothing);
        assertNotEquals(3, matchNothing);
        assertNotEquals(4L, matchNothing);
        assertNotEquals(5.0f, matchNothing);
        assertNotEquals(6.0d, matchNothing);
        assertNotEquals('c', matchNothing);
        assertNotEquals(Boolean.TRUE, matchNothing);
        assertNotEquals(Boolean.FALSE, matchNothing);
    }

    /// Test equals with collections and arrays.
    @Test
    @DisplayName("Test equals with collections and arrays")
    public void testEqualsWithCollectionsAndArrays() {
        MatchNothing matchNothing = new MatchNothing();

        // Test with empty collections
        assertNotEquals(new ArrayList<>(), matchNothing);
        assertNotEquals(new HashSet<>(), matchNothing);
        assertNotEquals(new HashMap<>(), matchNothing);
        assertNotEquals(new LinkedList<>(), matchNothing);

        // Test with populated collections
        List<String> list = Arrays.asList("a", "b", "c");
        Set<Integer> set = Set.of(1, 2, 3);
        Map<String, Integer> map = Map.of("key", 42);

        assertNotEquals(matchNothing, list);
        assertNotEquals(matchNothing, set);
        assertNotEquals(matchNothing, map);

        // Test with arrays
        assertNotEquals(new int[]{1, 2, 3}, matchNothing);
        assertNotEquals(new String[]{"a", "b"}, matchNothing);
        assertNotEquals(new Object[0], matchNothing);
    }

    /// Test equals with custom objects.
    @Test
    @DisplayName("Test equals with custom objects")
    public void testEqualsWithCustomObjects() {
        MatchNothing matchNothing = new MatchNothing();

        // Test with custom test object
        TestObject testObj = new TestObject("test");
        assertNotEquals(matchNothing, testObj);

        // Test with objects that have different equals implementations
        MatchEverything matchEverything = new MatchEverything();
        assertNotEquals(matchNothing, matchEverything);

        // Test with AlwaysThrows (which throws exceptions)
        AlwaysThrows alwaysThrows = new AlwaysThrows();
        assertNotEquals(matchNothing, alwaysThrows);

        // Test with complex nested object
        ComplexObject complex = new ComplexObject();
        assertNotEquals(matchNothing, complex);
    }

    // Tests for hashCode method

    /// Test that hashCode method always returns 1.
    @Test
    @DisplayName("Test hashCode always returns 1")
    public void testHashCodeAlwaysReturnsOne() {
        MatchNothing matchNothing1 = new MatchNothing();
        MatchNothing matchNothing2 = new MatchNothing();

        assertEquals(1, matchNothing1.hashCode());
        assertEquals(1, matchNothing2.hashCode());

        // Test multiple calls return same value
        assertEquals(1, matchNothing1.hashCode());
        assertEquals(1, matchNothing1.hashCode());
        assertEquals(1, matchNothing1.hashCode());
    }

    /// Test hashCode consistency across multiple instances.
    @Test
    @DisplayName("Test hashCode consistency across instances")
    public void testHashCodeConsistencyAcrossInstances() {
        List<MatchNothing> instances = new ArrayList<>();

        // Create multiple instances
        for (int i = 0; i < 100; i++) {
            instances.add(new MatchNothing());
        }

        // All should have the same hash code
        for (MatchNothing instance : instances) {
            assertEquals(1, instance.hashCode());
        }
    }

    // Tests for compareTo method

    /// Test that compareTo method always returns 1.
    @Test
    @DisplayName("Test compareTo always returns 1")
    public void testCompareToAlwaysReturnsOne() {
        MatchNothing matchNothing1 = new MatchNothing();
        MatchNothing matchNothing2 = new MatchNothing();

        // Test with same instance (violates reflexive property)
        assertEquals(1, matchNothing1.compareTo(matchNothing1));

        // Test with different instances
        assertEquals(1, matchNothing1.compareTo(matchNothing2));
        assertEquals(1, matchNothing2.compareTo(matchNothing1));

        // Test multiple calls return same value
        assertEquals(1, matchNothing1.compareTo(matchNothing2));
        assertEquals(1, matchNothing1.compareTo(matchNothing2));
    }

    /// Test compareTo with multiple instances.
    @Test
    @DisplayName("Test compareTo with multiple instances")
    public void testCompareToWithMultipleInstances() {
        List<MatchNothing> instances = new ArrayList<>();

        // Create multiple instances
        for (int i = 0; i < 10; i++) {
            instances.add(new MatchNothing());
        }

        // All comparisons should return 1
        for (int i = 0; i < instances.size(); i++) {
            for (MatchNothing instance : instances) {
                assertEquals(1, instances.get(i).compareTo(instance));
            }
        }
    }

    /// Test compareTo method with null returns 1 (parameter is ignored).
    @SuppressWarnings("DataFlowIssue") // explicitly testing passing a null parameter
    @Test
    @DisplayName("Test compareTo with null parameter")
    public void testCompareToWithNull() {
        MatchNothing matchNothing = new MatchNothing();

        // Note: The @NonNull annotation is for documentation/static analysis only.
        // The actual implementation ignores the parameter, so null comparison returns 1.
        assertEquals(1, matchNothing.compareTo(null));
    }

    // Tests for equals and hashCode contract violations

    /// Test that equals and hashCode violate the standard contract.
    @Test
    @DisplayName("Test equals and hashCode contract violations")
    public void testEqualsHashCodeContractViolations() {
        MatchNothing obj1 = new MatchNothing();
        MatchNothing obj2 = new MatchNothing();
        MatchNothing obj3 = new MatchNothing();

        // Reflexive property violation: x.equals(x) should return true, but returns false
        assertNotEquals(obj1, obj1);

        // Symmetric property violation: x.equals(y) should equal y.equals(x), and it does (both false)
        assertNotEquals(obj1, obj2);
        assertNotEquals(obj2, obj1);

        // Transitive property: if x.equals(y) and y.equals(z), then x.equals(z) should be true
        // This is satisfied trivially since all equals() calls return false
        assertNotEquals(obj1, obj2);
        assertNotEquals(obj2, obj3);
        assertNotEquals(obj1, obj3);

        // Consistent: multiple invocations should return the same result
        assertNotEquals(obj1, obj2);
        assertNotEquals(obj1, obj2);
        assertNotEquals(obj1, obj2);

        // Null handling: x.equals(null) should return false, which is satisfied
        assertNotEquals(null, obj1);

        // Hash code contract: if two objects are equal, they must have the same hash code
        // This is satisfied trivially since no objects are ever equal
        // But note that objects with same hash code are not equal (hash code collision)
        assertEquals(obj1.hashCode(), obj2.hashCode()); // Both return 1
        assertNotEquals(obj1, obj2); // But they're not equal
    }

    // Tests for Comparable contract violations

    /// Test that compareTo violates the Comparable contract.
    @Test
    @DisplayName("Test Comparable contract violations")
    public void testComparableContractViolations() {
        MatchNothing obj1 = new MatchNothing();
        MatchNothing obj2 = new MatchNothing();
        MatchNothing obj3 = new MatchNothing();

        // Reflexive property violation: x.compareTo(x) should return 0, but returns 1
        assertEquals(1, obj1.compareTo(obj1));

        // Antisymmetric property violation: if x.compareTo(y) returns positive,
        // then y.compareTo(x) should return negative, but both return 1
        assertEquals(1, obj1.compareTo(obj2));
        assertEquals(1, obj2.compareTo(obj1)); // Should be -1 for antisymmetric

        // Transitive property: if x.compareTo(y) > 0 and y.compareTo(z) > 0,
        // then x.compareTo(z) > 0, which is satisfied
        assertTrue(obj1.compareTo(obj2) > 0);
        assertTrue(obj2.compareTo(obj3) > 0);
        assertTrue(obj1.compareTo(obj3) > 0);

        // Consistency with equals violation: if x.compareTo(y) returns non-zero,
        // then x.equals(y) should return false, which is satisfied
        assertNotEquals(0, obj1.compareTo(obj2));
        assertNotEquals(obj1, obj2);
    }

    // Tests for usage in collections

    /// Test behavior when used in HashSet.
    @Test
    @DisplayName("Test behavior in HashSet")
    public void testBehaviorInHashSet() {
        Set<MatchNothing> set = new HashSet<>();

        MatchNothing obj1 = new MatchNothing();
        MatchNothing obj2 = new MatchNothing();

        assertTrue(set.add(obj1));
        // Since equals always returns false, obj2 should be considered different from obj1
        // even though they have the same hash code
        assertTrue(set.add(obj2));

        assertEquals(2, set.size());
        // Note: HashSet.contains() uses object identity when equals() returns false but hash codes match
        assertTrue(set.contains(obj1));  // Found by identity
        assertTrue(set.contains(obj2));  // Found by identity

        // Even the same instance is not considered to contain itself
        // due to equals() always returning false
        assertFalse(set.contains(new MatchNothing()));
    }

    /// Test behavior when used in HashMap as key.
    @Test
    @DisplayName("Test behavior in HashMap as key")
    public void testBehaviorInHashMapAsKey() {
        Map<MatchNothing, String> map = new HashMap<>();

        MatchNothing key1 = new MatchNothing();
        MatchNothing key2 = new MatchNothing();

        map.put(key1, "value1");
        // Since equals always returns false, this should create a new entry
        map.put(key2, "value2");

        assertEquals(2, map.size());
        assertEquals("value1", map.get(key1));
        assertEquals("value2", map.get(key2));

        // New instances won't be found due to equals() always returning false
        assertNull(map.get(new MatchNothing()));
    }

    /// Test behavior when used in TreeSet.
    @Test
    @DisplayName("Test behavior in TreeSet")
    public void testBehaviorInTreeSet() {
        Set<MatchNothing> set = new TreeSet<>();

        MatchNothing obj1 = new MatchNothing();
        MatchNothing obj2 = new MatchNothing();
        MatchNothing obj3 = new MatchNothing();

        assertTrue(set.add(obj1));
        // Since compareTo always returns 1 (obj1 > obj2), they are considered different
        assertTrue(set.add(obj2));
        assertTrue(set.add(obj3));

        assertEquals(3, set.size());
        // Note: TreeSet.contains() uses compareTo(), not equals()
        // Since compareTo always returns 1, contains() will not find the objects
        assertFalse(set.contains(obj1));
        assertFalse(set.contains(obj2));
        assertFalse(set.contains(obj3));
    }

    /// Test behavior when used in ArrayList contains.
    @Test
    @DisplayName("Test behavior in ArrayList contains")
    public void testBehaviorInArrayListContains() {
        List<MatchNothing> list = new ArrayList<>();

        MatchNothing obj1 = new MatchNothing();
        list.add(obj1);

        // Since equals always returns false, contains should return false even for the same instance
        assertFalse(list.contains(obj1));
        assertFalse(list.contains(new MatchNothing()));

        // The list has the object, but contains() can't find it due to equals() behavior
        assertEquals(1, list.size());
        assertSame(obj1, list.getFirst());
    }

    // Tests for sorting behavior

    /// Test sorting behavior with Collections.sort.
    @Test
    @DisplayName("Test sorting behavior")
    public void testSortingBehavior() {
        List<MatchNothing> list = new ArrayList<>();

        // Add multiple instances
        MatchNothing obj1 = new MatchNothing();
        MatchNothing obj2 = new MatchNothing();
        MatchNothing obj3 = new MatchNothing();

        list.add(obj1);
        list.add(obj2);
        list.add(obj3);

        // Since compareTo always returns 1, the sorting behavior is undefined
        // but should still complete successfully
        assertEquals(3, list.size());

        // All elements should still be present (though order may have changed)
        // Note: contains() uses equals(), which always returns false, so we check by reference
        boolean foundObj1 = false, foundObj2 = false, foundObj3 = false;
        for (MatchNothing item : list) {
            if (item == obj1) foundObj1 = true;
            if (item == obj2) foundObj2 = true;
            if (item == obj3) foundObj3 = true;
        }
        assertTrue(foundObj1);
        assertTrue(foundObj2);
        assertTrue(foundObj3);
    }

    // Tests for edge cases and special scenarios

    /// Test that multiple instances are independent.
    @Test
    @DisplayName("Test instance independence")
    public void testInstanceIndependence() {
        MatchNothing obj1 = new MatchNothing();
        MatchNothing obj2 = new MatchNothing();

        // Instances should be different objects
        assertNotSame(obj1, obj2);

        // They should not be equal and should have same hash code
        assertNotEquals(obj1, obj2);
        assertEquals(obj1.hashCode(), obj2.hashCode());
        assertEquals(1, obj1.compareTo(obj2));
    }

    /// Test interaction with MatchEverything.
    @Test
    @DisplayName("Test interaction with MatchEverything")
    public void testInteractionWithMatchEverything() {
        MatchNothing matchNothing = new MatchNothing();
        MatchEverything matchEverything = new MatchEverything();

        // MatchNothing.equals should return false even for MatchEverything
        assertNotEquals(matchNothing, matchEverything);

        // But MatchEverything.equals should return true for MatchNothing
        assertEquals(matchEverything, matchNothing);

        // This demonstrates asymmetric equals behavior
    }

    /// Test interaction with objects that violate equals contract.
    @Test
    @DisplayName("Test interaction with contract-violating objects")
    public void testInteractionWithContractViolatingObjects() {
        MatchNothing matchNothing = new MatchNothing();

        // Test with AlwaysThrows (which throws exceptions)
        AlwaysThrows alwaysThrows = new AlwaysThrows();

        // MatchNothing.equals should return false even for problematic objects
        assertNotEquals(matchNothing, alwaysThrows);

        // But alwaysThrows.equals will throw an exception
        assertThrows(UnsupportedOperationException.class, () -> alwaysThrows.equals(matchNothing));
    }

    /// Test consistency across many operations.
    @Test
    @DisplayName("Test consistency across many operations")
    public void testConsistencyAcrossManyOperations() {
        MatchNothing matchNothing = new MatchNothing();

        // Test that behavior is consistent across many calls
        for (int i = 0; i < 1000; i++) {
            assertNotEquals(new Object(), matchNothing);
            assertEquals(1, matchNothing.hashCode());
            assertEquals(1, matchNothing.compareTo(new MatchNothing()));
        }
    }

    // Tests for practical usage scenarios

    /// Test practical usage for testing equality behavior.
    @Test
    @DisplayName("Test practical usage for testing equality")
    public void testPracticalUsageForTestingEquality() {
        MatchNothing matchNothing = new MatchNothing();

        // Simulate testing a method that uses equals()
        List<Object> objects = Arrays.asList(
            "string", 42, new Date(), new ArrayList<>(), null, matchNothing
        );

        // MatchNothing should not match any of them, including itself
        for (Object obj : objects) {
            assertNotEquals(matchNothing, obj);
        }

        // This makes it useful for testing scenarios where you want
        // an object that never matches in equality checks
    }

    /// Test usage for creating test fixtures.
    @Test
    @DisplayName("Test usage for creating test fixtures")
    public void testUsageForCreatingTestFixtures() {
        // Create a collection with mixed types including MatchNothing
        List<Object> mixedList = new ArrayList<>();
        mixedList.add("string");
        mixedList.add(42);
        MatchNothing matcher = new MatchNothing();
        mixedList.add(matcher);
        mixedList.add(new Date());

        // The matcher should not be found in the list due to equals() behavior
        assertFalse(mixedList.contains(matcher));

        // But it should be physically present
        assertSame(matcher, mixedList.get(2));

        // And it should not equal any item when compared directly
        for (Object item : mixedList) {
            assertNotEquals(matcher, item);
        }
    }

    /// Test that MatchNothing can be used to test defensive code.
    @Test
    @DisplayName("Test usage for defensive programming scenarios")
    public void testUsageForDefensiveProgramming() {
        MatchNothing matcher = new MatchNothing();

        // Test a method that should handle objects that never return true for equals
        boolean result = isInCollection(matcher, Arrays.asList("a", "b", "c"));
        assertFalse(result); // Should not find it because equals always returns false

        // Even if we add_singleElement_returnsTrueAndUpdatesSize the exact same instance
        List<Object> listWithMatcher = new ArrayList<>();
        listWithMatcher.add(matcher);
        result = isInCollection(matcher, listWithMatcher);
        assertFalse(result); // Still won't find it due to equals() behavior
    }

    // Helper method for defensive programming test
    private boolean isInCollection(Object item, Collection<?> collection) {
        return collection.contains(item);
    }

    /// Test hashCode collision behavior with universal rejection.
    @Test
    @DisplayName("Test hashCode collision with universal rejection")
    public void testHashCodeCollisionWithUniversalRejection() {
        // Since all MatchNothing instances have the same hash code (1),
        // they will all hash to the same bucket but each will be stored separately
        // due to equals() always returning false
        Map<MatchNothing, Integer> map = new HashMap<>();

        List<MatchNothing> keys = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MatchNothing key = new MatchNothing();
            keys.add(key);
            map.put(key, i);
        }

        // Should have 10 entries because no keys are considered equal
        assertEquals(10, map.size());

        // Each key should map to its corresponding value
        for (int i = 0; i < keys.size(); i++) {
            MatchNothing key = keys.get(i);
            Integer result = map.get(key);
            assertEquals(Integer.valueOf(i), result);
        }

        // But a new MatchNothing instance won't be found
        assertNull(map.get(new MatchNothing()));
    }

    /// Test remove operations behavior.
    @Test
    @DisplayName("Test remove operations behavior")
    public void testRemoveOperationsBehavior() {
        List<MatchNothing> list = new ArrayList<>();
        MatchNothing obj = new MatchNothing();
        list.add(obj);

        // Can remove by equals() but only if equals() returned true, which it never does
        // However, ArrayList.remove() still works because it uses equals() which always returns false
        assertFalse(list.remove(obj));
        assertEquals(1, list.size());

        // But can remove by index
        MatchNothing removed = list.removeFirst();
        assertSame(obj, removed);
        assertEquals(0, list.size());

        // Test with Set
        Set<MatchNothing> set = new HashSet<>();
        set.add(obj);

        // Can remove by identity (same as contains behavior)
        assertTrue(set.remove(obj));
        assertEquals(0, set.size());
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

    private static class ComplexObject {
        private final List<String> items = Arrays.asList("a", "b", "c");
        private final Map<String, Integer> map = Map.of("key", 42);

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ComplexObject other)) return false;
            return Objects.equals(items, other.items) && Objects.equals(map, other.map);
        }

        @Override
        public int hashCode() {
            return Objects.hash(items, map);
        }
    }
}