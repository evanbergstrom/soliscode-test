/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.methods.MapMethods;

import static com.soliscode.test.breakable.BreakableMap.GET_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableMap.SIZE_ALWAYS_RETURNS_ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [BreakableMap]'s implementations of [Object]'s methods `equals`, `hashCode` and `toString`, and of its
/// `clone`: that equality and the string form follow the map's contents, that the hash code takes in its null
/// policies, and that a clone copies the contents, breaks, null policies and method statuses without sharing them.
///
/// The map's other methods, its constructors, builder and views are tested in [BreakableMapTest], and the
/// [java.util.Map] interface's default methods in [BreakableMapDefaultMethodsTest].
///
/// @see BreakableMap
public class BreakableMapObjectMethodsTest extends AbstractTest {

    /// Default constructor
    public BreakableMapObjectMethodsTest() {
        super();
    }

    // ========== Object Method Tests ==========

    /// Tests the equals method implementation for BreakableMap.
    ///
    /// Validates that BreakableMap correctly implements equals() by comparing the underlying
    /// map content rather than the break configuration. This ensures that maps with the same
    /// data but different breaks are considered equal, maintaining Map interface contract compliance.
    ///
    /// **Test Scenarios:**
    /// 1. **Empty Maps**: All empty maps should be equal regardless of configuration
    /// 2. **Same Content**: Maps with identical data should be equal
    /// 3. **Different Content**: Maps with different data should not be equal
    /// 4. **Cross-Type Equality**: BreakableMap should equal regular HashMap with same data
    /// 5. **Non-Map Objects**: Should not equal non-map objects
    ///
    /// **Equality Behavior:**
    /// - Comparison based on underlying map content only
    /// - Break configuration does not affect equality
    /// - Follows standard Map.equals() contract
    ///
    /// **Assertions:**
    /// - Empty equality: `assertEquals(map1, map2)` and `assertEquals(map1, regularMap)`
    /// - Content equality: `assertEquals(map1, map2)` after adding same data
    /// - Content inequality: `assertNotEquals(map1, map2)` after adding different data
    /// - Type safety: `assertNotEquals(map1, "Hello")` for non-map objects
    @Test
    @DisplayName("Test equals method")
    public void testEquals() {
        BreakableMap<String, Integer> map1 = new BreakableMap<>();
        BreakableMap<String, Integer> map2 = new BreakableMap<>();
        HashMap<String, Integer> regularMap = new HashMap<>();

        // Empty maps should be equal
        assertEquals(map1, map2);
        assertEquals(map1, regularMap);

        // Add same data
        map1.put("key", 42);
        map2.put("key", 42);
        regularMap.put("key", 42);

        assertEquals(map1, map2);
        assertEquals(map1, regularMap);

        // Different data should not be equal
        map2.put("key2", 84);
        assertNotEquals(map1, map2);
        assertEquals(map1, regularMap); // regularMap still equals map1 (both have only "key")

        // non-maps should not be equal
        //noinspection AssertBetweenInconvertibleTypes,MisorderedAssertEqualsArguments
        assertNotEquals(map1, "Hello");
    }

    /// Tests the hashCode method implementation for BreakableMap.
    ///
    /// Validates that BreakableMap correctly implements hashCode() by incorporating both the
    /// underlying map content and the break/policy configuration. This ensures that maps with
    /// the same data but different configurations have different hash codes when appropriate.
    ///
    /// **Test Scenarios:**
    /// 1. **Same Configuration**: Maps with identical data and configuration should have same hash code
    /// 2. **Different Configuration**: Maps with same data but different null policies should have different hash codes
    ///
    /// **HashCode Behavior:**
    /// - Incorporates underlying map content
    /// - Includes null policy configuration
    /// - Maintains consistency with equals() method
    ///
    /// **Assertions:**
    /// - Consistency: `assertEquals(map1.hashCode(), map2.hashCode())` for same configuration
    /// - Distinction: `assertNotEquals(map1.hashCode(), map3.hashCode())` for different configuration
    @Test
    @DisplayName("Test hashCode method")
    public void testHashCode() {
        BreakableMap<String, Integer> map1 = new BreakableMap.Builder<String, Integer>()
            .doesNotPermitNullKeys()
            .build();
        BreakableMap<String, Integer> map2 = new BreakableMap.Builder<String, Integer>()
            .doesNotPermitNullKeys()
            .build();
        BreakableMap<String, Integer> map3 = new BreakableMap<>(); // Different null policy

        map1.put("key", 42);
        map2.put("key", 42);
        map3.put("key", 43);

        // Maps with same data and configuration should have same hash code
        assertEquals(map1.hashCode(), map2.hashCode());

        // Maps with different configuration should likely have different hash codes
        assertNotEquals(map1.hashCode(), map3.hashCode());
    }

