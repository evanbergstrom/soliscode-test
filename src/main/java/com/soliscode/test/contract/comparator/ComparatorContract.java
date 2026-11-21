/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.comparator;

/// **Contract for the [java.util.Comparator] interface.**
///
/// A test class implements it with a [com.soliscode.test.provider.ComparatorProvider] of the comparator under test and
/// samples in tiers in its order; JUnit runs its test methods as the class's own. Where the comparator's ordering is
/// not consistent with `equals`, as `String.CASE_INSENSITIVE_ORDER`'s is not, the class overrides
/// [com.soliscode.test.contract.support.ComparatorContractSupport#comparatorIsConsistentWithEquals()] to return
/// `false`; where the comparator permits null arguments, it overrides
/// [com.soliscode.test.contract.support.ComparatorContractSupport#comparatorPermitsNulls()] to return `true`.
///
/// It tests `compare` ([CompareContract]), `reversed` ([ReversedContract]), the `thenComparing` forms
/// ([ThenComparingContract]) and the `nullsFirst` and `nullsLast` wrappers made from the comparator
/// ([NullsFirstAndNullsLastContract]); each says how it reads the Javadoc. `Comparator.equals(Object)` is not tested:
/// its requirement beyond `Object.equals`, that equal comparators impose the same ordering, is not tested here or by
/// `ObjectContract`.
///
/// It is not `contract.sortedmap.ComparatorContract` or `contract.sortedset.ComparatorContract`, which test the
/// `comparator()` method of a sorted map or set.
///
/// ## Usage Examples
/// ```java
/// class NaturalOrderTest extends AbstractTest implements ComparatorContract<Integer> {
///     @Override
///     public ComparatorProvider<Integer> comparatorProvider() {
///         return ComparatorProviders.naturalOrder(ComparableProviders.integers());
///     }
/// }
///
/// class CaseInsensitiveOrderTest extends AbstractTest implements ComparatorContract<String> {
///     @Override
///     public ComparatorProvider<String> comparatorProvider() {
///         return ComparatorProviders.caseInsensitiveOrder();
///     }
///
///     @Override
///     public boolean comparatorIsConsistentWithEquals() {
///         return false;
///     }
/// }
/// ```
///
/// @param <T> the type the comparator compares
/// @since 0.1.4
/// @see java.util.Comparator
public interface ComparatorContract<T> extends CompareContract<T>, ReversedContract<T>, ThenComparingContract<T>,
        NullsFirstAndNullsLastContract<T> {
}
