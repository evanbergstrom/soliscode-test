package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.sortedmap.SortedMapMethods;

import java.io.Serial;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/// A SortedMap implementation that can be programmatically broken for testing purposes.
///
/// This class extends BreakableMap and implements the SortedMap interface, providing
/// additional break constants for testing SortedMap-specific functionality. It wraps
/// an existing SortedMap and allows specific behaviors to be "broken" through the
/// application of Break constants.
///
/// BreakableSortedMap inherits all the break capabilities from BreakableMap and adds
/// specific breaks for SortedMap methods including:
///
/// - Comparator access failures
/// - First/last key retrieval failures
/// - Sub-map creation failures (headMap, tailMap, subMap)
/// - Key ordering and boundary failures
///
/// ## SortedMap-Specific Usage Examples
///
/// ### Testing Key Boundary Issues
/// <pre>{@code
/// // Create a sorted map that always fails to find first/last keys
/// SortedMap<String, Integer> brokenMap = new BreakableSortedMap.Builder<String, Integer>()
///     .addBreak(BreakableSortedMap.FIRST_KEY_THROWS_EXCEPTION)
///     .addBreak(BreakableSortedMap.LAST_KEY_THROWS_EXCEPTION)
///     .build();
///
/// brokenMap.put("apple", 1);
/// brokenMap.put("banana", 2);
/// // These will throw NoSuchElementException due to breaks
/// try {
///     brokenMap.firstKey(); // Will throw
/// } catch (NoSuchElementException e) {
///     // Expected due to break
/// }
/// }</pre>
///
/// ### Testing Sub-Map Creation Failures
/// <pre>{@code
/// // Create a sorted map that returns empty sub-maps
/// SortedMap<Integer, String> brokenMap = new BreakableSortedMap.Builder<Integer, String>()
///     .addBreak(BreakableSortedMap.HEAD_MAP_RETURNS_EMPTY_MAP)
///     .addBreak(BreakableSortedMap.TAIL_MAP_RETURNS_EMPTY_MAP)
///     .build();
///
/// brokenMap.put(1, "one");
/// brokenMap.put(2, "two");
/// brokenMap.put(3, "three");
///
/// SortedMap<Integer, String> headMap = brokenMap.headMap(3);
/// assert headMap.isEmpty(); // Empty due to break
/// }</pre>
///
/// ### Testing Comparator Issues
/// <pre>{@code
/// // Create a sorted map that returns null comparator
/// SortedMap<String, String> brokenMap = new BreakableSortedMap.Builder<String, String>()
///     .addBreak(BreakableSortedMap.COMPARATOR_ALWAYS_RETURNS_NULL)
///     .build();
///
/// Comparator<? super String> comp = brokenMap.comparator();
/// assert comp == null; // Null due to break
/// }</pre>
///
/// @param <K> the type of keys maintained by this map
/// @param <V> the type of mapped values
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableMap
/// @see SortedMap
/// @see Break
public class BreakableSortedMap<K, V> extends BreakableMap<K, V> implements SortedMap<K, V> {

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== comparator() method breaks ==========

    /// Break constant that causes comparator() to always return null.
    ///
    /// When this break is applied, the comparator() method will return null regardless
    /// of whether the underlying SortedMap has a comparator. This simulates scenarios
    /// where comparator access fails or becomes unavailable.
    ///
    /// **Affected Methods:**
    /// - {@link #comparator()} - Returns null instead of actual comparator
    ///
    /// **Use Cases:**
    /// - Testing code that depends on comparator availability
    /// - Simulating comparator access failures
    /// - Testing natural ordering fallback logic
    public static final Break COMPARATOR_ALWAYS_RETURNS_NULL =
            new Break("COMPARATOR_ALWAYS_RETURNS_NULL");

    /// Break constant that causes comparator() to always return the natural comparator if the keys are comparable..
    ///
    /// When this break is applied, the comparator() method will return the natural comparator for the keys regardless
    /// of whether another comparator has been provided. This simulates scenarios where the provided comparator is
    /// being ignored.
    ///
    /// **Affected Methods:**
    /// - {@link #comparator()} - Returns natural comparator instead of provided comparator
    ///
    /// **Use Cases:**
    /// - Testing code that depends on custom comparators
    /// - Simulating comparator setting failure
    public static final Break COMPARATOR_ALWAYS_RETURNS_NATURAL_COMPARATOR =
            new Break("COMPARATOR_ALWAYS_RETURNS_NATURAL_COMPARATOR");

    /// Break constant that causes comparator() to throw ClassCastException.
    ///
    /// When this break is applied, the comparator() method will throw a ClassCastException
    /// instead of returning the actual comparator. This simulates scenarios where
    /// comparator retrieval encounters type compatibility issues.
    ///
    /// **Affected Methods:**
    /// - {@link #comparator()} - Throws ClassCastException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in comparator access
    /// - Simulating type compatibility failures
    /// - Testing robustness of sorting logic
    public static final Break COMPARATOR_THROWS_EXCEPTION =
            new Break("COMPARATOR_THROWS_EXCEPTION");

    // ========== firstKey() method breaks ==========

