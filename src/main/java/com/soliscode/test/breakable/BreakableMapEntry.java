/*
 * Copyright 2026 Evan Bergstrom
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

import java.io.Serial;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.mapentry.MapEntryMethods;

/// A map entry that can be broken in well-defined ways, to show that
/// [com.soliscode.test.contract.mapentry.MapEntryContract] can fail.
///
/// It wraps a delegate entry, as [BreakableIterator] wraps an iterator. Unbroken, it delegates `getKey`, `getValue`
/// and `setValue` (after checking that `setValue` is supported, [MapEntryMethods#MAP_ENTRY_SET_VALUE]), and computes
/// `equals` and `hashCode` by [Map.Entry]'s formulas, and `toString` as `key=value`, from its own `getKey()` and
/// `getValue()`; so it writes through to the map behind the delegate where the delegate does. It has no builder.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.3
/// @see Map.Entry
public class BreakableMapEntry<K, V> extends AbstractBreakable implements Map.Entry<K, V> {

    /// **Purpose:** `getKey` loses the key.
    ///
    /// **Effect:** `getKey()` returns `null`; so `equals`, `hashCode` and `toString`, which use it, see a null key.
    ///
    /// **Affected Methods:** [#getKey()], [#equals(Object)], [#hashCode()], [#toString()]
    public static final Break MAP_ENTRY_GET_KEY_RETURNS_NULL =
            new Break("map entry getKey returns null");

    /// **Purpose:** `getValue` loses the value.
    ///
    /// **Effect:** `getValue()` returns `null`; so `equals`, `hashCode` and `toString` see a null value.
    ///
    /// **Affected Methods:** [#getValue()], [#equals(Object)], [#hashCode()], [#toString()]
    public static final Break MAP_ENTRY_GET_VALUE_RETURNS_NULL =
            new Break("map entry getValue returns null");

    /// **Purpose:** `setValue` returns the wrong value, where [Map.Entry#setValue(Object)] returns the old one.
    ///
    /// **Effect:** `setValue` sets the value, and returns the value it was given.
    ///
    /// **Affected Methods:** [#setValue(Object)]
    public static final Break MAP_ENTRY_SET_VALUE_RETURNS_NEW_VALUE =
            new Break("map entry setValue returns the new value, not the old");

    /// **Purpose:** `setValue` does not replace the value.
    ///
    /// **Effect:** `setValue` returns the old value and changes nothing.
    ///
    /// **Affected Methods:** [#setValue(Object)]
    public static final Break MAP_ENTRY_SET_VALUE_DOES_NOTHING =
            new Break("map entry setValue changes nothing");

    /// **Purpose:** `setValue` changes the entry but not the map behind it, where [Map.Entry#setValue(Object)]
    /// "writes through to the map".
    ///
    /// **Effect:** `setValue` returns the old value and keeps the new one in the breakable, which its `getValue`,
    /// `equals`, `hashCode` and `toString` use from then on; the delegate, and the map behind it, keep the old.
    ///
    /// **Affected Methods:** [#setValue(Object)], [#getValue()], [#equals(Object)], [#hashCode()], [#toString()]
    public static final Break MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH =
            new Break("map entry setValue does not write through to the map");

    /// **Purpose:** an unsupported `setValue` throws the wrong exception, where [Map.Entry#setValue(Object)] throws
    /// [UnsupportedOperationException].
    ///
    /// **Effect:** where [MapEntryMethods#MAP_ENTRY_SET_VALUE] is not supported, `setValue` throws
    /// [RuntimeException].
    ///
    /// **Affected Methods:** [#setValue(Object)]
    public static final Break MAP_ENTRY_SET_VALUE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED =
            new Break("map entry setValue throws the wrong exception if not supported");

    /// **Purpose:** `equals` is not overridden, so an entry equals only itself, where [Map.Entry#equals(Object)]
    /// compares the mappings.
    ///
    /// **Effect:** `equals(o)` is `this == o`.
    ///
    /// **Affected Methods:** [#equals(Object)]
    public static final Break MAP_ENTRY_EQUALS_IS_IDENTITY =
            new Break("map entry equals is identity");

    /// **Purpose:** `equals` ignores the keys.
    ///
    /// **Effect:** `equals` of an entry compares only the values.
    ///
    /// **Affected Methods:** [#equals(Object)]
    public static final Break MAP_ENTRY_EQUALS_IGNORES_KEY =
            new Break("map entry equals ignores the key");

    /// **Purpose:** `equals` ignores the values.
    ///
    /// **Effect:** `equals` of an entry compares only the keys.
    ///
    /// **Affected Methods:** [#equals(Object)]
    public static final Break MAP_ENTRY_EQUALS_IGNORES_VALUE =
            new Break("map entry equals ignores the value");

    /// **Purpose:** `equals` is true only for entries of its own class, where [Map.Entry#equals(Object)] works
    /// "across different implementations of the Map.Entry interface".
    ///
    /// **Effect:** `equals` is `false` for an entry that is not a `BreakableMapEntry`.
    ///
    /// **Affected Methods:** [#equals(Object)]
    public static final Break MAP_ENTRY_EQUALS_REQUIRES_SAME_CLASS =
            new Break("map entry equals requires an entry of the same class");

    /// **Purpose:** `equals` of an object that is not an entry compares it with the key, a fault of entries that
    /// stand for their keys.
    ///
    /// **Effect:** `equals(o)`, for an `o` that is not a [Map.Entry], is `Objects.equals(getKey(), o)`.
    ///
    /// **Affected Methods:** [#equals(Object)]
    public static final Break MAP_ENTRY_EQUALS_IS_TRUE_FOR_ITS_KEY =
            new Break("map entry equals is true for its key");

    /// **Purpose:** `equals` assumes its argument is an entry, where [Object#equals(Object)] returns `false` for
    /// `null` and for an object of another type.
    ///
    /// **Effect:** `equals` throws [NullPointerException] for `null`, and [ClassCastException] for an object that is
    /// not a [Map.Entry].
    ///
    /// **Affected Methods:** [#equals(Object)]
    public static final Break MAP_ENTRY_EQUALS_THROWS_FOR_NON_ENTRY =
            new Break("map entry equals throws for null or a non-entry");

    /// **Purpose:** `hashCode` is not overridden, where [Map.Entry#hashCode()] gives a formula.
    ///
    /// **Effect:** `hashCode()` is `System.identityHashCode(this)`.
    ///
    /// **Affected Methods:** [#hashCode()]
    public static final Break MAP_ENTRY_HASH_CODE_IS_IDENTITY =
            new Break("map entry hashCode is the identity hash code");

    /// **Purpose:** `hashCode` ignores the value.
    ///
    /// **Effect:** `hashCode()` is the key's hash code only.
    ///
    /// **Affected Methods:** [#hashCode()]
    public static final Break MAP_ENTRY_HASH_CODE_IGNORES_VALUE =
            new Break("map entry hashCode ignores the value");

    /// **Purpose:** `hashCode` uses another formula than [Map.Entry#hashCode()]'s, so that it differs from other
    /// classes' equal entries.
    ///
    /// **Effect:** `hashCode()` is `Objects.hash(getKey(), getValue())`.
    ///
    /// **Affected Methods:** [#hashCode()]
    public static final Break MAP_ENTRY_HASH_CODE_IS_OBJECTS_HASH =
            new Break("map entry hashCode is Objects.hash of the key and value");

    /// **Purpose:** `hashCode` is cached, and goes stale when `setValue` changes the value.
    ///
    /// **Effect:** `hashCode()` is the formula's result for the mapping the entry had when it was made.
    ///
    /// **Affected Methods:** [#hashCode()]
    public static final Break MAP_ENTRY_HASH_CODE_IS_STALE_AFTER_SET_VALUE =
            new Break("map entry hashCode is stale after setValue");

    @Serial
    private static final long serialVersionUID = 1L;

    /// The delegate entry.
    private final Map.Entry<K, V> delegate;

    /// The hash code the entry had when it was made, for [#MAP_ENTRY_HASH_CODE_IS_STALE_AFTER_SET_VALUE] only.
    private final int hashAtCreation;

    /// Whether `setValue` has kept a value here, for [#MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH] only.
    private boolean valueKept;

    /// The value `setValue` kept here, for [#MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH] only.
    private @Nullable V keptValue;

    /// Creates a `BreakableMapEntry` over a delegate entry, with the given breaks and method statuses.
    ///
    /// @param delegateArg    the delegate entry
    /// @param breaks         the set of breaks to apply
    /// @param methodStatuses the status of each optional method
    public BreakableMapEntry(final Map.Entry<K, V> delegateArg, final Set<Break> breaks,
                             final Map<InterfaceMethod, MethodStatus> methodStatuses) {
        super(breaks, methodStatuses, false);
        this.delegate = Objects.requireNonNull(delegateArg);
        this.hashAtCreation = Objects.hashCode(delegateArg.getKey()) ^ Objects.hashCode(delegateArg.getValue());
    }

    /// Implements [Map.Entry#getKey()].
    ///
    /// # Breaks
    /// - [#MAP_ENTRY_GET_KEY_RETURNS_NULL]
    ///
    /// @return the key, or `null` if broken
    @Override
    public @Nullable K getKey() {
        if (hasBreak(MAP_ENTRY_GET_KEY_RETURNS_NULL)) {
            return null;
        }
        return delegate.getKey();
    }

    /// Implements [Map.Entry#getValue()].
    ///
    /// # Breaks
    /// - [#MAP_ENTRY_GET_VALUE_RETURNS_NULL]
    /// - [#MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH]
    ///
    /// @return the value, or `null` if broken
    @Override
    public @Nullable V getValue() {
        if (hasBreak(MAP_ENTRY_GET_VALUE_RETURNS_NULL)) {
            return null;
        }
        return currentValue();
    }

    /// Implements the optional [Map.Entry#setValue(Object)].
    ///
    /// # Breaks
    /// - [#MAP_ENTRY_SET_VALUE_RETURNS_NEW_VALUE]
    /// - [#MAP_ENTRY_SET_VALUE_DOES_NOTHING]
    /// - [#MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH]
    /// - [#MAP_ENTRY_SET_VALUE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED]
    ///
    /// @param value the new value
    /// @return the old value, or the new one if broken
    /// @throws UnsupportedOperationException if `setValue` is not supported, here or by the delegate
    @Override
    public @Nullable V setValue(final V value) {
        checkOptionalMethodSupport(MapEntryMethods.MAP_ENTRY_SET_VALUE,
                MAP_ENTRY_SET_VALUE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED);
        if (hasBreak(MAP_ENTRY_SET_VALUE_DOES_NOTHING)) {
            return currentValue();
        }
        if (hasBreak(MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH)) {
            V old = currentValue();
            keptValue = value;
            valueKept = true;
            return old;
        }
        V old = delegate.setValue(value);
        return hasBreak(MAP_ENTRY_SET_VALUE_RETURNS_NEW_VALUE) ? value : old;
    }

    /// Implements [Map.Entry#equals(Object)]: `true` for an entry with an equal key and an equal value.
    ///
    /// # Breaks
    /// - [#MAP_ENTRY_EQUALS_IS_IDENTITY]
    /// - [#MAP_ENTRY_EQUALS_IGNORES_KEY]
    /// - [#MAP_ENTRY_EQUALS_IGNORES_VALUE]
    /// - [#MAP_ENTRY_EQUALS_REQUIRES_SAME_CLASS]
    /// - [#MAP_ENTRY_EQUALS_IS_TRUE_FOR_ITS_KEY]
    /// - [#MAP_ENTRY_EQUALS_THROWS_FOR_NON_ENTRY]
    /// - [#MAP_ENTRY_GET_KEY_RETURNS_NULL], [#MAP_ENTRY_GET_VALUE_RETURNS_NULL],
    ///   [#MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH], through `getKey()` and `getValue()`
    ///
    /// @param o the object to compare with
    /// @return whether it is an equal entry
    @Override
    public boolean equals(final @Nullable Object o) {
        if (hasBreak(MAP_ENTRY_EQUALS_IS_IDENTITY)) {
            return this == o;
        }
        if (hasBreak(MAP_ENTRY_EQUALS_THROWS_FOR_NON_ENTRY)) {
            if (o == null) {
                throw new NullPointerException("equals(null)");
            }
            if (!(o instanceof Map.Entry<?, ?>)) {
                throw new ClassCastException("not a Map.Entry: " + o);
            }
        }
        if (!(o instanceof Map.Entry<?, ?> other)) {
            return hasBreak(MAP_ENTRY_EQUALS_IS_TRUE_FOR_ITS_KEY) && Objects.equals(getKey(), o);
        }
        if (hasBreak(MAP_ENTRY_EQUALS_REQUIRES_SAME_CLASS) && !(o instanceof BreakableMapEntry<?, ?>)) {
            return false;
        }
        boolean keys = hasBreak(MAP_ENTRY_EQUALS_IGNORES_KEY) || Objects.equals(getKey(), other.getKey());
        boolean values = hasBreak(MAP_ENTRY_EQUALS_IGNORES_VALUE) || Objects.equals(getValue(), other.getValue());
        return keys && values;
    }

    /// Implements [Map.Entry#hashCode()]: the key's hash code exclusive or the value's, `0` for `null`.
    ///
    /// # Breaks
    /// - [#MAP_ENTRY_HASH_CODE_IS_IDENTITY]
    /// - [#MAP_ENTRY_HASH_CODE_IGNORES_VALUE]
    /// - [#MAP_ENTRY_HASH_CODE_IS_OBJECTS_HASH]
    /// - [#MAP_ENTRY_HASH_CODE_IS_STALE_AFTER_SET_VALUE]
    /// - [#MAP_ENTRY_GET_KEY_RETURNS_NULL], [#MAP_ENTRY_GET_VALUE_RETURNS_NULL],
    ///   [#MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH], through `getKey()` and `getValue()`
    ///
    /// @return the hash code
    @Override
    public int hashCode() {
        if (hasBreak(MAP_ENTRY_HASH_CODE_IS_IDENTITY)) {
            return System.identityHashCode(this);
        }
        if (hasBreak(MAP_ENTRY_HASH_CODE_IS_STALE_AFTER_SET_VALUE)) {
            return hashAtCreation;
        }
        if (hasBreak(MAP_ENTRY_HASH_CODE_IGNORES_VALUE)) {
            return Objects.hashCode(getKey());
        }
        if (hasBreak(MAP_ENTRY_HASH_CODE_IS_OBJECTS_HASH)) {
            return Objects.hash(getKey(), getValue());
        }
        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    }

    /// Returns the entry as `key=value`, from `getKey()` and `getValue()`, as [java.util.AbstractMap.SimpleEntry]
    /// does.
    ///
    /// @return the entry's string form
    @Override
    public String toString() {
        return getKey() + "=" + getValue();
    }

    /// The value, unbroken: the one kept here under [#MAP_ENTRY_SET_VALUE_DOES_NOT_WRITE_THROUGH], or the delegate's.
    private @Nullable V currentValue() {
        return valueKept ? keptValue : delegate.getValue();
    }
}
