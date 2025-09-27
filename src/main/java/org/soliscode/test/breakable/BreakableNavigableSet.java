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
import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/// **Breakable NavigableSet Implementation for Testing**
///
/// This class extends `BreakableSortedSet` to provide a NavigableSet implementation that can be
/// programmatically broken for comprehensive testing scenarios. It maintains all SortedSet functionality
/// while adding NavigableSet-specific navigation methods and their corresponding breakable behaviors.
///
/// ## Core Functionality
///
/// As a NavigableSet implementation, this class provides all standard navigation operations:
/// - **Ceiling/Floor Operations**: Finding the least/greatest elements relative to a given value
/// - **Higher/Lower Operations**: Finding elements strictly greater/less than a given value
/// - **Poll Operations**: Retrieving and removing first/last elements
/// - **Descendant Navigation**: Reverse iteration and subset operations
///
/// ### NavigableSet Semantics
///
/// Under normal operation (no breaks active), BreakableNavigableSet maintains proper NavigableSet behavior:
/// - **Navigation Methods**: Return appropriate elements based on ordering and search criteria
/// - **Poll Operations**: Remove and return boundary elements, returning null for empty sets
/// - **Reverse Views**: Provide consistent descending iteration and subset operations
/// - **Subset Operations**: Create navigable views of ranges within the set
///
/// ## Available Breaks
///
/// This class provides breaks for all NavigableSet-specific methods in addition to inheriting
/// all SortedSet breaks from the parent class. The breaks are organized into logical categories
/// based on the type of navigation functionality they affect.
///
/// ### Navigation Method Breaks
///
/// #### Ceiling/Floor Operation Breaks
/// ##### CEILING_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `ceiling()` method to always return null regardless of input or set contents
/// **Effect**: Navigation fails to find the least element greater than or equal to the given element
/// **Use Case**: Testing code that must handle failed ceiling lookups and null navigation results
///
/// ##### CEILING_RETURNS_FLOOR_VALUE
/// **Purpose**: Forces `ceiling()` method to return the floor value instead of the ceiling value
/// **Effect**: Navigation returns the wrong direction, providing greatest ≤ instead of least ≥
/// **Use Case**: Testing code robustness when navigation methods return contradictory results
///
/// ##### FLOOR_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `floor()` method to always return null regardless of input or set contents
/// **Effect**: Navigation fails to find the greatest element less than or equal to the given element
/// **Use Case**: Testing code that must handle failed floor lookups and null navigation results
///
/// ##### FLOOR_RETURNS_CEILING_VALUE
/// **Purpose**: Forces `floor()` method to return the ceiling value instead of the floor value
/// **Effect**: Navigation returns the wrong direction, providing least ≥ instead of greatest ≤
/// **Use Case**: Testing code robustness when navigation methods return contradictory results
///
/// #### Higher/Lower Operation Breaks
/// ##### HIGHER_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `higher()` method to always return null regardless of input or set contents
/// **Effect**: Navigation fails to find elements strictly greater than the given element
/// **Use Case**: Testing code that must handle failed higher lookups and null navigation results
///
/// ##### HIGHER_RETURNS_LOWER_VALUE
/// **Purpose**: Forces `higher()` method to return the lower value instead of the higher value
/// **Effect**: Navigation returns the opposite direction, providing element < instead of element >
/// **Use Case**: Testing code robustness when navigation methods return contradictory results
///
/// ##### LOWER_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `lower()` method to always return null regardless of input or set contents
/// **Effect**: Navigation fails to find elements strictly less than the given element
/// **Use Case**: Testing code that must handle failed lower lookups and null navigation results
///
/// ##### LOWER_RETURNS_HIGHER_VALUE
/// **Purpose**: Forces `lower()` method to return the higher value instead of the lower value
/// **Effect**: Navigation returns the opposite direction, providing element > instead of element <
/// **Use Case**: Testing code robustness when navigation methods return contradictory results
///
/// ### Poll Operation Breaks
///
/// #### Poll Null Return Breaks
/// ##### POLL_FIRST_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `pollFirst()` method to always return null without removing any elements
/// **Effect**: Poll operations appear to fail even when elements exist in the set
/// **Use Case**: Testing code that must handle failed poll operations and empty-set-like behavior
///
/// ##### POLL_LAST_ALWAYS_RETURNS_NULL
/// **Purpose**: Forces `pollLast()` method to always return null without removing any elements
/// **Effect**: Poll operations appear to fail even when elements exist in the set
/// **Use Case**: Testing code that must handle failed poll operations and empty-set-like behavior
///
/// #### Poll Inconsistency Breaks
/// ##### POLL_FIRST_DOES_NOT_REMOVE
/// **Purpose**: Forces `pollFirst()` method to return the first element but not remove it from the set
/// **Effect**: Creates inconsistency between return value and set modification behavior
/// **Use Case**: Testing code that assumes poll operations modify the collection state
///
/// ##### POLL_LAST_DOES_NOT_REMOVE
/// **Purpose**: Forces `pollLast()` method to return the last element but not remove it from the set
/// **Effect**: Creates inconsistency between return value and set modification behavior
/// **Use Case**: Testing code that assumes poll operations modify the collection state
///
/// ### Descendant View Breaks
///
/// #### DESCENDING_SET_RETURNS_EMPTY
/// **Purpose**: Forces `descendingSet()` method to always return an empty NavigableSet
/// **Effect**: Reverse navigation views appear empty regardless of actual set contents
/// **Use Case**: Testing code that depends on reverse iteration and must handle empty descendant views
///
/// #### DESCENDING_ITERATOR_RETURNS_EMPTY
/// **Purpose**: Forces `descendingIterator()` method to always return an empty iterator
/// **Effect**: Reverse iteration fails to access any elements regardless of set contents
/// **Use Case**: Testing code that uses reverse iteration and must handle empty iteration sequences
///
/// ## Builder Pattern
///
/// The class provides a comprehensive Builder for constructing BreakableNavigableSet instances:
///
/// ```java
/// // Create a navigable set with navigation breaks
/// BreakableNavigableSet<Integer> set = new BreakableNavigableSet.Builder<Integer>()
///     .addBreak(CEILING_ALWAYS_RETURNS_NULL)
///     .addBreak(POLL_FIRST_DOES_NOT_REMOVE)
///     .build();
///
/// // Create a set with custom comparator and multiple breaks
/// BreakableNavigableSet<String> customSet = new BreakableNavigableSet.Builder<String>()
///     .withComparator(String.CASE_INSENSITIVE_ORDER)
///     .addBreak(FLOOR_RETURNS_CEILING_VALUE)
///     .addBreak(DESCENDING_SET_RETURNS_EMPTY)
///     .build();
/// ```
///
/// ## Testing Applications
///
/// ### Ceiling/Floor Navigation Testing
/// Test that code properly handles ceiling and floor navigation failures:
///
/// ```java
/// @Test
/// void testCeilingFloorNavigationFailures() {
///     BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
///         .addBreak(CEILING_ALWAYS_RETURNS_NULL)
///         .addBreak(FLOOR_RETURNS_CEILING_VALUE)
///         .build();
///
///     brokenSet.addAll(Arrays.asList(1, 3, 5, 7, 9));
///
///     assertNull(brokenSet.ceiling(4)); // Should return 5, but returns null
///     assertEquals(5, brokenSet.floor(4)); // Should return 3, but returns ceiling instead
///
///     // Test that search algorithms handle broken navigation
///     testBinarySearchWithBrokenNavigation(brokenSet);
/// }
/// ```
///
/// ### Higher/Lower Navigation Testing
/// Test that code handles higher and lower navigation contradictions:
///
/// ```java
/// @Test
/// void testHigherLowerNavigationContradictions() {
///     BreakableNavigableSet<String> brokenSet = new BreakableNavigableSet.Builder<String>()
///         .addBreak(HIGHER_RETURNS_LOWER_VALUE)
///         .addBreak(LOWER_RETURNS_HIGHER_VALUE)
///         .build();
///
///     brokenSet.addAll(Arrays.asList("a", "c", "e", "g", "i"));
///
///     assertEquals("a", brokenSet.higher("c")); // Should return "e", but returns "a" (lower)
///     assertEquals("e", brokenSet.lower("c"));  // Should return "a", but returns "e" (higher)
///
///     // Test that range algorithms handle contradictory navigation
///     testRangeOperationsWithContradictoryNavigation(brokenSet);
/// }
/// ```
///
/// ### Navigation Null Return Testing
/// Test that code handles navigation methods that always return null:
///
/// ```java
/// @Test
/// void testNavigationNullReturns() {
///     BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
///         .addBreak(CEILING_ALWAYS_RETURNS_NULL)
///         .addBreak(FLOOR_ALWAYS_RETURNS_NULL)
///         .addBreak(HIGHER_ALWAYS_RETURNS_NULL)
///         .addBreak(LOWER_ALWAYS_RETURNS_NULL)
///         .build();
///
///     brokenSet.addAll(Arrays.asList(10, 20, 30, 40, 50));
///
///     assertNull(brokenSet.ceiling(25));  // All navigation methods broken
///     assertNull(brokenSet.floor(25));
///     assertNull(brokenSet.higher(25));
///     assertNull(brokenSet.lower(25));
///
///     // Test that algorithms gracefully degrade when navigation is unavailable
///     testNavigationFallbackStrategies(brokenSet);
/// }
/// ```
///
/// ### Poll Operation Null Return Testing
/// Test that code handles poll operations that always return null:
///
/// ```java
/// @Test
/// void testPollNullReturns() {
///     BreakableNavigableSet<String> brokenSet = new BreakableNavigableSet.Builder<String>()
///         .addBreak(POLL_FIRST_ALWAYS_RETURNS_NULL)
///         .addBreak(POLL_LAST_ALWAYS_RETURNS_NULL)
///         .build();
///
///     brokenSet.addAll(Arrays.asList("alpha", "beta", "gamma"));
///
///     assertNull(brokenSet.pollFirst()); // Returns null despite having elements
///     assertNull(brokenSet.pollLast());  // Returns null despite having elements
///     assertEquals(3, brokenSet.size()); // Elements remain in set
///
///     // Test that collection draining handles null poll returns
///     testCollectionDrainingWithNullPolls(brokenSet);
/// }
/// ```
///
/// ### Poll Operation Inconsistency Testing
/// Test that code handles poll operations that don't actually remove elements:
///
/// ```java
/// @Test
/// void testPollInconsistencies() {
///     BreakableNavigableSet<Integer> brokenSet = new BreakableNavigableSet.Builder<Integer>()
///         .addBreak(POLL_FIRST_DOES_NOT_REMOVE)
///         .addBreak(POLL_LAST_DOES_NOT_REMOVE)
///         .build();
///
///     brokenSet.addAll(Arrays.asList(1, 2, 3, 4, 5));
///
///     Integer first = brokenSet.pollFirst(); // Returns 1 but doesn't remove it
///     Integer last = brokenSet.pollLast();   // Returns 5 but doesn't remove it
///
///     assertEquals(1, first);
///     assertEquals(5, last);
///     assertEquals(5, brokenSet.size()); // Size unchanged due to breaks
///     assertTrue(brokenSet.contains(1)); // Elements still present
///     assertTrue(brokenSet.contains(5));
///
///     // Test that collection utilities handle inconsistent poll behavior
///     testPollConsistencyChecks(brokenSet);
/// }
/// ```
///
/// ### Descendant View Testing
/// Test that code handles broken descendant views and iteration:
///
/// ```java
/// @Test
/// void testDescendantViewBreaks() {
///     BreakableNavigableSet<String> brokenSet = new BreakableNavigableSet.Builder<String>()
///         .addBreak(DESCENDING_SET_RETURNS_EMPTY)
///         .addBreak(DESCENDING_ITERATOR_RETURNS_EMPTY)
///         .build();
///
///     brokenSet.addAll(Arrays.asList("apple", "banana", "cherry", "date"));
///
///     NavigableSet<String> descendingView = brokenSet.descendingSet();
///     assertTrue(descendingView.isEmpty()); // Should contain elements in reverse, but is empty
///
///     Iterator<String> descendingIter = brokenSet.descendingIterator();
///     assertFalse(descendingIter.hasNext()); // Should iterate in reverse, but is empty
///
///     // Test that reverse algorithms handle empty descendant views
///     testReverseProcessingWithEmptyViews(brokenSet);
/// }
/// ```
///
/// ### Combined Break Testing
/// Test that code handles multiple simultaneous navigation breaks:
///
/// ```java
/// @Test
/// void testCombinedNavigationBreaks() {
///     BreakableNavigableSet<Integer> complexBrokenSet = new BreakableNavigableSet.Builder<Integer>()
///         .addBreak(CEILING_RETURNS_FLOOR_VALUE)     // Navigation confusion
///         .addBreak(POLL_FIRST_DOES_NOT_REMOVE)     // Poll inconsistency
///         .addBreak(DESCENDING_SET_RETURNS_EMPTY)   // Descendant view failure
///         .addBreak(FIRST_ALWAYS_THROWS_EXCEPTION)  // Inherited SortedSet break
///         .build();
///
///     complexBrokenSet.addAll(Arrays.asList(10, 20, 30, 40, 50));
///
///     // Test multiple failure modes simultaneously
///     assertEquals(30, complexBrokenSet.ceiling(35)); // Returns floor instead of ceiling
///     assertThrows(NoSuchElementException.class, () -> complexBrokenSet.first()); // Inherited break
///
///     Integer polled = complexBrokenSet.pollFirst(); // Returns but doesn't remove
///     assertEquals(5, complexBrokenSet.size()); // Still has all elements
///
///     assertTrue(complexBrokenSet.descendingSet().isEmpty()); // Descendant view broken
///
///     // Test that robust algorithms handle multiple failure modes
///     testNavigationWithMultipleFailures(complexBrokenSet);
/// }
/// ```
///
/// ### Integration with SortedSet Breaks
/// Test that NavigableSet breaks work correctly with inherited SortedSet breaks:
///
/// ```java
/// @Test
/// void testNavigableSetWithSortedSetBreaks() {
///     BreakableNavigableSet<String> hybridBrokenSet = new BreakableNavigableSet.Builder<String>()
///         .addBreak(CEILING_ALWAYS_RETURNS_NULL)          // NavigableSet break
///         .addBreak(FIRST_RETURNS_LAST_ELEMENT)           // SortedSet break
///         .addBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY) // SortedSet break
///         .build();
///
///     hybridBrokenSet.addAll(Arrays.asList("alpha", "beta", "gamma", "delta"));
///
///     assertNull(hybridBrokenSet.ceiling("beta"));    // NavigableSet break active
///     assertEquals("delta", hybridBrokenSet.first()); // SortedSet break: returns last instead
///     assertTrue(hybridBrokenSet.subSet("beta", "delta").isEmpty()); // SortedSet break active
///
///     // Test that algorithms handle mixed break types gracefully
///     testHybridBreakScenarios(hybridBrokenSet);
/// }
/// ```
///
/// @param <E> the type of elements maintained by this navigable set
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableSortedSet
/// @see NavigableSet
/// @see java.util.TreeSet
public class BreakableNavigableSet<E> extends BreakableSortedSet<E> implements NavigableSet<E> {

