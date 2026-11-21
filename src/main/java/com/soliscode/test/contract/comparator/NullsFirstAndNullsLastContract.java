/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.comparator;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ComparatorContractSupport;

import static com.soliscode.test.contract.comparator.ComparatorChecks.assertNullEqualsNull;
import static com.soliscode.test.contract.comparator.ComparatorChecks.assertNullPlaced;
import static com.soliscode.test.contract.comparator.ComparatorChecks.assertOrdersAs;
import static com.soliscode.test.contract.comparator.ComparatorChecks.lexicographic;

/// Contract for the null-friendly wrappers of a [Comparator]: [Comparator#nullsFirst(Comparator)] and
/// [Comparator#nullsLast(Comparator)] made from the comparator under test.
///
/// It follows their Javadoc: the wrapper puts null before, or after, every non-null element, calls two nulls equal,
/// and orders two non-null elements by the comparator it wraps. `nullsFirst` and `nullsLast` are static, so the
/// comparator under test cannot change how they handle null; but the JDK's wrapper builds its own `reversed()` and
/// `thenComparing(other)` from the comparator it wraps (`nullsFirst(c).reversed()` is `nullsLast(c.reversed())`). So
/// each test checks the wrapper, its `reversed()` and its `thenComparing` with a tie-breaker, and fails through the
/// comparator under test only by way of the JDK's implementation: a comparator whose `reversed()` or `thenComparing`
/// is wrong.
///
/// The non-null order expected is built from the results the comparator under test is observed to give. Null against
/// null is checked on the wrapper and on its `reversed()`, where the wrapper's Javadoc settles it ("when both are null,
/// they are considered equal"), but not on its `thenComparing`, where `thenComparing`'s Javadoc would consult the
/// tie-breaker and the JDK does not.
///
/// @param <T> the type the comparator compares
/// @since 0.1.4
/// @see Comparator#nullsFirst(Comparator)
/// @see Comparator#nullsLast(Comparator)
public interface NullsFirstAndNullsLastContract<T> extends ComparatorContractSupport<T> {

    /// Tests that `nullsFirst(c)` puts null first and orders the samples as `c`; that its `reversed()` puts null last
    /// and orders the samples in reverse; and that its `thenComparing(tieBreaker)` puts null first and breaks `c`'s
    /// ties with the tie-breaker.
    @Test
    @DisplayName("nullsFirst() of the comparator puts null first, and so do its reversed() and thenComparing()")
    default void nullsFirstPutsNullFirst() {
        assertWrapper("nullsFirst(c)", true);
    }

    /// Tests that `nullsLast(c)` puts null last and orders the samples as `c`; that its `reversed()` puts null first
    /// and orders the samples in reverse; and that its `thenComparing(tieBreaker)` puts null last and breaks `c`'s
    /// ties with the tie-breaker.
    @Test
    @DisplayName("nullsLast() of the comparator puts null last, and so do its reversed() and thenComparing()")
    default void nullsLastPutsNullLast() {
        assertWrapper("nullsLast(c)", false);
    }

    /// Checks a wrapper, its `reversed()` and its `thenComparing(tieBreaker)`.
    ///
    /// @param name      the wrapper's name, for failures
    /// @param nullFirst whether it is `nullsFirst`
    private void assertWrapper(final String name, final boolean nullFirst) {
        Comparator<T> c = comparator();
        List<Sample<T>> samples = samples();
        Comparator<T> tieBreaker = positions(samples).tieBreaker();
        Comparator<T> wrapper = nullFirst ? Comparator.nullsFirst(c) : Comparator.nullsLast(c);

        assertNullPlaced(name, wrapper, nullFirst, samples);
        assertNullEqualsNull(name, wrapper);
        assertOrdersAs(name, wrapper, c, samples);

        String reversedName = name + ".reversed()";
        Comparator<T> reversed = wrapper.reversed();
        assertNullPlaced(reversedName, reversed, !nullFirst, samples);
        assertNullEqualsNull(reversedName, reversed);
        assertOrdersAs(reversedName, reversed, (x, y) -> c.compare(y, x), samples);

        String thenName = name + ".thenComparing(tieBreaker)";
        Comparator<T> then = wrapper.thenComparing(tieBreaker);
        assertNullPlaced(thenName, then, nullFirst, samples);
        assertOrdersAs(thenName, then, (x, y) -> lexicographic(c, tieBreaker, x, y), samples);
    }
}