    /// Break constant that causes firstKey() to throw NoSuchElementException even when elements exist.
    ///
    /// When this break is applied, the firstKey() method will throw NoSuchElementException
    /// regardless of whether the map contains elements. This simulates scenarios where
    /// first key retrieval fails despite the map having content.
    ///
    /// **Affected Methods:**
    /// - {@link #firstKey()} - Throws NoSuchElementException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in boundary key access
    /// - Simulating key retrieval failures
    /// - Testing edge case handling in sorted collections
    public static final Break FIRST_KEY_THROWS_EXCEPTION =
            new Break("FIRST_KEY_THROWS_EXCEPTION");

    /// Break constant that causes firstKey() to return a random key instead of the actual first key.
    ///
    /// When this break is applied, the firstKey() method will return an arbitrary key from
    /// the map instead of the first key in sort order. This simulates scenarios where
    /// key ordering is corrupted or unreliable.
    ///
    /// **Affected Methods:**
    /// - {@link #firstKey()} - Returns random key instead of first
    ///
    /// **Use Cases:**
    /// - Testing ordering assumptions
    /// - Simulating corrupted sort order
    /// - Testing robustness against ordering violations
    public static final Break FIRST_KEY_RETURNS_RANDOM_KEY =
            new Break("FIRST_KEY_RETURNS_RANDOM_KEY");

    // ========== lastKey() method breaks ==========

    /// Break constant that causes lastKey() to throw NoSuchElementException even when elements exist.
    ///
    /// When this break is applied, the lastKey() method will throw NoSuchElementException
    /// regardless of whether the map contains elements. This simulates scenarios where
    /// last key retrieval fails despite the map having content.
    ///
    /// **Affected Methods:**
    /// - {@link #lastKey()} - Throws NoSuchElementException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in boundary key access
    /// - Simulating key retrieval failures
    /// - Testing edge case handling in sorted collections
    public static final Break LAST_KEY_THROWS_EXCEPTION =
            new Break("LAST_KEY_THROWS_EXCEPTION");

    /// Break constant that causes lastKey() to return a random key instead of the actual last key.
    ///
    /// When this break is applied, the lastKey() method will return an arbitrary key from
    /// the map instead of the last key in sort order. This simulates scenarios where
    /// key ordering is corrupted or unreliable.
    ///
    /// **Affected Methods:**
    /// - {@link #lastKey()} - Returns random key instead of last
    ///
    /// **Use Cases:**
    /// - Testing ordering assumptions
    /// - Simulating corrupted sort order
    /// - Testing robustness against ordering violations
    public static final Break LAST_KEY_RETURNS_RANDOM_KEY =
            new Break("LAST_KEY_RETURNS_RANDOM_KEY");

    /// Break constant that causes headMap() to return an empty map regardless of input.
    ///
    /// When this break is applied, the headMap() method will return an empty SortedMap
    /// regardless of the toKey parameter or the actual content that should be included.
    /// This simulates scenarios where sub-map creation fails or returns incorrect results.
    ///
    /// **Affected Methods:**
    /// - {@link #headMap(Object)} - Returns empty map
    ///
    /// **Use Cases:**
    /// - Testing sub-map creation failures
    /// - Simulating range query failures
    /// - Testing empty result handling
    public static final Break HEAD_MAP_RETURNS_EMPTY_MAP =
            new Break("HEAD_MAP_RETURNS_EMPTY_MAP");

    /// Break constant that causes headMap() to return null instead of a SortedMap.
    ///
    /// When this break is applied, the headMap() method will return null instead of
    /// a valid SortedMap. This simulates scenarios where sub-map creation completely
    /// fails or is unavailable.
    ///
    /// **Affected Methods:**
    /// - {@link #headMap(Object)} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing null result handling
    /// - Simulating complete sub-map creation failure
    /// - Testing robustness against null returns
    public static final Break HEAD_MAP_RETURNS_NULL =
            new Break("HEAD_MAP_RETURNS_NULL");

    /// Break constant that causes headMap() to throw IllegalArgumentException.
    ///
    /// When this break is applied, the headMap() method will throw IllegalArgumentException
    /// regardless of the validity of the toKey parameter. This simulates scenarios where
    /// sub-map creation encounters argument validation failures.
    ///
    /// **Affected Methods:**
    /// - {@link #headMap(Object)} - Throws IllegalArgumentException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in sub-map creation
    /// - Simulating parameter validation failures
    /// - Testing argument validation robustness
    public static final Break HEAD_MAP_THROWS_EXCEPTION =
            new Break("HEAD_MAP_THROWS_EXCEPTION");

    /// Break constant that causes tailMap() to return an empty map regardless of input.
    ///
    /// When this break is applied, the tailMap() method will return an empty SortedMap
    /// regardless of the fromKey parameter or the actual content that should be included.
    /// This simulates scenarios where sub-map creation fails or returns incorrect results.
    ///
    /// **Affected Methods:**
    /// - {@link #tailMap(Object)} - Returns empty map
    ///
    /// **Use Cases:**
    /// - Testing sub-map creation failures
    /// - Simulating range query failures
    /// - Testing empty result handling
    public static final Break TAIL_MAP_RETURNS_EMPTY_MAP =
            new Break("TAIL_MAP_RETURNS_EMPTY_MAP");

