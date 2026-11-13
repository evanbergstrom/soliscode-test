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
import java.util.List;

import org.opentest4j.AssertionFailedError;

/// The base interface of the comparable contracts: it gives them the ordered samples, checked, and whether the class
/// under test claims that its natural ordering is consistent with `equals`.
///
/// @param <T> the type under test
/// @since 0.1.3
public interface ComparableContractSupport<T extends Comparable<? super T>> extends ComparableProviderSupport<T> {

    /// Whether the class under test claims that its natural ordering is consistent with `equals`: that
    /// `x.compareTo(y) == 0` exactly when `x.equals(y)`. [Comparable]'s Javadoc strongly recommends it, without
    /// requiring it; a class that does not claim it (`BigDecimal`) overrides this to return `false`, and the test of it
    /// is skipped (the user's decision on #40).
    ///
    /// @return `true` unless overridden
    default boolean naturalOrderingIsConsistentWithEquals() {
        return true;
    }

    /// Returns the provider's tiers, checked: at least one, none empty, and no null sample. A provider that breaks
    /// these rules fails the test with a message that says the provider, not the class under test, is at fault.
    ///
    /// @return the tiers, lowest first
    default List<List<T>> sampleTiers() {
        List<List<T>> tiers = comparableProvider().ascendingSamples();
        if (tiers == null || tiers.isEmpty()) {
            throw new AssertionFailedError("the comparable provider is at fault: it gave no tiers of samples");
        }
        for (int i = 0; i < tiers.size(); i++) {
            List<T> tier = tiers.get(i);
            if (tier == null || tier.isEmpty()) {
                throw new AssertionFailedError("the comparable provider is at fault: its tier " + i
                        + " is null or empty");
            }
            for (int j = 0; j < tier.size(); j++) {
                if (tier.get(j) == null) {
                    throw new AssertionFailedError("the comparable provider is at fault: sample " + j
                            + " of its tier " + i + " is null");
                }
            }
        }
        return tiers;
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

    /// A sample, with its place among the provider's tiers, so that a failure message can tell apart samples that
    /// print alike.
    ///
    /// @param value the sample
    /// @param tier its tier, from 0, lowest first
    /// @param index its index in the tier
    /// @param <T> the type under test
    record Sample<T>(T value, int tier, int index) {

        /// Returns the sample, with its tier and index.
        ///
        /// @return the description
        @Override
        public String toString() {
            return value + " (tier " + tier + ", sample " + index + ")";
        }
    }
}
