/*
 * Copyright 2024 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/// **Breakable SortedSet Implementation for Testing**
///
/// This class extends `BreakableSet` to provide a SortedSet implementation that can be
/// programmatically broken for comprehensive testing scenarios. It maintains sorted set semantics
/// (ordered, unique elements) while allowing controlled introduction of various failure modes
/// and behavioral anomalies specifically related to sorted collections.
///
/// ## Core Functionality
///
/// As a SortedSet implementation, this class enforces both uniqueness and ordering constraints
/// by default but can be configured to break these fundamental contracts through specific
/// breaks. This allows testing of code that must handle corrupted or non-compliant SortedSet instances.
///
/// ### SortedSet Semantics
///
/// Under normal operation (no breaks active), BreakableSortedSet maintains proper SortedSet behavior:
/// - **Ordering**: Elements are maintained in sorted order according to their natural ordering or provided comparator
/// - **Uniqueness**: Elements are added only if not already present (inherited from Set)
/// - **Navigation**: Provides methods for accessing first, last, and subsets of elements
/// - **Comparator Access**: Provides access to the comparator used for ordering (if any)
///
/// ### Breakable Behavior
///
/// The class introduces several SortedSet-specific breaks that can be used to simulate
/// various failure modes:
///
/// ## Available Breaks
///
/// ### Core Navigation Breaks
///
/// #### COMPARATOR_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `comparator()` method to always return null, even when a custom comparator was provided
/// **Effect**: The method returns null regardless of actual comparator configuration
/// **Use Case**: Testing code that depends on comparator availability and must handle unknown ordering
///
/// #### FIRST_ALWAYS_THROWS_EXCEPTION
/// **Purpose**: Forces `first()` method to always throw NoSuchElementException, even when elements exist
/// **Effect**: The method throws an exception regardless of set contents
/// **Use Case**: Testing error handling in code that accesses the first element
///
/// #### LAST_ALWAYS_THROWS_EXCEPTION
/// **Purpose**: Forces `last()` method to always throw NoSuchElementException, even when elements exist
/// **Effect**: The method throws an exception regardless of set contents
/// **Use Case**: Testing error handling in code that accesses the last element
///
/// ### Navigation Confusion Breaks
///
/// #### FIRST_RETURNS_LAST_ELEMENT
/// **Purpose**: Forces `first()` method to return the last element instead of the first
/// **Effect**: Navigation order is inverted for first() access only
/// **Use Case**: Testing code robustness when sorted order appears corrupted
///
/// #### LAST_RETURNS_FIRST_ELEMENT
/// **Purpose**: Forces `last()` method to return the first element instead of the last
/// **Effect**: Navigation order is inverted for last() access only
/// **Use Case**: Testing code robustness when sorted order appears corrupted
///
/// #### FIRST_SKIPS_FIRST_ELEMENT
/// **Purpose**: Forces `first()` method to skip the actual first element and return the second
/// **Effect**: First element becomes inaccessible via first() method
/// **Use Case**: Testing code handling of missing or skipped elements
///
/// #### LAST_SKIPS_LAST_ELEMENT
/// **Purpose**: Forces `last()` method to skip the actual last element and return the second-to-last
/// **Effect**: Last element becomes inaccessible via last() method
/// **Use Case**: Testing code handling of missing or skipped elements
///
/// ### Contract Violation Breaks
///
/// #### FIRST_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `first()` method to always return null, violating the SortedSet contract
/// **Effect**: Returns null instead of elements or proper exceptions
/// **Use Case**: Testing code handling of contract violations and unexpected null values
///
/// #### LAST_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `last()` method to always return null, violating the SortedSet contract
/// **Effect**: Returns null instead of elements or proper exceptions
/// **Use Case**: Testing code handling of contract violations and unexpected null values
///
/// #### FIRST_RETURNS_NULL_WHEN_EMPTY
/// **Purpose**: Forces `first()` method to return null when empty instead of throwing NoSuchElementException
/// **Effect**: Violates exception contract for empty sets
/// **Use Case**: Testing code handling of incorrect error behavior
///
/// #### LAST_RETURNS_NULL_WHEN_EMPTY
/// **Purpose**: Forces `last()` method to return null when empty instead of throwing NoSuchElementException
/// **Effect**: Violates exception contract for empty sets
/// **Use Case**: Testing code handling of incorrect error behavior
///
/// ### Subset Operation Breaks
///
/// #### SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY
/// **Purpose**: Forces subset operations (`subSet`, `headSet`, `tailSet`) to always return empty sets
/// **Effect**: All subset operations return empty collections regardless of parameters
/// **Use Case**: Testing code that depends on subset functionality and must handle empty results
///
/// #### SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION
/// **Purpose**: Forces subset operations to always throw IllegalArgumentException
/// **Effect**: All subset operations throw exceptions regardless of valid parameters
/// **Use Case**: Testing error handling in range-based operations
///
/// ```java
/// BreakableSortedSet<String> set = new BreakableSortedSet.Builder<String>()
///     .withBreak(BreakableSortedSet.FIRST_ALWAYS_THROWS_EXCEPTION)
///     .build();
///
/// set.add("element");
/// assertThrows(NoSuchElementException.class, () -> set.first()); // Throws despite having elements
/// ```
///
/// ## Builder Pattern
///
/// The class provides a comprehensive Builder for constructing BreakableSortedSet instances:
///
/// ### Basic Usage
/// ```java
/// // Create a standard breakable sorted set
/// BreakableSortedSet<Integer> set = new BreakableSortedSet.Builder<Integer>()
///     .build();
///
/// // Create a sorted set with specific comparator and breaks
/// BreakableSortedSet<String> brokenSet = new BreakableSortedSet.Builder<String>()
///     .withComparator(String.CASE_INSENSITIVE_ORDER)
///     .withBreak(COMPARATOR_ALWAYS_RETURNS_NULL)
///     .build();
/// ```
///
/// ### Advanced Configuration
/// ```java
/// // Create a sorted set with custom backing collection and multiple breaks
/// SortedSet<Integer> backingSet = new TreeSet<>();
/// BreakableSortedSet<Integer> customSet = new BreakableSortedSet.Builder<>(backingSet)
///     .withBreak(FIRST_ALWAYS_THROWS_EXCEPTION)
///     .withBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)
///     .build();
/// ```
///
/// ## Testing Applications
///
/// ### Comparator Dependency Testing
/// Test that code properly handles missing comparator information:
///
/// ```java
/// @Test
/// void testComparatorHandling() {
///     BreakableSortedSet<String> set = new BreakableSortedSet.Builder<String>()
///         .withComparator(String.CASE_INSENSITIVE_ORDER)
///         .withBreak(COMPARATOR_ALWAYS_RETURNS_NULL)
///         .build();
///
///     assertNull(set.comparator()); // Returns null despite having comparator
///     // Test that code handles unknown ordering gracefully
/// }
/// ```
///
/// ### Navigation Method Error Handling
/// Verify robust error handling when navigation methods fail:
///
/// ```java
/// @Test
/// void testNavigationErrors() {
///     BreakableSortedSet<Integer> set = new BreakableSortedSet.Builder<Integer>()
///         .withBreak(FIRST_ALWAYS_THROWS_EXCEPTION)
///         .build();
///
///     set.add(1);
///     assertThrows(NoSuchElementException.class, () -> set.first());
/// }
/// ```
///
/// ### Navigation Order Confusion Testing
/// Test handling of corrupted navigation order:
///
/// ```java
/// @Test
/// void testNavigationOrderConfusion() {
///     BreakableSortedSet<Integer> set = new BreakableSortedSet.Builder<Integer>()
///         .withBreak(FIRST_RETURNS_LAST_ELEMENT)
///         .withBreak(LAST_RETURNS_FIRST_ELEMENT)
///         .build();
///
///     set.addAll(Arrays.asList(1, 2, 3, 4, 5));
///
///     assertEquals(5, set.first()); // Returns last element instead
///     assertEquals(1, set.last());  // Returns first element instead
///     // Test that code handles inverted navigation
/// }
/// ```
///
/// ### Element Skipping Testing
/// Test handling when navigation methods skip elements:
///
/// ```java
/// @Test
/// void testElementSkipping() {
///     BreakableSortedSet<String> set = new BreakableSortedSet.Builder<String>()
///         .withBreak(FIRST_SKIPS_FIRST_ELEMENT)
///         .withBreak(LAST_SKIPS_LAST_ELEMENT)
///         .build();
///
///     set.addAll(Arrays.asList("a", "b", "c", "d", "e"));
///
///     assertEquals("b", set.first()); // Skips "a"
///     assertEquals("d", set.last());  // Skips "e"
///     assertTrue(set.contains("a"));  // But elements still exist
///     assertTrue(set.contains("e"));
/// }
/// ```
///
/// ### Contract Violation Testing
/// Test handling of SortedSet contract violations:
///
/// ```java
/// @Test
/// void testContractViolations() {
///     BreakableSortedSet<Integer> set = new BreakableSortedSet.Builder<Integer>()
///         .withBreak(FIRST_ALWAYS_RETURNS_NULL)
///         .withBreak(LAST_ALWAYS_RETURNS_NULL)
///         .build();
///
///     set.addAll(Arrays.asList(1, 2, 3));
///
///     assertNull(set.first()); // Contract violation: should return element or throw
///     assertNull(set.last());  // Contract violation: should return element or throw
///     assertEquals(3, set.size()); // But set still contains elements
/// }
/// ```
///
/// ### Empty Set Error Handling Testing
/// Test handling of incorrect empty set behavior:
///
/// ```java
/// @Test
/// void testEmptySetErrorHandling() {
///     BreakableSortedSet<String> set = new BreakableSortedSet.Builder<String>()
///         .withBreak(FIRST_RETURNS_NULL_WHEN_EMPTY)
///         .withBreak(LAST_RETURNS_NULL_WHEN_EMPTY)
///         .build();
///
///     assertNull(set.first()); // Should throw NoSuchElementException but returns null
///     assertNull(set.last());  // Should throw NoSuchElementException but returns null
///
///     // Test that code handles incorrect error behavior
///     assertThrows(SomeExpectedException.class, () -> processSortedSet(set));
/// }
/// ```
///
/// ### Subset Operation Testing
/// Test handling of subset operation failures:
///
/// ```java
/// @Test
/// void testSubsetOperations() {
///     BreakableSortedSet<String> set = new BreakableSortedSet.Builder<String>()
///         .withBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)
///         .build();
///
///     set.addAll(Arrays.asList("a", "b", "c", "d"));
///     assertTrue(set.subSet("b", "d").isEmpty()); // Returns empty despite valid range
///     assertTrue(set.headSet("c").isEmpty());     // All subset operations broken
///     assertTrue(set.tailSet("b").isEmpty());
/// }
/// ```
///
/// ### Subset Operation Exception Testing
/// Test handling when subset operations always throw exceptions:
///
/// ```java
/// @Test
/// void testSubsetOperationExceptions() {
///     BreakableSortedSet<Integer> set = new BreakableSortedSet.Builder<Integer>()
///         .withBreak(SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION)
///         .build();
///
///     set.addAll(Arrays.asList(1, 2, 3, 4, 5));
///
///     assertThrows(IllegalArgumentException.class, () -> set.subSet(2, 4));
///     assertThrows(IllegalArgumentException.class, () -> set.headSet(3));
///     assertThrows(IllegalArgumentException.class, () -> set.tailSet(2));
/// }
/// ```
///
/// ## Design Considerations
///
/// ### Thread Safety
/// This class is not thread-safe. The underlying collection's thread safety characteristics
/// determine the overall thread safety behavior.
///
/// ### Performance
/// - **Add Operations**: Depends on backing SortedSet implementation (typically O(log n))
/// - **Navigation**: O(1) for first/last operations when not broken
/// - **Subset Operations**: Depends on backing implementation and break configuration
///
/// ### Backing Collection
/// The implementation uses a configurable backing SortedSet (default: TreeSet) which
/// affects performance characteristics, ordering behavior, and comparator handling.
///
/// @param <E> the type of elements maintained by this sorted set
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableSet
/// @see SortedSet
/// @see java.util.TreeSet
public class BreakableSortedSet<E> extends BreakableSet<E> implements SortedSet<E> {

    /// The backing sorted set that stores the elements in sorted order.
    private final SortedSet<E> sortedSet;

    /// Break that forces the comparator() method to always return null, even when a comparator was provided.
    /// This simulates a sorted set that loses track of its ordering comparator.
    public static final Break COMPARATOR_ALWAYS_RETURNS_NULL =
        new Break("Comparator method always returns null");

    /// Break that forces the first() method to always throw NoSuchElementException, even when elements exist.
    /// This simulates a navigation failure in accessing the first element.
    public static final Break FIRST_ALWAYS_THROWS_EXCEPTION =
        new Break("First method always throws NoSuchElementException");

    /// Break that forces the first() method to return the last element instead of the first.
    /// This simulates a navigation confusion where the sorted order is inverted for first() access.
    public static final Break FIRST_RETURNS_LAST_ELEMENT =
            new Break("First method returns the last element");

    /// Break that forces the first() method to skip the actual first element and return the second element.
    /// This simulates a navigation failure where the first element is skipped or inaccessible.
    /// Throws NoSuchElementException if the set has fewer than 2 elements.
    public static final Break FIRST_SKIPS_FIRST_ELEMENT =
            new Break("First method skips the first element.");

    /// Break that forces the first() method to always return null, even when elements exist.
    /// This simulates a navigation failure where the first element cannot be retrieved.
    /// Note: This violates the SortedSet contract as first() should never return null.
    public static final Break FIRST_ALWAYS_RETURNS_NULL =
            new Break("First method always returns null");

    /// Break that forces the first() method to return null when the set is empty, instead of throwing NoSuchElementException.
    /// This simulates incorrect error handling where null is returned instead of the proper exception.
    /// Note: This violates the SortedSet contract which requires NoSuchElementException for empty sets.
    public static final Break FIRST_RETURNS_NULL_WHEN_EMPTY =
            new Break("First method returns null when empty");

    /// Break that forces the last() method to always throw NoSuchElementException, even when elements exist.
    /// This simulates a navigation failure in accessing the last element.
    public static final Break LAST_ALWAYS_THROWS_EXCEPTION =
        new Break("Last method always throws NoSuchElementException");

    /// Break that forces the last() method to return the first element instead of the last.
    /// This simulates a navigation confusion where the sorted order is inverted for last() access.
    public static final Break LAST_RETURNS_FIRST_ELEMENT =
            new Break("Last method returns the first element");

    /// Break that forces the last() method to skip the actual last element and return the second-to-last element.
    /// This simulates a navigation failure where the last element is skipped or inaccessible.
    /// Throws NoSuchElementException if the set has fewer than 2 elements.
    public static final Break LAST_SKIPS_LAST_ELEMENT =
            new Break("Last method skips the last element.");

    /// Break that forces the last() method to always return null, even when elements exist.
    /// This simulates a navigation failure where the last element cannot be retrieved.
    /// Note: This violates the SortedSet contract as last() should never return null.
    public static final Break LAST_ALWAYS_RETURNS_NULL =
            new Break("Last always returns null.");

    /// Break that forces the last() method to return null when the set is empty, instead of throwing NoSuchElementException.
    /// This simulates incorrect error handling where null is returned instead of the proper exception.
    /// Note: This violates the SortedSet contract which requires NoSuchElementException for empty sets.
    public static final Break LAST_RETURNS_NULL_WHEN_EMPTY =
            new Break("Last method returns null when empty");

    /// Break that forces all subset operations to return empty sets, regardless of parameters.
    /// This simulates subset operations that fail to return proper ranges.
    public static final Break SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY =
        new Break("Subset operations always return empty sets");

    /// Break that forces all subset operations to throw IllegalArgumentException.
    /// This simulates subset operations that reject all parameters as invalid.
    public static final Break SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION =
        new Break("Subset operations always throw IllegalArgumentException");

    /// Creates a new empty BreakableSortedSet with default configuration.
    ///
    /// The sorted set uses a TreeSet as the backing collection and has no breaks active.
    /// Elements will be ordered according to their natural ordering.
    ///
    /// ```java
    /// BreakableSortedSet<String> set = new BreakableSortedSet<>();
    /// set.add("zebra");
    /// set.add("apple");
    /// assertEquals("apple", set.first()); // Elements are sorted
    /// ```
    public BreakableSortedSet() {
        this(new TreeSet<>(), new ArrayList<>(), 0);
    }

    /// Creates a new BreakableSortedSet with the specified comparator.
    ///
    /// The sorted set uses a TreeSet with the provided comparator as the backing collection
    /// and has no breaks active. Elements will be ordered according to the comparator.
    ///
    /// ```java
    /// BreakableSortedSet<String> set = new BreakableSortedSet<>(String.CASE_INSENSITIVE_ORDER);
    /// set.add("Zebra");
    /// set.add("apple");
    /// assertEquals("apple", set.first()); // Case-insensitive ordering
    /// ```
    ///
    /// @param comparator the comparator that will be used to order this set
    public BreakableSortedSet(final @Nullable Comparator<? super E> comparator) {
        this(new TreeSet<>(comparator), new ArrayList<>(), 0);
    }

    /// Creates a new BreakableSortedSet as a copy of another BreakableSortedSet.
    ///
    /// This constructor creates a new sorted set that shares the same backing collection
    /// reference as the source set but starts with no breaks active.
    ///
    /// @param other the BreakableSortedSet to copy from
    /// @throws NullPointerException if other is null
    public BreakableSortedSet(final @NonNull BreakableSortedSet<E> other) {
        this(other.sortedSet, new ArrayList<>(), 0);
    }

    /// Creates a new BreakableSortedSet using the specified sorted set as the backing store.
    ///
    /// This constructor allows you to specify the underlying SortedSet implementation,
    /// which affects performance characteristics and ordering behavior.
    ///
    /// @param sortedSet the backing sorted set to use for element storage
    /// @throws NullPointerException if sortedSet is null
    public BreakableSortedSet(final @NonNull SortedSet<E> sortedSet) {
        this(sortedSet, new ArrayList<>(), 0);
    }

    /// Creates a new BreakableSortedSet with full configuration control.
    ///
    /// This constructor provides complete control over the sorted set's configuration,
    /// including the backing sorted set, active breaks, and behavioral characteristics.
    ///
    /// @param sortedSet the backing sorted set for element storage
    /// @param breaks the collection of breaks to activate
    /// @param characteristics the spliterator characteristics
    /// @throws NullPointerException if sortedSet or breaks is null
    public BreakableSortedSet(final @NonNull SortedSet<E> sortedSet,
                             final @NonNull Collection<Break> breaks,
                             final int characteristics) {
        super(sortedSet, breaks, characteristics);
        this.sortedSet = Objects.requireNonNull(sortedSet);
    }

    /// Returns the comparator used to order the elements in this set.
    ///
    /// Under normal operation, this method returns the comparator used by the backing
    /// SortedSet, or null if the set uses natural ordering. However, when the
    /// `COMPARATOR_ALWAYS_RETURNS_NULL` break is active, it always returns null
    /// regardless of the actual comparator configuration.
    ///
    /// ### Normal Behavior
    /// - Returns the actual comparator used for ordering
    /// - Returns null if natural ordering is used
    ///
    /// ### With COMPARATOR_ALWAYS_RETURNS_NULL Break
    /// - Always returns null regardless of actual comparator
    /// - Simulates loss of comparator information
    ///
    /// ```java
    /// BreakableSortedSet<String> normalSet = new BreakableSortedSet<>(String.CASE_INSENSITIVE_ORDER);
    /// assertEquals(String.CASE_INSENSITIVE_ORDER, normalSet.comparator());
    ///
    /// BreakableSortedSet<String> brokenSet = new BreakableSortedSet.Builder<String>()
    ///     .withComparator(String.CASE_INSENSITIVE_ORDER)
    ///     .withBreak(COMPARATOR_ALWAYS_RETURNS_NULL)
    ///     .build();
    /// assertNull(brokenSet.comparator()); // Returns null despite having comparator
    /// ```
    ///
    /// @return the comparator used to order elements, or null if natural ordering is used
    ///         (or if the COMPARATOR_ALWAYS_RETURNS_NULL break is active)
    @Override
    public Comparator<? super E> comparator() {
        if (hasBreak(COMPARATOR_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        return sortedSet.comparator();
    }

    /// Returns a view of the portion of this set whose elements range from fromElement to toElement.
    ///
    /// Under normal operation, this method returns a SortedSet view containing elements
    /// in the specified range. However, various breaks can alter this behavior to simulate
    /// different failure modes.
    ///
    /// ### Normal Behavior
    /// - Returns elements where fromElement <= element < toElement
    /// - The returned set is backed by this set, so changes are reflected
    /// - Throws IllegalArgumentException if fromElement > toElement
    ///
    /// ### With SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY Break
    /// - Always returns an empty SortedSet regardless of parameters
    /// - Simulates subset operations that fail to identify ranges
    ///
    /// ### With SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION Break
    /// - Always throws IllegalArgumentException regardless of parameters
    /// - Simulates subset operations that reject all range specifications
    ///
    /// ```java
    /// BreakableSortedSet<Integer> set = new BreakableSortedSet<>();
    /// set.addAll(Arrays.asList(1, 2, 3, 4, 5));
    /// assertEquals(2, set.subSet(2, 4).size()); // Normal: returns {2, 3}
    ///
    /// BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
    ///     .withBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)
    ///     .build();
    /// brokenSet.addAll(Arrays.asList(1, 2, 3, 4, 5));
    /// assertTrue(brokenSet.subSet(2, 4).isEmpty()); // Broken: always empty
    /// ```
    ///
    /// @param fromElement low endpoint (inclusive) of the returned set
    /// @param toElement high endpoint (exclusive) of the returned set
    /// @return a view of the specified range within this set
    /// @throws ClassCastException if fromElement and toElement cannot be compared using this set's comparator
    /// @throws NullPointerException if fromElement or toElement is null and this set does not permit nulls
    /// @throws IllegalArgumentException if fromElement is greater than toElement, or if this set itself
    ///         has a restricted range and fromElement or toElement lies outside the bounds of the range
    ///         (or if SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION break is active)
    @Override
    public @NonNull SortedSet<E> subSet(final E fromElement, final E toElement) {
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION)) {
            throw new IllegalArgumentException("Subset operations are broken");
        }
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)) {
            return new TreeSet<>(sortedSet.comparator());
        }
        return sortedSet.subSet(fromElement, toElement);
    }

    /// Returns a view of the portion of this set whose elements are strictly less than toElement.
    ///
    /// Under normal operation, this method returns a SortedSet view containing all elements
    /// less than the specified element. However, breaks can alter this behavior.
    ///
    /// ### Normal Behavior
    /// - Returns elements where element < toElement
    /// - The returned set is backed by this set
    ///
    /// ### With Breaks
    /// - SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY: returns empty set
    /// - SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION: throws IllegalArgumentException
    ///
    /// @param toElement high endpoint (exclusive) of the returned set
    /// @return a view of the portion of this set whose elements are strictly less than toElement
    /// @throws ClassCastException if toElement is not compatible with this set's comparator
    /// @throws NullPointerException if toElement is null and this set does not permit nulls
    /// @throws IllegalArgumentException if this set itself has a restricted range and toElement lies
    ///         outside the bounds of the range (or if SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION break is active)
    @Override
    public @NonNull SortedSet<E> headSet(final E toElement) {
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION)) {
            throw new IllegalArgumentException("Subset operations are broken");
        }
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)) {
            return new TreeSet<>(sortedSet.comparator());
        }
        return sortedSet.headSet(toElement);
    }

    /// Returns a view of the portion of this set whose elements are greater than or equal to fromElement.
    ///
    /// Under normal operation, this method returns a SortedSet view containing all elements
    /// greater than or equal to the specified element. However, breaks can alter this behavior.
    ///
    /// ### Normal Behavior
    /// - Returns elements where element >= fromElement
    /// - The returned set is backed by this set
    ///
    /// ### With Breaks
    /// - SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY: returns empty set
    /// - SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION: throws IllegalArgumentException
    ///
    /// @param fromElement low endpoint (inclusive) of the returned set
    /// @return a view of the portion of this set whose elements are greater than or equal to fromElement
    /// @throws ClassCastException if fromElement is not compatible with this set's comparator
    /// @throws NullPointerException if fromElement is null and this set does not permit nulls
    /// @throws IllegalArgumentException if this set itself has a restricted range and fromElement lies
    ///         outside the bounds of the range (or if SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION break is active)
    @Override
    public @NonNull SortedSet<E> tailSet(final E fromElement) {
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION)) {
            throw new IllegalArgumentException("Subset operations are broken");
        }
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)) {
            return new TreeSet<>(sortedSet.comparator());
        }
        return sortedSet.tailSet(fromElement);
    }

    /// Returns the first (lowest) element currently in this set.
    ///
    /// Under normal operation, this method returns the first element according to the
    /// set's ordering. However, when the `FIRST_ALWAYS_THROWS_EXCEPTION` break is active,
    /// it always throws an exception regardless of set contents.
    ///
    /// ### Normal Behavior
    /// - Returns the lowest element in the set
    /// - Throws NoSuchElementException if the set is empty
    ///
    /// ### With FIRST_ALWAYS_THROWS_EXCEPTION Break
    /// - Always throws NoSuchElementException, even when elements exist
    /// - Simulates navigation failure in accessing the first element
    ///
    /// ```java
    /// BreakableSortedSet<Integer> normalSet = new BreakableSortedSet<>();
    /// normalSet.addAll(Arrays.asList(3, 1, 2));
    /// assertEquals(Integer.valueOf(1), normalSet.first()); // Returns lowest element
    ///
    /// BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
    ///     .withBreak(FIRST_ALWAYS_THROWS_EXCEPTION)
    ///     .build();
    /// brokenSet.add(1);
    /// assertThrows(NoSuchElementException.class, () -> brokenSet.first()); // Throws despite having elements
    /// ```
    ///
    /// @return the first (lowest) element currently in this set
    /// @throws NoSuchElementException if this set is empty (or if FIRST_ALWAYS_THROWS_EXCEPTION break is active)
    @Override
    public E first() {
        if (hasBreak(FIRST_ALWAYS_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("First element access is broken");
        } else if (hasBreak(FIRST_ALWAYS_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(FIRST_RETURNS_LAST_ELEMENT)) {
            return sortedSet.last();
        } else if (hasBreak(FIRST_SKIPS_FIRST_ELEMENT)) {
            if (sortedSet.size() < 2) {
                throw new NoSuchElementException("First element access is broken");
            } else {
                Iterator<E> iter = sortedSet.iterator();
                iter.next();
                return iter.next();
            }
        } else if (sortedSet.isEmpty() && hasBreak(FIRST_RETURNS_NULL_WHEN_EMPTY)) {
            return null;
        } else {
            return sortedSet.first();
        }
    }

    /// Returns the last (highest) element currently in this set.
    ///
    /// Under normal operation, this method returns the last element according to the
    /// set's ordering. However, when the `LAST_ALWAYS_THROWS_EXCEPTION` break is active,
    /// it always throws an exception regardless of set contents.
    ///
    /// ### Normal Behavior
    /// - Returns the highest element in the set
    /// - Throws NoSuchElementException if the set is empty
    ///
    /// ### With LAST_ALWAYS_THROWS_EXCEPTION Break
    /// - Always throws NoSuchElementException, even when elements exist
    /// - Simulates navigation failure in accessing the last element
    ///
    /// @return the last (highest) element currently in this set
    /// @throws NoSuchElementException if this set is empty (or if LAST_ALWAYS_THROWS_EXCEPTION break is active)
    @Override
    public E last() {
        if (hasBreak(LAST_ALWAYS_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("Last element access is broken");
        } else if (hasBreak(LAST_ALWAYS_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(LAST_RETURNS_FIRST_ELEMENT)) {
            return sortedSet.first();
        } else if (hasBreak(LAST_SKIPS_LAST_ELEMENT)) {
            if (sortedSet.size() < 2) {
                throw new NoSuchElementException("last element access is broken");
            } else {
                Iterator<E> iter = sortedSet.reversed().iterator();
                iter.next();
                return iter.next();
            }
        } else if (sortedSet.isEmpty() && hasBreak(LAST_RETURNS_NULL_WHEN_EMPTY)) {
            return null;
        } else {
            return sortedSet.last();
        }
    }

    /// **Abstract Base Builder for BreakableSortedSet Construction**
    ///
    /// This abstract builder class provides the foundational functionality for constructing
    /// BreakableSortedSet implementations and their subclasses. It extends BreakableCollection.AbstractBuilder
    /// to inherit common collection functionality while adding sorted set-specific configuration
    /// capabilities, particularly comparator management and set semantics enforcement.
    ///
    /// ## Core Functionality
    ///
    /// The AbstractBuilder serves as the base for all sorted set builders in the breakable framework:
    /// - **Comparator Management**: Handles element ordering configuration
    /// - **Set Semantics**: Automatically enforces no-duplicates policy
    /// - **Type Safety**: Maintains generic type constraints across the builder hierarchy
    /// - **Configuration Inheritance**: Supports builder copying and extension
    /// - **Fluent Interface**: Enables method chaining for readable configuration
    ///
    /// ## Builder Hierarchy Integration
    ///
    /// This class integrates with the broader builder hierarchy:
    /// ```
    /// BreakableCollection.AbstractBuilder
    ///   └── BreakableSortedSet.AbstractBuilder
    ///       ├── BreakableSortedSet.Builder
    ///       └── BreakableNavigableSet.Builder (extends this pattern)
    /// ```
    ///
    /// ## Key Design Features
    ///
    /// ### Automatic Set Configuration
    /// All constructors automatically call `doesNotPermitDuplicates()` to enforce Set semantics,
    /// ensuring that any BreakableSortedSet built from this builder maintains uniqueness constraints
    /// unless explicitly overridden by breaks.
    ///
    /// ### Comparator Management
    /// The builder manages element ordering through a configurable Comparator:
    /// - **Natural Ordering**: Default behavior when no comparator is specified
    /// - **Custom Ordering**: Support for application-specific sorting logic
    /// - **Null Safety**: Proper handling of null comparator values
    ///
    /// ### Builder Pattern Compliance
    /// Implements the full builder pattern with support for:
    /// - **Method Chaining**: All configuration methods return the builder instance
    /// - **Copy Construction**: Builders can be created from other builders
    /// - **Immutable Building**: Configuration doesn't affect the builder after build()
    ///
    /// ## Constructor Variations
    ///
    /// ### Default Constructor
    /// ```java
    /// // Creates builder with natural ordering and no initial elements
    /// public AbstractBuilder()
    /// ```
    /// - Inherits from BreakableCollection.AbstractBuilder()
    /// - Automatically configures for Set semantics (no duplicates)
    /// - Uses natural ordering (comparator = null)
    ///
    /// ### Comparator Constructor
    /// ```java
    /// // Creates builder with specified comparator and no initial elements
    /// public AbstractBuilder(Comparator<? super E> comparator)
    /// ```
    /// - Initializes with custom element ordering
    /// - Automatically configures for Set semantics
    /// - Validates comparator is non-null
    ///
    /// ### Collection Constructor
    /// ```java
    /// // Creates builder with initial elements using natural ordering
    /// public AbstractBuilder(Collection<E> elements)
    /// ```
    /// - Pre-populates builder with provided elements
    /// - Automatically configures for Set semantics
    /// - Uses natural ordering for element comparison
    ///
    /// ### Copy Constructor
    /// ```java
    /// // Creates builder by copying another builder's configuration
    /// public AbstractBuilder(AbstractBuilder<B, C, E> other)
    /// ```
    /// - Copies all configuration from source builder
    /// - Preserves comparator settings
    /// - Inherits break configuration and element collection
    ///
    /// ## Usage Examples
    ///
    /// ### Basic Builder Extension
    /// ```java
    /// public static class CustomSortedSetBuilder
    ///         extends AbstractBuilder<CustomSortedSetBuilder, CustomSortedSet<String>, String> {
    ///
    ///     @Override
    ///     public CustomSortedSetBuilder self() {
    ///         return this;
    ///     }
    ///
    ///     @Override
    ///     public CustomSortedSet<String> build() {
    ///         TreeSet<String> storage = new TreeSet<>(comparator());
    ///         storage.addAll(elements());
    ///         return new CustomSortedSet<>(storage, breaks(), characteristics());
    ///     }
    /// }
    /// ```
    ///
    /// ### Comparator Configuration
    /// ```java
    /// // Natural ordering (default)
    /// AbstractBuilder<?, ?, String> builder1 = new ConcreteBuilder();
    ///
    /// // Custom ordering
    /// AbstractBuilder<?, ?, String> builder2 = new ConcreteBuilder(String.CASE_INSENSITIVE_ORDER);
    ///
    /// // Runtime comparator configuration
    /// AbstractBuilder<?, ?, String> builder3 = new ConcreteBuilder()
    ///     .withComparator(Collections.reverseOrder());
    /// ```
    ///
    /// ### Builder Copying and Extension
    /// ```java
    /// // Create base configuration
    /// AbstractBuilder<?, ?, Integer> baseBuilder = new ConcreteBuilder()
    ///     .addElements(Arrays.asList(1, 2, 3))
    ///     .withBreak(FIRST_ALWAYS_THROWS_EXCEPTION);
    ///
    /// // Create variant with additional configuration
    /// AbstractBuilder<?, ?, Integer> variantBuilder = new ConcreteBuilder(baseBuilder)
    ///     .withComparator(Collections.reverseOrder())
    ///     .withBreak(LAST_ALWAYS_RETURNS_NULL);
    /// ```
    ///
    /// ## Integration with Breakable Framework
    ///
    /// ### Set Semantics Enforcement
    /// The builder automatically configures built collections to reject duplicates,
    /// maintaining Set interface contracts unless overridden by specific breaks:
    /// ```java
    /// // Automatic duplicate rejection
    /// builder.addElements(Arrays.asList("a", "b", "a")); // Only "a" and "b" stored
    ///
    /// // Unless broken by specific breaks
    /// builder.withBreak(BreakableSet.ADD_RETURNS_TRUE_FOR_DUPLICATES);
    /// ```
    ///
    /// ### Comparator Integration
    /// The builder's comparator configuration directly affects the ordering
    /// behavior of all built sorted sets:
    /// ```java
    /// // Case-sensitive ordering (default)
    /// builder1.withComparator(null); // Natural ordering
    ///
    /// // Case-insensitive ordering
    /// builder2.withComparator(String.CASE_INSENSITIVE_ORDER);
    ///
    /// // Custom business logic ordering
    /// builder3.withComparator(Comparator.comparing(MyClass::getPriority));
    /// ```
    ///
    /// ## Design Considerations
    ///
    /// ### Thread Safety
    /// Builder instances are not thread-safe. Each builder should be used by a single thread
    /// or external synchronization must be provided.
    ///
    /// ### Memory Efficiency
    /// The builder maintains minimal state beyond what's inherited from the parent builder,
    /// storing only the comparator configuration.
    ///
    /// ### Type Safety
    /// The complex generic signature ensures type safety throughout the builder hierarchy
    /// while enabling proper method chaining and polymorphic behavior.
    ///
    /// @param <B> the concrete builder type for fluent method chaining
    /// @param <C> the concrete BreakableSet type being built
    /// @param <E> the element type for the sorted set
    /// @author evanbergstrom
    /// @since 1.0.0
    /// @see BreakableCollection.AbstractBuilder
    /// @see BreakableSortedSet.Builder
    protected abstract static class AbstractBuilder<B extends AbstractBuilder<B, C, E>, C extends BreakableSet<E>, E>
            extends BreakableCollection.AbstractBuilder<B, C, E> {

        /// The comparator used for element ordering in the built sorted set.
        /// A null value indicates natural ordering should be used.
        private Comparator<? super E> comparator;

        /// Creates a new builder with default configuration.
        ///
        /// This constructor initializes the builder with:
        /// - Natural element ordering (no custom comparator)
        /// - No initial elements
        /// - Set semantics (duplicates not permitted)
        /// - No breaks active
        ///
        /// Usage:
        /// ```java
        /// AbstractBuilder<?, ?, String> builder = new ConcreteBuilder();
        /// ```
        public AbstractBuilder() {
            super();
            doesNotPermitDuplicates();
        }

        /// Creates a new builder with a specified comparator for element ordering.
        ///
        /// This constructor initializes the builder with:
        /// - Custom element ordering using the provided comparator
        /// - No initial elements
        /// - Set semantics (duplicates not permitted)
        /// - No breaks active
        ///
        /// Usage:
        /// ```java
        /// AbstractBuilder<?, ?, String> builder = new ConcreteBuilder(String.CASE_INSENSITIVE_ORDER);
        /// ```
        ///
        /// @param comparator the comparator to use for element ordering
        /// @throws NullPointerException if comparator is null
        public AbstractBuilder(final @Nullable Comparator<? super E> comparator) {
            super();
            this.comparator = Objects.requireNonNull(comparator);
            doesNotPermitDuplicates();
        }

        /// Creates a new builder with initial elements using natural ordering.
        ///
        /// This constructor initializes the builder with:
        /// - Natural element ordering (no custom comparator)
        /// - Initial elements from the provided collection
        /// - Set semantics (duplicates not permitted)
        /// - No breaks active
        ///
        /// Usage:
        /// ```java
        /// Collection<String> initial = Arrays.asList("a", "b", "c");
        /// AbstractBuilder<?, ?, String> builder = new ConcreteBuilder(initial);
        /// ```
        ///
        /// @param elements the initial elements for the builder
        /// @throws NullPointerException if elements is null
        public AbstractBuilder(final @NonNull Collection<E> elements) {
            super(elements);
            doesNotPermitDuplicates();
        }

        /// Creates a new builder by copying configuration from another builder.
        ///
        /// This copy constructor creates a new builder that inherits all configuration
        /// from the source builder, including:
        /// - Element collection
        /// - Comparator settings
        /// - Break configuration
        /// - Behavioral characteristics
        ///
        /// Usage:
        /// ```java
        /// AbstractBuilder<?, ?, String> original = new ConcreteBuilder()
        ///     .withComparator(String.CASE_INSENSITIVE_ORDER)
        ///     .withBreak(FIRST_ALWAYS_THROWS_EXCEPTION);
        ///
        /// AbstractBuilder<?, ?, String> copy = new ConcreteBuilder(original);
        /// ```
        ///
        /// @param other the builder to copy configuration from
        /// @throws NullPointerException if other is null
        public AbstractBuilder(final @NonNull AbstractBuilder<B, C, E> other) {
            super(other);
            this.comparator = other.comparator;
        }

        /// Configures the builder to use the specified comparator for element ordering.
        ///
        /// ```java
        /// BreakableSortedSet<String> set = new BreakableSortedSet.Builder<String>()
        ///     .withComparator(String.CASE_INSENSITIVE_ORDER)
        ///     .withBreak(COMPARATOR_ALWAYS_RETURNS_NULL)
        ///     .build();
        /// ```
        ///
        /// @param aComparator the comparator to use for ordering elements
        /// @return this builder for method chaining
        public B withComparator(final @Nullable Comparator<? super E> aComparator) {
            this.comparator = Objects.requireNonNull(aComparator);
            return self();
        }


        /// Returns the comparator to use for the set being built.
        /// @return the comparator for the set.
        protected Comparator<? super E> comparator() {
            return comparator;
        }
    }

    /// **Builder for BreakableSortedSet Construction**
    ///
    /// This builder class provides a fluent interface for constructing BreakableSortedSet instances
    /// with specific configurations, breaks, and behavioral characteristics. The builder
    /// extends the BreakableSet.Builder to inherit common set functionality while providing
    /// sorted set-specific configuration options.
    ///
    /// ## Key Features
    ///
    /// - **Fluent Interface**: Method chaining for readable configuration
    /// - **Type Safety**: Generic type preservation throughout the building process
    /// - **Comparator Support**: Configurable element ordering
    /// - **SortedSet Breaks**: Access to sorted set-specific behavioral breaks
    /// - **Configuration Flexibility**: Support for custom backing sorted sets
    ///
    /// ## Builder Configuration
    ///
    /// ### Basic SortedSet Creation
    /// ```java
    /// BreakableSortedSet<String> set = new BreakableSortedSet.Builder<String>()
    ///     .build();
    /// ```
    ///
    /// ### SortedSet with Custom Comparator
    /// ```java
    /// BreakableSortedSet<String> set = new BreakableSortedSet.Builder<String>()
    ///     .withComparator(String.CASE_INSENSITIVE_ORDER)
    ///     .build();
    /// ```
    ///
    /// ### SortedSet with Breaks and Restrictions
    /// ```java
    /// BreakableSortedSet<Integer> brokenSet = new BreakableSortedSet.Builder<Integer>()
    ///     .withBreak(FIRST_ALWAYS_THROWS_EXCEPTION)
    ///     .withBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)
    ///     .build();
    /// ```
    ///
    /// @param <E> the type of elements maintained by the built sorted set
    /// @see BreakableSet.Builder
    /// @see BreakableSortedSet
    /// @since 1.0.0
    public static class Builder<E> extends AbstractBuilder<Builder<E>, BreakableSortedSet<E>, E> {

        /// Creates a new builder with default configuration.
        ///
        /// This constructor initializes the builder with:
        /// - TreeSet as the backing collection with natural ordering
        /// - No breaks active
        /// - Standard sorted set semantics
        ///
        /// ```java
        /// BreakableSortedSet.Builder<String> builder = new BreakableSortedSet.Builder<>();
        /// BreakableSortedSet<String> set = builder.build();
        /// ```
        public Builder() {
            super();
        }

        /// Creates a new builder with a custom comparator.
        ///
        /// This constructor initializes the builder with a TreeSet that uses the
        /// specified comparator for element ordering.
        ///
        /// ```java
        /// BreakableSortedSet<String> set = new BreakableSortedSet.Builder<String>()
        ///     .withComparator(String.CASE_INSENSITIVE_ORDER)
        ///     .build();
        /// ```
        ///
        /// @param comparator the comparator to use for ordering elements
        public Builder(final @Nullable Comparator<? super E> comparator) {
            super();
        }

        /// Creates a new builder with a custom backing sorted set implementation.
        ///
        /// @param elements the elements to add to the set
        /// @throws NullPointerException if sortedSet is null
        public Builder(final @NonNull Collection<E> elements) {
            super(elements);
        }

        /// Creates a new builder by copying configuration from another builder.
        ///
        /// This copy constructor creates a new builder that inherits all configuration
        /// from the source builder, including the backing sorted set reference.
        ///
        /// @param other the builder to copy configuration from
        /// @throws NullPointerException if other is null
        public Builder(final @NonNull Builder<E> other) {
            super(other);
        }



        /// Returns this builder instance for method chaining.
        ///
        /// This method is required by the builder pattern to enable fluent
        /// interface method chaining with the correct concrete type.
        ///
        /// @return this builder instance
        @Override
        public Builder<E> self() {
            return this;
        }

        /// Creates a copy of this builder with identical configuration.
        ///
        /// This method creates a new builder instance that has the same configuration
        /// as the current builder, including the backing sorted set reference.
        ///
        /// @return a new builder with identical configuration
        @Override
        public Builder<E> copy() {
            return new Builder<>(this);
        }

        /// Constructs a new BreakableSortedSet instance with the current builder configuration.
        ///
        /// This method creates a new BreakableSortedSet using all the settings configured
        /// in the builder, including breaks, method restrictions, and the backing sorted set.
        ///
        /// @return a new BreakableSortedSet instance configured according to this builder's settings
        @Override
        public BreakableSortedSet<E> build() {
            final TreeSet<E> storage = new TreeSet<>(comparator());
            storage.addAll(elements());

            BreakableSortedSet<E> broken = new BreakableSortedSet<>(storage, breaks(), characteristics());
            broken.setPermitsNulls(permitsNulls());
            broken.setPermitsDuplicates(permitsDuplicates());
            broken.setPermitsIncompatibleTypes(permitsIncompatibleTypes());
            unsupportedMethods().forEach(broken::doesNotSupportMethod);
            return broken;
        }
    }
}
