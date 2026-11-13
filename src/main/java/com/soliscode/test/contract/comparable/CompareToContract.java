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
package com.soliscode.test.contract.comparable;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ComparableContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [compareTo(T)][Comparable#compareTo(Object)] method of a [Comparable].
///
/// It follows the Javadoc of [Comparable] and [Comparable#compareTo(Object)]. The three laws (sign symmetry,
/// transitivity, and that elements that compare as equal compare alike with every other) are tested on the results
/// observed between every pair, or triple, of the provider's samples. It reads the Javadoc more strictly in two places
/// and more loosely in two:
/// - "less than" is read as the provider's order: the provider states the class's natural order, so a `compareTo`
///   that reverses it, or calls every pair equal, fails, though it keeps the laws.
/// - Consistency with `equals`, which the Javadoc strongly recommends without requiring, is required by default; a
///   class that does not claim it opts out with [#naturalOrderingIsConsistentWithEquals()].
/// - `ClassCastException` "if the specified object's type prevents it from being compared" is not tested: generics
///   keep a test from passing a wrong type but through a raw call, and which types are wrong depends on the class
///   (a `Comparable<Object>` takes every object).
/// - That `x.compareTo(y)` throws exactly when `y.compareTo(x)` does is tested only for null, through the null test:
///   no compatible sample should throw.
///
/// With fewer than three tiers of samples, the transitivity test may find no triple to check, and then passes.
///
/// @param <T> the type under test
/// @since 0.1.3
/// @see Comparable#compareTo(Object)
public interface CompareToContract<T extends Comparable<? super T>> extends ComparableContractSupport<T> {

    /// Tests that `compareTo` agrees with the provider's order: negative from a lower tier to a higher, positive from
    /// a higher to a lower, and zero within a tier.
    @Test
    @DisplayName("compareTo() follows the order of the provider's samples")
    default void compareToFollowsTheSamplesOrder() {
        List<Sample<T>> samples = samples();
        for (final Sample<T> x : samples) {
            for (final Sample<T> y : samples) {
                int expected = Integer.compare(x.tier(), y.tier());
                int actual = Integer.signum(x.value().compareTo(y.value()));
                assertEquals(expected, actual, () -> "signum(" + x + ".compareTo(" + y + "))");
            }
        }
    }

    /// Tests that `signum(x.compareTo(y)) == -signum(y.compareTo(x))` for every pair of samples, each with itself
    /// included, for which the law makes `x.compareTo(x)` zero.
    @Test
    @DisplayName("compareTo() is sign-symmetric: x.compareTo(y) and y.compareTo(x) have opposite signs")
    default void compareToIsSignSymmetric() {
        List<Sample<T>> samples = samples();
        for (final Sample<T> x : samples) {
            for (final Sample<T> y : samples) {
                int xy = Integer.signum(x.value().compareTo(y.value()));
                int yx = Integer.signum(y.value().compareTo(x.value()));
                assertEquals(-yx, xy, () -> "signum(" + x + ".compareTo(" + y + ")) is " + xy + ", and signum(" + y
                        + ".compareTo(" + x + ")) is " + yx);
            }
        }
    }

    /// Tests that `x.compareTo(y) > 0` and `y.compareTo(z) > 0` imply `x.compareTo(z) > 0`, for every triple of
    /// samples.
    @Test
    @DisplayName("compareTo() is transitive: x > y and y > z imply x > z")
    default void compareToIsTransitive() {
        List<Sample<T>> samples = samples();
        for (final Sample<T> x : samples) {
            for (final Sample<T> y : samples) {
                if (x.value().compareTo(y.value()) <= 0) {
                    continue;
                }
                for (final Sample<T> z : samples) {
                    if (y.value().compareTo(z.value()) > 0) {
                        int xz = Integer.signum(x.value().compareTo(z.value()));
                        assertTrue(xz > 0, () -> x + " > " + y + " and " + y + " > " + z + ", but signum(" + x
                                + ".compareTo(" + z + ")) is " + xz);
                    }
                }
            }
        }
    }

    /// Tests that `x.compareTo(y) == 0` implies `signum(x.compareTo(z)) == signum(y.compareTo(z))`, for every `z`.
    @Test
    @DisplayName("compareTo(): elements that compare as equal compare alike with every other")
    default void compareToZeroComparesAlikeWithOthers() {
        List<Sample<T>> samples = samples();
        for (final Sample<T> x : samples) {
            for (final Sample<T> y : samples) {
                if (x.value().compareTo(y.value()) != 0) {
                    continue;
                }
                for (final Sample<T> z : samples) {
                    int xz = Integer.signum(x.value().compareTo(z.value()));
                    int yz = Integer.signum(y.value().compareTo(z.value()));
                    assertEquals(xz, yz, () -> x + ".compareTo(" + y + ") is 0, but signum(" + x + ".compareTo(" + z
                            + ")) is " + xz + " and signum(" + y + ".compareTo(" + z + ")) is " + yz);
                }
            }
        }
    }

    /// Tests that `compareTo(null)` throws [NullPointerException], as [Comparable]'s Javadoc says it should, though
    /// `equals(null)` returns `false`.
    @Test
    @DisplayName("compareTo(null) throws NullPointerException")
    default void compareToNullThrowsNullPointerException() {
        for (final Sample<T> x : samples()) {
            assertThrows(NullPointerException.class, () -> x.value().compareTo(null), () -> x + ".compareTo(null)");
        }
    }

    /// Tests that `x.compareTo(y) == 0` exactly when `x.equals(y)`, for every pair of samples, unless the class does
    /// not claim it ([#naturalOrderingIsConsistentWithEquals()]).
    @Test
    @DisplayName("compareTo() is consistent with equals(), where the class claims it")
    default void compareToIsConsistentWithEquals() {
        if (!naturalOrderingIsConsistentWithEquals()) {
            return;
        }
        List<Sample<T>> samples = samples();
        for (final Sample<T> x : samples) {
            for (final Sample<T> y : samples) {
                boolean zero = x.value().compareTo(y.value()) == 0;
                boolean equal = x.value().equals(y.value());
                assertEquals(zero, equal, () -> x + ".compareTo(" + y + ") == 0 is " + zero + ", but " + x
                        + ".equals(" + y + ") is " + equal);
            }
        }
    }
}
