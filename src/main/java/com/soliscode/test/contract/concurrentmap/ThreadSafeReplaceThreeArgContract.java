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

/// **Contract for `replace(key, oldValue, newValue)` of a `ConcurrentMap` under contention**
///
/// `replace(key, oldValue, newValue)` is "performed atomically" ([ConcurrentMap#replace(Object, Object, Object)]).
/// Threads contend on [#HOT_KEYS] keys: each call reads a key's value once, then tries once to replace it with a
/// unique value, and records a success in an [UpdateLog]. Every value stored must be on the chain from the key's final
/// value back to its first, and the chain must hold exactly the successes: no update lost.
///
/// It runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#replace(Object, Object, Object)
/// @since 0.1.2
public interface ThreadSafeReplaceThreeArgContract<M extends ConcurrentMap<K, V>, K, V>
        extends ThreadSafeConcurrentMapContractSupport<M, K, V> {

    /// How many calls each thread makes: more than most tests, since each needs a get and a replace to collide.
    int REPLACE_ITERATIONS = 5 * ITERATIONS;

    /// Tests that no successful `replace(key, oldValue, newValue)` is lost.
    @DisplayName("replace(K, V, V) is atomic under contention")
    @VerySlow
    @ConcurrencyTest
    default void replaceThreeArgWhenCalledUnderContentionIsAtomic() {
        if (!supportsMethod(MapMethods.REPLACE_THREE_ARG)) {
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
        coordinator.add(THREADS, REPLACE_ITERATIONS, m -> {
            K key = keys.get((int) (calls.getAndIncrement() % HOT_KEYS));
            V old = m.get(key);
            V value = values.get();
            if (old != null && m.replace(key, old, value)) {
                log.record(value, old);
                forKey(stored, key).add(value);
            }
        });
        coordinator.execute(map);
        for (final K key : keys) {
            V last = map.get(key);
            assertNotNull(last, "a key lost its value");
            log.assertExactly(forKey(firsts, key), last, forKey(stored, key), "replace(K, V, V)");
        }
    }
}
