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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// **Contract for `computeIfAbsent` of a `ConcurrentMap` under contention**
///
/// The default of [ConcurrentMap#computeIfAbsent] returns the value `putIfAbsent` stored, or the one that beat it, so
/// every call for a key returns the value finally mapped to it, though the function may be called more than once.
/// Threads walk fresh keys in step, [#THREADS] calls to a key, each computing a unique value.
///
/// It runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#computeIfAbsent(Object, java.util.function.Function)
/// @since 0.1.2
public interface ThreadSafeComputeIfAbsentContract<M extends ConcurrentMap<K, V>, K, V>
        extends ThreadSafeConcurrentMapContractSupport<M, K, V> {

    /// Tests that every `computeIfAbsent` for a key returns the value finally mapped to it.
    @DisplayName("computeIfAbsent(K, Function) is atomic under contention")
    @VerySlow
    @ConcurrencyTest
    default void computeIfAbsentWhenCalledUnderContentionIsAtomic() {
        if (!supportsMethod(MapMethods.COMPUTE_IF_ABSENT)) {
            return;
        }
        List<K> keys = distinctKeys(CALLS / THREADS);
        Supplier<V> values = valueProvider().uniqueInstanceSupplier();
        Map<K, Queue<V>> returned = queuesFor(keys);
        AtomicLong calls = new AtomicLong();
        ContentionCoordinator<M> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, ITERATIONS, m -> {
            K key = keys.get((int) (calls.getAndIncrement() / THREADS));
            V result = m.computeIfAbsent(key, k -> values.get());
            if (result != null) {
                forKey(returned, key).add(result);
            }
        });
        M map = provider().emptyInstance();
        coordinator.execute(map);
        for (final K key : keys) {
            V mapped = map.get(key);
            assertEquals(THREADS, forKey(returned, key).size(), "computeIfAbsent returned null for a non-null result");
            for (final V value : forKey(returned, key)) {
                assertEquals(mapped, value, "computeIfAbsent returned a value other than the one mapped");
            }
        }
    }
}
