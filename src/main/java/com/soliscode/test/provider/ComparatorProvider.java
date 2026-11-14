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
package com.soliscode.test.provider;

import java.util.Comparator;
import java.util.List;

/// Makes what a [com.soliscode.test.contract.comparator.ComparatorContract] tests: the comparator under test, and
/// samples in tiers in its order.
///
/// Each tier holds one or more samples that the comparator calls equal, and every sample of a tier is less than every
/// sample of the next. The contract reads this order as the one the comparator documents: a `compare` that disagrees
/// with it fails, even where it keeps `Comparator`'s laws. The `thenComparing` tests see a tie only within a tier of
/// two or more samples that are not the same object: cached boxes (`Integer.valueOf(2)`) and string literals are.
///
/// ## Usage Examples
/// ```java
/// class ByLengthTest extends AbstractTest implements ComparatorContract<String> {
///     @Override
///     public ComparatorProvider<String> comparatorProvider() {
///         return ComparatorProviders.of(Comparator.comparingInt(String::length),
///                 List.of(List.of(""), List.of("a", "b"), List.of("abc")));
///     }
///
///     @Override
///     public boolean comparatorIsConsistentWithEquals() {
///         return false;
///     }
/// }
/// ```
///
/// @param <T> the type of the samples
/// @since 0.1.4
/// @see ComparatorProviders
public interface ComparatorProvider<T> {

    /// Returns the comparator under test.
    ///
    /// @return the comparator
    Comparator<T> comparator();

    /// Returns the samples, in tiers, in ascending order of the comparator. There is at least one tier, no tier is
    /// empty, and no sample is null. A contract does not change them.
    ///
    /// @return the tiers, lowest first
    List<List<T>> ascendingSamples();
}
