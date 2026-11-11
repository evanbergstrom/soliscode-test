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
package com.soliscode.test.contract.spliterator;

import java.util.Spliterator;

/// **Contract for the [Spliterator] interface.**
///
/// A test class implements it with a [com.soliscode.test.provider.SpliteratorProvider] of the spliterator under
/// test and an element provider; JUnit runs its test methods as the class's own.
///
/// It follows the Javadoc of [Spliterator], and where it reads it strictly or loosely, the method contract says so:
/// - The source is finite, and holds only unique elements from the element provider, in the order it makes them,
///   or none. Every bound counts calls: more than `TRAVERSAL_LIMIT` `tryAdvance` calls that return `true`, or
///   splits, fail a test rather than hang it.
/// - Order is checked where the source documents `ORDERED`
///   ([com.soliscode.test.provider.SpliteratorSource#characteristics()]); where the spliterator reports it
///   without the source documenting it, the `ORDERED` test checks a split's parts against an unsplit traversal.
/// - The characteristics the source's class documents must be reported ([CharacteristicsContract]), and repeated
///   calls to `characteristics()` must agree ("should", read as a requirement).
/// - `CONCURRENT` with `SIZED` or `IMMUTABLE` on a top-level spliterator fails, and `IMMUTABLE` and `CONCURRENT`
///   are tested with a removal during traversal ([ReportedCharacteristicsContract]).
/// - `trySplit` may return `null` for any reason ([TrySplitContract]).
/// - Not tested: "sequentially in the current thread"; what a spliterator does after the action throws; the policy
///   of one that reports neither `IMMUTABLE` nor `CONCURRENT`; `estimateSize` where the size is not known.
///
/// ## Usage Examples
/// ```java
/// class ArrayListSpliteratorTest extends AbstractTest
///         implements SpliteratorContract<Spliterator<Integer>, Integer>, WithIntegerElement {
///     @Override
///     public SpliteratorProvider<Spliterator<Integer>, Integer> spliteratorProvider() {
///         return SpliteratorProviders.arrayList();
///     }
/// }
/// ```
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Spliterator
public interface SpliteratorContract<S extends Spliterator<E>, E>
        extends TryAdvanceContract<S, E>, ForEachRemainingContract<S, E>, TrySplitContract<S, E>,
        EstimateSizeContract<S, E>, GetExactSizeIfKnownContract<S, E>, CharacteristicsContract<S, E>,
        HasCharacteristicsContract<S, E>, GetComparatorContract<S, E>, ReportedCharacteristicsContract<S, E> {
}
