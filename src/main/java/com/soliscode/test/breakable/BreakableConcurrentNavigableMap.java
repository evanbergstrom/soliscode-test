package com.soliscode.test.breakable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.contract.navigablemap.NavigableMapMethods;
import com.soliscode.test.contract.sortedmap.SortedMapMethods;

/// A ConcurrentNavigableMap implementation that can be programmatically broken for testing purposes.
///
/// This class extends BreakableConcurrentMap and implements the ConcurrentNavigableMap interface,
/// providing additional break constants for testing NavigableMap-specific functionality in a
/// thread-safe environment. It wraps an existing ConcurrentNavigableMap and allows specific
/// behaviors to be "broken" through the Break mechanism while maintaining the thread safety
/// guarantees and ordering properties of ConcurrentNavigableMap.
///
/// ## Overview
///
/// BreakableConcurrentNavigableMap provides comprehensive testing capabilities for code that works
/// with ConcurrentNavigableMap implementations. It supports all standard ConcurrentNavigableMap
/// operations including navigation methods, submap views, and atomic operations while enabling
/// controlled behavioral modifications through break constants. This is particularly useful for:
///
/// - **Testing NavigableMap Contract Compliance**: Verifying that code correctly handles sorted navigation operations
/// - **Race Condition Simulation**: Testing thread safety and concurrent access patterns
/// - **Edge Case Testing**: Simulating error conditions in navigation and submap operations
/// - **Framework Development**: Building robust testing utilities for concurrent collections
/// - **Integration Testing**: Validating behavior when NavigableMap implementations behave unexpectedly
///
/// ## Break Categories
///
/// The class provides breaks organized into several categories:
///
/// **Navigation Breaks**: Control the behavior of key/entry lookup methods
/// - `LOWER_ENTRY_RETURNS_NULL`: Forces lowerEntry to return null
/// - `FLOOR_KEY_RETURNS_NULL`: Forces floorKey to return null
/// - `CEILING_ENTRY_RETURNS_NULL`: Forces ceilingEntry to return null
/// - `HIGHER_KEY_RETURNS_NULL`: Forces higherKey to return null
///
/// **Extrema Breaks**: Control first/last key and entry operations
/// - `FIRST_KEY_THROWS_EXCEPTION`: Forces firstKey to throw NoSuchElementException
/// - `LAST_ENTRY_RETURNS_NULL`: Forces lastEntry to return null
/// - `POLL_FIRST_ENTRY_RETURNS_NULL`: Forces pollFirstEntry to return null
///
/// **View Breaks**: Control map view operations
/// - `DESCENDING_MAP_RETURNS_NULL`: Forces descendingMap to return null
/// - `NAVIGABLE_KEY_SET_RETURNS_NULL`: Forces navigableKeySet to return null
/// - `DESCENDING_KEY_SET_RETURNS_NULL`: Forces descendingKeySet to return null
///
/// **Submap Breaks**: Control submap creation and bounds handling
/// - `SUB_MAP_RETURNS_NULL`: Forces subMap operations to return null
/// - `SUB_MAP_IGNORES_BOUNDS`: Forces subMap to ignore bounds and return entire map
/// - `HEAD_MAP_IGNORES_INCLUSIVE`: Forces headMap to ignore inclusive parameter
/// - `TAIL_MAP_IGNORES_INCLUSIVE`: Forces tailMap to ignore inclusive parameter
///
/// **View faults** (`ConcurrentNavigableMapContract` checks them): a view that is a copy, not backed by the map
/// (`DESCENDING_MAP_IS_A_COPY`, `SUB_MAP_FOUR_ARG_IS_A_COPY`, `KEY_SET_IS_A_COPY` and the others named `*_IS_A_COPY`),
/// a descending map in ascending order (`DESCENDING_MAP_IS_IN_ASCENDING_ORDER`), and a range view whose `null` bound
/// throws `IllegalArgumentException`, not `NullPointerException`
/// (`SUB_MAP_FOUR_ARG_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION` and the others named
/// `*_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION`).
///
/// **Thread-safety faults**: those of [BreakableConcurrentMap], which this class inherits.
///
/// ## Usage Examples
///
/// ### Basic Break Testing
/// ```java
/// BreakableConcurrentNavigableMap<String, Integer> map =
///     BreakableConcurrentNavigableMap.<String, Integer>builder()
///         .addBreak(FIRST_KEY_THROWS_EXCEPTION)
///         .build();
///
/// map.put("key", 1);
/// // This will throw NoSuchElementException instead of returning "key"
/// map.firstKey();
/// ```
///
/// ### Builder Pattern Usage
/// ```java
/// BreakableConcurrentNavigableMap<String, Integer> map =
///     BreakableConcurrentNavigableMap.<String, Integer>builder()
///         .addBreak(LOWER_ENTRY_RETURNS_NULL)
///         .addBreak(SUB_MAP_FOUR_ARG_IS_A_COPY)
///         .doesNotSupport(NavigableMapMethods.DESCENDING_MAP)
///         .build();
/// ```
///
/// ### Wrapping Existing Maps
/// ```java
/// ConcurrentSkipListMap<String, Integer> skipList = new ConcurrentSkipListMap<>();
/// BreakableConcurrentNavigableMap<String, Integer> breakableMap =
///     BreakableConcurrentNavigableMap.wrap(skipList);
/// ```
///
/// ## Thread Safety
///
/// This implementation maintains the thread safety guarantees of the underlying ConcurrentNavigableMap.
/// All break behaviors are applied atomically and do not interfere with the concurrent access patterns
/// of the wrapped map. However, some breaks may simulate race conditions to test concurrent access scenarios.
///
/// ## Method Support
///
/// All ConcurrentNavigableMap methods are supported and can be individually disabled using the
/// MapMethods enum. Unsupported methods will throw UnsupportedOperationException when called.
///
/// @param <K> the type of keys maintained by this map
/// @param <V> the type of mapped values
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableConcurrentMap
/// @see ConcurrentNavigableMap
/// @see Break
public class BreakableConcurrentNavigableMap<K, V> extends BreakableConcurrentMap<K, V>
        implements ConcurrentNavigableMap<K, V>, Serializable {

    /// Break that causes the comparator method to return null instead of the actual comparator.
    /// This can be used to test handling of maps that appear to have no ordering despite being NavigableMap instances.
    ///
    /// **Affected Methods:**
    /// - {@link #comparator()} - Returns null
    public static final Break COMPARATOR_RETURNS_NULL = new Break("COMPARATOR_RETURNS_NULL");

    /// Break that causes the firstKey method to throw NoSuchElementException even when the map is not empty.
    /// This tests error handling for extrema operations in navigable maps.
    ///
    /// **Affected Methods:**
    /// - {@link #firstKey()} - Throws NoSuchElementException
    public static final Break FIRST_KEY_THROWS_EXCEPTION =
            new Break("CONCURRENT_NAVIGABLE_MAP_FIRST_KEY_THROWS_EXCEPTION");

    /// Break that causes the lastKey method to throw NoSuchElementException even when the map is not empty.
    /// This tests error handling for extrema operations in navigable maps.
    ///
    /// **Affected Methods:**
    /// - {@link #lastKey()} - Throws NoSuchElementException
    public static final Break LAST_KEY_THROWS_EXCEPTION =
            new Break("CONCURRENT_NAVIGABLE_MAP_LAST_KEY_THROWS_EXCEPTION");

    /// Break that causes the lowerEntry method to return null even when a lower entry exists.
    /// This tests handling of navigation operations that fail to find existing entries.
    ///
    /// **Affected Methods:**
    /// - {@link #lowerEntry(Object)} - Returns null
    public static final Break LOWER_ENTRY_RETURNS_NULL = new Break("LOWER_ENTRY_RETURNS_NULL");

    /// Break that causes the lowerKey method to return null even when a lower key exists.
    /// This tests handling of navigation operations that fail to find existing keys.
    ///
    /// **Affected Methods:**
    /// - {@link #lowerKey(Object)} - Returns null
    public static final Break LOWER_KEY_RETURNS_NULL = new Break("LOWER_KEY_RETURNS_NULL");

    /// Break that causes the floorEntry method to return null even when a floor entry exists.
    /// This tests handling of navigation operations that fail to find existing entries.
    ///
    /// **Affected Methods:**
    /// - {@link #floorEntry(Object)} - Returns null
    public static final Break FLOOR_ENTRY_RETURNS_NULL = new Break("FLOOR_ENTRY_RETURNS_NULL");

    /// Break that causes the floorKey method to return null even when a floor key exists.
    /// This tests handling of navigation operations that fail to find existing keys.
    ///
    /// **Affected Methods:**
    /// - {@link #floorKey(Object)} - Returns null
    public static final Break FLOOR_KEY_RETURNS_NULL = new Break("FLOOR_KEY_RETURNS_NULL");

    /// Break that causes the ceilingEntry method to return null even when a ceiling entry exists.
    /// This tests handling of navigation operations that fail to find existing entries.
    ///
    /// **Affected Methods:**
    /// - {@link #ceilingEntry(Object)} - Returns null
    public static final Break CEILING_ENTRY_RETURNS_NULL = new Break("CEILING_ENTRY_RETURNS_NULL");

    /// Break that causes the ceilingKey method to return null even when a ceiling key exists.
    /// This tests handling of navigation operations that fail to find existing keys.
    ///
    /// **Affected Methods:**
    /// - {@link #ceilingKey(Object)} - Returns null
    public static final Break CEILING_KEY_RETURNS_NULL = new Break("CEILING_KEY_RETURNS_NULL");

    /// Break that causes the higherEntry method to return null even when a higher entry exists.
    /// This tests handling of navigation operations that fail to find existing entries.
    ///
    /// **Affected Methods:**
    /// - {@link #higherEntry(Object)} - Returns null
    public static final Break HIGHER_ENTRY_RETURNS_NULL = new Break("HIGHER_ENTRY_RETURNS_NULL");

    /// Break that causes the higherKey method to return null even when a higher key exists.
    /// This tests handling of navigation operations that fail to find existing keys.
    ///
    /// **Affected Methods:**
    /// - {@link #higherKey(Object)} - Returns null
    public static final Break HIGHER_KEY_RETURNS_NULL = new Break("HIGHER_KEY_RETURNS_NULL");

    /// Break that causes the firstEntry method to return null even when the map is not empty.
    /// This tests handling of extrema operations that fail unexpectedly.
    ///
    /// **Affected Methods:**
    /// - {@link #firstEntry()} - Returns null
    public static final Break FIRST_ENTRY_RETURNS_NULL = new Break("FIRST_ENTRY_RETURNS_NULL");

    /// Break that causes the lastEntry method to return null even when the map is not empty.
    /// This tests handling of extrema operations that fail unexpectedly.
    ///
    /// **Affected Methods:**
    /// - {@link #lastEntry()} - Returns null
    public static final Break LAST_ENTRY_RETURNS_NULL = new Break("LAST_ENTRY_RETURNS_NULL");

    /// Break that causes the pollFirstEntry method to return null without removing any entry.
    /// This tests handling of polling operations that fail to retrieve entries.
    ///
    /// **Affected Methods:**
    /// - {@link #pollFirstEntry()} - Returns null without removing an entry
    public static final Break POLL_FIRST_ENTRY_RETURNS_NULL = new Break("POLL_FIRST_ENTRY_RETURNS_NULL");

    /// Break that causes the pollLastEntry method to return null without removing any entry.
    /// This tests handling of polling operations that fail to retrieve entries.
    ///
    /// **Affected Methods:**
    /// - {@link #pollLastEntry()} - Returns null without removing an entry
    public static final Break POLL_LAST_ENTRY_RETURNS_NULL = new Break("POLL_LAST_ENTRY_RETURNS_NULL");

    /// Break that causes the descendingMap method to return null instead of a descending view.
    /// This tests handling of view operations that fail to create proper views.
    ///
    /// **Affected Methods:**
    /// - {@link #descendingMap()} - Returns null
    public static final Break DESCENDING_MAP_RETURNS_NULL =
            new Break("CONCURRENT_NAVIGABLE_MAP_DESCENDING_MAP_RETURNS_NULL");

    /// Break that causes the navigableKeySet method to return null instead of a navigable key set.
    /// This tests handling of view operations that fail to create proper key set views.
    ///
    /// **Affected Methods:**
    /// - {@link #navigableKeySet()} - Returns null
    public static final Break NAVIGABLE_KEY_SET_RETURNS_NULL =
            new Break("CONCURRENT_NAVIGABLE_MAP_NAVIGABLE_KEY_SET_RETURNS_NULL");

    /// Break that causes the descendingKeySet method to return null instead of a descending key set.
    /// This tests handling of view operations that fail to create proper descending key set views.
    ///
    /// **Affected Methods:**
    /// - {@link #descendingKeySet()} - Returns null
    public static final Break DESCENDING_KEY_SET_RETURNS_NULL =
            new Break("CONCURRENT_NAVIGABLE_MAP_DESCENDING_KEY_SET_RETURNS_NULL");

    /// Break that causes all subMap methods to return null instead of creating submaps.
    /// This tests handling of submap operations that fail to create proper submap views.
    ///
    /// **Affected Methods:**
    /// - {@link #subMap(Object, boolean, Object, boolean)} - Returns null
    /// - {@link #subMap(Object, Object)} - Returns null
    public static final Break SUB_MAP_RETURNS_NULL = new Break("CONCURRENT_NAVIGABLE_MAP_SUB_MAP_RETURNS_NULL");

    /// Break that causes all headMap methods to return null instead of creating head maps.
    /// This tests handling of submap operations that fail to create proper head map views.
    ///
    /// **Affected Methods:**
    /// - {@link #headMap(Object, boolean)} - Returns null
    /// - {@link #headMap(Object)} - Returns null
    public static final Break HEAD_MAP_RETURNS_NULL = new Break("CONCURRENT_NAVIGABLE_MAP_HEAD_MAP_RETURNS_NULL");

    /// Break that causes all tailMap methods to return null instead of creating tail maps.
    /// This tests handling of submap operations that fail to create proper tail map views.
    ///
    /// **Affected Methods:**
    /// - {@link #tailMap(Object, boolean)} - Returns null
    /// - {@link #tailMap(Object)} - Returns null
    public static final Break TAIL_MAP_RETURNS_NULL = new Break("CONCURRENT_NAVIGABLE_MAP_TAIL_MAP_RETURNS_NULL");

    /// Break that causes the four-argument subMap to ignore the specified bounds and return a view of the entire
    /// map, from its first key to its last, both inclusive. On an empty map it throws
    /// [java.util.NoSuchElementException] (from `firstKey()`). The two-argument subMap is not changed.
    /// This tests handling of submap operations that fail to properly respect bounds.
    ///
    /// **Affected Methods:**
    /// - {@link #subMap(Object, boolean, Object, boolean)} - Returns the whole map
    public static final Break SUB_MAP_IGNORES_BOUNDS = new Break("SUB_MAP_IGNORES_BOUNDS");

    /// Break that causes the two-argument headMap to ignore the inclusive parameter and always use an inclusive
    /// bound. The one-argument headMap is not changed.
    /// This tests handling of submap operations that fail to properly respect inclusion flags.
    ///
    /// **Affected Methods:**
    /// - {@link #headMap(Object, boolean)} - Treats `inclusive` as true
    public static final Break HEAD_MAP_IGNORES_INCLUSIVE = new Break("HEAD_MAP_IGNORES_INCLUSIVE");

    /// Break that causes the two-argument tailMap to ignore the inclusive parameter and always use an inclusive
    /// bound. The one-argument tailMap is not changed.
    /// This tests handling of submap operations that fail to properly respect inclusion flags.
    ///
    /// **Affected Methods:**
    /// - {@link #tailMap(Object, boolean)} - Treats `inclusive` as true
    public static final Break TAIL_MAP_IGNORES_INCLUSIVE = new Break("TAIL_MAP_IGNORES_INCLUSIVE");

    /// `descendingMap()` returns a copy: a new [ConcurrentSkipListMap] holding the view's mappings, in its order, that
    /// is not backed by the map.
    ///
    /// **Affected Methods:**
    /// - {@link #descendingMap()} - Returns a copy
    /// - `reversed()` - Returns a copy, through `NavigableMap`'s default, which returns `descendingMap()`
    public static final Break DESCENDING_MAP_IS_A_COPY = new Break("DESCENDING_MAP_IS_A_COPY");

    /// `subMap(K, boolean, K, boolean)` returns a copy: a new [ConcurrentSkipListMap] holding the view's mappings, in
    /// its order, that is not backed by the map, and does not refuse a key outside the view's range.
    ///
    /// **Affected Methods:**
    /// - {@link #subMap(Object, boolean, Object, boolean)} - Returns a copy
    public static final Break SUB_MAP_FOUR_ARG_IS_A_COPY = new Break("SUB_MAP_FOUR_ARG_IS_A_COPY");

    /// `subMap(K, K)` returns a copy: a new [ConcurrentSkipListMap] holding the view's mappings, in its order, that is
    /// not backed by the map, and does not refuse a key outside the view's range.
    ///
    /// **Affected Methods:**
    /// - {@link #subMap(Object, Object)} - Returns a copy
    public static final Break SUB_MAP_IS_A_COPY = new Break("SUB_MAP_IS_A_COPY");

    /// `headMap(K, boolean)` returns a copy: a new [ConcurrentSkipListMap] holding the view's mappings, in its order,
    /// that is not backed by the map, and does not refuse a key outside the view's range.
    ///
    /// **Affected Methods:**
    /// - {@link #headMap(Object, boolean)} - Returns a copy
    public static final Break HEAD_MAP_TWO_ARG_IS_A_COPY = new Break("HEAD_MAP_TWO_ARG_IS_A_COPY");

    /// `headMap(K)` returns a copy: a new [ConcurrentSkipListMap] holding the view's mappings, in its order, that is
    /// not backed by the map, and does not refuse a key outside the view's range.
    ///
    /// **Affected Methods:**
    /// - {@link #headMap(Object)} - Returns a copy
    public static final Break HEAD_MAP_IS_A_COPY = new Break("HEAD_MAP_IS_A_COPY");

    /// `tailMap(K, boolean)` returns a copy: a new [ConcurrentSkipListMap] holding the view's mappings, in its order,
    /// that is not backed by the map, and does not refuse a key outside the view's range.
    ///
    /// **Affected Methods:**
    /// - {@link #tailMap(Object, boolean)} - Returns a copy
    public static final Break TAIL_MAP_TWO_ARG_IS_A_COPY = new Break("TAIL_MAP_TWO_ARG_IS_A_COPY");

    /// `tailMap(K)` returns a copy: a new [ConcurrentSkipListMap] holding the view's mappings, in its order, that is
    /// not backed by the map, and does not refuse a key outside the view's range.
    ///
    /// **Affected Methods:**
    /// - {@link #tailMap(Object)} - Returns a copy
    public static final Break TAIL_MAP_IS_A_COPY = new Break("TAIL_MAP_IS_A_COPY");

    /// `navigableKeySet()` returns a copy: a new [ConcurrentSkipListSet] of the keys, in the view's order, that is not
    /// backed by the map and supports `add`.
    ///
    /// **Affected Methods:**
    /// - {@link #navigableKeySet()} - Returns a copy
    public static final Break NAVIGABLE_KEY_SET_IS_A_COPY = new Break("NAVIGABLE_KEY_SET_IS_A_COPY");

    /// `keySet()` returns a copy: a new [ConcurrentSkipListSet] of the keys, in the view's order, that is not backed
    /// by the map and supports `add`.
    ///
    /// **Affected Methods:**
    /// - {@link #keySet()} - Returns a copy
    public static final Break KEY_SET_IS_A_COPY = new Break("KEY_SET_IS_A_COPY");

    /// `descendingKeySet()` returns a copy: a new [ConcurrentSkipListSet] of the keys, in the view's order, that is not
    /// backed by the map and supports `add`.
    ///
    /// **Affected Methods:**
    /// - {@link #descendingKeySet()} - Returns a copy
    public static final Break DESCENDING_KEY_SET_IS_A_COPY = new Break("DESCENDING_KEY_SET_IS_A_COPY");

    /// `descendingMap()` returns the store itself: backed by the map, but in the map's own order, not the reverse.
    ///
    /// **Affected Methods:**
    /// - {@link #descendingMap()} - Returns a view in ascending order
    /// - `reversed()` - Returns a view in ascending order, through `NavigableMap`'s default, which returns
    ///   `descendingMap()`
    public static final Break DESCENDING_MAP_IS_IN_ASCENDING_ORDER = new Break("DESCENDING_MAP_IS_IN_ASCENDING_ORDER");

    /// `subMap(K, boolean, K, boolean)` throws `IllegalArgumentException`, not `NullPointerException`, for a `null`
    /// bound.
    ///
    /// **Affected Methods:**
    /// - {@link #subMap(Object, boolean, Object, boolean)} - Throws `IllegalArgumentException` for a `null` bound
    public static final Break SUB_MAP_FOUR_ARG_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION =
            new Break("SUB_MAP_FOUR_ARG_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION");

    /// `subMap(K, K)` throws `IllegalArgumentException`, not `NullPointerException`, for a `null` bound.
    ///
    /// **Affected Methods:**
    /// - {@link #subMap(Object, Object)} - Throws `IllegalArgumentException` for a `null` bound
    public static final Break SUB_MAP_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION =
            new Break("SUB_MAP_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION");

    /// `headMap(K, boolean)` throws `IllegalArgumentException`, not `NullPointerException`, for a `null` bound.
    ///
    /// **Affected Methods:**
    /// - {@link #headMap(Object, boolean)} - Throws `IllegalArgumentException` for a `null` bound
    public static final Break HEAD_MAP_TWO_ARG_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION =
            new Break("HEAD_MAP_TWO_ARG_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION");

    /// `headMap(K)` throws `IllegalArgumentException`, not `NullPointerException`, for a `null` bound.
    ///
    /// **Affected Methods:**
    /// - {@link #headMap(Object)} - Throws `IllegalArgumentException` for a `null` bound
    public static final Break HEAD_MAP_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION =
            new Break("HEAD_MAP_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION");

    /// `tailMap(K, boolean)` throws `IllegalArgumentException`, not `NullPointerException`, for a `null` bound.
    ///
    /// **Affected Methods:**
    /// - {@link #tailMap(Object, boolean)} - Throws `IllegalArgumentException` for a `null` bound
    public static final Break TAIL_MAP_TWO_ARG_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION =
            new Break("TAIL_MAP_TWO_ARG_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION");

    /// `tailMap(K)` throws `IllegalArgumentException`, not `NullPointerException`, for a `null` bound.
    ///
    /// **Affected Methods:**
    /// - {@link #tailMap(Object)} - Throws `IllegalArgumentException` for a `null` bound
    public static final Break TAIL_MAP_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION =
            new Break("TAIL_MAP_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION");


    /// Creates a new BreakableConcurrentNavigableMap with no breaks, wrapping a new ConcurrentSkipListMap.
    /// This constructor provides a default implementation suitable for most testing scenarios.
    @SuppressWarnings("SortedCollectionWithNonComparableKeys")
    public BreakableConcurrentNavigableMap() {
        this(new ConcurrentSkipListMap<>(), new HashSet<>(), new HashMap<>(), NO_NULL_PERMITS);
    }

    /// Creates a new BreakableConcurrentNavigableMap with no breaks, wrapping the specified ConcurrentNavigableMap.
    /// The wrapped map will be used as the underlying implementation for all operations.
    ///
    /// @param concurrentNavigableMapArg the ConcurrentNavigableMap to wrap and delegate operations to
    public BreakableConcurrentNavigableMap(final ConcurrentNavigableMap<K, V> concurrentNavigableMapArg) {
        this(concurrentNavigableMapArg, new HashSet<>(), new HashMap<>(), NO_NULL_PERMITS);
    }

    /// Creates a new BreakableConcurrentNavigableMap with the specified breaks, wrapping the specified
    /// ConcurrentNavigableMap. The builder uses this protected constructor internally to create instances with
    /// specific break configurations.
    ///
    /// @param concurrentNavigableMapArg the ConcurrentNavigableMap to wrap and delegate operations to
    /// @param breaks the collection of breaks to apply to this instance
    /// @param methodStatuses the map of method statuses to track break application
    /// @param permits the flags that say which keys and values the map permits
    protected BreakableConcurrentNavigableMap(final ConcurrentNavigableMap<K, V> concurrentNavigableMapArg,
                                              final Set<Break> breaks,
                                              final Map<InterfaceMethod, MethodStatus> methodStatuses,
                                              final int permits) {
        super(concurrentNavigableMapArg, breaks, methodStatuses, permits);
    }

    /// Creates a new BreakableConcurrentNavigableMap as a copy of another BreakableConcurrentNavigableMap.
    /// The new instance will have the same breaks and configuration as the original, but will wrap
    /// a new ConcurrentSkipListMap containing the same key-value mappings.
    ///
    /// @param other the BreakableConcurrentNavigableMap to copy
    public BreakableConcurrentNavigableMap(final BreakableConcurrentNavigableMap<K, V> other) {
        this(new ConcurrentSkipListMap<>(other.navigableStore()), new HashSet<>(other.breaks()),
                new HashMap<>(other.methodStatuses()), other.permits());
    }

    /// Returns the store, which is always a [ConcurrentNavigableMap] for this class.
    ///
    /// @return the store
    private ConcurrentNavigableMap<K, V> navigableStore() {
        return (ConcurrentNavigableMap<K, V>) store();
    }

    /// Copies the store into a [ConcurrentSkipListMap] with the same comparator, so that a clone has a store of
    /// its own.
    ///
    /// @param storeArg the store to copy
    /// @return a new store holding the same mappings, in the same order
    @Override
    protected Map<K, V> copyOfStore(final Map<K, V> storeArg) {
        return new ConcurrentSkipListMap<>((ConcurrentNavigableMap<K, V>) storeArg);
    }

    /// Creates a new BreakableConcurrentNavigableMap that wraps the specified ConcurrentNavigableMap.
    /// This is a convenience factory method equivalent to using the single-argument constructor.
    ///
    /// @param <K> the type of keys maintained by this map
    /// @param <V> the type of mapped values
    /// @param concurrentNavigableMap the ConcurrentNavigableMap to wrap
    /// @return a new BreakableConcurrentNavigableMap wrapping the specified map
    public static <K, V> BreakableConcurrentNavigableMap<K, V> wrap(
            final ConcurrentNavigableMap<K, V> concurrentNavigableMap) {
        return new BreakableConcurrentNavigableMap<>(concurrentNavigableMap);
    }

    /// Creates a new Builder for configuring and creating BreakableConcurrentNavigableMap instances.
    /// The builder provides a fluent interface for adding breaks and configuring method support.
    ///
    /// @param <K> the type of keys maintained by this map
    /// @param <V> the type of mapped values
    /// @return a new Builder instance
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    /// Retrieves the comparator used to order the keys in this map, or `null` if the keys are
    /// ordered based on their natural ordering.
    ///
    /// ## Purpose
    /// This method is a standard implementation of [java.util.SortedMap#comparator()], allowing clients
    /// to retrieve the comparator that governs the ordering of entries in this map.
    ///
    /// ## Usage Examples
    /// ```java
    ///     BreakableConcurrentNavigableMap<String, Integer> map =
    ///         BreakableConcurrentNavigableMap.wrap(new ConcurrentSkipListMap<>());
    ///     Comparator<? super String> comparator = map.comparator();
    ///     assertNull(comparator); // natural ordering
    /// ```
    ///
    /// ## Effect of Breaks
    /// If the break `COMPARATOR_RETURNS_NULL` is active, this method will always return `null`,
    /// regardless of the actual comparator configuration for the underlying map.
    ///
    /// ## Thread Safety
    /// This method is thread-safe as it delegates to the underlying `ConcurrentNavigableMap`,
    /// which guarantees concurrent access safety.
    ///
    /// ## Performance
    /// This method provides constant-time performance under normal circumstances as it retrieves the
    /// comparator reference from the underlying implementation.
    ///
    /// @return the comparator used to order the keys in this map, or `null` if the map uses
    ///         the keys' natural ordering.
    /// @see java.util.SortedMap#comparator()
    /// @see ConcurrentNavigableMap#comparator()
    /// @since 1.0
    @Override
    public @Nullable Comparator<? super K> comparator() {
        checkMethodSupport(SortedMapMethods.COMPARATOR);

        if (hasBreak(COMPARATOR_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().comparator();
    }

    @Override
    public K firstKey() {
        checkMethodSupport(SortedMapMethods.FIRST_KEY);
        if (hasBreak(FIRST_KEY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("Break: firstKey() throws exception");
        }
        return navigableStore().firstKey();
    }

    @Override
    public K lastKey() {
        checkMethodSupport(SortedMapMethods.LAST_KEY);

        if (hasBreak(LAST_KEY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("Break: lastKey() throws exception");
        }
        return navigableStore().lastKey();
    }

    @Override
    public Map.@Nullable Entry<K, V> lowerEntry(final K key) {
        checkMethodSupport(NavigableMapMethods.LOWER_ENTRY);
        if (hasBreak(LOWER_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().lowerEntry(key);
    }

    @Override
    public @Nullable K lowerKey(final K key) {
        checkMethodSupport(NavigableMapMethods.LOWER_KEY);
        if (hasBreak(LOWER_KEY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().lowerKey(key);
    }

    @Override
    public Map.@Nullable Entry<K, V> floorEntry(final K key) {
        checkMethodSupport(NavigableMapMethods.FLOOR_ENTRY);
        if (hasBreak(FLOOR_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().floorEntry(key);
    }

    @Override
    public @Nullable K floorKey(final K key) {
        checkMethodSupport(NavigableMapMethods.FLOOR_KEY);
        if (hasBreak(FLOOR_KEY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().floorKey(key);
    }

    @Override
    public Map.@Nullable Entry<K, V> ceilingEntry(final K key) {
        checkMethodSupport(NavigableMapMethods.CEILING_ENTRY);
        if (hasBreak(CEILING_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().ceilingEntry(key);
    }

    @Override
    public @Nullable K ceilingKey(final K key) {
        checkMethodSupport(NavigableMapMethods.CEILING_KEY);
        if (hasBreak(CEILING_KEY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().ceilingKey(key);
    }

    @Override
    public Map.@Nullable Entry<K, V> higherEntry(final K key) {
        checkMethodSupport(NavigableMapMethods.HIGHER_ENTRY);
        if (hasBreak(HIGHER_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().higherEntry(key);
    }

    @Override
    public @Nullable K higherKey(final K key) {
        checkMethodSupport(NavigableMapMethods.HIGHER_KEY);
        if (hasBreak(HIGHER_KEY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().higherKey(key);
    }

    @Override
    public Map.@Nullable Entry<K, V> firstEntry() {
        checkMethodSupport(NavigableMapMethods.FIRST_ENTRY);
        if (hasBreak(FIRST_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().firstEntry();
    }

    @Override
    public Map.@Nullable Entry<K, V> lastEntry() {
        checkMethodSupport(NavigableMapMethods.LAST_ENTRY);
        if (hasBreak(LAST_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().lastEntry();
    }

    @Override
    public Map.@Nullable Entry<K, V> pollFirstEntry() {
        checkMethodSupport(NavigableMapMethods.POLL_FIRST_ENTRY);
        if (hasBreak(POLL_FIRST_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().pollFirstEntry();
    }

    @Override
    public Map.@Nullable Entry<K, V> pollLastEntry() {
        checkMethodSupport(NavigableMapMethods.POLL_LAST_ENTRY);
        if (hasBreak(POLL_LAST_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return navigableStore().pollLastEntry();
    }

    @Override
    public @Nullable ConcurrentNavigableMap<K, V> descendingMap() {
        checkMethodSupport(NavigableMapMethods.DESCENDING_MAP);
        if (hasBreak(DESCENDING_MAP_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(DESCENDING_MAP_IS_IN_ASCENDING_ORDER)) {
            return navigableStore();
        }
        return copyIf(DESCENDING_MAP_IS_A_COPY, navigableStore().descendingMap());
    }

    @Override
    public @Nullable NavigableSet<K> navigableKeySet() {
        checkMethodSupport(NavigableMapMethods.NAVIGABLE_KEY_SET);
        if (hasBreak(NAVIGABLE_KEY_SET_RETURNS_NULL)) {
            return null;
        }
        return WeakConsistencyViews.navigableSet(
                copyIf(NAVIGABLE_KEY_SET_IS_A_COPY, navigableStore().navigableKeySet()), breaks());
    }

    /// Returns a [NavigableSet] view of the keys, as `navigableKeySet()` does.
    ///
    /// Breaks: [#KEY_SET_IS_A_COPY], and [BreakableMap#KEY_SET_RETURNS_EMPTY_SET] (an empty set) and
    /// [BreakableMap#KEY_SET_RETURNS_NULL_WHEN_EMPTY] (`null` when the map is empty). `navigableKeySet()`'s breaks
    /// do not affect it.
    ///
    /// @return the keys
    @Override
    public @Nullable NavigableSet<K> keySet() {
        checkMethodSupport(MapMethods.KEY_SET);
        if (hasBreak(KEY_SET_RETURNS_EMPTY_SET)) {
            return Collections.emptyNavigableSet();
        }
        if (hasBreak(KEY_SET_RETURNS_NULL_WHEN_EMPTY) && navigableStore().isEmpty()) {
            return null;
        }
        return WeakConsistencyViews.navigableSet(copyIf(KEY_SET_IS_A_COPY, navigableStore().keySet()), breaks());
    }

    @Override
    public @Nullable NavigableSet<K> descendingKeySet() {
        checkMethodSupport(NavigableMapMethods.DESCENDING_KEY_SET);
        if (hasBreak(DESCENDING_KEY_SET_RETURNS_NULL)) {
            return null;
        }
        return WeakConsistencyViews.navigableSet(
                copyIf(DESCENDING_KEY_SET_IS_A_COPY, navigableStore().descendingKeySet()), breaks());
    }

    @Override
    public @Nullable ConcurrentNavigableMap<K, V> subMap(final K fromKey, final boolean fromInclusive,
                                                         final K toKey, final boolean toInclusive) {
        checkMethodSupport(NavigableMapMethods.SUB_MAP_FOUR_ARG);
        checkBounds(SUB_MAP_FOUR_ARG_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION, fromKey, toKey);
        if (hasBreak(SUB_MAP_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(SUB_MAP_IGNORES_BOUNDS)) {
            // The whole map, from its first key to its last; firstKey() throws on an empty map.
            return navigableStore().subMap(navigableStore().firstKey(), true, navigableStore().lastKey(), true);
        }
        return copyIf(SUB_MAP_FOUR_ARG_IS_A_COPY,
                navigableStore().subMap(fromKey, fromInclusive, toKey, toInclusive));
    }

    @Override
    public @Nullable ConcurrentNavigableMap<K, V> headMap(final K toKey, final boolean inclusive) {
        checkMethodSupport(NavigableMapMethods.HEAD_MAP_TWO_ARG);
        checkBounds(HEAD_MAP_TWO_ARG_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION, toKey);
        if (hasBreak(HEAD_MAP_RETURNS_NULL)) {
            return null;
        }
        return copyIf(HEAD_MAP_TWO_ARG_IS_A_COPY,
                navigableStore().headMap(toKey, inclusive || hasBreak(HEAD_MAP_IGNORES_INCLUSIVE)));
    }

    @Override
    public @Nullable ConcurrentNavigableMap<K, V> tailMap(final K fromKey, final boolean inclusive) {
        checkMethodSupport(NavigableMapMethods.TAIL_MAP_TWO_ARG);
        checkBounds(TAIL_MAP_TWO_ARG_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION, fromKey);
        if (hasBreak(TAIL_MAP_RETURNS_NULL)) {
            return null;
        }
        return copyIf(TAIL_MAP_TWO_ARG_IS_A_COPY,
                navigableStore().tailMap(fromKey, inclusive || hasBreak(TAIL_MAP_IGNORES_INCLUSIVE)));
    }

    @Override
    public @Nullable ConcurrentNavigableMap<K, V> subMap(final K fromKey, final K toKey) {
        checkMethodSupport(SortedMapMethods.SUB_MAP);
        checkBounds(SUB_MAP_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION, fromKey, toKey);
        if (hasBreak(SUB_MAP_RETURNS_NULL)) {
            return null;
        }
        return copyIf(SUB_MAP_IS_A_COPY, navigableStore().subMap(fromKey, toKey));
    }

    @Override
    public @Nullable ConcurrentNavigableMap<K, V> headMap(final K toKey) {
        checkMethodSupport(SortedMapMethods.HEAD_MAP);
        checkBounds(HEAD_MAP_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION, toKey);
        if (hasBreak(HEAD_MAP_RETURNS_NULL)) {
            return null;
        }
        return copyIf(HEAD_MAP_IS_A_COPY, navigableStore().headMap(toKey));
    }

    @Override
    public @Nullable ConcurrentNavigableMap<K, V> tailMap(final K fromKey) {
        checkMethodSupport(SortedMapMethods.TAIL_MAP);
        checkBounds(TAIL_MAP_NULL_BOUND_THROWS_WRONG_NULL_EXCEPTION, fromKey);
        if (hasBreak(TAIL_MAP_RETURNS_NULL)) {
            return null;
        }
        return copyIf(TAIL_MAP_IS_A_COPY, navigableStore().tailMap(fromKey));
    }

    /// Returns a view, or, with a copy break set, a copy of it that is not backed by the map.
    ///
    /// @param copyBreak the break that makes it a copy
    /// @param view the view
    /// @return the view, or a copy of it
    private ConcurrentNavigableMap<K, V> copyIf(final Break copyBreak, final ConcurrentNavigableMap<K, V> view) {
        return hasBreak(copyBreak) ? new ConcurrentSkipListMap<>(view) : view;
    }

    /// Returns a key set, or, with a copy break set, a copy of it that is not backed by the map.
    ///
    /// @param copyBreak the break that makes it a copy
    /// @param view the key set
    /// @return the key set, or a copy of it
    private NavigableSet<K> copyIf(final Break copyBreak, final NavigableSet<K> view) {
        return hasBreak(copyBreak) ? new ConcurrentSkipListSet<>(view) : view;
    }

    /// Throws for a `null` bound, where the map refuses null keys: `NullPointerException`, or, with the break set,
    /// `IllegalArgumentException`.
    ///
    /// @param wrongNull the break that throws the wrong exception
    /// @param bounds the bounds
    private void checkBounds(final Break wrongNull, final @Nullable Object... bounds) {
        for (final Object bound : bounds) {
            if (bound == null && !permitsNullKeys()) {
                throw hasBreak(wrongNull)
                        ? new IllegalArgumentException("bound is null") : new NullPointerException("bound is null");
            }
        }
    }

    /// Builder for creating BreakableConcurrentNavigableMap instances with fluent configuration.
    ///
    /// It allows for method chaining to configure breaks, method support, and the entries the map starts with.
    ///
    /// @param <K> the type of keys maintained by this map
    /// @param <V> the type of mapped values
    public static class Builder<K, V>
            extends BreakableConcurrentMap.AbstractBuilder<Builder<K, V>, BreakableConcurrentNavigableMap<K, V>, K, V> {

        /// The comparator of the built map's store, or `null` for natural order.
        private @Nullable Comparator<? super K> comparator;

        /// Creates a builder with no entries.
        public Builder() {
            super();
        }

        /// Creates a copy of another builder, with its comparator.
        ///
        /// @param other the builder to copy
        public Builder(final Builder<K, V> other) {
            super(other);
            this.comparator = other.comparator;
        }

        /// Creates a builder that starts with the entries of a map.
        ///
        /// @param map the entries to start with
        public Builder(final Map<K, V> map) {
            super(map);
        }

        /// Sets the comparator that orders the built map's keys.
        ///
        /// @param comparatorArg the comparator
        /// @return this builder
        public Builder<K, V> setComparator(final Comparator<? super K> comparatorArg) {
            this.comparator = comparatorArg;
            return self();
        }

        @Override
        public Builder<K, V> self() {
            return this;
        }

        @Override
        public Builder<K, V> copy() {
            return new Builder<>(this);
        }

        @Override
        public BreakableConcurrentNavigableMap<K, V> copy(
                final BreakableConcurrentNavigableMap<K, V> other) {
            return new BreakableConcurrentNavigableMap<>(other);
        }

        @SuppressWarnings("SortedCollectionWithNonComparableKeys")
        @Override
        public BreakableConcurrentNavigableMap<K, V> build(final Map<K, V> entries) {
            ConcurrentNavigableMap<K, V> map = new ConcurrentSkipListMap<>(comparator);
            map.putAll(elements());
            map.putAll(entries);
            return new BreakableConcurrentNavigableMap<>(map, new HashSet<>(breaks()),
                    new HashMap<>(methodStatuses()), permits());
        }
    }
}
