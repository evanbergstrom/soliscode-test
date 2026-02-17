package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.navigablemap.NavigableMapMethods;

import java.io.Serial;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/// A NavigableMap implementation that can be programmatically broken for testing purposes.
///
/// This class extends BreakableSortedMap and implements the NavigableMap interface, providing
/// additional break constants for testing NavigableMap-specific functionality. It wraps
/// an existing NavigableMap and allows specific behaviors to be "broken" through the
/// Break mechanism.
///
/// ## Overview
///
/// BreakableNavigableMap provides comprehensive testing capabilities for code that works with
/// NavigableMap implementations. It supports all standard NavigableMap operations while enabling
/// controlled behavioral modifications through break constants. This is particularly useful for:
///
/// - **Testing NavigableMap Contract Compliance**: Verifying that code correctly handles NavigableMap operations
/// - **Error Condition Simulation**: Testing how code responds to navigation failures and edge cases
/// - **Boundary Testing**: Ensuring proper handling of map boundaries and navigation limits
/// - **Performance Testing**: Simulating slow or failing navigation operations
/// - **Robustness Testing**: Verifying code resilience against unexpected NavigableMap behaviors
///
/// ## NavigableMap-Specific Break Constants
///
/// This class provides break constants for all NavigableMap-specific methods:
///
/// ### Navigation Breaks
/// - **LOWER_ENTRY_***: Control lowerEntry() behavior - returns null, throws exceptions, or returns random entries
/// - **LOWER_KEY_***: Control lowerKey() behavior - returns null, throws exceptions, or returns random keys
/// - **FLOOR_ENTRY_***: Control floorEntry() behavior - similar patterns for floor navigation
/// - **FLOOR_KEY_***: Control floorKey() behavior - similar patterns for floor navigation
/// - **CEILING_ENTRY_***: Control ceilingEntry() behavior - similar patterns for ceiling navigation
/// - **CEILING_KEY_***: Control ceilingKey() behavior - similar patterns for ceiling navigation
/// - **HIGHER_ENTRY_***: Control higherEntry() behavior - similar patterns for higher navigation
/// - **HIGHER_KEY_***: Control higherKey() behavior - similar patterns for higher navigation
///
/// ### Entry Access Breaks
/// - **FIRST_ENTRY_***: Control firstEntry() behavior - returns null, throws exceptions, or returns random entries
/// - **LAST_ENTRY_***: Control lastEntry() behavior - similar patterns for last entry access
/// - **POLL_FIRST_ENTRY_***: Control pollFirstEntry() behavior - affects removal of first entries
/// - **POLL_LAST_ENTRY_***: Control pollLastEntry() behavior - affects removal of last entries
///
/// ### View and Collection Breaks
/// - **DESCENDING_MAP_***: Control descendingMap() behavior - returns empty maps, null, or throws exceptions
/// - **NAVIGABLE_KEY_SET_***: Control navigableKeySet() behavior - affects navigable key set access
/// - **DESCENDING_KEY_SET_***: Control descendingKeySet() behavior - affects descending key set access
///
/// ### Enhanced Sub-Map Breaks
/// - **SUB_MAP_FOUR_ARG_***: Control the four-argument subMap() method behavior
/// - **HEAD_MAP_TWO_ARG_***: Control the two-argument headMap() method behavior
/// - **TAIL_MAP_TWO_ARG_***: Control the two-argument tailMap() method behavior
///
/// ## Usage Examples
///
/// ### Basic NavigableMap Testing
/// ```java
/// BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap<>();
/// map.put("apple", 1);
/// map.put("banana", 2);
/// map.put("cherry", 3);
///
/// // Normal navigation
/// assertEquals("banana", map.higherKey("apple"));
/// assertEquals("apple", map.lowerKey("banana"));
/// ```
///
/// ### Navigation Failure Testing
/// ```java
/// BreakableNavigableMap<String, Integer> brokenMap = new BreakableNavigableMap.Builder<String, Integer>()
///     .addBreak(HIGHER_KEY_ALWAYS_RETURNS_NULL)
///     .addBreak(LOWER_ENTRY_THROWS_EXCEPTION)
///     .build();
///
/// brokenMap.put("apple", 1);
/// brokenMap.put("banana", 2);
///
/// assertNull(brokenMap.higherKey("apple")); // Break causes null return
/// assertThrows(NoSuchElementException.class, () -> brokenMap.lowerEntry("banana")); // Break causes exception
/// ```
///
/// ### Builder Pattern with NavigableMap
/// ```java
/// BreakableNavigableMap<Integer, String> map = new BreakableNavigableMap.Builder<Integer, String>()
///     .addBreak(DESCENDING_MAP_RETURNS_EMPTY_MAP)
///     .addBreak(POLL_FIRST_ENTRY_THROWS_EXCEPTION)
///     .doesNotPermitNullKeys()
///     .build();
/// ```
///
/// ## Inheritance Hierarchy
///
/// ```
/// BreakableMap<K, V>
///     ↓
/// BreakableSortedMap<K, V>
///     ↓
/// BreakableNavigableMap<K, V>
/// ```
///
/// This class inherits all Map and SortedMap functionality from its parent classes while adding
/// NavigableMap-specific capabilities. All inherited break constants and behaviors remain available.
///
/// ## Thread Safety
///
/// Like its parent classes, BreakableNavigableMap is not thread-safe. External synchronization
/// is required for concurrent access, particularly when navigation operations are involved.
///
/// ## Performance Considerations
///
/// Navigation operations delegate to the underlying NavigableMap implementation unless breaks
/// are applied. Break checking adds minimal overhead. The class maintains the performance
/// characteristics of the underlying NavigableMap for non-broken operations.
///
/// @param <K> the type of keys maintained by this map
/// @param <V> the type of mapped values
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableSortedMap
/// @see NavigableMap
/// @see Break
public class BreakableNavigableMap<K, V> extends BreakableSortedMap<K, V> implements NavigableMap<K, V> {

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== Navigation Entry Breaks ==========

