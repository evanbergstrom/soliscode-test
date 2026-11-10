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

/// **Contract for `putIfAbsent` of a `ConcurrentMap` under contention**
///
/// `putIfAbsent` is "performed atomically" ([ConcurrentMap#putIfAbsent]). Threads walk fresh keys in step, [#THREADS]
/// calls to a key, each putting a unique value. Per key exactly one call must return `null`, and that call's value
/// must be the one finally mapped; every other call must return it.
///
/// It runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#putIfAbsent(Object, Object)
/// @since 0.1.2
public interface ThreadSafePutIfAbsentContract<M extends ConcurrentMap<K, V>, K, V>
        extends ThreadSafeConcurrentMapContractSupport<M, K, V> {

    /// Tests that, per key, one `putIfAbsent` wins, and the others return its value.
    @DisplayName("putIfAbsent(K, V) is atomic under contention")
    @VerySlow
    @ConcurrencyTest
    default void putIfAbsentWhenCalledUnderContentionIsAtomic() {
        if (!supportsMethod(MapMethods.PUT_IF_ABSENT)) {
            return;
        }
        List<K> keys = distinctKeys(CALLS / THREADS);
        Supplier<V> values = valueProvider().uniqueInstanceSupplier();
        Map<K, Queue<V>> winners = queuesFor(keys);
        Map<K, Queue<V>> returned = queuesFor(keys);
        AtomicLong calls = new AtomicLong();
        ContentionCoordinator<M> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, ITERATIONS, m -> {
            K key = keys.get((int) (calls.getAndIncrement() / THREADS));
            V value = values.get();
            V previous = m.putIfAbsent(key, value);
            forKey(previous == null ? winners : returned, key).add(previous == null ? value : previous);
        });
        M map = provider().emptyInstance();
        coordinator.execute(map);
        for (final K key : keys) {
            Queue<V> won = forKey(winners, key);
            assertEquals(1, won.size(), () -> "putIfAbsent returned null " + won.size() + " times for one key");
            V winner = won.peek();
            assertEquals(winner, map.get(key), "the value mapped is not the one whose putIfAbsent returned null");
            for (final V value : forKey(returned, key)) {
                assertEquals(winner, value, "putIfAbsent returned a value other than the one mapped");
            }
        }
    }
}
