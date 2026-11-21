/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/// Factories of [ComparatorProvider]s: a comparator with given tiers, the natural and reverse orders over a
/// [ComparableProvider]'s samples, and `String.CASE_INSENSITIVE_ORDER`.
///
/// @since 0.1.4
public final class ComparatorProviders {

    private ComparatorProviders() { }

    /// A provider of the given comparator and tiers, lowest first, copied.
    ///
    /// @param comparator the comparator under test
    /// @param tiers      the tiers: at least one, none empty, each holding samples the comparator calls equal, every
    ///                   sample less than every sample of the next tier
    /// @param <T> the type of the samples
    /// @return the provider
    /// @throws NullPointerException if `comparator`, `tiers`, a tier or a sample is null
    /// @throws IllegalArgumentException if there are no tiers, or a tier is empty
    public static <T> ComparatorProvider<T> of(final Comparator<T> comparator,
                                               final List<? extends List<? extends T>> tiers) {
        Objects.requireNonNull(comparator, "comparator");
        List<List<T>> copy = ComparableProviders.checkedCopy(tiers, "comparator");
        return new ComparatorProvider<>() {
            @Override
            public Comparator<T> comparator() {
                return comparator;
            }

            @Override
            public List<List<T>> ascendingSamples() {
                return copy;
            }
        };
    }

    /// A provider of the given comparator and samples, lowest first, each in a tier of its own, so that the
    /// contract's `thenComparing` tests see no tie: they need a tier of two samples that are not the same object.
    ///
    /// @param comparator the comparator under test
    /// @param samples    the samples, each less than the next
    /// @param <T> the type of the samples
    /// @return the provider
    /// @throws NullPointerException if `comparator`, `samples` or a sample is null
    /// @throws IllegalArgumentException if there are no samples
    @SafeVarargs
    public static <T> ComparatorProvider<T> ascending(final Comparator<T> comparator, final T... samples) {
        Objects.requireNonNull(samples, "samples");
        for (int i = 0; i < samples.length; i++) {
            Objects.requireNonNull(samples[i], "sample " + i);
        }
        return of(comparator, Arrays.stream(samples).map(List::of).toList());
    }

    /// [Comparator#naturalOrder()] over the samples of a [ComparableProvider], in its tiers.
    ///
    /// @param samples the provider of the samples, read once
    /// @param <T> the type of the samples
    /// @return the provider
    public static <T extends Comparable<? super T>> ComparatorProvider<T> naturalOrder(
            final ComparableProvider<T> samples) {
        return of(Comparator.<T>naturalOrder(), samples.ascendingSamples());
    }

    /// [Comparator#reverseOrder()] over the samples of a [ComparableProvider], in its tiers, reversed.
    ///
    /// @param samples the provider of the samples, read once
    /// @param <T> the type of the samples
    /// @return the provider
    public static <T extends Comparable<? super T>> ComparatorProvider<T> reverseOrder(
            final ComparableProvider<T> samples) {
        List<List<T>> tiers = new ArrayList<>(samples.ascendingSamples());
        Collections.reverse(tiers);
        return of(Comparator.<T>reverseOrder(), tiers);
    }

    /// [String#CASE_INSENSITIVE_ORDER] over `""`, `"_"`, {`"A"`, `"a"`}, {`"ab"`, `"AB"`, `"aB"`}, {`"b"`, `"B"`},
    /// {`"i"`, `"I"`, `"ı"` (U+0131), `"İ"` (U+0130)}, `"SS"`, `"z"`, `"ß"`, {`"é"`, `"É"`} and {U+10400, U+10428}.
    ///
    /// - `"_"` (0x5F) comes before `"a"`: a comparison of upper case alone would put it after `"A"` (0x41).
    /// - The dotted and dotless i's are one tier: each folds, through upper case and then lower, to `"i"`.
    /// - `"SS"` comes before `"ß"`: there is no full case folding.
    /// - U+10400 and U+10428, a supplementary letter and its lower case, are one tier: the order folds code points,
    ///   not UTF-16 units.
    ///
    /// The order is not consistent with `equals`, so a test class of it overrides
    /// [com.soliscode.test.contract.support.ComparatorContractSupport#comparatorIsConsistentWithEquals()].
    ///
    /// @return the provider
    public static ComparatorProvider<String> caseInsensitiveOrder() {
        return of(String.CASE_INSENSITIVE_ORDER, List.of(List.of(""), List.of("_"), List.of("A", "a"),
                List.of("ab", "AB", "aB"), List.of("b", "B"), List.of("i", "I", "ı", "İ"), List.of("SS"),
                List.of("z"), List.of("ß"), List.of("é", "É"),
                List.of(Character.toString(0x10400), Character.toString(0x10428))));
    }
}
