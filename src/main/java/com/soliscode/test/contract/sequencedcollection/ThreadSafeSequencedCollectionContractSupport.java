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
package com.soliscode.test.contract.sequencedcollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.SequencedCollection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;

import com.soliscode.test.contract.collection.ThreadSafeCollectionContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/// Support for the thread-safety contracts of a [SequencedCollection]: the run of producers and consumers at both
/// ends that [ThreadSafeAddRemoveFirstLastContract] and [ThreadSafeReversedContract] share.
///
/// @param <C> the type of collection
/// @param <E> the type of elements
/// @since 0.1.6
public interface ThreadSafeSequencedCollectionContractSupport<C extends SequencedCollection<E>, E>
        extends ThreadSafeCollectionContractSupport<C, E> {

    /// The number of threads in each role of a sequenced thread-safety test.
    int THREADS = 2;

    /// How many times each thread of a sequenced thread-safety test calls the collection.
    int ITERATIONS = 10_000;

    /// Runs producers and consumers at both ends of the collection, through `target`, and asserts that nothing was
    /// lost or removed twice.
    ///
    /// [#THREADS] producers each alternate `addFirst` and `addLast` of unique elements, by their own count of calls,
    /// so that the two producers' `addFirst` calls often coincide; an add that throws [IllegalStateException], as a
    /// full bounded deque's does ([java.util.Deque#addFirst]), adds nothing and is not counted. [#THREADS] consumers
    /// call `removeFirst` in the first round and `removeLast` in the second, each round on a fresh empty collection,
    /// and ignore a [NoSuchElementException]. What was removed, with what is left, must then be exactly what was
    /// added, with no `null`, and the collection consistent ([#assertConsistent]).
    ///
    /// Each round is on [#contentionCoordinator()], in phases, which bounds it in time.
    ///
    /// @param target the collection the calls go to, given the collection under test: itself, or a view of it
    /// @param description what the calls go through, for the failure messages
    default void assertAddsAndRemovesAtBothEndsAreThreadSafe(final Function<C, SequencedCollection<E>> target,
                                                             final String description) {
        for (final boolean fromLast : new boolean[] {false, true}) {
            C collection = provider().emptyInstance();
            Supplier<E> supplier = elementProvider().uniqueInstanceSupplier();
            Queue<E> added = new ConcurrentLinkedQueue<>();
            Queue<E> removed = new ConcurrentLinkedQueue<>();
            ThreadLocal<int[]> calls = ThreadLocal.withInitial(() -> new int[1]);
            ContentionCoordinator<C> coordinator = contentionCoordinator();
            coordinator.add(THREADS, ITERATIONS, c -> {
                E e = supplier.get();
                try {
                    if (calls.get()[0]++ % 2 == 0) {
                        target.apply(c).addFirst(e);
                    } else {
                        target.apply(c).addLast(e);
                    }
                } catch (final IllegalStateException full) {
                    return;
                }
                added.add(e);
            });
            String remove = fromLast ? "removeLast()" : "removeFirst()";
            coordinator.add(THREADS, ITERATIONS, c -> {
                E e;
                try {
                    e = fromLast ? target.apply(c).removeLast() : target.apply(c).removeFirst();
                } catch (final NoSuchElementException empty) {
                    return;
                }
                assertNotNull(e, remove + " through " + description + " returned a null that was never added");
                removed.add(e);
            });
            coordinator.execute(collection);
            List<E> all = new ArrayList<>(removed);
            all.addAll(collection);
            assertEquals(counts(added), counts(all), "the elements removed with " + remove + " through "
                    + description + " and left, against those added");
            assertConsistent(collection);
        }
    }

    private static <E> Map<E, Integer> counts(final Iterable<E> elements) {
        Map<E, Integer> counts = new HashMap<>();
        for (final E e : elements) {
            counts.merge(e, 1, Integer::sum);
        }
        return counts;
    }
}
