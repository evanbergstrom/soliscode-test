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

/// **Contract for the timed `pollFirst` method of a `BlockingDeque`**
///
/// [BlockingDeque#pollFirst(long, TimeUnit)] "Retrieves and removes the first element of this deque, waiting up to the
/// specified wait time if necessary for an element to become available", and returns `null` "if the specified waiting
/// time elapses before an element is available". The contract checks that it returns the first element (what
/// `peekFirst()` returned just before) and removes it, returns `null` on an empty deque only after at least the timeout
/// ([BlockingQueueContractSupport#TIMED_WAIT_MILLIS], measured with [System#nanoTime]), and throws
/// [InterruptedException] when interrupted on an empty deque.
///
/// *Stricter reading:* the interrupt status is set before the call, as
/// [com.soliscode.test.contract.blockingqueue.PutContract] does.
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @see BlockingDeque#pollFirst(long, TimeUnit)
/// @since 0.1.2
public interface PollFirstWithTimeoutContract<D extends BlockingDeque<E>, E>
        extends BlockingQueueContractSupport<D, E> {

    /// Tests that the timed `pollFirst` returns the first element and removes it, once for each element, leaving the
    /// deque empty; or, where it is not supported, throws [UnsupportedOperationException].
    ///
    /// @throws Exception if the deque throws one
    @DisplayName("pollFirst(long, TimeUnit) returns and removes the first element of the deque")
    @Test
    default void pollFirstWithTimeoutReturnsAndRemovesFirstElement() throws Exception {
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        D deque = provider().createInstance(values);
        if (supportsMethod(BlockingDequeMethods.POLL_FIRST_TIMEOUT)) {
            List<@Nullable E> polled = new ArrayList<>();
            boundedRun(() -> {
                for (int i = 0; i < values.size(); i++) {
                    @Nullable E expected = supportsMethod(DequeMethods.PEEK_FIRST) ? deque.peekFirst() : null;
                    E e = deque.pollFirst(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS);
                    if (expected != null) {
                        assertEquals(expected, e, "pollFirst(long, TimeUnit) did not return the first element");
                    }
                    polled.add(e);
                    assertEquals(values.size() - i - 1, deque.size(),
                            "pollFirst(long, TimeUnit) did not remove the element");
                }
            });
            assertSameElements(values, polled, "the elements pollFirst(long, TimeUnit) returned");
            assertEquals(0, deque.size(), "pollFirst(long, TimeUnit) left elements in the deque");
        } else {
            assertThrows(UnsupportedOperationException.class,
                    () -> boundedRun(() -> deque.pollFirst(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
        }
    }

    /// Tests that the timed `pollFirst` on an empty deque returns `null` only once at least the timeout has passed.
    ///
    /// @throws Exception if the deque throws one
    @DisplayName("pollFirst(long, TimeUnit) on an empty deque returns null after the timeout")
    @Test
    default void pollFirstWithTimeoutWhenEmptyReturnsNullAfterTimeout() throws Exception {
        if (supportsMethod(BlockingDequeMethods.POLL_FIRST_TIMEOUT)) {
            D deque = provider().emptyInstance();
            long waited = bounded(() -> {
                long start = System.nanoTime();
                assertNull(deque.pollFirst(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS),
                        "pollFirst(long, TimeUnit) returned an element from an empty deque");
                return System.nanoTime() - start;
            });
            assertTrue(waited >= TimeUnit.MILLISECONDS.toNanos(TIMED_WAIT_MILLIS),
                    "pollFirst(long, TimeUnit) returned null after " + waited + " ns, before its timeout of "
                            + TIMED_WAIT_MILLIS + " ms");
        }
    }

    /// Tests that the timed `pollFirst` on an empty deque, by a thread whose interrupt status is set, throws
    /// [InterruptedException].
    ///
    /// @throws Exception if the deque throws one other than the expected
    @DisplayName("pollFirst(long, TimeUnit) on an empty deque throws InterruptedException when interrupted")
    @Test
    default void pollFirstWithTimeoutWhenEmptyAndInterruptedThrowsInterruptedException() throws Exception {
        if (supportsMethod(BlockingDequeMethods.POLL_FIRST_TIMEOUT)) {
            D deque = provider().emptyInstance();
            assertThrows(InterruptedException.class,
                    () -> boundedInterrupted(() -> deque.pollFirst(TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
        }
    }
}
