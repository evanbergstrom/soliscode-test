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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.map.MapMethods;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for `removeIf` on the `entrySet()` and `values()` of a `ConcurrentMap` under contention**
///
/// [ConcurrentMap]: the views "must override methods such as removeIf when necessary to preserve atomicity
/// guarantees". A predicate on values sees a value another thread can change. A few keys hold "first" values, kept in
/// a set of their own. In turn: a remover calls the view's `removeIf` for first values; 2 replacers call
/// `replace(key, unique)`; a refiller `put`s a new first value, so that the removers keep finding some. Nothing removes
/// a value that is not a first one, so each such value a `replace` stored must be mapped at the end, or returned by a
/// later `replace` or `put`. A `removeIf` that tests a value and then removes its key through the iterator can remove
/// one put in between. There is a test for each view.
///
/// `keySet().removeIf` is not tested: its predicate sees only keys, so removing by key is always what it means.
///
/// It runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <M> the concurrent map type being tested
/// @param <K> the key type
/// @param <V> the value type
/// @see ConcurrentMap
/// @since 0.1.2
public interface ThreadSafeViewRemoveIfContract<M extends ConcurrentMap<K, V>, K, V>
        extends ThreadSafeConcurrentMapContractSupport<M, K, V> {

    /// The calls in turn: a remover, 2 replacers and a refiller.
    int GROUP = 4;

    /// The keys the test contends on.
    int VIEW_KEYS = 2;

    /// How many calls each thread makes: more than the other tests, since the race is narrow.
    int VIEW_ITERATIONS = 10_000;

    /// How many times the removers' predicate spins on a match.
    int SPINS = 100;

    /// Tests that `entrySet().removeIf` removes only first values, never a value `replace` put meanwhile.
    @DisplayName("entrySet().removeIf is atomic under contention")
    @VerySlow
    @ConcurrencyTest
    default void entrySetRemoveIfWhenCalledUnderContentionRemovesOnlyMatchingValues() {
        if (supportsMethod(MapMethods.ENTRY_SET)) {
            viewRemoveIfUnderContention((m, firsts) -> m.entrySet().removeIf(e -> isFirst(firsts, e.getValue())));
        }
    }

    /// Tests that `values().removeIf` removes only first values, never a value `replace` put meanwhile.
    @DisplayName("values().removeIf is atomic under contention")
    @VerySlow
    @ConcurrencyTest
    default void valuesRemoveIfWhenCalledUnderContentionRemovesOnlyMatchingValues() {
        if (supportsMethod(MapMethods.VALUES)) {
            viewRemoveIfUnderContention((m, firsts) -> m.values().removeIf(v -> isFirst(firsts, v)));
        }
    }

    /// The removers' predicate: whether a value is a first one. On a match it spins ([Thread#onSpinWait()], [#SPINS]
    /// times), which widens the gap between the view's test and its removal, where a thread-unsafe `removeIf` lets a
    /// `replace` in.
    ///
    /// @param firsts the first values
    /// @param value the value
    /// @return whether it is a first value
    default boolean isFirst(final Set<V> firsts, final V value) {
        boolean first = firsts.contains(value);
        for (int i = 0; first && i < SPINS; i++) {
            Thread.onSpinWait();
        }
        return first;
    }

    /// Runs the removers, replacers and refillers, and checks that no value a `replace` stored was removed.
    ///
    /// @param remover removes the first values from the map, through a view
    default void viewRemoveIfUnderContention(final BiConsumer<M, Set<V>> remover) {
        if (!supportsMethod(MapMethods.REPLACE_TWO_ARG) || !supportsMethod(MapMethods.PUT)) {
            return;
        }
        List<K> keys = distinctKeys(VIEW_KEYS);
        Supplier<V> values = valueProvider().uniqueInstanceSupplier();
        Set<V> firsts = ConcurrentHashMap.newKeySet();
        M map = filledWith(keys, () -> {
            V first = values.get();
            firsts.add(first);
            return first;
        });
        Map<K, Queue<V>> stored = queuesFor(keys);
        Set<V> replaced = ConcurrentHashMap.newKeySet();
        AtomicLong calls = new AtomicLong();
        ContentionCoordinator<M> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, VIEW_ITERATIONS, m -> {
            long call = calls.getAndIncrement();
            K key = keys.get((int) (call / GROUP % VIEW_KEYS));
            switch ((int) (call % GROUP)) {
                case 0 -> remover.accept(m, firsts);
                case 1, 2 -> {
                    V value = values.get();
                    V old = m.replace(key, value);
                    if (old != null) {
                        forKey(stored, key).add(value);
                        replaced.add(old);
                    }
                }
                default -> {
                    V first = values.get();
                    firsts.add(first);
                    V old = m.put(key, first);
                    if (old != null) {
                        replaced.add(old);
                    }
                }
            }
        });
        coordinator.execute(map);
        for (final K key : keys) {
            V last = map.get(key);
            for (final V value : forKey(stored, key)) {
                assertTrue(value.equals(last) || replaced.contains(value), () -> "the value " + value
                        + " that replace stored was removed by removeIf, whose predicate matched only first values");
            }
        }
    }
}
