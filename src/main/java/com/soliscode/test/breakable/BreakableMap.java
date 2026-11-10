package com.soliscode.test.breakable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.contract.support.MapProviderSupport;
import com.soliscode.test.provider.MapProvider;
import com.soliscode.test.provider.ObjectProvider;

import static com.soliscode.test.util.IterableTestUtils.copy;
import static com.soliscode.test.util.IterableTestUtils.first;
import static com.soliscode.test.util.IterableTestUtils.last;
import static com.soliscode.test.util.IterableTestUtils.skipFirstIterator;
import static com.soliscode.test.util.IterableTestUtils.skipLastIterator;

/// A `Map` implementation that can be programmatically broken for testing purposes.
///
/// This class wraps an existing `Map` and allows specific behaviors to be "broken" through the application of `Break`
/// constants. Each break represents a specific way that a `Map` method can fail or behave incorrectly. The breaks
/// include:
///
/// - **Size reporting errors**: always zero, off by one
/// - **Key/value containment check failures**: always false, opposite value
/// - **Get/put operation failures**: always returns null, does not add pair
/// - **Iteration and view collection failures**: skips first pair, returns empty set
/// - **Java 8+ functional method failures**: `compute`, `merge`, `forEach` failures
/// - **Removal operation failures**: does not remove key, always returns null
/// - **Bulk operation failures**: `putAll`, `clear` failures
///
/// ## Usage Example
///
/// ```java
/// // Create a map that never finds keys
/// Map<String, String> brokenMap = new BreakableMap.Builder<String, String>()
///     .addBreak(BreakableMap.CONTAINS_KEY_ALWAYS_RETURNS_FALSE)
///     .addBreak(BreakableMap.GET_ALWAYS_RETURNS_NULL)
///     .build();
///
/// brokenMap.put("existing", "value");
/// assertFalse(brokenMap.containsKey("existing")); // False due to break
/// assertNull(brokenMap.get("existing")); // Null due to break
/// ```
///
/// ## Thread Safety
///
/// This class is not thread-safe. External synchronization is required for concurrent access. For concurrent use, wrap
/// with {@link java.util.Collections#synchronizedMap(Map)} or use `BreakableConcurrentMap`.
///
/// @param <K> the type of keys maintained by this map
/// @param <V> the type of mapped values
/// @since 1.0
/// @see Break
/// @see java.util.Map
/// @see java.util.HashMap
/// @see BreakableConcurrentMap
public class BreakableMap<K, V> extends AbstractBreakable implements Map<K, V>, Cloneable, Serializable {

    // ========== clear() method breaks ==========

    /// Break constant that causes `clear()` to not remove any pairs from the map.
    ///
    /// With this break, `clear()` will return normally but the map will still contain all of its original entries.
    ///
    /// **Affected Methods:**
    /// - {@link #clear()} - Does not remove entries
    ///
    /// @since 1.0
    public static final Break CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS =
            new Break("clear() does not remove any pairs");

    /// Break constant that causes `clear()` to not remove the first pair from the map.
    ///
    /// With this break, `clear()` will remove all but the first entry encountered during iteration.
    ///
    /// **Affected Methods:**
    /// - {@link #clear()} - Leaves the first entry
    ///
    /// @since 1.0
    public static final Break CLEAR_DOES_NOT_REMOVE_FIRST_PAIR =
            new Break("clear() does not remove the first pair");

    /// Break constant that causes `clear()` to not remove the last pair from the map.
    ///
    /// With this break, `clear()` will remove all but the last entry encountered during iteration.
    ///
    /// **Affected Methods:**
    /// - {@link #clear()} - Leaves the last entry
    ///
    /// @since 1.0
    public static final Break CLEAR_DOES_NOT_REMOVE_LAST_PAIR =
            new Break("clear() does not remove the last pair");

    // ========== compute(Object, Function) method breaks =========

    /// Break constant that causes `compute(Object, Function)` to not compute the value.
    ///
    /// With this break, `compute` will not invoke the remapping function and will not modify the map.
    ///
    /// **Affected Methods:**
    /// - {@link #compute(Object, BiFunction)} - Does not compute value
    ///
    /// @since 1.0
    public static final Break COMPUTE_DOES_NOT_COMPUTE_VALUE =
            new Break("compute(Object, Function) does not compute value");

    /// Break constant that causes `compute(Object, Function)` to always return `null`.
    ///
    /// With this break, `compute` will return `null` regardless of the actual computation result or map state.
    ///
    /// **Affected Methods:**
    /// - {@link #compute(Object, BiFunction)} - Returns `null`
    ///
    /// @since 1.0
    public static final Break COMPUTE_ALWAYS_RETURNS_NULL =
            new Break("compute(Object, Function) always returns null");

    // ========== computeIfAbsent(Object, Function) method breaks =========

    /// Break constant that causes `computeIfAbsent(Object, Function)` to never compute the value.
    ///
    /// With this break, `computeIfAbsent` will not invoke the mapping function even if the key is missing.
    ///
    /// **Affected Methods:**
    /// - {@link #computeIfAbsent(Object, Function)} - Never computes value
    ///
    /// @since 1.0
    public static final Break COMPUTE_IF_ABSENT_NEVER_COMPUTES_VALUE =
            new Break("computeIfAbsent(Object, Function) never computes the value");

    /// Break constant that causes `computeIfAbsent(Object, Function)` to always compute the value.
    ///
    /// With this break, `computeIfAbsent` will invoke the mapping function even if the key is already present.
    ///
    /// **Affected Methods:**
    /// - {@link #computeIfAbsent(Object, Function)} - Always computes value
    ///
    /// @since 1.0
    public static final Break COMPUTE_IF_ABSENT_ALWAYS_COMPUTES_VALUE =
            new Break("computeIfAbsent(Object, Function) always computes value");

    /// Break constant that causes `computeIfAbsent(Object, Function)` to always return `null`.
    ///
    /// With this break, `computeIfAbsent` will return `null` whether a value was computed or already existed.
    ///
    /// **Affected Methods:**
    /// - {@link #computeIfAbsent(Object, Function)} - Returns `null`
    ///
    /// @since 1.0
    public static final Break COMPUTE_IF_ABSENT_ALWAYS_RETURNS_NULL =
            new Break("computeIfAbsent(Object, Function) always returns null");

    // ========== computeIfPresent(Object, Function) method breaks =========

    /// Break constant that causes `computeIfPresent(Object, Function)` to never compute the value.
    ///
    /// With this break, `computeIfPresent` will not invoke the remapping function even if the key is present.
    ///
    /// **Affected Methods:**
    /// - {@link #computeIfPresent(Object, BiFunction)} - Does not compute value
    ///
    /// @since 1.0
    public static final Break COMPUTE_IF_PRESENT_NEVER_COMPUTES_VALUE =
            new Break("computeIfPresent(Object, Function) does not compute value");

    /// Break constant that causes `computeIfPresent(Object, Function)` to always compute the value.
    ///
    /// With this break, `computeIfPresent` will invoke the remapping function even if the key is missing.
    ///
    /// **Affected Methods:**
    /// - {@link #computeIfPresent(Object, BiFunction)} - Always computes value
    ///
    /// @since 1.0
    public static final Break COMPUTE_IF_PRESENT_ALWAYS_COMPUTES_VALUE =
            new Break("computeIfPresent(Object, Function) always computes value");

    /// Break constant that causes `computeIfPresent(Object, Function)` to always return `null`.
    ///
    /// With this break, `computeIfPresent` will return `null` regardless of the computation result or map state.
    ///
    /// **Affected Methods:**
    /// - {@link #computeIfPresent(Object, BiFunction)} - Returns `null`
    ///
    /// @since 1.0
    public static final Break COMPUTE_IF_PRESENT_ALWAYS_RETURNS_NULL =
            new Break("computeIfPresent(Object, Function) always returns null");

    // ========== containsKey(Object) method breaks ==========

    /// Break constant that causes `containsKey(Object)` to always return `true`.
    ///
    /// **Affected Methods:**
    /// - {@link #containsKey(Object)} - Returns `true`
    ///
    /// @since 1.0
    public static final Break CONTAINS_KEY_ALWAYS_RETURNS_TRUE =
            new Break("containsKey(Object) method always returns true");

    /// Break constant that causes `containsKey(Object)` to always return `false`.
    ///
    /// **Affected Methods:**
    /// - {@link #containsKey(Object)} - Returns `false`
    ///
    /// @since 1.0
    public static final Break CONTAINS_KEY_ALWAYS_RETURNS_FALSE =
            new Break("containsKey(Object) method always returns false");

    /// Break constant that causes `containsKey(Object)` to return the opposite value.
    ///
    /// **Affected Methods:**
    /// - {@link #containsKey(Object)} - Returns opposite of actual containment
    ///
    /// @since 1.0
    public static final Break CONTAINS_KEY_RETURNS_OPPOSITE_VALUE =
            new Break("containsKey(Object) method always returns the opposite value");

    // ========== containsValue(Object) method breaks ==========

    /// Break constant that causes `containsValue(Object)` to always return `true`.
    ///
    /// **Affected Methods:**
    /// - {@link #containsValue(Object)} - Returns `true`
    ///
    /// @since 1.0
    public static final Break CONTAINS_VALUE_ALWAYS_RETURNS_TRUE =
            new Break("containsValue(Object) method always returns true");

    /// Break constant that causes `containsValue(Object)` to always return `false`.
    ///
    /// **Affected Methods:**
    /// - {@link #containsValue(Object)} - Returns `false`
    ///
    /// @since 1.0
    public static final Break CONTAINS_VALUE_ALWAYS_RETURNS_FALSE =
            new Break("containsValue(Object) method always returns false");

