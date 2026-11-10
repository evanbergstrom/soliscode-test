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

import java.util.HashMap;
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
import com.soliscode.test.contract.support.UpdateLog;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/// **Contract for `replaceAll` of a `ConcurrentMap` under contention**
///
/// The default of [ConcurrentMap#replaceAll] replaces each entry with `replace(key, old, new)`, retried, so each
/// entry's update is atomic. One thread calls `replaceAll` again and again; the others `compute` on [#HOT_KEYS] keys;
/// one more puts and removes other keys, so `replaceAll` meets a changing map. Both functions log their updates in an
/// [UpdateLog], and every value `compute` returned as stored must be on the chain from the key's final value back to
/// its first. Nothing may throw: the contract reads `replaceAll` as weakly consistent, as the `java.util.concurrent`
/// package summary says most concurrent collections are.
///
/// It runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#replaceAll(java.util.function.BiFunction)
/// @since 0.1.2
public interface ThreadSafeReplaceAllContract<M extends ConcurrentMap<K, V>, K, V>
        extends ThreadSafeConcurrentMapContractSupport<M, K, V> {

    /// How many times the one thread calls `replaceAll`.
    int REPLACE_ALLS = 200;

    /// Tests that `replaceAll` loses none of the updates `compute` makes meanwhile, and throws nothing.
    @DisplayName("replaceAll(BiFunction) replaces each entry atomically under contention")
    @VerySlow
    @ConcurrencyTest
    default void replaceAllWhenCalledUnderContentionLosesNoUpdate() {
        if (!supportsMethod(MapMethods.REPLACE_ALL) || !supportsMethod(MapMethods.COMPUTE)
                || !supportsMethod(MapMethods.REMOVE)) {
            return;
        }
        List<K> keys = distinctKeys(HOT_KEYS * 2);
        List<K> hot = keys.subList(0, HOT_KEYS);
        List<K> others = keys.subList(HOT_KEYS, keys.size());
        Supplier<V> values = valueProvider().uniqueInstanceSupplier();
        M map = filledWith(hot, values);
        Map<K, V> firsts = new HashMap<>(map);
        Map<K, Queue<V>> stored = queuesFor(hot);
        UpdateLog<V> log = new UpdateLog<>();
        AtomicLong calls = new AtomicLong();
        AtomicLong changes = new AtomicLong();
        ContentionCoordinator<M> coordinator = new ContentionCoordinator<>();
        coordinator.add(1, REPLACE_ALLS, m -> m.replaceAll((k, old) -> {
            V value = values.get();
            log.record(value, old);
            return value;
        }));
        coordinator.add(THREADS - 1, ITERATIONS, m -> {
            K key = hot.get((int) (calls.getAndIncrement() % HOT_KEYS));
            V result = m.compute(key, (k, old) -> {
                V value = values.get();
                if (old != null) {
                    log.record(value, old);
                }
                return value;
            });
            if (result != null) {
                forKey(stored, key).add(result);
            }
        });
        coordinator.add(1, ITERATIONS, m -> {
            long change = changes.getAndIncrement();
            K key = others.get((int) (change / 2 % others.size()));
            if (change % 2 == 0) {
                m.put(key, values.get());
            } else {
                m.remove(key);
            }
        });
        coordinator.execute(map);
        for (final K key : hot) {
            V last = map.get(key);
            assertNotNull(last, "a key lost its value");
            log.assertNoUpdateLost(forKey(firsts, key), last, forKey(stored, key), "replaceAll with compute");
        }
    }
}
