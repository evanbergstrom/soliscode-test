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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// **Contract for `remove(key, value)` of a `ConcurrentMap` under contention**
///
/// `remove(key, value)` is "performed atomically" ([ConcurrentMap#remove(Object, Object)]). Threads walk keys mapped
/// to their first values, in step, 4 calls to a key: 2 call `replace(key, first, unique)` and 2 call
/// `remove(key, first)`. Done in any order, one call succeeds: the remove wins, and the replace finds the key gone, or
/// the replace wins, and the stale remove finds another value. So per key exactly one call must succeed. A remove that
/// checks the value and then removes the key, in steps, can remove the value a replace put in between: two succeed.
///
/// It runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap#remove(Object, Object)
/// @since 0.1.2
public interface ThreadSafeRemoveTwoArgContract<M extends ConcurrentMap<K, V>, K, V>
        extends ThreadSafeConcurrentMapContractSupport<M, K, V> {

    /// The calls to each key: 2 replaces and 2 removes.
    int GROUP = 4;

    /// Tests that, per key, exactly one of the replaces and removes of its first value succeeds.
    @DisplayName("remove(K, V) is atomic under contention")
    @VerySlow
    @ConcurrencyTest
    default void removeTwoArgWhenCalledUnderContentionIsAtomic() {
        if (!supportsMethod(MapMethods.REMOVE_TWO_ARG) || !supportsMethod(MapMethods.REPLACE_THREE_ARG)) {
            return;
        }
        List<K> keys = distinctKeys(CALLS / GROUP);
        Supplier<V> values = valueProvider().uniqueInstanceSupplier();
        M map = filledWith(keys, values);
        Map<K, V> firsts = new HashMap<>(map);
        AtomicIntegerArray successes = new AtomicIntegerArray(keys.size());
        AtomicLong calls = new AtomicLong();
        ContentionCoordinator<M> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, ITERATIONS, m -> {
            long call = calls.getAndIncrement();
            int index = (int) (call / GROUP);
            K key = keys.get(index);
            V first = forKey(firsts, key);
            boolean succeeded = call % GROUP < 2 ? m.replace(key, first, values.get()) : m.remove(key, first);
            if (succeeded) {
                successes.incrementAndGet(index);
            }
        });
        coordinator.execute(map);
        for (int i = 0; i < keys.size(); i++) {
            int count = successes.get(i);
            assertEquals(1, count, () -> count + " of the replaces and removes of one key's first value succeeded");
        }
    }
}