    /// Break constant that causes tailMap() to return null instead of a SortedMap.
    ///
    /// When this break is applied, the tailMap() method will return null instead of
    /// a valid SortedMap. This simulates scenarios where sub-map creation completely
    /// fails or is unavailable.
    ///
    /// **Affected Methods:**
    /// - {@link #tailMap(Object)} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing null result handling
    /// - Simulating complete sub-map creation failure
    /// - Testing robustness against null returns
    public static final Break TAIL_MAP_RETURNS_NULL =
            new Break("TAIL_MAP_RETURNS_NULL");

    /// Break constant that causes tailMap() to throw IllegalArgumentException.
    ///
    /// When this break is applied, the tailMap() method will throw IllegalArgumentException
    /// regardless of the validity of the fromKey parameter. This simulates scenarios where
    /// sub-map creation encounters argument validation failures.
    ///
    /// **Affected Methods:**
    /// - {@link #tailMap(Object)} - Throws IllegalArgumentException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in sub-map creation
    /// - Simulating parameter validation failures
    /// - Testing argument validation robustness
    public static final Break TAIL_MAP_THROWS_EXCEPTION =
            new Break("TAIL_MAP_THROWS_EXCEPTION");

    /// Break constant that causes subMap() to return an empty map regardless of input.
    ///
    /// When this break is applied, the subMap() method will return an empty SortedMap
    /// regardless of the fromKey and toKey parameters or the actual content that should
    /// be included. This simulates scenarios where range-based sub-map creation fails.
    ///
    /// **Affected Methods:**
    /// - {@link #subMap(Object, Object)} - Returns empty map
    ///
    /// **Use Cases:**
    /// - Testing range query failures
    /// - Simulating sub-map creation failures
    /// - Testing empty result handling in range operations
    public static final Break SUB_MAP_RETURNS_EMPTY_MAP =
            new Break("SUB_MAP_RETURNS_EMPTY_MAP");

    /// Break constant that causes subMap() to return null instead of a SortedMap.
    ///
    /// When this break is applied, the subMap() method will return null instead of
    /// a valid SortedMap. This simulates scenarios where range-based sub-map creation
    /// completely fails or is unavailable.
    ///
    /// **Affected Methods:**
    /// - {@link #subMap(Object, Object)} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing null result handling
    /// - Simulating complete range query failure
    /// - Testing robustness against null returns
    public static final Break SUB_MAP_RETURNS_NULL =
            new Break("SUB_MAP_RETURNS_NULL");

    /// Break constant that causes subMap() to throw IllegalArgumentException.
    ///
    /// When this break is applied, the subMap() method will throw IllegalArgumentException
    /// regardless of the validity of the fromKey and toKey parameters. This simulates
    /// scenarios where range validation fails or parameters are rejected.
    ///
    /// **Affected Methods:**
    /// - {@link #subMap(Object, Object)} - Throws IllegalArgumentException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in range operations
    /// - Simulating parameter validation failures
    /// - Testing argument validation robustness
    public static final Break SUB_MAP_THROWS_EXCEPTION =
            new Break("SUB_MAP_THROWS_EXCEPTION");

    // ========== Instance Variables ==========

    /// The underlying SortedMap that provides the actual functionality.
    /// This map is wrapped and its behavior can be modified through breaks.
    private final @NonNull SortedMap<K, V> sortedMap;

    // ========== Constructors ==========

    /// Creates a BreakableSortedMap with no breaks applied using TreeMap as the underlying implementation.
    ///
    /// This constructor creates an empty sorted map that behaves normally until breaks are applied.
    /// The map uses natural ordering of keys (keys must implement Comparable) and permits null
    /// keys and values by default.
    ///
    /// **Default Configuration:**
    /// - Empty TreeMap as underlying implementation
    /// - Natural key ordering (Comparable-based)
    /// - Permits null keys: true
    /// - Permits null values: true
    /// - No breaks applied
    ///
    /// **Usage:**
    /// ```java
    /// BreakableSortedMap<String, Integer> map = new BreakableSortedMap<>();
    /// map.put("apple", 1);
    /// map.put("banana", 2);
    /// ```
    @SuppressWarnings("SortedCollectionWithNonComparableKeys")
    public BreakableSortedMap() {
        super();
        this.sortedMap = new TreeMap<>();
    }

    /// Creates a BreakableSortedMap by copying another BreakableSortedMap.
    ///
    /// This constructor creates a new instance that shares the underlying sorted map data
    /// with the original but copies all break configuration and policies. The resulting
    /// map is functionally identical but independent for future modifications.
    ///
    /// **Copy Behavior:**
    /// - Underlying sorted map data is shared (shallow copy)
    /// - Break configuration is deep copied
    /// - Null policies are copied
    /// - Independent modification after creation
    ///
    /// **Usage:**
    /// ```java
    /// BreakableSortedMap<String, Integer> original = new BreakableSortedMap<>();
    /// original.addBreak(FIRST_KEY_THROWS_EXCEPTION);
    /// BreakableSortedMap<String, Integer> copy = new BreakableSortedMap<>(original);
    /// // copy has same data and breaks but is independent
    /// ```
    ///
    /// @param other the BreakableSortedMap to copy; must not be null
    /// @throws NullPointerException if other is null
    public BreakableSortedMap(final @NonNull BreakableSortedMap<K, V> other) {
        super(other);
        this.sortedMap = other.sortedMap;
    }

