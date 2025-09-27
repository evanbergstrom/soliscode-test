package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableSortedSet.*;

/**
 * Comprehensive test suite for validating the behavior of {@link BreakableSortedSet} and its various breakable behaviors.
 *
 * <p>This test class verifies that the {@code BreakableSortedSet} class correctly implements programmatically
 * broken {@link SortedSet} behavior for testing purposes. The {@code BreakableSortedSet} is designed to
 * simulate faulty or edge-case implementations of the SortedSet interface, enabling thorough testing of
 * code that depends on proper SortedSet behavior.
 *
 * <h3>Purpose and Use Cases</h3>
 * <p>The {@code BreakableSortedSet} is particularly valuable for:
 * <ul>
 * <li>Testing error handling in code that uses sorted collections</li>
 * <li>Validating robustness of algorithms against malformed sorted set behavior</li>
 * <li>Ensuring collection-dependent code handles navigation method failures gracefully</li>
 * <li>Testing edge cases in range-based operations and subset functionality</li>
 * <li>Verifying defensive programming practices in sorted collection-dependent code</li>
 * </ul>
 *
 * <h3>Breakable Behaviors Tested</h3>
 * <p>This test suite validates the following types of breakable behaviors:
 *
 * <h4>Comparator Breaks</h4>
 * <ul>
 * <li>{@code COMPARATOR_ALWAYS_RETURNS_NULL} - Comparator method always returns null</li>
 * </ul>
 *
 * <h4>Navigation Breaks</h4>
 * <ul>
 * <li>{@code FIRST_ALWAYS_THROWS_EXCEPTION} - first() method always throws NoSuchElementException</li>
 * <li>{@code FIRST_RETURNS_LAST_ELEMENT} - first() method returns the last element</li>
 * <li>{@code FIRST_SKIPS_FIRST_ELEMENT} - first() method returns the second element</li>
 * <li>{@code FIRST_ALWAYS_RETURNS_NULL} - first() method always returns null</li>
 * <li>{@code FIRST_RETURNS_NULL_WHEN_EMPTY} - first() method returns null when empty</li>
 * <li>{@code LAST_ALWAYS_THROWS_EXCEPTION} - last() method always throws NoSuchElementException</li>
 * <li>{@code LAST_RETURNS_FIRST_ELEMENT} - last() method returns the first element</li>
 * <li>{@code LAST_SKIPS_LAST_ELEMENT} - last() method returns the second-to-last element</li>
 * <li>{@code LAST_ALWAYS_RETURNS_NULL} - last() method always returns null</li>
 * <li>{@code LAST_RETURNS_NULL_WHEN_EMPTY} - last() method returns null when empty</li>
 * </ul>
 *
 * <h4>Subset Operation Breaks</h4>
 * <ul>
 * <li>{@code SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY} - All subset operations return empty sets</li>
 * <li>{@code SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION} - All subset operations throw exceptions</li>
 * </ul>
 *
 * <h3>Testing Strategy</h3>
 * <p>Each test method focuses on a specific break behavior and verifies that:
 * <ul>
 * <li>The break is correctly applied to the sorted set instance</li>
 * <li>The broken behavior persists across method calls</li>
 * <li>The break doesn't affect unrelated sorted set functionality</li>
 * <li>Edge cases and boundary conditions are handled appropriately</li>
 * </ul>
 *
 * <p>The tests use the {@link BreakableSortedSet.Builder} pattern to construct sorted sets with specific
 * breaks, then verify the expected broken behavior through direct method calls and assertions.
 *
 * <h3>Integration with Testing Framework</h3>
 * <p>These breakable sorted sets are designed to be used in conjunction with collection contract
 * tests to ensure that sorted collection implementations properly handle malformed behavior.
 * This is particularly important for algorithms that depend on sorted set characteristics for
 * correctness and performance.
 *
 * <p>Usage example:
 * <pre>{@code
 * // Create a sorted set with broken navigation
 * BreakableSortedSet<String> sortedSet = new BreakableSortedSet.Builder<String>()
 *     .addBreak(FIRST_ALWAYS_THROWS_EXCEPTION)
 *     .build();
 *
 * sortedSet.addAll(Arrays.asList("a", "b", "c"));
 * assertThrows(NoSuchElementException.class, () -> sortedSet.first()); // Should throw despite having elements
 * }</pre>
 *
 * @author evanbergstrom
 * @see BreakableSortedSet
 * @see BreakableSortedSet.Builder
 * @see SortedSet
 * @since 1.0
 */
