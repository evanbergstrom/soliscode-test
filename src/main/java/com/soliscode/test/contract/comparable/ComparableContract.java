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
