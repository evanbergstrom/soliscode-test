/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;

import java.util.List;

/// Makes the ordered samples a [com.soliscode.test.contract.comparable.ComparableContract] tests: samples of the class
/// under test, in tiers, in the class's natural order.
///
/// Each tier holds one or more samples that compare as equal to each other, and every sample of a tier is less than
/// every sample of the next. The contract reads this order as the one the class documents: a `compareTo` that
/// disagrees with it fails, even where it keeps `Comparable`'s laws.
///
/// ## Usage Examples
/// ```java
/// class MoneyTest extends AbstractTest implements ComparableContract<Money> {
///     @Override
///     public ComparableProvider<Money> comparableProvider() {
///         return ComparableProviders.ascending(Money.of(-1), Money.ZERO, Money.of(5));
///     }
/// }
/// ```
///
/// @param <T> the type of the samples
/// @since 0.1.3
/// @see ComparableProviders
@FunctionalInterface
public interface ComparableProvider<T extends Comparable<? super T>> {

    /// Returns the samples, in tiers, in ascending order. There is at least one tier, no tier is empty, and no sample
    /// is null. A contract does not change them.
    ///
    /// @return the tiers, lowest first
    List<List<T>> ascendingSamples();
}
