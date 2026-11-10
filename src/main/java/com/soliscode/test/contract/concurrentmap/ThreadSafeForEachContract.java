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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for `forEach` of a `ConcurrentMap` under contention**
///
/// The default of [ConcurrentMap#forEach] walks `entrySet()` and skips an entry removed meanwhile, so it passes only
/// mappings the map held; and the contract reads its traversal as weakly consistent, as the `java.util.concurrent`
/// package summary says most concurrent collections' are, so it throws nothing. Writers put unique values on 16 keys
/// and remove them; readers call `forEach`. Each pair it passes must have a non-null value that was put for the key.
/// A `forEach` that walks the keys and then gets each, in steps, passes `null` for a key removed in between.
///
/// It runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#forEach(java.util.function.BiConsumer)
/// @since 0.1.2
public interface ThreadSafeForEachContract<M extends ConcurrentMap<K, V>, K, V>
        extends ThreadSafeConcurrentMapContractSupport<M, K, V> {

    /// The keys the writers change.
    int KEYS = 16;

    /// How many times each reader calls `forEach`.
    int FOR_EACHES = 500;

    /// Tests that `forEach` passes only mappings put, and throws nothing, while other threads put and remove.
    @DisplayName("forEach(BiConsumer) passes only mappings the map held under contention")
    @VerySlow
    @ConcurrencyTest
    default void forEachWhenCalledUnderContentionPassesOnlyMappingsHeld() {
        if (!supportsMethod(MapMethods.FOR_EACH) || !supportsMethod(MapMethods.REMOVE) || permitNullValues()) {
            return;
        }
        List<K> keys = distinctKeys(KEYS);
        Supplier<V> values = valueProvider().uniqueInstanceSupplier();
        M map = filledWith(keys, values);
        Map<K, Set<V>> put = new ConcurrentHashMap<>();
        for (final K key : keys) {
            put.put(key, ConcurrentHashMap.newKeySet());
            forKey(put, key).add(map.get(key));
        }
        Queue<String> problems = new ConcurrentLinkedQueue<>();
        AtomicLong changes = new AtomicLong();
        ContentionCoordinator<M> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS / 2, ITERATIONS, m -> {
            long change = changes.getAndIncrement();
            K key = keys.get((int) (change / 2 % KEYS));
            if (change % 2 == 0) {
                V value = values.get();
                forKey(put, key).add(value);
                m.put(key, value);
            } else {
                m.remove(key);
            }
        });
        coordinator.add(THREADS / 2, FOR_EACHES, m -> {
            try {
                m.forEach((k, v) -> {
                    Set<V> putForKey = put.get(k);
                    if (v == null) {
                        problems.add("forEach passed a null value");
                    } else if (putForKey == null || !putForKey.contains(v)) {
                        problems.add("forEach passed " + k + "=" + v + ", which was never put");
                    }
                });
            } catch (final RuntimeException e) {
                problems.add("forEach threw " + e);
            }
        });
        coordinator.execute(map);
        assertTrue(problems.isEmpty(), () -> problems.size() + " bad results, the first: " + problems.peek());
    }
}
