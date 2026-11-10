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

/// **Contract for the thread safety of a `ConcurrentMap`**
///
/// [ConcurrentMap]: "A Map providing thread safety and atomicity guarantees." This contract extends
/// [ConcurrentMapContract] with a test, under contention, of each guarantee its Javadoc states:
/// - `putIfAbsent`, `remove(key, value)` and both `replace` forms are "performed atomically"
///   ([ThreadSafePutIfAbsentContract], [ThreadSafeRemoveTwoArgContract], [ThreadSafeReplaceThreeArgContract],
///   [ThreadSafeReplaceTwoArgContract]);
/// - `compute`, `computeIfPresent`, `merge` and `computeIfAbsent` lose no update, though "the remapping function may be
///   called multiple times" ([ThreadSafeComputeContract], [ThreadSafeComputeIfPresentContract],
///   [ThreadSafeMergeContract], [ThreadSafeComputeIfAbsentContract]);
/// - `replaceAll` replaces each entry atomically ([ThreadSafeReplaceAllContract]);
/// - `getOrDefault` and `forEach` see only mappings the map held ([ThreadSafeGetOrDefaultContract],
///   [ThreadSafeForEachContract]);
/// - the views' `removeIf` preserves atomicity ([ThreadSafeViewRemoveIfContract]).
///
/// The tests are tagged [com.soliscode.test.annotations.ConcurrencyTest] and
/// [com.soliscode.test.annotations.VerySlow], and run on a [com.soliscode.test.safety.ContentionCoordinator], which
/// bounds them in time. They draw unique values from the value provider, so it must give at least a few tens of
/// thousands of distinct values.
///
/// ## Usage Examples
///
/// ```java
/// class ConcurrentHashMapSafetyTest extends AbstractTest
///         implements ThreadSafeConcurrentMapContract<ConcurrentHashMap<Integer, String>, Integer, String>,
///         WithIntegerKeyAndStringValue<ConcurrentHashMap<Integer, String>> {
///     ...
/// }
/// ```
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap
/// @since 0.1.2
public interface ThreadSafeConcurrentMapContract<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContract<M, K, V>,
        ThreadSafePutIfAbsentContract<M, K, V>,
        ThreadSafeRemoveTwoArgContract<M, K, V>,
        ThreadSafeReplaceThreeArgContract<M, K, V>,
        ThreadSafeReplaceTwoArgContract<M, K, V>,
        ThreadSafeComputeContract<M, K, V>,
        ThreadSafeComputeIfPresentContract<M, K, V>,
        ThreadSafeMergeContract<M, K, V>,
        ThreadSafeComputeIfAbsentContract<M, K, V>,
        ThreadSafeReplaceAllContract<M, K, V>,
        ThreadSafeGetOrDefaultContract<M, K, V>,
        ThreadSafeForEachContract<M, K, V>,
        ThreadSafeViewRemoveIfContract<M, K, V> {
}
