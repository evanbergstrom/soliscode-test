/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.jspecify.annotations.Nullable;

/// A comparator that can be broken in well-defined ways, to show that
/// [com.soliscode.test.contract.comparator.ComparatorContract] can fail.
///
/// It wraps a delegate comparator, as [BreakableComparable] wraps a value. Unbroken, `compare` of two non-null
/// arguments is the delegate's, and a null argument is refused with [NullPointerException], even where the delegate
/// permits nulls, unless one of the two null breaks is set. `reversed()` and every `thenComparing` form are
/// [Comparator]'s own defaults, called through `Comparator.super`, so that they call this object's `compare`: a break
/// of `compare` is seen through them as through `compare`. Each key form builds its comparator and calls
/// `Comparator.super.thenComparing(Comparator)`, not this object's `thenComparing(Comparator)`, so that a break of one
/// form does not reach the others; and each form checks its arguments for null before any break acts. It is made with
/// its constructor, and has no builder.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// ## Thread Safety
/// Once it is safely published, its delegate never changes. Its breaks are a copy of the set it was made with, which
/// `addBreak` changes, unsynchronized. It may be shared between threads only while its breaks are not changed, and
/// only where its delegate is itself safe to share: it calls the delegate without a lock. The serial numbers of
/// [#COMPARE_BREAKS_TIES_BY_IDENTITY] are kept in a synchronized map.
///
/// ## Serialization
/// It is serializable, as every breakable is, where its delegate is. A deserialized comparator numbers the objects it
/// compares afresh.
///
/// @param <T> the type it compares
/// @since 0.1.4
/// @see Comparator
public class BreakableComparator<T> extends AbstractBreakable implements Comparator<T> {

    /// **Purpose:** `compare` reverses the order.
    ///
    /// **Effect:** `compare` of two non-null arguments returns the negated sign of the delegate's result. The three
    /// laws still hold.
    ///
    /// **Affected Methods:** [#compare(Object, Object)]
    public static final Break COMPARE_IS_REVERSED =
            new Break("compare reverses the order");

    /// **Purpose:** `compare` calls every pair equal.
    ///
    /// **Effect:** `compare` of two non-null arguments returns 0. The three laws still hold.
    ///
    /// **Affected Methods:** [#compare(Object, Object)]
    public static final Break COMPARE_ALWAYS_RETURNS_ZERO =
            new Break("compare always returns zero");

    /// **Purpose:** `compare` of an object with itself is not zero, where sign symmetry makes `compare(x, x)` zero.
    ///
    /// **Effect:** `compare(x, x)` returns 1 for a non-null `x`.
    ///
    /// **Affected Methods:** [#compare(Object, Object)]
    public static final Break COMPARE_OF_AN_OBJECT_WITH_ITSELF_IS_NOT_ZERO =
            new Break("compare of an object with itself is not zero");

    /// **Purpose:** `compare` is not sign-symmetric, where [Comparator#compare(Object, Object)] requires
    /// `signum(compare(x, y)) == -signum(compare(y, x))`.
    ///
    /// **Effect:** `compare` returns 1 for every pair of non-null arguments the delegate does not call equal: each is
    /// greater than the other.
    ///
    /// **Affected Methods:** [#compare(Object, Object)]
    public static final Break COMPARE_IS_NOT_SIGN_SYMMETRIC =
            new Break("compare is not sign-symmetric");

    /// **Purpose:** `compare` is not transitive, where [Comparator#compare(Object, Object)] requires that `x > y` and
    /// `y > z` imply `x > z`: an order in a cycle, as rock, paper and scissors are.
    ///
    /// **Effect:** with `rx = Math.floorMod(x.hashCode(), 3)` and `ry` the same for `y`, `compare(x, y)` returns 1
    /// where `Math.floorMod(rx - ry, 3) == 1`, -1 where it is 2, and the delegate's result where `rx == ry`. That rule
    /// is antisymmetric, so the break is still sign-symmetric, and calls equal only what the delegate does. On
    /// `Integer`s in their natural order, -1 > 1 > 0 > -1.
    ///
    /// **Affected Methods:** [#compare(Object, Object)]
    public static final Break COMPARE_IS_CYCLIC =
            new Break("compare orders objects in a cycle");

