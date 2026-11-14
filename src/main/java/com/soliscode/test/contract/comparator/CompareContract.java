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
package com.soliscode.test.contract.comparator;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ComparatorContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [compare(T, T)][Comparator#compare(Object, Object)] method of a [Comparator].
///
/// It follows the Javadoc of [Comparator] and [Comparator#compare(Object, Object)]. The three laws (sign symmetry,
/// transitivity, and that elements that compare as zero compare alike with every other) are tested on the results
/// observed between every pair, or triple, of [#lawSamples()]: the provider's samples, with `null` when the comparator
/// permits it. It reads the Javadoc more strictly in three places and more loosely in two:
/// - "less than" is read as the provider's order: the provider states the comparator's order, so a `compare` that
///   reverses it, or calls every pair equal, fails, though it keeps the laws.
/// - Consistency with `equals`, which the Javadoc does not require, is required by default; a comparator that does not
///   claim it opts out with [#comparatorIsConsistentWithEquals()].
/// - Null arguments, which the Javadoc lets a comparator permit, are refused by default, with
///   [NullPointerException]; a comparator that permits them opts in with [#comparatorPermitsNulls()].
/// - `ClassCastException` "if the arguments' types prevent them from being compared" is not tested: generics keep a
///   test from passing a wrong type but through a raw call, and which types are wrong depends on the comparator.
/// - That `compare(x, y)` throws exactly when `compare(y, x)` does is tested only for null, through the null test.
///
/// With fewer than three tiers of samples, the transitivity test may find no triple to check, and then passes.
///
/// @param <T> the type the comparator compares
/// @since 0.1.4
/// @see Comparator#compare(Object, Object)
public interface CompareContract<T> extends ComparatorContractSupport<T> {

    /// Tests that `compare` agrees with the provider's order: negative from a lower tier to a higher, positive from a
    /// higher to a lower, and zero within a tier.
    @Test
    @DisplayName("compare() follows the order of the provider's samples")
    default void compareFollowsTheSamplesOrder() {
        Comparator<T> c = comparator();
        List<Sample<T>> samples = samples();
        for (final Sample<T> x : samples) {
            for (final Sample<T> y : samples) {
                int expected = Integer.compare(x.tier(), y.tier());
                int actual = Integer.signum(c.compare(x.value(), y.value()));
                assertEquals(expected, actual, () -> "signum(compare(" + x + ", " + y + "))");
            }
        }
    }

    /// Tests that `signum(compare(x, y)) == -signum(compare(y, x))` for every pair of law samples, each with itself
    /// included, for which the law makes `compare(x, x)` zero.
    @Test
    @DisplayName("compare() is sign-symmetric: compare(x, y) and compare(y, x) have opposite signs")
    default void compareIsSignSymmetric() {
        Comparator<T> c = comparator();
        List<LawSample<T>> samples = lawSamples();
        for (final LawSample<T> x : samples) {
            for (final LawSample<T> y : samples) {
                int xy = Integer.signum(c.compare(x.value(), y.value()));
                int yx = Integer.signum(c.compare(y.value(), x.value()));
                assertEquals(-yx, xy, () -> "signum(compare(" + x + ", " + y + ")) is " + xy + ", and signum(compare("
                        + y + ", " + x + ")) is " + yx);
            }
        }
    }

    /// Tests that `compare(x, y) > 0` and `compare(y, z) > 0` imply `compare(x, z) > 0`, for every triple of law
    /// samples.
    @Test
    @DisplayName("compare() is transitive: x > y and y > z imply x > z")
    default void compareIsTransitive() {
        Comparator<T> c = comparator();
        List<LawSample<T>> samples = lawSamples();
        for (final LawSample<T> x : samples) {
            for (final LawSample<T> y : samples) {
                if (c.compare(x.value(), y.value()) <= 0) {
                    continue;
                }
                for (final LawSample<T> z : samples) {
                    if (c.compare(y.value(), z.value()) > 0) {
                        int xz = Integer.signum(c.compare(x.value(), z.value()));
                        assertTrue(xz > 0, () -> x + " > " + y + " and " + y + " > " + z + ", but signum(compare(" + x
                                + ", " + z + ")) is " + xz);
                    }
                }
            }
        }
    }

    /// Tests that `compare(x, y) == 0` implies `signum(compare(x, z)) == signum(compare(y, z))`, for every `z`, over
    /// the law samples.
    @Test
    @DisplayName("compare(): elements that compare as equal compare alike with every other")
    default void compareZeroComparesAlikeWithOthers() {
        Comparator<T> c = comparator();
        List<LawSample<T>> samples = lawSamples();
        for (final LawSample<T> x : samples) {
            for (final LawSample<T> y : samples) {
                if (c.compare(x.value(), y.value()) != 0) {
                    continue;
                }
                for (final LawSample<T> z : samples) {
                    int xz = Integer.signum(c.compare(x.value(), z.value()));
                    int yz = Integer.signum(c.compare(y.value(), z.value()));
                    assertEquals(xz, yz, () -> "compare(" + x + ", " + y + ") is 0, but signum(compare(" + x + ", " + z
                            + ")) is " + xz + " and signum(compare(" + y + ", " + z + ")) is " + yz);
                }
            }
        }
    }

    /// Tests that `compare` throws [NullPointerException] when either argument, or both, is null, as
    /// [Comparator#compare(Object, Object)] says it does where the comparator does not permit nulls, unless
    /// [#comparatorPermitsNulls()].
    @Test
    @DisplayName("compare() with a null argument throws NullPointerException, unless the comparator permits nulls")
    default void compareNullThrowsNullPointerException() {
        if (comparatorPermitsNulls()) {
            return;
        }
        Comparator<T> c = comparator();
        for (final Sample<T> x : samples()) {
            assertThrows(NullPointerException.class, () -> c.compare(null, x.value()),
                    () -> "compare(null, " + x + ")");
            assertThrows(NullPointerException.class, () -> c.compare(x.value(), null),
                    () -> "compare(" + x + ", null)");
        }
        assertThrows(NullPointerException.class, () -> c.compare(null, null), "compare(null, null)");
    }

    /// Tests that `compare(x, y) == 0` exactly when `x.equals(y)`, for every pair of samples, unless the comparator
    /// does not claim it ([#comparatorIsConsistentWithEquals()]).
    @Test
    @DisplayName("compare() is consistent with equals(), where the comparator claims it")
    default void compareIsConsistentWithEquals() {
        if (!comparatorIsConsistentWithEquals()) {
            return;
        }
        Comparator<T> c = comparator();
        List<Sample<T>> samples = samples();
        for (final Sample<T> x : samples) {
            for (final Sample<T> y : samples) {
                boolean zero = c.compare(x.value(), y.value()) == 0;
                boolean equal = x.value().equals(y.value());
                assertEquals(zero, equal, () -> "compare(" + x + ", " + y + ") == 0 is " + zero + ", but " + x
                        + ".equals(" + y + ") is " + equal);
            }
        }
    }
}
