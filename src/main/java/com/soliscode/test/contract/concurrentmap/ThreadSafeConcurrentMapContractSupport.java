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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import com.soliscode.test.contract.support.ConcurrentMapContractSupport;

/// What the thread-safety contracts of a [ConcurrentMap] share: the sizes of a run, and helpers to make keys and
/// fill a map.
///
/// Each test runs [#THREADS] threads of [#ITERATIONS] calls on a [com.soliscode.test.safety.ContentionCoordinator].
/// A test of an operation that can race only once per key walks a long run of fresh keys with all its threads in
/// step: a shared counter counts calls, and call `n` acts on key `n / group`, so each key gets `group` calls close
/// together in time. A test of a repeatable operation uses [#HOT_KEYS] keys for the whole run.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @since 0.1.2
public interface ThreadSafeConcurrentMapContractSupport<M extends ConcurrentMap<K, V>, K, V>
        extends ConcurrentMapContractSupport<M, K, V> {

    /// The number of threads each test runs.
    int THREADS = 4;

    /// How many calls each thread makes.
    int ITERATIONS = 2_000;

    /// The number of calls in a run: [#THREADS] times [#ITERATIONS].
    int CALLS = THREADS * ITERATIONS;

    /// The number of keys a test of a repeatable operation contends on.
    int HOT_KEYS = 4;

    /// Returns distinct keys.
    ///
    /// @param count how many
    /// @return the keys
    default List<K> distinctKeys(final int count) {
        Supplier<K> supplier = keyProvider().uniqueInstanceSupplier();
        List<K> keys = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            keys.add(supplier.get());
        }
        return keys;
    }

    /// Maps each key to a value from a supplier, in a new empty map from the provider.
    ///
    /// @param keys the keys
    /// @param values the supplier of the values
    /// @return the map
    default M filledWith(final List<K> keys, final Supplier<V> values) {
        M map = provider().emptyInstance();
        for (final K key : keys) {
            map.put(key, values.get());
        }
        return map;
    }

    /// Returns what a test recorded for a key, in a map it filled for every key.
    ///
    /// @param <T> the type recorded
    /// @param perKey the records, one per key
    /// @param key the key
    /// @return the record for the key
    default <T> T forKey(final Map<K, T> perKey, final K key) {
        return Objects.requireNonNull(perKey.get(key), "the test recorded nothing for a key");
    }

    /// Returns a map from each key to a new, empty, thread-safe queue, for the values a test records per key.
    ///
    /// @param <T> the type recorded
    /// @param keys the keys
    /// @return the map
    default <T> Map<K, Queue<T>> queuesFor(final List<K> keys) {
        Map<K, Queue<T>> queues = new ConcurrentHashMap<>();
        for (final K key : keys) {
            queues.put(key, new ConcurrentLinkedQueue<>());
        }
        return queues;
    }
}
