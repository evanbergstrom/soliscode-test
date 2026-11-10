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

/// **Contract for `computeIfPresent` of a `ConcurrentMap` under contention**
///
/// The steps of the default ("map operations and the remapping function may be called multiple times") land through
/// atomic conditional updates, so no update is lost. Threads contend on [#HOT_KEYS] keys, each call making a unique
/// value from the old one and logging the pair in an [UpdateLog]. Every value a call returned as stored must be on the
/// chain from the key's final value back to its first. The function may be called more than once; the calls that
/// lost are logged but never stored, so they do not count.
///
/// It runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#computeIfPresent(Object, java.util.function.BiFunction)
/// @since 0.1.2
public interface ThreadSafeComputeIfPresentContract<M extends ConcurrentMap<K, V>, K, V>
        extends ThreadSafeConcurrentMapContractSupport<M, K, V> {

    /// Tests that no value computeIfPresent reported stored is lost.
    @DisplayName("computeIfPresent(K, BiFunction) loses no update under contention")
    @VerySlow
    @ConcurrencyTest
    default void computeIfPresentWhenCalledUnderContentionLosesNoUpdate() {
        if (!supportsMethod(MapMethods.COMPUTE_IF_PRESENT)) {
            return;
        }
        List<K> keys = distinctKeys(HOT_KEYS);
        Supplier<V> values = valueProvider().uniqueInstanceSupplier();
        M map = filledWith(keys, values);
        Map<K, V> firsts = new HashMap<>(map);
        Map<K, Queue<V>> stored = queuesFor(keys);
        UpdateLog<V> log = new UpdateLog<>();
        AtomicLong calls = new AtomicLong();
        ContentionCoordinator<M> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, ITERATIONS, m -> {
            K key = keys.get((int) (calls.getAndIncrement() % HOT_KEYS));
            V result = m.computeIfPresent(key, (k, old) -> {
                V value = values.get();
                log.record(value, old);
                return value;
            });
            if (result != null) {
                forKey(stored, key).add(result);
            }
        });
        coordinator.execute(map);
        for (final K key : keys) {
            V last = map.get(key);
            assertNotNull(last, "a key lost its value");
            log.assertNoUpdateLost(forKey(firsts, key), last, forKey(stored, key), "computeIfPresent");
        }
    }
}
