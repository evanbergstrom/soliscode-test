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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;
import com.soliscode.test.safety.ContentionCoordinator;
import com.soliscode.test.util.RecordingSupplier;

/// **Contract for the timed `offerFirst`, `offerLast`, `pollFirst` and `pollLast` of a `BlockingDeque` under
/// contention**
///
/// "Like BlockingQueue, a BlockingDeque is thread safe" ([BlockingDeque]). Producers offer unique elements,
/// alternating the timed `offerFirst` and `offerLast`, and record those accepted; consumers poll and record what
/// they get. What was polled, with what is left, must be exactly what was accepted: nothing lost, nothing polled
/// twice.
///
/// The test runs twice, on a fresh deque each time: once with every consumer calling the timed `pollLast`, then with
/// every consumer calling the timed `pollFirst`. So the consumers always contend for the same end. A timeout of zero
/// takes the timed methods' path without waiting, as
/// [com.soliscode.test.contract.blockingqueue.ThreadSafeOfferPollContract] does, so the test stays fast.
///
/// Each run is on a [ContentionCoordinator], which bounds it in time.
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @see BlockingDeque#offerFirst(Object, long, TimeUnit)
/// @see BlockingDeque#pollLast(long, TimeUnit)
/// @since 0.1.2
public interface ThreadSafeOfferPollFirstLastContract<D extends BlockingDeque<E>, E>
        extends BlockingQueueContractSupport<D, E> {

    /// The number of producers, and of consumers.
    int THREADS = 2;

    /// How many times each producer and each consumer calls the deque, in each run.
    int ITERATIONS = 10_000;

    /// Tests that elements offered at both ends by producers and polled by consumers at one end, all at once, are
    /// each polled at most once, and that none is lost; first with the consumers at the last end, then at the first.
    @DisplayName("the timed offerFirst, offerLast, pollFirst and pollLast are thread safe under contention")
    @VerySlow
    @ConcurrencyTest
    default void timedOfferAndPollAtBothEndsWhenCalledUnderContentionAreThreadSafe() {
        if (!supportsMethod(BlockingDequeMethods.OFFER_FIRST_TIMEOUT)
                || !supportsMethod(BlockingDequeMethods.OFFER_LAST_TIMEOUT)
                || !supportsMethod(BlockingDequeMethods.POLL_FIRST_TIMEOUT)
                || !supportsMethod(BlockingDequeMethods.POLL_LAST_TIMEOUT)) {
            return;
        }
        for (final boolean fromLast : new boolean[] {true, false}) {
            D deque = provider().emptyInstance();
            RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();
            Queue<E> offered = new ConcurrentLinkedQueue<>();
            Queue<E> polled = new ConcurrentLinkedQueue<>();
            AtomicLong offers = new AtomicLong();
            ContentionCoordinator<D> coordinator = new ContentionCoordinator<>();
            coordinator.add(THREADS, ITERATIONS, unchecked(d -> {
                E e = supplier.get();
                boolean accepted = offers.getAndIncrement() % 2 == 0 ? d.offerFirst(e, 0, TimeUnit.MILLISECONDS)
                        : d.offerLast(e, 0, TimeUnit.MILLISECONDS);
                if (accepted) {
                    offered.add(e);
                }
            }));
            coordinator.add(THREADS, ITERATIONS, unchecked(d -> {
                @Nullable E e = fromLast ? d.pollLast(0, TimeUnit.MILLISECONDS) : d.pollFirst(0, TimeUnit.MILLISECONDS);
                if (e != null) {
                    polled.add(e);
                }
            }));
            coordinator.execute(deque);
            List<E> all = new ArrayList<>(polled);
            all.addAll(deque);
            assertSameElements(offered, all, "the elements polled with " + (fromLast ? "pollLast" : "pollFirst")
                    + " and left, against those offered");
        }
    }
}
