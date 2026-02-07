package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.map.MapMethods;
import org.soliscode.test.contract.navigablemap.NavigableMapMethods;
import org.soliscode.test.contract.sortedmap.SortedMapMethods;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

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
/// **Concurrency Breaks**: Simulate race conditions and concurrent access issues
/// - `NAVIGATION_RACE_CONDITION`: Simulates race conditions in navigation operations
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
///         .addBreak(NAVIGATION_RACE_CONDITION)
///         .doesNotSupport(MapMethods.DescendingMap)
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
    public static final Break COMPARATOR_RETURNS_NULL = new Break("COMPARATOR_RETURNS_NULL");

    /// Break that causes the firstKey method to throw NoSuchElementException even when the map is not empty.
    /// This tests error handling for extrema operations in navigable maps.
    public static final Break FIRST_KEY_THROWS_EXCEPTION = new Break("FIRST_KEY_THROWS_EXCEPTION");

    /// Break that causes the lastKey method to throw NoSuchElementException even when the map is not empty.
    /// This tests error handling for extrema operations in navigable maps.
    public static final Break LAST_KEY_THROWS_EXCEPTION = new Break("LAST_KEY_THROWS_EXCEPTION");

    /// Break that causes the lowerEntry method to return null even when a lower entry exists.
    /// This tests handling of navigation operations that fail to find existing entries.
    public static final Break LOWER_ENTRY_RETURNS_NULL = new Break("LOWER_ENTRY_RETURNS_NULL");

    /// Break that causes the lowerKey method to return null even when a lower key exists.
    /// This tests handling of navigation operations that fail to find existing keys.
    public static final Break LOWER_KEY_RETURNS_NULL = new Break("LOWER_KEY_RETURNS_NULL");

    /// Break that causes the floorEntry method to return null even when a floor entry exists.
    /// This tests handling of navigation operations that fail to find existing entries.
    public static final Break FLOOR_ENTRY_RETURNS_NULL = new Break("FLOOR_ENTRY_RETURNS_NULL");

    /// Break that causes the floorKey method to return null even when a floor key exists.
    /// This tests handling of navigation operations that fail to find existing keys.
    public static final Break FLOOR_KEY_RETURNS_NULL = new Break("FLOOR_KEY_RETURNS_NULL");

    /// Break that causes the ceilingEntry method to return null even when a ceiling entry exists.
    /// This tests handling of navigation operations that fail to find existing entries.
    public static final Break CEILING_ENTRY_RETURNS_NULL = new Break("CEILING_ENTRY_RETURNS_NULL");

    /// Break that causes the ceilingKey method to return null even when a ceiling key exists.
    /// This tests handling of navigation operations that fail to find existing keys.
    public static final Break CEILING_KEY_RETURNS_NULL = new Break("CEILING_KEY_RETURNS_NULL");

    /// Break that causes the higherEntry method to return null even when a higher entry exists.
    /// This tests handling of navigation operations that fail to find existing entries.
    public static final Break HIGHER_ENTRY_RETURNS_NULL = new Break("HIGHER_ENTRY_RETURNS_NULL");

    /// Break that causes the higherKey method to return null even when a higher key exists.
    /// This tests handling of navigation operations that fail to find existing keys.
    public static final Break HIGHER_KEY_RETURNS_NULL = new Break("HIGHER_KEY_RETURNS_NULL");

    /// Break that causes the firstEntry method to return null even when the map is not empty.
    /// This tests handling of extrema operations that fail unexpectedly.
    public static final Break FIRST_ENTRY_RETURNS_NULL = new Break("FIRST_ENTRY_RETURNS_NULL");

    /// Break that causes the lastEntry method to return null even when the map is not empty.
    /// This tests handling of extrema operations that fail unexpectedly.
    public static final Break LAST_ENTRY_RETURNS_NULL = new Break("LAST_ENTRY_RETURNS_NULL");

    /// Break that causes the pollFirstEntry method to return null without removing any entry.
    /// This tests handling of polling operations that fail to retrieve entries.
    public static final Break POLL_FIRST_ENTRY_RETURNS_NULL = new Break("POLL_FIRST_ENTRY_RETURNS_NULL");

    /// Break that causes the pollLastEntry method to return null without removing any entry.
    /// This tests handling of polling operations that fail to retrieve entries.
    public static final Break POLL_LAST_ENTRY_RETURNS_NULL = new Break("POLL_LAST_ENTRY_RETURNS_NULL");

    /// Break that causes the descendingMap method to return null instead of a descending view.
    /// This tests handling of view operations that fail to create proper views.
    public static final Break DESCENDING_MAP_RETURNS_NULL = new Break("DESCENDING_MAP_RETURNS_NULL");

    /// Break that causes the navigableKeySet method to return null instead of a navigable key set.
    /// This tests handling of view operations that fail to create proper key set views.
    public static final Break NAVIGABLE_KEY_SET_RETURNS_NULL = new Break("NAVIGABLE_KEY_SET_RETURNS_NULL");

    /// Break that causes the descendingKeySet method to return null instead of a descending key set.
    /// This tests handling of view operations that fail to create proper descending key set views.
    public static final Break DESCENDING_KEY_SET_RETURNS_NULL = new Break("DESCENDING_KEY_SET_RETURNS_NULL");

    /// Break that causes all subMap methods to return null instead of creating submaps.
    /// This tests handling of submap operations that fail to create proper submap views.
    public static final Break SUB_MAP_RETURNS_NULL = new Break("SUB_MAP_RETURNS_NULL");

    /// Break that causes all headMap methods to return null instead of creating head maps.
    /// This tests handling of submap operations that fail to create proper head map views.
    public static final Break HEAD_MAP_RETURNS_NULL = new Break("HEAD_MAP_RETURNS_NULL");

    /// Break that causes all tailMap methods to return null instead of creating tail maps.
    /// This tests handling of submap operations that fail to create proper tail map views.
    public static final Break TAIL_MAP_RETURNS_NULL = new Break("TAIL_MAP_RETURNS_NULL");

    /// Break that causes subMap operations to ignore the specified bounds and return the entire map.
    /// This tests handling of submap operations that fail to properly respect bounds.
    public static final Break SUB_MAP_IGNORES_BOUNDS = new Break("SUB_MAP_IGNORES_BOUNDS");

    /// Break that causes headMap operations to ignore the inclusive parameter and always use inclusive bounds.
    /// This tests handling of submap operations that fail to properly respect inclusion flags.
    public static final Break HEAD_MAP_IGNORES_INCLUSIVE = new Break("HEAD_MAP_IGNORES_INCLUSIVE");

    /// Break that causes tailMap operations to ignore the inclusive parameter and always use inclusive bounds.
    /// This tests handling of submap operations that fail to properly respect inclusion flags.
    public static final Break TAIL_MAP_IGNORES_INCLUSIVE = new Break("TAIL_MAP_IGNORES_INCLUSIVE");

    /// Break that simulates race conditions in navigation operations by calling the method twice.
    /// This tests handling of concurrent access patterns and potential race conditions.
    public static final Break NAVIGATION_RACE_CONDITION = new Break("NAVIGATION_RACE_CONDITION");

    private final ConcurrentNavigableMap<K, V> concurrentNavigableMap;

    /// Creates a new BreakableConcurrentNavigableMap with no breaks, wrapping a new ConcurrentSkipListMap.
    /// This constructor provides a default implementation suitable for most testing scenarios.
    public BreakableConcurrentNavigableMap() {
        this(new ConcurrentSkipListMap<>(), new HashSet<>(), new HashMap<>(), DEFAULT_PERMITS);
    }

    /// Creates a new BreakableConcurrentNavigableMap with no breaks, wrapping the specified ConcurrentNavigableMap.
    /// The wrapped map will be used as the underlying implementation for all operations.
    ///
    /// @param concurrentNavigableMap the ConcurrentNavigableMap to wrap and delegate operations to
    public BreakableConcurrentNavigableMap(final ConcurrentNavigableMap<K, V> concurrentNavigableMap) {
        super(concurrentNavigableMap, new HashSet<>(), new HashMap<>(), DEFAULT_PERMITS);
        this.concurrentNavigableMap = concurrentNavigableMap;
    }

    /// Creates a new BreakableConcurrentNavigableMap with the specified breaks, wrapping the specified ConcurrentNavigableMap.
    /// This protected constructor is used internally by the builder to create instances with specific break configurations.
    ///
    /// @param concurrentNavigableMap the ConcurrentNavigableMap to wrap and delegate operations to
    /// @param breaks the collection of breaks to apply to this instance
    protected BreakableConcurrentNavigableMap(final ConcurrentNavigableMap<K, V> concurrentNavigableMap,
                                              final Set<Break> breaks,
                                              final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
                                              final int permits) {
        super(this.concurrentNavigableMap =  concurrentNavigableMap, breaks, methodStatuses, permits);
    }

    /// Creates a new BreakableConcurrentNavigableMap as a copy of another BreakableConcurrentNavigableMap.
    /// The new instance will have the same breaks and configuration as the original, but will wrap
    /// a new ConcurrentSkipListMap containing the same key-value mappings.
    ///
    /// @param other the BreakableConcurrentNavigableMap to copy
    public BreakableConcurrentNavigableMap(final BreakableConcurrentNavigableMap<K, V> other) {
        super(other);
        this.concurrentNavigableMap = new ConcurrentSkipListMap<>(other.concurrentNavigableMap);
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
    ///     System.out.println(comparator == null ? "Natural ordering" : "Custom comparator in use.");
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
        return concurrentNavigableMap.comparator();
    }

    @Override
    public K firstKey() {
        checkMethodSupport(SortedMapMethods.FIRST_KEY);
        if (hasBreak(FIRST_KEY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("Break: firstKey() throws exception");
        }
        return concurrentNavigableMap.firstKey();
    }

    @Override
    public K lastKey() {
        checkMethodSupport(SortedMapMethods.LAST_KEY);

        if (hasBreak(LAST_KEY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("Break: lastKey() throws exception");
        }
        return concurrentNavigableMap.lastKey();
    }

    @Override
    public Map.@Nullable Entry<K, V> lowerEntry(final K key) {
        checkMethodSupport(NavigableMapMethods.LOWER_ENTRY);

        if (hasBreak(LOWER_ENTRY_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(NAVIGATION_RACE_CONDITION)) {
            concurrentNavigableMap.lowerEntry(key);
            return concurrentNavigableMap.lowerEntry(key);
        }
        return concurrentNavigableMap.lowerEntry(key);
    }

    @Override
    public @Nullable K lowerKey(final K key) {
        checkMethodSupport(NavigableMapMethods.LOWER_ENTRY);

        if (hasBreak(LOWER_KEY_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(NAVIGATION_RACE_CONDITION)) {
            concurrentNavigableMap.lowerKey(key);
            return concurrentNavigableMap.lowerKey(key);
        }
        return concurrentNavigableMap.lowerKey(key);
    }

    @Override
    public Map.@Nullable Entry<K, V> floorEntry(final K key) {
        checkMethodSupport(NavigableMapMethods.FLOOR_ENTRY);

        if (hasBreak(FLOOR_ENTRY_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(NAVIGATION_RACE_CONDITION)) {
            concurrentNavigableMap.floorEntry(key);
            return concurrentNavigableMap.floorEntry(key);
        }
        return concurrentNavigableMap.floorEntry(key);
    }

    @Override
    public @Nullable K floorKey(final K key) {
        checkMethodSupport(NavigableMapMethods.FLOOR_KEY);

        if (hasBreak(FLOOR_KEY_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(NAVIGATION_RACE_CONDITION)) {
            concurrentNavigableMap.floorKey(key);
            return concurrentNavigableMap.floorKey(key);
        }
        return concurrentNavigableMap.floorKey(key);
    }

    @Override
    public Map.@Nullable Entry<K, V> ceilingEntry(final K key) {
        checkMethodSupport(NavigableMapMethods.CEILING_ENTRY);

        if (hasBreak(CEILING_ENTRY_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(NAVIGATION_RACE_CONDITION)) {
            concurrentNavigableMap.ceilingEntry(key);
            return concurrentNavigableMap.ceilingEntry(key);
        }
        return concurrentNavigableMap.ceilingEntry(key);
    }

    @Override
    public @Nullable K ceilingKey(final K key) {
        checkMethodSupport(NavigableMapMethods.CEILING_KEY);

        if (hasBreak(CEILING_KEY_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(NAVIGATION_RACE_CONDITION)) {
            concurrentNavigableMap.ceilingKey(key);
            return concurrentNavigableMap.ceilingKey(key);
        }
        return concurrentNavigableMap.ceilingKey(key);
    }

    @Override
    public Map.@Nullable Entry<K, V> higherEntry(final K key) {
        checkMethodSupport(NavigableMapMethods.HIGHER_ENTRY);

        if (hasBreak(HIGHER_ENTRY_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(NAVIGATION_RACE_CONDITION)) {
            concurrentNavigableMap.higherEntry(key);
            return concurrentNavigableMap.higherEntry(key);
        }
        return concurrentNavigableMap.higherEntry(key);
    }

    @Override
    public @Nullable K higherKey(final K key) {
        checkMethodSupport(NavigableMapMethods.HIGHER_KEY);

        if (!supportsMethod(NavigableMapMethods.HIGHER_KEY)) {
            throw new UnsupportedOperationException("higherKey() method is not supported");
        }
        if (hasBreak(HIGHER_KEY_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(NAVIGATION_RACE_CONDITION)) {
            concurrentNavigableMap.higherKey(key);
            return concurrentNavigableMap.higherKey(key);
        }
        return concurrentNavigableMap.higherKey(key);
    }

    @Override
    public Map.@Nullable Entry<K, V> firstEntry() {
        checkMethodSupport(NavigableMapMethods.FIRST_ENTRY);

        if (hasBreak(FIRST_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return concurrentNavigableMap.firstEntry();
    }

    @Override
    public Map.@Nullable Entry<K, V> lastEntry() {
        checkMethodSupport(NavigableMapMethods.LAST_ENTRY);

        if (hasBreak(LAST_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return concurrentNavigableMap.lastEntry();
    }

    @Override
    public Map.@Nullable Entry<K, V> pollFirstEntry() {
        checkMethodSupport(NavigableMapMethods.POLL_FIRST_ENTRY);

        if (hasBreak(POLL_FIRST_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return concurrentNavigableMap.pollFirstEntry();
    }

    @Override
    public Map.@Nullable Entry<K, V> pollLastEntry() {
        checkMethodSupport(NavigableMapMethods.POLL_LAST_ENTRY);

        if (hasBreak(POLL_LAST_ENTRY_RETURNS_NULL)) {
            return null;
        }
        return concurrentNavigableMap.pollLastEntry();
    }

    @Override
    public @Nullable ConcurrentNavigableMap<K, V> descendingMap() {
        checkMethodSupport(NavigableMapMethods.DESCENDING_MAP);
        if (hasBreak(DESCENDING_MAP_RETURNS_NULL)) {
            return null;
        }
        return concurrentNavigableMap.descendingMap();
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        checkMethodSupport(NavigableMapMethods.NAVIGABLE_KEY_SET);
        if (hasBreak(NAVIGABLE_KEY_SET_RETURNS_NULL)) {
            return null;
        }
        return concurrentNavigableMap.navigableKeySet();
    }

    @Override
    public @NonNull NavigableSet<K> keySet() {
        checkMethodSupport(MapMethods.KEY_SET);
        return navigableKeySet();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        checkMethodSupport(NavigableMapMethods.DESCENDING_KEY_SET);
        if (hasBreak(DESCENDING_KEY_SET_RETURNS_NULL)) {
            return null;
        }
        return concurrentNavigableMap.descendingKeySet();
    }

    @Override
    public ConcurrentNavigableMap<K, V> subMap(final K fromKey, final boolean fromInclusive,
                                               final K toKey, final boolean toInclusive) {
        checkMethodSupport(NavigableMapMethods.NAVIGABLE_KEY_SET);
        if (hasBreak(SUB_MAP_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(SUB_MAP_IGNORES_BOUNDS)) {
            // Return the entire map by using the first and last keys with inclusive bounds
            K firstKey = concurrentNavigableMap.firstKey();
            K lastKey = concurrentNavigableMap.lastKey();
            if (firstKey != null && lastKey != null) {
                return concurrentNavigableMap.subMap(firstKey, true, lastKey, true);
            }
            return concurrentNavigableMap.subMap(fromKey, true, toKey, true);
        }
        return concurrentNavigableMap.subMap(fromKey, fromInclusive, toKey, toInclusive);
    }

    @Override
    public ConcurrentNavigableMap<K, V> headMap(final K toKey, final boolean inclusive) {
        checkMethodSupport(NavigableMapMethods.HEAD_MAP_TWO_ARG);

        if (hasBreak(HEAD_MAP_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(HEAD_MAP_IGNORES_INCLUSIVE)) {
            return concurrentNavigableMap.headMap(toKey, true);
        }
        return concurrentNavigableMap.headMap(toKey, inclusive);
    }

    @Override
    public ConcurrentNavigableMap<K, V> tailMap(final K fromKey, final boolean inclusive) {
        checkMethodSupport(NavigableMapMethods.TAIL_MAP_TWO_ARG);

        if (hasBreak(TAIL_MAP_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(TAIL_MAP_IGNORES_INCLUSIVE)) {
            return concurrentNavigableMap.tailMap(fromKey, true);
        }
        return concurrentNavigableMap.tailMap(fromKey, inclusive);
    }

    @Override
    public @NonNull ConcurrentNavigableMap<K, V> subMap(final K fromKey, final K toKey) {
        checkMethodSupport(SortedMapMethods.SUB_MAP);

        if (hasBreak(SUB_MAP_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }
        return concurrentNavigableMap.subMap(fromKey, toKey);
    }

    @Override
    public @NonNull ConcurrentNavigableMap<K, V> headMap(final K toKey) {
        checkMethodSupport(SortedMapMethods.HEAD_MAP);

        if (!supportsMethod(SortedMapMethods.HEAD_MAP)) {
            throw new UnsupportedOperationException("headMap(K) method is not supported");
        }
        if (hasBreak(HEAD_MAP_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }
        return concurrentNavigableMap.headMap(toKey);
    }

    @Override
    public @NonNull ConcurrentNavigableMap<K, V> tailMap(final K fromKey) {
        checkMethodSupport(SortedMapMethods.TAIL_MAP);

        if (hasBreak(TAIL_MAP_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }
        return concurrentNavigableMap.tailMap(fromKey);
    }

    /// Builder for creating BreakableConcurrentNavigableMap instances with fluent configuration.
    ///
    /// This builder extends BreakableConcurrentMap.Builder and provides additional methods
    /// for configuring ConcurrentNavigableMap-specific behavior. It allows for method chaining
    /// to configure breaks, method support, and the underlying ConcurrentNavigableMap implementation.
    ///
    /// @param <K> the type of keys maintained by this map
    /// @param <V> the type of mapped values
    public static class Builder<K, V> extends BreakableConcurrentMap.Builder<K, V> {

        /// Creates a new Builder with a default ConcurrentSkipListMap as the underlying implementation.
        /// This constructor provides a standard NavigableMap implementation suitable for most testing scenarios.
        public Builder() {
            super(new ConcurrentSkipListMap<>());
        }

        /// Returns this builder instance to support fluent method chaining.
        /// This method overrides the parent implementation to return the correct type.
        ///
        /// @return this builder instance
        @Override
        public @NonNull Builder<K, V> self() {
            return this;
        }

        public Builder(final Builder<K, V> other) {
            super(other);
        }

        /// Creates a new Builder wrapping the specified ConcurrentNavigableMap.
        /// The provided map will be used as the underlying implementation for all operations.
        ///
        /// @param concurrentNavigableMap the ConcurrentNavigableMap to wrap
        public Builder(final ConcurrentNavigableMap<K, V> concurrentNavigableMap) {
            super(concurrentNavigableMap);
        }


        /// Builds and returns a new BreakableConcurrentNavigableMap instance with the configured breaks and method support.
        /// The returned instance will wrap the configured ConcurrentNavigableMap and apply all specified breaks.
        ///
        /// @return a new BreakableConcurrentNavigableMap instance
        @Override
        public @NonNull BreakableConcurrentNavigableMap<K, V> build() {
            return new BreakableConcurrentNavigableMap<>(new ConcurrentSkipListMap<>(elements()), breaks(),
                    methodStatuses(), permits());
        }

        /// Creates a new Builder wrapping the specified ConcurrentNavigableMap.
        /// This method provides a fluent alternative to the constructor for configuring the underlying map.
        ///
        /// @param concurrentNavigableMap the ConcurrentNavigableMap to wrap
        /// @return a new Builder instance wrapping the specified map
        public Builder<K, V> withConcurrentNavigableMap(final ConcurrentNavigableMap<K, V> concurrentNavigableMap) {
            return new Builder<>(concurrentNavigableMap);
        }

        /// Creates a copy of this Builder with the same configuration.
        /// The copy will have the same breaks, method support configuration, and underlying map contents.
        ///
        /// @return a new Builder instance with the same configuration as this one
        public @NonNull Builder<K, V> copy() {
            return new Builder<>(this);
        }
    }
}