    /// The backing navigable set that stores the elements and provides navigation functionality.
    private final NavigableSet<E> navigableSet;

    // ========== Navigation Method Breaks ==========

    /// Break that causes ceiling() method to always return null regardless of input.
    public static final Break CEILING_ALWAYS_RETURNS_NULL =
            new Break("ceiling() method always returns null");

    /// Break that causes ceiling() method to return floor value instead of ceiling value.
    public static final Break CEILING_RETURNS_FLOOR_VALUE =
            new Break("ceiling() method returns floor value instead");

    /// Break that causes floor() method to always return null regardless of input.
    public static final Break FLOOR_ALWAYS_RETURNS_NULL =
            new Break("floor() method always returns null");

    /// Break that causes floor() method to return ceiling value instead of floor value.
    public static final Break FLOOR_RETURNS_CEILING_VALUE =
            new Break("floor() method returns ceiling value instead");

    /// Break that causes higher() method to always return null regardless of input.
    public static final Break HIGHER_ALWAYS_RETURNS_NULL =
            new Break("higher() method always returns null");

    /// Break that causes higher() method to return lower value instead of higher value.
    public static final Break HIGHER_RETURNS_LOWER_VALUE =
            new Break("higher() method returns lower value instead");

    /// Break that causes lower() method to always return null regardless of input.
    public static final Break LOWER_ALWAYS_RETURNS_NULL =
            new Break("lower() method always returns null");

