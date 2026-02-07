package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableNavigableSet.*;

/**
 * Comprehensive test suite for validating the behavior of {@link BreakableNavigableSet} and its various breakable behaviors.
 *
 * <p>This test class verifies that the {@code BreakableNavigableSet} class correctly implements programmatically
 * broken {@link NavigableSet} behavior for testing purposes. The {@code BreakableNavigableSet} extends the functionality
 * of {@code BreakableSortedSet} with additional navigation methods and their corresponding breakable behaviors.
 *
 * ### Purpose and Use Cases
 * <p>The {@code BreakableNavigableSet} is particularly valuable for:
 * <ul>
 * <li>Testing error handling in code that uses NavigableSet navigation methods</li>
 * <li>Validating robustness of algorithms against malformed navigation behavior</li>
 * <li>Ensuring navigation-dependent code handles method failures gracefully</li>
 * <li>Testing edge cases in ceiling, floor, higher, and lower operations</li>
 * <li>Verifying poll operation consistency and error handling</li>
 * <li>Testing descending iteration and view functionality</li>
 * </ul>
 *
 * ### Breakable Behaviors Tested
 * <p>This test suite validates the following types of breakable behaviors:
 *
 * #### Navigation Method Breaks
 * <ul>
 * <li>{@code CEILING_ALWAYS_RETURNS_NULL} - ceiling() method always returns null</li>
 * <li>{@code CEILING_RETURNS_FLOOR_VALUE} - ceiling() returns floor value instead</li>
 * <li>{@code FLOOR_ALWAYS_RETURNS_NULL} - floor() method always returns null</li>
 * <li>{@code FLOOR_RETURNS_CEILING_VALUE} - floor() returns ceiling value instead</li>
 * <li>{@code HIGHER_ALWAYS_RETURNS_NULL} - higher() method always returns null</li>
 * <li>{@code HIGHER_RETURNS_LOWER_VALUE} - higher() returns lower value instead</li>
 * <li>{@code LOWER_ALWAYS_RETURNS_NULL} - lower() method always returns null</li>
 * <li>{@code LOWER_RETURNS_HIGHER_VALUE} - lower() returns higher value instead</li>
 * </ul>
 *
 * #### Poll Operation Breaks
 * <ul>
 * <li>{@code POLL_FIRST_ALWAYS_RETURNS_NULL} - pollFirst() always returns null</li>
 * <li>{@code POLL_FIRST_DOES_NOT_REMOVE} - pollFirst() returns element but doesn't remove it</li>
 * <li>{@code POLL_LAST_ALWAYS_RETURNS_NULL} - pollLast() always returns null</li>
 * <li>{@code POLL_LAST_DOES_NOT_REMOVE} - pollLast() returns element but doesn't remove it</li>
 * </ul>
 *
 * #### Descendant View Breaks
 * <ul>
 * <li>{@code DESCENDING_SET_RETURNS_EMPTY} - descendingSet() always returns empty set</li>
 * <li>{@code DESCENDING_ITERATOR_RETURNS_EMPTY} - descendingIterator() returns empty iterator</li>
 * </ul>
 *
 * ### Testing Strategy
 * <p>Each test method focuses on a specific break behavior and verifies that:
 * <ul>
 * <li>The break is correctly applied to the navigable set instance</li>
 * <li>The broken behavior persists across method calls</li>
 * <li>The break doesn't affect unrelated navigable set functionality</li>
 * <li>Edge cases and boundary conditions are handled appropriately</li>
 * <li>Normal operations continue to work when breaks are not applied</li>
 * </ul>
 *
 * @author evanbergstrom
 * @see BreakableNavigableSet
 * @see BreakableNavigableSet.Builder
 * @see NavigableSet
 * @since 1.0
 */
@DisplayName("Tests for BreakableNavigableSet")
public class BreakableNavigableSetTest {

    // ========== Navigation Method Tests ==========

    /**
     * Tests the {@code CEILING_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code ceiling()} method always
     * returns {@code null}, even when valid ceiling elements exist in the set.
     */
    @Test
    public void testCeilingAlwaysReturnsNull() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(CEILING_ALWAYS_RETURNS_NULL)
                .build();

