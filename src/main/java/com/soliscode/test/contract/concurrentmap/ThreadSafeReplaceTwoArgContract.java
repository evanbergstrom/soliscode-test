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

import static org.junit.jupiter.api.Assertions.assertFalse;

/// **Contract for `replace(key, value)` of a `ConcurrentMap` under contention**
///
/// `replace(key, value)` is "performed atomically" ([ConcurrentMap#replace(Object, Object)]): it maps the key only if
/// it has a value. Threads contend on [#HOT_KEYS] keys, 5 calls in turn: 2 `replace(key, unique)`, a `remove(key)`, a
/// `putIfAbsent(key, unique)` and a `get(key)`. A value whose `replace` returned `null` was not stored, so it must
/// never be seen: not by a `get`, not returned by a `replace`, `remove` or `putIfAbsent`, and not mapped at the end. A
/// replace that checks for the key and then puts, in steps, can put back a key removed in between.
///
/// It runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#replace(Object, Object)
/// @since 0.1.2
public interface ThreadSafeReplaceTwoArgContract<M extends ConcurrentMap<K, V>, K, V>
        extends ThreadSafeConcurrentMapContractSupport<M, K, V> {

    /// The calls in turn: 2 replaces, a remove, a putIfAbsent and a get.
    int GROUP = 5;

    /// Tests that a value whose `replace(key, value)` returned `null` is never seen.
    @DisplayName("replace(K, V) is atomic under contention")
    @VerySlow
    @ConcurrencyTest
    default void replaceTwoArgWhenCalledUnderContentionIsAtomic() {
        if (!supportsMethod(MapMethods.REPLACE_TWO_ARG) || !supportsMethod(MapMethods.REMOVE)
                || !supportsMethod(MapMethods.PUT_IF_ABSENT)) {
            return;
        }
        List<K> keys = distinctKeys(HOT_KEYS);
        Supplier<V> values = valueProvider().uniqueInstanceSupplier();
        M map = filledWith(keys, values);
        Set<V> notStored = ConcurrentHashMap.newKeySet();
        Queue<V> seen = new ConcurrentLinkedQueue<>();
        AtomicLong calls = new AtomicLong();
        ContentionCoordinator<M> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, ITERATIONS, m -> {
            long call = calls.getAndIncrement();
            K key = keys.get((int) (call / GROUP % HOT_KEYS));
            V result;
            switch ((int) (call % GROUP)) {
                case 0, 1 -> {
                    V value = values.get();
                    result = m.replace(key, value);
                    if (result == null) {
                        notStored.add(value);
                    }
                }
                case 2 -> result = m.remove(key);
                case 3 -> result = m.putIfAbsent(key, values.get());
                default -> result = m.get(key);
            }
            if (result != null) {
                seen.add(result);
            }
        });
        coordinator.execute(map);
        seen.addAll(map.values());
        for (final V value : seen) {
            assertFalse(notStored.contains(value),
                    () -> "replace(K, V) returned null, so stored nothing, yet its value " + value + " was seen");
        }
    }
}