@DisplayName("Tests for BreakableSortedSet")
public class BreakableSortedSetTest {

    /**
     * Tests the {@code COMPARATOR_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code comparator()} method
     * always returns {@code null}, even when a custom comparator was provided during
     * construction. This simulates a sorted set that loses track of its ordering comparator.
     *
     * <p>The test confirms that despite having a comparator configured, the broken
     * behavior overrides the normal comparator access, which is useful for testing
     * code that must handle unknown ordering scenarios.
     */
    @Test
    public void testComparatorAlwaysReturnsNull() {
        // Test with natural ordering - should return null normally
        BreakableSortedSet<String> naturalSet = new BreakableSortedSet.Builder<String>()
                .addBreak(COMPARATOR_ALWAYS_RETURNS_NULL)
                .build();
        assertNull(naturalSet.comparator());

        // Test with custom comparator - should return null with break
        BreakableSortedSet<String> customSet = new BreakableSortedSet.Builder<String>()
                .withComparator(String.CASE_INSENSITIVE_ORDER)
                .addBreak(COMPARATOR_ALWAYS_RETURNS_NULL)
                .build();
        assertNull(customSet.comparator()); // Should return null despite having comparator

        // Verify normal behavior without break
        BreakableSortedSet<String> normalSet = new BreakableSortedSet.Builder<String>()
                .withComparator(String.CASE_INSENSITIVE_ORDER)
                .build();
        assertEquals(String.CASE_INSENSITIVE_ORDER, normalSet.comparator());
    }

    /**
     * Tests the {@code FIRST_ALWAYS_THROWS_EXCEPTION} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code first()} method always
     * throws {@link NoSuchElementException}, even when elements exist in the set.
     * This simulates a navigation failure in accessing the first element.
     *
     * <p>The test confirms that the broken behavior persists regardless of set contents,
     * which is useful for testing error handling in code that accesses the first element.
     */
    @Test
    public void testFirstAlwaysThrowsException() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(FIRST_ALWAYS_THROWS_EXCEPTION)
                .build();

        // Should throw even with elements present
        brokenSet.addAll(Arrays.asList(3, 1, 2));
        assertFalse(brokenSet.isEmpty());
        assertThrows(NoSuchElementException.class, () -> brokenSet.first());

        // Verify normal behavior without break
        BreakableSortedSet<Integer> normalSet = new BreakableSortedSet<>();
        normalSet.addAll(Arrays.asList(3, 1, 2));
        assertEquals(Integer.valueOf(1), normalSet.first()); // Should return lowest element