    /// **Purpose:** `compare` treats near objects as equal, a fault of comparisons with a tolerance, where
    /// [Comparator#compare(Object, Object)] requires that objects that compare as zero compare alike with every other.
    ///
    /// **Effect:** `compare` returns 0 where the arguments' hash codes differ by exactly one, in `long` arithmetic,
    /// and the delegate's result elsewhere. It is still sign-symmetric. On `Integer`s in their natural order, -1 and 0
    /// compare as zero, and 0 and 1, but -1 is less than 1.
    ///
    /// **Affected Methods:** [#compare(Object, Object)]
    public static final Break COMPARE_TREATS_NEAR_VALUES_AS_EQUAL =
            new Break("compare treats objects whose hash codes differ by one as equal");

    /// **Purpose:** `compare` permits null, where the contract reads a comparator as refusing it unless it says
    /// otherwise.
    ///
    /// **Effect:** `compare` puts null first: `compare(null, null)` returns 0, `compare(null, x)` -1 and
    /// `compare(x, null)` 1. That is a lawful order, so a test class that says the comparator permits nulls passes
    /// with it.
    ///
    /// **Affected Methods:** [#compare(Object, Object)]
    public static final Break COMPARE_NULL_DOES_NOT_THROW =
            new Break("compare with a null argument puts null first");

    /// **Purpose:** `compare` with a null argument throws the wrong exception, where
    /// [Comparator#compare(Object, Object)] says it throws [NullPointerException].
    ///
    /// **Effect:** `compare` with a null argument throws [IllegalArgumentException].
    ///
    /// **Affected Methods:** [#compare(Object, Object)]
    public static final Break COMPARE_NULL_THROWS_ILLEGAL_ARGUMENT =
            new Break("compare with a null argument throws IllegalArgumentException");

    /// **Purpose:** `compare` breaks ties by identity, so that equal objects compare unequal: an ordering not
    /// consistent with `equals`.
    ///
    /// **Effect:** where the delegate returns 0 and the arguments are not the same object, `compare` compares their
    /// serial numbers, given in the order the objects are first compared in such a tie. The order is the delegate's,
    /// then that order, so the three laws still hold. While the break is set, the comparator holds every object it has
    /// compared in a tie.
    ///
    /// **Affected Methods:** [#compare(Object, Object)]
    public static final Break COMPARE_BREAKS_TIES_BY_IDENTITY =
            new Break("compare breaks ties by identity");

    /// **Purpose:** `reversed()` does not reverse the order.
    ///
    /// **Effect:** `reversed()` returns this comparator.
    ///
    /// **Affected Methods:** [#reversed()]
    public static final Break REVERSED_KEEPS_THE_ORDER =
            new Break("reversed returns the same order");

    /// **Purpose:** `thenComparing(other)` does not break ties.
    ///
    /// **Effect:** `thenComparing(other)` returns this comparator, once it has checked that `other` is not null.
    ///
    /// **Affected Methods:** [#thenComparing(Comparator)]
    public static final Break THEN_COMPARING_IGNORES_THE_OTHER =
            new Break("thenComparing(other) ignores the other comparator");

    /// **Purpose:** `thenComparing(other)` is not lexicographic in the documented order.
    ///
    /// **Effect:** `thenComparing(other)` consults `other` first, and this comparator only where `other` returns 0.
    ///
    /// **Affected Methods:** [#thenComparing(Comparator)]
    public static final Break THEN_COMPARING_CONSULTS_THE_OTHER_FIRST =
            new Break("thenComparing(other) consults the other comparator first");

    /// **Purpose:** `thenComparing(null)` does not throw, where [Comparator#thenComparing(Comparator)] says it throws
    /// [NullPointerException].
    ///
    /// **Effect:** `thenComparing(null)` returns this comparator.
    ///
    /// **Affected Methods:** [#thenComparing(Comparator)]
    public static final Break THEN_COMPARING_ACCEPTS_NULL =
            new Break("thenComparing(null) returns the comparator");

    /// **Purpose:** `thenComparing(keyExtractor)` does not break ties.
    ///
    /// **Effect:** `thenComparing(keyExtractor)` returns this comparator, once it has checked that `keyExtractor` is
    /// not null.
    ///
    /// **Affected Methods:** [#thenComparing(Function)]
    public static final Break THEN_COMPARING_IGNORES_THE_KEY =
            new Break("thenComparing(keyExtractor) ignores the key");

