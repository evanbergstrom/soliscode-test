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
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.queue.QueueMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;
import com.soliscode.test.util.RecordingSupplier;

/// **Contract for `offer`, `poll` and `remove()` of a `BlockingQueue` under contention**
///
/// "All queuing methods achieve their effects atomically" ([BlockingQueue]). Producers `offer` unique elements,
/// alternating `offer(E)` and the timed `offer` with a timeout of zero, and record those accepted; consumers cycle
/// through `poll()`, the timed `poll` with a timeout of zero, and `remove()`, and record what they get. What was
/// removed, with what is left, must be exactly what was accepted: nothing lost, nothing removed twice.
///
/// A timeout of zero takes the timed methods' path without waiting, so the test stays fast. It runs on a
/// [ContentionCoordinator], which bounds it in time.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#offer(Object)
/// @see BlockingQueue#poll(long, TimeUnit)
/// @since 0.1.2
public interface ThreadSafeOfferPollContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// The number of producers, and of consumers.
    int THREADS = 2;

    /// How many times each producer and each consumer calls the queue.
    int ITERATIONS = 10_000;

    /// Tests that elements offered by producers and polled or removed by consumers, all at once, are each removed
    /// at most once, and that none is lost.
    @DisplayName("offer(E), poll() and remove() and their timed forms are thread safe under contention")
    @VerySlow
    @ConcurrencyTest
    default void offerAndPollWhenCalledUnderContentionAreThreadSafe() {
        if (!supportsMethod(QueueMethods.OFFER) || !supportsMethod(QueueMethods.POLL)
                || !supportsMethod(QueueMethods.REMOVE) || !supportsMethod(BlockingQueueMethods.OFFER_TIMEOUT)
                || !supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            return;
        }
        Q queue = provider().emptyInstance();
        RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();
        Queue<E> offered = new ConcurrentLinkedQueue<>();
        Queue<E> removed = new ConcurrentLinkedQueue<>();
        AtomicLong offers = new AtomicLong();
        AtomicLong polls = new AtomicLong();
        ContentionCoordinator<Q> coordinator = new ContentionCoordinator<>();
        coordinator.add(THREADS, ITERATIONS, unchecked(q -> {
            E e = supplier.get();
            boolean accepted = offers.getAndIncrement() % 2 == 0 ? q.offer(e) : q.offer(e, 0, TimeUnit.MILLISECONDS);
            if (accepted) {
                offered.add(e);
            }
        }));
        coordinator.add(THREADS, ITERATIONS, unchecked(q -> {
            @Nullable E e;
            long call = polls.getAndIncrement() % 3;
            if (call == 0) {
                e = q.poll();
            } else if (call == 1) {
                e = q.poll(0, TimeUnit.MILLISECONDS);
            } else {
                try {
                    e = q.remove();
                } catch (final NoSuchElementException empty) {
                    e = null;
                }
            }
            if (e != null) {
                removed.add(e);
            }
        }));
        coordinator.execute(queue);
        List<E> all = new ArrayList<>(removed);
        all.addAll(queue);
        assertSameElements(offered, all, "the elements removed and left, against those offered");
    }
}