        // Verify normal exception for empty set
        BreakableSortedSet<Integer> emptySet = new BreakableSortedSet<>();
        assertThrows(NoSuchElementException.class, () -> emptySet.first());
    }

    /**
     * Tests the {@code FIRST_RETURNS_LAST_ELEMENT} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code first()} method returns
     * the last element instead of the first element. This simulates a navigation confusion
     * where first and last are swapped.
     */
    @Test
    public void testFirstReturnsLastElement() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(FIRST_RETURNS_LAST_ELEMENT)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3, 4, 5));

        // Should return 5 (last element) instead of 1 (first element)
        assertEquals(Integer.valueOf(5), brokenSet.first());

        // Last should still work normally
        assertEquals(Integer.valueOf(5), brokenSet.last());
    }

    /**
     * Tests the {@code FIRST_SKIPS_FIRST_ELEMENT} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code first()} method returns
     * the second element instead of the actual first element. This simulates a navigation
     * error where the iterator skips the first position.
     */
    @Test
    public void testFirstSkipsFirstElement() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(FIRST_SKIPS_FIRST_ELEMENT)
                .build();

        // With multiple elements, should return the second element
        brokenSet.addAll(Arrays.asList(1, 2, 3, 4, 5));
        assertEquals(Integer.valueOf(2), brokenSet.first()); // Returns second element

        // With only one element, should throw exception (no second element)
        BreakableSortedSet<Integer> singleElementSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(FIRST_SKIPS_FIRST_ELEMENT)
                .build();
        singleElementSet.add(1);
        assertThrows(NoSuchElementException.class, () -> singleElementSet.first());

        // With empty set, should throw exception
        BreakableSortedSet<Integer> emptySet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(FIRST_SKIPS_FIRST_ELEMENT)
                .build();
        assertThrows(NoSuchElementException.class, () -> emptySet.first());
    }

    /**
     * Tests the {@code FIRST_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code first()} method always
     * returns {@code null}, even when elements exist. This simulates a navigation
     * failure where the first element cannot be accessed.
     */
    @Test
    public void testFirstAlwaysReturnsNull() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(FIRST_ALWAYS_RETURNS_NULL)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3));
        assertNull(brokenSet.first()); // Should return null despite having elements

        // Empty set should also return null
        BreakableSortedSet<Integer> emptySet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(FIRST_ALWAYS_RETURNS_NULL)
                .build();
        assertNull(emptySet.first());
    }

    /**
     * Tests the {@code FIRST_RETURNS_NULL_WHEN_EMPTY} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code first()} method returns
     * {@code null} when the set is empty, instead of throwing {@link NoSuchElementException}.
     * This simulates graceful degradation for empty set access.
     */
    @Test
    public void testFirstReturnsNullWhenEmpty() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(FIRST_RETURNS_NULL_WHEN_EMPTY)
                .build();

        // Empty set should return null instead of throwing exception
        assertNull(brokenSet.first());

        // Non-empty set should work normally
        brokenSet.add(1);
        assertEquals(Integer.valueOf(1), brokenSet.first());
    }

    /**
     * Tests the {@code LAST_ALWAYS_THROWS_EXCEPTION} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code last()} method always
     * throws {@link NoSuchElementException}, even when elements exist in the set.
     * This simulates a navigation failure in accessing the last element.
     *
     * <p>The test confirms that the broken behavior persists regardless of set contents,
     * which is useful for testing error handling in code that accesses the last element.
     */
    @Test
    public void testLastAlwaysThrowsException() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(LAST_ALWAYS_THROWS_EXCEPTION)
                .build();

        // Should throw even with elements present
        brokenSet.addAll(Arrays.asList(3, 1, 2));
        assertFalse(brokenSet.isEmpty());
        assertThrows(NoSuchElementException.class, () -> brokenSet.last());

        // Verify normal behavior without break
        BreakableSortedSet<Integer> normalSet = new BreakableSortedSet<>();
        normalSet.addAll(Arrays.asList(3, 1, 2));
        assertEquals(Integer.valueOf(3), normalSet.last()); // Should return highest element

        // Verify normal exception for empty set
        BreakableSortedSet<Integer> emptySet = new BreakableSortedSet<>();
        assertThrows(NoSuchElementException.class, () -> emptySet.last());
    }

    /**
     * Tests the {@code LAST_RETURNS_FIRST_ELEMENT} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code last()} method returns
     * the first element instead of the last element. This simulates a navigation confusion
     * where first and last are swapped for the last() method.
     */
    @Test
    public void testLastReturnsFirstElement() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(LAST_RETURNS_FIRST_ELEMENT)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3, 4, 5));

        // Should return 1 (first element) instead of 5 (last element)
        assertEquals(Integer.valueOf(1), brokenSet.last());

        // First should still work normally
        assertEquals(Integer.valueOf(1), brokenSet.first());
    }

    /**
     * Tests the {@code LAST_SKIPS_LAST_ELEMENT} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code last()} method returns
     * the second-to-last element instead of the actual last element. This simulates a navigation
     * error where the iterator misses the last position.
     */
    @Test
    public void testLastSkipsLastElement() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(LAST_SKIPS_LAST_ELEMENT)
                .build();

        // With multiple elements, should return the second-to-last element
        brokenSet.addAll(Arrays.asList(1, 2, 3, 4, 5));
        assertEquals(Integer.valueOf(4), brokenSet.last()); // Returns second-to-last element

        // With only one element, should throw exception (no second-to-last element)
        BreakableSortedSet<Integer> singleElementSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(LAST_SKIPS_LAST_ELEMENT)
                .build();
        singleElementSet.add(1);
        assertThrows(NoSuchElementException.class, singleElementSet::last);

        // With empty set, should throw exception
        BreakableSortedSet<Integer> emptySet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(LAST_SKIPS_LAST_ELEMENT)
                .build();
        assertThrows(NoSuchElementException.class, emptySet::last);
    }

    /**
     * Tests the {@code LAST_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code last()} method always
     * returns {@code null}, even when elements exist. This simulates a navigation
     * failure where the last element cannot be accessed.
     */
    @Test
    public void testLastAlwaysReturnsNull() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(LAST_ALWAYS_RETURNS_NULL)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3));
        assertNull(brokenSet.last()); // Should return null despite having elements

        // Empty set should also return null
        BreakableSortedSet<Integer> emptySet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(LAST_ALWAYS_RETURNS_NULL)
                .build();
        assertNull(emptySet.last());
    }

    /**
     * Tests the {@code LAST_RETURNS_NULL_WHEN_EMPTY} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code last()} method returns
     * {@code null} when the set is empty, instead of throwing {@link NoSuchElementException}.
     * This simulates graceful degradation for empty set access.
     */
    @Test
    public void testLastReturnsNullWhenEmpty() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(LAST_RETURNS_NULL_WHEN_EMPTY)
                .build();

        // Empty set should return null instead of throwing exception
        assertNull(brokenSet.last());

        // Non-empty set should work normally
        brokenSet.add(1);
        assertEquals(Integer.valueOf(1), brokenSet.last());
    }

    /**
     * Tests the {@code SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY} break behavior.
     *
     * <p>Verifies that when this break is applied, all subset operations ({@code subSet},
     * {@code headSet}, {@code tailSet}) always return empty sets regardless of the
     * parameters provided or the actual contents of the set.
     *
     * <p>This simulates subset operations that fail to identify ranges properly,
     * which is useful for testing code that depends on subset functionality and
     * must handle empty results gracefully.
     */
    @Test
    public void testSubsetOperationsAlwaysReturnEmpty() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3, 4, 5));

        // All subset operations should return empty sets
        assertTrue(brokenSet.subSet(2, 4).isEmpty());
        assertTrue(brokenSet.headSet(3).isEmpty());
        assertTrue(brokenSet.tailSet(3).isEmpty());

        // Verify normal behavior without break
        BreakableSortedSet<Integer> normalSet = new BreakableSortedSet<>();
        normalSet.addAll(Arrays.asList(1, 2, 3, 4, 5));

        assertEquals(2, normalSet.subSet(2, 4).size()); // Should contain {2, 3}
        assertEquals(2, normalSet.headSet(3).size());   // Should contain {1, 2}
        assertEquals(3, normalSet.tailSet(3).size());   // Should contain {3, 4, 5}
    }

    /**
     * Tests the {@code SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION} break behavior.
     *
     * <p>Verifies that when this break is applied, all subset operations ({@code subSet},
     * {@code headSet}, {@code tailSet}) always throw {@link IllegalArgumentException}
     * regardless of the parameters provided.
     *
     * <p>This simulates subset operations that reject all range specifications as invalid,
     * which is useful for testing error handling in range-based operations.
     */
    @Test
    public void testSubsetOperationsAlwaysThrowException() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION)
                .build();

        brokenSet.addAll(Arrays.asList(1, 2, 3, 4, 5));

        // All subset operations should throw exceptions
        assertThrows(IllegalArgumentException.class, () -> brokenSet.subSet(2, 4));
        assertThrows(IllegalArgumentException.class, () -> brokenSet.headSet(3));
        assertThrows(IllegalArgumentException.class, () -> brokenSet.tailSet(3));
    }

    /**
     * Tests the normal behavior of subset operations without breaks.
     *
     * <p>This test validates the baseline behavior by verifying that subset operations
     * work correctly when no breaks are applied. It serves as a control test to ensure
     * that the break behaviors deviate from the expected normal operation.
     */
    @Test
    public void testNormalSubsetOperations() {
        BreakableSortedSet<String> set = new BreakableSortedSet<>();
        set.addAll(Arrays.asList("apple", "banana", "cherry", "date", "elderberry"));

        // Test subSet operation
        SortedSet<String> subset = set.subSet("banana", "date");
        assertEquals(2, subset.size());
        assertTrue(subset.contains("banana"));
        assertTrue(subset.contains("cherry"));
        assertFalse(subset.contains("date")); // Exclusive upper bound

        // Test headSet operation
        SortedSet<String> headSet = set.headSet("cherry");
        assertEquals(2, headSet.size());
        assertTrue(headSet.contains("apple"));
        assertTrue(headSet.contains("banana"));

        // Test tailSet operation
        SortedSet<String> tailSet = set.tailSet("cherry");
        assertEquals(3, tailSet.size());
        assertTrue(tailSet.contains("cherry"));
        assertTrue(tailSet.contains("date"));
        assertTrue(tailSet.contains("elderberry"));
    }

    /**
     * Tests the builder functionality with comparator configuration.
     *
     * <p>Verifies that the builder correctly handles comparator configuration and
     * that the resulting sorted set uses the specified comparator for ordering.
     * This test also ensures that comparator changes preserve existing elements.
     */
    @Test
    public void testBuilderWithComparator() {
        // Test with case-insensitive comparator
        BreakableSortedSet<String> set = new BreakableSortedSet.Builder<String>()
                .withComparator(String.CASE_INSENSITIVE_ORDER)
                .build();

        set.addAll(Arrays.asList("Zebra", "apple", "Cherry"));

        assertEquals("apple", set.first());     // "apple" comes first case-insensitively
        assertEquals("Zebra", set.last());      // "Zebra" comes last case-insensitively
        assertEquals(String.CASE_INSENSITIVE_ORDER, set.comparator());

        // Test changing comparator with builder
        BreakableSortedSet.Builder<String> builder = new BreakableSortedSet.Builder<>();
        builder.withComparator(Collections.reverseOrder());
        BreakableSortedSet<String> reverseSet = builder.build();

        reverseSet.addAll(Arrays.asList("a", "b", "c"));
        assertEquals("c", reverseSet.first()); // Reverse order
        assertEquals("a", reverseSet.last());
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
        BreakableSortedSet.Builder<Integer> baseBuilder = new BreakableSortedSet.Builder<Integer>()
                .withComparator(Collections.reverseOrder())
                .addBreak(COMPARATOR_ALWAYS_RETURNS_NULL);

        // Create a copy and modify it
        BreakableSortedSet.Builder<Integer> copyBuilder = new BreakableSortedSet.Builder<>(baseBuilder)
                .addBreak(FIRST_ALWAYS_THROWS_EXCEPTION);

        // Build sets from both builders
        BreakableSortedSet<Integer> baseSet = baseBuilder.build();
        BreakableSortedSet<Integer> copySet = copyBuilder.build();

        // Both should have the COMPARATOR_ALWAYS_RETURNS_NULL break
        assertNull(baseSet.comparator());
        assertNull(copySet.comparator());

        // Only the copy should have the FIRST_ALWAYS_THROWS_EXCEPTION break
        baseSet.add(1);
        copySet.add(1);

        // Base set should work normally for first() (except comparator)
        assertEquals(Integer.valueOf(1), baseSet.first());

        // Copy set should throw exception for first()
        assertThrows(NoSuchElementException.class, () -> copySet.first());
    }

    /**
     * Tests the integration of multiple breaks working together.
     *
     * <p>Verifies that multiple breaks can be applied simultaneously and that
     * each break affects only its intended functionality while leaving other
     * operations working normally.
     */
    @Test
    public void testMultipleBreaksIntegration() {
        BreakableSortedSet<String> multiBreakSet = new BreakableSortedSet.Builder<String>()
                .addBreak(COMPARATOR_ALWAYS_RETURNS_NULL)
                .addBreak(FIRST_ALWAYS_THROWS_EXCEPTION)
                .addBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)
                .build();

        multiBreakSet.addAll(Arrays.asList("apple", "banana", "cherry"));

        // Comparator break should be active
        assertNull(multiBreakSet.comparator());

        // First break should be active
        assertThrows(NoSuchElementException.class, () -> multiBreakSet.first());

        // Subset break should be active
        assertTrue(multiBreakSet.subSet("apple", "cherry").isEmpty());

        // Last should still work normally (no break applied)
        assertEquals("cherry", multiBreakSet.last());

        // Basic set operations should still work
        assertTrue(multiBreakSet.contains("banana"));
        assertEquals(3, multiBreakSet.size());
    }

    /**
     * Tests the normal sorted set behavior without any breaks applied.
     *
     * <p>This test serves as a comprehensive control test, verifying that
     * BreakableSortedSet behaves like a standard SortedSet when no breaks
     * are active. This ensures the implementation correctly delegates to
     * the underlying sorted set for normal operations.
     */
    @Test
    public void testNormalSortedSetBehavior() {
        BreakableSortedSet<Integer> set = new BreakableSortedSet<>();

        // Test basic sorted set properties
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());

        // Add elements and verify ordering
        set.addAll(Arrays.asList(5, 1, 3, 2, 4));
        assertEquals(5, set.size());
        assertEquals(Integer.valueOf(1), set.first());
        assertEquals(Integer.valueOf(5), set.last());

        // Test natural ordering is maintained
        Iterator<Integer> iterator = set.iterator();
        int previous = iterator.next();
        while (iterator.hasNext()) {
            int current = iterator.next();
            assertTrue(previous < current, "Elements should be in ascending order");
            previous = current;
        }

        // Test set semantics (no duplicates)
        assertTrue(set.add(6));  // New element
        assertFalse(set.add(5)); // Duplicate element
        assertEquals(6, set.size());

        // Test comparator for natural ordering
        assertNull(set.comparator()); // Natural ordering returns null
    }

    /**
     * Tests error handling in normal operations for edge cases.
     *
     * <p>Verifies that BreakableSortedSet properly handles edge cases and
     * error conditions according to the SortedSet contract, such as accessing
     * elements from empty sets or invalid range parameters.
     */
    @Test
    public void testEdgeCasesAndErrorHandling() {
        BreakableSortedSet<String> emptySet = new BreakableSortedSet<>();

        // Empty set operations should throw appropriate exceptions
        assertThrows(NoSuchElementException.class, () -> emptySet.first());
        assertThrows(NoSuchElementException.class, () -> emptySet.last());

        // Test invalid range parameters
        BreakableSortedSet<String> populatedSet = new BreakableSortedSet<>();
        populatedSet.addAll(Arrays.asList("a", "b", "c", "d"));

        // Invalid range (from > to) should throw exception
        assertThrows(IllegalArgumentException.class, () -> populatedSet.subSet("d", "a"));
    }
}