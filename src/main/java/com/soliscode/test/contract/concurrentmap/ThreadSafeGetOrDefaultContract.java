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

/// **Contract for `getOrDefault` of a `ConcurrentMap` under contention**
///
/// The default of [ConcurrentMap#getOrDefault] is one `get`, so it returns a value the key held, or the default; never
/// `null`, where the map refuses null values. Writers put unique values on [#HOT_KEYS] keys and remove them, in
/// turn; readers call `getOrDefault` with a default of their own. A `getOrDefault` that checks for the key and then
/// gets it, in steps, returns `null` for a key removed in between.
///
/// It runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#getOrDefault(Object, Object)
/// @since 0.1.2
public interface ThreadSafeGetOrDefaultContract<M extends ConcurrentMap<K, V>, K, V>
        extends ThreadSafeConcurrentMapContractSupport<M, K, V> {

    /// The calls in turn: a put, a remove, and 2 reads.
    int GROUP = 4;

    /// Tests that `getOrDefault` returns a value put for the key, or the default, never `null`.
    @DisplayName("getOrDefault(Object, V) returns a value or the default under contention")
    @VerySlow
    @ConcurrencyTest
    default void getOrDefaultWhenCalledUnderContentionReturnsValueOrDefault() {
        if (!supportsMethod(MapMethods.GET_OR_DEFAULT) || !supportsMethod(MapMethods.REMOVE) || permitNullValues()) {
            return;
        }
        List<K> keys = distinctKeys(HOT_KEYS);
        Supplier<V> values = valueProvider().uniqueInstanceSupplier();
        V defaultValue = values.get();
        M map = filledWith(keys, values);
        Map<K, Set<V>> put = new ConcurrentHashMap<>();
        for (final K key : keys) {
            put.put(key, ConcurrentHashMap.newKeySet());
            forKey(put, key).add(map.get(key));
        }
        Queue<String> problems = new ConcurrentLinkedQueue<>();
        AtomicLong calls = new AtomicLong();
        ContentionCoordinator<M> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, ITERATIONS, m -> {
            long call = calls.getAndIncrement();
            K key = keys.get((int) (call / GROUP % HOT_KEYS));
            switch ((int) (call % GROUP)) {
                case 0 -> {
                    V value = values.get();
                    forKey(put, key).add(value);
                    m.put(key, value);
                }
                case 1 -> m.remove(key);
                default -> {
                    V result = m.getOrDefault(key, defaultValue);
                    if (result == null) {
                        problems.add("getOrDefault returned null");
                    } else if (!result.equals(defaultValue) && !forKey(put, key).contains(result)) {
                        problems.add("getOrDefault returned " + result + ", which was never put for the key");
                    }
                }
            }
        });
        coordinator.execute(map);
        assertTrue(problems.isEmpty(), () -> problems.size() + " bad results, the first: " + problems.peek());
    }
}
