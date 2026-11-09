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
package com.soliscode.test.contract.navigableset;

import java.util.NavigableSet;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.contract.sortedset.SortedSetContract;

/// **Contract for the `NavigableSet` interface**
///
/// This contract extends [SortedSetContract] and adds a method contract for each method that [NavigableSet] declares
/// with a meaning of its own: `lower`, `floor`, `ceiling`, `higher`, `pollFirst`, `pollLast`, `descendingSet`,
/// `descendingIterator`, and the views with inclusive or exclusive bounds, `subSet(E, boolean, E, boolean)`,
/// `headSet(E, boolean)` and `tailSet(E, boolean)`.
///
/// `NavigableSet` restates the rest: `iterator()` and the one-bound views (`headSet(E)` is `headSet(E, false)`)
/// are tested by [SortedSetContract]; `removeFirst()`, `removeLast()` and `reversed()`, whose defaults it adds, by the
/// inherited `SequencedCollection` contracts and [com.soliscode.test.contract.sortedset.SortedReversedContract].
///
/// The expected results come from a [java.util.TreeSet] holding the same elements, with the set's `comparator()`
/// ([com.soliscode.test.contract.support.SortedSetContractSupport]). `NavigableSet`'s methods are not optional
/// operations, so the contract has no method enum; the tests that change the set run only where
/// [com.soliscode.test.contract.collection.CollectionMethods#ADD] or
/// [com.soliscode.test.contract.collection.CollectionMethods#REMOVE] is supported.
///
/// ## Usage Examples
///
/// ```java
/// class TreeSetTest extends AbstractTest
///         implements NavigableSetContract<TreeSet<Integer>, Integer>, WithIntegerElement {
///
///     @Override
///     public CollectionProvider<TreeSet<Integer>, Integer> provider() {
///         return CollectionProviders.from(TreeSet::new, TreeSet::new, TreeSet::new, PROVIDER);
///     }
///
///     @Override
///     public boolean permitNulls() {
///         return false;
///     }
///
///     @Override
///     public boolean permitDuplicates() {
///         return false;
///     }
///
///     @Override
///     public boolean permitIncompatibleTypes() {
///         return false;
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the tests depends on
/// the [NavigableSet] and [com.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <S> the navigable set type being tested
/// @param <E> the element type being tested
/// @see NavigableSet
/// @since 0.1.2
public interface NavigableSetContract<S extends NavigableSet<E>, E> extends SortedSetContract<S, E>,
        LowerContract<S, E>,
        FloorContract<S, E>,
        CeilingContract<S, E>,
        HigherContract<S, E>,
        PollFirstContract<S, E>,
        PollLastContract<S, E>,
        DescendingSetContract<S, E>,
        DescendingIteratorContract<S, E>,
        SubSetFourArgContract<S, E>,
        HeadSetTwoArgContract<S, E>,
        TailSetTwoArgContract<S, E> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    @Override
    void doesNotSupportMethod(InterfaceMethod method);
}
