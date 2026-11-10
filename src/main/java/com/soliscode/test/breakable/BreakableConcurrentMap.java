package com.soliscode.test.breakable;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.map.MapMethods;

/// A [ConcurrentMap] that can be broken for testing: a [BreakableMap] over a concurrent store (a [ConcurrentHashMap]
/// unless another is given), with breaks of its own for the methods [ConcurrentMap] declares or re-specifies.
///
/// ## Breaks
///
/// - **Single-thread faults** of `putIfAbsent`, `remove(key, value)` and both `replace` forms (for example
///   [#PUT_IF_ABSENT_IGNORES_EXISTING]), of a `null` function result ([#COMPUTE_KEEPS_ENTRY_ON_NULL_RESULT] and the
///   others), and of null refusal ([#NULL_KEY_THROWS_WRONG_NULL_EXCEPTION], [#NULL_VALUE_THROWS_WRONG_NULL_EXCEPTION],
///   [#NULL_FUNCTION_THROWS_WRONG_NULL_EXCEPTION]).
/// - **Thread-safety faults** (`*_IS_NOT_THREAD_SAFE`): each runs the method as separate steps on the store, the
///   check-then-act that [ConcurrentMap]'s Javadoc calls the method "equivalent to", without its atomicity. With no
///   other thread running, each gives the method's correct results; under contention, updates are lost or a
///   stale check acts. `ThreadSafeConcurrentMapContract` catches them.
///
/// The breaks of [BreakableMap] apply as well.
///
/// ## Nulls
///
/// The map refuses null keys and values, as its store does, whichever way it is made. Every method checks its
/// arguments (support, then key, value and function) before any break acts.
///
/// ## Thread safety
///
/// The map is as thread safe as its store, unless a thread-safety break is set. The breaks and the method support
/// are set before the map is shared.
///
/// ## Usage Examples
///
/// ```java
/// BreakableConcurrentMap<String, Integer> map = new BreakableConcurrentMap.Builder<String, Integer>()
///     .addBreak(BreakableConcurrentMap.COMPUTE_IS_NOT_THREAD_SAFE)
///     .build();
/// ```
///
/// @param <K> the type of keys maintained by this concurrent map
/// @param <V> the type of mapped values
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableMap
/// @see ConcurrentMap
/// @see Break
public class BreakableConcurrentMap<K, V> extends BreakableMap<K, V> implements ConcurrentMap<K, V>, Serializable {

    // ========== putIfAbsent ==========

    /// `putIfAbsent` puts the value even when the key is mapped, and returns the old value.
    public static final Break PUT_IF_ABSENT_IGNORES_EXISTING =
            new Break("PUT_IF_ABSENT_IGNORES_EXISTING");

    /// `putIfAbsent` acts as it should, but always returns `null`.
    public static final Break PUT_IF_ABSENT_ALWAYS_RETURNS_NULL =
            new Break("PUT_IF_ABSENT_ALWAYS_RETURNS_NULL");

    /// `putIfAbsent` returns the current value, but never adds a mapping.
    public static final Break PUT_IF_ABSENT_DOES_NOT_ADD_PAIR =
            new Break("PUT_IF_ABSENT_DOES_NOT_ADD_PAIR");

    /// `putIfAbsent` is `if (!containsKey(k)) return put(k, v); else return get(k);` on the store, in steps: two
    /// threads can both find the key absent, and both put.
    ///
    /// @since 0.1.2
    public static final Break PUT_IF_ABSENT_IS_NOT_THREAD_SAFE =
            new Break("PUT_IF_ABSENT_IS_NOT_THREAD_SAFE");

    // ========== remove(key, value) ==========

    /// `remove(key, value)` removes the key whatever its value.
    public static final Break REMOVE_TWO_ARG_IGNORES_VALUE =
            new Break("REMOVE_TWO_ARG_IGNORES_VALUE");

    /// `remove(key, value)` removes nothing, and always returns `false`.
    public static final Break REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE =
            new Break("REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE");

    /// `remove(key, value)` returns whether the key is mapped to the value, but removes nothing.
    public static final Break REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR =
            new Break("REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR");

    /// `remove(key, value)` checks the value, then removes the key, in steps: it can remove a value another thread
    /// put in between.
    ///
    /// @since 0.1.2
    public static final Break REMOVE_TWO_ARG_IS_NOT_THREAD_SAFE =
            new Break("REMOVE_TWO_ARG_IS_NOT_THREAD_SAFE");

    // ========== replace(key, oldValue, newValue) ==========

    /// `replace(key, oldValue, newValue)` replaces any value of a mapped key.
    public static final Break REPLACE_THREE_ARG_IGNORES_OLD_VALUE =
            new Break("REPLACE_THREE_ARG_IGNORES_OLD_VALUE");

