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
package com.soliscode.test.contract.support;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

/// The base interface of the comparator contracts: it gives them the comparator under test, its ordered samples,
/// checked, the positions of the samples a test uses, and what the comparator claims of `equals` and of nulls.
///
/// @param <T> the type the comparator compares
/// @since 0.1.4
public interface ComparatorContractSupport<T> extends ComparatorProviderSupport<T> {

    /// Whether the comparator under test claims that its ordering is consistent with `equals`: that
    /// `compare(x, y) == 0` exactly when `x.equals(y)`. [Comparator]'s Javadoc does not require it; the contract
    /// requires it unless a test class overrides this to return `false`, as one of `String.CASE_INSENSITIVE_ORDER`
    /// does, and the test of it is then skipped (the user's decision on #42).
    ///
    /// @return `true` unless overridden
    default boolean comparatorIsConsistentWithEquals() {
        return true;
    }

    /// Whether the comparator under test permits null arguments, which [Comparator]'s Javadoc allows. Unless a test
    /// class overrides this to return `true`, `compare` with a null argument must throw [NullPointerException]; when it
    /// does, the laws are tested with `null` among the samples (the user's decision on #42).
    ///
    /// @return `false` unless overridden
    default boolean comparatorPermitsNulls() {
        return false;
    }

    /// Returns the provider's comparator, checked: a provider that gives none fails the test with a message that says
    /// the provider, not the comparator, is at fault.
    ///
    /// @return the comparator under test
    default Comparator<T> comparator() {
        @Nullable Comparator<T> comparator = comparatorProvider().comparator();
        if (comparator == null) {
            throw new AssertionFailedError("the comparator provider is at fault: it gave no comparator");
        }
        return comparator;
    }

    /// Returns the provider's tiers, checked: at least one, none empty, and no null sample. A provider that breaks
    /// these rules fails the test with a message that says the provider, not the comparator, is at fault.
    ///
    /// @return the tiers, lowest first
    default List<List<T>> sampleTiers() {
        return SampleTiers.checked(comparatorProvider().ascendingSamples(), "comparator");
    }

    /// Returns the samples of every tier, lowest first, each with its tier and index.
    ///
    /// @return the samples
    default List<Sample<T>> samples() {
        List<List<T>> tiers = sampleTiers();
        List<Sample<T>> samples = new ArrayList<>();
        for (int i = 0; i < tiers.size(); i++) {
            for (int j = 0; j < tiers.get(i).size(); j++) {
                samples.add(new Sample<>(tiers.get(i).get(j), i, j));
            }
        }
        return samples;
    }

    /// Returns the samples the laws are tested over: the samples, with one `null` sample last when
    /// [#comparatorPermitsNulls()]. The `null` sample has no tier: only the order test reads tiers, and it uses
    /// [#samples()].
    ///
    /// @return the samples, with `null` if the comparator permits it
    default List<LawSample<T>> lawSamples() {
        List<LawSample<T>> samples = new ArrayList<>();
        for (final Sample<T> s : samples()) {
            samples.add(new LawSample<>(s));
        }
        if (comparatorPermitsNulls()) {
            samples.add(new LawSample<>(null));
        }
        return samples;
    }

    /// Returns the positions of the given samples, which a test takes from one call of [#samples()], so that the
    /// objects it compares are the ones it finds.
    ///
    /// @param samples the samples the test compares
    /// @return their positions
    default SamplePositions<T> positions(final List<Sample<T>> samples) {
        return new SamplePositions<>(samples);
    }

    /// A sample, with its place among the provider's tiers, so that a failure message can tell apart samples that
    /// print alike.
    ///
    /// @param value the sample
    /// @param tier its tier, from 0, lowest first
    /// @param index its index in the tier
    /// @param <T> the type of the samples
    record Sample<T>(T value, int tier, int index) {

        /// Returns the sample, with its tier and index.
        ///
        /// @return the description
        @Override
        public String toString() {
            return value + " (tier " + tier + ", sample " + index + ")";
        }
    }

    /// A sample the laws are tested over: one of the provider's samples, or `null`.
    ///
    /// @param sample the sample, or `null` for the null law sample
    /// @param <T> the type of the samples
    record LawSample<T>(@Nullable Sample<T> sample) {

        /// Returns the sample's value, or `null` for the null law sample.
        ///
        /// @return the value
        public @Nullable T value() {
            return sample == null ? null : sample.value();
        }

        /// Returns the sample, with its tier and index, or `null`.
        ///
        /// @return the description
        @Override
        public String toString() {
            return sample == null ? "null" : sample.toString();
        }
    }

    /// The positions of a test's samples, by identity, in the order of the list they came from: the first occurrence,
    /// where one object appears twice. They give the `thenComparing` tests their tie-breaker and their keys.
    ///
    /// @param <T> the type of the samples
    final class SamplePositions<T> {

        private final IdentityHashMap<T, Integer> positions = new IdentityHashMap<>();

        /// Finds the positions of the samples.
        ///
        /// @param samples the samples, in order
        SamplePositions(final List<Sample<T>> samples) {
            for (int i = 0; i < samples.size(); i++) {
                positions.putIfAbsent(samples.get(i).value(), i);
            }
        }

        /// Returns the position of a sample.
        ///
        /// @param value the sample
        /// @return its position, from 0
        /// @throws AssertionFailedError if `value` is null or is not one of the samples: the comparator under test
        ///         passed its key extractor or tie-breaker an object it was not asked to compare
        public int position(final @Nullable T value) {
            Integer position = value == null ? null : positions.get(value);
            if (position == null) {
                throw new AssertionFailedError("the tie-breaker was given " + value
                        + ", which is not one of the samples compared");
            }
            return position;
        }

        /// Returns the tie-breaker: a comparator by descending position. Across tiers it disagrees with the comparator
        /// under test, so that a `thenComparing` that consults it first fails.
        ///
        /// @return the tie-breaker
        public Comparator<T> tieBreaker() {
            return (a, b) -> Integer.compare(position(b), position(a));
        }
    }
}