    /// Break constant that causes `containsValue(Object)` to return the opposite value.
    ///
    /// **Affected Methods:**
    /// - {@link #containsValue(Object)} - Returns opposite of actual containment
    ///
    /// @since 1.0
    public static final Break CONTAINS_VALUE_RETURNS_OPPOSITE_VALUE =
            new Break("containsValue(Object) method always returns the opposite value");

    // ========== entrySet() method breaks ==========

    /// Break constant that causes `entrySet()` to always return an empty set.
    ///
    /// **Affected Methods:**
    /// - {@link #entrySet()} - Returns empty set
    ///
    /// @since 1.0
    public static final Break ENTRY_SET_RETURNS_EMPTY_SET =
            new Break("entrySet() always returns an empty set");

    /// Break constant that causes `entrySet()` to return `null` when the map is empty.
    ///
    /// **Affected Methods:**
    /// - {@link #entrySet()} - Returns `null` if empty
    ///
    /// @since 1.0
    public static final Break ENTRY_SET_RETURNS_NULL_WHEN_EMPTY =
            new Break("entrySet() returns null when empty");

    // ========== forEach(BiConsumer) method breaks ==========

    /// Break constant that causes `forEach(BiConsumer)` to not call the action on any elements.
    ///
    /// **Affected Methods:**
    /// - {@link #forEach(BiConsumer)} - Action never executed
    ///
    /// @since 1.0
    public static final Break FOR_EACH_DOES_NOT_CALL_ACTION =
            new Break("forEach(BiConsumer) does not execute any action");

    /// Break constant that causes `forEach(BiConsumer)` to skip the first pair encountered.
    ///
    /// **Affected Methods:**
    /// - {@link #forEach(BiConsumer)} - Skips first entry
    ///
    /// @since 1.0
    public static final Break FOR_EACH_SKIPS_FIRST_PAIR =
            new Break("forEach(BiConsumer) skips the first pair");

    /// Break constant that causes `forEach(BiConsumer)` to skip the last pair encountered.
    ///
    /// **Affected Methods:**
    /// - {@link #forEach(BiConsumer)} - Skips last entry
    ///
    /// @since 1.0
    public static final Break FOR_EACH_SKIPS_LAST_PAIR =
            new Break("forEach(BiConsumer) skips the last pair");

    // ========== get(Object) method breaks ==========

    /// Break constant that causes `get(Object)` to always return `null`.
    ///
    /// **Affected Methods:**
    /// - {@link #get(Object)} - Returns `null`
    ///
    /// @since 1.0
    public static final Break GET_ALWAYS_RETURNS_NULL =
            new Break("get(Object) method always returns null");

    /// Break constant that causes `get(Object)` to fail for the first key.
    ///
    /// The method returns `null` for the first key encountered during iteration, even if it's present in the map.
    ///
    /// **Affected Methods:**
    /// - {@link #get(Object)} - Returns `null` for first key
    ///
    /// @since 1.0
    public static final Break GET_FAILS_FOR_FIRST_KEY =
            new Break("get(Object) fails for the first key");

    /// Break constant that causes `get(Object)` to fail for the last key.
    ///
    /// The method returns `null` for the last key encountered during iteration, even if it's present in the map.
    ///
    /// **Affected Methods:**
    /// - {@link #get(Object)} - Returns `null` for last key
    ///
    /// @since 1.0
    public static final Break GET_FAILS_FOR_LAST_KEY =
            new Break("get(Object) fails for the last key");

    // ========== getOrDefault(Object,Object) method breaks ==========

    /// Break constant that causes `getOrDefault(Object, Object)` to always return `null`.
    ///
    /// **Affected Methods:**
    /// - {@link #getOrDefault(Object, Object)} - Returns `null`
    ///
    /// @since 1.0
    public static final Break GET_OR_DEFAULT_ALWAYS_RETURNS_NULL =
            new Break("getOrDefault(Object,Object) method always returns null");

    /// Break constant that causes `getOrDefault(Object, Object)` to fail for the first key.
    ///
    /// The method returns the default value for the first key encountered during iteration, though it is present.
    ///
    /// **Affected Methods:**
    /// - {@link #getOrDefault(Object, Object)} - Returns default value for first key
    ///
    /// @since 1.0
    public static final Break GET_OR_DEFAULT_FAILS_FOR_FIRST_KEY =
            new Break("getOrDefault(Object,Object) fails for the first key");

    /// Break constant that causes `getOrDefault(Object, Object)` to fail for the last key.
    ///
    /// The method returns the default value for the last key encountered during iteration, though it is present.
    ///
    /// **Affected Methods:**
    /// - {@link #getOrDefault(Object, Object)} - Returns default value for last key
    ///
    /// @since 1.0
    public static final Break GET_OR_DEFAULT_FAILS_FOR_LAST_KEY =
            new Break("getOrDefault(Object,Object) fails for the last key");

    // ========== isEmpty method breaks ==========

    /// Break constant that causes `isEmpty()` to always return `true`.
    ///
    /// **Affected Methods:**
    /// - {@link #isEmpty()} - Returns `true`
    ///
    /// @since 1.0
    public static final Break IS_EMPTY_ALWAYS_RETURNS_TRUE =
            new Break("isEmpty() method always returns true");

    /// Break constant that causes `isEmpty()` to always return `false`.
    ///
    /// **Affected Methods:**
    /// - {@link #isEmpty()} - Returns `false`
    ///
    /// @since 1.0
    public static final Break IS_EMPTY_ALWAYS_RETURNS_FALSE =
            new Break("isEmpty() method always returns false");

    /// Break constant that causes `isEmpty()` to return the opposite value.
    ///
    /// **Affected Methods:**
    /// - {@link #isEmpty()} - Returns opposite of actual emptiness
    ///
    /// @since 1.0
    public static final Break IS_EMPTY_RETURNS_OPPOSITE_VALUE =
            new Break("isEmpty() method always returns the opposite value");

    // ========== keySet() method breaks ==========

    /// Break constant that causes `keySet()` to always return an empty set.
    ///
    /// **Affected Methods:**
    /// - {@link #keySet()} - Returns empty set
    ///
    /// @since 1.0
    public static final Break KEY_SET_RETURNS_EMPTY_SET =
            new Break("keySet() always returns an empty set");

    /// Break constant that causes `keySet()` to return `null` when the map is empty.
    ///
    /// **Affected Methods:**
    /// - {@link #keySet()} - Returns `null` if empty
    ///
    /// @since 1.0
    public static final Break KEY_SET_RETURNS_NULL_WHEN_EMPTY =
            new Break("keySet() returns null when empty");

    // ========== merge(Object, Object, BiFunction) method breaks =========

    /// Break constant that causes `merge(Object, Object, BiFunction)` to not merge values.
    ///
    /// With this break, `merge` will return the existing value for the key without invoking the remapping function.
    ///
    /// **Affected Methods:**
    /// - {@link #merge(Object, Object, BiFunction)} - Does not merge values
    ///
    /// @since 1.0
    public static final Break MERGE_DOES_NOT_MERGE_THE_VALUES =
            new Break("merge(Object, Object, BiFunction) does not merge the values");

    /// Break constant that causes `merge(Object, Object, BiFunction)` to always return `null`.
    ///
    /// **Affected Methods:**
    /// - {@link #merge(Object, Object, BiFunction)} - Returns `null`
    ///
    /// @since 1.0
    public static final Break MERGE_ALWAYS_RETURNS_NULL =
            new Break("merge(Object, Object, BiFunction) always returns null");

    // ========== put(Object, Object) method breaks ==========

    /// Break constant that causes `put(K, V)` to not add the pair to the map.
    ///
    /// **Affected Methods:**
    /// - {@link #put(Object, Object)} - Does not add entry
    ///
    /// @since 1.0
    public static final Break PUT_DOES_NOT_ADD_PAIR =
            new Break("put(Object,Object) does not add the pair");

    /// Break constant that causes `put(K, V)` to always return `null`.
    ///
    /// **Affected Methods:**
    /// - {@link #put(Object, Object)} - Returns `null`
    ///
    /// @since 1.0
    public static final Break PUT_ALWAYS_RETURNS_NULL =
            new Break("put(Object,Object) always returns null");

    // ========== putAll(Map) method breaks ==========

    /// Break constant that causes `putAll(Map)` to not add any pairs to the map.
    ///
    /// **Affected Methods:**
    /// - {@link #putAll(Map)} - Does not add entries
    ///
    /// @since 1.0
    public static final Break PUT_ALL_DOES_NOT_ADD_ANY_PAIRS =
            new Break("putAll(Map) does not add any pairs");

    // ========== putIfAbsent(Object,Object) method breaks ==========

    /// Break constant that causes `putIfAbsent(K, V)` to not put the value.
    ///
    /// **Affected Methods:**
    /// - {@link #putIfAbsent(Object, Object)} - Does not put value
    ///
    /// @since 1.0
    public static final Break PUT_IF_ABSENT_DOES_NOT_PUT_VALUE =
            new Break("putIfAbsent(Object,Object) does not replace value");

    /// Break constant that causes `putIfAbsent(K, V)` to always return `null`.
    ///
    /// **Affected Methods:**
    /// - {@link #putIfAbsent(Object, Object)} - Returns `null`
    ///
    /// @since 1.0
    public static final Break PUT_IF_ABSENT_ALWAYS_RETURNS_NULL =
            new Break("putIfAbsent(Object,Object) always returns null");

    /// Break constant that causes `putIfAbsent(K, V)` to always replace the value.
    ///
    /// With this break, `putIfAbsent` will replace the existing value even if the key is already present.
    ///
    /// **Affected Methods:**
    /// - {@link #putIfAbsent(Object, Object)} - Always replaces value
    ///
    /// @since 1.0
    public static final Break PUT_IF_ABSENT_ALWAYS_REPLACES_VALUE =
            new Break("putIfAbsent(Object,Object) always replaces value");

    // ========== remove(Object) method breaks ==========

