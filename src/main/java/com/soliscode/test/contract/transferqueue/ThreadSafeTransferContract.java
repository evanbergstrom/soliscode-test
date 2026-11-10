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
package com.soliscode.test.contract.transferqueue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.blockingqueue.BlockingQueueMethods;
import com.soliscode.test.contract.support.TransferQueueContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;
import com.soliscode.test.util.RecordingSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// **Contract for `transfer` of a `TransferQueue` under contention**
///
/// A `TransferQueue` is a [java.util.concurrent.BlockingQueue], whose "queuing methods achieve their effects
/// atomically". Producers `transfer` unique elements, and consumers `take` as many. After each `transfer` returns,
/// its element must have been received, so it is no longer in the queue; every element must be taken exactly once,
/// and the queue left empty.
///
/// A producer that finds its element still enqueued counts it and carries on, so that the consumers' takes still
/// balance and the run ends; the count must be 0. The test runs on a [ContentionCoordinator], which bounds it in
/// time.
///
/// @param <Q> the transfer queue type being tested
/// @param <E> the element type being tested
/// @see TransferQueue#transfer(Object)
/// @see TransferQueue#take()
/// @since 0.1.2
public interface ThreadSafeTransferContract<Q extends TransferQueue<E>, E> extends TransferQueueContractSupport<Q, E> {

    /// The number of producers ({@value}), twice the number of consumers: the more producers race for one waiting
    /// consumer, the likelier a check-then-act hand-off shows (measured on #34).
    int PRODUCERS = 4;

    /// The number of consumers ({@value}); each takes as many elements as two producers transfer.
    int CONSUMERS = 2;

    /// How many times ({@value}) each producer calls the queue; each consumer calls it twice as often. Each transfer
    /// waits for a consumer to take its element, so the calls run one hand-off at a time (measured on #34).
    int ITERATIONS = 5_000;

    /// How long ({@value} s) the coordinator waits for the producers and consumers to finish, longer than its default
    /// of 5 s, since the calls run one hand-off at a time, and below the 10 s
    /// [com.soliscode.test.annotations.TestTags#THREAD_SAFETY_TIMEOUT] that [ConcurrencyTest] gives the test under
    /// JUnit, so that the coordinator, not JUnit, reports a run that does not finish.
    int SHUTDOWN_TIMEOUT_SECONDS = 9;

    /// Tests that elements handed over by producers and taken by consumers, all at once, are each received before
    /// the producer's call returns, and taken exactly once.
    @DisplayName("transfer(E) and take() are thread safe under contention with each other")
    @VerySlow
    @ConcurrencyTest
    default void transferAndTakeWhenCalledUnderContentionAreThreadSafe() {
        if (!supportsMethod(TransferQueueMethods.TRANSFER) || !supportsMethod(BlockingQueueMethods.TAKE)) {
            return;
        }
        Q queue = provider().emptyInstance();
        RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();
        Queue<E> taken = new ConcurrentLinkedQueue<>();
        AtomicInteger leftEnqueued = new AtomicInteger();
        ContentionCoordinator<Q> coordinator = new ContentionCoordinator<>(
                ContentionCoordinator.DEFAULT_START_TIMEOUT_SECONDS, SHUTDOWN_TIMEOUT_SECONDS,
                ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
        coordinator.add(PRODUCERS, ITERATIONS, unchecked(q -> {
            E e = supplier.get();
            q.transfer(e);
            if (q.contains(e)) {
                leftEnqueued.incrementAndGet();
            }
        }));
        coordinator.add(CONSUMERS, ITERATIONS * PRODUCERS / CONSUMERS, unchecked(q -> taken.add(q.take())));
        coordinator.execute(queue);
        assertEquals(0, leftEnqueued.get(), "transfer(E) returned while its element was still enqueued, not received");
        assertEquals(0, queue.size(), "elements left after as many takes as transfers");
        assertSameElements(supplier.recorded(), taken, "the elements taken");
    }
}
