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
package com.soliscode.test.contract.concurrentmap;

import java.util.concurrent.ConcurrentMap;

import com.soliscode.test.contract.map.MapContract;

/// **Contract for the `ConcurrentMap` interface**
///
/// [ConcurrentMap]: "A Map providing thread safety and atomicity guarantees." This contract extends [MapContract] with
/// a method contract for each method `ConcurrentMap` declares or re-specifies: [GetOrDefaultContract],
/// [ForEachContract], [ReplaceAllContract], [PutIfAbsentContract], [RemoveTwoArgContract],
/// [ReplaceThreeArgContract], [ReplaceTwoArgContract], [ComputeIfAbsentContract], [ComputeIfPresentContract],
/// [ComputeContract] and [MergeContract]. They test, on one thread, what `ConcurrentMap` and the `Map` methods it
/// inherits say beyond `MapContract`'s tests: null refusal, the `NullPointerException` for a `null` function even where
/// the function would not be called, and what a `null` function result does. Atomicity under contention is
/// `ThreadSafeConcurrentMapContract`'s.
///
/// ## Readings
///
/// - **Nulls, stricter.** The contract expects null keys and values to be refused, as `ConcurrentHashMap` and
///   `ConcurrentSkipListMap` refuse them: every default of `ConcurrentMap` "assumes that the ConcurrentMap cannot
///   contain null values". A test of a concurrent map that permits nulls overrides `permitNullKeys()` and
///   `permitNullValues()` to return `true`, and the null tests do not run.
/// - **Optional exceptions.** Where the JDK makes the `NullPointerException` optional (`remove(key, value)`, and a
///   null key in `getOrDefault`), the test accepts the other result, `false` or the default.
/// - **`replaceAll`'s `null` result, stricter.** `Map#replaceAll` makes its `NullPointerException` for a `null`
///   result optional; on a map that refuses null values the only other outcome is to store one, so the contract
///   requires the exception.
/// - **Weakly consistent views, stricter.** `ConcurrentMap` does not promise them; the `java.util.concurrent` package
///   summary says "most" concurrent collections have them, and `ConcurrentHashMap` and `ConcurrentSkipListMap` do.
///   [ViewIteratorsWeakConsistencyContract] tests the iterators of `keySet()`, `values()` and `entrySet()` after a
///   change to the map during a traversal, by default; a test of a map whose views' iterators are fail-fast overrides
///   `iteratorsAreWeaklyConsistent()` to return `false`.
///
/// ## Usage Examples
///
/// ```java
/// class ConcurrentHashMapTest extends AbstractTest
///         implements ConcurrentMapContract<ConcurrentHashMap<Integer, String>, Integer, String>,
///         WithIntegerKeyAndStringValue<ConcurrentHashMap<Integer, String>> {
///
///     @Override
///     public MapProvider<Integer, String, ConcurrentHashMap<Integer, String>> provider() {
///         return MapProviders.from(ConcurrentHashMap::new, ConcurrentHashMap::new, ..., KEY_PROVIDER,
///                 VALUE_PROVIDER);
///     }
/// }
/// ```
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap
/// @see ThreadSafeConcurrentMapContract
/// @since 0.1.2
public interface ConcurrentMapContract<M extends ConcurrentMap<K, V>, K, V> extends MapContract<M, K, V>,
        GetOrDefaultContract<M, K, V>,
        ForEachContract<M, K, V>,
        ReplaceAllContract<M, K, V>,
        PutIfAbsentContract<M, K, V>,
        RemoveTwoArgContract<M, K, V>,
        ReplaceThreeArgContract<M, K, V>,
        ReplaceTwoArgContract<M, K, V>,
        ComputeIfAbsentContract<M, K, V>,
        ComputeIfPresentContract<M, K, V>,
        ComputeContract<M, K, V>,
        MergeContract<M, K, V>,
        ViewIteratorsWeakConsistencyContract<M, K, V> {
}
