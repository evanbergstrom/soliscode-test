package org.soliscode.test.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for the `MatchEverything` class. These tests verify that the class correctly implements
/// equality, hashing, and comparison behavior to match everything as designed for testing purposes.
///
/// @author evanbergstrom
/// @since 1.0
/// @see MatchEverything
@DisplayName("Tests for MatchEverything")
public class MatchEverythingTest extends AbstractTest {

    // Tests for constructor

    /// Test that default constructor creates a valid instance.
    @Test
    @DisplayName("Test default constructor")
    public void testDefaultConstructor() {
        MatchEverything matchEverything = new MatchEverything();
        
        // Verify instance was created successfully
        assertNotNull(matchEverything);
        
        // Verify it implements Comparable
        assertInstanceOf(Comparable.class, matchEverything);
    }

    // Tests for equals method

    /// Test that equals method always returns true with various objects.
    @Test
    @DisplayName("Test equals always returns true")
    public void testEqualsAlwaysReturnsTrue() {
        MatchEverything matchEverything = new MatchEverything();
        
        // Test with null
        assertTrue(matchEverything.equals(null));
        
        // Test with same instance
        assertTrue(matchEverything.equals(matchEverything));
        
        // Test with different MatchEverything instance
        assertTrue(matchEverything.equals(new MatchEverything()));
        
        // Test with different object types
        assertTrue(matchEverything.equals("string"));
        assertTrue(matchEverything.equals(Integer.valueOf(42)));
        assertTrue(matchEverything.equals(new Object()));
        assertTrue(matchEverything.equals(new ArrayList<>()));
        assertTrue(matchEverything.equals(Boolean.TRUE));
        assertTrue(matchEverything.equals(new Date()));
    }

    /// Test equals with primitive wrapper types.
    @Test
    @DisplayName("Test equals with primitive wrapper types")
    public void testEqualsWithPrimitiveWrappers() {
        MatchEverything matchEverything = new MatchEverything();
        
        assertTrue(matchEverything.equals(Byte.valueOf((byte) 1)));
        assertTrue(matchEverything.equals(Short.valueOf((short) 2)));
        assertTrue(matchEverything.equals(Integer.valueOf(3)));
        assertTrue(matchEverything.equals(Long.valueOf(4L)));
        assertTrue(matchEverything.equals(Float.valueOf(5.0f)));
        assertTrue(matchEverything.equals(Double.valueOf(6.0)));
        assertTrue(matchEverything.equals(Character.valueOf('c')));
        assertTrue(matchEverything.equals(Boolean.valueOf(true)));
        assertTrue(matchEverything.equals(Boolean.valueOf(false)));
    }

    /// Test equals with collections and arrays.
    @Test
    @DisplayName("Test equals with collections and arrays")
    public void testEqualsWithCollectionsAndArrays() {
        MatchEverything matchEverything = new MatchEverything();
        
        // Test with empty collections
        assertTrue(matchEverything.equals(new ArrayList<>()));
        assertTrue(matchEverything.equals(new HashSet<>()));
        assertTrue(matchEverything.equals(new HashMap<>()));
        assertTrue(matchEverything.equals(new LinkedList<>()));
        
        // Test with populated collections
        List<String> list = Arrays.asList("a", "b", "c");
        Set<Integer> set = Set.of(1, 2, 3);
        Map<String, Integer> map = Map.of("key", 42);
        
        assertTrue(matchEverything.equals(list));
        assertTrue(matchEverything.equals(set));
        assertTrue(matchEverything.equals(map));
        
        // Test with arrays
        assertTrue(matchEverything.equals(new int[]{1, 2, 3}));
        assertTrue(matchEverything.equals(new String[]{"a", "b"}));
        assertTrue(matchEverything.equals(new Object[0]));
    }

    /// Test equals with custom objects.
    @Test
    @DisplayName("Test equals with custom objects")
    public void testEqualsWithCustomObjects() {
        MatchEverything matchEverything = new MatchEverything();
        
        // Test with custom test object
        TestObject testObj = new TestObject("test");
        assertTrue(matchEverything.equals(testObj));
        
        // Test with objects that have different equals implementations
        AlwaysThrows alwaysThrows = new AlwaysThrows();
        assertTrue(matchEverything.equals(alwaysThrows));
        
        // Test with complex nested object
        ComplexObject complex = new ComplexObject();
        assertTrue(matchEverything.equals(complex));
    }

    // Tests for hashCode method