    /// Break that causes lower() method to return higher value instead of lower value.
    public static final Break LOWER_RETURNS_HIGHER_VALUE =
            new Break("lower() method returns higher value instead");

    // ========== Poll Operation Breaks ==========

    /// Break that causes pollFirst() method to always return null without removing elements.
    public static final Break POLL_FIRST_ALWAYS_RETURNS_NULL =
            new Break("pollFirst() method always returns null");

    /// Break that causes pollFirst() method to return element but not remove it from the set.
    public static final Break POLL_FIRST_DOES_NOT_REMOVE =
            new Break("pollFirst() method returns element but does not remove it");

    /// Break that causes pollLast() method to always return null without removing elements.
    public static final Break POLL_LAST_ALWAYS_RETURNS_NULL =
            new Break("pollLast() method always returns null");

    /// Break that causes pollLast() method to return element but not remove it from the set.
    public static final Break POLL_LAST_DOES_NOT_REMOVE =
            new Break("pollLast() method returns element but does not remove it");

    // ========== Descendant View Breaks ==========

    /// Break that causes descendingSet() method to always return an empty set.
    public static final Break DESCENDING_SET_RETURNS_EMPTY =
            new Break("descendingSet() method always returns empty set");

    /// Break that causes descendingIterator() method to always return an empty iterator.
    public static final Break DESCENDING_ITERATOR_RETURNS_EMPTY =
            new Break("descendingIterator() method always returns empty iterator");

