package com.soliscode.test.breakable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.soliscode.test.breakable.BreakableSortedSet.COMPARATOR_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableSortedSet.FIRST_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableSortedSet.FIRST_ALWAYS_THROWS_EXCEPTION;
import static com.soliscode.test.breakable.BreakableSortedSet.FIRST_RETURNS_LAST_ELEMENT;
import static com.soliscode.test.breakable.BreakableSortedSet.FIRST_RETURNS_NULL_WHEN_EMPTY;
import static com.soliscode.test.breakable.BreakableSortedSet.FIRST_SKIPS_FIRST_ELEMENT;
import static com.soliscode.test.breakable.BreakableSortedSet.LAST_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableSortedSet.LAST_ALWAYS_THROWS_EXCEPTION;
import static com.soliscode.test.breakable.BreakableSortedSet.LAST_RETURNS_FIRST_ELEMENT;
import static com.soliscode.test.breakable.BreakableSortedSet.LAST_RETURNS_NULL_WHEN_EMPTY;
import static com.soliscode.test.breakable.BreakableSortedSet.LAST_SKIPS_LAST_ELEMENT;
import static com.soliscode.test.breakable.BreakableSortedSet.SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY;
import static com.soliscode.test.breakable.BreakableSortedSet.SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Comprehensive test suite for validating the behavior of [BreakableSortedSet] and its various breakable behaviors.
///
/// This test class verifies that the `BreakableSortedSet` class correctly implements programmatically
/// broken [SortedSet] behavior for testing purposes. The `BreakableSortedSet` is designed to
/// simulate faulty or edge-case implementations of the SortedSet interface, enabling thorough testing of
/// code that depends on proper SortedSet behavior.
/// ### Purpose and Use Cases
///
/// The `BreakableSortedSet` is particularly valuable for:
///
///   - Testing error handling in code that uses sorted collections
///   - Validating robustness of algorithms against malformed sorted set behavior
///   - Ensuring collection-dependent code handles navigation method failures gracefully
///   - Testing edge cases in range-based operations and subset functionality
///   - Verifying defensive programming practices in sorted collection-dependent code
///
/// ### Breakable Behaviors Tested
///
/// This test suite validates the following types of breakable behaviors:
/// #### Comparator Breaks
///
///   - `COMPARATOR_ALWAYS_RETURNS_NULL` - Comparator method always returns null
///
/// #### Navigation Breaks
///
///   - `FIRST_ALWAYS_THROWS_EXCEPTION` - first() method always throws NoSuchElementException
///   - `FIRST_RETURNS_LAST_ELEMENT` - first() method returns the last element
///   - `FIRST_SKIPS_FIRST_ELEMENT` - first() method returns the second element
///   - `FIRST_ALWAYS_RETURNS_NULL` - first() method always returns null
///   - `FIRST_RETURNS_NULL_WHEN_EMPTY` - first() method returns null when empty
///   - `LAST_ALWAYS_THROWS_EXCEPTION` - last() method always throws NoSuchElementException
///   - `LAST_RETURNS_FIRST_ELEMENT` - last() method returns the first element
///   - `LAST_SKIPS_LAST_ELEMENT` - last() method returns the second-to-last element
///   - `LAST_ALWAYS_RETURNS_NULL` - last() method always returns null
///   - `LAST_RETURNS_NULL_WHEN_EMPTY` - last() method returns null when empty
///
/// #### Subset Operation Breaks
///
///   - `SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY` - All subset operations return empty sets
///   - `SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION` - All subset operations throw exceptions
///
/// ### Testing Strategy
///
/// Each test method focuses on a specific break behavior and verifies that:
///
///   - The break is correctly applied to the sorted set instance
///   - The broken behavior persists across method calls
///   - The break doesn't affect unrelated sorted set functionality
///   - Edge cases and boundary conditions are handled appropriately
///
///
/// The tests use the [BreakableSortedSet.Builder] pattern to construct sorted sets with specific
/// breaks, then verify the expected broken behavior through direct method calls and assertions.
/// ### Integration with Testing Framework
///
/// These breakable sorted sets are designed to be used in conjunction with collection contract
/// tests to ensure that sorted collection implementations properly handle malformed behavior.
/// This is particularly important for algorithms that depend on sorted set characteristics for
/// correctness and performance.
///
/// Usage example:
/// ```java
/// // Create a sorted set with broken navigation
/// BreakableSortedSet<String> sortedSet = new BreakableSortedSet.Builder<String>()
///         .addBreak(FIRST_ALWAYS_THROWS_EXCEPTION)
///         .build();
/// sortedSet.addAll(Arrays.asList("a", "b", "c"));
/// assertThrows(NoSuchElementException.class, () -> sortedSet.first()); // Should throw despite having elements
/// ```
///
/// @author evanbergstrom
/// @see BreakableSortedSet
/// @see BreakableSortedSet.Builder
/// @see SortedSet
/// @since 1.0
@DisplayName("Tests for BreakableSortedSet")
public class BreakableSortedSetTest {