    /// **Purpose:** `thenComparing(keyExtractor, keyComparator)` breaks ties in the wrong order.
    ///
    /// **Effect:** `thenComparing(keyExtractor, keyComparator)` compares the keys with `keyComparator.reversed()`.
    ///
    /// **Affected Methods:** [#thenComparing(Function, Comparator)]
    public static final Break THEN_COMPARING_REVERSES_THE_KEY_COMPARATOR =
            new Break("thenComparing(keyExtractor, keyComparator) reverses the key comparator");

    /// **Purpose:** `thenComparingInt` does not break ties.
    ///
    /// **Effect:** `thenComparingInt(keyExtractor)` returns this comparator, once it has checked that `keyExtractor`
    /// is not null.
    ///
    /// **Affected Methods:** [#thenComparingInt(ToIntFunction)]
    public static final Break THEN_COMPARING_INT_IGNORES_THE_KEY =
            new Break("thenComparingInt ignores the key");

    /// **Purpose:** `thenComparingLong` does not break ties.
    ///
    /// **Effect:** `thenComparingLong(keyExtractor)` returns this comparator, once it has checked that `keyExtractor`
    /// is not null.
    ///
    /// **Affected Methods:** [#thenComparingLong(ToLongFunction)]
    public static final Break THEN_COMPARING_LONG_IGNORES_THE_KEY =
            new Break("thenComparingLong ignores the key");

    /// **Purpose:** `thenComparingDouble` does not break ties.
    ///
    /// **Effect:** `thenComparingDouble(keyExtractor)` returns this comparator, once it has checked that
    /// `keyExtractor` is not null.
    ///
    /// **Affected Methods:** [#thenComparingDouble(ToDoubleFunction)]
    public static final Break THEN_COMPARING_DOUBLE_IGNORES_THE_KEY =
            new Break("thenComparingDouble ignores the key");

    @Serial
    private static final long serialVersionUID = 1L;

    /// The delegate.
    private final Comparator<T> delegate;

    /// The serial numbers of the objects compared, for [#COMPARE_BREAKS_TIES_BY_IDENTITY] only: given in a first tie,
    /// and made again empty when the comparator is deserialized.
    private transient Map<Object, Long> serials;

    /// Creates a `BreakableComparator` over a delegate, with the given breaks.
    ///
    /// @param delegateArg the delegate
    /// @param breaks      the set of breaks to apply, copied
    public BreakableComparator(final Comparator<T> delegateArg, final Set<Break> breaks) {
        super(breaks, Map.of(), false);
        this.delegate = Objects.requireNonNull(delegateArg);
        this.serials = newSerials();
    }

