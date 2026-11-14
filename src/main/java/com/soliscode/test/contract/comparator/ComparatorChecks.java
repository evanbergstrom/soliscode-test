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

import com.soliscode.test.contract.support.ComparatorContractSupport.Sample;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// The assertions the comparator contracts share: that a comparator made from the one under test orders the samples
/// as expected, and where it puts null.
final class ComparatorChecks {

    private ComparatorChecks() { }

    /// The lexicographic order of two comparators, as `thenComparing` documents it: `first`, and where it gives zero,
    /// `then`.
    ///
    /// @param first the comparator consulted first
    /// @param then  the comparator that breaks its ties
    /// @param x     the first object
    /// @param y     the second object
    /// @param <T> the type of the objects
    /// @return the sign of the lexicographic comparison
    static <T> int lexicographic(final Comparator<T> first, final Comparator<T> then, final T x, final T y) {
        int r = Integer.signum(first.compare(x, y));
        return r != 0 ? r : Integer.signum(then.compare(x, y));
    }

    /// Asserts that `actual` orders every pair of samples as `expected` does, by sign.
    ///
    /// @param name     how to name `actual` in a failure
    /// @param actual   the comparator checked
    /// @param expected the comparator whose results it must agree with
    /// @param samples  the samples
    /// @param <T> the type of the samples
    static <T> void assertOrdersAs(final String name, final Comparator<T> actual, final Comparator<T> expected,
                                   final List<Sample<T>> samples) {
        for (final Sample<T> x : samples) {
            for (final Sample<T> y : samples) {
                int e = Integer.signum(expected.compare(x.value(), y.value()));
                int a = Integer.signum(actual.compare(x.value(), y.value()));
                assertEquals(e, a, () -> "signum(" + name + ".compare(" + x + ", " + y + "))");
            }
        }
    }

    /// Asserts that `actual` puts null before every sample, or after it.
    ///
    /// @param name      how to name `actual` in a failure
    /// @param actual    the comparator checked
    /// @param nullFirst whether null must come first
    /// @param samples   the samples
    /// @param <T> the type of the samples
    static <T> void assertNullPlaced(final String name, final Comparator<T> actual, final boolean nullFirst,
                                     final List<Sample<T>> samples) {
        int before = nullFirst ? -1 : 1;
        for (final Sample<T> x : samples) {
            assertEquals(before, Integer.signum(actual.compare(null, x.value())),
                    () -> "signum(" + name + ".compare(null, " + x + "))");
            assertEquals(-before, Integer.signum(actual.compare(x.value(), null)),
                    () -> "signum(" + name + ".compare(" + x + ", null))");
        }
    }

    /// Asserts that `actual` calls two nulls equal.
    ///
    /// @param name   how to name `actual` in a failure
    /// @param actual the comparator checked
    /// @param <T> the type it compares
    static <T> void assertNullEqualsNull(final String name, final Comparator<T> actual) {
        assertEquals(0, Integer.signum(actual.compare(null, null)), () -> "signum(" + name + ".compare(null, null))");
    }
}