    /// Break constant that causes `remove(Object)` to not remove the key from the map.
    ///
    /// **Affected Methods:**
    /// - {@link #remove(Object)} - Does not remove key
    ///
    /// @since 1.0
    public static final Break REMOVE_DOES_NOT_REMOVE_KEY =
            new Break("remove(Object) does not remove the key");

    /// Break constant that causes `remove(Object)` to always return `null`.
    ///
    /// **Affected Methods:**
    /// - {@link #remove(Object)} - Returns `null`
    ///
    /// @since 1.0
    public static final Break REMOVE_ALWAYS_RETURNS_NULL =
            new Break("remove(Object) always returns null");

    // ========== remove(Object,Object) method breaks ==========

    /// Break constant that causes `remove(Object, Object)` to not remove the value.
    ///
    /// **Affected Methods:**
    /// - {@link #remove(Object, Object)} - Does not remove value
    ///
    /// @since 1.0
    public static final Break REMOVE_DOES_NOT_REMOVE_VALUE =
            new Break("remove(Object,Object) does not remove the value");

    /// Break constant that causes `remove(Object, Object)` to not check value match.
    ///
    /// With this break, the method will remove the key if it exists, whether or not its value matches.
    ///
    /// **Affected Methods:**
    /// - {@link #remove(Object, Object)} - Does not check value
    ///
    /// @since 1.0
    public static final Break REMOVE_DOES_NOT_CHECK_VALUE_MATCH =
            new Break("remove(Object,Object) does not check if the value matches before removal");

    /// Break constant that causes `remove(Object, Object)` to always return `true`.
    ///
    /// **Affected Methods:**
    /// - {@link #remove(Object, Object)} - Returns `true`
    ///
    /// @since 1.0
    public static final Break REMOVE_ALWAYS_RETURNS_TRUE =
            new Break("remove(Object,Object) always returns true");

    /// Break constant that causes `remove(Object, Object)` to always return `false`.
    ///
    /// **Affected Methods:**
    /// - {@link #remove(Object, Object)} - Returns `false`
    ///
    /// @since 1.0
    public static final Break REMOVE_ALWAYS_RETURNS_FALSE =
            new Break("remove(Object,Object) always returns false");

    /// Break constant that causes `remove(Object, Object)` to always return the opposite value.
    ///
    /// **Affected Methods:**
    /// - {@link #remove(Object, Object)} - Returns opposite result
    ///
    /// @since 1.0
    public static final Break REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("remove(Object,Object) always returns opposite value");

    // ========== replace(Object, Object) method breaks ==========

    /// Break constant that causes `replace(K, V)` to not replace the value.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object)} - Does not replace value
    ///
    /// @since 1.0
    public static final Break REPLACE_2_ARG_DOES_NOT_REPLACE_VALUE =
            new Break("replace(Object, Object) does not replace the value");

    /// Break constant that causes `replace(K, V)` to always return `null`.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object)} - Returns `null`
    ///
    /// @since 1.0
    public static final Break REPLACE_2_ARG_ALWAYS_RETURNS_NULL =
            new Break("replace(Object, Object) always returns null");

    // ========== replace(Object, Object, Object) method breaks ==========

    /// Break constant that causes `replace(K, V, V)` to not replace the value.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object, Object)} - Does not replace value
    ///
    /// @since 1.0
    public static final Break REPLACE_3_ARG_DOES_NOT_REPLACE_VALUE =
            new Break("replace(Object, Object, Object) does not replace the value");

    /// Break constant that causes `replace(K, V, V)` to only match the key.
    ///
    /// With this break, the method will replace the value if the key exists, whether or not the old value matches.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object, Object)} - Only matches key
    ///
    /// @since 1.0
    public static final Break REPLACE_3_ARG_ONLY_MATCHES_KEY =
            new Break("replace(Object, Object, Object) only matches the key");

    /// Break constant that causes `replace(K, V, V)` to always return `true`.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object, Object)} - Returns `true`
    ///
    /// @since 1.0
    public static final Break REPLACE_3_ARG_ALWAYS_RETURNS_TRUE =
            new Break("replace(Object, Object, Object) always returns true");

    /// Break constant that causes `replace(K, V, V)` to always return `false`.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object, Object)} - Returns `false`
    ///
    /// @since 1.0
    public static final Break REPLACE_3_ARG_ALWAYS_RETURNS_FALSE =
            new Break("replace(Object, Object, Object) always returns false");

    /// Break constant that causes `replace(K, V, V)` to always return the opposite value.
    ///
    /// **Affected Methods:**
    /// - {@link #replace(Object, Object, Object)} - Returns opposite result
    ///
    /// @since 1.0
    public static final Break REPLACE_3_ARG_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("replace(Object, Object, Object) always returns the opposite value");

    // ========== replaceAll(BiFunction) method breaks ==========

    /// Break constant that causes `replaceAll(BiFunction)` to not call the action on any elements.
    ///
    /// **Affected Methods:**
    /// - {@link #replaceAll(BiFunction)} - Action never executed
    ///
    /// @since 1.0
    public static final Break REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS =
            new Break("replaceAll(BiFunction) does not replace any pairs");

    /// Break constant that causes `replaceAll(BiFunction)` to skip the first pair encountered.
    ///
    /// **Affected Methods:**
    /// - {@link #replaceAll(BiFunction)} - Skips first entry
    ///
    /// @since 1.0
    public static final Break REPLACE_ALL_SKIPS_THE_FIRST_PAIR =
            new Break("replaceAll(BiFunction) skips the first pair");

    /// Break constant that causes `replaceAll(BiFunction)` to skip the last pair encountered.
    ///
    /// **Affected Methods:**
    /// - {@link #replaceAll(BiFunction)} - Skips last entry
    ///
    /// @since 1.0
    public static final Break REPLACE_ALL_SKIPS_THE_LAST_PAIR =
            new Break("replaceAll(BiFunction) skips the last pair");

    // ========== size() method breaks ==========

    /// Break constant that causes `size()` to always return zero.
    ///
    /// **Affected Methods:**
    /// - {@link #size()} - Returns 0
    ///
    /// @since 1.0
    public static final Break SIZE_ALWAYS_RETURNS_ZERO =
            new Break("Size method always returns zero");

    /// Break constant that causes `size()` to be off by one.
    ///
    /// **Affected Methods:**
    /// - {@link #size()} - Returns actual size minus 1
    ///
    /// @since 1.0
    public static final Break SIZE_IS_OFF_BY_ONE =
            new Break("Size method is off by one");

    // ========== values() method breaks ==========

    /// Break constant that causes `values()` to always return an empty collection.
    ///
    /// **Affected Methods:**
    /// - {@link #values()} - Returns empty collection
    ///
    /// @since 1.0
    public static final Break VALUES_RETURNS_EMPTY_COLLECTION =
            new Break("values() always returns an empty collection");

    /// Break constant that causes `values()` to return `null` when the map is empty.
    ///
    /// **Affected Methods:**
    /// - {@link #values()} - Returns `null` if empty
    ///
    /// @since 1.0
    public static final Break VALUES_RETURNS_NULL_WHEN_EMPTY =
            new Break("values() returns null when empty");

    /// A bit flag that, when set in the permits, says that the map permits null keys.
    protected static final int PERMITS_NULL_KEYS = 0b0001;

    /// A bit flag that, when set in the permits, says that the map permits null values.
    protected static final int PERMITS_NULL_VALUES = 0b0010;

    /// A bit flag that, when set in the permits, says that the map permits keys of incompatible types.
    protected static final int PERMITS_INCOMPATIBLE_KEYS = 0b0100;

    /// A bit flag that, when set in the permits, says that the map permits values of incompatible types.
    protected static final int PERMITS_INCOMPATIBLE_VALUES = 0b1000;

    /// The default permits: every flag set (`PERMITS_NULL_KEYS`, `PERMITS_NULL_VALUES`, `PERMITS_INCOMPATIBLE_KEYS` and
    /// `PERMITS_INCOMPATIBLE_VALUES`).
    protected static final int DEFAULT_PERMITS = PERMITS_NULL_KEYS | PERMITS_NULL_VALUES
            | PERMITS_INCOMPATIBLE_KEYS | PERMITS_INCOMPATIBLE_VALUES;

    @Serial
    private static final long serialVersionUID = 1L;

    /// The underlying map.
    private Map<K, V> map; // Must be non-final to support clone()

    /// Flag indicating whether this map permits null keys.
    private final int permits;

    /// Creates an empty map, backed by a `HashMap`, that has no breaks and permits null keys and values.
    ///
    /// @since 1.0
    public BreakableMap() {
        this(new HashMap<>(), DEFAULT_BREAKS, DEFAULT_METHOD_STATUSES, DEFAULT_PERMITS, DEFAULT_SAFETY);
    }

    /// Creates a copy of another BreakableMap with the same configuration.
    ///
    /// The underlying map, the breaks and the permits are copied; the keys and values are not cloned.
    ///
    /// @param other the breakable map to copy; must not be null
    /// @throws NullPointerException if `other` is null
    /// @since 1.0
    public BreakableMap(final BreakableMap<K, V> other) {
        this(new HashMap<>(other.map), new HashSet<>(other.breaks()), new HashMap<>(other.methodStatuses()),
                other.permits(), other.isSafe());
    }

    /// Creates a `BreakableMap` wrapping the specified map, with the default permits and no breaks.
    ///
    /// @param mapArg the underlying map to wrap; must not be null
    /// @throws NullPointerException if `map` is null
    /// @since 1.0
    public BreakableMap(final Map<K, V> mapArg) {
        this(mapArg, DEFAULT_BREAKS, DEFAULT_METHOD_STATUSES, DEFAULT_PERMITS, DEFAULT_SAFETY);
    }

