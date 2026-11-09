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
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.deque.DequeMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the timed `pollLast` method of a `BlockingDeque`**
///
/// [BlockingDeque#pollLast(long, TimeUnit)] "Retrieves and removes the last element of this deque, waiting up to the
/// specified wait time if necessary for an element to become available", and returns `null` "if the specified waiting
/// time elapses before an element is available". The contract checks that it returns the last element (what
/// `peekLast()` returned just before) and removes it, returns `null` on an empty deque only after at least the timeout
/// ([BlockingQueueContractSupport#TIMED_WAIT_MILLIS], measured with [System#nanoTime]), and throws
/// [InterruptedException] when interrupted on an empty deque.
///
/// *Stricter reading:* the interrupt status is set before the call, as
/// [com.soliscode.test.contract.blockingqueue.PutContract] does.
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @see BlockingDeque#pollLast(long, TimeUnit)
/// @since 0.1.2
public interface PollLastWithTimeoutContract<D extends BlockingDeque<E>, E>
        extends BlockingQueueContractSupport<D, E> {

    /// Tests that the timed `pollLast` returns the last element and removes it, once for each element, leaving the
    /// deque empty; or, where it is not supported, throws [UnsupportedOperationException].
    ///
    /// @throws Exception if the deque throws one
    @DisplayName("pollLast(long, TimeUnit) returns and removes the last element of the deque")
    @Test
    default void pollLastWithTimeoutReturnsAndRemovesLastElement() throws Exception {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        D deque = provider().createInstance(values);
        if (supportsMethod(BlockingDequeMethods.POLL_LAST_TIMEOUT)) {
            List<@Nullable E> polled = new ArrayList<>();
            boundedRun(() -> {
                for (int i = 0; i < values.size(); i++) {
                    @Nullable E expected = supportsMethod(DequeMethods.PEEK_LAST) ? deque.peekLast() : null;
                    E e = deque.pollLast(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS);
                    if (expected != null) {
                        assertEquals(expected, e, "pollLast(long, TimeUnit) did not return the last element");
                    }
                    polled.add(e);
                    assertEquals(values.size() - i - 1, deque.size(),
                            "pollLast(long, TimeUnit) did not remove the element");
                }
            });
            assertSameElements(values, polled, "the elements pollLast(long, TimeUnit) returned");
            assertEquals(0, deque.size(), "pollLast(long, TimeUnit) left elements in the deque");
        } else {
            assertThrows(UnsupportedOperationException.class,
                    () -> boundedRun(() -> deque.pollLast(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
        }
    }

    /// Tests that the timed `pollLast` on an empty deque returns `null` only once at least the timeout has passed.
    ///
    /// @throws Exception if the deque throws one
    @DisplayName("pollLast(long, TimeUnit) on an empty deque returns null after the timeout")
    @Test
    default void pollLastWithTimeoutWhenEmptyReturnsNullAfterTimeout() throws Exception {
        if (supportsMethod(BlockingDequeMethods.POLL_LAST_TIMEOUT)) {
            D deque = provider().emptyInstance();
            long waited = bounded(() -> {
                long start = System.nanoTime();
                assertNull(deque.pollLast(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS),
                        "pollLast(long, TimeUnit) returned an element from an empty deque");
                return System.nanoTime() - start;
            });
            assertTrue(waited >= TimeUnit.MILLISECONDS.toNanos(TIMED_WAIT_MILLIS),
                    "pollLast(long, TimeUnit) returned null after " + waited + " ns, before its timeout of "
                            + TIMED_WAIT_MILLIS + " ms");
        }
    }

    /// Tests that the timed `pollLast` on an empty deque, by a thread whose interrupt status is set, throws
    /// [InterruptedException].
    ///
    /// @throws Exception if the deque throws one other than the expected
    @DisplayName("pollLast(long, TimeUnit) on an empty deque throws InterruptedException when interrupted")
    @Test
    default void pollLastWithTimeoutWhenEmptyAndInterruptedThrowsInterruptedException() throws Exception {
        if (supportsMethod(BlockingDequeMethods.POLL_LAST_TIMEOUT)) {
            D deque = provider().emptyInstance();
            assertThrows(InterruptedException.class,
                    () -> boundedInterrupted(() -> deque.pollLast(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
        }
    }
}
