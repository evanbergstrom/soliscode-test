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
package com.soliscode.test.contract.concurrentnavigablemap;

import java.util.concurrent.ConcurrentNavigableMap;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.contract.concurrentmap.ConcurrentMapContract;
import com.soliscode.test.contract.navigablemap.NavigableMapContract;

/// **Contract for the `ConcurrentNavigableMap` interface**
///
/// [ConcurrentNavigableMap]: "A ConcurrentMap supporting NavigableMap operations, and recursively so for its navigable
/// sub-maps." This contract runs [ConcurrentMapContract] and [NavigableMapContract], and adds a method contract for
/// each of the 10 methods `ConcurrentNavigableMap` re-specifies to return a concurrent view: [DescendingMapContract],
/// [SubMapFourArgContract], [SubMapContract], [HeadMapTwoArgContract], [HeadMapContract], [TailMapTwoArgContract],
/// [TailMapContract], [KeySetContract], [NavigableKeySetContract] and [DescendingKeySetContract]. They test that each
/// view is backed by the map, both ways; that a key set refuses `add` and `addAll`; that `descendingMap()` is in the
/// reverse of the comparator's order; and that a range view's `null` bound throws `NullPointerException`.
///
/// ## Readings
///
/// - **Recursively, by running the contract on views.** No test checks a view of a view. To test that a map's
///   sub-maps are concurrent navigable maps in turn, run this contract against a provider of its views (a
///   `descendingMap()`, or a `subMap`, `headMap` or `tailMap` whose range holds every key the provider gives), as
///   `ContractCoverageTest` does for `ConcurrentSkipListMap`.
/// - **Nulls, stricter.** As [ConcurrentMapContract], the contract expects null keys to be refused, so a `null` bound
///   must throw `NullPointerException` ("if fromKey or toKey is null and this map does not permit null keys"). A test
///   of a map that permits null keys overrides `permitNullKeys()`, and the null-bound tests do not run.
/// - **Removal through a key set, looser.** The JDK lists `Iterator.remove`, `Set.remove`, `removeAll`, `retainAll`
///   and `clear`; the contract tests the first two.
/// - **Weak consistency.** The key sets' "iterators and spliterators are weakly consistent".
///   [KeySetViewsWeakConsistencyContract] tests both iterators of each key set, after a change to the map during a
///   traversal, as [com.soliscode.test.contract.support.WeakConsistencyChecks] reads the term; their spliterators are
///   not tested. The other views' iterators are tested as [ConcurrentMapContract] tests them.
///
/// Thread safety is [com.soliscode.test.contract.concurrentmap.ThreadSafeConcurrentMapContract]'s:
/// `ConcurrentNavigableMap` adds no atomicity guarantee of its own. Run it against the map and its views.
///
/// ## Usage Examples
///
/// ```java
/// class ConcurrentSkipListMapTest extends AbstractTest
///         implements ConcurrentNavigableMapContract<ConcurrentSkipListMap<Integer, String>, Integer, String>,
///         WithIntegerKeyAndStringValue<ConcurrentSkipListMap<Integer, String>> {
///
///     ConcurrentSkipListMapTest() {
///         doesNotSupportMethod(SequencedMapMethods.PUT_FIRST);
///         doesNotSupportMethod(SequencedMapMethods.PUT_LAST);
///     }
///
///     @Override
///     public MapProvider<Integer, String, ConcurrentSkipListMap<Integer, String>> provider() {
///         return MapProviders.from(ConcurrentSkipListMap::new, ConcurrentSkipListMap::new, ..., KEY_PROVIDER,
///                 VALUE_PROVIDER);
///     }
/// }
/// ```
///
/// @param <M> the concurrent navigable map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentNavigableMap
/// @since 0.1.2
public interface ConcurrentNavigableMapContract<M extends ConcurrentNavigableMap<K, V>, K, V>
        extends ConcurrentMapContract<M, K, V>, NavigableMapContract<M, K, V>,
        DescendingMapContract<M, K, V>,
        SubMapFourArgContract<M, K, V>,
        SubMapContract<M, K, V>,
        HeadMapTwoArgContract<M, K, V>,
        HeadMapContract<M, K, V>,
        TailMapTwoArgContract<M, K, V>,
        TailMapContract<M, K, V>,
        KeySetContract<M, K, V>,
        NavigableKeySetContract<M, K, V>,
        DescendingKeySetContract<M, K, V>,
        KeySetViewsWeakConsistencyContract<M, K, V> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    @Override
    void doesNotSupportMethod(InterfaceMethod method);
}