    /// `replace(key, oldValue, newValue)` replaces nothing, and always returns `false`.
    public static final Break REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE =
            new Break("REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE");

    /// `replace(key, oldValue, newValue)` returns whether it would replace, but replaces nothing.
    public static final Break REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE =
            new Break("REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE");

    /// `replace(key, oldValue, newValue)` checks the old value, then puts, in steps: two threads can both see the
    /// old value and both replace it, so one update is lost.
    ///
    /// @since 0.1.2
    public static final Break REPLACE_THREE_ARG_IS_NOT_THREAD_SAFE =
            new Break("REPLACE_THREE_ARG_IS_NOT_THREAD_SAFE");

    // ========== replace(key, value) ==========

    /// `replace(key, value)` acts as it should, but always returns `null`.
    public static final Break REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL =
            new Break("REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL");

    /// `replace(key, value)` returns the current value, but replaces nothing.
    public static final Break REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE =
            new Break("REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE");

    /// `replace(key, value)` puts the value when the key is absent, and returns `null`.
    ///
    /// @since 0.1.2
    public static final Break REPLACE_TWO_ARG_ADDS_ABSENT_KEY =
            new Break("REPLACE_TWO_ARG_ADDS_ABSENT_KEY");

    /// `replace(key, value)` is `if (containsKey(k)) return put(k, v); else return null;` on the store, in steps:
    /// it can put back a key another thread removed in between.
    ///
    /// @since 0.1.2
    public static final Break REPLACE_TWO_ARG_IS_NOT_THREAD_SAFE =
            new Break("REPLACE_TWO_ARG_IS_NOT_THREAD_SAFE");

    // ========== The compute methods and merge ==========

    /// `computeIfAbsent` gets, applies the function, then puts, in steps: two threads can both compute a value for
    /// an absent key, and each return its own.
    ///
    /// @since 0.1.2
    public static final Break COMPUTE_IF_ABSENT_IS_NOT_THREAD_SAFE =
            new Break("COMPUTE_IF_ABSENT_IS_NOT_THREAD_SAFE");

    /// `computeIfAbsent` throws `NullPointerException` when the function returns `null`, where it should record no
    /// mapping and return `null`.
    ///
    /// @since 0.1.2
    public static final Break COMPUTE_IF_ABSENT_THROWS_ON_NULL_RESULT =
            new Break("COMPUTE_IF_ABSENT_THROWS_ON_NULL_RESULT");

    /// `computeIfPresent` gets, applies the function, then puts or removes, in steps: an update another thread made
    /// in between is lost.
    ///
    /// @since 0.1.2
    public static final Break COMPUTE_IF_PRESENT_IS_NOT_THREAD_SAFE =
            new Break("COMPUTE_IF_PRESENT_IS_NOT_THREAD_SAFE");

    /// `computeIfPresent` keeps the old value when the function returns `null`, where it should remove the mapping.
    ///
    /// @since 0.1.2
    public static final Break COMPUTE_IF_PRESENT_KEEPS_ENTRY_ON_NULL_RESULT =
            new Break("COMPUTE_IF_PRESENT_KEEPS_ENTRY_ON_NULL_RESULT");

    /// `compute` gets, applies the function, then puts or removes, in steps: an update another thread made in
    /// between is lost.
    ///
    /// @since 0.1.2
    public static final Break COMPUTE_IS_NOT_THREAD_SAFE =
            new Break("COMPUTE_IS_NOT_THREAD_SAFE");

    /// `compute` keeps the old value when the function returns `null`, where it should remove the mapping.
    ///
    /// @since 0.1.2
    public static final Break COMPUTE_KEEPS_ENTRY_ON_NULL_RESULT =
            new Break("COMPUTE_KEEPS_ENTRY_ON_NULL_RESULT");

    /// `compute` on an absent key throws `NullPointerException` when the function returns `null`, where it should
    /// leave the key absent and return `null`.
    ///
    /// @since 0.1.2
    public static final Break COMPUTE_THROWS_ON_NULL_RESULT_FOR_ABSENT_KEY =
            new Break("COMPUTE_THROWS_ON_NULL_RESULT_FOR_ABSENT_KEY");

    /// `merge` gets, then puts the value or the function's result (or removes the key), in steps: an update another
    /// thread made in between is lost.
    ///
    /// @since 0.1.2
    public static final Break MERGE_IS_NOT_THREAD_SAFE =
            new Break("MERGE_IS_NOT_THREAD_SAFE");