    /// Default constructor.
    public BreakableSortedSetTest() {
        super();
    }

    /// Tests the `COMPARATOR_ALWAYS_RETURNS_NULL` break behavior.
    ///
    /// Verifies that when this break is applied, the `comparator()` method
    /// always returns `null`, even when a custom comparator was provided during
    /// construction. This simulates a sorted set that loses track of its ordering comparator.
    ///
    /// The test confirms that despite having a comparator configured, the broken
    /// behavior overrides the normal comparator access, which is useful for testing
    /// code that must handle unknown ordering scenarios.
    @Test
    @DisplayName("comparator() returns null with COMPARATOR_ALWAYS_RETURNS_NULL, even with a comparator set")
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

    /// Tests the `FIRST_ALWAYS_THROWS_EXCEPTION` break behavior.
    ///
    /// Verifies that when this break is applied, the `first()` method always
    /// throws [NoSuchElementException], even when elements exist in the set.
    /// This simulates a navigation failure in accessing the first element.
    ///
    /// The test confirms that the broken behavior persists regardless of set contents,
    /// which is useful for testing error handling in code that accesses the first element.
    @Test
    @DisplayName("first() throws NoSuchElementException with FIRST_ALWAYS_THROWS_EXCEPTION, even when not empty")
    public void testFirstAlwaysThrowsException() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(FIRST_ALWAYS_THROWS_EXCEPTION)
                .build();

        // Should throw even with elements present
        brokenSet.addAll(Arrays.asList(3, 1, 2));
        assertFalse(brokenSet.isEmpty());
        assertThrows(NoSuchElementException.class, brokenSet::first);

        // Verify normal behavior without break
        BreakableSortedSet<Integer> normalSet = new BreakableSortedSet<>();
        normalSet.addAll(Arrays.asList(3, 1, 2));
        assertEquals(Integer.valueOf(1), normalSet.first()); // Should return lowest element