    /// Test that hashCode method always returns 1.
    @Test
    @DisplayName("Test hashCode always returns 1")
    public void testHashCodeAlwaysReturnsOne() {
        MatchEverything matchEverything1 = new MatchEverything();
        MatchEverything matchEverything2 = new MatchEverything();
        
        assertEquals(1, matchEverything1.hashCode());
        assertEquals(1, matchEverything2.hashCode());
        
        // Test multiple calls return same value
        assertEquals(1, matchEverything1.hashCode());
        assertEquals(1, matchEverything1.hashCode());
        assertEquals(1, matchEverything1.hashCode());
    }

    /// Test hashCode consistency across multiple instances.
    @Test
    @DisplayName("Test hashCode consistency across instances")
    public void testHashCodeConsistencyAcrossInstances() {
        List<MatchEverything> instances = new ArrayList<>();
        
        // Create multiple instances
        for (int i = 0; i < 100; i++) {
            instances.add(new MatchEverything());
        }
        
        // All should have the same hash code
        for (MatchEverything instance : instances) {
            assertEquals(1, instance.hashCode());
        }
    }

    // Tests for compareTo method

    /// Test that compareTo method always returns 0.
    @Test
    @DisplayName("Test compareTo always returns 0")
    public void testCompareToAlwaysReturnsZero() {
        MatchEverything matchEverything1 = new MatchEverything();
        MatchEverything matchEverything2 = new MatchEverything();
        
        // Test with same instance
        assertEquals(0, matchEverything1.compareTo(matchEverything1));
        
        // Test with different instances
        assertEquals(0, matchEverything1.compareTo(matchEverything2));
        assertEquals(0, matchEverything2.compareTo(matchEverything1));
        
        // Test multiple calls return same value
        assertEquals(0, matchEverything1.compareTo(matchEverything2));
        assertEquals(0, matchEverything1.compareTo(matchEverything2));
    }

    /// Test compareTo with multiple instances.
    @Test
    @DisplayName("Test compareTo with multiple instances")
    public void testCompareToWithMultipleInstances() {
        List<MatchEverything> instances = new ArrayList<>();
        
        // Create multiple instances
        for (int i = 0; i < 10; i++) {
            instances.add(new MatchEverything());
        }
        
        // All comparisons should return 0
        for (int i = 0; i < instances.size(); i++) {
            for (int j = 0; j < instances.size(); j++) {
                assertEquals(0, instances.get(i).compareTo(instances.get(j)));
            }
        }
    }

    /// Test compareTo method with null returns 0 (parameter is ignored).
    @SuppressWarnings("DataFlowIssue") // explicitly testing passing a null parameter
    @Test
    @DisplayName("Test compareTo with null parameter")
    public void testCompareToWithNull() {
        MatchEverything matchEverything = new MatchEverything();
        
        // Note: The @NonNull annotation is for documentation/static analysis only.
        // The actual implementation ignores the parameter, so null comparison returns 0.
        assertEquals(0, matchEverything.compareTo(null));
    }

    // Tests for equals and hashCode contract