        brokenSet.addAll(Arrays.asList(1, 3, 5, 7, 9));

        // All ceiling operations should return null
        assertNull(brokenSet.ceiling(0)); // Should find 1, but returns null
        assertNull(brokenSet.ceiling(2)); // Should find 3, but returns null
        assertNull(brokenSet.ceiling(5)); // Should find 5, but returns null
        assertNull(brokenSet.ceiling(10)); // Should return null normally anyway

        // Verify normal behavior without break
        BreakableNavigableSet<Integer> normalSet = new BreakableNavigableSet<>();
        normalSet.addAll(Arrays.asList(1, 3, 5, 7, 9));
        assertEquals(Integer.valueOf(1), normalSet.ceiling(0));
        assertEquals(Integer.valueOf(3), normalSet.ceiling(2));
        assertEquals(Integer.valueOf(5), normalSet.ceiling(5));
    }

    /**
     * Tests the {@code CEILING_RETURNS_FLOOR_VALUE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code ceiling()} method returns
     * the floor value instead of the ceiling value, effectively swapping the semantics.
     */
    @Test
    public void testCeilingReturnsFloorValue() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(CEILING_RETURNS_FLOOR_VALUE)
                .build();

        brokenSet.addAll(Arrays.asList(1, 3, 5, 7, 9));

        // ceiling() should return floor values instead
        assertNull(brokenSet.ceiling(0)); // floor(0) = null
        assertEquals(Integer.valueOf(1), brokenSet.ceiling(2)); // floor(2) = 1
        assertEquals(Integer.valueOf(5), brokenSet.ceiling(5)); // floor(5) = 5
        assertEquals(Integer.valueOf(7), brokenSet.ceiling(8)); // floor(8) = 7
    }

    /**
     * Tests the {@code FLOOR_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code floor()} method always
     * returns {@code null}, even when valid floor elements exist in the set.
     */
    @Test
    public void testFloorAlwaysReturnsNull() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(FLOOR_ALWAYS_RETURNS_NULL)
                .build();

        brokenSet.addAll(Arrays.asList(1, 3, 5, 7, 9));

        // All floor operations should return null
        assertNull(brokenSet.floor(0)); // Should return null normally anyway
        assertNull(brokenSet.floor(2)); // Should find 1, but returns null
        assertNull(brokenSet.floor(5)); // Should find 5, but returns null
        assertNull(brokenSet.floor(10)); // Should find 9, but returns null

        // Verify normal behavior without break
        BreakableNavigableSet<Integer> normalSet = new BreakableNavigableSet<>();
        normalSet.addAll(Arrays.asList(1, 3, 5, 7, 9));
        assertEquals(Integer.valueOf(1), normalSet.floor(2));
        assertEquals(Integer.valueOf(5), normalSet.floor(5));
        assertEquals(Integer.valueOf(9), normalSet.floor(10));
    }

    /**
     * Tests the {@code FLOOR_RETURNS_CEILING_VALUE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code floor()} method returns
     * the ceiling value instead of the floor value, effectively swapping the semantics.
     */
    @Test
    public void testFloorReturnsCeilingValue() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(FLOOR_RETURNS_CEILING_VALUE)
                .build();

        brokenSet.addAll(Arrays.asList(1, 3, 5, 7, 9));

        // floor() should return ceiling values instead
        assertEquals(Integer.valueOf(1), brokenSet.floor(0)); // ceiling(0) = 1
        assertEquals(Integer.valueOf(3), brokenSet.floor(2)); // ceiling(2) = 3
        assertEquals(Integer.valueOf(5), brokenSet.floor(5)); // ceiling(5) = 5
        assertNull(brokenSet.floor(10)); // ceiling(10) = null
    }

    /**
     * Tests the {@code HIGHER_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code higher()} method always
     * returns {@code null}, even when valid higher elements exist in the set.
     */
    @Test
    public void testHigherAlwaysReturnsNull() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(HIGHER_ALWAYS_RETURNS_NULL)
                .build();

        brokenSet.addAll(Arrays.asList(1, 3, 5, 7, 9));

        // All higher operations should return null
        assertNull(brokenSet.higher(0)); // Should find 1, but returns null
        assertNull(brokenSet.higher(1)); // Should find 3, but returns null
        assertNull(brokenSet.higher(5)); // Should find 7, but returns null
        assertNull(brokenSet.higher(9)); // Should return null normally anyway

        // Verify normal behavior without break
        BreakableNavigableSet<Integer> normalSet = new BreakableNavigableSet<>();
        normalSet.addAll(Arrays.asList(1, 3, 5, 7, 9));
        assertEquals(Integer.valueOf(1), normalSet.higher(0));
        assertEquals(Integer.valueOf(3), normalSet.higher(1));
        assertEquals(Integer.valueOf(7), normalSet.higher(5));
    }

    /**
     * Tests the {@code HIGHER_RETURNS_LOWER_VALUE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code higher()} method returns
     * the lower value instead of the higher value, effectively swapping the semantics.
     */
    @Test
    public void testHigherReturnsLowerValue() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(HIGHER_RETURNS_LOWER_VALUE)
                .build();

        brokenSet.addAll(Arrays.asList(1, 3, 5, 7, 9));

        // higher() should return lower values instead
        assertNull(brokenSet.higher(0)); // lower(0) = null
        assertNull(brokenSet.higher(1)); // lower(1) = null
        assertEquals(Integer.valueOf(3), brokenSet.higher(5)); // lower(5) = 3
        assertEquals(Integer.valueOf(7), brokenSet.higher(9)); // lower(9) = 7
    }

    /**
     * Tests the {@code LOWER_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code lower()} method always
     * returns {@code null}, even when valid lower elements exist in the set.
     */
    @Test
    public void testLowerAlwaysReturnsNull() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(LOWER_ALWAYS_RETURNS_NULL)
                .build();

        brokenSet.addAll(Arrays.asList(1, 3, 5, 7, 9));

        // All lower operations should return null
        assertNull(brokenSet.lower(0)); // Should return null normally anyway
        assertNull(brokenSet.lower(2)); // Should find 1, but returns null
        assertNull(brokenSet.lower(5)); // Should find 3, but returns null
        assertNull(brokenSet.lower(10)); // Should find 9, but returns null

        // Verify normal behavior without break
        BreakableNavigableSet<Integer> normalSet = new BreakableNavigableSet<>();
        normalSet.addAll(Arrays.asList(1, 3, 5, 7, 9));
        assertEquals(Integer.valueOf(1), normalSet.lower(2));
        assertEquals(Integer.valueOf(3), normalSet.lower(5));
        assertEquals(Integer.valueOf(9), normalSet.lower(10));
    }

    /**
     * Tests the {@code LOWER_RETURNS_HIGHER_VALUE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code lower()} method returns
     * the higher value instead of the lower value, effectively swapping the semantics.
     */
    @Test
    public void testLowerReturnsHigherValue() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(LOWER_RETURNS_HIGHER_VALUE)
                .build();

        brokenSet.addAll(Arrays.asList(1, 3, 5, 7, 9));

        // lower() should return higher values instead
        assertEquals(Integer.valueOf(1), brokenSet.lower(0)); // higher(0) = 1
        assertEquals(Integer.valueOf(3), brokenSet.lower(1)); // higher(1) = 3
        assertEquals(Integer.valueOf(7), brokenSet.lower(5)); // higher(5) = 7
        assertNull(brokenSet.lower(10)); // higher(10) = null
    }

    // ========== Poll Operation Tests ==========

    /**
     * Tests the {@code POLL_FIRST_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code pollFirst()} method always
     * returns {@code null}, even when elements exist in the set.
     */
    @Test
    public void testPollFirstAlwaysReturnsNull() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(POLL_FIRST_ALWAYS_RETURNS_NULL)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3));

        // pollFirst should return null despite having elements
        assertNull(brokenSet.pollFirst());
        assertEquals(3, brokenSet.size()); // Size should remain unchanged

        // Try again to ensure consistency
        assertNull(brokenSet.pollFirst());
        assertEquals(3, brokenSet.size());

        // Verify normal behavior without break
        BreakableNavigableSet<Integer> normalSet = new BreakableNavigableSet<>();
        normalSet.addAll(Arrays.asList(1, 2, 3));
        assertEquals(Integer.valueOf(1), normalSet.pollFirst());
        assertEquals(2, normalSet.size());
    }

    /**
     * Tests the {@code POLL_FIRST_DOES_NOT_REMOVE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code pollFirst()} method returns
     * the first element but does not remove it from the set.
     */
    @Test
    public void testPollFirstDoesNotRemove() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(POLL_FIRST_DOES_NOT_REMOVE)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3));

        // pollFirst should return first element but not remove it
        assertEquals(Integer.valueOf(1), brokenSet.pollFirst());
        assertEquals(3, brokenSet.size()); // Size should remain unchanged
        assertTrue(brokenSet.contains(1)); // Element should still be present

        // Multiple calls should return the same element
        assertEquals(Integer.valueOf(1), brokenSet.pollFirst());
        assertEquals(3, brokenSet.size());

        // Test with empty set
        BreakableNavigableSet<Integer> emptySet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(POLL_FIRST_DOES_NOT_REMOVE)
                .build();
        assertNull(emptySet.pollFirst());
    }

    /**
     * Tests the {@code POLL_LAST_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code pollLast()} method always
     * returns {@code null}, even when elements exist in the set.
     */
    @Test
    public void testPollLastAlwaysReturnsNull() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(POLL_LAST_ALWAYS_RETURNS_NULL)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3));

        // pollLast should return null despite having elements
        assertNull(brokenSet.pollLast());
        assertEquals(3, brokenSet.size()); // Size should remain unchanged

        // Try again to ensure consistency
        assertNull(brokenSet.pollLast());
        assertEquals(3, brokenSet.size());

        // Verify normal behavior without break
        BreakableNavigableSet<Integer> normalSet = new BreakableNavigableSet<>();
        normalSet.addAll(Arrays.asList(1, 2, 3));
        assertEquals(Integer.valueOf(3), normalSet.pollLast());
        assertEquals(2, normalSet.size());
    }

    /**
     * Tests the {@code POLL_LAST_DOES_NOT_REMOVE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code pollLast()} method returns
     * the last element but does not remove it from the set.
     */
    @Test
    public void testPollLastDoesNotRemove() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(POLL_LAST_DOES_NOT_REMOVE)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3));

        // pollLast should return last element but not remove it
        assertEquals(Integer.valueOf(3), brokenSet.pollLast());
        assertEquals(3, brokenSet.size()); // Size should remain unchanged
        assertTrue(brokenSet.contains(3)); // Element should still be present

        // Multiple calls should return the same element
        assertEquals(Integer.valueOf(3), brokenSet.pollLast());
        assertEquals(3, brokenSet.size());

        // Test with empty set
        BreakableNavigableSet<Integer> emptySet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(POLL_LAST_DOES_NOT_REMOVE)
                .build();
        assertNull(emptySet.pollLast());
    }

    // ========== Descendant View Tests ==========

    /**
     * Tests the {@code DESCENDING_SET_RETURNS_EMPTY} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code descendingSet()} method always
     * returns an empty set, regardless of the actual contents of the set.
     */
    @Test
    public void testDescendingSetReturnsEmpty() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(DESCENDING_SET_RETURNS_EMPTY)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3, 4, 5));

        // descendingSet should return empty despite having elements
        NavigableSet<Integer> descendingSet = brokenSet.descendingSet();
        assertTrue(descendingSet.isEmpty());
        assertEquals(0, descendingSet.size());

        // Verify normal behavior without break
        BreakableNavigableSet<Integer> normalSet = new BreakableNavigableSet<>();
        normalSet.addAll(Arrays.asList(1, 2, 3, 4, 5));
        NavigableSet<Integer> normalDescending = normalSet.descendingSet();
        assertEquals(5, normalDescending.size());
        assertEquals(Integer.valueOf(5), normalDescending.first());
        assertEquals(Integer.valueOf(1), normalDescending.last());
    }

    /**
     * Tests the {@code DESCENDING_ITERATOR_RETURNS_EMPTY} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code descendingIterator()} method always
     * returns an empty iterator, regardless of the actual contents of the set.
     */
    @Test
    public void testDescendingIteratorReturnsEmpty() {
        BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(DESCENDING_ITERATOR_RETURNS_EMPTY)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3, 4, 5));

        // descendingIterator should return empty despite having elements
        Iterator<Integer> descendingIterator = brokenSet.descendingIterator();
        assertFalse(descendingIterator.hasNext());

        // Verify normal behavior without break
        BreakableNavigableSet<Integer> normalSet = new BreakableNavigableSet<>();
        normalSet.addAll(Arrays.asList(1, 2, 3, 4, 5));
        Iterator<Integer> normalDescendingIterator = normalSet.descendingIterator();
        assertTrue(normalDescendingIterator.hasNext());
        assertEquals(Integer.valueOf(5), normalDescendingIterator.next());
        assertEquals(Integer.valueOf(4), normalDescendingIterator.next());
    }

    // ========== Integration and Normal Behavior Tests ==========

    /**
     * Tests the normal NavigableSet behavior without any breaks applied.
     *
     * <p>This test serves as a comprehensive control test, verifying that
     * BreakableNavigableSet behaves like a standard NavigableSet when no breaks
     * are active. This ensures the implementation correctly delegates to
     * the underlying navigable set for normal operations.
     */
    @Test
    public void testNormalNavigableSetBehavior() {
        BreakableNavigableSet<String> set = new BreakableNavigableSet<>();
        set.addAll(Arrays.asList("apple", "banana", "cherry", "date", "elderberry"));

        // Test navigation methods
        assertEquals("apple", set.lower("banana"));
        assertEquals("banana", set.floor("banana"));
        assertEquals("banana", set.ceiling("banana"));
        assertEquals("cherry", set.higher("banana"));

        // Test with values not in set
        assertEquals("apple", set.floor("avocado"));
        assertEquals("banana", set.ceiling("avocado"));
        assertEquals("apple", set.lower("avocado"));
        assertEquals("banana", set.higher("avocado"));

        // Test poll operations
        assertEquals("apple", set.pollFirst());
        assertEquals(4, set.size());
        assertEquals("elderberry", set.pollLast());
        assertEquals(3, set.size());

        // Test descending operations
        NavigableSet<String> descendingSet = set.descendingSet();
        assertEquals(3, descendingSet.size());
        assertEquals("date", descendingSet.first());
        assertEquals("banana", descendingSet.last());

        Iterator<String> descendingIterator = set.descendingIterator();
        assertEquals("date", descendingIterator.next());
        assertEquals("cherry", descendingIterator.next());
        assertEquals("banana", descendingIterator.next());
    }

    /**
     * Tests the integration of multiple breaks working together.
     *
     * <p>Verifies that multiple NavigableSet breaks can be applied simultaneously
     * and that each break affects only its intended functionality while leaving
     * other operations working normally.
     */
    @Test
    public void testMultipleBreaksIntegration() {
        BreakableNavigableSet<Integer> multiBreakSet = new BreakableNavigableSet.Builder<Integer>()
                .addBreak(CEILING_ALWAYS_RETURNS_NULL)
                .addBreak(POLL_FIRST_DOES_NOT_REMOVE)
                .addBreak(DESCENDING_ITERATOR_RETURNS_EMPTY)
                .build();

        multiBreakSet.addAll(Arrays.asList(0, 2, 4, 6, 8));

        // Test that each break works independently
        assertNull(multiBreakSet.ceiling(3)); // CEILING_ALWAYS_RETURNS_NULL
        assertEquals(0, multiBreakSet.pollFirst()); // POLL_FIRST_DOES_NOT_REMOVE
        assertEquals(5, multiBreakSet.size()); // Element not removed
        assertFalse(multiBreakSet.descendingIterator().hasNext()); // DESCENDING_ITERATOR_RETURNS_EMPTY

        // Test that non-broken operations still work
        assertEquals(2, multiBreakSet.floor(3)); // floor should work normally
        assertEquals(4, multiBreakSet.higher(3)); // higher should work normally
        assertEquals(5, multiBreakSet.size()); // Basic operations should work
        assertTrue(multiBreakSet.contains(4));
    }

    /**
     * Tests the builder functionality with custom comparator.
     *
     * <p>Verifies that the builder correctly handles comparator configuration and
     * that the resulting navigable set uses the specified comparator for ordering
     * and navigation operations.
     */
    @Test
    public void testBuilderWithCustomComparator() {
        // Test with reverse ordering
        BreakableNavigableSet<Integer> reverseSet = new BreakableNavigableSet.Builder<Integer>()
                .withComparator(Collections.reverseOrder())
                .build();

        reverseSet.addAll(Arrays.asList(0, 2, 4, 6, 8));

        // Verify reverse ordering
        assertEquals(8, reverseSet.first());
        assertEquals(0, reverseSet.last());

        // Test navigation methods with reverse ordering
        assertEquals(4, reverseSet.ceiling(5)); // ceiling in reverse = floor in normal
        assertEquals(4, reverseSet.floor(3)); // floor in reverse = ceiling in normal
        assertEquals(4, reverseSet.higher(6)); // higher in reverse = lower in normal
        assertEquals(4, reverseSet.lower(2)); // lower in reverse = higher in normal
    }

    /**
     * Tests the builder copy constructor functionality.
     *
     * <p>Verifies that the builder copy constructor creates independent builders
     * that can be configured separately while preserving the original configuration
     * as a starting point.
     */
    @Test
    public void testBuilderCopyConstructor() {
        // Create a base builder configuration
        BreakableNavigableSet.Builder<Integer> baseBuilder = new BreakableNavigableSet.Builder<Integer>()
                .withComparator(Collections.reverseOrder())
                .addBreak(CEILING_ALWAYS_RETURNS_NULL);

        // Create a copy and add_singleElement_returnsTrueAndUpdatesSize additional breaks
        BreakableNavigableSet.Builder<Integer> copyBuilder = new BreakableNavigableSet.Builder<>(baseBuilder)
                .addBreak(POLL_FIRST_DOES_NOT_REMOVE);

        // Build sets from both builders
        BreakableNavigableSet<Integer> baseSet = baseBuilder.build();
        BreakableNavigableSet<Integer> copySet = copyBuilder.build();

        baseSet.addAll(Arrays.asList(1, 2, 3));
        copySet.addAll(Arrays.asList(1, 2, 3));

        // Both should have the ceiling break
        assertNull(baseSet.ceiling(2));
        assertNull(copySet.ceiling(2));

        // Only copy should have the poll break
        assertEquals(Integer.valueOf(3), baseSet.pollFirst()); // Normal behavior (reverse order)
        assertEquals(2, baseSet.size()); // Element removed

        assertEquals(Integer.valueOf(3), copySet.pollFirst()); // Returns but doesn't remove
        assertEquals(3, copySet.size()); // Element not removed due to break
    }

    /**
     * Tests error handling in normal operations for edge cases.
     *
     * <p>Verifies that BreakableNavigableSet properly handles edge cases and
     * error conditions according to the NavigableSet contract, such as accessing
     * elements from empty sets or boundary conditions.
     */
    @Test
    public void testEdgeCasesAndErrorHandling() {
        BreakableNavigableSet<String> emptySet = new BreakableNavigableSet<>();

        // Test navigation methods on empty set
        assertNull(emptySet.lower("test"));
        assertNull(emptySet.floor("test"));
        assertNull(emptySet.ceiling("test"));
        assertNull(emptySet.higher("test"));

        // Test poll operations on empty set
        assertNull(emptySet.pollFirst());
        assertNull(emptySet.pollLast());

        // Test descending operations on empty set
        NavigableSet<String> emptyDescending = emptySet.descendingSet();
        assertTrue(emptyDescending.isEmpty());
        assertFalse(emptySet.descendingIterator().hasNext());

        // Test with single element
        BreakableNavigableSet<Integer> singleElementSet = new BreakableNavigableSet<>();
        singleElementSet.add(5);

        assertEquals(Integer.valueOf(5), singleElementSet.floor(5));
        assertEquals(Integer.valueOf(5), singleElementSet.ceiling(5));
        assertNull(singleElementSet.lower(5));
        assertNull(singleElementSet.higher(5));
    }
}