    /// Creates a new empty BreakableNavigableSet with default configuration.
    ///
    /// The set uses a TreeSet as the backing collection and has no breaks active.
    /// This constructor is equivalent to using the Builder with default settings.
    ///
    /// ```java
    /// BreakableNavigableSet<String> set = new BreakableNavigableSet<>();
    /// set.add("element");
    /// ```
    public BreakableNavigableSet() {
        this(new TreeSet<>(), new ArrayList<>(), 0);
    }

    /// Creates a new BreakableNavigableSet as a shallow copy of another BreakableNavigableSet.
    ///
    /// This constructor creates a new set that shares the same backing collection reference
    /// as the source set but starts with no breaks active. This is useful for creating
    /// variations of existing sets with different break configurations.
    ///
    /// ```java
    /// BreakableNavigableSet<Integer> original = new BreakableNavigableSet<>();
    /// original.addAll(Arrays.asList(1, 2, 3));
    ///
    /// BreakableNavigableSet<Integer> copy = new BreakableNavigableSet<>(original);
    /// // copy contains the same elements but no breaks
    /// ```
    ///
    /// @param other the BreakableNavigableSet to copy from
    /// @throws NullPointerException if other is null
    public BreakableNavigableSet(final @NonNull BreakableNavigableSet<E> other) {
        this(other.navigableSet, new ArrayList<>(), 0);
    }

