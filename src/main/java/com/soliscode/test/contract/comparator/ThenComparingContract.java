/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.comparator;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ComparatorContractSupport;

import static com.soliscode.test.contract.comparator.ComparatorChecks.assertOrdersAs;
import static com.soliscode.test.contract.comparator.ComparatorChecks.lexicographic;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the `thenComparing` methods of a [Comparator]: [Comparator#thenComparing(Comparator)],
/// [Comparator#thenComparing(Function)], [Comparator#thenComparing(Function, Comparator)],
/// [Comparator#thenComparingInt(ToIntFunction)], [Comparator#thenComparingLong(ToLongFunction)] and
/// [Comparator#thenComparingDouble(ToDoubleFunction)].
///
/// It follows their Javadoc: each returns "a lexicographic-order comparator", which orders as the comparator under
/// test does and, where it gives zero, by the other comparator or the key; each throws [NullPointerException] for a
/// null argument. The expected order is built from the results the comparator under test is observed to give, so that
/// a fault of `compare` is reported by the [CompareContract] tests, not here.
///
/// The ties are broken by the samples' positions, descending, so that a comparator that consults the tie-breaker
/// first disagrees with one that consults it second. The tests run over the provider's samples, not null, and see a
/// tie only within a tier of two or more samples that are not the same object (cached boxes such as
/// `Integer.valueOf(2)` and string literals are): with no such tier, a `thenComparing` that ignores its argument
/// passes.
///
/// @param <T> the type the comparator compares
/// @since 0.1.4
/// @see Comparator#thenComparing(Comparator)
public interface ThenComparingContract<T> extends ComparatorContractSupport<T> {

    /// Tests that `thenComparing(other)` orders as the comparator, and its ties as `other`.
    @Test
    @DisplayName("thenComparing(other) breaks the comparator's ties with the other comparator")
    default void thenComparingBreaksTiesWithTheOtherComparator() {
        Comparator<T> c = comparator();
        List<Sample<T>> samples = samples();
        Comparator<T> tieBreaker = positions(samples).tieBreaker();
        assertOrdersAs("thenComparing(tieBreaker)", c.thenComparing(tieBreaker),
                (x, y) -> lexicographic(c, tieBreaker, x, y), samples);
    }

    /// Tests that `thenComparing(keyExtractor)` orders as the comparator, and its ties by the key's natural order.
    @Test
    @DisplayName("thenComparing(keyExtractor) breaks the comparator's ties with the key")
    default void thenComparingByKeyBreaksTiesWithTheKey() {
        Comparator<T> c = comparator();
        List<Sample<T>> samples = samples();
        SamplePositions<T> positions = positions(samples);
        Function<T, Integer> key = x -> -positions.position(x);
        assertOrdersAs("thenComparing(key)", c.thenComparing(key),
                (x, y) -> lexicographic(c, positions.tieBreaker(), x, y), samples);
    }

    /// Tests that `thenComparing(keyExtractor, keyComparator)` orders as the comparator, and its ties by the key
    /// compared with `keyComparator`.
    @Test
    @DisplayName("thenComparing(keyExtractor, keyComparator) breaks the comparator's ties with the key comparator")
    default void thenComparingByKeyAndComparatorBreaksTiesWithTheKeyComparator() {
        Comparator<T> c = comparator();
        List<Sample<T>> samples = samples();
        SamplePositions<T> positions = positions(samples);
        Function<T, Integer> key = x -> -positions.position(x);
        Comparator<T> ascending = (x, y) -> Integer.compare(positions.position(x), positions.position(y));
        assertOrdersAs("thenComparing(key, reverseOrder())", c.thenComparing(key, Comparator.reverseOrder()),
                (x, y) -> lexicographic(c, ascending, x, y), samples);
    }

    /// Tests that `thenComparingInt(keyExtractor)` orders as the comparator, and its ties by the `int` key.
    @Test
    @DisplayName("thenComparingInt() breaks the comparator's ties with the key")
    default void thenComparingIntBreaksTiesWithTheKey() {
        Comparator<T> c = comparator();
        List<Sample<T>> samples = samples();
        SamplePositions<T> positions = positions(samples);
        ToIntFunction<T> key = x -> -positions.position(x);
        assertOrdersAs("thenComparingInt(key)", c.thenComparingInt(key),
                (x, y) -> lexicographic(c, positions.tieBreaker(), x, y), samples);
    }

    /// Tests that `thenComparingLong(keyExtractor)` orders as the comparator, and its ties by the `long` key.
    @Test
    @DisplayName("thenComparingLong() breaks the comparator's ties with the key")
    default void thenComparingLongBreaksTiesWithTheKey() {
        Comparator<T> c = comparator();
        List<Sample<T>> samples = samples();
        SamplePositions<T> positions = positions(samples);
        ToLongFunction<T> key = x -> -(long) positions.position(x);
        assertOrdersAs("thenComparingLong(key)", c.thenComparingLong(key),
                (x, y) -> lexicographic(c, positions.tieBreaker(), x, y), samples);
    }

    /// Tests that `thenComparingDouble(keyExtractor)` orders as the comparator, and its ties by the `double` key.
    @Test
    @DisplayName("thenComparingDouble() breaks the comparator's ties with the key")
    default void thenComparingDoubleBreaksTiesWithTheKey() {
        Comparator<T> c = comparator();
        List<Sample<T>> samples = samples();
        SamplePositions<T> positions = positions(samples);
        ToDoubleFunction<T> key = x -> -(double) positions.position(x);
        assertOrdersAs("thenComparingDouble(key)", c.thenComparingDouble(key),
                (x, y) -> lexicographic(c, positions.tieBreaker(), x, y), samples);
    }

    /// Tests that each `thenComparing` form throws [NullPointerException] for each null argument.
    @Test
    @DisplayName("thenComparing() with a null argument throws NullPointerException, in each form")
    default void thenComparingNullArgumentThrowsNullPointerException() {
        Comparator<T> c = comparator();
        Function<T, Integer> key = x -> 0;
        assertThrows(NullPointerException.class, () -> c.thenComparing((Comparator<T>) null),
                "thenComparing((Comparator) null)");
        assertThrows(NullPointerException.class, () -> c.thenComparing((Function<T, Integer>) null),
                "thenComparing((Function) null)");
        assertThrows(NullPointerException.class, () -> c.thenComparing(null, Comparator.<Integer>naturalOrder()),
                "thenComparing(null, keyComparator)");
        assertThrows(NullPointerException.class, () -> c.thenComparing(key, null),
                "thenComparing(keyExtractor, null)");
        assertThrows(NullPointerException.class, () -> c.thenComparingInt(null), "thenComparingInt(null)");
        assertThrows(NullPointerException.class, () -> c.thenComparingLong(null), "thenComparingLong(null)");
        assertThrows(NullPointerException.class, () -> c.thenComparingDouble(null), "thenComparingDouble(null)");
    }
}