    /// Creates a `BreakableMap` wrapping the specified map with the given breaks and null handling configuration.
    ///
    /// @param mapArg the underlying map to wrap; must not be null
    /// @param breaks the collection of breaks to apply; must not be null
    /// @param methodStatuses the method status configuration; must not be null
    /// @param permitsArg the bitwise combination of permitted features
    /// @param isSafe whether the map is safe for concurrent access
    /// @throws NullPointerException if `map`, `breaks`, or `methodStatuses` is null
    /// @since 1.0
    protected BreakableMap(final Map<K, V> mapArg, final Set<Break> breaks,
                        final Map<InterfaceMethod, MethodStatus> methodStatuses,
                        final int permitsArg, final boolean isSafe) {
        super(breaks, methodStatuses, isSafe);
        this.map = Objects.requireNonNull(mapArg);
        this.permits = permitsArg;
    }

    /// Compares the specified object with this map for equality.
    ///
    /// Only the mappings are compared; breaks and null handling configuration are not considered.
    ///
    /// @param o object to be compared for equality with this map
    /// @return true if the specified object is equal to this map
    @Override
    public boolean equals(final @Nullable Object o) {
        if (o instanceof BreakableMap<?, ?> otherBroken) {
            return Objects.equals(map, otherBroken.map);
        }
        if (o instanceof Map<?, ?> otherMap) {
            return Objects.equals(map, otherMap);
        }
        return false;
    }

    /// Returns the hash code value for this map, that of the underlying map.
    ///
    /// @return the hash code value for this map
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /// Returns a string representation of this map.
    ///
    /// It is that of the underlying map; breaks and configuration are not included.
    ///
    /// @return a string representation of this map
    @Override
    public String toString() {
        return map.toString();
    }

    /// Creates a shallow copy of this `BreakableMap`.
    ///
    /// The underlying map, the breaks and the method statuses are copied; keys and values are not cloned.
    ///
    /// @return a shallow copy of this map
    /// @since 1.0
    @Override
    @SuppressWarnings("unchecked")
    public BreakableMap<K, V> clone() {
        try {
            BreakableMap<K, V> copy = (BreakableMap<K, V>) super.clone();
            copy.map = copyOfStore(map);
            return copy;
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException("Cloning BreakableMap failed", e);
        }
    }

    /// Returns the map this breakable wraps, its store.
    ///
    /// @return the store
    /// @since 0.1.2
    protected Map<K, V> store() {
        return map;
    }

    /// Returns a copy of a store, for [#clone()]: a `HashMap` here. A subclass whose store must be of another kind
    /// (a concurrent map, say) overrides it.
    ///
    /// @param storeArg the store to copy
    /// @return a new store holding the same mappings
    /// @since 0.1.2
    protected Map<K, V> copyOfStore(final Map<K, V> storeArg) {
        return new HashMap<>(storeArg);
    }

    /// Returns the bitwise combination of permitted features for this map.
    ///
    /// @return the permits bitmask
    /// @since 1.0
    protected int permits() {
        return permits;
    }

    /// Indicates if the map permits null keys.
    ///
    /// When they are not, a null key passed to any method results in a NullPointerException.
    ///
    /// @return true if the map permits null keys, false if it does not
    /// @since 1.0
    public boolean permitsNullKeys() {
        return (permits & PERMITS_NULL_KEYS) != 0;
    }

    /// Indicates if the map permits null values.
    ///
    /// When they are not, a null value passed to any method results in a NullPointerException.
    ///
    /// @return true if the map permits null values, false if it does not
    /// @since 1.0
    public boolean permitsNullValues() {
        return (permits & PERMITS_NULL_VALUES) != 0;
    }

    /// Returns the number of key-value mappings in this map.
    ///
    /// ## Supported Breaks
    /// - {@link #SIZE_ALWAYS_RETURNS_ZERO} - Always returns 0 regardless of actual size
    /// - {@link #SIZE_IS_OFF_BY_ONE} - Returns actual size minus 1
    ///
    /// @return the number of key-value mappings in this map
    /// @throws UnsupportedOperationException if this map does not support the 'size' operation
    /// @see java.util.Map#size()
    @Override
    public int size() {
        if (supportsMethod(MapMethods.SIZE)) {
            if (hasBreak(SIZE_ALWAYS_RETURNS_ZERO)) {
                return 0;
            } else if (hasBreak(SIZE_IS_OFF_BY_ONE)) {
                return map.size() - 1;
            }
            return map.size();
        } else {
            throw new UnsupportedOperationException("Unsupported method: size()");
        }
    }