    /// `merge` keeps the old value when the function returns `null`, where it should remove the mapping.
    ///
    /// @since 0.1.2
    public static final Break MERGE_KEEPS_ENTRY_ON_NULL_RESULT =
            new Break("MERGE_KEEPS_ENTRY_ON_NULL_RESULT");

    // ========== replaceAll, getOrDefault, forEach and the views ==========

    /// `replaceAll` gets each value, applies the function, then puts the result unconditionally, in steps: an update
    /// another thread made to the entry in between is lost.
    ///
    /// @since 0.1.2
    public static final Break REPLACE_ALL_IS_NOT_THREAD_SAFE =
            new Break("REPLACE_ALL_IS_NOT_THREAD_SAFE");

    /// `getOrDefault` is `containsKey(k) ? get(k) : defaultValue`, in steps: it returns `null` for a key another
    /// thread removed in between.
    ///
    /// @since 0.1.2
    public static final Break GET_OR_DEFAULT_IS_NOT_THREAD_SAFE =
            new Break("GET_OR_DEFAULT_IS_NOT_THREAD_SAFE");

    /// `forEach` walks the keys and gets each value, in steps: it passes `null` for a key another thread removed in
    /// between.
    ///
    /// @since 0.1.2
    public static final Break FOR_EACH_IS_NOT_THREAD_SAFE =
            new Break("FOR_EACH_IS_NOT_THREAD_SAFE");

    /// `entrySet().removeIf` tests each entry, then removes its key through the iterator, as `Collection`'s default
    /// does: it can remove a value another thread put in between.
    ///
    /// @since 0.1.2
    public static final Break ENTRY_SET_REMOVE_IF_IS_NOT_THREAD_SAFE =
            new Break("ENTRY_SET_REMOVE_IF_IS_NOT_THREAD_SAFE");

    /// `values().removeIf` tests each value, then removes its key through the iterator, as `Collection`'s default
    /// does: it can remove a value another thread put in between.
    ///
    /// @since 0.1.2
    public static final Break VALUES_REMOVE_IF_IS_NOT_THREAD_SAFE =
            new Break("VALUES_REMOVE_IF_IS_NOT_THREAD_SAFE");

    // ========== Nulls ==========

    /// A null key throws `IllegalArgumentException`, not `NullPointerException`, in `getOrDefault`, `putIfAbsent`,
    /// `remove(key, value)`, both `replace` forms, `computeIfAbsent`, `computeIfPresent`, `compute` and `merge`.
    ///
    /// @since 0.1.2
    public static final Break NULL_KEY_THROWS_WRONG_NULL_EXCEPTION =
            new Break("CONCURRENT_MAP_NULL_KEY_THROWS_WRONG_NULL_EXCEPTION");

    /// A null value throws `IllegalArgumentException`, not `NullPointerException`, in `putIfAbsent`,
    /// `remove(key, value)`, both values of `replace(key, oldValue, newValue)`, `replace(key, value)`, `merge`'s value
    /// and `replaceAll`'s function results. `getOrDefault`'s default may be `null`, and is not checked.
    ///
    /// @since 0.1.2
    public static final Break NULL_VALUE_THROWS_WRONG_NULL_EXCEPTION =
            new Break("CONCURRENT_MAP_NULL_VALUE_THROWS_WRONG_NULL_EXCEPTION");

    /// A null function or action throws `IllegalArgumentException`, not `NullPointerException`, in `forEach`,
    /// `replaceAll`, `computeIfAbsent`, `computeIfPresent`, `compute` and `merge`.
    ///
    /// @since 0.1.2
    public static final Break NULL_FUNCTION_THROWS_WRONG_NULL_EXCEPTION =
            new Break("CONCURRENT_MAP_NULL_FUNCTION_THROWS_WRONG_NULL_EXCEPTION");

    /// The permits of a map that refuses null keys and values, as a concurrent map's store does.
    protected static final int NO_NULL_PERMITS = DEFAULT_PERMITS & ~(PERMITS_NULL_KEYS | PERMITS_NULL_VALUES);

    @Serial
    private static final long serialVersionUID = 2L;

    // ========== Constructors ==========

    /// Creates an empty map, backed by a [ConcurrentHashMap], with no breaks, refusing null keys and values.
    public BreakableConcurrentMap() {
        this(new ConcurrentHashMap<>(), new HashSet<>(), new HashMap<>(), NO_NULL_PERMITS);
    }

    /// Creates a copy of another map: its mappings in a new [ConcurrentHashMap], its breaks, method support and
    /// permits. The copy does not share its store with the original.
    ///
    /// @param other the map to copy
    public BreakableConcurrentMap(final BreakableConcurrentMap<K, V> other) {
        this(new ConcurrentHashMap<>(other.concurrentStore()), new HashSet<>(other.breaks()),
                new HashMap<>(other.methodStatuses()), other.permits());
    }

