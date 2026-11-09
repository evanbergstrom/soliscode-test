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
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the timed `offer` method of a `BlockingQueue`**
///
/// [BlockingQueue#offer(Object, long, TimeUnit)] "Inserts the specified element into this queue, waiting up to the
/// specified wait time if necessary for space to become available", and returns `false` "if the specified waiting
/// time elapses before space is available". The contract checks that it adds each element to a queue with room and
/// returns `true`, refuses `null`, returns `false` on a full queue only after at least the timeout
/// ([BlockingQueueContractSupport#TIMED_WAIT_MILLIS], measured with [System#nanoTime]) without adding the element,
/// and throws [InterruptedException] when interrupted on a full queue.
///
/// *Stricter reading:* the interrupt status is set before the call, as [PutContract] does.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#offer(Object, long, TimeUnit)
/// @since 0.1.2
public interface OfferWithTimeoutContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// Tests that the timed `offer` adds each element to a queue with room and returns `true`, or, where it is not
    /// supported, throws [UnsupportedOperationException].
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("offer(E, long, TimeUnit) adds each element to a queue with room and returns true")
    @Test
    default void offerWithTimeoutWithRoomAddsElementAndReturnsTrue() throws Exception {
        Q queue = provider().emptyInstance();
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        if (supportsMethod(BlockingQueueMethods.OFFER_TIMEOUT)) {
            boundedRun(() -> {
                for (int i = 0; i < values.size(); i++) {
                    assertTrue(queue.offer(values.get(i), TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS),
                            "offer(E, long, TimeUnit) returned false for a queue with room");
                    assertTrue(queue.contains(values.get(i)), "offer(E, long, TimeUnit) did not add the element");
                    assertEquals(i + 1, queue.size());
                }
            });
        } else {
            assertThrows(UnsupportedOperationException.class, () -> boundedRun(
                    () -> queue.offer(values.get(0), TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
        }
    }

    /// Tests that the timed `offer` of `null` throws [NullPointerException].
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("offer(null, long, TimeUnit) throws NullPointerException")
    @Test
    // NullAway: the test passes null on purpose, to check that the timed offer refuses it.
    @SuppressWarnings("NullAway")
    default void offerWithTimeoutWithNullThrowsNullPointerException() throws Exception {
        if (supportsMethod(BlockingQueueMethods.OFFER_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            assertThrows(NullPointerException.class,
                    () -> boundedRun(() -> queue.offer(null, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
            assertEquals(0, queue.size(), "offer(null, long, TimeUnit) changed the queue");
        }
    }

    /// Tests that the timed `offer` on a full queue returns `false` only once at least the timeout has passed, and
    /// does not add the element.
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("offer(E, long, TimeUnit) on a full queue returns false after the timeout")
    @Test
    default void offerWithTimeoutWhenFullReturnsFalseAfterTimeout() throws Exception {
        if (supportsMethod(BlockingQueueMethods.OFFER_TIMEOUT)) {
            Optional<Q> full = fullInstance();
            if (full.isEmpty()) {
                return;
            }
            Q queue = full.get();
            int size = queue.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(queue));
            long waited = bounded(() -> {
                long start = System.nanoTime();
                assertFalse(queue.offer(extra, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS),
                        "offer(E, long, TimeUnit) returned true for a full queue");
                return System.nanoTime() - start;
            });
            assertTrue(waited >= TimeUnit.MILLISECONDS.toNanos(TIMED_WAIT_MILLIS),
                    "offer(E, long, TimeUnit) returned false after " + waited + " ns, before its timeout of "
                            + TIMED_WAIT_MILLIS + " ms");
            assertFalse(queue.contains(extra), "offer(E, long, TimeUnit) added the element to a full queue");
            assertEquals(size, queue.size());
        }
    }

    /// Tests that the timed `offer` on a full queue, by a thread whose interrupt status is set, throws
    /// [InterruptedException] and does not add the element.
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("offer(E, long, TimeUnit) on a full queue throws InterruptedException when interrupted")
    @Test
    default void offerWithTimeoutWhenFullAndInterruptedThrowsInterruptedException() throws Exception {
        if (supportsMethod(BlockingQueueMethods.OFFER_TIMEOUT)) {
            Optional<Q> full = fullInstance();
            if (full.isEmpty()) {
                return;
            }
            Q queue = full.get();
            int size = queue.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(queue));
            assertThrows(InterruptedException.class, () -> boundedInterrupted(
                    () -> queue.offer(extra, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
            assertFalse(queue.contains(extra), "an interrupted offer(E, long, TimeUnit) added the element");
            assertEquals(size, queue.size());
        }
    }
}