        // Verify normal exception for empty set
        BreakableSortedSet<Integer> emptySet = new BreakableSortedSet<>();
        assertThrows(NoSuchElementException.class, emptySet::first);
    }

    /// Tests the `FIRST_RETURNS_LAST_ELEMENT` break behavior.
    ///
    /// Verifies that when this break is applied, the `first()` method returns
    /// the last element instead of the first element. This simulates a navigation confusion
    /// where first and last are swapped.
    @Test
    @DisplayName("first() returns the last element with FIRST_RETURNS_LAST_ELEMENT")
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

    /// Tests the `FIRST_SKIPS_FIRST_ELEMENT` break behavior.
    ///
    /// Verifies that when this break is applied, the `first()` method returns
    /// the second element instead of the actual first element. This simulates a navigation
    /// error where the iterator skips the first position.
    @Test
    @DisplayName("first() returns the second element with FIRST_SKIPS_FIRST_ELEMENT")
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
        assertThrows(NoSuchElementException.class, singleElementSet::first);

        // With empty set, should throw exception
        BreakableSortedSet<Integer> emptySet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(FIRST_SKIPS_FIRST_ELEMENT)
                .build();
        assertThrows(NoSuchElementException.class, emptySet::first);
    }

    /// Tests the `FIRST_ALWAYS_RETURNS_NULL` break behavior.
    ///
    /// Verifies that when this break is applied, the `first()` method always
    /// returns `null`, even when elements exist. This simulates a navigation
    /// failure where the first element cannot be accessed.
    @Test
    @DisplayName("first() returns null with FIRST_ALWAYS_RETURNS_NULL, even when not empty")
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

    /// Tests the `FIRST_RETURNS_NULL_WHEN_EMPTY` break behavior.
    ///
    /// Verifies that when this break is applied, the `first()` method returns
    /// `null` when the set is empty, instead of throwing [NoSuchElementException].
    /// This simulates graceful degradation for empty set access.
    @Test
    @DisplayName("first() returns null for an empty set with FIRST_RETURNS_NULL_WHEN_EMPTY")
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

    /// Tests the `LAST_ALWAYS_THROWS_EXCEPTION` break behavior.
    ///
    /// Verifies that when this break is applied, the `last()` method always
    /// throws [NoSuchElementException], even when elements exist in the set.
    /// This simulates a navigation failure in accessing the last element.
    ///
    /// The test confirms that the broken behavior persists regardless of set contents,
    /// which is useful for testing error handling in code that accesses the last element.
    @Test
    @DisplayName("last() throws NoSuchElementException with LAST_ALWAYS_THROWS_EXCEPTION, even when not empty")
    public void testLastAlwaysThrowsException() {
        BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
                .addBreak(LAST_ALWAYS_THROWS_EXCEPTION)
                .build();

        // Should throw even with elements present
        brokenSet.addAll(Arrays.asList(3, 1, 2));
        assertFalse(brokenSet.isEmpty());
        assertThrows(NoSuchElementException.class, brokenSet::last);

        // Verify normal behavior without break
        BreakableSortedSet<Integer> normalSet = new BreakableSortedSet<>();
        normalSet.addAll(Arrays.asList(3, 1, 2));
        assertEquals(Integer.valueOf(3), normalSet.last()); // Should return highest element

        // Verify normal exception for empty set
        BreakableSortedSet<Integer> emptySet = new BreakableSortedSet<>();
        assertThrows(NoSuchElementException.class, emptySet::last);
    }

    /// Tests the `LAST_RETURNS_FIRST_ELEMENT` break behavior.
    ///
    /// Verifies that when this break is applied, the `last()` method returns
    /// the first element instead of the last element. This simulates a navigation confusion
    /// where first and last are swapped for the last() method.
    @Test
    @DisplayName("last() returns the first element with LAST_RETURNS_FIRST_ELEMENT")
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

    /// Tests the `LAST_SKIPS_LAST_ELEMENT` break behavior.
    ///
    /// Verifies that when this break is applied, the `last()` method returns
    /// the second-to-last element instead of the actual last element. This simulates a navigation
    /// error where the iterator misses the last position.
    @Test
    @DisplayName("last() returns the second-to-last element with LAST_SKIPS_LAST_ELEMENT")
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

    /// Tests the `LAST_ALWAYS_RETURNS_NULL` break behavior.
    ///
    /// Verifies that when this break is applied, the `last()` method always
    /// returns `null`, even when elements exist. This simulates a navigation
    /// failure where the last element cannot be accessed.
    @Test
    @DisplayName("last() returns null with LAST_ALWAYS_RETURNS_NULL, even when not empty")
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

    /// Tests the `LAST_RETURNS_NULL_WHEN_EMPTY` break behavior.
    ///
    /// Verifies that when this break is applied, the `last()` method returns
    /// `null` when the set is empty, instead of throwing [NoSuchElementException].
    /// This simulates graceful degradation for empty set access.
    @Test
    @DisplayName("last() returns null for an empty set with LAST_RETURNS_NULL_WHEN_EMPTY")
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

    /// Tests the `SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY` break behavior.
    ///
    /// Verifies that when this break is applied, all subset operations (`subSet`,
    /// `headSet`, `tailSet`) always return empty sets regardless of the
    /// parameters provided or the actual contents of the set.
    ///
    /// This simulates subset operations that fail to identify ranges properly,
    /// which is useful for testing code that depends on subset functionality and
    /// must handle empty results gracefully.
    @Test
    @DisplayName("subSet, headSet and tailSet return empty sets with SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY")
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

    /// Tests the `SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION` break behavior.
    ///
    /// Verifies that when this break is applied, all subset operations (`subSet`,
    /// `headSet`, `tailSet`) always throw [IllegalArgumentException]
    /// regardless of the parameters provided.
    ///
    /// This simulates subset operations that reject all range specifications as invalid,
    /// which is useful for testing error handling in range-based operations.
    @Test
    @DisplayName("subSet, headSet and tailSet throw with SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION")
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

    /// Tests the normal behavior of subset operations without breaks.
    ///
    /// This test validates the baseline behavior by verifying that subset operations
    /// work correctly when no breaks are applied. It serves as a control test to ensure
    /// that the break behaviors deviate from the expected normal operation.
    @Test
    @DisplayName("subSet, headSet and tailSet return the right ranges with no break")
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

    /// Tests the builder functionality with comparator configuration.
    ///
    /// Verifies that the builder correctly handles comparator configuration and
    /// that the resulting sorted set uses the specified comparator for ordering.
    /// This test also ensures that comparator changes preserve existing elements.
    @Test
    @DisplayName("the builder's comparator orders the set and keeps its elements")
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

    /// Tests the builder copy constructor functionality.
    ///
    /// Verifies that the builder copy constructor creates independent builders
    /// that can be configured separately while preserving the original configuration
    /// as a starting point.
    @Test
    @DisplayName("a builder copied from another can be configured independently")
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
        assertThrows(NoSuchElementException.class, copySet::first);
    }

    /// Tests the integration of multiple breaks working together.
    ///
    /// Verifies that multiple breaks can be applied simultaneously and that
    /// each break affects only its intended functionality while leaving other
    /// operations working normally.
    @Test
    @DisplayName("several breaks together each change only their own method")
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
        assertThrows(NoSuchElementException.class, multiBreakSet::first);

        // Subset break should be active
        assertTrue(multiBreakSet.subSet("apple", "cherry").isEmpty());

        // Last should still work normally (no break applied)
        assertEquals("cherry", multiBreakSet.last());

        // Basic set operations should still work
        assertTrue(multiBreakSet.contains("banana"));
        assertEquals(3, multiBreakSet.size());
    }

    /// Tests the normal sorted set behavior without any breaks applied.
    ///
    /// This test serves as a comprehensive control test, verifying that
    /// BreakableSortedSet behaves like a standard SortedSet when no breaks
    /// are active. This ensures the implementation correctly delegates to
    /// the underlying sorted set for normal operations.
    @Test
    @DisplayName("with no break, the set behaves as a standard SortedSet")
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

    /// Tests error handling in normal operations for edge cases.
    ///
    /// Verifies that BreakableSortedSet properly handles edge cases and
    /// error conditions according to the SortedSet contract, such as accessing
    /// elements from empty sets or invalid range parameters.
    @Test
    @DisplayName("with no break, empty sets and invalid ranges throw as SortedSet specifies")
    public void testEdgeCasesAndErrorHandling() {
        BreakableSortedSet<String> emptySet = new BreakableSortedSet<>();

        // Empty set operations should throw appropriate exceptions
        assertThrows(NoSuchElementException.class, emptySet::first);
        assertThrows(NoSuchElementException.class, emptySet::last);

        // Test invalid range parameters
        BreakableSortedSet<String> populatedSet = new BreakableSortedSet<>();
        populatedSet.addAll(Arrays.asList("a", "b", "c", "d"));

        // Invalid range (from > to) should throw exception
        assertThrows(IllegalArgumentException.class, () -> populatedSet.subSet("d", "a"));
    }

    // ========== addAll with elements already present (#19) ==========

    /// Tests that `addAll` adds the new elements of a batch that also holds elements already in the set, and returns
    /// `true`, as [java.util.Set#addAll] does: the present ones leave the set unchanged.
    @Test
    @DisplayName("addAll(Collection) adds the new elements of a batch that holds present ones, and returns true")
    public void addAllWithPresentAndNewElementsAddsTheNewOnes() {
        BreakableSortedSet<Integer> set = new BreakableSortedSet.Builder<Integer>().addElements(1).build();

        assertTrue(set.addAll(List.of(2, 3, 1)));
        assertEquals(Set.of(1, 2, 3), set);
    }

    /// Tests that `addAll` returns `false`, and leaves the set unchanged, when every element of the batch is already
    /// in the set.
    @Test
    @DisplayName("addAll(Collection) returns false when every element of the batch is present")
    public void addAllWithOnlyPresentElementsReturnsFalse() {
        BreakableSortedSet<Integer> set = new BreakableSortedSet.Builder<Integer>().addElements(1, 2).build();

        assertFalse(set.addAll(List.of(2, 1)));
        assertEquals(Set.of(1, 2), set);
    }
}
