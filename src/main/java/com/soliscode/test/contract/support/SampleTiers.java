/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.support;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

/// The check of a provider's tiers of ordered samples, which [ComparableContractSupport] and
/// [ComparatorContractSupport] share.
final class SampleTiers {

    private SampleTiers() { }

    /// Returns the tiers, checked: at least one, none empty, and no null sample. A provider that breaks these rules
    /// fails the test with a message that says the provider, not the class under test, is at fault.
    ///
    /// @param tiers the provider's tiers
    /// @param kind  the kind of provider, for the message: `"comparable"` or `"comparator"`
    /// @param <T> the type of the samples
    /// @return the tiers, lowest first
    static <T> List<List<T>> checked(final @Nullable List<List<T>> tiers, final String kind) {
        if (tiers == null || tiers.isEmpty()) {
            throw new AssertionFailedError("the " + kind + " provider is at fault: it gave no tiers of samples");
        }
        for (int i = 0; i < tiers.size(); i++) {
            List<T> tier = tiers.get(i);
            if (tier == null || tier.isEmpty()) {
                throw new AssertionFailedError("the " + kind + " provider is at fault: its tier " + i
                        + " is null or empty");
            }
            for (int j = 0; j < tier.size(); j++) {
                if (tier.get(j) == null) {
                    throw new AssertionFailedError("the " + kind + " provider is at fault: sample " + j
                            + " of its tier " + i + " is null");
                }
            }
        }
        return tiers;
    }
}