    /// Creates a new BreakableNavigableSet using the specified navigable set as the backing store.
    ///
    /// This constructor allows you to specify the underlying NavigableSet implementation,
    /// which affects performance characteristics and iteration order. The set starts
    /// with no breaks active and enforces standard navigable set semantics.
    ///
    /// ```java
    /// // Use TreeSet for sorted navigation
    /// NavigableSet<String> backing = new TreeSet<>();
    /// BreakableNavigableSet<String> set = new BreakableNavigableSet<>(backing);
    ///
    /// // Use TreeSet with custom comparator
    /// NavigableSet<Integer> customOrdered = new TreeSet<>(Collections.reverseOrder());
    /// BreakableNavigableSet<Integer> reverseSet = new BreakableNavigableSet<>(customOrdered);
    /// ```
    ///
    /// @param navigableSet the backing navigable set to use for element storage
    /// @throws NullPointerException if navigableSet is null
    public BreakableNavigableSet(final @NonNull NavigableSet<E> navigableSet) {
        this(navigableSet, new ArrayList<>(), 0);
    }

    /// Creates a new BreakableNavigableSet with full configuration control.
    ///
    /// This constructor provides complete control over the set's configuration, including
    /// the backing navigable set, active breaks, and behavioral characteristics. It's primarily
    /// used internally by the Builder but can be used directly for advanced scenarios.
    ///
    /// ```java
    /// NavigableSet<String> backing = new TreeSet<>();
    /// Collection<Break> breaks = Arrays.asList(CEILING_ALWAYS_RETURNS_NULL);
    /// int characteristics = Spliterator.ORDERED;
    ///
    /// BreakableNavigableSet<String> set = new BreakableNavigableSet<>(backing, breaks, characteristics);
    /// ```
    ///
    /// @param navigableSet the backing navigable set for element storage
    /// @param breaks the collection of breaks to activate
    /// @param characteristics the spliterator characteristics
    /// @throws NullPointerException if navigableSet or breaks is null
    public BreakableNavigableSet(final @NonNull NavigableSet<E> navigableSet,
                                 final @NonNull Collection<Break> breaks,
                                 final int characteristics) {
        super(navigableSet, breaks, characteristics);
        this.navigableSet = Objects.requireNonNull(navigableSet);
    }