    /// Creates a BreakableSortedMap wrapping an existing SortedMap with specified breaks and policies.
    ///
    /// This constructor creates a BreakableSortedMap that wraps the provided SortedMap and applies
    /// the specified breaks and null handling policies. The underlying map is used directly
    /// (not copied), so modifications through the BreakableSortedMap will affect the original.
    ///
    /// **Construction Behavior:**
    /// - Wraps the provided SortedMap directly
    /// - Applies all specified breaks immediately
    /// - Enforces null policies on construction if existing data violates them
    /// - Validates null policy compliance
    ///
    /// **Usage:**
    /// ```java
    /// TreeMap<String, Integer> existingMap = new TreeMap<>();
    /// existingMap.put("apple", 1);
    /// Set<Break> breaks = Set.of(FIRST_KEY_THROWS_EXCEPTION);
    /// BreakableSortedMap<String, Integer> wrapped = new BreakableSortedMap<>(
    ///     existingMap, breaks, true, true);
    /// ```
    ///
    /// @param sortedMap the SortedMap to wrap; must not be null
    /// @param breaks the collection of breaks to apply; must not be null
    /// @throws NullPointerException if sortedMap or breaks is null, or if existing data
    ///                              violates the specified null policies
    public BreakableSortedMap(final @NonNull SortedMap<K, V> sortedMap,
                              final @NonNull Set<Break> breaks,
                              final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
                              final int permits,
                              final boolean isSafe) {
        super(this.sortedMap = sortedMap, breaks, methodStatuses, permits, isSafe);
    }

    // ========== SortedMap Interface Methods ==========

