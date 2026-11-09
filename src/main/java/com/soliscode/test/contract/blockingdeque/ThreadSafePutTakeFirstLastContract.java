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
package com.soliscode.test.contract.blockingdeque;

import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;
import com.soliscode.test.util.RecordingSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// **Contract for `putFirst`, `putLast`, `takeFirst` and `takeLast` of a `BlockingDeque` under contention**
///
/// "Like BlockingQueue, a BlockingDeque is thread safe" ([BlockingDeque]), and a `BlockingQueue`'s queuing methods
/// "achieve their effects atomically". Producers put unique elements, alternating `putFirst` and `putLast`, and
/// consumers take as many. Every element must be taken exactly once, and the deque left empty.
///
/// The test runs twice, on a fresh deque each time: once with every consumer calling `takeLast`, then with every
/// consumer calling `takeFirst`. So the consumers always contend for the same end.
///
/// Each run is on a [ContentionCoordinator], which bounds it in time.
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @see BlockingDeque#putFirst(Object)
/// @see BlockingDeque#takeLast()
/// @since 0.1.2
public interface ThreadSafePutTakeFirstLastContract<D extends BlockingDeque<E>, E>
        extends BlockingQueueContractSupport<D, E> {

    /// The number of producers, and of consumers.
    int THREADS = 2;

    /// How many times each producer and each consumer calls the deque, in each run.
    int ITERATIONS = 10_000;

    /// Tests that elements put at both ends by producers and taken by consumers at one end, all at once, are each
    /// taken exactly once; first with the consumers at the last end, then at the first.
    @DisplayName("putFirst(E), putLast(E), takeFirst() and takeLast() are thread safe under contention")
    @VerySlow
    @ConcurrencyTest
    default void putAndTakeAtBothEndsWhenCalledUnderContentionAreThreadSafe() {
        if (!supportsMethod(BlockingDequeMethods.PUT_FIRST) || !supportsMethod(BlockingDequeMethods.PUT_LAST)
                || !supportsMethod(BlockingDequeMethods.TAKE_FIRST)
                || !supportsMethod(BlockingDequeMethods.TAKE_LAST)) {
            return;
        }
        for (final boolean fromLast : new boolean[] {true, false}) {
            D deque = provider().emptyInstance();
            RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();
            Queue<E> taken = new ConcurrentLinkedQueue<>();
            AtomicLong puts = new AtomicLong();
            ContentionCoordinator<D> coordinator = new ContentionCoordinator<>();
            coordinator.add(THREADS, ITERATIONS, unchecked(d -> {
                E e = supplier.get();
                if (puts.getAndIncrement() % 2 == 0) {
                    d.putFirst(e);
                } else {
                    d.putLast(e);
                }
            }));
            coordinator.add(THREADS, ITERATIONS, unchecked(d -> taken.add(fromLast ? d.takeLast() : d.takeFirst())));
            coordinator.execute(deque);
            String consumers = fromLast ? "takeLast()" : "takeFirst()";
            assertEquals(0, deque.size(), "elements left after as many calls of " + consumers + " as puts");
            assertSameElements(supplier.recorded(), taken, "the elements " + consumers + " took");
        }
    }
}