    /// Break constant that causes lowerEntry() to always return null.
    ///
    /// When this break is applied, the lowerEntry() method will return null regardless
    /// of whether there are entries with keys less than the specified key.
    ///
    /// **Affected Methods:**
    /// - {@link #lowerEntry(Object)} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing navigation failure handling
    /// - Simulating empty map conditions
    /// - Testing boundary condition logic
    public static final Break LOWER_ENTRY_ALWAYS_RETURNS_NULL =
            new Break("LOWER_ENTRY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes lowerEntry() to throw NoSuchElementException.
    ///
    /// When this break is applied, the lowerEntry() method will throw NoSuchElementException
    /// instead of returning the appropriate entry or null.
    ///
    /// **Affected Methods:**
    /// - {@link #lowerEntry(Object)} - Throws NoSuchElementException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in navigation
    /// - Simulating navigation failures
    /// - Testing robustness against unexpected exceptions
    public static final Break LOWER_ENTRY_THROWS_EXCEPTION =
            new Break("LOWER_ENTRY_THROWS_EXCEPTION");

    /// Break constant that causes lowerEntry() to return a random entry from the map.
    ///
    /// When this break is applied, the lowerEntry() method will return an arbitrary
    /// entry from the map instead of the correct lower entry.
    ///
    /// **Affected Methods:**
    /// - {@link #lowerEntry(Object)} - Returns random entry
    ///
    /// **Use Cases:**
    /// - Testing ordering assumptions
    /// - Simulating corrupted navigation
    /// - Testing robustness against incorrect results
    public static final Break LOWER_ENTRY_RETURNS_RANDOM_ENTRY =
            new Break("LOWER_ENTRY_RETURNS_RANDOM_ENTRY");

    // ========== Navigation Key Breaks ==========

    /// Break constant that causes lowerKey() to always return null.
    public static final Break LOWER_KEY_ALWAYS_RETURNS_NULL =
            new Break("LOWER_KEY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes lowerKey() to throw NoSuchElementException.
    public static final Break LOWER_KEY_THROWS_EXCEPTION =
            new Break("LOWER_KEY_THROWS_EXCEPTION");

    /// Break constant that causes lowerKey() to return a random key from the map.
    public static final Break LOWER_KEY_RETURNS_RANDOM_KEY =
            new Break("LOWER_KEY_RETURNS_RANDOM_KEY");

    // ========== Floor Navigation Breaks ==========

    /// Break constant that causes floorEntry() to always return null.
    public static final Break FLOOR_ENTRY_ALWAYS_RETURNS_NULL =
            new Break("FLOOR_ENTRY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes floorEntry() to throw NoSuchElementException.
    public static final Break FLOOR_ENTRY_THROWS_EXCEPTION =
            new Break("FLOOR_ENTRY_THROWS_EXCEPTION");

    /// Break constant that causes floorEntry() to return a random entry from the map.
    public static final Break FLOOR_ENTRY_RETURNS_RANDOM_ENTRY =
            new Break("FLOOR_ENTRY_RETURNS_RANDOM_ENTRY");

    /// Break constant that causes floorKey() to always return null.
    public static final Break FLOOR_KEY_ALWAYS_RETURNS_NULL =
            new Break("FLOOR_KEY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes floorKey() to throw NoSuchElementException.
    public static final Break FLOOR_KEY_THROWS_EXCEPTION =
            new Break("FLOOR_KEY_THROWS_EXCEPTION");

    /// Break constant that causes floorKey() to return a random key from the map.
    public static final Break FLOOR_KEY_RETURNS_RANDOM_KEY =
            new Break("FLOOR_KEY_RETURNS_RANDOM_KEY");

    // ========== Ceiling Navigation Breaks ==========

    /// Break constant that causes ceilingEntry() to always return null.
    public static final Break CEILING_ENTRY_ALWAYS_RETURNS_NULL =
            new Break("CEILING_ENTRY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes ceilingEntry() to throw NoSuchElementException.
    public static final Break CEILING_ENTRY_THROWS_EXCEPTION =
            new Break("CEILING_ENTRY_THROWS_EXCEPTION");

    /// Break constant that causes ceilingEntry() to return a random entry from the map.
    public static final Break CEILING_ENTRY_RETURNS_RANDOM_ENTRY =
            new Break("CEILING_ENTRY_RETURNS_RANDOM_ENTRY");

    /// Break constant that causes ceilingKey() to always return null.
    public static final Break CEILING_KEY_ALWAYS_RETURNS_NULL =
            new Break("CEILING_KEY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes ceilingKey() to throw NoSuchElementException.
    public static final Break CEILING_KEY_THROWS_EXCEPTION =
            new Break("CEILING_KEY_THROWS_EXCEPTION");

    /// Break constant that causes ceilingKey() to return a random key from the map.
    public static final Break CEILING_KEY_RETURNS_RANDOM_KEY =
            new Break("CEILING_KEY_RETURNS_RANDOM_KEY");

    // ========== Higher Navigation Breaks ==========

    /// Break constant that causes higherEntry() to always return null.
    public static final Break HIGHER_ENTRY_ALWAYS_RETURNS_NULL =
            new Break("HIGHER_ENTRY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes higherEntry() to throw NoSuchElementException.
    public static final Break HIGHER_ENTRY_THROWS_EXCEPTION =
            new Break("HIGHER_ENTRY_THROWS_EXCEPTION");

    /// Break constant that causes higherEntry() to return a random entry from the map.
    public static final Break HIGHER_ENTRY_RETURNS_RANDOM_ENTRY =
            new Break("HIGHER_ENTRY_RETURNS_RANDOM_ENTRY");

    /// Break constant that causes higherKey() to always return null.
    public static final Break HIGHER_KEY_ALWAYS_RETURNS_NULL =
            new Break("HIGHER_KEY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes higherKey() to throw NoSuchElementException.
    public static final Break HIGHER_KEY_THROWS_EXCEPTION =
            new Break("HIGHER_KEY_THROWS_EXCEPTION");

    /// Break constant that causes higherKey() to return a random key from the map.
    public static final Break HIGHER_KEY_RETURNS_RANDOM_KEY =
            new Break("HIGHER_KEY_RETURNS_RANDOM_KEY");

    // ========== Entry Access Breaks ==========

    /// Break constant that causes firstEntry() to always return null.
    public static final Break FIRST_ENTRY_ALWAYS_RETURNS_NULL =
            new Break("FIRST_ENTRY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes firstEntry() to throw NoSuchElementException.
    public static final Break FIRST_ENTRY_THROWS_EXCEPTION =
            new Break("FIRST_ENTRY_THROWS_EXCEPTION");

    /// Break constant that causes firstEntry() to return a random entry from the map.
    public static final Break FIRST_ENTRY_RETURNS_RANDOM_ENTRY =
            new Break("FIRST_ENTRY_RETURNS_RANDOM_ENTRY");

    /// Break constant that causes lastEntry() to always return null.
    public static final Break LAST_ENTRY_ALWAYS_RETURNS_NULL =
            new Break("LAST_ENTRY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes lastEntry() to throw NoSuchElementException.
    public static final Break LAST_ENTRY_THROWS_EXCEPTION =
            new Break("LAST_ENTRY_THROWS_EXCEPTION");

    /// Break constant that causes lastEntry() to return a random entry from the map.
    public static final Break LAST_ENTRY_RETURNS_RANDOM_ENTRY =
            new Break("LAST_ENTRY_RETURNS_RANDOM_ENTRY");

    // ========== Poll Entry Breaks ==========

    /// Break constant that causes pollFirstEntry() to always return null without removing anything.
    public static final Break POLL_FIRST_ENTRY_ALWAYS_RETURNS_NULL =
            new Break("POLL_FIRST_ENTRY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes pollFirstEntry() to throw NoSuchElementException.
    public static final Break POLL_FIRST_ENTRY_THROWS_EXCEPTION =
            new Break("POLL_FIRST_ENTRY_THROWS_EXCEPTION");

    /// Break constant that causes pollFirstEntry() to return an entry without removing it.
    public static final Break POLL_FIRST_ENTRY_DOES_NOT_REMOVE =
            new Break("POLL_FIRST_ENTRY_DOES_NOT_REMOVE");

    /// Break constant that causes pollLastEntry() to always return null without removing anything.
    public static final Break POLL_LAST_ENTRY_ALWAYS_RETURNS_NULL =
            new Break("POLL_LAST_ENTRY_ALWAYS_RETURNS_NULL");

    /// Break constant that causes pollLastEntry() to throw NoSuchElementException.
    public static final Break POLL_LAST_ENTRY_THROWS_EXCEPTION =
            new Break("POLL_LAST_ENTRY_THROWS_EXCEPTION");

    /// Break constant that causes pollLastEntry() to return an entry without removing it.
    public static final Break POLL_LAST_ENTRY_DOES_NOT_REMOVE =
            new Break("POLL_LAST_ENTRY_DOES_NOT_REMOVE");

    // ========== Map View Breaks ==========

    /// Break constant that causes descendingMap() to return an empty map.
    public static final Break DESCENDING_MAP_RETURNS_EMPTY_MAP =
            new Break("DESCENDING_MAP_RETURNS_EMPTY_MAP");

    /// Break constant that causes descendingMap() to return null.
    public static final Break DESCENDING_MAP_RETURNS_NULL =
            new Break("DESCENDING_MAP_RETURNS_NULL");

    /// Break constant that causes descendingMap() to throw UnsupportedOperationException.
    public static final Break DESCENDING_MAP_THROWS_EXCEPTION =
            new Break("DESCENDING_MAP_THROWS_EXCEPTION");

    // ========== Key Set Breaks ==========

    /// Break constant that causes navigableKeySet() to return an empty set.
    public static final Break NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET =
            new Break("NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET");

    /// Break constant that causes navigableKeySet() to return null.
    public static final Break NAVIGABLE_KEY_SET_RETURNS_NULL =
            new Break("NAVIGABLE_KEY_SET_RETURNS_NULL");

    /// Break constant that causes navigableKeySet() to throw UnsupportedOperationException.
    public static final Break NAVIGABLE_KEY_SET_THROWS_EXCEPTION =
            new Break("NAVIGABLE_KEY_SET_THROWS_EXCEPTION");

    /// Break constant that causes descendingKeySet() to return an empty set.
    public static final Break DESCENDING_KEY_SET_RETURNS_EMPTY_SET =
            new Break("DESCENDING_KEY_SET_RETURNS_EMPTY_SET");

    /// Break constant that causes descendingKeySet() to return null.
    public static final Break DESCENDING_KEY_SET_RETURNS_NULL =
            new Break("DESCENDING_KEY_SET_RETURNS_NULL");

    /// Break constant that causes descendingKeySet() to throw UnsupportedOperationException.
    public static final Break DESCENDING_KEY_SET_THROWS_EXCEPTION =
            new Break("DESCENDING_KEY_SET_THROWS_EXCEPTION");

    // ========== Enhanced Sub-Map Breaks ==========

    /// Break constant that causes the four-argument subMap() to return an empty map.
    public static final Break SUB_MAP_FOUR_ARG_RETURNS_EMPTY_MAP =
            new Break("SUB_MAP_FOUR_ARG_RETURNS_EMPTY_MAP");

    /// Break constant that causes the four-argument subMap() to return null.
    public static final Break SUB_MAP_FOUR_ARG_RETURNS_NULL =
            new Break("SUB_MAP_FOUR_ARG_RETURNS_NULL");

    /// Break constant that causes the four-argument subMap() to throw IllegalArgumentException.
    public static final Break SUB_MAP_FOUR_ARG_THROWS_EXCEPTION =
            new Break("SUB_MAP_FOUR_ARG_THROWS_EXCEPTION");

    /// Break constant that causes the two-argument headMap() to return an empty map.
    public static final Break HEAD_MAP_TWO_ARG_RETURNS_EMPTY_MAP =
            new Break("HEAD_MAP_TWO_ARG_RETURNS_EMPTY_MAP");

    /// Break constant that causes the two-argument headMap() to return null.
    public static final Break HEAD_MAP_TWO_ARG_RETURNS_NULL =
            new Break("HEAD_MAP_TWO_ARG_RETURNS_NULL");

    /// Break constant that causes the two-argument headMap() to throw IllegalArgumentException.
    public static final Break HEAD_MAP_TWO_ARG_THROWS_EXCEPTION =
            new Break("HEAD_MAP_TWO_ARG_THROWS_EXCEPTION");

    /// Break constant that causes the two-argument tailMap() to return an empty map.
    public static final Break TAIL_MAP_TWO_ARG_RETURNS_EMPTY_MAP =
            new Break("TAIL_MAP_TWO_ARG_RETURNS_EMPTY_MAP");

    /// Break constant that causes the two-argument tailMap() to return null.
    public static final Break TAIL_MAP_TWO_ARG_RETURNS_NULL =
            new Break("TAIL_MAP_TWO_ARG_RETURNS_NULL");

    /// Break constant that causes the two-argument tailMap() to throw IllegalArgumentException.
    public static final Break TAIL_MAP_TWO_ARG_THROWS_EXCEPTION =
            new Break("TAIL_MAP_TWO_ARG_THROWS_EXCEPTION");

    // ========== Instance Fields ==========

    /// The underlying NavigableMap that this BreakableNavigableMap wraps.
    /// All operations delegate to this map unless breaks are applied.
    private final @NonNull NavigableMap<K, V> navigableMap;

    // ========== Constructors ==========

    /// Creates an empty BreakableNavigableMap with natural ordering.
    ///
    /// This constructor creates a new BreakableNavigableMap backed by an empty TreeMap.
    /// The map will use natural ordering for keys and have default null policies.
    ///
    /// **Default Configuration:**
    /// - Empty NavigableMap (TreeMap implementation)
    /// - Natural key ordering (Comparable-based)
    /// - Permits null keys: true
    /// - Permits null values: true
    /// - No breaks applied
    ///
    /// **Usage:**
    /// ```java
    /// BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap<>();
    /// map.put("apple", 1);
    /// map.put("banana", 2);
    /// ```
    @SuppressWarnings("SortedCollectionWithNonComparableKeys")
    public BreakableNavigableMap() {
        super();
        this.navigableMap = new TreeMap<>();
    }

    /// Creates a BreakableNavigableMap by copying another BreakableNavigableMap.
    ///
    /// This constructor creates a new instance that shares the underlying navigable map data
    /// and inherits all configuration from the source map.
    ///
    /// **Inherited Configuration:**
    /// - All break settings from source
    /// - Null key and value policies
    /// - Method support configuration
    /// - Underlying NavigableMap reference (shared, not copied)
    ///
    /// **Usage:**
    /// ```java
    /// BreakableNavigableMap<String, Integer> original = new BreakableNavigableMap<>();
    /// BreakableNavigableMap<String, Integer> copy = new BreakableNavigableMap<>(original);
    /// ```
    ///
    /// @param other the BreakableNavigableMap to copy configuration from
    /// @throws NullPointerException if other is null
    public BreakableNavigableMap(final @NonNull BreakableNavigableMap<K, V> other) {
        super(other);
        this.navigableMap = Objects.requireNonNull(other.navigableMap, "NavigableMap cannot be null");
    }

    /// Creates a BreakableNavigableMap with the specified configuration.
    ///
    /// This constructor allows full control over the BreakableNavigableMap configuration,
    /// including the underlying NavigableMap, breaks, and null policies.
    ///
    /// **Usage:**
    /// ```java
    /// TreeMap<String, Integer> treeMap = new TreeMap<>();
    /// Set<Break> breaks = Set.of(HIGHER_KEY_ALWAYS_RETURNS_NULL);
    /// BreakableNavigableMap<String, Integer> map = new BreakableNavigableMap<>(
    ///     treeMap, breaks, true, false);
    /// ```
    ///
    /// @param navigableMap the NavigableMap to wrap
    /// @param breaks the breaks to apply
    /// @throws NullPointerException if navigableMap or breaks is null
    /// @throws IllegalArgumentException if the map contains null keys/values that
    ///                                  violate the specified null policies
    protected BreakableNavigableMap(
            final @NonNull NavigableMap<K, V> navigableMap,
            final @NonNull Set<Break> breaks,
            final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int permits,
            final boolean isSafe) {
        super(this.navigableMap = navigableMap, breaks, methodStatuses, permits, isSafe);
    }

    // ========== NavigableMap Interface Implementation ==========

    /// {@inheritDoc}
    ///
    /// Returns a key-value mapping associated with the greatest key strictly less than the given key,
    /// or null if there is no such key. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #LOWER_ENTRY_ALWAYS_RETURNS_NULL} - Returns null regardless of actual content
    /// - {@link #LOWER_ENTRY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #LOWER_ENTRY_RETURNS_RANDOM_ENTRY} - Returns arbitrary entry from map
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to LOWER_ENTRY_THROWS_EXCEPTION break
    @Override
    public Map.@Nullable Entry<K, V> lowerEntry(final K key) {
        if (!supportsMethod(NavigableMapMethods.LOWER_ENTRY)) {
            throw new UnsupportedOperationException("lowerEntry() method is not supported");
        }
        if (hasBreak(LOWER_ENTRY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(LOWER_ENTRY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("LowerEntry access failed due to break");
        }
        if (hasBreak(LOWER_ENTRY_RETURNS_RANDOM_ENTRY) && !navigableMap.isEmpty()) {
            K randomKey = navigableMap.keySet().iterator().next();
            return Map.entry(randomKey, navigableMap.get(randomKey));
        }
        return navigableMap.lowerEntry(key);
    }

    /// {@inheritDoc}
    ///
    /// Returns the greatest key strictly less than the given key, or `null` if there is no such key.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #LOWER_KEY_ALWAYS_RETURNS_NULL} - Returns null regardless of actual content
    /// - {@link #LOWER_KEY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #LOWER_KEY_RETURNS_RANDOM_KEY} - Returns arbitrary key from map
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to LOWER_KEY_THROWS_EXCEPTION break
    ///
    /// @param key the key
    /// @return the greatest key less than `key`, or `null` if there is no such key
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#lowerKey(Object)
    @Override
    public @Nullable K lowerKey(final K key) {
        if (!supportsMethod(NavigableMapMethods.LOWER_KEY)) {
            throw new UnsupportedOperationException("lowerKey() method is not supported");
        }
        if (hasBreak(LOWER_KEY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(LOWER_KEY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("LowerKey access failed due to break");
        }
        if (hasBreak(LOWER_KEY_RETURNS_RANDOM_KEY) && !navigableMap.isEmpty()) {
            return navigableMap.keySet().iterator().next();
        }
        return navigableMap.lowerKey(key);
    }

    /// {@inheritDoc}
    ///
    /// Returns a key-value mapping associated with the greatest key less than or equal to the
    /// given key, or `null` if there is no such key. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #FLOOR_ENTRY_ALWAYS_RETURNS_NULL} - Returns null regardless of actual content
    /// - {@link #FLOOR_ENTRY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #FLOOR_ENTRY_RETURNS_RANDOM_ENTRY} - Returns arbitrary entry from map
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to FLOOR_ENTRY_THROWS_EXCEPTION break
    ///
    /// @param key the key
    /// @return an entry with the greatest key less than or equal to `key`, or `null` if there is no such key
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#floorEntry(Object)
    @Override
    public Map.@Nullable Entry<K, V> floorEntry(final K key) {
        if (!supportsMethod(NavigableMapMethods.FLOOR_ENTRY)) {
            throw new UnsupportedOperationException("floorEntry() method is not supported");
        }
        if (hasBreak(FLOOR_ENTRY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(FLOOR_ENTRY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("FloorEntry access failed due to break");
        }
        if (hasBreak(FLOOR_ENTRY_RETURNS_RANDOM_ENTRY) && !navigableMap.isEmpty()) {
            K randomKey = navigableMap.keySet().iterator().next();
            return Map.entry(randomKey, navigableMap.get(randomKey));
        }
        return navigableMap.floorEntry(key);
    }

    /// {@inheritDoc}
    ///
    /// Returns the greatest key less than or equal to the given key, or `null` if there is no such key.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #FLOOR_KEY_ALWAYS_RETURNS_NULL} - Returns null regardless of actual content
    /// - {@link #FLOOR_KEY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #FLOOR_KEY_RETURNS_RANDOM_KEY} - Returns arbitrary key from map
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to FLOOR_KEY_THROWS_EXCEPTION break
    ///
    /// @param key the key
    /// @return the greatest key less than or equal to `key`, or `null` if there is no such key
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#floorKey(Object)
    @Override
    public @Nullable K floorKey(final K key) {
        if (!supportsMethod(NavigableMapMethods.FLOOR_KEY)) {
            throw new UnsupportedOperationException("floorKey() method is not supported");
        }
        if (hasBreak(FLOOR_KEY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(FLOOR_KEY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("FloorKey access failed due to break");
        }
        if (hasBreak(FLOOR_KEY_RETURNS_RANDOM_KEY) && !navigableMap.isEmpty()) {
            return navigableMap.keySet().iterator().next();
        }
        return navigableMap.floorKey(key);
    }

    /// {@inheritDoc}
    ///
    /// Returns a key-value mapping associated with the least key greater than or equal to the
    /// given key, or `null` if there is no such key. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #CEILING_ENTRY_ALWAYS_RETURNS_NULL} - Returns null regardless of actual content
    /// - {@link #CEILING_ENTRY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #CEILING_ENTRY_RETURNS_RANDOM_ENTRY} - Returns arbitrary entry from map
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to CEILING_ENTRY_THROWS_EXCEPTION break
    ///
    /// @param key the key
    /// @return an entry with the least key greater than or equal to `key`, or `null` if there is no such key
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#ceilingEntry(Object)
    @Override
    public Map.@Nullable Entry<K, V> ceilingEntry(final K key) {
        if (!supportsMethod(NavigableMapMethods.CEILING_ENTRY)) {
            throw new UnsupportedOperationException("ceilingEntry() method is not supported");
        }
        if (hasBreak(CEILING_ENTRY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(CEILING_ENTRY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("CeilingEntry access failed due to break");
        }
        if (hasBreak(CEILING_ENTRY_RETURNS_RANDOM_ENTRY) && !navigableMap.isEmpty()) {
            K randomKey = navigableMap.keySet().iterator().next();
            return Map.entry(randomKey, navigableMap.get(randomKey));
        }
        return navigableMap.ceilingEntry(key);
    }

    /// {@inheritDoc}
    ///
    /// Returns the least key greater than or equal to the given key, or `null` if there is no such key.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #CEILING_KEY_ALWAYS_RETURNS_NULL} - Returns null regardless of actual content
    /// - {@link #CEILING_KEY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #CEILING_KEY_RETURNS_RANDOM_KEY} - Returns arbitrary key from map
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to CEILING_KEY_THROWS_EXCEPTION break
    ///
    /// @param key the key
    /// @return the least key greater than or equal to `key`, or `null` if there is no such key
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#ceilingKey(Object)
    @Override
    public @Nullable K ceilingKey(final K key) {
        if (!supportsMethod(NavigableMapMethods.CEILING_KEY)) {
            throw new UnsupportedOperationException("ceilingKey() method is not supported");
        }
        if (hasBreak(CEILING_KEY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(CEILING_KEY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("CeilingKey access failed due to break");
        }
        if (hasBreak(CEILING_KEY_RETURNS_RANDOM_KEY) && !navigableMap.isEmpty()) {
            return navigableMap.keySet().iterator().next();
        }
        return navigableMap.ceilingKey(key);
    }

    /// {@inheritDoc}
    ///
    /// Returns a key-value mapping associated with the least key strictly greater than the given key,
    /// or `null` if there is no such key. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #HIGHER_ENTRY_ALWAYS_RETURNS_NULL} - Returns null regardless of actual content
    /// - {@link #HIGHER_ENTRY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #HIGHER_ENTRY_RETURNS_RANDOM_ENTRY} - Returns arbitrary entry from map
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to HIGHER_ENTRY_THROWS_EXCEPTION break
    ///
    /// @param key the key
    /// @return an entry with the least key greater than `key`, or `null` if there is no such key
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#higherEntry(Object)
    @Override
    public Map.@Nullable Entry<K, V> higherEntry(final K key) {
        if (!supportsMethod(NavigableMapMethods.HIGHER_ENTRY)) {
            throw new UnsupportedOperationException("higherEntry() method is not supported");
        }
        if (hasBreak(HIGHER_ENTRY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(HIGHER_ENTRY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("HigherEntry access failed due to break");
        }
        if (hasBreak(HIGHER_ENTRY_RETURNS_RANDOM_ENTRY) && !navigableMap.isEmpty()) {
            K randomKey = navigableMap.keySet().iterator().next();
            return Map.entry(randomKey, navigableMap.get(randomKey));
        }
        return navigableMap.higherEntry(key);
    }

    /// {@inheritDoc}
    ///
    /// Returns the least key strictly greater than the given key, or `null` if there is no such key.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #HIGHER_KEY_ALWAYS_RETURNS_NULL} - Returns null regardless of actual content
    /// - {@link #HIGHER_KEY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #HIGHER_KEY_RETURNS_RANDOM_KEY} - Returns arbitrary key from map
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to HIGHER_KEY_THROWS_EXCEPTION break
    ///
    /// @param key the key
    /// @return the least key greater than `key`, or `null` if there is no such key
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#higherKey(Object)
    @Override
    public @Nullable K higherKey(final K key) {
        if (!supportsMethod(NavigableMapMethods.HIGHER_KEY)) {
            throw new UnsupportedOperationException("higherKey() method is not supported");
        }
        if (hasBreak(HIGHER_KEY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(HIGHER_KEY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("HigherKey access failed due to break");
        }
        if (hasBreak(HIGHER_KEY_RETURNS_RANDOM_KEY) && !navigableMap.isEmpty()) {
            return navigableMap.keySet().iterator().next();
        }
        return navigableMap.higherKey(key);
    }

    /// {@inheritDoc}
    ///
    /// Returns a key-value mapping associated with the least key in this map,
    /// or `null` if the map is empty. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #FIRST_ENTRY_ALWAYS_RETURNS_NULL} - Returns null regardless of actual content
    /// - {@link #FIRST_ENTRY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #FIRST_ENTRY_RETURNS_RANDOM_ENTRY} - Returns arbitrary entry from map
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to FIRST_ENTRY_THROWS_EXCEPTION break
    ///
    /// @return an entry with the least key, or `null` if this map is empty
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#firstEntry()
    @Override
    public Map.@Nullable Entry<K, V> firstEntry() {
        if (!supportsMethod(NavigableMapMethods.FIRST_ENTRY)) {
            throw new UnsupportedOperationException("firstEntry() method is not supported");
        }
        if (hasBreak(FIRST_ENTRY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(FIRST_ENTRY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("FirstEntry access failed due to break");
        }
        if (hasBreak(FIRST_ENTRY_RETURNS_RANDOM_ENTRY) && !navigableMap.isEmpty()) {
            K randomKey = navigableMap.keySet().iterator().next();
            return Map.entry(randomKey, navigableMap.get(randomKey));
        }
        return navigableMap.firstEntry();
    }

    /// {@inheritDoc}
    ///
    /// Returns a key-value mapping associated with the greatest key in this map,
    /// or `null` if the map is empty. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #LAST_ENTRY_ALWAYS_RETURNS_NULL} - Returns null regardless of actual content
    /// - {@link #LAST_ENTRY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #LAST_ENTRY_RETURNS_RANDOM_ENTRY} - Returns arbitrary entry from map
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to LAST_ENTRY_THROWS_EXCEPTION break
    ///
    /// @return an entry with the greatest key, or `null` if this map is empty
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#lastEntry()
    @Override
    public Map.@Nullable Entry<K, V> lastEntry() {
        if (!supportsMethod(NavigableMapMethods.LAST_ENTRY)) {
            throw new UnsupportedOperationException("lastEntry() method is not supported");
        }
        if (hasBreak(LAST_ENTRY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(LAST_ENTRY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("LastEntry access failed due to break");
        }
        if (hasBreak(LAST_ENTRY_RETURNS_RANDOM_ENTRY) && !navigableMap.isEmpty()) {
            K randomKey = navigableMap.keySet().iterator().next();
            return Map.entry(randomKey, navigableMap.get(randomKey));
        }
        return navigableMap.lastEntry();
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the key-value mapping associated with the least key in this map,
    /// or `null` if the map is empty. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POLL_FIRST_ENTRY_ALWAYS_RETURNS_NULL} - Returns null regardless of content
    /// - {@link #POLL_FIRST_ENTRY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #POLL_FIRST_ENTRY_DOES_NOT_REMOVE} - Returns entry but doesn't remove it
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to POLL_FIRST_ENTRY_THROWS_EXCEPTION break
    ///
    /// @return the removed first entry of this map, or `null` if this map is empty
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#pollFirstEntry()
    @Override
    public Map.@Nullable Entry<K, V> pollFirstEntry() {
        if (!supportsMethod(NavigableMapMethods.POLL_FIRST_ENTRY)) {
            throw new UnsupportedOperationException("pollFirstEntry() method is not supported");
        }
        if (hasBreak(POLL_FIRST_ENTRY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(POLL_FIRST_ENTRY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("PollFirstEntry access failed due to break");
        }
        if (hasBreak(POLL_FIRST_ENTRY_DOES_NOT_REMOVE)) {
            return navigableMap.firstEntry(); // Return but don't remove
        }
        return navigableMap.pollFirstEntry();
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the key-value mapping associated with the greatest key in this map,
    /// or `null` if the map is empty. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POLL_LAST_ENTRY_ALWAYS_RETURNS_NULL} - Returns null regardless of content
    /// - {@link #POLL_LAST_ENTRY_THROWS_EXCEPTION} - Throws NoSuchElementException
    /// - {@link #POLL_LAST_ENTRY_DOES_NOT_REMOVE} - Returns entry but doesn't remove it
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw NoSuchElementException due to POLL_LAST_ENTRY_THROWS_EXCEPTION break
    ///
    /// @return the removed last entry of this map, or `null` if this map is empty
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#pollLastEntry()
    @Override
    public Map.@Nullable Entry<K, V> pollLastEntry() {
        if (!supportsMethod(NavigableMapMethods.POLL_LAST_ENTRY)) {
            throw new UnsupportedOperationException("pollLastEntry() method is not supported");
        }
        if (hasBreak(POLL_LAST_ENTRY_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(POLL_LAST_ENTRY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("PollLastEntry access failed due to break");
        }
        if (hasBreak(POLL_LAST_ENTRY_DOES_NOT_REMOVE)) {
            return navigableMap.lastEntry(); // Return but don't remove
        }
        return navigableMap.pollLastEntry();
    }

    /// {@inheritDoc}
    ///
    /// Returns a reverse order view of the mappings contained in this map.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #DESCENDING_MAP_RETURNS_EMPTY_MAP} - Returns an empty navigable map
    /// - {@link #DESCENDING_MAP_RETURNS_NULL} - Returns null
    /// - {@link #DESCENDING_MAP_THROWS_EXCEPTION} - Throws UnsupportedOperationException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported or due to break
    ///
    /// @return a reverse order view of this map
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#descendingMap()
    @Override
    public @NonNull NavigableMap<K, V> descendingMap() {
        if (!supportsMethod(NavigableMapMethods.DESCENDING_MAP)) {
            throw new UnsupportedOperationException("descendingMap() method is not supported");
        }
        if (hasBreak(DESCENDING_MAP_RETURNS_EMPTY_MAP)) {
            //noinspection SortedCollectionWithNonComparableKeys
            return new TreeMap<>();
        }
        if (hasBreak(DESCENDING_MAP_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }
        if (hasBreak(DESCENDING_MAP_THROWS_EXCEPTION)) {
            throw new UnsupportedOperationException("DescendingMap access failed due to break");
        }
        return navigableMap.descendingMap();
    }

    /// {@inheritDoc}
    ///
    /// Returns a {@link NavigableSet} view of the keys contained in this map.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET} - Returns an empty navigable set
    /// - {@link #NAVIGABLE_KEY_SET_RETURNS_NULL} - Returns null
    /// - {@link #NAVIGABLE_KEY_SET_THROWS_EXCEPTION} - Throws UnsupportedOperationException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported or due to break
    ///
    /// @return a navigable set view of the keys in this map
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#navigableKeySet()
    @SuppressWarnings("SortedCollectionWithNonComparableKeys")
    @Override
    public @NonNull NavigableSet<K> navigableKeySet() {
        if (!supportsMethod(NavigableMapMethods.NAVIGABLE_KEY_SET)) {
            throw new UnsupportedOperationException("navigableKeySet() method is not supported");
        }
        if (hasBreak(NAVIGABLE_KEY_SET_RETURNS_EMPTY_SET)) {
            return new TreeMap<K, Object>().navigableKeySet();
        }
        if (hasBreak(NAVIGABLE_KEY_SET_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }
        if (hasBreak(NAVIGABLE_KEY_SET_THROWS_EXCEPTION)) {
            throw new UnsupportedOperationException("NavigableKeySet access failed due to break");
        }
        return navigableMap.navigableKeySet();
    }

    /// {@inheritDoc}
    ///
    /// Returns a reverse order {@link NavigableSet} view of the keys contained in this map.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #DESCENDING_KEY_SET_RETURNS_EMPTY_SET} - Returns an empty navigable set
    /// - {@link #DESCENDING_KEY_SET_RETURNS_NULL} - Returns null
    /// - {@link #DESCENDING_KEY_SET_THROWS_EXCEPTION} - Throws UnsupportedOperationException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported or due to break
    ///
    /// @return a reverse order navigable set view of the keys in this map
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#descendingKeySet()
    @SuppressWarnings("SortedCollectionWithNonComparableKeys")
    @Override
    public @NonNull NavigableSet<K> descendingKeySet() {
        if (!supportsMethod(NavigableMapMethods.DESCENDING_KEY_SET)) {
            throw new UnsupportedOperationException("descendingKeySet() method is not supported");
        }
        if (hasBreak(DESCENDING_KEY_SET_RETURNS_EMPTY_SET)) {
            return new TreeMap<K, Object>().descendingKeySet();
        }
        if (hasBreak(DESCENDING_KEY_SET_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }
        if (hasBreak(DESCENDING_KEY_SET_THROWS_EXCEPTION)) {
            throw new UnsupportedOperationException("DescendingKeySet access failed due to break");
        }
        return navigableMap.descendingKeySet();
    }

    /// {@inheritDoc}
    ///
    /// Returns a view of the portion of this map whose keys range from `fromKey` to `toKey`.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #SUB_MAP_FOUR_ARG_RETURNS_EMPTY_MAP} - Returns an empty navigable map
    /// - {@link #SUB_MAP_FOUR_ARG_RETURNS_NULL} - Returns null
    /// - {@link #SUB_MAP_FOUR_ARG_THROWS_EXCEPTION} - Throws IllegalArgumentException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw IllegalArgumentException due to SUB_MAP_FOUR_ARG_THROWS_EXCEPTION break
    ///
    /// @param fromKey low endpoint of the keys in the returned map
    /// @param fromInclusive `true` if the low endpoint is to be included in the returned view
    /// @param toKey high endpoint of the keys in the returned map
    /// @param toInclusive `true` if the high endpoint is to be included in the returned view
    /// @return a view of the portion of this map whose keys range from `fromKey` to `toKey`
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#subMap(Object, boolean, Object, boolean)
    @Override
    public @NonNull NavigableMap<K, V> subMap(final K fromKey, final boolean fromInclusive,
                                             final K toKey, final boolean toInclusive) {
        if (!supportsMethod(NavigableMapMethods.SUB_MAP_FOUR_ARG)) {
            throw new UnsupportedOperationException("subMap(K, boolean, K, boolean) method is not supported");
        }
        if (hasBreak(SUB_MAP_FOUR_ARG_RETURNS_EMPTY_MAP)) {
            //noinspection SortedCollectionWithNonComparableKeys
            return new TreeMap<>();
        }
        if (hasBreak(SUB_MAP_FOUR_ARG_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }
        if (hasBreak(SUB_MAP_FOUR_ARG_THROWS_EXCEPTION)) {
            throw new IllegalArgumentException("SubMap creation failed due to break");
        }
        return navigableMap.subMap(fromKey, fromInclusive, toKey, toInclusive);
    }

    /// {@inheritDoc}
    ///
    /// Returns a view of the portion of this map whose keys are less than (or equal to, if `inclusive` is true) `toKey`.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #HEAD_MAP_TWO_ARG_RETURNS_EMPTY_MAP} - Returns an empty navigable map
    /// - {@link #HEAD_MAP_TWO_ARG_RETURNS_NULL} - Returns null
    /// - {@link #HEAD_MAP_TWO_ARG_THROWS_EXCEPTION} - Throws IllegalArgumentException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw IllegalArgumentException due to HEAD_MAP_TWO_ARG_THROWS_EXCEPTION break
    ///
    /// @param toKey high endpoint of the keys in the returned map
    /// @param inclusive `true` if the high endpoint is to be included in the returned view
    /// @return a view of the portion of this map whose keys are less than (or equal to, if `inclusive` is true) `toKey`
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#headMap(Object, boolean)
    @Override
    public @NonNull NavigableMap<K, V> headMap(final K toKey, final boolean inclusive) {
        if (!supportsMethod(NavigableMapMethods.HEAD_MAP_TWO_ARG)) {
            throw new UnsupportedOperationException("headMap(K, boolean) method is not supported");
        }
        if (hasBreak(HEAD_MAP_TWO_ARG_RETURNS_EMPTY_MAP)) {
            //noinspection SortedCollectionWithNonComparableKeys
            return new TreeMap<>();
        }
        if (hasBreak(HEAD_MAP_TWO_ARG_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }
        if (hasBreak(HEAD_MAP_TWO_ARG_THROWS_EXCEPTION)) {
            throw new IllegalArgumentException("HeadMap creation failed due to break");
        }
        return navigableMap.headMap(toKey, inclusive);
    }

    /// {@inheritDoc}
    ///
    /// Returns a view of the portion of this map whose keys are greater than (or equal to, if `inclusive` is true) `fromKey`.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #TAIL_MAP_TWO_ARG_RETURNS_EMPTY_MAP} - Returns an empty navigable map
    /// - {@link #TAIL_MAP_TWO_ARG_RETURNS_NULL} - Returns null
    /// - {@link #TAIL_MAP_TWO_ARG_THROWS_EXCEPTION} - Throws IllegalArgumentException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw IllegalArgumentException due to TAIL_MAP_TWO_ARG_THROWS_EXCEPTION break
    ///
    /// @param fromKey low endpoint of the keys in the returned map
    /// @param inclusive `true` if the low endpoint is to be included in the returned view
    /// @return a view of the portion of this map whose keys are greater than (or equal to, if `inclusive` is true) `fromKey`
    /// @throws UnsupportedOperationException if the method is not supported
    /// @see NavigableMap#tailMap(Object, boolean)
    @Override
    public @NonNull NavigableMap<K, V> tailMap(final K fromKey, final boolean inclusive) {
        if (!supportsMethod(NavigableMapMethods.TAIL_MAP_TWO_ARG)) {
            throw new UnsupportedOperationException("tailMap(K, boolean) method is not supported");
        }
        if (hasBreak(TAIL_MAP_TWO_ARG_RETURNS_EMPTY_MAP)) {
            //noinspection SortedCollectionWithNonComparableKeys
            return new TreeMap<>();
        }
        if (hasBreak(TAIL_MAP_TWO_ARG_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }
        if (hasBreak(TAIL_MAP_TWO_ARG_THROWS_EXCEPTION)) {
            throw new IllegalArgumentException("TailMap creation failed due to break");
        }
        return navigableMap.tailMap(fromKey, inclusive);
    }

    // ========== Builder Class ==========

    /// Builder class for creating BreakableNavigableMap instances with fluent configuration.
    ///
    /// This builder extends BreakableSortedMap.Builder and provides additional configuration
    /// options specific to NavigableMap functionality.
    /// @param <K> the type of keys maintained by this map
    /// @param <V> the type of mapped values
    public static class Builder<K, V> extends BreakableSortedMap.AbstractBuilder<Builder<K, V>,
            BreakableNavigableMap<K, V>, K, V> {

        /// Creates a new Builder with default configuration.
        @SuppressWarnings("SortedCollectionWithNonComparableKeys")
        public Builder() {
            this(new TreeMap<>());
        }

        /// Creates a new Builder by copying configuration from another builder.
        ///
        /// @param other the builder to copy configuration from
        /// @throws NullPointerException if other is null
        public Builder(final @NonNull Builder<K, V> other) {
            super(other);
        }

        /// Creates a new Builder pre-populated with elements from the specified map.
        ///
        /// @param elements the map whose elements are to be placed in the builder
        /// @throws NullPointerException if elements is null
        public Builder(final @NonNull Map<K, V> elements) {
            super(elements);
        }

        /// {@inheritDoc}
        @Override
        public Builder<K, V> self() {
            return this;
        }

        /// {@inheritDoc}
        @Override
        public @NonNull Builder<K, V> copy() {
            return new Builder<>(this);
        }

        /// Builds a new [BreakableNavigableMap] instance with the configured settings.
        ///
        /// @return a new BreakableNavigableMap instance
        @Override
        public @NonNull BreakableNavigableMap<K, V> build() {
            NavigableMap<K, V> map;
            if (comparator() == null) {
                map = new TreeMap<>(elements());
            } else {
                map = new TreeMap<>(comparator());
                map.putAll(elements());
            }
            return new BreakableNavigableMap<>(map, breaks(), methodStatuses(), permits(), isSafe());
        }
    }

    // ========== Static Factory Methods ==========

    /// Creates a BreakableNavigableMap that wraps the specified NavigableMap with the given breaks.
    ///
    /// @param <K> the type of keys maintained by the map
    /// @param <V> the type of mapped values
    /// @param navigableMap the NavigableMap to wrap
    /// @param breaks the set of breaks to apply
    /// @return a new BreakableNavigableMap wrapping the specified map
    /// @throws NullPointerException if navigableMap or breaks is null
    /// @since 1.0.0
    public static <K, V> @NonNull BreakableNavigableMap<K, V> wrap(
            final @NonNull NavigableMap<K, V> navigableMap,
            final @NonNull Set<Break> breaks) {
        return new BreakableNavigableMap<>(navigableMap, breaks, DEFAULT_METHOD_STATUSES, DEFAULT_PERMITS,
                DEFAULT_SAFETY);
    }

    /// Creates a BreakableNavigableMap that wraps the specified NavigableMap with full configuration.
    ///
    /// @param <K> the type of keys maintained by the map
    /// @param <V> the type of mapped values
    /// @param navigableMap the NavigableMap to wrap
    /// @param breaks the set of breaks to apply
    /// @param permitsNullKeys whether the map permits null keys
    /// @param permitsNullValues whether the map permits null values
    /// @return a new BreakableNavigableMap wrapping the specified map
    /// @throws NullPointerException if navigableMap or breaks is null
    /// @since 1.0.0
    public static <K, V> @NonNull BreakableNavigableMap<K, V> wrap(
            final @NonNull NavigableMap<K, V> navigableMap,
            final @NonNull Set<Break> breaks,
            final boolean permitsNullKeys,
            final boolean permitsNullValues) {
        return new BreakableNavigableMap<>(navigableMap, breaks, DEFAULT_METHOD_STATUSES, DEFAULT_PERMITS,
                DEFAULT_SAFETY);
    }
}