    /// Tests the toString method implementation for BreakableMap.
    ///
    /// Validates that BreakableMap provides a meaningful string representation that shows
    /// the underlying map content in a standard format, making it useful for debugging
    /// and logging purposes.
    ///
    /// **Test Scenarios:**
    /// 1. **Empty Map**: Should return standard empty map representation
    /// 2. **Non-Empty Map**: Should include key-value pairs in the output
    ///
    /// **ToString Behavior:**
    /// - Delegates to underlying map's toString() implementation
    /// - Provides standard Map string representation
    /// - Break configuration not included in output
    ///
    /// **Assertions:**
    /// - Empty representation: `assertEquals("{}", map.toString())`
    /// - Content inclusion: `assertTrue(map.toString().contains("key"))` and
    ///   `assertTrue(map.toString().contains("42"))`
    @Test
    @DisplayName("Test toString method")
    public void testToString() {
        BreakableMap<String, Integer> map = new BreakableMap<>();
        assertEquals("{}", map.toString());

        map.put("key", 42);
        assertTrue(map.toString().contains("key"));
        assertTrue(map.toString().contains("42"));
    }

    /// Tests the clone method implementation for BreakableMap.
    ///
    /// Validates that BreakableMap correctly implements clone() by creating a deep copy of
    /// the underlying map data while preserving all break configuration and null policies.
    /// This ensures that the cloned map is functionally identical but completely independent.
    ///
    /// **Test Scenario:**
    /// - Creates a BreakableMap with breaks, null policies, and data
    /// - Clones the map and verifies complete configuration transfer
    /// - Tests independence by modifying original after cloning
    ///
    /// **Clone Behavior:**
    /// - Creates new BreakableMap instance
    /// - Deep copies underlying map data
    /// - Preserves all break configurations
    /// - Maintains null key and value policies
    /// - Ensures complete independence from original
    ///
    /// **Assertions:**
    /// - Instance independence: `assertNotSame(original, cloned)`
    /// - Data preservation: `assertTrue(cloned.containsKey("key1"))` and `assertTrue(cloned.containsKey("key2"))`
    /// - Policy preservation: `assertFalse(cloned.permitsNullKeys())` and `assertFalse(cloned.permitsNullValues())`
    /// - Break preservation: `assertEquals(0, cloned.size())` and `assertNull(cloned.get("key1"))`
    /// - Independence: modifications to original don't affect clone
    /// - Policy enforcement: clone maintains null restrictions independently
    @Test
    @DisplayName("Test clone method")
    public void testClone() {
        BreakableMap<String, Integer> original = new BreakableMap.Builder<String, Integer>()
            .addBreak(SIZE_ALWAYS_RETURNS_ZERO)
            .addBreak(GET_ALWAYS_RETURNS_NULL)
            .doesNotPermitNullKeys()
            .doesNotPermitNullValues()
            .build();

        original.put("key1", 1);
        original.put("key2", 2);

        BreakableMap<String, Integer> cloned = original.clone();

        // Should be different objects
        assertNotSame(original, cloned);

        // Should have same data (even though get() returns null due to break)
        assertTrue(cloned.containsKey("key1"));
        assertTrue(cloned.containsKey("key2"));

        // Should have same null key policy
        assertFalse(cloned.permitsNullKeys());
        assertEquals(original.permitsNullKeys(), cloned.permitsNullKeys());

        // Should have same null value policy
        assertFalse(cloned.permitsNullValues());
        assertEquals(original.permitsNullValues(), cloned.permitsNullValues());

        // Should have same breaks
        assertEquals(0, cloned.size()); // SIZE_ALWAYS_RETURNS_ZERO is active
        assertNull(cloned.get("key1")); // GET_ALWAYS_RETURNS_NULL is active

        // Should be independent - modifications to original don't affect clone
        original.put("key3", 3);
        assertFalse(cloned.containsKey("key3"));

        // Clone should maintain null policies independently
        assertThrows(NullPointerException.class, () -> cloned.put(null, 42));
        assertThrows(NullPointerException.class, () -> cloned.put("key", null));
        assertThrows(NullPointerException.class, () -> cloned.containsKey(null));
        assertThrows(NullPointerException.class, () -> cloned.containsValue(null));
    }

    @Test
    @DisplayName("clone() copies the breaks and method statuses: a change to the clone's does not reach the original")
    public void cloneDoesNotShareBreaksOrMethodStatuses() {
        BreakableMap<String, Integer> original = new BreakableMap.Builder<String, Integer>().setSafe(true).build();
        BreakableMap<String, Integer> cloned = original.clone();
        cloned.addBreak(SIZE_ALWAYS_RETURNS_ZERO);
        cloned.doesNotSupportMethod(MapMethods.GET);
        assertFalse(original.hasBreak(SIZE_ALWAYS_RETURNS_ZERO));
        assertTrue(original.supportsMethod(MapMethods.GET));
        assertTrue(cloned.isSafe());
    }
}