    /// Reads the object, and gives it an empty map of serial numbers.
    ///
    /// @param in the stream
    /// @throws IOException if the stream cannot be read
    /// @throws ClassNotFoundException if a class in the stream cannot be found
    @Serial
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        serials = newSerials();
    }

    /// Returns an empty, synchronized identity map for the serial numbers.
    ///
    /// @return the map
    private static Map<Object, Long> newSerials() {
        return Collections.synchronizedMap(new IdentityHashMap<>());
    }

    /// Implements [Comparator#compare(Object, Object)]: the delegate's result, for two non-null arguments.
    ///
    /// # Breaks
    /// - [#COMPARE_NULL_DOES_NOT_THROW]
    /// - [#COMPARE_NULL_THROWS_ILLEGAL_ARGUMENT]
    /// - [#COMPARE_OF_AN_OBJECT_WITH_ITSELF_IS_NOT_ZERO]
    /// - [#COMPARE_ALWAYS_RETURNS_ZERO]
    /// - [#COMPARE_IS_REVERSED]
    /// - [#COMPARE_IS_NOT_SIGN_SYMMETRIC]
    /// - [#COMPARE_IS_CYCLIC]
    /// - [#COMPARE_TREATS_NEAR_VALUES_AS_EQUAL]
    /// - [#COMPARE_BREAKS_TIES_BY_IDENTITY]
    ///
    /// @param x the first object; `null` only to be refused
    /// @param y the second object; `null` only to be refused
    /// @return a negative number, zero or a positive number as `x` is less than, equal to or greater than `y`
    /// @throws NullPointerException if an argument is null, unless broken
    @Override
    public int compare(final @Nullable T x, final @Nullable T y) {
        if (x == null || y == null) {
            return compareWithNull(x, y);
        }
        if (hasBreak(COMPARE_OF_AN_OBJECT_WITH_ITSELF_IS_NOT_ZERO) && x == y) {
            return 1;
        }
        if (hasBreak(COMPARE_ALWAYS_RETURNS_ZERO)) {
            return 0;
        }
        int c = delegate.compare(x, y);
        if (hasBreak(COMPARE_IS_REVERSED)) {
            return -Integer.signum(c);
        }
        if (hasBreak(COMPARE_IS_NOT_SIGN_SYMMETRIC)) {
            return c == 0 ? 0 : 1;
        }
        return withOrderBreaks(x, y, c, x == y);
    }

    /// `compare` with a null argument: throws [NullPointerException], or as [#COMPARE_NULL_DOES_NOT_THROW] or
    /// [#COMPARE_NULL_THROWS_ILLEGAL_ARGUMENT] says.
    private int compareWithNull(final @Nullable T x, final @Nullable T y) {
        if (hasBreak(COMPARE_NULL_DOES_NOT_THROW)) {
            return x == null ? (y == null ? 0 : -1) : 1;
        }
        if (hasBreak(COMPARE_NULL_THROWS_ILLEGAL_ARGUMENT)) {
            throw new IllegalArgumentException("compare with a null argument");
        }
        throw new NullPointerException("compare with a null argument");
    }

    /// The delegate's comparison `c` of `x` and `y`, changed by [#COMPARE_IS_CYCLIC],
    /// [#COMPARE_TREATS_NEAR_VALUES_AS_EQUAL] or [#COMPARE_BREAKS_TIES_BY_IDENTITY] where set; `same` is whether `x`
    /// and `y` are one object.
    private int withOrderBreaks(final T x, final T y, final int c, final boolean same) {
        if (hasBreak(COMPARE_IS_CYCLIC)) {
            int rx = Math.floorMod(x.hashCode(), 3);
            int ry = Math.floorMod(y.hashCode(), 3);
            if (rx != ry) {
                return Math.floorMod(rx - ry, 3) == 1 ? 1 : -1;
            }
        }
        if (hasBreak(COMPARE_TREATS_NEAR_VALUES_AS_EQUAL)
                && Math.abs((long) x.hashCode() - (long) y.hashCode()) == 1L) {
            return 0;
        }
        if (hasBreak(COMPARE_BREAKS_TIES_BY_IDENTITY) && c == 0 && !same) {
            return Long.compare(serial(x), serial(y));
        }
        return c;
    }

    /// Returns the serial number of an object, given the first time it is asked for. The synchronized map's
    /// `computeIfAbsent` holds its lock throughout, so two threads cannot give one number twice.
    ///
    /// @param o the object
    /// @return its serial number
    private long serial(final Object o) {
        Map<Object, Long> map = serials;
        return map.computeIfAbsent(o, k -> (long) map.size());
    }

    /// Implements [Comparator#reversed()]: `Comparator`'s default, which reverses this comparator.
    ///
    /// # Breaks
    /// - [#REVERSED_KEEPS_THE_ORDER]
    ///
    /// @return the reverse of this comparator
    @Override
    public Comparator<T> reversed() {
        if (hasBreak(REVERSED_KEEPS_THE_ORDER)) {
            return this;
        }
        return Comparator.super.reversed();
    }

    /// Implements [Comparator#thenComparing(Comparator)]: `Comparator`'s default.
    ///
    /// # Breaks
    /// - [#THEN_COMPARING_ACCEPTS_NULL]
    /// - [#THEN_COMPARING_IGNORES_THE_OTHER]
    /// - [#THEN_COMPARING_CONSULTS_THE_OTHER_FIRST]
    ///
    /// @param other the comparator that breaks this one's ties
    /// @return the lexicographic comparator
    /// @throws NullPointerException if `other` is null, unless broken
    @Override
    public Comparator<T> thenComparing(final @Nullable Comparator<? super T> other) {
        if (other == null && hasBreak(THEN_COMPARING_ACCEPTS_NULL)) {
            return this;
        }
        Objects.requireNonNull(other);
        if (hasBreak(THEN_COMPARING_IGNORES_THE_OTHER)) {
            return this;
        }
        if (hasBreak(THEN_COMPARING_CONSULTS_THE_OTHER_FIRST)) {
            return (Comparator<T> & Serializable) (a, b) -> {
                int r = other.compare(a, b);
                return r != 0 ? r : compare(a, b);
            };
        }
        return Comparator.super.thenComparing(other);
    }

    /// Implements [Comparator#thenComparing(Function)]: `Comparator`'s default, which breaks ties by the key's
    /// natural order.
    ///
    /// # Breaks
    /// - [#THEN_COMPARING_IGNORES_THE_KEY]
    ///
    /// @param keyExtractor the function that extracts the key
    /// @param <U> the type of the key
    /// @return the lexicographic comparator
    /// @throws NullPointerException if `keyExtractor` is null
    @Override
    public <U extends Comparable<? super U>> Comparator<T> thenComparing(
            final Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        if (hasBreak(THEN_COMPARING_IGNORES_THE_KEY)) {
            return this;
        }
        return Comparator.super.thenComparing(Comparator.comparing(keyExtractor));
    }

    /// Implements [Comparator#thenComparing(Function, Comparator)]: `Comparator`'s default, which breaks ties by the
    /// key, compared with `keyComparator`.
    ///
    /// # Breaks
    /// - [#THEN_COMPARING_REVERSES_THE_KEY_COMPARATOR]
    ///
    /// @param keyExtractor  the function that extracts the key
    /// @param keyComparator the comparator of the keys
    /// @param <U> the type of the key
    /// @return the lexicographic comparator
    /// @throws NullPointerException if an argument is null
    @Override
    public <U> Comparator<T> thenComparing(final Function<? super T, ? extends U> keyExtractor,
                                           final Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        if (hasBreak(THEN_COMPARING_REVERSES_THE_KEY_COMPARATOR)) {
            return Comparator.super.thenComparing(Comparator.comparing(keyExtractor, keyComparator.reversed()));
        }
        return Comparator.super.thenComparing(Comparator.comparing(keyExtractor, keyComparator));
    }

    /// Implements [Comparator#thenComparingInt(ToIntFunction)]: `Comparator`'s default.
    ///
    /// # Breaks
    /// - [#THEN_COMPARING_INT_IGNORES_THE_KEY]
    ///
    /// @param keyExtractor the function that extracts the key
    /// @return the lexicographic comparator
    /// @throws NullPointerException if `keyExtractor` is null
    @Override
    public Comparator<T> thenComparingInt(final ToIntFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        if (hasBreak(THEN_COMPARING_INT_IGNORES_THE_KEY)) {
            return this;
        }
        return Comparator.super.thenComparing(Comparator.comparingInt(keyExtractor));
    }

    /// Implements [Comparator#thenComparingLong(ToLongFunction)]: `Comparator`'s default.
    ///
    /// # Breaks
    /// - [#THEN_COMPARING_LONG_IGNORES_THE_KEY]
    ///
    /// @param keyExtractor the function that extracts the key
    /// @return the lexicographic comparator
    /// @throws NullPointerException if `keyExtractor` is null
    @Override
    public Comparator<T> thenComparingLong(final ToLongFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        if (hasBreak(THEN_COMPARING_LONG_IGNORES_THE_KEY)) {
            return this;
        }
        return Comparator.super.thenComparing(Comparator.comparingLong(keyExtractor));
    }

    /// Implements [Comparator#thenComparingDouble(ToDoubleFunction)]: `Comparator`'s default.
    ///
    /// # Breaks
    /// - [#THEN_COMPARING_DOUBLE_IGNORES_THE_KEY]
    ///
    /// @param keyExtractor the function that extracts the key
    /// @return the lexicographic comparator
    /// @throws NullPointerException if `keyExtractor` is null
    @Override
    public Comparator<T> thenComparingDouble(final ToDoubleFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        if (hasBreak(THEN_COMPARING_DOUBLE_IGNORES_THE_KEY)) {
            return this;
        }
        return Comparator.super.thenComparing(Comparator.comparingDouble(keyExtractor));
    }
}
