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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.queue.QueueMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the timed `poll` method of a `BlockingQueue`**
///
/// [BlockingQueue#poll(long, TimeUnit)] "Retrieves and removes the head of this queue, waiting up to the specified
/// wait time if necessary for an element to become available", and returns `null` "if the specified waiting time
/// elapses before an element is available". The contract checks that it returns the head (what `peek()` returned
/// just before) and removes it, returns `null` on an empty queue only after at least the timeout
/// ([BlockingQueueContractSupport#TIMED_WAIT_MILLIS], measured with [System#nanoTime]), and throws
/// [InterruptedException] when interrupted on an empty queue.
///
/// *Stricter reading:* the interrupt status is set before the call, as [PutContract] does.
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#poll(long, TimeUnit)
/// @since 0.1.2
public interface PollWithTimeoutContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// Tests that the timed `poll` returns the head and removes it, once for each element, leaving the queue empty;
    /// or, where it is not supported, throws [UnsupportedOperationException].
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("poll(long, TimeUnit) returns and removes the head of the queue")
    @Test
    default void pollWithTimeoutReturnsAndRemovesHead() throws Exception {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        Q queue = provider().createInstance(values);
        if (supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            List<@Nullable E> polled = new ArrayList<>();
            boundedRun(() -> {
                for (int i = 0; i < values.size(); i++) {
                    @Nullable E head = supportsMethod(QueueMethods.PEEK) ? queue.peek() : null;
                    E e = queue.poll(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS);
                    if (head != null) {
                        assertEquals(head, e, "poll(long, TimeUnit) did not return the head");
                    }
                    polled.add(e);
                    assertEquals(values.size() - i - 1, queue.size(), "poll(long, TimeUnit) did not remove the head");
                }
            });
            assertSameElements(values, polled, "the elements poll(long, TimeUnit) returned");
            assertEquals(0, queue.size(), "poll(long, TimeUnit) left elements in the queue");
        } else {
            assertThrows(UnsupportedOperationException.class,
                    () -> boundedRun(() -> queue.poll(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
        }
    }

    /// Tests that the timed `poll` on an empty queue returns `null` only once at least the timeout has passed.
    ///
    /// @throws Exception if the queue throws one
    @DisplayName("poll(long, TimeUnit) on an empty queue returns null after the timeout")
    @Test
    default void pollWithTimeoutWhenEmptyReturnsNullAfterTimeout() throws Exception {
        if (supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            long waited = bounded(() -> {
                long start = System.nanoTime();
                assertNull(queue.poll(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS),
                        "poll(long, TimeUnit) returned an element from an empty queue");
                return System.nanoTime() - start;
            });
            assertTrue(waited >= TimeUnit.MILLISECONDS.toNanos(TIMED_WAIT_MILLIS),
                    "poll(long, TimeUnit) returned null after " + waited + " ns, before its timeout of "
                            + TIMED_WAIT_MILLIS + " ms");
        }
    }

    /// Tests that the timed `poll` on an empty queue, by a thread whose interrupt status is set, throws
    /// [InterruptedException].
    ///
    /// @throws Exception if the queue throws one other than the expected
    @DisplayName("poll(long, TimeUnit) on an empty queue throws InterruptedException when interrupted")
    @Test
    default void pollWithTimeoutWhenEmptyAndInterruptedThrowsInterruptedException() throws Exception {
        if (supportsMethod(BlockingQueueMethods.POLL_TIMEOUT)) {
            Q queue = provider().emptyInstance();
            assertThrows(InterruptedException.class,
                    () -> boundedInterrupted(() -> queue.poll(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
        }
    }
}
