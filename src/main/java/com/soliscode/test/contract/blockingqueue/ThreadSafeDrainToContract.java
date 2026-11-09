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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.queue.QueueMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;
import com.soliscode.test.util.RecordingSupplier;

/// **Contract for `drainTo` of a `BlockingQueue` under contention**
///
/// "All queuing methods achieve their effects atomically" ([BlockingQueue]); `drainTo` is one. Producers `offer`
/// unique elements and record those accepted, while drainers call `drainTo` and `drainTo` with a maximum
/// alternately, each into a list of its own, and record what they get. What was drained, with what is left, must be
/// exactly what was accepted: nothing lost, nothing drained twice.
///
/// The test runs on a [ContentionCoordinator], which bounds it in time.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#drainTo(java.util.Collection)
/// @see BlockingQueue#drainTo(java.util.Collection, int)
/// @since 0.1.2
public interface ThreadSafeDrainToContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// The number of producers, and of drainers.
    int THREADS = 2;

    /// How many times each producer and each drainer calls the queue.
    int ITERATIONS = 10_000;

    /// The maximum given to `drainTo(Collection, int)`.
    int MAX_ELEMENTS = 5;

    /// Tests that elements offered by producers and drained by drainers, all at once, are each drained at most once,
    /// and that none is lost.
    @DisplayName("drainTo(Collection) and drainTo(Collection, int) are thread safe under contention with offer(E)")
    @VerySlow
    @ConcurrencyTest
    default void drainToWhenCalledUnderContentionIsThreadSafe() {
        if (!supportsMethod(QueueMethods.OFFER) || !supportsMethod(BlockingQueueMethods.DRAIN_TO)
                || !supportsMethod(BlockingQueueMethods.DRAIN_TO_MAX_ELEMENTS)) {
            return;
        }
        Q queue = provider().emptyInstance();
        RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();
        Queue<E> offered = new ConcurrentLinkedQueue<>();
        Queue<E> drained = new ConcurrentLinkedQueue<>();
        AtomicLong drains = new AtomicLong();
        ContentionCoordinator<Q> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, ITERATIONS, q -> {
            E e = supplier.get();
            if (q.offer(e)) {
                offered.add(e);
            }
        });
        coordinator.add(THREADS, ITERATIONS, q -> {
            List<E> target = new ArrayList<>();
            if (drains.getAndIncrement() % 2 == 0) {
                q.drainTo(target);
            } else {
                q.drainTo(target, MAX_ELEMENTS);
            }
            drained.addAll(target);
        });
        coordinator.execute(queue);
        List<E> all = new ArrayList<>(drained);
        all.addAll(queue);
        assertSameElements(offered, all, "the elements drained and left, against those offered");
    }
}
