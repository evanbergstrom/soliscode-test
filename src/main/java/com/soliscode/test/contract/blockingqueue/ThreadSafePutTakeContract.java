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
package com.soliscode.test.contract.blockingqueue;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.collection.CollectionMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;
import com.soliscode.test.util.RecordingSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// **Contract for `put`, `add` and `take` of a `BlockingQueue` under contention**
///
/// "BlockingQueue implementations are thread-safe. All queuing methods achieve their effects atomically", and "A
/// BlockingQueue can safely be used with multiple producers and multiple consumers" ([BlockingQueue]). Producers
/// `put` and `add` unique elements (an `add` refused by a full queue is followed by a `put` of the same element),
/// and consumers `take` as many. Every element must be taken exactly once, and the queue left empty.
///
/// The test runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#put(Object)
/// @see BlockingQueue#take()
/// @since 0.1.2
public interface ThreadSafePutTakeContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// The number of producers, and of consumers.
    int THREADS = 2;

    /// How many times each producer and each consumer calls the queue.
    int ITERATIONS = 10_000;

    /// Tests that elements put and added by producers and taken by consumers, all at once, are each taken exactly
    /// once.
    @DisplayName("put(E), add(E) and take() are thread safe under contention with each other")
    @VerySlow
    @ConcurrencyTest
    default void putAndTakeWhenCalledUnderContentionAreThreadSafe() {
        if (!supportsMethod(BlockingQueueMethods.PUT) || !supportsMethod(BlockingQueueMethods.TAKE)
                || !supportsMethod(CollectionMethods.ADD)) {
            return;
        }
        Q queue = provider().emptyInstance();
        RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();
        Queue<E> taken = new ConcurrentLinkedQueue<>();
        AtomicLong calls = new AtomicLong();
        ContentionCoordinator<Q> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, ITERATIONS, unchecked(q -> {
            E e = supplier.get();
            if (calls.getAndIncrement() % 2 == 0) {
                q.put(e);
            } else {
                try {
                    q.add(e);
                } catch (final IllegalStateException full) {
                    q.put(e);
                }
            }
        }));
        coordinator.add(THREADS, ITERATIONS, unchecked(q -> taken.add(q.take())));
        coordinator.execute(queue);
        assertEquals(0, queue.size(), "elements left after as many takes as puts");
        assertSameElements(supplier.recorded(), taken, "the elements taken");
    }
}
