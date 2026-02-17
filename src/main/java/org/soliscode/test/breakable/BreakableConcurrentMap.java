package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.map.MapMethods;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/// A ConcurrentMap implementation that can be programmatically broken for testing purposes.
///
/// This class extends BreakableMap and implements the ConcurrentMap interface, providing
/// additional break constants for testing ConcurrentMap-specific functionality. It wraps
/// an existing ConcurrentMap and allows specific behaviors to be "broken" through the
/// Break mechanism while maintaining the thread safety guarantees of ConcurrentMap.
///
/// ## Overview
///
/// BreakableConcurrentMap provides comprehensive testing capabilities for code that works with
/// ConcurrentMap implementations. It supports all standard ConcurrentMap operations while enabling
/// controlled behavioral modifications through break constants. This is particularly useful for:
///
/// - **Testing ConcurrentMap Contract Compliance**: Verifying that code correctly handles atomic operations
/// - **Concurrency Testing**: Simulating race conditions and atomic operation failures
/// - **Error Condition Simulation**: Testing how code responds to atomic operation inconsistencies
/// - **Performance Testing**: Simulating slow or failing atomic operations
/// - **Robustness Testing**: Verifying code resilience against unexpected ConcurrentMap behaviors
///
/// ## ConcurrentMap-Specific Break Constants
///
/// This class provides break constants for testing atomic operations that are guaranteed by ConcurrentMap:
///
/// ### Atomic Operation Breaks
/// - **PUT_IF_ABSENT_IGNORES_EXISTING**: Forces putIfAbsent() to ignore existing mappings
/// - **PUT_IF_ABSENT_ALWAYS_RETURNS_NULL**: Forces putIfAbsent() to always return null regardless of existing value
/// - **PUT_IF_ABSENT_DOES_NOT_ADD_PAIR**: Forces putIfAbsent() to return expected value but not add_singleElement_returnsTrueAndUpdatesSize the mapping
/// - **REMOVE_TWO_ARG_IGNORES_VALUE**: Forces remove(key, value) to ignore value matching
/// - **REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE**: Forces remove(key, value) to always return false
/// - **REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR**: Forces remove(key, value) to return true but not remove the mapping
///
/// ### Replace Operation Breaks
/// - **REPLACE_THREE_ARG_IGNORES_OLD_VALUE**: Forces replace(key, oldValue, newValue) to ignore old value matching
/// - **REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE**: Forces replace(key, oldValue, newValue) to always return false
/// - **REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE**: Forces replace() to return true but not actually replace
/// - **REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL**: Forces replace(key, value) to always return null
/// - **REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE**: Forces replace() to return old value but not replace
///
/// ### Compute Operation Breaks
/// - **COMPUTE_IF_ABSENT_RACE_CONDITION**: Simulates race condition where multiple threads compute values
/// - **COMPUTE_IF_PRESENT_RACE_CONDITION**: Simulates race condition in computeIfPresent operations
/// - **COMPUTE_RACE_CONDITION**: Simulates general race condition in compute operations
/// - **MERGE_RACE_CONDITION**: Simulates race condition in merge operations
///
/// ## Usage Examples
///
/// ### Basic ConcurrentMap Testing
/// ```java
/// BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();
/// map.putIfAbsent("key1", 100);
/// map.replace("key1", 200);
///
/// // Normal concurrent map operations
/// Integer oldValue = map.replace("key1", 100, 300);
/// boolean removed = map.remove("key1", 300);
/// ```
///
/// ### ConcurrentMap Failure Testing
/// ```java
/// BreakableConcurrentMap<String, Integer> brokenMap = new BreakableConcurrentMap.Builder<String, Integer>()
///     .addBreak(PUT_IF_ABSENT_IGNORES_EXISTING)
///     .addBreak(REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE)
///     .build();
///
/// // Test atomic operation failures
/// // putIfAbsent() will ignore existing mappings due to break
/// // remove(key, value) will always return false due to break
/// ```
///
/// ### Builder Pattern with ConcurrentMap
/// ```java
/// BreakableConcurrentMap<String, String> map = new BreakableConcurrentMap.Builder<String, String>()
///     .addBreak(REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE)
///     .addBreak(COMPUTE_IF_ABSENT_RACE_CONDITION)
///     .doesNotPermitNullValues()
///     .build();
/// ```
///
/// ## Inheritance Hierarchy
///
/// ```
/// AbstractBreakable
///     ↓
/// BreakableMap<K,V>
///     ↓
/// BreakableConcurrentMap<K,V>
/// ```
///
/// This class inherits all Map functionality from BreakableMap while adding ConcurrentMap-specific
/// capabilities. All inherited break constants and behaviors remain available.
///
/// ## Thread Safety
///
/// BreakableConcurrentMap maintains the thread safety guarantees of the underlying ConcurrentMap
/// implementation for normal operations. However, break checking adds additional complexity that
/// may affect performance in highly concurrent scenarios. The break configuration itself is not
/// thread-safe and should be set before concurrent access begins.
///
/// ## Performance Considerations
///
/// ConcurrentMap operations delegate to the underlying ConcurrentMap implementation unless breaks
/// are applied. Break checking adds minimal overhead. The class maintains the performance
/// characteristics of the underlying ConcurrentMap for non-broken operations.
///
/// @param <K> the type of keys maintained by this concurrent map
/// @param <V> the type of mapped values
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableMap
/// @see ConcurrentMap
/// @see Break
public class BreakableConcurrentMap<K, V> extends BreakableMap<K, V> implements ConcurrentMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== putIfAbsent Method Breaks ==========

    /// Break constant that causes putIfAbsent() to ignore existing mappings.
    ///
    /// When this break is applied, the putIfAbsent() method will add_singleElement_returnsTrueAndUpdatesSize a new mapping
    /// even if a mapping for the key already exists, violating the atomic guarantee.
    ///
    /// **Affected Methods:**
    /// - {@link #putIfAbsent(Object, Object)} - Ignores existing mappings
    ///
    /// **Use Cases:**
    /// - Testing putIfAbsent() atomicity failure handling
    /// - Simulating race conditions in concurrent insertion
    /// - Testing duplicate key handling in concurrent scenarios
    public static final Break PUT_IF_ABSENT_IGNORES_EXISTING =
            new Break("PUT_IF_ABSENT_IGNORES_EXISTING");

    /// Break constant that causes putIfAbsent() to always return null.
    ///
    /// When this break is applied, the putIfAbsent() method will always return null
    /// regardless of whether a mapping already exists.
    ///
    /// **Affected Methods:**
    /// - {@link #putIfAbsent(Object, Object)} - Always returns null
    ///
    /// **Use Cases:**
    /// - Testing putIfAbsent() return value handling
    /// - Simulating incorrect atomic operation reporting
    /// - Testing code that depends on putIfAbsent() return values
    public static final Break PUT_IF_ABSENT_ALWAYS_RETURNS_NULL =
            new Break("PUT_IF_ABSENT_ALWAYS_RETURNS_NULL");

    /// Break constant that causes putIfAbsent() to return expected value but not add_singleElement_returnsTrueAndUpdatesSize the mapping.
    ///
    /// When this break is applied, the putIfAbsent() method will return the correct value
    /// but not actually add_singleElement_returnsTrueAndUpdatesSize the mapping if it was absent.
    ///
    /// **Affected Methods:**
    /// - {@link #putIfAbsent(Object, Object)} - Returns correct value but doesn't add_singleElement_returnsTrueAndUpdatesSize mapping
    ///
    /// **Use Cases:**
    /// - Testing silent putIfAbsent() operation failures
    /// - Simulating partial atomic operation completion
    /// - Testing map state consistency
    public static final Break PUT_IF_ABSENT_DOES_NOT_ADD_PAIR =
            new Break("PUT_IF_ABSENT_DOES_NOT_ADD_PAIR");

    // ========== remove(key, value) Method Breaks ==========

    /// Break constant that causes remove(key, value) to ignore value matching.
    ///
    /// When this break is applied, the remove() method will remove the mapping
    /// regardless of whether the current value matches the specified value.
    ///
    /// **Affected Methods:**
    /// - {@link #remove(Object, Object)} - Ignores value matching
    ///
    /// **Use Cases:**
    /// - Testing remove() atomicity failure handling
    /// - Simulating race conditions in concurrent removal
    /// - Testing value comparison logic in atomic operations
    public static final Break REMOVE_TWO_ARG_IGNORES_VALUE =
            new Break("REMOVE_TWO_ARG_IGNORES_VALUE");

    /// Break constant that causes remove(key, value) to always return false.
    ///
    /// When this break is applied, the remove() method will always return false
    /// regardless of whether the mapping exists with the specified value.
    ///
    /// **Affected Methods:**
    /// - {@link #remove(Object, Object)} - Always returns false
    ///
    /// **Use Cases:**
    /// - Testing remove() return value handling
    /// - Simulating remove operation failures
    /// - Testing removal confirmation logic
    public static final Break REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE =
            new Break("REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes remove(key, value) to return true but not remove the mapping.
    ///
    /// When this break is applied, the remove() method will return true as if
    /// the removal was successful but not actually remove the mapping.
    ///
    /// **Affected Methods:**
    /// - {@link #remove(Object, Object)} - Returns true but doesn't remove mapping
    ///
    /// **Use Cases:**
    /// - Testing silent remove() operation failures
    /// - Simulating partial atomic operation completion
    /// - Testing map state consistency after removal
    public static final Break REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR =
            new Break("REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR");

    // ========== replace(key, oldValue, newValue) Method Breaks ==========

    /// Break constant that causes replace(key, oldValue, newValue) to ignore old value matching.
    ///
    /// When this break is applied, the replace() method will replace the value
    /// regardless of whether the current value matches the specified old value.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object, Object)} - Ignores old value matching
    ///
    /// **Use Cases:**
    /// - Testing replace() atomicity failure handling
    /// - Simulating race conditions in concurrent replacement
    /// - Testing value comparison logic in atomic operations
    public static final Break REPLACE_THREE_ARG_IGNORES_OLD_VALUE =
            new Break("REPLACE_THREE_ARG_IGNORES_OLD_VALUE");

    /// Break constant that causes replace(key, oldValue, newValue) to always return false.
    ///
    /// When this break is applied, the replace() method will always return false
    /// regardless of whether the mapping exists with the specified old value.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object, Object)} - Always returns false
    ///
    /// **Use Cases:**
    /// - Testing replace() return value handling
    /// - Simulating replace operation failures
    /// - Testing replacement confirmation logic
    public static final Break REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE =
            new Break("REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes replace(key, oldValue, newValue) to return true but not replace.
    ///
    /// When this break is applied, the replace() method will return true as if
    /// the replacement was successful but not actually replace the value.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object, Object)} - Returns true but doesn't replace value
    ///
    /// **Use Cases:**
    /// - Testing silent replace() operation failures
    /// - Simulating partial atomic operation completion
    /// - Testing map state consistency after replacement
    public static final Break REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE =
            new Break("REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE");

    // ========== replace(key, value) Method Breaks ==========

    /// Break constant that causes replace(key, value) to always return null.
    ///
    /// When this break is applied, the replace() method will always return null
    /// regardless of whether a mapping exists for the key.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object)} - Always returns null
    ///
    /// **Use Cases:**
    /// - Testing replace() return value handling
    /// - Simulating replace operation result failures
    /// - Testing replacement result processing logic
    public static final Break REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL =
            new Break("REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL");

    /// Break constant that causes replace(key, value) to return old value but not replace.
    ///
    /// When this break is applied, the replace() method will return the old value
    /// but not actually replace it with the new value.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object)} - Returns old value but doesn't replace
    ///
    /// **Use Cases:**
    /// - Testing silent replace() operation failures
    /// - Simulating partial atomic operation completion
    /// - Testing map state consistency after replacement
    public static final Break REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE =
            new Break("REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE");

    // ========== Race Condition Simulation Breaks ==========

    /// Break constant that simulates race conditions in computeIfAbsent operations.
    ///
    /// When this break is applied, the computeIfAbsent() method may exhibit behavior
    /// consistent with race conditions where multiple threads attempt to compute values.
    ///
    /// **Affected Methods:**
    /// - {@link #computeIfAbsent(Object, Function)} - Simulates race condition
    ///
    /// **Use Cases:**
    /// - Testing computeIfAbsent() race condition handling
    /// - Simulating concurrent computation scenarios
    /// - Testing thread safety in concurrent map usage
    public static final Break COMPUTE_IF_ABSENT_RACE_CONDITION =
            new Break("COMPUTE_IF_ABSENT_RACE_CONDITION");

    /// Break constant that simulates race conditions in computeIfPresent operations.
    ///
    /// When this break is applied, the computeIfPresent() method may exhibit behavior
    /// consistent with race conditions where multiple threads attempt to compute values.
    ///
    /// **Affected Methods:**
    /// - {@link #computeIfPresent(Object, BiFunction)} - Simulates race condition
    ///
    /// **Use Cases:**
    /// - Testing computeIfPresent() race condition handling
    /// - Simulating concurrent computation scenarios
    /// - Testing thread safety in concurrent map usage
    public static final Break COMPUTE_IF_PRESENT_RACE_CONDITION =
            new Break("COMPUTE_IF_PRESENT_RACE_CONDITION");

    /// Break constant that simulates race conditions in compute operations.
    ///
    /// When this break is applied, the compute() method may exhibit behavior
    /// consistent with race conditions where multiple threads attempt to compute values.
    ///
    /// **Affected Methods:**
    /// - {@link #compute(Object, BiFunction)} - Simulates race condition
    ///
    /// **Use Cases:**
    /// - Testing compute() race condition handling
    /// - Simulating concurrent computation scenarios
    /// - Testing thread safety in concurrent map usage
    public static final Break COMPUTE_RACE_CONDITION =
            new Break("COMPUTE_RACE_CONDITION");

    /// Break constant that simulates race conditions in merge operations.
    ///
    /// When this break is applied, the merge() method may exhibit behavior
    /// consistent with race conditions where multiple threads attempt to merge values.
    ///
    /// **Affected Methods:**
    /// - {@link #merge(Object, Object, BiFunction)} - Simulates race condition
    ///
    /// **Use Cases:**
    /// - Testing merge() race condition handling
    /// - Simulating concurrent merge scenarios
    /// - Testing thread safety in concurrent map usage
    public static final Break MERGE_RACE_CONDITION =
            new Break("MERGE_RACE_CONDITION");

    // ========== Instance Fields ==========

    /// The underlying ConcurrentMap that this BreakableConcurrentMap wraps.
    /// All operations delegate to this map unless breaks are applied.
    ///
    /// This map is initialized during construction and provides the actual storage
    /// and concurrency control for the breakable implementation.
    ///
    /// @see ConcurrentMap
    private final @NonNull ConcurrentMap<K, V> concurrentMap;

    // ========== Constructors ==========

    /// Creates an empty BreakableConcurrentMap backed by a ConcurrentHashMap.
    ///
    /// This constructor creates a new BreakableConcurrentMap backed by an empty ConcurrentHashMap.
    /// The map will have default null policies and no breaks applied.
    ///
    /// **Default Configuration:**
    /// - Empty ConcurrentMap (ConcurrentHashMap implementation)
    /// - Permits null values: true
    /// - Does not permit null keys: false (ConcurrentHashMap limitation)
    /// - No breaks applied
    ///
    /// **Usage:**
    /// ```java
    /// BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap<>();
    /// map.putIfAbsent("key1", 100);
    /// map.replace("key1", 200);
    /// ```
    public BreakableConcurrentMap() {
        this(new ConcurrentHashMap<>(), new HashSet<>(), new HashMap<>(), DEFAULT_PERMITS);
    }

    /// Creates a BreakableConcurrentMap by copying another BreakableConcurrentMap.
    ///
    /// This constructor creates a new instance that shares the underlying map data
    /// and inherits all configuration from the source map.
    ///
    /// **Inherited Configuration:**
    /// - All break settings from source
    /// - Null key and value policies
    /// - Method support configuration
    /// - Underlying ConcurrentMap reference (shared, not copied)
    ///
    /// **Usage:**
    /// ```java
    /// BreakableConcurrentMap<String, Integer> original = new BreakableConcurrentMap<>();
    /// BreakableConcurrentMap<String, Integer> copy = new BreakableConcurrentMap<>(original);
    /// ```
    ///
    /// @param other the BreakableConcurrentMap to copy configuration from
    /// @throws NullPointerException if other is null
    public BreakableConcurrentMap(final @NonNull BreakableConcurrentMap<K, V> other) {
        this(new ConcurrentHashMap<>(other.concurrentMap), new HashSet<>(other.breaks()),
                new HashMap<>(other.methodStatuses()), DEFAULT_PERMITS);

    }

    /// Constructs a `BreakableConcurrentMap` instance that serves as a wrapper
    /// around the provided [ConcurrentMap], enabling support for breaking operations
    /// based on predefined conditions and method statuses. This implementation allows
    /// augmentation of concurrency support by applying controlled interruptions on specific
    /// map methods.
    ///
    /// @param concurrentMap The underlying [ConcurrentMap] implementation that
    ///                      this class will delegate its operations to. Must not be `null`.
    /// @param breaks A [Set] of [Break] conditions that define the operational
    ///               interruptions. Each `Break` influences the behavior of this map
    ///               based on predefined rules. Must not be `null`.
    /// @param methodStatuses A [Map] that associates [InterfaceMethod] definitions
    ///                       with their corresponding [MethodStatus]. This configuration
    ///                       determines the allowed operations and their statuses. Must not
    ///                       be `null`.
    /// @param permits The maximum number of active permits allowed for concurrent operations.
    ///                This parameter manages concurrency constraints and must be a non-negative
    ///                integer.
    protected BreakableConcurrentMap(
            final @NonNull ConcurrentMap<K, V> concurrentMap,
            final @NonNull Set<Break> breaks,
            final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int permits) {
        super(this.concurrentMap = concurrentMap, breaks, methodStatuses, permits, true);
    }

    // ========== ConcurrentMap Interface Implementation ==========
    // Note: ConcurrentMap doesn't add_singleElement_returnsTrueAndUpdatesSize new methods beyond Map since Java 8,
    // but provides stronger atomicity guarantees

    /// Associates the specified value with the specified key in this map if it is not already present.
    ///
    /// This method provides an atomic implementation of `putIfAbsent` as specified by the
    /// [ConcurrentMap] interface. The operation is atomic with respect to the underlying map.
    ///
    /// ### Behavioral Modifications (Breaks)
    ///
    /// The standard atomic behavior can be intentionally broken using the following constants:
    /// - {@link #PUT_IF_ABSENT_IGNORES_EXISTING}: Bypasses the "if absent" check and always puts the value.
    /// - {@link #PUT_IF_ABSENT_ALWAYS_RETURNS_NULL}: Performs the operation but always reports success via `null` return.
    /// - {@link #PUT_IF_ABSENT_DOES_NOT_ADD_PAIR}: Checks the state and returns what it would have done, but performs no write.
    ///
    /// @param key key with which the specified value is to be associated
    /// @param value value to be associated with the specified key
    /// @return the previous value associated with the specified key, or `null` if there was no mapping for the key.
    ///         (A `null` return can also indicate that the map previously associated `null` with the key,
    ///         if the implementation supports null values.)
    /// @throws UnsupportedOperationException if the `PUT_IF_ABSENT` operation is marked as unsupported
    /// @throws ClassCastException if the key or value is of an inappropriate type for this map
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys
    /// @throws IllegalArgumentException if some property of the specified key or value prevents it from being stored in this map
    /// @see java.util.concurrent.ConcurrentMap#putIfAbsent(Object, Object)
    /// @since 1.0.0
    @Override
    public @Nullable V putIfAbsent(final @NonNull K key, final V value) {
        checkMethodSupport(MapMethods.PUT_IF_ABSENT);

        if (hasBreak(PUT_IF_ABSENT_ALWAYS_RETURNS_NULL)) {
            // Still perform the operation but return null
            concurrentMap.putIfAbsent(key, value);
            return null;
        }

        if (hasBreak(PUT_IF_ABSENT_DOES_NOT_ADD_PAIR)) {
            // Return what the operation would return but don't actually perform it
            return concurrentMap.get(key);
        }

        if (hasBreak(PUT_IF_ABSENT_IGNORES_EXISTING)) {
            // Always put, ignoring existing value
            return concurrentMap.put(key, value);
        }

        // Delegate to parent for other potential breaks
        return super.putIfAbsent(key, value);
    }

    /// Removes the entry for a key only if currently mapped to the given value.
    ///
    /// This method provides an atomic implementation of `remove` as specified by the
    /// [ConcurrentMap] interface.
    ///
    /// ### Behavioral Modifications (Breaks)
    ///
    /// The standard atomic behavior can be intentionally broken using the following constants:
    /// - {@link #REMOVE_TWO_ARG_IGNORES_VALUE}: Removes the key regardless of its current value.
    /// - {@link #REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE}: Never removes anything and always returns `false`.
    /// - {@link #REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR}: Simulates the check and returns the result, but performs no removal.
    ///
    /// @param key key with which the specified value is associated
    /// @param value value expected to be associated with the specified key
    /// @return `true` if the value was removed
    /// @throws UnsupportedOperationException if the `REMOVE_TWO_ARG` operation is marked as unsupported
    /// @throws ClassCastException if the key or value is of an inappropriate type for this map
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys
    /// @see ConcurrentMap#remove(Object, Object)
    /// @since 1.0.0
    @Override
    public boolean remove(final @NonNull Object key, final Object value) {
        checkMethodSupport(MapMethods.REMOVE_TWO_ARG);

        if (hasBreak(REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        if (hasBreak(REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR)) {
            // Return what the operation would return but don't actually perform it
            return Objects.equals(concurrentMap.get(key), value);
        }

        if (hasBreak(REMOVE_TWO_ARG_IGNORES_VALUE)) {
            // Remove regardless of value matching
            return concurrentMap.remove(key) != null;
        }

        // Delegate to parent for other potential breaks
        return super.remove(key, value);
    }

    /// Replaces the entry for a key only if currently mapped to the given value.
    ///
    /// This method provides an atomic implementation of `replace` as specified by the
    /// [ConcurrentMap] interface.
    ///
    /// ### Behavioral Modifications (Breaks)
    ///
    /// The standard atomic behavior can be intentionally broken using the following constants:
    /// - {@link #REPLACE_THREE_ARG_IGNORES_OLD_VALUE}: Replaces the value even if the current value doesn't match `oldValue`.
    /// - {@link #REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE}: Never replaces anything and always returns `false`.
    /// - {@link #REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE}: Simulates the check and returns the result, but performs no replacement.
    ///
    /// @param key key with which the specified value is associated
    /// @param oldValue value expected to be associated with the specified key
    /// @param newValue value to be associated with the specified key
    /// @return `true` if the value was replaced
    /// @throws UnsupportedOperationException if the `REPLACE_THREE_ARG` operation is marked as unsupported
    /// @throws ClassCastException if the class of the specified key or value prevents it from being stored in this map
    /// @throws NullPointerException if a specified key or value is null, and this map does not permit null keys or values
    /// @throws IllegalArgumentException if some property of a specified key or value prevents it from being stored in this map
    /// @see ConcurrentMap#replace(Object, Object, Object)
    /// @since 1.0.0
    @Override
    public boolean replace(final @NonNull K key, final @NonNull V oldValue, final @NonNull V newValue) {
        checkMethodSupport(MapMethods.REPLACE_THREE_ARG);

        if (hasBreak(REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        if (hasBreak(REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE)) {
            // Return what the operation would return but don't actually perform it
            return Objects.equals(concurrentMap.get(key), oldValue);
        }

        if (hasBreak(REPLACE_THREE_ARG_IGNORES_OLD_VALUE)) {
            // Replace regardless of old value matching
            if (concurrentMap.containsKey(key)) {
                concurrentMap.put(key, newValue);
                return true;
            }
            return false;
        }

        // Delegate to parent for other potential breaks
        return super.replace(key, oldValue, newValue);
    }

    /// Replaces the entry for a key only if it is currently mapped to some value.
    ///
    /// This method provides an atomic implementation of `replace` as specified by the
    /// [ConcurrentMap] interface.
    ///
    /// ### Behavioral Modifications (Breaks)
    ///
    /// The standard atomic behavior can be intentionally broken using the following constants:
    /// - {@link #REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL}: Performs the replacement but always returns `null` as if the key was missing.
    /// - {@link #REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE}: Returns the current value but performs no replacement.
    ///
    /// @param key key with which the specified value is associated
    /// @param value value to be associated with the specified key
    /// @return the previous value associated with the specified key, or `null` if there was no mapping for the key.
    /// @throws UnsupportedOperationException if the `REPLACE_TWO_ARG` operation is marked as unsupported
    /// @throws ClassCastException if the class of the specified key or value prevents it from being stored in this map
    /// @throws NullPointerException if the specified key or value is null, and this map does not permit null keys or values
    /// @throws IllegalArgumentException if some property of the specified key or value prevents it from being stored in this map
    /// @see ConcurrentMap#replace(Object, Object)
    /// @since 1.0.0
    @Override
    public @Nullable V replace(final @NonNull K key, final @NonNull V value) {
        checkMethodSupport(MapMethods.REPLACE_TWO_ARG);

        if (hasBreak(REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL)) {
            // Still perform the operation but return null
            concurrentMap.replace(key, value);
            return null;
        }

        if (hasBreak(REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE)) {
            // Return what the operation would return but don't actually perform it
            return concurrentMap.get(key);
        }

        // Delegate to parent for other potential breaks
        return super.replace(key, value);
    }

    /// Attempts to compute a mapping for the specified key and its current mapped value,
    /// or null if there is no current mapping.
    ///
    /// This method provides an atomic implementation of `compute` as specified by the
    /// [ConcurrentMap] interface.
    ///
    /// ### Behavioral Modifications (Breaks)
    ///
    /// The standard atomic behavior can be intentionally broken using the following constants:
    /// - {@link #COMPUTE_RACE_CONDITION}: Simulates race condition behavior by performing the operation twice.
    ///
    /// @param key key with which the specified value is to be associated
    /// @param remappingFunction the function to compute a value
    /// @return the new value associated with the specified key, or null if none
    /// @throws UnsupportedOperationException if the `COMPUTE` operation is marked as unsupported
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys,
    ///         or if the remappingFunction is null
    /// @see ConcurrentMap#compute(Object, BiFunction)
    /// @since 1.0.0
    @Override
    public @Nullable V compute(final K key,
                               final @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        checkMethodSupport(MapMethods.COMPUTE);

        if (hasBreak(COMPUTE_RACE_CONDITION)) {
            // Simulate race condition by performing operation twice
            V result = super.compute(key, remappingFunction);
            super.compute(key, remappingFunction);
            return result;
        }

        // Delegate to parent for full break support
        return super.compute(key, remappingFunction);
    }

    /// If the specified key is not already associated with a value, attempts to compute its value
    /// and enters it into the map.
    ///
    /// This method provides an atomic implementation of `computeIfAbsent` as specified by the
    /// [ConcurrentMap] interface.
    ///
    /// ### Behavioral Modifications (Breaks)
    ///
    /// The standard atomic behavior can be intentionally broken using the following constants:
    /// - {@link #COMPUTE_IF_ABSENT_RACE_CONDITION}: Simulates race condition behavior by checking existence separately.
    ///
    /// @param key key with which the specified value is to be associated
    /// @param mappingFunction the function to compute a value
    /// @return the current (existing or computed) value associated with the specified key,
    ///         or null if the computed value is null
    /// @throws UnsupportedOperationException if the `COMPUTE_IF_ABSENT` operation is marked as unsupported
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys,
    ///         or if the mappingFunction is null
    /// @see ConcurrentMap#computeIfAbsent(Object, Function)
    /// @since 1.0.0
    @Override
    public @Nullable V computeIfAbsent(final K key, final @NonNull Function<? super K, ? extends V> mappingFunction) {
        checkMethodSupport(MapMethods.COMPUTE_IF_ABSENT);

        if (hasBreak(COMPUTE_IF_ABSENT_RACE_CONDITION)) {
            // Simulate race condition by checking existence separately
            if (!concurrentMap.containsKey(key)) {
                // Simulate delay that could allow another thread to add_singleElement_returnsTrueAndUpdatesSize the key
                V value = mappingFunction.apply(key);
                concurrentMap.putIfAbsent(key, value);
                return value;
            }
            return concurrentMap.get(key);
        }
        // Delegate to parent for full break support
        return super.computeIfAbsent(key, mappingFunction);
    }

    /// If the value for the specified key is present, attempts to compute a new mapping
    /// given the key and its current mapped value.
    ///
    /// This method provides an atomic implementation of `computeIfPresent` as specified by the
    /// [ConcurrentMap] interface.
    ///
    /// ### Behavioral Modifications (Breaks)
    ///
    /// The standard atomic behavior can be intentionally broken using the following constants:
    /// - {@link #COMPUTE_IF_PRESENT_RACE_CONDITION}: Simulates race condition behavior by checking existence separately.
    ///
    /// @param key key with which the specified value is to be associated
    /// @param remappingFunction the function to compute a value
    /// @return the new value associated with the specified key, or null if none
    /// @throws UnsupportedOperationException if the `COMPUTE_IF_PRESENT` operation is marked as unsupported
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys,
    ///         or if the remappingFunction is null
    /// @see ConcurrentMap#computeIfPresent(Object, BiFunction)
    /// @since 1.0.0
    @Override
    public @Nullable V computeIfPresent(final K key,
                                    final @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        checkMethodSupport(MapMethods.COMPUTE_IF_PRESENT);

        if (hasBreak(COMPUTE_IF_PRESENT_RACE_CONDITION)) {
            // Simulate race condition by checking existence separately
            V currentValue = concurrentMap.get(key);
            if (currentValue != null) {
                // Simulate delay that could allow another thread to remove the key
                V newValue = remappingFunction.apply(key, currentValue);
                if (newValue != null) {
                    concurrentMap.put(key, newValue);
                    return newValue;
                } else {
                    concurrentMap.remove(key);
                    return null;
                }
            }
            return null;
        }

        // Delegate to parent for full break support
        return super.computeIfPresent(key, remappingFunction);
    }

    /// If the specified key is not already associated with a value or is associated with null,
    /// associates it with the given non-null value.
    ///
    /// This method provides an atomic implementation of `merge` as specified by the
    /// [ConcurrentMap] interface.
    ///
    /// ### Behavioral Modifications (Breaks)
    ///
    /// The standard atomic behavior can be intentionally broken using the following constants:
    /// - {@link #MERGE_RACE_CONDITION}: Simulates race condition behavior by performing operations separately.
    ///
    /// @param key key with which the resulting value is to be associated
    /// @param value the non-null value to be merged with the existing value
    ///        associated with the key or, if no existing value or a null value
    ///        is associated with the key, to be associated with the key
    /// @param remappingFunction the function to recompute a value if present
    /// @return the new value associated with the specified key, or null if no value is associated with the key
    /// @throws UnsupportedOperationException if the `MERGE` operation is marked as unsupported
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys,
    ///         or if the value or remappingFunction is null
    /// @see ConcurrentMap#merge(Object, Object, BiFunction)
    /// @since 1.0.0
    @Override
    public @Nullable V merge(final K key, final @NonNull V value,
                             final @NonNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        checkMethodSupport(MapMethods.MERGE);

        if (hasBreak(MERGE_RACE_CONDITION)) {
            // Simulate race condition by performing operations separately
            V currentValue = concurrentMap.get(key);
            if (currentValue == null) {
                return concurrentMap.putIfAbsent(key, value);
            } else {
                V newValue = remappingFunction.apply(currentValue, value);
                if (newValue != null) {
                    concurrentMap.put(key, newValue);
                    return newValue;
                } else {
                    concurrentMap.remove(key);
                    return null;
                }
            }
        }

        // Delegate to parent for full break support
        return super.merge(key, value, remappingFunction);
    }

    // ========== Builder Class ==========

    /// Builder class for creating BreakableConcurrentMap instances with fluent configuration.
    ///
    /// This builder extends BreakableMap.Builder and provides additional configuration
    /// options specific to ConcurrentMap functionality. It allows setting up a
    /// [BreakableConcurrentMap] with specific breaks and method support statuses.
    ///
    /// @param <K> the type of keys maintained by the built concurrent map
    /// @param <V> the type of mapped values
    /// @see BreakableConcurrentMap
    /// @since 1.0.0
    public static class Builder<K, V> extends BreakableMap.Builder<K, V> {

        /// Creates a new Builder with default configuration.
        ///
        /// The builder starts with:
        /// - An empty element set
        /// - No breaks
        /// - All methods supported
        /// - Default null permits
        public Builder() {
            super();
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
        /// @param map the map whose elements are to be placed in the builder
        /// @throws NullPointerException if map is null
        public Builder(final @NonNull Map<K, V> map) {
            super(map);
        }

        /// {@inheritDoc}
        @Override
        public @NonNull Builder<K, V> self() {
            return this;
        }

        /// {@inheritDoc}
        @Override
        public @NonNull Builder<K, V> copy() {
            return new Builder<>(this);
        }

        /// Builds a new BreakableConcurrentMap instance with the configured settings.
        @Override
        public @NonNull BreakableConcurrentMap<K, V> build() {
            return new BreakableConcurrentMap<>(new ConcurrentHashMap<>(elements()), breaks(), methodStatuses(),
                    permits());
        }
    }

    // ========== Static Factory Methods ==========

    /// Creates a BreakableConcurrentMap that wraps the specified ConcurrentMap with the given breaks.
    ///
    /// This factory method provides a convenient way to create a breakable wrapper around
    /// an existing concurrent map instance with a set of predefined breaks.
    ///
    /// @param <K> the type of keys maintained by the map
    /// @param <V> the type of mapped values
    /// @param concurrentMap the ConcurrentMap to wrap
    /// @param breaks the set of breaks to apply to the map
    /// @return a new BreakableConcurrentMap wrapping the specified map
    /// @throws NullPointerException if concurrentMap or breaks is null
    /// @see #BreakableConcurrentMap(ConcurrentMap, Set, Map, int)
    /// @since 1.0.0
    public static <K, V> @NonNull BreakableConcurrentMap<K, V> wrap(
            final @NonNull ConcurrentMap<K, V> concurrentMap,
            final @NonNull Set<Break> breaks) {
        return new BreakableConcurrentMap<>(concurrentMap, breaks, new HashMap<>(), DEFAULT_PERMITS);
    }
}
