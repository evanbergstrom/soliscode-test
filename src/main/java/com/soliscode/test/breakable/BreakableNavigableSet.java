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
package com.soliscode.test.breakable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;

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
/// #### POLL_FIRST_THROWS_WHEN_EMPTY
/// **Purpose**: Forces `pollFirst()` to throw `NoSuchElementException` for an empty set, where it returns null
///
/// #### POLL_LAST_THROWS_WHEN_EMPTY
/// **Purpose**: Forces `pollLast()` to throw `NoSuchElementException` for an empty set, where it returns null
///
/// ### Range View Breaks
///
/// #### HEAD_SET_IGNORES_INCLUSIVE
/// **Purpose**: Forces `headSet(to, inclusive)` to exclude `to` whatever `inclusive` says
///
/// #### TAIL_SET_IGNORES_INCLUSIVE
/// **Purpose**: Forces `tailSet(from, inclusive)` to include `from` whatever `inclusive` says
///
/// #### SUBSET_IGNORES_INCLUSIVE
/// **Purpose**: Forces `subSet(from, fromInclusive, to, toInclusive)` to be `[from, to)` whatever the flags say
///
/// The two-argument and four-argument views are the store's views, as the one-argument ones are, and
/// [SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY][BreakableSortedSet#SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY],
/// [SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION][BreakableSortedSet#SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION],
/// [SUBSET_ACCEPTS_REVERSED_RANGE][BreakableSortedSet#SUBSET_ACCEPTS_REVERSED_RANGE],
/// [SUBSET_OPERATIONS_RETURN_COPIES][BreakableSortedSet#SUBSET_OPERATIONS_RETURN_COPIES] and
/// [SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD][BreakableSortedSet#SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD] reach
/// them too.
///
/// ### Descendant View Breaks
///
/// #### DESCENDING_SET_RETURNS_COPY
/// **Purpose**: Forces `descendingSet()` to return a copy in descending order, not a view backed by the set
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
///     // pollFirst() returns first(), which throws under FIRST_ALWAYS_THROWS_EXCEPTION, so it returns null
///     assertNull(complexBrokenSet.pollFirst());
///     assertEquals(5, complexBrokenSet.size()); // Still has all elements
///
///     assertTrue(complexBrokenSet.descendingSet().isEmpty()); // Descendant view broken
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
///     assertEquals("gamma", hybridBrokenSet.first()); // SortedSet break: returns the last, "gamma"
///     assertTrue(hybridBrokenSet.subSet("beta", "delta").isEmpty()); // SortedSet break active
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

    // ========== Navigation Method Breaks ==========

    /// Break that causes ceiling() method to always return null regardless of input.
    ///
    /// **Affected Methods:** [ceiling(Object)][#ceiling(Object)]
    /// @see #ceiling(Object)
    public static final Break CEILING_ALWAYS_RETURNS_NULL =
            new Break("ceiling() method always returns null");

    /// Break that causes ceiling() method to return floor value instead of ceiling value.
    ///
    /// **Affected Methods:** [ceiling(Object)][#ceiling(Object)]
    /// @see #ceiling(Object)
    public static final Break CEILING_RETURNS_FLOOR_VALUE =
            new Break("ceiling() method returns floor value instead");

    /// Break that causes floor() method to always return null regardless of input.
    ///
    /// **Affected Methods:** [floor(Object)][#floor(Object)]
    /// @see #floor(Object)
    public static final Break FLOOR_ALWAYS_RETURNS_NULL =
            new Break("floor() method always returns null");

    /// Break that causes floor() method to return ceiling value instead of floor value.
    ///
    /// **Affected Methods:** [floor(Object)][#floor(Object)]
    /// @see #floor(Object)
    public static final Break FLOOR_RETURNS_CEILING_VALUE =
            new Break("floor() method returns ceiling value instead");

    /// Break that causes higher() method to always return null regardless of input.
    ///
    /// **Affected Methods:** [higher(Object)][#higher(Object)]
    /// @see #higher(Object)
    public static final Break HIGHER_ALWAYS_RETURNS_NULL =
            new Break("higher() method always returns null");

    /// Break that causes higher() method to return lower value instead of higher value.
    ///
    /// **Affected Methods:** [higher(Object)][#higher(Object)]
    /// @see #higher(Object)
    public static final Break HIGHER_RETURNS_LOWER_VALUE =
            new Break("higher() method returns lower value instead");

    /// Break that causes lower() method to always return null regardless of input.
    ///
    /// **Affected Methods:** [lower(Object)][#lower(Object)]
    /// @see #lower(Object)
    public static final Break LOWER_ALWAYS_RETURNS_NULL =
            new Break("lower() method always returns null");

    /// Break that causes lower() method to return higher value instead of lower value.
    ///
    /// **Affected Methods:** [lower(Object)][#lower(Object)]
    /// @see #lower(Object)
    public static final Break LOWER_RETURNS_HIGHER_VALUE =
            new Break("lower() method returns higher value instead");

    // ========== Poll Operation Breaks ==========

    /// Break that causes pollFirst() method to always return null without removing elements.
    ///
    /// **Affected Methods:** [pollFirst()][#pollFirst()]
    /// @see #pollFirst()
    public static final Break POLL_FIRST_ALWAYS_RETURNS_NULL =
            new Break("pollFirst() method always returns null");

    /// Break that causes pollFirst() method to return element but not remove it from the set.
    ///
    /// The element returned is what [first()][#first()] returns, so the `FIRST_` breaks change it, and `null` if
    /// `first()` throws [java.util.NoSuchElementException].
    ///
    /// **Affected Methods:** [pollFirst()][#pollFirst()]
    /// @see #pollFirst()
    public static final Break POLL_FIRST_DOES_NOT_REMOVE =
            new Break("pollFirst() method returns element but does not remove it");

    /// Break that causes pollLast() method to always return null without removing elements.
    ///
    /// **Affected Methods:** [pollLast()][#pollLast()]
    /// @see #pollLast()
    public static final Break POLL_LAST_ALWAYS_RETURNS_NULL =
            new Break("pollLast() method always returns null");

    /// Break that causes pollLast() method to return element but not remove it from the set.
    ///
    /// The element returned is what [last()][#last()] returns, so the `LAST_` breaks change it, and `null` if
    /// `last()` throws [java.util.NoSuchElementException].
    ///
    /// **Affected Methods:** [pollLast()][#pollLast()]
    /// @see #pollLast()
    public static final Break POLL_LAST_DOES_NOT_REMOVE =
            new Break("pollLast() method returns element but does not remove it");

    /// **Purpose:** simulates a `pollFirst()` that treats an empty set as an error.
    ///
    /// **Effect:** `pollFirst()` throws [NoSuchElementException] for an empty set, where [NavigableSet#pollFirst()]
    /// returns `null`. A set that is not empty is not changed.
    ///
    /// **Affected Methods:** [pollFirst()][#pollFirst()]
    /// @see #pollFirst()
    public static final Break POLL_FIRST_THROWS_WHEN_EMPTY =
            new Break("pollFirst() throws NoSuchElementException for an empty set");

    /// **Purpose:** simulates a `pollLast()` that treats an empty set as an error.
    ///
    /// **Effect:** `pollLast()` throws [NoSuchElementException] for an empty set, where [NavigableSet#pollLast()]
    /// returns `null`. A set that is not empty is not changed.
    ///
    /// **Affected Methods:** [pollLast()][#pollLast()]
    /// @see #pollLast()
    public static final Break POLL_LAST_THROWS_WHEN_EMPTY =
            new Break("pollLast() throws NoSuchElementException for an empty set");

    // ========== Range View Breaks ==========

    /// **Purpose:** simulates a head view that ignores its `inclusive` flag.
    ///
    /// **Effect:** `headSet(to, inclusive)` excludes `to` whatever `inclusive` says, where
    /// [NavigableSet#headSet(Object, boolean)] includes it when `inclusive` is `true`.
    ///
    /// **Affected Methods:** [headSet(Object, boolean)][#headSet(Object,boolean)] only
    /// @see #headSet(Object, boolean)
    public static final Break HEAD_SET_IGNORES_INCLUSIVE =
            new Break("headSet(to, inclusive) always excludes to");

    /// **Purpose:** simulates a tail view that ignores its `inclusive` flag.
    ///
    /// **Effect:** `tailSet(from, inclusive)` includes `from` whatever `inclusive` says, where
    /// [NavigableSet#tailSet(Object, boolean)] excludes it when `inclusive` is `false`.
    ///
    /// **Affected Methods:** [tailSet(Object, boolean)][#tailSet(Object,boolean)] only
    /// @see #tailSet(Object, boolean)
    public static final Break TAIL_SET_IGNORES_INCLUSIVE =
            new Break("tailSet(from, inclusive) always includes from");

    /// **Purpose:** simulates a range view that ignores its `inclusive` flags.
    ///
    /// **Effect:** `subSet(from, fromInclusive, to, toInclusive)` includes `from` and excludes `to` whatever the
    /// flags say, as `subSet(from, to)` does, where [NavigableSet#subSet(Object, boolean, Object, boolean)] follows
    /// them.
    ///
    /// **Affected Methods:** [subSet(Object, boolean, Object, boolean)][#subSet(Object,boolean,Object,boolean)] only
    /// @see #subSet(Object, boolean, Object, boolean)
    public static final Break SUBSET_IGNORES_INCLUSIVE =
            new Break("subSet(from, fromInclusive, to, toInclusive) ignores the flags");

    // ========== Descendant View Breaks ==========

    /// **Purpose:** simulates a descending view that is a snapshot, not a view.
    ///
    /// **Effect:** `descendingSet()` returns a copy of the set in descending order, so a change to the set does not
    /// show in it, and a change to it does not reach the set ([NavigableSet#descendingSet()]: "The descending set is
    /// backed by this set").
    ///
    /// **Affected Methods:** [descendingSet()][#descendingSet()] only
    /// @see #descendingSet()
    public static final Break DESCENDING_SET_RETURNS_COPY =
            new Break("descendingSet() returns a copy, not a view");

    /// Break that causes descendingSet() method to always return an empty set.
    ///
    /// **Affected Methods:** [descendingSet()][#descendingSet()]
    /// @see #descendingSet()
    public static final Break DESCENDING_SET_RETURNS_EMPTY =
            new Break("descendingSet() method always returns empty set");

    /// Break that causes descendingIterator() method to always return an empty iterator.
    ///
    /// **Affected Methods:** [descendingIterator()][#descendingIterator()]
    /// @see #descendingIterator()
    public static final Break DESCENDING_ITERATOR_RETURNS_EMPTY =
            new Break("descendingIterator() method always returns empty iterator");

    /// The backing navigable set that stores the elements and provides navigation functionality.
    private final NavigableSet<E> navigableSet;

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
        //noinspection SortedCollectionWithNonComparableKeys
        this(new TreeSet<>(), DEFAULT_BREAKS, DEFAULT_METHOD_STATUSES, DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a new BreakableNavigableSet as a copy of another BreakableNavigableSet.
    ///
    /// This constructor copies the source set's elements into a new `TreeSet` with the same ordering, and copies
    /// its breaks, method statuses, characteristics, permits, safety and compatible type. Changes to one set do not
    /// reach the other.
    ///
    /// ```java
    /// BreakableNavigableSet<Integer> original = new BreakableNavigableSet<>();
    /// original.addAll(Arrays.asList(1, 2, 3));
    ///
    /// BreakableNavigableSet<Integer> copy = new BreakableNavigableSet<>(original);
    /// // copy contains the same elements, and has the same breaks
    /// ```
    ///
    /// @param other the BreakableNavigableSet to copy from
    /// @throws NullPointerException if other is null
    public BreakableNavigableSet(final BreakableNavigableSet<E> other) {
        this(new TreeSet<>(other.navigableSet), new HashSet<>(other.breaks()), new HashMap<>(other.methodStatuses()),
                other.characteristics(), other.permits(), other.isSafe(), other.compatibleType());
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
    /// @param navigableSetArg the backing navigable set to use for element storage
    /// @throws NullPointerException if navigableSet is null
    public BreakableNavigableSet(final NavigableSet<E> navigableSetArg) {
        this(navigableSetArg, DEFAULT_BREAKS, DEFAULT_METHOD_STATUSES, DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a new BreakableNavigableSet with full configuration control.
    ///
    /// This constructor provides complete control over the set's configuration, including
    /// the backing navigable set, active breaks, and behavioral characteristics. It is used by the Builder, and is
    /// protected, so a subclass can call it:
    ///
    /// ```java
    /// // In a subclass's constructor:
    /// super(new TreeSet<>(), Set.of(CEILING_ALWAYS_RETURNS_NULL), Map.of(), Spliterator.ORDERED,
    ///         DEFAULT_PERMITS, false, Object.class);
    /// ```
    ///
    /// @param navigableSetArg the backing navigable set for element storage
    /// @param breaks the collection of breaks to activate
    /// @param methodStatuses the method status configuration.
    /// @param characteristics the spliterator characteristics
    /// @param permits         the flags that indicate what types of values are supported by the collection.
    /// @param isSafe         whether the collection is safe for concurrent access by multiple threads.
    /// @param compatibleType the type that this collection is compatible with.
    /// @throws NullPointerException if navigableSet or breaks is null
    protected BreakableNavigableSet(final NavigableSet<E> navigableSetArg,
                                    final Set<Break> breaks,
                                    final Map<InterfaceMethod, MethodStatus> methodStatuses,
                                    final int characteristics,
                                    final int permits,
                                    final boolean isSafe,
                                    final Class<?> compatibleType) {
        super(navigableSetArg, breaks, methodStatuses, characteristics, permits, isSafe, compatibleType);
        this.navigableSet = Objects.requireNonNull(navigableSetArg);
    }

    @Override
    public NavigableSet<E> reversed() {
        if (hasBreak(BreakableSequencedCollection.REVERSED_DOES_NOT_REVERSE_COLLECTION)) {
            return this;
        }
        if (hasBreak(BreakableSequencedCollection.REVERSED_MODIFIES_THE_COLLECTION)) {
            NavigableSet<E> reversed = navigableSet.reversed();
            navigableSet.clear();
            navigableSet.addAll(reversed);
            return this;
        }
        return new BreakableNavigableSet<>(navigableSet.reversed(), breaks(), methodStatuses(), characteristics(),
                permits(), isSafe(), compatibleType());
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
        if (navigableSet.isEmpty() && hasBreak(POLL_FIRST_THROWS_WHEN_EMPTY)) {
            throw new NoSuchElementException("pollFirst() of an empty set is broken");
        } else if (hasBreak(POLL_FIRST_ALWAYS_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(POLL_FIRST_DOES_NOT_REMOVE)) {
            try {
                return first();
            } catch (final NoSuchElementException e) {
                return null;
            }
        } else {
            return navigableSet.pollFirst();
        }
    }

    /// {@inheritDoc}
    @Override
    public @Nullable E pollLast() {
        if (navigableSet.isEmpty() && hasBreak(POLL_LAST_THROWS_WHEN_EMPTY)) {
            throw new NoSuchElementException("pollLast() of an empty set is broken");
        } else if (hasBreak(POLL_LAST_ALWAYS_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(POLL_LAST_DOES_NOT_REMOVE)) {
            try {
                return last();
            } catch (final NoSuchElementException e) {
                return null;
            }
        } else {
            return navigableSet.pollLast();
        }
    }

    /// {@inheritDoc}
    @Override
    public Iterator<E> descendingIterator() {
        if (hasBreak(DESCENDING_ITERATOR_RETURNS_EMPTY)) {
            return Collections.emptyIterator();
        } else {
            return navigableSet.descendingIterator();
        }
    }

    /// {@inheritDoc}
    @Override
    public NavigableSet<E> descendingSet() {
        if (hasBreak(DESCENDING_SET_RETURNS_EMPTY)) {
            return new BreakableNavigableSet<>();
        } else if (hasBreak(DESCENDING_SET_RETURNS_COPY)) {
            return new TreeSet<>(navigableSet.descendingSet());
        } else {
            // Return a new BreakableNavigableSet wrapping the descending view
            // This ensures breaks are not inherited by the descending view
            return new BreakableNavigableSet<>(navigableSet.descendingSet());
        }
    }

    /// Returns a view of the elements from `fromElement` to `toElement`, each bound included where its flag is
    /// `true`: the store's view, as [NavigableSet#subSet(Object, boolean, Object, boolean)] says, without this set's
    /// configuration (as the one-argument views are).
    ///
    /// # Breaks
    /// - [SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION][#SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION]: throws
    ///   [IllegalArgumentException]
    /// - [SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY][#SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY]: an empty set
    /// - [SUBSET_ACCEPTS_REVERSED_RANGE][#SUBSET_ACCEPTS_REVERSED_RANGE]: an empty set for `fromElement` after
    ///   `toElement`
    /// - [SUBSET_IGNORES_INCLUSIVE][#SUBSET_IGNORES_INCLUSIVE]: `[fromElement, toElement)` whatever the flags say
    /// - [SUBSET_OPERATIONS_RETURN_COPIES][#SUBSET_OPERATIONS_RETURN_COPIES] and
    ///   [SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD][#SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD]: as for
    ///   [subSet(Object, Object)][#subSet(Object,Object)]
    ///
    /// @param fromElement low endpoint of the returned set
    /// @param fromInclusive `true` if the low endpoint is to be included in the returned view
    /// @param toElement high endpoint of the returned set
    /// @param toInclusive `true` if the high endpoint is to be included in the returned view
    /// @return a view of the range
    /// @throws IllegalArgumentException if `fromElement` is after `toElement` (or a break says so)
    @Override
    public NavigableSet<E> subSet(final E fromElement, final boolean fromInclusive,
                                  final E toElement, final boolean toInclusive) {
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION)) {
            throw new IllegalArgumentException("Subset operations are broken");
        }
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)) {
            return new TreeSet<>(navigableSet.comparator());
        }
        // reverseOrder(comparator) is natural reverse order for a null comparator, so it compares with no cast.
        if (hasBreak(SUBSET_ACCEPTS_REVERSED_RANGE)
                && Collections.reverseOrder(navigableSet.comparator()).compare(fromElement, toElement) < 0) {
            return new TreeSet<>(navigableSet.comparator());
        }
        if (hasBreak(SUBSET_IGNORES_INCLUSIVE)) {
            return brokenView(navigableSet.subSet(fromElement, true, toElement, false));
        }
        return brokenView(navigableSet.subSet(fromElement, fromInclusive, toElement, toInclusive));
    }

    /// Returns a view of the elements before `toElement`, and `toElement` itself where `inclusive` is `true`: the
    /// store's view, as [NavigableSet#headSet(Object, boolean)] says, without this set's configuration.
    ///
    /// # Breaks
    /// - [SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION][#SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION]: throws
    ///   [IllegalArgumentException]
    /// - [SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY][#SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY]: an empty set
    /// - [HEAD_SET_IGNORES_INCLUSIVE][#HEAD_SET_IGNORES_INCLUSIVE]: excludes `toElement` whatever `inclusive` says
    /// - [SUBSET_OPERATIONS_RETURN_COPIES][#SUBSET_OPERATIONS_RETURN_COPIES] and
    ///   [SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD][#SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD]: as for
    ///   [headSet(Object)][#headSet(Object)]
    ///
    /// @param toElement high endpoint of the returned set
    /// @param inclusive `true` if the high endpoint is to be included in the returned view
    /// @return a view of the range
    @Override
    public NavigableSet<E> headSet(final E toElement, final boolean inclusive) {
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION)) {
            throw new IllegalArgumentException("Subset operations are broken");
        }
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)) {
            return new TreeSet<>(navigableSet.comparator());
        }
        return brokenView(navigableSet.headSet(toElement, inclusive && !hasBreak(HEAD_SET_IGNORES_INCLUSIVE)));
    }

    /// Returns a view of the elements after `fromElement`, and `fromElement` itself where `inclusive` is `true`: the
    /// store's view, as [NavigableSet#tailSet(Object, boolean)] says, without this set's configuration.
    ///
    /// # Breaks
    /// - [SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION][#SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION]: throws
    ///   [IllegalArgumentException]
    /// - [SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY][#SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY]: an empty set
    /// - [TAIL_SET_IGNORES_INCLUSIVE][#TAIL_SET_IGNORES_INCLUSIVE]: includes `fromElement` whatever `inclusive` says
    /// - [SUBSET_OPERATIONS_RETURN_COPIES][#SUBSET_OPERATIONS_RETURN_COPIES] and
    ///   [SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD][#SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD]: as for
    ///   [tailSet(Object)][#tailSet(Object)]
    ///
    /// @param fromElement low endpoint of the returned set
    /// @param inclusive `true` if the low endpoint is to be included in the returned view
    /// @return a view of the range
    @Override
    public NavigableSet<E> tailSet(final E fromElement, final boolean inclusive) {
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_THROW_EXCEPTION)) {
            throw new IllegalArgumentException("Subset operations are broken");
        }
        if (hasBreak(SUBSET_OPERATIONS_ALWAYS_RETURN_EMPTY)) {
            return new TreeSet<>(navigableSet.comparator());
        }
        return brokenView(navigableSet.tailSet(fromElement, inclusive || hasBreak(TAIL_SET_IGNORES_INCLUSIVE)));
    }

    /// Applies the view breaks to a range view of the store.
    private NavigableSet<E> brokenView(final NavigableSet<E> view) {
        if (hasBreak(SUBSET_OPERATIONS_RETURN_COPIES)) {
            return new TreeSet<>(view);
        }
        if (hasBreak(SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD)) {
            return new RangeIgnoringView<>(view);
        }
        return view;
    }

    /// A range view whose `add(E)` returns `false` for an element outside the range instead of throwing
    /// [IllegalArgumentException]; everything else is the view's. For
    /// [SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD][#SUBSET_OPERATIONS_IGNORE_OUT_OF_RANGE_ADD].
    ///
    /// @param <E> the element type
    private static final class RangeIgnoringView<E> extends AbstractSet<E> implements NavigableSet<E> {
        private final NavigableSet<E> view;

        RangeIgnoringView(final NavigableSet<E> viewArg) {
            this.view = viewArg;
        }

        @Override
        public boolean add(final E e) {
            try {
                return view.add(e);
            } catch (final IllegalArgumentException ignored) {
                return false;
            }
        }

        @Override
        public boolean addAll(final Collection<? extends E> c) {
            return view.addAll(c);
        }

        @Override
        public Iterator<E> iterator() {
            return view.iterator();
        }

        @Override
        public int size() {
            return view.size();
        }

        @Override
        public boolean contains(final @Nullable Object o) {
            return view.contains(o);
        }

        @Override
        public boolean remove(final @Nullable Object o) {
            return view.remove(o);
        }

        @Override
        public @Nullable Comparator<? super E> comparator() {
            return view.comparator();
        }

        @Override
        public E first() {
            return view.first();
        }

        @Override
        public E last() {
            return view.last();
        }

        @Override
        public @Nullable E lower(final E e) {
            return view.lower(e);
        }

        @Override
        public @Nullable E floor(final E e) {
            return view.floor(e);
        }

        @Override
        public @Nullable E ceiling(final E e) {
            return view.ceiling(e);
        }

        @Override
        public @Nullable E higher(final E e) {
            return view.higher(e);
        }

        @Override
        public @Nullable E pollFirst() {
            return view.pollFirst();
        }

        @Override
        public @Nullable E pollLast() {
            return view.pollLast();
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return view.descendingSet();
        }

        @Override
        public Iterator<E> descendingIterator() {
            return view.descendingIterator();
        }

        @Override
        public NavigableSet<E> subSet(final E fromElement, final boolean fromInclusive,
                                      final E toElement, final boolean toInclusive) {
            return view.subSet(fromElement, fromInclusive, toElement, toInclusive);
        }

        @Override
        public NavigableSet<E> headSet(final E toElement, final boolean inclusive) {
            return view.headSet(toElement, inclusive);
        }

        @Override
        public NavigableSet<E> tailSet(final E fromElement, final boolean inclusive) {
            return view.tailSet(fromElement, inclusive);
        }

        @Override
        public SortedSet<E> subSet(final E fromElement, final E toElement) {
            return view.subSet(fromElement, toElement);
        }

        @Override
        public SortedSet<E> headSet(final E toElement) {
            return view.headSet(toElement);
        }

        @Override
        public SortedSet<E> tailSet(final E fromElement) {
            return view.tailSet(fromElement);
        }
    }

    /// **Builder for BreakableNavigableSet Construction**
    ///
    /// This builder class extends BreakableSortedSet.AbstractBuilder to provide NavigableSet-specific
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
        public Builder(final Collection<E> elements) {
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
        public Builder(final BreakableNavigableSet.Builder<E> other) {
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

        @Override
        public BreakableNavigableSet<E> copy(final BreakableNavigableSet<E> other) {
            return new BreakableNavigableSet<>(other);
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
        public BreakableNavigableSet<E> build(final Iterable<E> iterable) {
            final NavigableSet<E> storage = new TreeSet<>(comparator());
            storage.addAll(elements());
            iterable.forEach(storage::add);
            return new BreakableNavigableSet<>(storage, new HashSet<>(breaks()), new HashMap<>(methodStatuses()),
                    characteristics(), permits(), isSafe(), compatibleType());
        }
    }
}