    /// Returns true if this map contains no key-value mappings.
    ///
    /// ## Supported Breaks
    /// - {@link #IS_EMPTY_ALWAYS_RETURNS_TRUE} - Always returns true regardless of actual content
    /// - {@link #IS_EMPTY_ALWAYS_RETURNS_FALSE} - Always returns false regardless of actual content
    /// - {@link #IS_EMPTY_RETURNS_OPPOSITE_VALUE} - Returns the opposite of the actual emptiness state
    ///
    /// @return true if this map contains no key-value mappings
    /// @throws UnsupportedOperationException if this map does not support the 'isEmpty' operation
    /// @see java.util.Map#isEmpty()
    @Override
    public boolean isEmpty() {
        if (supportsMethod(MapMethods.IS_EMPTY)) {
            if (hasBreak(IS_EMPTY_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(IS_EMPTY_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(IS_EMPTY_RETURNS_OPPOSITE_VALUE)) {
                return !map.isEmpty();
            }
            return map.isEmpty();
        } else {
            throw new UnsupportedOperationException("Unsupported method: isEmpty()");
        }
    }

    /// Returns true if this map contains a mapping for the specified key.
    ///
    /// ## Supported Breaks
    /// - {@link #CONTAINS_KEY_ALWAYS_RETURNS_TRUE} - Always returns true regardless of key existence
    /// - {@link #CONTAINS_KEY_ALWAYS_RETURNS_FALSE} - Always returns false regardless of key existence
    /// - {@link #CONTAINS_KEY_RETURNS_OPPOSITE_VALUE} - Returns the opposite of the actual containment result
    ///
    /// @param key key whose presence in this map is to be tested
    /// @return true if this map contains a mapping for the specified key
    /// @throws ClassCastException if the key is of an inappropriate type for this map
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys
    /// @throws UnsupportedOperationException if this map does not support the 'containsKey' operation
    /// @see java.util.Map#containsKey(Object)
    @Override
    public boolean containsKey(final Object key) {
        if (supportsMethod(MapMethods.CONTAINS_KEY)) {
            validateKey(key);
            if (hasBreak(CONTAINS_KEY_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(CONTAINS_KEY_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(CONTAINS_KEY_RETURNS_OPPOSITE_VALUE)) {
                return !map.containsKey(key);
            }
            return map.containsKey(key);
        } else {
            throw new UnsupportedOperationException("Unsupported method: containsKey(Object)");
        }
    }

    /// Returns true if this map maps one or more keys to the specified value.
    ///
    /// ## Supported Breaks
    /// - {@link #CONTAINS_VALUE_ALWAYS_RETURNS_TRUE} - Always returns true regardless of value existence
    /// - {@link #CONTAINS_VALUE_ALWAYS_RETURNS_FALSE} - Always returns false regardless of value existence
    /// - {@link #CONTAINS_VALUE_RETURNS_OPPOSITE_VALUE} - Returns the opposite of the actual containment result
    ///
    /// @param value value whose presence in this map is to be tested
    /// @return true if this map maps one or more keys to the specified value
    /// @throws ClassCastException if the value is of an inappropriate type for this map
    /// @throws NullPointerException if the specified value is null and this map does not permit null values
    /// @throws UnsupportedOperationException if this map does not support the 'containsValue' operation
    /// @see java.util.Map#containsValue(Object)
    @Override
    public boolean containsValue(final Object value) {
        if (supportsMethod(MapMethods.CONTAINS_VALUE)) {
            validateValue(value);
            if (hasBreak(CONTAINS_VALUE_ALWAYS_RETURNS_TRUE)) {
                return true;
            } else if (hasBreak(CONTAINS_VALUE_ALWAYS_RETURNS_FALSE)) {
                return false;
            } else if (hasBreak(CONTAINS_VALUE_RETURNS_OPPOSITE_VALUE)) {
                return !map.containsValue(value);
            }
            return map.containsValue(value);
        } else {
            throw new UnsupportedOperationException("Unsupported method: containsValue(Object)");
        }
    }

    /// Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
    ///
    /// ## Supported Breaks
    /// - {@link #GET_ALWAYS_RETURNS_NULL} - Always returns null regardless of key existence
    /// - {@link #GET_FAILS_FOR_FIRST_KEY} - Returns null for the first key in the key set iteration order
    /// - {@link #GET_FAILS_FOR_LAST_KEY} - Returns null for the last key in the key set iteration order
    ///
    /// @param key the key whose associated value is to be returned
    /// @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
    /// @throws ClassCastException if the key is of an inappropriate type for this map
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys
    /// @throws UnsupportedOperationException if this map does not support the 'get' operation
    /// @see java.util.Map#get(Object)
    @Override
    public @Nullable V get(final Object key) {
        if (supportsMethod(MapMethods.GET)) {
            validateKey(key);
            if (hasBreak(GET_ALWAYS_RETURNS_NULL)
                    || (hasBreak(GET_FAILS_FOR_FIRST_KEY) && key.equals(first(map.keySet())))
                    || (hasBreak(GET_FAILS_FOR_LAST_KEY) && key.equals(last(map.keySet())))) {
                return null;
            }
            return map.get(key);
        } else {
            throw new UnsupportedOperationException("Unsupported method: get(Object)");
        }
    }

    /// Associates the specified value with the specified key in this map.
    ///
    /// ## Supported Breaks
    /// - {@link #PUT_DOES_NOT_ADD_PAIR} - Does not add the key-value pair to the map
    /// - {@link #PUT_ALWAYS_RETURNS_NULL} - Always returns null regardless of previous value
    ///
    /// @param key key with which the specified value is to be associated
    /// @param value value to be associated with the specified key
    /// @return the previous value associated with key, or null if there was no mapping for key
    /// @throws UnsupportedOperationException if this map does not support the 'put' operation
    /// @throws ClassCastException if the class of the specified key or value prevents it from being stored in this map
    /// @throws NullPointerException if the key or value is null and this map does not permit null keys or values
    /// @throws IllegalArgumentException if some property of the specified key or value prevents it from being stored
    ///                                  in this map
    /// @see java.util.Map#put(Object, Object)
    @Override
    public @Nullable V put(final K key, final V value) {
        if (supportsMethod(MapMethods.PUT)) {
            validateKey(key);
            validateValue(value);
            if (hasBreak(PUT_DOES_NOT_ADD_PAIR)) {
                return null;
            }
            V previous = map.put(key, value);
            if (hasBreak(PUT_ALWAYS_RETURNS_NULL)) {
                return null;
            } else {
                return previous;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: put(Object,Object)");
        }
    }

    /// Removes the mapping for a key from this map if it is present.
    ///
    /// ## Supported Breaks
    /// - {@link #REMOVE_DOES_NOT_REMOVE_KEY} - Does not actually remove the key but returns the associated value
    /// - {@link #REMOVE_ALWAYS_RETURNS_NULL} - Always returns null regardless of previous value
    ///
    /// @param key key whose mapping is to be removed from the map
    /// @return the previous value associated with key, or null if there was no mapping for key
    /// @throws ClassCastException if the key is of an inappropriate type for this map
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys
    /// @throws UnsupportedOperationException if this map does not support the 'remove' operation
    /// @see java.util.Map#remove(Object)
    @Override
    public @Nullable V remove(final Object key) {
        validateKey(key);
        if (supportsMethod(MapMethods.REMOVE)) {
            if (hasBreak(REMOVE_DOES_NOT_REMOVE_KEY)) {
                return map.get(key);
            }
            V previous = map.remove(key);
            if (hasBreak(REMOVE_ALWAYS_RETURNS_NULL)) {
                return null;
            }
            return previous;
        } else {
            throw new UnsupportedOperationException("Unsupported method: remove(Object)");
        }
    }

    /// Copies all the mappings from the specified map to this map.
    ///
    /// ## Supported Breaks
    /// - {@link #PUT_ALL_DOES_NOT_ADD_ANY_PAIRS} - Does not add any of the key-value pairs to the map
    ///
    /// @param m mappings to be stored in this map
    /// @throws UnsupportedOperationException if this map does not support the 'putAll' operation
    /// @throws ClassCastException if the class of a key or value in the specified map prevents it from being stored
    /// @throws NullPointerException if the specified map is null, or if this map does not permit null keys or values,
    ///     and the specified map contains null keys or values
    /// @throws IllegalArgumentException if some property of a key or value in the specified map prevents it from being
    ///     stored in this map
    /// @see java.util.Map#putAll(Map)
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        if (supportsMethod(MapMethods.PUT_ALL)) {
            // Found by iterating: asking a map that refuses nulls whether it contains one may throw.
            for (final Entry<? extends K, ? extends V> entry : m.entrySet()) {
                if ((entry.getKey() == null && !permitsNullKeys())
                        || (entry.getValue() == null && !permitsNullValues())) {
                    throw new NullPointerException("the map holds a null key or value, which this map does not permit");
                }
            }
            if (hasBreak(PUT_ALL_DOES_NOT_ADD_ANY_PAIRS)) {
                return;
            }
            map.putAll(m);
        } else {
            throw new UnsupportedOperationException("Unsupported method: putAll(Map)");
        }
    }

    /// Removes all the mappings from this map.
    ///
    /// ## Supported Breaks
    /// - {@link #CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS} - Does not remove any key-value pairs
    /// - {@link #CLEAR_DOES_NOT_REMOVE_FIRST_PAIR} - Removes all pairs except the first one in iteration order
    /// - {@link #CLEAR_DOES_NOT_REMOVE_LAST_PAIR} - Removes all pairs except the last one in iteration order
    ///
    /// @throws UnsupportedOperationException if this map does not support the 'clear' operation
    /// @see java.util.Map#clear()
    @Override
    public void clear() {
        if (supportsMethod(MapMethods.CLEAR)) {
            if (hasBreak(CLEAR_ALL_DOES_NOT_REMOVE_ANY_PAIRS)) {
                return;
            }
            if (hasBreak(CLEAR_DOES_NOT_REMOVE_FIRST_PAIR)) {
                Iterator<Entry<K, V>> iterator = skipFirstIterator(map.entrySet());
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
                return;
            }
            if (hasBreak(CLEAR_DOES_NOT_REMOVE_LAST_PAIR)) {
                Iterator<Entry<K, V>> iterator = skipLastIterator(map.entrySet());
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
                return;
            }
            map.clear();
        } else {
            throw new UnsupportedOperationException("Unsupported method: clear()");
        }
    }

    /// Returns a `Set` view of the keys contained in this map.
    ///
    /// ## Supported Breaks
    /// - {@link #KEY_SET_RETURNS_EMPTY_SET} - Always returns an empty set regardless of actual keys
    /// - {@link #KEY_SET_RETURNS_NULL_WHEN_EMPTY} - Returns null when the map is empty instead of an empty set
    ///
    /// @return a set view of the keys contained in this map
    /// @throws UnsupportedOperationException if this map does not support the `keySet` operation
    /// @see java.util.Map#keySet()
    @SuppressWarnings("DataFlowIssue") // returns null on a break
    @Override
    public @Nullable Set<K> keySet() {
        if (supportsMethod(MapMethods.KEY_SET)) {
            if (hasBreak(KEY_SET_RETURNS_EMPTY_SET)) {
                return Collections.emptySet();
            }
            if (hasBreak(KEY_SET_RETURNS_NULL_WHEN_EMPTY) && map.isEmpty()) {
                return null;
            }
            // TODO: Determine if/how breakableMap should be able to specify the characteristics for the key set
            return BreakableSet.wrap(map.keySet(), breaks(), methodStatuses(),
                    BreakableSet.DEFAULT_CHARACTERISTICS, permits(), isSafe(), Object.class);
        } else {
            throw new UnsupportedOperationException("Unsupported method: keySet()");
        }
    }

    /// Returns a `Collection` view of the values contained in this map.
    ///
    /// ## Supported Breaks
    /// - {@link #VALUES_RETURNS_EMPTY_COLLECTION} - Always returns an empty collection regardless of actual values
    /// - {@link #VALUES_RETURNS_NULL_WHEN_EMPTY} - Returns null when the map is empty instead of an empty collection
    ///
    /// @return a collection view of the values contained in this map
    /// @throws UnsupportedOperationException if this map does not support the 'values' operation
    /// @see java.util.Map#values()
    @Override
    @SuppressWarnings("DataFlowIssue") // returns null on a break
    public @Nullable Collection<V> values() {
        if (supportsMethod(MapMethods.VALUES)) {
            if (hasBreak(VALUES_RETURNS_EMPTY_COLLECTION)) {
                return Collections.emptyList();
            }
            if (hasBreak(VALUES_RETURNS_NULL_WHEN_EMPTY) && map.isEmpty()) {
                return null;
            }
            return BreakableCollection.wrap(map.values(), breaks(), methodStatuses(),
                    BreakableCollection.DEFAULT_CHARACTERISTICS, permits(), isSafe(), Object.class);
        } else {
            throw new UnsupportedOperationException("Unsupported method: values()");
        }
    }

    /// Returns a `Set` view of the mappings contained in this map.
    ///
    /// ## Supported Breaks
    /// - {@link #ENTRY_SET_RETURNS_EMPTY_SET} - Always returns an empty set regardless of actual entries
    /// - {@link #ENTRY_SET_RETURNS_NULL_WHEN_EMPTY} - Returns null when the map is empty instead of an empty set
    ///
    /// @return a set view of the mappings contained in this map
    /// @throws UnsupportedOperationException if this map does not support the 'entrySet' operation
    /// @see java.util.Map#entrySet()
    @SuppressWarnings("DataFlowIssue") // returns null on a break
    @Override
    public @Nullable Set<Entry<K, V>> entrySet() {
        if (supportsMethod(MapMethods.ENTRY_SET)) {
            // TODO: This needs to support entry sets that potentially prevent null keys and values

            if (hasBreak(ENTRY_SET_RETURNS_EMPTY_SET)) {
                return Collections.emptySet();
            }
            if (hasBreak(ENTRY_SET_RETURNS_NULL_WHEN_EMPTY) && map.isEmpty()) {
                return null;
            }
            return map.entrySet();
        } else {
            throw new UnsupportedOperationException("Unsupported method: entrySet()");
        }
    }

    /// Returns the value to which the specified key is mapped, or `defaultValue` if it has no mapping.
    ///
    /// ## Supported Breaks
    /// - {@link #GET_OR_DEFAULT_ALWAYS_RETURNS_NULL} - Always returns null regardless of key existence or default value
    /// - {@link #GET_OR_DEFAULT_FAILS_FOR_FIRST_KEY} - Returns the default value for the first key in iteration order
    /// - {@link #GET_OR_DEFAULT_FAILS_FOR_LAST_KEY} - Returns default value for the last key in key set iteration order
    ///
    /// @param key the key whose associated value is to be returned
    /// @param defaultValue the default mapping of the key
    /// @return the value to which the specified key is mapped, or `defaultValue` if it has no mapping
    /// @throws ClassCastException if the key is of an inappropriate type for this map
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys
    /// @throws UnsupportedOperationException if this map does not support the 'getOrDefault' operation
    /// @see java.util.Map#getOrDefault(Object, Object)
    @Override
    public @Nullable V getOrDefault(final Object key, final V defaultValue) {
        if (supportsMethod(MapMethods.GET_OR_DEFAULT)) {
            validateKey(key);
            if (hasBreak(GET_OR_DEFAULT_ALWAYS_RETURNS_NULL)) {
                return null;
            }
            if ((hasBreak(GET_OR_DEFAULT_FAILS_FOR_FIRST_KEY) && key.equals(first(map.keySet())))
                || (hasBreak(GET_OR_DEFAULT_FAILS_FOR_LAST_KEY) && key.equals(last(map.keySet())))) {
                return defaultValue;
            }
            return map.getOrDefault(key, defaultValue);
        } else {
            throw new UnsupportedOperationException("Unsupported method: getOrDefault(Object,Object)");
        }
    }

    /// Performs the given action for each entry in this map.
    ///
    /// ## Supported Breaks
    /// - {@link #FOR_EACH_DOES_NOT_CALL_ACTION} - Does not execute the action on any entries
    /// - {@link #FOR_EACH_SKIPS_FIRST_PAIR} - Skips the first entry in iteration order
    /// - {@link #FOR_EACH_SKIPS_LAST_PAIR} - Skips the last entry in iteration order
    ///
    /// @param action the action to be performed for each entry
    /// @throws NullPointerException if the specified action is null
    /// @throws UnsupportedOperationException if this map does not support the 'forEach' operation
    /// @see java.util.Map#forEach(BiConsumer)
    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        if (supportsMethod(MapMethods.FOR_EACH)) {
            if (hasBreak(FOR_EACH_DOES_NOT_CALL_ACTION)) {
                return;
            }
            if (hasBreak(FOR_EACH_SKIPS_FIRST_PAIR)) {
                Iterator<Entry<K, V>> iterator = skipFirstIterator(map.entrySet());
                while (iterator.hasNext()) {
                    Entry<K, V> entry = validateEntry(iterator.next());
                    action.accept(entry.getKey(), entry.getValue());
                }
                return;
            }
            if (hasBreak(FOR_EACH_SKIPS_LAST_PAIR)) {
                Iterator<Entry<K, V>> iterator = skipLastIterator(map.entrySet());
                while (iterator.hasNext()) {
                    Entry<K, V> entry = validateEntry(iterator.next());
                    action.accept(entry.getKey(), entry.getValue());
                }
                return;
            }
            for (final Entry<K, V> kvEntry : map.entrySet()) {
                Entry<K, V> entry = validateEntry(kvEntry);
                action.accept(entry.getKey(), entry.getValue());
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: forEach(BiConsumeR)");
        }
    }

    /// Replaces each entry's value with the result of invoking the given function on that entry.
    ///
    /// ## Supported Breaks
    /// - {@link #REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS} - Does not replace any values
    /// - {@link #REPLACE_ALL_SKIPS_THE_FIRST_PAIR} - Skips replacing the first entry in iteration order
    /// - {@link #REPLACE_ALL_SKIPS_THE_LAST_PAIR} - Skips replacing the last entry in iteration order
    ///
    /// @param function the function to apply to each entry
    /// @throws UnsupportedOperationException if this map does not support the 'replaceAll' operation
    /// @throws ClassCastException if the class of a replacement value prevents it from being stored in this map
    /// @throws NullPointerException if the specified function is null, or if a replacement value is null and this map
    ///     does not permit null values
    /// @throws IllegalArgumentException if some property of a replacement value prevents it from being stored
    /// @see java.util.Map#replaceAll(BiFunction)
    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        if (supportsMethod(MapMethods.REPLACE_ALL)) {
            Objects.requireNonNull(function);
            if (hasBreak(REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS)) {
                return;
            }
            if (hasBreak(REPLACE_ALL_SKIPS_THE_FIRST_PAIR)) {
                Iterator<Entry<K, V>> iterator = skipFirstIterator(map.entrySet());
                while (iterator.hasNext()) {
                    Entry<K, V> entry = validateEntry(iterator.next());
                    entry.setValue(function.apply(entry.getKey(), entry.getValue()));
                }
                return;
            }
            if (hasBreak(REPLACE_ALL_SKIPS_THE_LAST_PAIR)) {
                Iterator<Entry<K, V>> iterator = skipLastIterator(map.entrySet());
                while (iterator.hasNext()) {
                    Entry<K, V> entry = validateEntry(iterator.next());
                    entry.setValue(function.apply(entry.getKey(), entry.getValue()));
                }
                return;
            }
            for (final Entry<K, V> kvEntry : map.entrySet()) {
                Entry<K, V> entry = validateEntry(kvEntry);
                entry.setValue(function.apply(entry.getKey(), entry.getValue()));
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: replaceAll(BiFunction)");
        }
    }

    /// Associates the specified key with the given value if it is absent or mapped to null.
    ///
    /// ## Supported Breaks
    /// - {@link #PUT_IF_ABSENT_DOES_NOT_PUT_VALUE} - Does not put the value but returns the existing value or null
    /// - {@link #PUT_IF_ABSENT_ALWAYS_RETURNS_NULL} - Always returns null regardless of whether a value was put
    /// - {@link #PUT_IF_ABSENT_ALWAYS_REPLACES_VALUE} - Always replaces the value even if one already exists
    ///
    /// @param key key with which the specified value is to be associated
    /// @param value value to be associated with the specified key
    /// @return the previous value associated with the specified key, or null if there was no mapping for the key
    /// @throws UnsupportedOperationException if this map does not support the 'putIfAbsent' operation
    /// @throws ClassCastException if the key or value is of an inappropriate type for this map
    /// @throws NullPointerException if the key or value is null and this map does not permit null keys or values
    /// @throws IllegalArgumentException if some property of the specified key or value prevents it from being stored
    /// @see java.util.Map#putIfAbsent(Object, Object)
    @Override
    public @Nullable V putIfAbsent(final K key, final V value) {
        if (supportsMethod(MapMethods.PUT_IF_ABSENT)) {
            validateKey(key);
            validateValue(value);
            @Nullable V existing;
            if (hasBreak(PUT_IF_ABSENT_DOES_NOT_PUT_VALUE)) {
                existing = map.getOrDefault(key, null);
            } else if (hasBreak(PUT_IF_ABSENT_ALWAYS_REPLACES_VALUE)) {
                existing =  map.put(key, value);
            } else {
                existing = map.putIfAbsent(key, value);
            }
            if (hasBreak(PUT_IF_ABSENT_ALWAYS_RETURNS_NULL)) {
                existing = null;
            }
            return existing;
        } else {
            throw new UnsupportedOperationException("Unsupported method: putIfAbsent(Object,Object)");
        }
    }

    /// Removes the entry for the specified key only if it is currently mapped to the specified value.
    ///
    /// ## Supported Breaks
    /// - {@link #REMOVE_DOES_NOT_REMOVE_VALUE} - Does not remove the entry but returns whether it would have been
    /// - {@link #REMOVE_DOES_NOT_CHECK_VALUE_MATCH} - Removes the key without checking if the value matches
    /// - {@link #REMOVE_ALWAYS_RETURNS_TRUE} - Always returns true regardless of whether anything was removed
    /// - {@link #REMOVE_ALWAYS_RETURNS_FALSE} - Always returns false regardless of whether anything was removed
    /// - {@link #REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE} - Returns the opposite of what would normally be returned
    ///
    /// @param key key with which the specified value is associated
    /// @param value value expected to be associated with the specified key
    /// @return true if the value was removed
    /// @throws UnsupportedOperationException if the `remove` operation is not supported by this map
    /// @throws ClassCastException if the key or value is of an inappropriate type for this map
    /// @throws NullPointerException if the key or value is null and this map does not permit null keys or values
    /// @see java.util.Map#remove(Object, Object)
    @Override
    public boolean remove(final Object key, final Object value) {
        if (supportsMethod(MapMethods.REMOVE_TWO_ARG)) {
            validateKey(key);
            validateValue(value);
            boolean removed;
            if (hasBreak(REMOVE_DOES_NOT_REMOVE_VALUE)) {
                removed = map.containsKey(key) && map.get(key).equals(value);
            } else if (hasBreak(REMOVE_DOES_NOT_CHECK_VALUE_MATCH)) {
                removed = map.remove(key) != null;
            } else {
                removed = map.remove(key, value);
            }
            if (hasBreak(REMOVE_ALWAYS_RETURNS_TRUE)) {
                removed = true;
            } else if (hasBreak(REMOVE_ALWAYS_RETURNS_FALSE)) {
                removed = false;
            } else if (hasBreak(REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
                removed = !removed;
            }
            return removed;
        } else {
            throw new UnsupportedOperationException("Unsupported method: remove(Object,Object)");
        }
    }

    /// Replaces the entry for the specified key only if currently mapped to the specified value.
    ///
    /// ## Supported Breaks
    /// - {@link #REPLACE_3_ARG_DOES_NOT_REPLACE_VALUE} - Does not replace the value but returns whether it would be
    /// - {@link #REPLACE_3_ARG_ONLY_MATCHES_KEY} - Only checks that the key exists, ignoring the old value
    /// - {@link #REPLACE_3_ARG_ALWAYS_RETURNS_TRUE} - Always returns true regardless of whether anything was replaced
    /// - {@link #REPLACE_3_ARG_ALWAYS_RETURNS_FALSE} - Always returns false regardless of whether anything was replaced
    /// - {@link #REPLACE_3_ARG_ALWAYS_RETURNS_OPPOSITE_VALUE} - Returns the opposite of what would normally be returned
    ///
    /// @param key key with which the specified value is associated
    /// @param oldValue value expected to be associated with the specified key
    /// @param newValue value to be associated with the specified key
    /// @return true if the value was replaced
    /// @throws UnsupportedOperationException if this map does not support the 'replace' operation
    /// @throws ClassCastException if the class of a specified key or value prevents it from being stored in this map
    /// @throws NullPointerException if a key or value is null and this map does not permit null keys or values
    /// @throws IllegalArgumentException if some property of a specified key or value prevents it from being stored
    /// @see java.util.Map#replace(Object, Object, Object)
    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        boolean replaced;
        if (supportsMethod(MapMethods.REPLACE_THREE_ARG)) {
            validateKey(key);
            validateValue(oldValue);
            validateValue(newValue);
            if (hasBreak(REPLACE_3_ARG_DOES_NOT_REPLACE_VALUE)) {
                replaced = map.containsKey(key) && map.get(key).equals(oldValue);
            } else if (hasBreak(REPLACE_3_ARG_ONLY_MATCHES_KEY)) {
                replaced = map.replace(key, newValue) != null;
            } else {
                replaced = map.replace(key, oldValue, newValue);
            }

            if (hasBreak(REPLACE_3_ARG_ALWAYS_RETURNS_TRUE)) {
                return true;
            }
            if (hasBreak(REPLACE_3_ARG_ALWAYS_RETURNS_FALSE)) {
                return false;
            }
            if (hasBreak(REPLACE_3_ARG_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
                return !replaced;
            }
            return replaced;
        } else {
            throw new UnsupportedOperationException("Unsupported method: replace(Object,Object)");
        }
    }

    /// Replaces the entry for the specified key only if it is currently mapped to some value.
    ///
    /// ## Supported Breaks
    /// - {@link #REPLACE_2_ARG_DOES_NOT_REPLACE_VALUE} - Does not replace the value but returns the current value
    /// - {@link #REPLACE_2_ARG_ALWAYS_RETURNS_NULL} - Always returns null regardless of the previous value
    ///
    /// @param key key with which the specified value is associated
    /// @param value value to be associated with the specified key
    /// @return the previous value associated with the specified key, or null if there was no mapping for the key
    /// @throws UnsupportedOperationException if this map does not support the 'replace' operation
    /// @throws ClassCastException if the class of the specified key or value prevents it from being stored in this map
    /// @throws NullPointerException if the key or value is null and this map does not permit null keys or values
    /// @throws IllegalArgumentException if some property of the specified key or value prevents it from being stored
    /// @see java.util.Map#replace(Object, Object)
    @Override
    public @Nullable V replace(final K key, final V value) {
        if (supportsMethod(MapMethods.REPLACE_TWO_ARG)) {
            validateKey(key);
            validateValue(value);
            V replaced;
            if (hasBreak(REPLACE_2_ARG_DOES_NOT_REPLACE_VALUE)) {
                replaced = map.get(key);
            } else {
                replaced = map.replace(key, value);
            }

            if (hasBreak(REPLACE_2_ARG_ALWAYS_RETURNS_NULL)) {
                return null;
            }
            return replaced;
        } else {
            throw new UnsupportedOperationException("Unsupported method: replace(Object,Object)");
        }
    }

    /// Computes a value for the specified key with the given function if it is absent or mapped to null.
    ///
    /// ## Supported Breaks
    /// - {@link #COMPUTE_IF_ABSENT_NEVER_COMPUTES_VALUE} - Never computes or stores a value, always returns null
    /// - {@link #COMPUTE_IF_ABSENT_ALWAYS_COMPUTES_VALUE} - Computes and stores a value even if the key exists
    /// - {@link #COMPUTE_IF_ABSENT_ALWAYS_RETURNS_NULL} - Always returns null regardless of computed value
    ///
    /// @param key key with which the specified value is to be associated
    /// @param mappingFunction the function to compute a value
    /// @return the current (existing or computed) value associated with the key, or null if the computed value is null
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys, or the
    ///     mappingFunction is null
    /// @throws UnsupportedOperationException if this map does not support the 'computeIfAbsent' operation
    /// @throws ClassCastException if the class of the specified key or value prevents it from being stored in this map
    /// @throws IllegalArgumentException if some property of the specified key or value prevents it from being stored
    /// @see java.util.Map#computeIfAbsent(Object, Function)
    @Override
    public @Nullable V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        if (supportsMethod(MapMethods.COMPUTE_IF_ABSENT)) {
            validateKey(key);
            Objects.requireNonNull(mappingFunction);
            V computed;
            if (hasBreak(COMPUTE_IF_ABSENT_NEVER_COMPUTES_VALUE)) {
                computed = null;
            } else if (hasBreak(COMPUTE_IF_ABSENT_ALWAYS_COMPUTES_VALUE)) {
                computed = validateValue(mappingFunction.apply(key));
                map.put(key, computed);
            } else {

                // A null result records no mapping (Map#computeIfAbsent), so it is not validated.
                computed = map.computeIfAbsent(key, mappingFunction);
            }

            if (hasBreak(COMPUTE_IF_ABSENT_ALWAYS_RETURNS_NULL)) {
                return null;
            } else {
                return computed;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: computeIfAbsent(Object, Function)");
        }
    }

    /// Computes a new value for the specified key with the given function if it is present and non-null.
    ///
    /// ## Supported Breaks
    /// - {@link #COMPUTE_IF_PRESENT_NEVER_COMPUTES_VALUE} - Never computes a new value, always returns null
    /// - {@link #COMPUTE_IF_PRESENT_ALWAYS_COMPUTES_VALUE} - Always computes and stores a new value if key exists
    /// - {@link #COMPUTE_IF_PRESENT_ALWAYS_RETURNS_NULL} - Always returns null regardless of computed value
    ///
    /// @param key key with which the specified value is to be associated
    /// @param remappingFunction the function to compute a value
    /// @return the new value associated with the specified key, or null if none
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys, or the
    ///     remappingFunction is null
    /// @throws UnsupportedOperationException if this map does not support the 'computeIfPresent' operation
    /// @throws ClassCastException if the class of the specified key or value prevents it from being stored in this map
    /// @throws IllegalArgumentException if some property of the specified key or value prevents it from being stored
    /// @see java.util.Map#computeIfPresent(Object, BiFunction)
    @Override
    public @Nullable V computeIfPresent(final K key,
                              final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (supportsMethod(MapMethods.COMPUTE_IF_PRESENT)) {
            validateKey(key);
            Objects.requireNonNull(remappingFunction);
            V computed;
            if (hasBreak(COMPUTE_IF_PRESENT_NEVER_COMPUTES_VALUE)) {
                computed = null;
            } else if (hasBreak(COMPUTE_IF_PRESENT_ALWAYS_COMPUTES_VALUE)) {
                // Computes even when the key is absent, which computeIfPresent must not.
                computed = validateValue(remappingFunction.apply(key, map.get(key)));
                map.put(key, computed);
            } else {
                // A null result removes the mapping (Map#computeIfPresent), so it is not validated.
                computed = map.computeIfPresent(key, remappingFunction);
            }
            if (hasBreak(COMPUTE_IF_PRESENT_ALWAYS_RETURNS_NULL)) {
                return null;
            } else {
                return computed;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: computeIfPresent(Object, Function)");
        }
    }

    /// Computes a new mapping for the specified key from its current mapped value, or null if it has none.
    ///
    /// ## Supported Breaks
    /// - {@link #COMPUTE_DOES_NOT_COMPUTE_VALUE} - Does not compute a new value, returns the current value
    /// - {@link #COMPUTE_ALWAYS_RETURNS_NULL} - Always returns null regardless of computed value
    ///
    /// @param key key with which the specified value is to be associated
    /// @param remappingFunction the function to compute a value
    /// @return the new value associated with the specified key, or null if none
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys, or the
    ///     remappingFunction is null
    /// @throws UnsupportedOperationException if this map does not support the 'compute' operation
    /// @throws ClassCastException if the class of the specified key or value prevents it from being stored in this map
    /// @throws IllegalArgumentException if some property of the specified key or value prevents it from being stored
    /// @see java.util.Map#compute(Object, BiFunction)
    @Override
    public @Nullable V compute(final K key,
                     final BiFunction<? super K, ? super @Nullable V, ? extends V> remappingFunction) {
        if (supportsMethod(MapMethods.COMPUTE)) {
            validateKey(key);
            Objects.requireNonNull(remappingFunction);
            V computed;
            if (hasBreak(COMPUTE_DOES_NOT_COMPUTE_VALUE)) {
                computed = map.get(key);
            } else {
                // A null result removes the mapping (Map#compute), so it is not validated.
                computed = map.compute(key, remappingFunction);
            }
            if (hasBreak(COMPUTE_ALWAYS_RETURNS_NULL)) {
                return null;
            } else {
                return computed;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: compute(Object, Function)");
        }
    }

    /// Associates the specified key with the given value if it is absent or null, or else merges the two values with
    /// the given remapping function, removing the entry if the result is null.
    ///
    /// ## Supported Breaks
    /// - {@link #MERGE_DOES_NOT_MERGE_THE_VALUES} - Does not merge values, returns the current value
    /// - {@link #MERGE_ALWAYS_RETURNS_NULL} - Always returns null regardless of merged value
    ///
    /// @param key key with which the resulting value is to be associated
    /// @param value the non-null value to be merged with the existing value associated with the key or to be associated
    ///     with the key, if no existing value or a null value is associated with the key
    /// @param remappingFunction the function to recompute a value if present
    /// @return the new value associated with the specified key, or null if no value is associated with the key
    /// @throws UnsupportedOperationException if this map does not support the 'merge' operation
    /// @throws ClassCastException if the class of the specified key or value prevents it from being stored in this map
    /// @throws NullPointerException if the specified key is null and this map does not permit null keys or the value or
    ///     remappingFunction is null
    /// @throws IllegalArgumentException if some property of the specified key or value prevents it from being stored
    /// @see java.util.Map#merge(Object, Object, BiFunction)
    @Override
    public @Nullable V merge(final K key, final V value,
                   final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (supportsMethod(MapMethods.MERGE)) {
            validateKey(key);
            validateValue(value);
            Objects.requireNonNull(remappingFunction);
            V merged;
            if (hasBreak(MERGE_DOES_NOT_MERGE_THE_VALUES)) {
                merged = map.get(key);
            } else {
                // A null result removes the mapping (Map#merge), so it is not validated.
                merged = map.merge(key, value, remappingFunction);
            }

            if (hasBreak(MERGE_ALWAYS_RETURNS_NULL)) {
                return null;
            } else {
                return merged;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported method: merge(Object, Object, BiFunction)");
        }
    }

    /// Validates a key according to the null key policy.
    ///
    /// @param <T> the type of the key
    /// @param key the key to validate
    /// @throws NullPointerException if the key is `null` and null keys are not permitted
    /// @since 1.0
    protected <T> void validateKey(final T key) {
        if (!permitsNullKeys() && key == null) {
            throw new  NullPointerException("key is null");
        }
    }

    /// Validates a value according to the null value policy.
    ///
    /// @param <T> the type of the value
    /// @param value the value to validate
    /// @return the validated value
    /// @throws NullPointerException if the value is `null` and null values are not permitted
    /// @since 1.0
    protected <T> T validateValue(final T value) {
        if (!permitsNullValues() && value == null) {
            throw new  NullPointerException("Value is null");
        }
        return value;
    }

    /// Validates a map entry according to the null key and value policies.
    ///
    /// @param entry the entry to validate; must not be `null`
    /// @return the validated entry
    /// @throws NullPointerException if the entry's key is `null` and null keys are not permitted,
    ///         or if the entry's value is `null` and null values are not permitted
    /// @since 1.0
    protected Entry<K, V> validateEntry(final Entry<K, V> entry) {
        if (!permitsNullKeys() && entry.getKey() == null) {
            throw new NullPointerException("Key is null");
        }
        if (!permitsNullValues() && entry.getValue() == null) {
            throw new NullPointerException("Value is null");
        }
        return entry;
    }

    /// Creates a `BreakableMap` that wraps an existing map with the specified configuration.
    ///
    /// This static factory method creates a `BreakableMap` without using the `Builder`.
    ///
    /// @param <K> the type of keys maintained by the map
    /// @param <V> the type of mapped values
    /// @param map the map to wrap; must not be `null`
    /// @param breaks the set of breaks to apply; must not be `null`
    /// @param statuses the method statuses to apply; must not be `null`
    /// @param permits the bitwise combination of permitted features
    /// @param isSafe whether the map is safe for concurrent access
    /// @return a new `BreakableMap` wrapping the specified map
    /// @throws NullPointerException if `map`, `breaks`, or `statuses` is `null`
    /// @since 1.0
    public static <K, V> BreakableMap<K, V> wrap(final Map<K, V> map,
                                                 final Set<Break> breaks,
                                                 final Map<InterfaceMethod, MethodStatus> statuses,
                                                 final int permits,
                                                 final boolean isSafe) {
        return new BreakableMap<>(map, breaks, statuses, permits, isSafe);
    }

    /// Abstract base class for implementing builders for `BreakableMap` and its subclasses.
    ///
    /// It holds the entries to be added and the permits (whether null keys and values are allowed).
    ///
    /// @param <B> the type of the builder subclass for fluent API support
    /// @param <M> the type of map being built, extending `BreakableMap`
    /// @param <K> the type of keys in the map
    /// @param <V> the type of values in the map
    /// @since 1.0
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, M, K, V>,
                                                  M extends BreakableMap<K, V>, K, V>
        extends AbstractBreakableBuilder<B, M>
        implements BreakableMapBuilder<M, K, V> {

        /// The map that will be used to create the breakable map.
        private final Map<K, V> elements;

        private int permits = DEFAULT_PERMITS;

        /// Constructs a new `AbstractBuilder` instance that holds no entries.
        protected AbstractBuilder() {
            this.elements = new LinkedHashMap<>();
        }

        /// Constructs a new `AbstractBuilder` instance that holds a copy of the entries of a map.
        ///
        /// @param map the map containing the initial key-value pairs to populate the builder; must not be `null`.
        /// @throws NullPointerException if the `map` is `null`.
        /// @since 1.0
        protected AbstractBuilder(final Map<K, V> map) {
            this.elements = new LinkedHashMap<>(map);
        }

        /// Constructs a new `AbstractBuilder` instance with a copy of the entries and permits of another.
        ///
        /// @param other the builder to copy from; must not be `null`.
        /// @throws NullPointerException if `other` is `null`.
        /// @since 1.0
        protected AbstractBuilder(final AbstractBuilder<B, M, K, V>  other) {
            super(other);
            this.elements = new LinkedHashMap<>(other.elements);
            this.permits = other.permits;
        }

        /// Returns the underlying map being used by this builder.
        /// @return the map
        /// @since 1.0
        protected Map<K, V> elements() {
            return elements;
        }

        /// Returns whether this builder permits null keys.
        /// @return true if null keys are permitted
        /// @since 1.0
        protected boolean permitsNullKeys() {
            return (permits & PERMITS_NULL_KEYS) != 0;
        }

        /// Returns whether this builder permits null values.
        /// @return true if null values are permitted
        /// @since 1.0
        protected boolean permitsNullValues() {
            return (permits & PERMITS_NULL_VALUES) != 0;
        }

        /// Configures the map to not permit null keys.
        /// @return this builder for method chaining
        /// @since 1.0
        public B doesNotPermitNullKeys() {
            this.permits &= ~PERMITS_NULL_KEYS;
            return self();
        }

        /// Configures the map to not permit null values.
        /// @return this builder for method chaining
        /// @since 1.0
        public B doesNotPermitNullValues() {
            this.permits &= ~PERMITS_NULL_VALUES;
            return self();
        }

        /// Adds a key-value pair to the map being built.
        ///
        /// @param key the key to add
        /// @param value the value to add
        /// @return this builder for method chaining
        /// @since 1.0
        public B add(final K key, final V value) {
            elements.put(key, value);
            return self();
        }

        /// Adds every entry of a map to the builder.
        ///
        /// @param entries the entries to add; must not be null
        /// @return this builder for method chaining
        public B addAll(final Map<K, V> entries) {
            elements.putAll(entries);
            return self();
        }

        /// Builds a breakable map that holds the builder's entries.
        ///
        /// @return a new breakable map
        @Override
        public final M build() {
            return build(Collections.emptyMap());
        }

        /// Retrieves the permission rules associated with this builder.
        /// @return the bitwise combination of the permits for this builder.
        protected int permits() {
            return permits;
        }
    }

    /// Builder for creating BreakableMap instances.
    ///
    /// @param <K> the type of keys
    /// @param <V> the type of values
    /// @since 1.0
    public static class Builder<K, V> extends AbstractBuilder<Builder<K, V>, BreakableMap<K, V>, K, V> {

        /// Creates a builder with an empty HashMap.
        /// @since 1.0
        public Builder() {
            super(new HashMap<>());
        }

        /// Creates a builder and initializes it with the elements provided.
        ///
        /// @param map the initial map; must not be null
        /// @since 1.0
        public Builder(final Map<K, V> map) {
            super(map);
        }

        /// Creates a builder by copying another builder.
        ///
        /// @param other the builder to copy; must not be null
        /// @since 1.0
        public Builder(final Builder<K, V> other) {
            super(other);
        }

        /// @since 1.0
        @Override
        public Builder<K, V> self() {
            return this;
        }

        /// Returns a copy of this builder.
        ///
        /// @return a copy of this builder
        /// @since 1.0
        @Override
        public Builder<K, V> copy() {
            return new BreakableMap.Builder<>(this);
        }

        @Override
        public BreakableMap<K, V> copy(final BreakableMap<K, V> other) {
            return new BreakableMap<>(other);
        }

        /// Builds and returns a new `BreakableMap` instance.
        ///
        /// @return a new `BreakableMap`
        /// @since 1.0
        @Override
        public BreakableMap<K, V> build(final Map<K, V> entries) {
            Map<K, V> map = new HashMap<>(elements());
            map.putAll(entries);
            return new BreakableMap<>(map, new HashSet<>(breaks()), new HashMap<>(methodStatuses()), permits(),
                    isSafe());
        }
    }

    /// Creates a map provider for instances of `BreakableMap` that have no breaks.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @param keyProvider the provider of keys
    /// @param valueProvider the provider of values
    /// @return a map provider for breakable maps
    public static <K, V> MapProvider<K, V, BreakableMap<K, V>> mapProvider(
            final ObjectProvider<K> keyProvider,
            final ObjectProvider<V> valueProvider) {
        return mapProvider(new Builder<>(), keyProvider, valueProvider);
    }

    /// Creates a map provider for instances of `BreakableMap` built by a builder.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @param builder the builder that configures each instance; it is copied, so later changes to it are not seen
    /// @param keyProvider the provider of keys
    /// @param valueProvider the provider of values
    /// @return a map provider for breakable maps
    public static <K, V> MapProvider<K, V, BreakableMap<K, V>> mapProvider(
            final Builder<K, V> builder,
            final ObjectProvider<K> keyProvider,
            final ObjectProvider<V> valueProvider) {
        return BreakableBuilder.mapProvider(builder.copy(), keyProvider, valueProvider);
    }

    /// Mixin interface that adds an implementation of the `objectProvider()` method that provides instances of
    /// `BreakableMap` that do not have any breaks applied.
    ///
    /// @param <K> key type
    /// @param <V> value type
    /// @since 1.0
    public interface WithProvider<K, V> extends MapProviderSupport<BreakableMap<K, V>, K, V> {
        /// Returns a `MapProvider` for `BreakableMap` instances.
        ///
        /// @return a map objectProvider for breakable maps
        @Override
        default MapProvider<K, V, BreakableMap<K, V>> provider() {
            return BreakableMap.mapProvider(keyProvider(), valueProvider());
        }
    }
}