    // ========== NavigableSet Implementation ==========

    /// {@inheritDoc}
    @Override
    public @Nullable E lower(final E e) {
        if (hasBreak(LOWER_ALWAYS_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(LOWER_RETURNS_HIGHER_VALUE)) {
            return navigableSet.higher(e);
        } else {
            return navigableSet.lower(e);
        }
    }

    /// {@inheritDoc}
    @Override
    public @Nullable E floor(final E e) {
        if (hasBreak(FLOOR_ALWAYS_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(FLOOR_RETURNS_CEILING_VALUE)) {
            return navigableSet.ceiling(e);
        } else {
            return navigableSet.floor(e);
        }
    }

    /// {@inheritDoc}
    @Override
    public @Nullable E ceiling(final E e) {
        if (hasBreak(CEILING_ALWAYS_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(CEILING_RETURNS_FLOOR_VALUE)) {
            return navigableSet.floor(e);
        } else {
            return navigableSet.ceiling(e);
        }
    }

    /// {@inheritDoc}
    @Override
    public @Nullable E higher(final E e) {
        if (hasBreak(HIGHER_ALWAYS_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(HIGHER_RETURNS_LOWER_VALUE)) {
            return navigableSet.lower(e);
        } else {
            return navigableSet.higher(e);
        }
    }

    /// {@inheritDoc}
    @Override
    public @Nullable E pollFirst() {
        if (hasBreak(POLL_FIRST_ALWAYS_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(POLL_FIRST_DOES_NOT_REMOVE)) {
            try {
                return first();
            } catch (NoSuchElementException e) {
                return null;
            }
        } else {
            return navigableSet.pollFirst();
        }
    }

    /// {@inheritDoc}
    @Override
    public @Nullable E pollLast() {
        if (hasBreak(POLL_LAST_ALWAYS_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(POLL_LAST_DOES_NOT_REMOVE)) {
            try {
                return last();
            } catch (NoSuchElementException e) {
                return null;
            }
        } else {
            return navigableSet.pollLast();
        }
    }

    /// {@inheritDoc}
    @Override
    public @NonNull Iterator<E> descendingIterator() {
        if (hasBreak(DESCENDING_ITERATOR_RETURNS_EMPTY)) {
            return Collections.emptyIterator();
        } else {
            return navigableSet.descendingIterator();
        }
    }

    /// {@inheritDoc}
    @Override
    public @NonNull NavigableSet<E> descendingSet() {
        if (hasBreak(DESCENDING_SET_RETURNS_EMPTY)) {
            return new BreakableNavigableSet<>();
        } else {
            // Return a new BreakableNavigableSet wrapping the descending view
            // This ensures breaks are not inherited by the descending view
            return new BreakableNavigableSet<>(navigableSet.descendingSet());
        }
    }

    /// {@inheritDoc}
    @Override
    public @NonNull NavigableSet<E> subSet(final E fromElement, final boolean fromInclusive,
                                           final E toElement, final boolean toInclusive) {
        // Delegate to parent class for subset break handling, then wrap result
        SortedSet<E> parentSubset = super.subSet(fromElement, toElement);
        if (parentSubset.isEmpty()) {
            return new BreakableNavigableSet<>();
        } else {
            return new BreakableNavigableSet<>(
                navigableSet.subSet(fromElement, fromInclusive, toElement, toInclusive)
            );
        }
    }

    /// {@inheritDoc}
    @Override
    public @NonNull NavigableSet<E> headSet(final E toElement, final boolean inclusive) {
        // Delegate to parent class for subset break handling, then wrap result
        SortedSet<E> parentHeadSet = super.headSet(toElement);
        if (parentHeadSet.isEmpty()) {
            return new BreakableNavigableSet<>();
        } else {
            return new BreakableNavigableSet<>(navigableSet.headSet(toElement, inclusive));
        }
    }

    /// {@inheritDoc}
    @Override
    public @NonNull NavigableSet<E> tailSet(final E fromElement, final boolean inclusive) {
        // Delegate to parent class for subset break handling, then wrap result
        SortedSet<E> parentTailSet = super.tailSet(fromElement);
        if (parentTailSet.isEmpty()) {
            return new BreakableNavigableSet<>();
        } else {
            return new BreakableNavigableSet<>(navigableSet.tailSet(fromElement, inclusive));
        }
    }

    /// **Builder for BreakableNavigableSet Construction**
    ///
    /// This builder class extends BreakableSortedSet.Builder to provide NavigableSet-specific
    /// configuration while inheriting all SortedSet and Collection builder functionality.
    ///
    /// ## Key Features
    ///
    /// - **Fluent Interface**: Method chaining for readable configuration
    /// - **NavigableSet Support**: Handles NavigableSet backing collections
    /// - **Break Configuration**: Supports all NavigableSet-specific breaks
    /// - **Inherited Functionality**: All SortedSet and Collection breaks available
    ///
    /// ## Usage Examples
    ///
    /// ### Basic NavigableSet Creation
    /// ```java
    /// BreakableNavigableSet<Integer> set = new BreakableNavigableSet.Builder<Integer>()
    ///     .build();
    /// ```
    ///
    /// ### NavigableSet with Custom Comparator and Breaks
    /// ```java
    /// BreakableNavigableSet<String> set = new BreakableNavigableSet.Builder<String>()
    ///     .withComparator(String.CASE_INSENSITIVE_ORDER)
    ///     .addBreak(CEILING_ALWAYS_RETURNS_NULL)
    ///     .addBreak(POLL_FIRST_DOES_NOT_REMOVE)
    ///     .build();
    /// ```
    ///
    /// @param <E> the type of elements maintained by the built navigable set
    /// @see BreakableSortedSet.Builder
    /// @see BreakableNavigableSet
    /// @since 1.0.0
    public static class Builder<E>
            extends BreakableSortedSet.AbstractBuilder<Builder<E>, BreakableNavigableSet<E>, E> {

        /// Creates a new builder with default configuration.
        ///
        /// This constructor initializes the builder with:
        /// - TreeSet as the backing collection (optimal for NavigableSet operations)
        /// - No breaks active
        /// - Standard navigable set semantics
        /// - Natural ordering (if elements implement Comparable)
        ///
        /// ```java
        /// BreakableNavigableSet.Builder<String> builder = new BreakableNavigableSet.Builder<>();
        /// BreakableNavigableSet<String> set = builder.build();
        /// ```
        public Builder() {
            super();
        }

        /**
         * Creates a new builder with the collection of elements.
         * @param elements The elements to add to the navigable set.
         */
        public Builder(final @NonNull Collection<E> elements) {
            super(elements);
        }

        /// Creates a new builder by copying configuration from another builder.
        ///
        /// This copy constructor creates a new builder that inherits all configuration
        /// from the source builder, including breaks, comparator, and behavioral settings.
        ///
        /// ```java
        /// BreakableNavigableSet.Builder<String> template = new BreakableNavigableSet.Builder<String>()
        ///     .addBreak(CEILING_ALWAYS_RETURNS_NULL);
        ///
        /// BreakableNavigableSet.Builder<String> copy = new BreakableNavigableSet.Builder<>(template);
        /// ```
        ///
        /// @param other the builder to copy configuration from
        /// @throws NullPointerException if other is null
        public Builder(final BreakableNavigableSet.@NonNull Builder<E> other) {
            super(other);
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> self() {
            return this;
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> copy() {
            return new BreakableNavigableSet.Builder<>(this);
        }


        /// Constructs a new BreakableNavigableSet instance with the current builder configuration.
        ///
        /// This method creates a new BreakableNavigableSet using all the settings configured in
        /// the builder, including NavigableSet-specific breaks, SortedSet breaks, and Collection breaks.
        ///
        /// ```java
        /// BreakableNavigableSet<Integer> set = new BreakableNavigableSet.Builder<Integer>()
        ///     .addBreak(CEILING_ALWAYS_RETURNS_NULL)
        ///     .addBreak(FIRST_ALWAYS_THROWS_EXCEPTION)  // Inherited from SortedSet
        ///     .build();
        /// ```
        ///
        /// @return a new BreakableNavigableSet instance configured according to this builder's settings
        @Override
        public BreakableNavigableSet<E> build() {
            final NavigableSet<E> storage = new TreeSet<>(comparator());
            storage.addAll(elements());

            BreakableNavigableSet<E> broken = new BreakableNavigableSet<>(storage, breaks(), characteristics());
            broken.setPermitsNulls(permitsNulls());
            broken.setPermitsDuplicates(permitsDuplicates());
            broken.setPermitsIncompatibleTypes(permitsIncompatibleTypes());
            unsupportedMethods().forEach(broken::doesNotSupportMethod);
            return broken;
        }
    }
}