    /// {@inheritDoc}
    ///
    /// Returns the comparator used to order the keys in this map, or null if this map uses
    /// the natural ordering of its keys. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #COMPARATOR_ALWAYS_RETURNS_NULL} - Returns null regardless of actual comparator
    /// - {@link #COMPARATOR_THROWS_EXCEPTION} - Throws ClassCastException
    /// - {@link #COMPARATOR_ALWAYS_RETURNS_NATURAL_COMPARATOR} - Always return s natural comparator
    ///
    /// **Normal Behavior:**
    /// When no breaks are applied, this method delegates to the underlying SortedMap's
    /// comparator() method and returns the actual comparator being used for key ordering.
    ///
    /// **Break Behavior:**
    /// - **COMPARATOR_ALWAYS_RETURNS_NULL**: Returns null even if the underlying map has a comparator
    /// - **COMPARATOR_THROWS_EXCEPTION**: Throws ClassCastException instead of returning comparator
    /// - **COMPARATOR_ALWAYS_RETURNS_NATURAL_COMPARATOR**: Returns the natural comparator if the map has at least one
    ///   key.
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw ClassCastException due to COMPARATOR_THROWS_EXCEPTION break
    ///
    /// @return the comparator used to order the keys in this map, or null if this map uses
    ///         the natural ordering of its keys
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws ClassCastException if the COMPARATOR_THROWS_EXCEPTION break is applied
    @SuppressWarnings("unchecked")
    @Override
    public @Nullable Comparator<? super K> comparator() {
        if (!supportsMethod(SortedMapMethods.COMPARATOR)) {
            throw new UnsupportedOperationException("comparator() method is not supported");
        }
        if (hasBreak(COMPARATOR_ALWAYS_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(COMPARATOR_THROWS_EXCEPTION)) {
            throw new ClassCastException("Comparator access failed due to break");
        }
        if (hasBreak(COMPARATOR_ALWAYS_RETURNS_NATURAL_COMPARATOR) && !sortedMap.isEmpty()) {
            K key = sortedMap.firstKey();
            if (key instanceof Comparable<?> c) {
                // unchecked cast
                return (Comparator<? super K>) Comparator.naturalOrder();
            }
        }
        return sortedMap.comparator();
    }

    /// {@inheritDoc}
    ///
    /// Returns the first (lowest) key currently in this map. The behavior can be modified
    /// by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #FIRST_KEY_THROWS_EXCEPTION} - Throws NoSuchElementException even when elements exist
    /// - {@link #FIRST_KEY_RETURNS_RANDOM_KEY} - Returns arbitrary key instead of first
    ///
    /// **Normal Behavior:**
    /// When no breaks are applied, this method delegates to the underlying SortedMap's
    /// firstKey() method and returns the first key according to the map's ordering.
    ///
    /// **Break Behavior:**
    /// - **FIRST_KEY_THROWS_EXCEPTION**: Always throws NoSuchElementException
    /// - **FIRST_KEY_RETURNS_RANDOM_KEY**: Returns a random key from the map instead of the first
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - Throws NoSuchElementException if the map is empty or due to FIRST_KEY_THROWS_EXCEPTION break
    ///
    /// @return the first (lowest) key currently in this map
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws NoSuchElementException if this map is empty or due to break
    @Override
    public @NonNull K firstKey() {
        if (!supportsMethod(SortedMapMethods.FIRST_KEY)) {
            throw new UnsupportedOperationException("firstKey() method is not supported");
        }

        if (hasBreak(FIRST_KEY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("First key access failed due to break");
        }

        if (hasBreak(FIRST_KEY_RETURNS_RANDOM_KEY) && !sortedMap.isEmpty()) {
            // Return an arbitrary key instead of the first
            return sortedMap.keySet().iterator().next();
        }

        return sortedMap.firstKey();
    }

    /// {@inheritDoc}
    ///
    /// Returns the last (highest) key currently in this map. The behavior can be modified
    /// by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #LAST_KEY_THROWS_EXCEPTION} - Throws NoSuchElementException even when elements exist
    /// - {@link #LAST_KEY_RETURNS_RANDOM_KEY} - Returns arbitrary key instead of last
    ///
    /// **Normal Behavior:**
    /// When no breaks are applied, this method delegates to the underlying SortedMap's
    /// lastKey() method and returns the last key according to the map's ordering.
    ///
    /// **Break Behavior:**
    /// - **LAST_KEY_THROWS_EXCEPTION**: Always throws NoSuchElementException
    /// - **LAST_KEY_RETURNS_RANDOM_KEY**: Returns a random key from the map instead of the last
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - Throws NoSuchElementException if the map is empty or due to LAST_KEY_THROWS_EXCEPTION break
    ///
    /// @return the last (highest) key currently in this map
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws NoSuchElementException if this map is empty or due to break
    @Override
    public @NonNull K lastKey() {
        if (!supportsMethod(SortedMapMethods.LAST_KEY)) {
            throw new UnsupportedOperationException("lastKey() method is not supported");
        }

        if (hasBreak(LAST_KEY_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("Last key access failed due to break");
        }

        if (hasBreak(LAST_KEY_RETURNS_RANDOM_KEY) && !sortedMap.isEmpty()) {
            // Return an arbitrary key instead of the last
            return sortedMap.keySet().iterator().next();
        }

        return sortedMap.lastKey();
    }

    /// {@inheritDoc}
    ///
    /// Returns a view of the portion of this map whose keys are strictly less than toKey.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #HEAD_MAP_RETURNS_EMPTY_MAP} - Returns empty map regardless of content
    /// - {@link #HEAD_MAP_RETURNS_NULL} - Returns null instead of SortedMap
    /// - {@link #HEAD_MAP_THROWS_EXCEPTION} - Throws IllegalArgumentException
    ///
    /// **Normal Behavior:**
    /// When no breaks are applied, this method delegates to the underlying SortedMap's
    /// headMap() method and returns a view of the portion with keys less than toKey.
    ///
    /// **Break Behavior:**
    /// - **HEAD_MAP_RETURNS_EMPTY_MAP**: Returns an empty TreeMap
    /// - **HEAD_MAP_RETURNS_NULL**: Returns null
    /// - **HEAD_MAP_THROWS_EXCEPTION**: Throws IllegalArgumentException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - Throws IllegalArgumentException due to HEAD_MAP_THROWS_EXCEPTION break
    /// - May throw ClassCastException, NullPointerException, or IllegalArgumentException
    ///   from the underlying implementation
    ///
    /// @param toKey high endpoint (exclusive) of the keys in the returned map
    /// @return a view of the portion of this map whose keys are strictly less than toKey
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws IllegalArgumentException due to break or if toKey is not compatible with this map's comparator
    /// @throws ClassCastException if toKey is not compatible with this map's comparator
    /// @throws NullPointerException if toKey is null and this map does not permit null keys
    @Override
    public @NonNull SortedMap<K, V> headMap(final K toKey) {
        if (!supportsMethod(SortedMapMethods.HEAD_MAP)) {
            throw new UnsupportedOperationException("headMap() method is not supported");
        }

        if (hasBreak(HEAD_MAP_RETURNS_EMPTY_MAP)) {
            //noinspection SortedCollectionWithNonComparableKeys
            return new TreeMap<>();
        }

        if (hasBreak(HEAD_MAP_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }

        if (hasBreak(HEAD_MAP_THROWS_EXCEPTION)) {
            throw new IllegalArgumentException("HeadMap creation failed due to break");
        }

        return sortedMap.headMap(toKey);
    }

    /// {@inheritDoc}
    ///
    /// Returns a view of the portion of this map whose keys are greater than or equal to fromKey.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #TAIL_MAP_RETURNS_EMPTY_MAP} - Returns empty map regardless of content
    /// - {@link #TAIL_MAP_RETURNS_NULL} - Returns null instead of SortedMap
    /// - {@link #TAIL_MAP_THROWS_EXCEPTION} - Throws IllegalArgumentException
    ///
    /// **Normal Behavior:**
    /// When no breaks are applied, this method delegates to the underlying SortedMap's
    /// tailMap() method and returns a view of the portion with keys greater than or equal to fromKey.
    ///
    /// **Break Behavior:**
    /// - **TAIL_MAP_RETURNS_EMPTY_MAP**: Returns an empty TreeMap
    /// - **TAIL_MAP_RETURNS_NULL**: Returns null
    /// - **TAIL_MAP_THROWS_EXCEPTION**: Throws IllegalArgumentException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - Throws IllegalArgumentException due to TAIL_MAP_THROWS_EXCEPTION break
    /// - May throw ClassCastException, NullPointerException, or IllegalArgumentException
    ///   from the underlying implementation
    ///
    /// @param fromKey low endpoint (inclusive) of the keys in the returned map
    /// @return a view of the portion of this map whose keys are greater than or equal to fromKey
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws IllegalArgumentException due to break or if fromKey is not compatible with this map's comparator
    /// @throws ClassCastException if fromKey is not compatible with this map's comparator
    /// @throws NullPointerException if fromKey is null and this map does not permit null keys
    @Override
    public @NonNull SortedMap<K, V> tailMap(final K fromKey) {
        if (!supportsMethod(SortedMapMethods.TAIL_MAP)) {
            throw new UnsupportedOperationException("tailMap() method is not supported");
        }

        if (hasBreak(TAIL_MAP_RETURNS_EMPTY_MAP)) {
            //noinspection SortedCollectionWithNonComparableKeys
            return new TreeMap<>();
        }

        if (hasBreak(TAIL_MAP_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }

        if (hasBreak(TAIL_MAP_THROWS_EXCEPTION)) {
            throw new IllegalArgumentException("TailMap creation failed due to break");
        }

        return sortedMap.tailMap(fromKey);
    }

    /// {@inheritDoc}
    ///
    /// Returns a view of the portion of this map whose keys range from fromKey (inclusive)
    /// to toKey (exclusive). The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #SUB_MAP_RETURNS_EMPTY_MAP} - Returns empty map regardless of content
    /// - {@link #SUB_MAP_RETURNS_NULL} - Returns null instead of SortedMap
    /// - {@link #SUB_MAP_THROWS_EXCEPTION} - Throws IllegalArgumentException
    ///
    /// **Normal Behavior:**
    /// When no breaks are applied, this method delegates to the underlying SortedMap's
    /// subMap() method and returns a view of the portion with keys in the specified range.
    ///
    /// **Break Behavior:**
    /// - **SUB_MAP_RETURNS_EMPTY_MAP**: Returns an empty TreeMap
    /// - **SUB_MAP_RETURNS_NULL**: Returns null
    /// - **SUB_MAP_THROWS_EXCEPTION**: Throws IllegalArgumentException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - Throws IllegalArgumentException due to SUB_MAP_THROWS_EXCEPTION break or if
    ///   fromKey is greater than toKey
    /// - May throw ClassCastException or NullPointerException from the underlying implementation
    ///
    /// @param fromKey low endpoint (inclusive) of the keys in the returned map
    /// @param toKey high endpoint (exclusive) of the keys in the returned map
    /// @return a view of the portion of this map whose keys range from fromKey (inclusive)
    ///         to toKey (exclusive)
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws IllegalArgumentException due to break, if fromKey is greater than toKey, or
    ///         if the keys are not compatible with this map's comparator
    /// @throws ClassCastException if fromKey or toKey is not compatible with this map's comparator
    /// @throws NullPointerException if fromKey or toKey is null and this map does not permit null keys
    @Override
    public @NonNull SortedMap<K, V> subMap(final K fromKey, final K toKey) {
        if (!supportsMethod(SortedMapMethods.SUB_MAP)) {
            throw new UnsupportedOperationException("subMap() method is not supported");
        }

        if (hasBreak(SUB_MAP_RETURNS_EMPTY_MAP)) {
            //noinspection SortedCollectionWithNonComparableKeys
            return new TreeMap<>();
        }

        if (hasBreak(SUB_MAP_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null;
        }

        if (hasBreak(SUB_MAP_THROWS_EXCEPTION)) {
            throw new IllegalArgumentException("SubMap creation failed due to break");
        }

        return sortedMap.subMap(fromKey, toKey);
    }

    // ========== Builder Pattern ==========

    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, M, K, V>,
            M extends BreakableSortedMap<K, V>, K, V>
            extends BreakableMap.AbstractBuilder<B, M, K, V> {

        private Comparator<K> comparator;

        public AbstractBuilder() {
            super();
        }

        public AbstractBuilder(final @NonNull Map<K, V> map) {
            super(map);
        }

        public AbstractBuilder(final @NonNull AbstractBuilder<B, M, K, V> other) {
            super(other);
        }

        public B setComparator(final @NonNull Comparator<K> newComparator) {
            this.comparator = newComparator;
            return self();
        }

        public Comparator<K> comparator() {
            return comparator;
        }
    }

    /// Builder for creating BreakableSortedMap instances with custom configuration.
    ///
    /// The Builder pattern allows for fluent, readable construction of BreakableSortedMap instances
    /// with specific breaks, null policies, and method support configuration. This approach
    /// provides better readability and flexibility compared to constructors with many parameters.
    ///
    /// ## Builder Capabilities
    ///
    /// ### Break Configuration
    /// <pre>{@code
    /// BreakableSortedMap<String, Integer> map = new BreakableSortedMap.Builder<String, Integer>()
    ///     .addBreak(FIRST_KEY_THROWS_EXCEPTION)
    ///     .addBreak(HEAD_MAP_RETURNS_EMPTY_MAP)
    ///     .build();
    /// }</pre>
    ///
    /// ### Null Policy Configuration
    /// <pre>{@code
    /// BreakableSortedMap<String, Integer> map = new BreakableSortedMap.Builder<String, Integer>()
    ///     .doesNotPermitNullKeys()
    ///     .doesNotPermitNullValues()
    ///     .build();
    /// }</pre>
    ///
    /// ### Method Support Configuration
    /// <pre>{@code
    /// BreakableSortedMap<String, Integer> map = new BreakableSortedMap.Builder<String, Integer>()
    ///     .doesNotSupport(MapMethods.FirstKey)
    ///     .doesNotSupport(MapMethods.LastKey)
    ///     .build();
    /// }</pre>
    ///
    /// ### Pre-populated Map
    /// <pre>{@code
    /// TreeMap<String, Integer> existingData = new TreeMap<>();
    /// existingData.put("apple", 1);
    /// existingData.put("banana", 2);
    ///
    /// BreakableSortedMap<String, Integer> map = new BreakableSortedMap.Builder<>(existingData)
    ///     .addBreak(LAST_KEY_THROWS_EXCEPTION)
    ///     .build();
    /// }</pre>
    ///
    /// ### Builder Reuse and Copying
    /// <pre>{@code
    /// BreakableSortedMap.Builder<String, Integer> template =
    ///     new BreakableSortedMap.Builder<String, Integer>()
    ///         .doesNotPermitNullKeys()
    ///         .addBreak(COMPARATOR_ALWAYS_RETURNS_NULL);
    ///
    /// BreakableSortedMap<String, Integer> map1 = template.copy()
    ///     .addBreak(FIRST_KEY_THROWS_EXCEPTION)
    ///     .build();
    ///
    /// BreakableSortedMap<String, Integer> map2 = template.copy()
    ///     .addBreak(LAST_KEY_THROWS_EXCEPTION)
    ///     .build();
    /// }</pre>
    ///
    /// @param <K> the type of keys maintained by maps built by this builder
    /// @param <V> the type of mapped values in maps built by this builder
    /// @since 1.0
    /// @see BreakableSortedMap
    /// @see Break
    /// @see SortedMapMethods
    public static class Builder<K, V> extends AbstractBuilder<Builder<K, V>, BreakableSortedMap<K, V>, K, V> {

        /// Creates a new Builder with default configuration.
        ///
        /// **Default Configuration:**
        /// - Empty TreeMap as underlying implementation
        /// - Natural key ordering (Comparable-based)
        /// - Permits null keys: true
        /// - Permits null values: true
        /// - All methods supported
        /// - No breaks applied
        ///
        /// **Usage:**
        /// ```java
        /// BreakableSortedMap<String, Integer> map = new BreakableSortedMap.Builder<String, Integer>()
        ///     .addBreak(FIRST_KEY_THROWS_EXCEPTION)
        ///     .build();
        /// ```
        @SuppressWarnings("SortedCollectionWithNonComparableKeys")
        public Builder() {
            this(new TreeMap<>());
        }

        /// Creates a new Builder that will wrap the specified SortedMap.
        ///
        /// The provided SortedMap will be wrapped directly (not copied), so modifications
        /// through the resulting BreakableSortedMap will affect the original map.
        ///
        /// **Configuration:**
        /// - Uses provided SortedMap as underlying implementation
        /// - Inherits comparator from provided map
        /// - Permits null keys: true (default)
        /// - Permits null values: true (default)
        /// - All methods supported (default)
        /// - No breaks applied (default)
        ///
        /// **Usage:**
        /// ```java
        /// TreeMap<String, Integer> existingMap = new TreeMap<>();
        /// existingMap.put("apple", 1);
        /// BreakableSortedMap<String, Integer> map = new BreakableSortedMap.Builder<>(existingMap)
        ///     .addBreak(LAST_KEY_THROWS_EXCEPTION)
        ///     .build();
        /// ```
        ///
        /// @param elements the map containing the elements
        /// @throws NullPointerException if sortedMap is null
        public Builder(final @NonNull Map<K, V> elements) {
            super(elements);
        }

        @Override
        public Builder<K, V> self() {
            return this;
        }

        /// Creates a copy of this Builder with identical configuration.
        ///
        /// The copied Builder is completely independent and can be modified without
        /// affecting the original. This enables template-based construction patterns
        /// where a base configuration is established and then specialized.
        ///
        /// **Copy Behavior:**
        /// - All break configuration is copied
        /// - All method support configuration is copied
        /// - Null policy configuration is copied
        /// - Underlying SortedMap reference is copied (shared)
        /// - Independent modification after copying
        ///
        /// **Usage:**
        /// ```java
        /// BreakableSortedMap.Builder<String, Integer> template =
        ///     new BreakableSortedMap.Builder<String, Integer>()
        ///         .doesNotPermitNullKeys();
        ///
        /// BreakableSortedMap.Builder<String, Integer> specialized = template.copy()
        ///     .addBreak(FIRST_KEY_THROWS_EXCEPTION);
        /// ```
        ///
        /// @return a new Builder with identical configuration to this one
        @Override
        public @NonNull Builder<K, V> copy() {
            return new Builder<>(this);
        }

        /// Creates a builder by copying another builder.
        /// @param other the builder to copy
        public Builder(final @NonNull Builder<K, V> other) {
            super(other);
        }

        /// Builds the BreakableSortedMap with the configured settings.
        ///
        /// This method creates the final BreakableSortedMap instance using all the configuration
        /// that has been applied to this Builder. The Builder can be reused after calling
        /// build() to create additional instances with the same configuration.
        ///
        /// **Build Process:**
        /// 1. Validates null policy compliance with existing data
        /// 2. Creates BreakableSortedMap with configured SortedMap
        /// 3. Applies all configured breaks
        /// 4. Sets null handling policies
        /// 5. Configures method support
        ///
        /// **Exception Handling:**
        /// - Throws NullPointerException if existing data violates null policies
        ///
        /// **Usage:**
        /// ```java
        /// BreakableSortedMap<String, Integer> map = new BreakableSortedMap.Builder<String, Integer>()
        ///     .addBreak(FIRST_KEY_THROWS_EXCEPTION)
        ///     .doesNotPermitNullKeys()
        ///     .build();
        /// ```
        ///
        /// @return a new BreakableSortedMap configured according to this Builder's settings
        /// @throws NullPointerException if existing data in the underlying map violates
        ///                              the configured null policies
        @Override
        public @NonNull BreakableSortedMap<K, V> build() {
            SortedMap<K, V> map;
            if (comparator() == null) {
                map = new TreeMap<>(elements());
            } else {
                map = new TreeMap<>(comparator());
                map.putAll(elements());
            }
            return new BreakableSortedMap<>(map, breaks(), methodStatuses(), permits(), isSafe());
        }
    }

    // ========== Static Factory Methods ==========

    /// Creates a BreakableSortedMap wrapping the specified SortedMap with the given breaks.
    ///
    /// This static factory method provides a convenient way to create a BreakableSortedMap
    /// that wraps an existing SortedMap with specific breaks applied. The created map
    /// uses permissive null policies by default.
    ///
    /// **Factory Behavior:**
    /// - Wraps the provided SortedMap directly (not copied)
    /// - Applies all specified breaks immediately
    /// - Uses permissive null policies (permits both null keys and values)
    /// - All methods are supported by default
    ///
    /// **Usage:**
    /// ```java
    /// TreeMap<String, Integer> existingMap = new TreeMap<>();
    /// existingMap.put("apple", 1);
    /// existingMap.put("banana", 2);
    ///
    /// Set<Break> breaks = Set.of(
    ///     FIRST_KEY_THROWS_EXCEPTION,
    ///     HEAD_MAP_RETURNS_EMPTY_MAP
    /// );
    ///
    /// BreakableSortedMap<String, Integer> wrapped = BreakableSortedMap.wrap(existingMap, breaks);
    /// ```
    ///
    /// @param <K> the type of keys maintained by the map
    /// @param <V> the type of mapped values
    /// @param sortedMap the SortedMap to wrap; must not be null
    /// @param breaks the collection of breaks to apply; must not be null
    /// @return a new BreakableSortedMap wrapping the specified SortedMap with the given breaks
    /// @throws NullPointerException if sortedMap or breaks is null
    public static <K, V> @NonNull BreakableSortedMap<K, V> wrap(
            final @NonNull SortedMap<K, V> sortedMap,
            final @NonNull Set<Break> breaks) {
        return new BreakableSortedMap<>(sortedMap, breaks, DEFAULT_METHOD_STATUSES, DEFAULT_PERMITS, DEFAULT_SAFETY);
    }

    /// Creates a BreakableSortedMap wrapping the specified SortedMap with the given breaks and policies.
    ///
    /// This static factory method provides complete control over the BreakableSortedMap creation,
    /// allowing specification of both breaks and null handling policies. This is useful when
    /// you need precise control over the map's behavior.
    ///
    /// **Factory Behavior:**
    /// - Wraps the provided SortedMap directly (not copied)
    /// - Applies all specified breaks immediately
    /// - Enforces specified null policies
    /// - Validates existing data against null policies
    /// - All methods are supported by default
    ///
    /// **Usage:**
    /// ```java
    /// TreeMap<String, Integer> existingMap = new TreeMap<>();
    /// existingMap.put("apple", 1);
    /// Set<Break> breaks = Set.of(LAST_KEY_THROWS_EXCEPTION);
    ///
    /// BreakableSortedMap<String, Integer> wrapped = BreakableSortedMap.wrap(
    ///     existingMap, breaks, false, false); // Strict null policies
    /// ```
    ///
    /// @param <K> the type of keys maintained by the map
    /// @param <V> the type of mapped values
    /// @param sortedMap the SortedMap to wrap; must not be null
    /// @param breaks the collection of breaks to apply; must not be null
    /// @return a new BreakableSortedMap wrapping the specified SortedMap with the given
    ///         breaks and policies
    /// @throws NullPointerException if sortedMap or breaks is null, or if existing data
    ///                              violates the specified null policies
    public static <K, V> @NonNull BreakableSortedMap<K, V> wrap(
            final @NonNull SortedMap<K, V> sortedMap,
            final @NonNull Set<Break> breaks,
            final int permits) {
        return new BreakableSortedMap<>(sortedMap, breaks, DEFAULT_METHOD_STATUSES, permits,
                DEFAULT_SAFETY);
    }
}
