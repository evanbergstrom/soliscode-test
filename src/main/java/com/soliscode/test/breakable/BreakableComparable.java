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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.jspecify.annotations.Nullable;

/// A comparable value that can be broken in well-defined ways, to show that
/// [com.soliscode.test.contract.comparable.ComparableContract] can fail.
///
/// It wraps a value, as [BreakableMapEntry] wraps an entry. Unbroken, `compareTo` is the values' `compareTo`, and
/// `equals`, `hashCode` and `toString` are the value's: two wrappers of equal values are equal, so its natural
/// ordering is consistent with `equals` where the value's is. It is made with its constructor, and has no builder.
///
/// `compareTo` handles `null` first, through its two null breaks or with [NullPointerException], and only then the
/// other breaks, so that none of them changes `compareTo(null)`.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// ## Thread Safety
/// Once it is safely published, its value and serial number never change, and the serial numbers come from an
/// [AtomicLong]. Its breaks are a copy of the set it was made with, which `addBreak` changes, unsynchronized. It may
/// be shared between threads only while its breaks are not changed, and only where its value is itself safe to share:
/// it reads the value without a lock.
///
/// ## Serialization
/// It is serializable, as every breakable is, where its value is. A deserialized object takes a fresh serial number,
/// so that two copies read from one stream are still told apart by [#COMPARE_TO_BREAKS_TIES_BY_IDENTITY].
///
/// @param <T> the type of the value
/// @since 0.1.3
/// @see Comparable
public class BreakableComparable<T extends Comparable<? super T>> extends AbstractBreakable
        implements Comparable<BreakableComparable<T>> {

    /// **Purpose:** `compareTo` reverses the natural order.
    ///
    /// **Effect:** `compareTo` returns the negated sign of the values' `compareTo`. The three laws still hold.
    ///
    /// **Affected Methods:** [#compareTo(BreakableComparable)]
    public static final Break COMPARE_TO_IS_REVERSED =
            new Break("compareTo reverses the natural order");

    /// **Purpose:** `compareTo` calls every pair equal.
    ///
    /// **Effect:** `compareTo` of a non-null object returns 0. The three laws still hold.
    ///
    /// **Affected Methods:** [#compareTo(BreakableComparable)]
    public static final Break COMPARE_TO_ALWAYS_RETURNS_ZERO =
            new Break("compareTo always returns zero");

    /// **Purpose:** `compareTo` of the object itself is not zero, where sign symmetry makes `x.compareTo(x)` zero.
    ///
    /// **Effect:** `compareTo(this)` returns 1.
    ///
    /// **Affected Methods:** [#compareTo(BreakableComparable)]
    public static final Break COMPARE_TO_SELF_IS_NOT_ZERO =
            new Break("compareTo of itself is not zero");

    /// **Purpose:** `compareTo` is not sign-symmetric, where [Comparable#compareTo(Object)] requires
    /// `signum(x.compareTo(y)) == -signum(y.compareTo(x))`.
    ///
    /// **Effect:** `compareTo` returns 1 for every object whose value does not compare as equal: each of two
    /// unequal objects is greater than the other.
    ///
    /// **Affected Methods:** [#compareTo(BreakableComparable)]
    public static final Break COMPARE_TO_IS_NOT_SIGN_SYMMETRIC =
            new Break("compareTo is not sign-symmetric");

    /// **Purpose:** `compareTo` is not transitive, where [Comparable#compareTo(Object)] requires that `x > y` and
    /// `y > z` imply `x > z`: an order in a cycle, as rock, paper and scissors are.
    ///
    /// **Effect:** with `rx = Math.floorMod(value.hashCode(), 3)` for this object and `ry` the same for the other,
    /// `compareTo` returns 1 where `Math.floorMod(rx - ry, 3) == 1`, -1 where it is 2, and the values' `compareTo`
    /// where `rx == ry`. That rule is antisymmetric, so the break is still sign-symmetric, and compares as zero only
    /// what the values do. On `Integer`s, -1 > 1 > 0 > -1.
    ///
    /// **Affected Methods:** [#compareTo(BreakableComparable)]
    public static final Break COMPARE_TO_IS_CYCLIC =
            new Break("compareTo orders values in a cycle");

    /// **Purpose:** `compareTo` treats near values as equal, a fault of comparisons with a tolerance, where
    /// [Comparable#compareTo(Object)] requires that objects that compare as zero compare alike with every other.
    ///
    /// **Effect:** `compareTo` returns 0 where the values' hash codes differ by exactly one, in `long` arithmetic, and
    /// the values' `compareTo` elsewhere. It is still sign-symmetric. On `Integer`s, -1 and 0 compare as zero, and 0
    /// and 1, but -1 is less than 1.
    ///
    /// **Affected Methods:** [#compareTo(BreakableComparable)]
    public static final Break COMPARE_TO_TREATS_NEAR_VALUES_AS_EQUAL =
            new Break("compareTo treats values whose hash codes differ by one as equal");

    /// **Purpose:** `compareTo(null)` does not throw, where [Comparable] says it throws [NullPointerException].
    ///
    /// **Effect:** `compareTo(null)` returns 1, as if null came first.
    ///
    /// **Affected Methods:** [#compareTo(BreakableComparable)]
    public static final Break COMPARE_TO_NULL_DOES_NOT_THROW =
            new Break("compareTo(null) returns 1");

    /// **Purpose:** `compareTo(null)` throws the wrong exception, where [Comparable] says it throws
    /// [NullPointerException].
    ///
    /// **Effect:** `compareTo(null)` throws [IllegalArgumentException].
    ///
    /// **Affected Methods:** [#compareTo(BreakableComparable)]
    public static final Break COMPARE_TO_NULL_THROWS_ILLEGAL_ARGUMENT =
            new Break("compareTo(null) throws IllegalArgumentException");

    /// **Purpose:** `compareTo` breaks ties by identity, so that equal objects compare unequal: an ordering not
    /// consistent with `equals`.
    ///
    /// **Effect:** where the values compare as zero and the other object is not this one, `compareTo` compares the
    /// two objects' serial numbers, given in the order they were made. The order is the values' order, then the
    /// order of making, so the three laws still hold.
    ///
    /// **Affected Methods:** [#compareTo(BreakableComparable)]
    public static final Break COMPARE_TO_BREAKS_TIES_BY_IDENTITY =
            new Break("compareTo breaks ties by identity");

    /// **Purpose:** `equals` is identity, so that objects that compare as zero are not equal: an ordering not
    /// consistent with `equals`.
    ///
    /// **Effect:** `equals(o)` is `this == o`. `hashCode` is still the value's, which `Object`'s contract allows.
    ///
    /// **Affected Methods:** [#equals(Object)]
    public static final Break COMPARABLE_EQUALS_IS_IDENTITY =
            new Break("equals of a comparable is identity");

    @Serial
    private static final long serialVersionUID = 1L;

    /// The source of serial numbers, for [#COMPARE_TO_BREAKS_TIES_BY_IDENTITY] only.
    private static final AtomicLong SERIALS = new AtomicLong();

    /// The value.
    private final T value;

    /// This object's serial number, for [#COMPARE_TO_BREAKS_TIES_BY_IDENTITY] only: given when it is made, and again
    /// when it is deserialized.
    private transient long serial;

    /// Creates a `BreakableComparable` of a value, with the given breaks.
    ///
    /// @param valueArg the value
    /// @param breaks   the set of breaks to apply, copied
    public BreakableComparable(final T valueArg, final Set<Break> breaks) {
        super(new HashSet<>(breaks), Map.of(), false);
        this.value = Objects.requireNonNull(valueArg);
        this.serial = SERIALS.getAndIncrement();
    }

    /// Reads the object, and gives it a fresh serial number.
    ///
    /// @param in the stream
    /// @throws IOException if the stream cannot be read
    /// @throws ClassNotFoundException if a class in the stream cannot be found
    @Serial
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        serial = SERIALS.getAndIncrement();
    }

    /// Returns the value.
    ///
    /// @return the value
    public T value() {
        return value;
    }

    /// Implements [Comparable#compareTo(Object)]: the values' `compareTo`.
    ///
    /// # Breaks
    /// - [#COMPARE_TO_NULL_DOES_NOT_THROW]
    /// - [#COMPARE_TO_NULL_THROWS_ILLEGAL_ARGUMENT]
    /// - [#COMPARE_TO_SELF_IS_NOT_ZERO]
    /// - [#COMPARE_TO_ALWAYS_RETURNS_ZERO]
    /// - [#COMPARE_TO_IS_REVERSED]
    /// - [#COMPARE_TO_IS_NOT_SIGN_SYMMETRIC]
    /// - [#COMPARE_TO_IS_CYCLIC]
    /// - [#COMPARE_TO_TREATS_NEAR_VALUES_AS_EQUAL]
    /// - [#COMPARE_TO_BREAKS_TIES_BY_IDENTITY]
    ///
    /// @param other the object to compare with; `null` only to be refused
    /// @return a negative number, zero or a positive number as this object is less than, equal to or greater than
    ///         the other
    /// @throws NullPointerException if `other` is null, unless broken
    @Override
    public int compareTo(final @Nullable BreakableComparable<T> other) {
        if (other == null) {
            if (hasBreak(COMPARE_TO_NULL_DOES_NOT_THROW)) {
                return 1;
            }
            if (hasBreak(COMPARE_TO_NULL_THROWS_ILLEGAL_ARGUMENT)) {
                throw new IllegalArgumentException("compareTo(null)");
            }
            throw new NullPointerException("compareTo(null)");
        }
        if (hasBreak(COMPARE_TO_SELF_IS_NOT_ZERO) && other == this) {
            return 1;
        }
        if (hasBreak(COMPARE_TO_ALWAYS_RETURNS_ZERO)) {
            return 0;
        }
        int c = value.compareTo(other.value);
        if (hasBreak(COMPARE_TO_IS_REVERSED)) {
            return -Integer.signum(c);
        }
        if (hasBreak(COMPARE_TO_IS_NOT_SIGN_SYMMETRIC)) {
            return c == 0 ? 0 : 1;
        }
        if (hasBreak(COMPARE_TO_IS_CYCLIC)) {
            int rx = Math.floorMod(value.hashCode(), 3);
            int ry = Math.floorMod(other.value.hashCode(), 3);
            if (rx != ry) {
                return Math.floorMod(rx - ry, 3) == 1 ? 1 : -1;
            }
        }
        if (hasBreak(COMPARE_TO_TREATS_NEAR_VALUES_AS_EQUAL)
                && Math.abs((long) value.hashCode() - (long) other.value.hashCode()) == 1L) {
            return 0;
        }
        if (hasBreak(COMPARE_TO_BREAKS_TIES_BY_IDENTITY) && c == 0 && other != this) {
            return Long.compare(serial, other.serial);
        }
        return c;
    }

    /// Returns whether `o` is a `BreakableComparable` of an equal value.
    ///
    /// # Breaks
    /// - [#COMPARABLE_EQUALS_IS_IDENTITY]
    ///
    /// @param o the object to compare with
    /// @return whether it wraps an equal value
    @Override
    public boolean equals(final @Nullable Object o) {
        if (hasBreak(COMPARABLE_EQUALS_IS_IDENTITY)) {
            return this == o;
        }
        return o instanceof BreakableComparable<?> other && value.equals(other.value);
    }

    /// Returns the value's hash code.
    ///
    /// @return the hash code
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /// Returns the value's string form.
    ///
    /// @return the string form
    @Override
    public String toString() {
        return value.toString();
    }
}