    /// Creates a map over a store, with breaks, method support and permits.
    ///
    /// @param concurrentMapArg the store, which the map delegates to
    /// @param breaks the breaks
    /// @param methodStatuses the method support
    /// @param permits the permits ([BreakableMap]'s `PERMITS_*` flags)
    protected BreakableConcurrentMap(
            final ConcurrentMap<K, V> concurrentMapArg,
            final Set<Break> breaks,
            final Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int permits) {
        super(concurrentMapArg, breaks, methodStatuses, permits, true);
    }

    // ========== The store ==========

    /// Returns the store, which is always a [ConcurrentMap] for this class.
    ///
    /// @return the store
    private ConcurrentMap<K, V> concurrentStore() {
        return (ConcurrentMap<K, V>) store();
    }

    /// Copies the store into a new [ConcurrentHashMap], so that a clone has a store of its own.
    ///
    /// @param storeArg the store to copy
    /// @return a new store holding the same mappings
    @Override
    protected Map<K, V> copyOfStore(final Map<K, V> storeArg) {
        return new ConcurrentHashMap<>(storeArg);
    }

    // ========== Argument checks ==========

    private void checkKey(final @Nullable Object key) {
        if (key == null && !permitsNullKeys()) {
            throw hasBreak(NULL_KEY_THROWS_WRONG_NULL_EXCEPTION)
                    ? new IllegalArgumentException("key is null") : new NullPointerException("key is null");
        }
    }

    private void checkValue(final @Nullable Object value) {
        if (value == null && !permitsNullValues()) {
            throw hasBreak(NULL_VALUE_THROWS_WRONG_NULL_EXCEPTION)
                    ? new IllegalArgumentException("value is null") : new NullPointerException("value is null");
        }
    }

    private void checkFunction(final @Nullable Object function) {
        if (function == null) {
            throw hasBreak(NULL_FUNCTION_THROWS_WRONG_NULL_EXCEPTION)
                    ? new IllegalArgumentException("function is null") : new NullPointerException("function is null");
        }
    }

    // ========== Map methods that ConcurrentMap re-specifies ==========

    /// Returns the value of a key, or the default if it has none.
    ///
    /// Breaks: [#GET_OR_DEFAULT_IS_NOT_THREAD_SAFE], [#NULL_KEY_THROWS_WRONG_NULL_EXCEPTION], and [BreakableMap]'s.
    ///
    /// @param key the key
    /// @param defaultValue the value to return if the key has none
    /// @return the value, or the default
    @Override
    public @Nullable V getOrDefault(final Object key, final V defaultValue) {
        checkMethodSupport(MapMethods.GET_OR_DEFAULT);
        checkKey(key);
        if (hasBreak(GET_OR_DEFAULT_IS_NOT_THREAD_SAFE)) {
            ConcurrentMap<K, V> store = concurrentStore();
            return store.containsKey(key) ? store.get(key) : defaultValue;
        }
        return super.getOrDefault(key, defaultValue);
    }