    /// Test that equals and hashCode follow the contract.
    @Test
    @DisplayName("Test equals and hashCode contract")
    public void testEqualsHashCodeContract() {
        MatchEverything obj1 = new MatchEverything();
        MatchEverything obj2 = new MatchEverything();
        MatchEverything obj3 = new MatchEverything();
        
        // Reflexive: x.equals(x) should return true
        assertTrue(obj1.equals(obj1));
        
        // Symmetric: x.equals(y) should return true if and only if y.equals(x) returns true
        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));
        
        // Transitive: if x.equals(y) returns true and y.equals(z) returns true, then x.equals(z) should return true
        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj3));
        assertTrue(obj1.equals(obj3));
        
        // Consistent: multiple invocations should return the same result
        assertTrue(obj1.equals(obj2));
        assertTrue(obj1.equals(obj2));
        assertTrue(obj1.equals(obj2));
        
        // Null handling: x.equals(null) should return false for non-null x
        // NOTE: MatchEverything violates this contract by design - it returns true for null
        assertTrue(obj1.equals(null)); // This is the intended behavior
        
        // Hash code contract: if two objects are equal, they must have the same hash code
        assertEquals(obj1.hashCode(), obj2.hashCode());
        assertEquals(obj2.hashCode(), obj3.hashCode());
    }

    // Tests for Comparable contract

    /// Test that compareTo follows the Comparable contract.
    @Test
    @DisplayName("Test Comparable contract")
    public void testComparableContract() {
        MatchEverything obj1 = new MatchEverything();
        MatchEverything obj2 = new MatchEverything();
        MatchEverything obj3 = new MatchEverything();
        
        // Reflexive: x.compareTo(x) should return 0
        assertEquals(0, obj1.compareTo(obj1));
        
        // Antisymmetric: if x.compareTo(y) returns 0, then y.compareTo(x) should return 0
        assertEquals(0, obj1.compareTo(obj2));
        assertEquals(0, obj2.compareTo(obj1));
        
        // Transitive: if x.compareTo(y) returns 0 and y.compareTo(z) returns 0, then x.compareTo(z) should return 0
        assertEquals(0, obj1.compareTo(obj2));
        assertEquals(0, obj2.compareTo(obj3));
        assertEquals(0, obj1.compareTo(obj3));
        
        // Consistency with equals: if x.compareTo(y) returns 0, then x.equals(y) should return true
        assertEquals(0, obj1.compareTo(obj2));
        assertTrue(obj1.equals(obj2));
    }

    // Tests for usage in collections

    /// Test behavior when used in HashSet.
    @Test
    @DisplayName("Test behavior in HashSet")
    public void testBehaviorInHashSet() {
        Set<MatchEverything> set = new HashSet<>();
        
        MatchEverything obj1 = new MatchEverything();
        MatchEverything obj2 = new MatchEverything();
        
        assertTrue(set.add(obj1));
        // Since equals always returns true, obj2 should be considered equal to obj1
        // and add should return false (element already exists)
        assertFalse(set.add(obj2));
        
        assertEquals(1, set.size());
        assertTrue(set.contains(obj1));
        assertTrue(set.contains(obj2));
        assertTrue(set.contains(new MatchEverything()));
    }

    /// Test behavior when used in HashMap as key.
    @Test
    @DisplayName("Test behavior in HashMap as key")
    public void testBehaviorInHashMapAsKey() {
        Map<MatchEverything, String> map = new HashMap<>();
        
        MatchEverything key1 = new MatchEverything();
        MatchEverything key2 = new MatchEverything();
        
        map.put(key1, "value1");
        // Since equals always returns true, this should replace the existing value
        map.put(key2, "value2");
        
        assertEquals(1, map.size());
        assertEquals("value2", map.get(key1));
        assertEquals("value2", map.get(key2));
        assertEquals("value2", map.get(new MatchEverything()));
    }

    /// Test behavior when used in TreeSet.
    @Test
    @DisplayName("Test behavior in TreeSet")
    public void testBehaviorInTreeSet() {
        Set<MatchEverything> set = new TreeSet<>();
        
        MatchEverything obj1 = new MatchEverything();
        MatchEverything obj2 = new MatchEverything();
        MatchEverything obj3 = new MatchEverything();
        
        assertTrue(set.add(obj1));
        // Since compareTo always returns 0, objects are considered equal
        assertFalse(set.add(obj2));
        assertFalse(set.add(obj3));
        
        assertEquals(1, set.size());
        assertTrue(set.contains(obj1));
        assertTrue(set.contains(obj2));
        assertTrue(set.contains(new MatchEverything()));
    }

    /// Test behavior when used in ArrayList contains.
    @Test
    @DisplayName("Test behavior in ArrayList contains")
    public void testBehaviorInArrayListContains() {
        List<MatchEverything> list = new ArrayList<>();
        
        MatchEverything obj1 = new MatchEverything();
        list.add(obj1);
        
        // Since equals always returns true, contains should return true for any MatchEverything
        assertTrue(list.contains(new MatchEverything()));
        assertTrue(list.contains(obj1));
        
        // Even contains with different types should return true (violating normal equals contract)
        // Note: This might not work as expected due to type checking in ArrayList.contains()
        // but let's test the equals method directly
        MatchEverything testObj = new MatchEverything();
        assertTrue(testObj.equals("string"));
        assertTrue(testObj.equals(42));
    }

    // Tests for sorting behavior

    /// Test sorting behavior with Collections.sort.
    @Test
    @DisplayName("Test sorting behavior")
    public void testSortingBehavior() {
        List<MatchEverything> list = new ArrayList<>();
        
        // Add multiple instances
        for (int i = 0; i < 10; i++) {
            list.add(new MatchEverything());
        }
        
        // Sorting should complete without errors since compareTo always returns 0
        assertDoesNotThrow(() -> Collections.sort(list));
        
        // All elements should still be considered equal
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                assertEquals(0, list.get(i).compareTo(list.get(j)));
                assertTrue(list.get(i).equals(list.get(j)));
            }
        }
    }

    // Tests for edge cases and special scenarios

    /// Test that multiple instances are independent.
    @Test
    @DisplayName("Test instance independence")
    public void testInstanceIndependence() {
        MatchEverything obj1 = new MatchEverything();
        MatchEverything obj2 = new MatchEverything();
        
        // Instances should be different objects
        assertNotSame(obj1, obj2);
        
        // But they should be equal and have same hash code
        assertTrue(obj1.equals(obj2));
        assertEquals(obj1.hashCode(), obj2.hashCode());
        assertEquals(0, obj1.compareTo(obj2));
    }

    /// Test interaction with objects that violate equals contract.
    @Test
    @DisplayName("Test interaction with contract-violating objects")
    public void testInteractionWithContractViolatingObjects() {
        MatchEverything matchEverything = new MatchEverything();
        
        // Test with AlwaysThrows (which throws exceptions)
        AlwaysThrows alwaysThrows = new AlwaysThrows();
        
        // MatchEverything.equals should return true even for problematic objects
        assertTrue(matchEverything.equals(alwaysThrows));
        
        // But alwaysThrows.equals will throw an exception
        assertThrows(UnsupportedOperationException.class, () -> {
            alwaysThrows.equals(matchEverything);
        });
    }

    /// Test consistency across many operations.
    @Test
    @DisplayName("Test consistency across many operations")
    public void testConsistencyAcrossManyOperations() {
        MatchEverything matchEverything = new MatchEverything();
        
        // Test that behavior is consistent across many calls
        for (int i = 0; i < 1000; i++) {
            assertTrue(matchEverything.equals(new Object()));
            assertEquals(1, matchEverything.hashCode());
            assertEquals(0, matchEverything.compareTo(new MatchEverything()));
        }
    }

    // Tests for practical usage scenarios

    /// Test practical usage for testing equality behavior.
    @Test
    @DisplayName("Test practical usage for testing equality")
    public void testPracticalUsageForTestingEquality() {
        MatchEverything matchEverything = new MatchEverything();
        
        // Simulate testing a method that uses equals()
        List<Object> objects = Arrays.asList(
            "string", 42, new Date(), new ArrayList<>(), null
        );
        
        // MatchEverything should match all of them
        for (Object obj : objects) {
            assertTrue(matchEverything.equals(obj));
        }
        
        // This makes it useful for testing scenarios where you want
        // an object that always matches in equality checks
    }

    /// Test usage for creating test fixtures.
    @Test
    @DisplayName("Test usage for creating test fixtures")
    public void testUsageForCreatingTestFixtures() {
        // Create a collection with mixed types including MatchEverything
        List<Object> mixedList = new ArrayList<>();
        mixedList.add("string");
        mixedList.add(42);
        mixedList.add(new MatchEverything());
        mixedList.add(new Date());
        
        MatchEverything matcher = new MatchEverything();
        
        // The matcher should be found in the list regardless of position
        assertTrue(mixedList.contains(matcher));
        
        // And it should equal every item when compared directly
        for (Object item : mixedList) {
            assertTrue(matcher.equals(item));
        }
    }

    /// Test that MatchEverything can be used to test defensive code.
    @Test
    @DisplayName("Test usage for defensive programming scenarios")
    public void testUsageForDefensiveProgramming() {
        MatchEverything matcher = new MatchEverything();
        
        // Test a method that should handle objects that always return true for equals
        boolean result = isInCollection(matcher, Arrays.asList("a", "b", "c"));
        assertTrue(result); // Should find it because equals always returns true
        
        result = isInCollection(matcher, Collections.emptyList());
        assertFalse(result); // Empty list should still return false
    }
    
    // Helper method for defensive programming test
    private boolean isInCollection(Object item, Collection<?> collection) {
        return collection.contains(item);
    }

    /// Test hashCode collision behavior.
    @Test
    @DisplayName("Test hashCode collision behavior")
    public void testHashCodeCollisionBehavior() {
        // Since all MatchEverything instances have the same hash code (1),
        // they will all hash to the same bucket in hash-based collections
        Map<MatchEverything, Integer> map = new HashMap<>();
        
        for (int i = 0; i < 100; i++) {
            MatchEverything key = new MatchEverything();
            map.put(key, i);
        }
        
        // Should only have one entry because all keys are considered equal
        assertEquals(1, map.size());
        
        // The value should be the last one inserted (99)
        assertEquals(Integer.valueOf(99), map.get(new MatchEverything()));
    }

    // Helper classes for testing

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
            return Objects.equals(value, other.value);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
        
        @Override
        public String toString() {
            return "TestObject{" + value + "}";
        }
    }
    
    private static class ComplexObject {
        private final List<String> items = Arrays.asList("a", "b", "c");
        private final Map<String, Integer> map = Map.of("key", 42);
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ComplexObject)) return false;
            ComplexObject other = (ComplexObject) obj;
            return Objects.equals(items, other.items) && Objects.equals(map, other.map);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(items, map);
        }
    }
}