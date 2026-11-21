/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.comparable;

/// **Contract for the [Comparable] interface.**
///
/// A test class implements it with a [com.soliscode.test.provider.ComparableProvider] of ordered samples of the class
/// under test; JUnit runs its test methods as the class's own. Where the class's natural ordering is not consistent
/// with `equals`, as `BigDecimal`'s is not, the class overrides
/// [com.soliscode.test.contract.support.ComparableContractSupport#naturalOrderingIsConsistentWithEquals()] to return
/// `false`.
///
/// `Comparable` has one method, so this contract is [CompareToContract], which says how it reads the Javadoc.
///
/// ## Usage Examples
/// ```java
/// class IntegerComparableTest extends AbstractTest implements ComparableContract<Integer> {
///     @Override
///     public ComparableProvider<Integer> comparableProvider() {
///         return ComparableProviders.integers();
///     }
/// }
///
/// class BigDecimalComparableTest extends AbstractTest implements ComparableContract<BigDecimal> {
///     @Override
///     public ComparableProvider<BigDecimal> comparableProvider() {
///         return ComparableProviders.bigDecimals();
///     }
///
///     @Override
///     public boolean naturalOrderingIsConsistentWithEquals() {
///         return false;
///     }
/// }
/// ```
///
/// @param <T> the type under test
/// @since 0.1.3
/// @see Comparable
public interface ComparableContract<T extends Comparable<? super T>> extends CompareToContract<T> {
}