    /// Passes each mapping to the action.
    ///
    /// Breaks: [#FOR_EACH_IS_NOT_THREAD_SAFE], [#NULL_FUNCTION_THROWS_WRONG_NULL_EXCEPTION], and [BreakableMap]'s.
    ///
    /// @param action the action
    @Override
    // NullAway: FOR_EACH_IS_NOT_THREAD_SAFE passes null for a key removed meanwhile, on purpose.
    @SuppressWarnings("NullAway")
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        checkMethodSupport(MapMethods.FOR_EACH);
        checkFunction(action);
        if (hasBreak(FOR_EACH_IS_NOT_THREAD_SAFE)) {
            ConcurrentMap<K, V> store = concurrentStore();
            for (final K key : store.keySet()) {
                action.accept(key, store.get(key));
            }
            return;
        }
        super.forEach(action);
    }

    /// Replaces each value with the function's result for it. With none of [BreakableMap]'s `replaceAll` breaks set,
    /// it calls the store's `replaceAll`, which replaces each entry atomically.
    ///
    /// Breaks: [#REPLACE_ALL_IS_NOT_THREAD_SAFE], [#NULL_FUNCTION_THROWS_WRONG_NULL_EXCEPTION],
    /// [#NULL_VALUE_THROWS_WRONG_NULL_EXCEPTION] (for a `null` result), and [BreakableMap]'s.
    ///
    /// @param function the function
    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        checkMethodSupport(MapMethods.REPLACE_ALL);
        checkFunction(function);
        BiFunction<K, V, V> checked = (k, v) -> {
            V result = function.apply(k, v);
            checkValue(result);
            return result;
        };
        if (hasBreak(REPLACE_ALL_IS_NOT_THREAD_SAFE)) {
            // Steps on the store: get, apply, then an unconditional put. (Not the entries' setValue, which a
            // ConcurrentSkipListMap's entries do not support.)
            ConcurrentMap<K, V> store = concurrentStore();
            for (final K key : store.keySet()) {
                V value = store.get(key);
                if (value != null) {
                    store.put(key, checked.apply(key, value));
                }
            }
            return;
        }
        if (hasBreak(REPLACE_ALL_DOES_NOT_REPLACE_ANY_PAIRS) || hasBreak(REPLACE_ALL_SKIPS_THE_FIRST_PAIR)
                || hasBreak(REPLACE_ALL_SKIPS_THE_LAST_PAIR)) {
            super.replaceAll(checked);
            return;
        }
        concurrentStore().replaceAll(checked);
    }

    /// Associates the value with the key, if the key has no value.
    ///
    /// Breaks: [#PUT_IF_ABSENT_IGNORES_EXISTING], [#PUT_IF_ABSENT_ALWAYS_RETURNS_NULL],
    /// [#PUT_IF_ABSENT_DOES_NOT_ADD_PAIR], [#PUT_IF_ABSENT_IS_NOT_THREAD_SAFE], the `NULL_*` breaks, and
    /// [BreakableMap]'s.
    ///
    /// @param key the key
    /// @param value the value
    /// @return the key's previous value, or `null` if it had none
    @Override
    public @Nullable V putIfAbsent(final K key, final V value) {
        checkMethodSupport(MapMethods.PUT_IF_ABSENT);
        checkKey(key);
        checkValue(value);
        ConcurrentMap<K, V> store = concurrentStore();
        if (hasBreak(PUT_IF_ABSENT_ALWAYS_RETURNS_NULL)) {
            store.putIfAbsent(key, value);
            return null;
        }
        if (hasBreak(PUT_IF_ABSENT_DOES_NOT_ADD_PAIR)) {
            return store.get(key);
        }
        if (hasBreak(PUT_IF_ABSENT_IGNORES_EXISTING)) {
            return store.put(key, value);
        }
        if (hasBreak(PUT_IF_ABSENT_IS_NOT_THREAD_SAFE)) {
            return !store.containsKey(key) ? store.put(key, value) : store.get(key);
        }
        return super.putIfAbsent(key, value);
    }

    /// Removes the key, if it is mapped to the value.
    ///
    /// Breaks: [#REMOVE_TWO_ARG_IGNORES_VALUE], [#REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE],
    /// [#REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR], [#REMOVE_TWO_ARG_IS_NOT_THREAD_SAFE], the `NULL_*` breaks, and
    /// [BreakableMap]'s.
    ///
    /// @param key the key
    /// @param value the value the key must be mapped to
    /// @return `true` if the key was removed
    @Override
    public boolean remove(final Object key, final Object value) {
        checkMethodSupport(MapMethods.REMOVE_TWO_ARG);
        checkKey(key);
        checkValue(value);
        ConcurrentMap<K, V> store = concurrentStore();
        if (hasBreak(REMOVE_TWO_ARG_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(REMOVE_TWO_ARG_DOES_NOT_REMOVE_PAIR)) {
            return Objects.equals(store.get(key), value);
        }
        if (hasBreak(REMOVE_TWO_ARG_IGNORES_VALUE)) {
            return store.remove(key) != null;
        }
        if (hasBreak(REMOVE_TWO_ARG_IS_NOT_THREAD_SAFE)) {
            if (store.containsKey(key) && Objects.equals(store.get(key), value)) {
                store.remove(key);
                return true;
            }
            return false;
        }
        return super.remove(key, value);
    }

    /// Replaces the key's value, if it is mapped to the old value.
    ///
    /// Breaks: [#REPLACE_THREE_ARG_IGNORES_OLD_VALUE], [#REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE],
    /// [#REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE], [#REPLACE_THREE_ARG_IS_NOT_THREAD_SAFE], the `NULL_*` breaks, and
    /// [BreakableMap]'s.
    ///
    /// @param key the key
    /// @param oldValue the value the key must be mapped to
    /// @param newValue the value to map it to
    /// @return `true` if the value was replaced
    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        checkMethodSupport(MapMethods.REPLACE_THREE_ARG);
        checkKey(key);
        checkValue(oldValue);
        checkValue(newValue);
        ConcurrentMap<K, V> store = concurrentStore();
        if (hasBreak(REPLACE_THREE_ARG_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        if (hasBreak(REPLACE_THREE_ARG_DOES_NOT_REPLACE_VALUE)) {
            return Objects.equals(store.get(key), oldValue);
        }
        if (hasBreak(REPLACE_THREE_ARG_IGNORES_OLD_VALUE)) {
            if (store.containsKey(key)) {
                store.put(key, newValue);
                return true;
            }
            return false;
        }
        if (hasBreak(REPLACE_THREE_ARG_IS_NOT_THREAD_SAFE)) {
            if (store.containsKey(key) && Objects.equals(store.get(key), oldValue)) {
                store.put(key, newValue);
                return true;
            }
            return false;
        }
        return super.replace(key, oldValue, newValue);
    }

    /// Replaces the key's value, if it has one.
    ///
    /// Breaks: [#REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL], [#REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE],
    /// [#REPLACE_TWO_ARG_ADDS_ABSENT_KEY], [#REPLACE_TWO_ARG_IS_NOT_THREAD_SAFE], the `NULL_*` breaks, and
    /// [BreakableMap]'s.
    ///
    /// @param key the key
    /// @param value the value to map it to
    /// @return the key's previous value, or `null` if it had none
    @Override
    public @Nullable V replace(final K key, final V value) {
        checkMethodSupport(MapMethods.REPLACE_TWO_ARG);
        checkKey(key);
        checkValue(value);
        ConcurrentMap<K, V> store = concurrentStore();
        if (hasBreak(REPLACE_TWO_ARG_ALWAYS_RETURNS_NULL)) {
            store.replace(key, value);
            return null;
        }
        if (hasBreak(REPLACE_TWO_ARG_DOES_NOT_REPLACE_VALUE)) {
            return store.get(key);
        }
        if (hasBreak(REPLACE_TWO_ARG_ADDS_ABSENT_KEY)) {
            return store.put(key, value);
        }
        if (hasBreak(REPLACE_TWO_ARG_IS_NOT_THREAD_SAFE)) {
            return store.containsKey(key) ? store.put(key, value) : null;
        }
        return super.replace(key, value);
    }

    /// Computes a value for the key from its current value, or `null` if it has none, and maps it (or removes the
    /// key, for a `null` result).
    ///
    /// Breaks: [#COMPUTE_IS_NOT_THREAD_SAFE], [#COMPUTE_KEEPS_ENTRY_ON_NULL_RESULT],
    /// [#COMPUTE_THROWS_ON_NULL_RESULT_FOR_ABSENT_KEY], the `NULL_*` breaks, and [BreakableMap]'s.
    ///
    /// @param key the key
    /// @param remappingFunction the function
    /// @return the key's new value, or `null` if it has none
    @Override
    public @Nullable V compute(final K key,
                               final BiFunction<? super K, ? super @Nullable V, ? extends V> remappingFunction) {
        checkMethodSupport(MapMethods.COMPUTE);
        checkKey(key);
        checkFunction(remappingFunction);
        if (hasBreak(COMPUTE_IS_NOT_THREAD_SAFE)) {
            ConcurrentMap<K, V> store = concurrentStore();
            V oldValue = store.get(key);
            V newValue = remappingFunction.apply(key, oldValue);
            putOrRemove(store, key, oldValue, newValue);
            return newValue;
        }
        if (hasBreak(COMPUTE_KEEPS_ENTRY_ON_NULL_RESULT)) {
            return super.compute(key, (k, v) -> {
                V result = remappingFunction.apply(k, v);
                return result == null ? v : result;
            });
        }
        if (hasBreak(COMPUTE_THROWS_ON_NULL_RESULT_FOR_ABSENT_KEY)) {
            return super.compute(key, (k, v) -> {
                V result = remappingFunction.apply(k, v);
                if (result == null && v == null) {
                    throw new NullPointerException("computed value is null");
                }
                return result;
            });
        }
        return super.compute(key, remappingFunction);
    }

    /// Computes a value for a key that has none, and maps it, unless the result is `null`.
    ///
    /// Breaks: [#COMPUTE_IF_ABSENT_IS_NOT_THREAD_SAFE], [#COMPUTE_IF_ABSENT_THROWS_ON_NULL_RESULT], the `NULL_*`
    /// breaks, and [BreakableMap]'s.
    ///
    /// @param key the key
    /// @param mappingFunction the function
    /// @return the key's value, existing or computed, or `null` if it has none
    @Override
    public @Nullable V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        checkMethodSupport(MapMethods.COMPUTE_IF_ABSENT);
        checkKey(key);
        checkFunction(mappingFunction);
        if (hasBreak(COMPUTE_IF_ABSENT_IS_NOT_THREAD_SAFE)) {
            ConcurrentMap<K, V> store = concurrentStore();
            V oldValue = store.get(key);
            if (oldValue != null) {
                return oldValue;
            }
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                store.put(key, newValue);
            }
            return newValue;
        }
        if (hasBreak(COMPUTE_IF_ABSENT_THROWS_ON_NULL_RESULT)) {
            return super.computeIfAbsent(key, k -> {
                V result = mappingFunction.apply(k);
                if (result == null) {
                    throw new NullPointerException("computed value is null");
                }
                return result;
            });
        }
        return super.computeIfAbsent(key, mappingFunction);
    }

    /// Computes a new value for a key that has one, and maps it (or removes the key, for a `null` result).
    ///
    /// Breaks: [#COMPUTE_IF_PRESENT_IS_NOT_THREAD_SAFE], [#COMPUTE_IF_PRESENT_KEEPS_ENTRY_ON_NULL_RESULT], the
    /// `NULL_*` breaks, and [BreakableMap]'s.
    ///
    /// @param key the key
    /// @param remappingFunction the function
    /// @return the key's new value, or `null` if it has none
    @Override
    public @Nullable V computeIfPresent(final K key,
                                        final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        checkMethodSupport(MapMethods.COMPUTE_IF_PRESENT);
        checkKey(key);
        checkFunction(remappingFunction);
        if (hasBreak(COMPUTE_IF_PRESENT_IS_NOT_THREAD_SAFE)) {
            ConcurrentMap<K, V> store = concurrentStore();
            V oldValue = store.get(key);
            if (oldValue == null) {
                return null;
            }
            V newValue = remappingFunction.apply(key, oldValue);
            putOrRemove(store, key, oldValue, newValue);
            return newValue;
        }
        if (hasBreak(COMPUTE_IF_PRESENT_KEEPS_ENTRY_ON_NULL_RESULT)) {
            return super.computeIfPresent(key, (k, v) -> {
                V result = remappingFunction.apply(k, v);
                return result == null ? v : result;
            });
        }
        return super.computeIfPresent(key, remappingFunction);
    }

    /// Maps a key that has no value to the value, or else maps it to the function's result for its value and the
    /// given one (or removes it, for a `null` result).
    ///
    /// Breaks: [#MERGE_IS_NOT_THREAD_SAFE], [#MERGE_KEEPS_ENTRY_ON_NULL_RESULT], the `NULL_*` breaks, and
    /// [BreakableMap]'s.
    ///
    /// @param key the key
    /// @param value the value to map or merge
    /// @param remappingFunction the function
    /// @return the key's new value, or `null` if it has none
    @Override
    public @Nullable V merge(final K key, final V value,
                             final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        checkMethodSupport(MapMethods.MERGE);
        checkKey(key);
        checkValue(value);
        checkFunction(remappingFunction);
        if (hasBreak(MERGE_IS_NOT_THREAD_SAFE)) {
            ConcurrentMap<K, V> store = concurrentStore();
            V oldValue = store.get(key);
            if (oldValue == null) {
                store.put(key, value);
                return value;
            }
            V newValue = remappingFunction.apply(oldValue, value);
            putOrRemove(store, key, oldValue, newValue);
            return newValue;
        }
        if (hasBreak(MERGE_KEEPS_ENTRY_ON_NULL_RESULT)) {
            return super.merge(key, value, (v, given) -> {
                V result = remappingFunction.apply(v, given);
                return result == null ? v : result;
            });
        }
        return super.merge(key, value, remappingFunction);
    }

    /// The last step of a thread-unsafe compute: puts the new value, or removes the key for a `null` one.
    private void putOrRemove(final ConcurrentMap<K, V> store, final K key, final @Nullable V oldValue,
                             final @Nullable V newValue) {
        if (newValue != null) {
            store.put(key, newValue);
        } else if (oldValue != null) {
            store.remove(key);
        }
    }

    // ========== The views ==========

    /// Returns the entries, as [BreakableMap] does. With [#ENTRY_SET_REMOVE_IF_IS_NOT_THREAD_SAFE], the view's
    /// `removeIf` removes through the iterator.
    ///
    /// @return the entries
    @Override
    public @Nullable Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = super.entrySet();
        if (entries != null && hasBreak(ENTRY_SET_REMOVE_IF_IS_NOT_THREAD_SAFE)) {
            return new IteratorRemoveIfSet<>(entries);
        }
        return entries;
    }

    /// Returns the values, as [BreakableMap] does. With [#VALUES_REMOVE_IF_IS_NOT_THREAD_SAFE], it returns the store's
    /// view, with a `removeIf` that removes through the store's iterator, which removes by key. (The breakable's own
    /// view removes by value, which is accidentally safe for unique values.)
    ///
    /// @return the values
    @Override
    public @Nullable Collection<V> values() {
        if (hasBreak(VALUES_REMOVE_IF_IS_NOT_THREAD_SAFE)) {
            checkMethodSupport(MapMethods.VALUES);
            return new IteratorRemoveIfCollection<>(concurrentStore().values());
        }
        return super.values();
    }

    /// A set view over another, through its iterator, size, `contains` and `remove`; the other methods are
    /// `AbstractSet`'s, and `removeIf` is `Collection`'s default: test each element, then remove it through the
    /// iterator.
    private static final class IteratorRemoveIfSet<E> extends AbstractSet<E> {
        private final Set<E> set;

        IteratorRemoveIfSet(final Set<E> setArg) {
            this.set = setArg;
        }

        @Override
        public Iterator<E> iterator() {
            return set.iterator();
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean contains(final @Nullable Object o) {
            return set.contains(o);
        }

        @Override
        public boolean remove(final @Nullable Object o) {
            return set.remove(o);
        }
    }

    /// A collection view over another, through its iterator, size and `contains`; the other methods are
    /// `AbstractCollection`'s, and `removeIf` is `Collection`'s default: test each element, then remove it through the
    /// iterator.
    private static final class IteratorRemoveIfCollection<E> extends AbstractCollection<E> {
        private final Collection<E> collection;

        IteratorRemoveIfCollection(final Collection<E> collectionArg) {
            this.collection = collectionArg;
        }

        @Override
        public Iterator<E> iterator() {
            return collection.iterator();
        }

        @Override
        public int size() {
            return collection.size();
        }

        @Override
        public boolean contains(final @Nullable Object o) {
            return collection.contains(o);
        }
    }

    // ========== Builder Class ==========

    /// Base class for builders of [BreakableConcurrentMap] and its subclasses. A builder starts refusing null keys and
    /// values, as a concurrent map's store does.
    ///
    /// @param <B> the type of the builder
    /// @param <M> the type of map built
    /// @param <K> the type of keys
    /// @param <V> the type of values
    /// @since 1.0.0
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, M, K, V>,
            M extends BreakableConcurrentMap<K, V>, K, V>
            extends BreakableMap.AbstractBuilder<B, M, K, V> {

        /// Creates a builder with no entries.
        protected AbstractBuilder() {
            super();
            refuseNulls();
        }

        /// Creates a builder that starts with the entries of a map.
        ///
        /// @param map the entries to start with
        protected AbstractBuilder(final Map<K, V> map) {
            super(map);
            refuseNulls();
        }

        /// Creates a copy of another builder, with its permits.
        ///
        /// @param other the builder to copy
        protected AbstractBuilder(final AbstractBuilder<B, M, K, V> other) {
            super(other);
        }

        private void refuseNulls() {
            doesNotPermitNullKeys();
            doesNotPermitNullValues();
        }
    }

    /// Builder class for creating BreakableConcurrentMap instances with fluent configuration.
    ///
    /// It allows setting up a [BreakableConcurrentMap] with specific breaks and method support statuses.
    ///
    /// @param <K> the type of keys maintained by the built concurrent map
    /// @param <V> the type of mapped values
    /// @see BreakableConcurrentMap
    /// @since 1.0.0
    public static class Builder<K, V> extends AbstractBuilder<Builder<K, V>, BreakableConcurrentMap<K, V>, K, V> {

        /// Creates a builder with no entries.
        public Builder() {
            super();
        }

        /// Creates a copy of another builder.
        ///
        /// @param other the builder to copy
        public Builder(final Builder<K, V> other) {
            super(other);
        }

        /// Creates a builder that starts with the entries of a map.
        ///
        /// @param map the entries to start with
        public Builder(final Map<K, V> map) {
            super(map);
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
        public BreakableConcurrentMap<K, V> copy(final BreakableConcurrentMap<K, V> other) {
            return new BreakableConcurrentMap<>(other);
        }

        @Override
        public BreakableConcurrentMap<K, V> build(final Map<K, V> entries) {
            ConcurrentMap<K, V> map = new ConcurrentHashMap<>(elements());
            map.putAll(entries);
            return new BreakableConcurrentMap<>(map, new HashSet<>(breaks()), new HashMap<>(methodStatuses()),
                    permits());
        }
    }

    // ========== Static Factory Methods ==========

    /// Creates a map over a concurrent store, with breaks, refusing null keys and values.
    ///
    /// @param <K> the type of keys maintained by the map
    /// @param <V> the type of mapped values
    /// @param concurrentMap the store
    /// @param breaks the breaks
    /// @return a new map over the store
    /// @since 1.0.0
    public static <K, V> BreakableConcurrentMap<K, V> wrap(
            final ConcurrentMap<K, V> concurrentMap,
            final Set<Break> breaks) {
        return new BreakableConcurrentMap<>(concurrentMap, breaks, new HashMap<>(), NO_NULL_PERMITS);
    }
}
