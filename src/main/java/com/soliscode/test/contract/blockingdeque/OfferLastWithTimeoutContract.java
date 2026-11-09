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
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.deque.DequeMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the timed `offerLast` method of a `BlockingDeque`**
///
/// [BlockingDeque#offerLast(Object, long, TimeUnit)] "Inserts the specified element at the end of this deque, waiting
/// up to the specified wait time if necessary for space to become available", and returns `false` "if the specified
/// waiting time elapses before space is available". The contract checks that it adds each element at the end of a
/// deque with room and returns `true`, refuses `null`, returns `false` on a full deque only after at least the timeout
/// ([BlockingQueueContractSupport#TIMED_WAIT_MILLIS], measured with [System#nanoTime]) without adding the element,
/// and throws [InterruptedException] when interrupted on a full deque.
///
/// *Stricter reading:* the interrupt status is set before the call, as
/// [com.soliscode.test.contract.blockingqueue.PutContract] does.
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @see BlockingDeque#offerLast(Object, long, TimeUnit)
/// @since 0.1.2
public interface OfferLastWithTimeoutContract<D extends BlockingDeque<E>, E>
        extends BlockingQueueContractSupport<D, E> {

    /// Tests that the timed `offerLast` adds each element at the end of a deque with room and returns `true`, or,
    /// where it is not supported, throws [UnsupportedOperationException].
    ///
    /// @throws Exception if the deque throws one
    @DisplayName("offerLast(E, long, TimeUnit) adds each element at the end of a deque with room and returns true")
    @Test
    default void offerLastWithTimeoutWithRoomAddsElementAtEnd() throws Exception {
        D deque = provider().emptyInstance();
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        if (supportsMethod(BlockingDequeMethods.OFFER_LAST_TIMEOUT)) {
            boundedRun(() -> {
                for (int i = 0; i < values.size(); i++) {
                    assertTrue(deque.offerLast(values.get(i), TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS),
                            "offerLast(E, long, TimeUnit) returned false for a deque with room");
                    assertTrue(deque.contains(values.get(i)), "offerLast(E, long, TimeUnit) did not add the element");
                    assertEquals(i + 1, deque.size());
                    if (supportsMethod(DequeMethods.PEEK_LAST)) {
                        assertEquals(values.get(i), deque.peekLast(),
                                "offerLast(E, long, TimeUnit) did not add the element at the end");
                    }
                }
            });
        } else {
            assertThrows(UnsupportedOperationException.class, () -> boundedRun(
                    () -> deque.offerLast(values.get(0), TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
        }
    }

    /// Tests that the timed `offerLast` of `null` throws [NullPointerException].
    ///
    /// @throws Exception if the deque throws one other than the expected
    @DisplayName("offerLast(null, long, TimeUnit) throws NullPointerException")
    @Test
    // NullAway: the test passes null on purpose, to check that the timed offerLast refuses it.
    @SuppressWarnings("NullAway")
    default void offerLastWithTimeoutWithNullThrowsNullPointerException() throws Exception {
        if (supportsMethod(BlockingDequeMethods.OFFER_LAST_TIMEOUT)) {
            D deque = provider().emptyInstance();
            assertThrows(NullPointerException.class,
                    () -> boundedRun(() -> deque.offerLast(null, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
            assertEquals(0, deque.size(), "offerLast(null, long, TimeUnit) changed the deque");
        }
    }

    /// Tests that the timed `offerLast` on a full deque returns `false` only once at least the timeout has passed,
    /// and does not add the element.
    ///
    /// @throws Exception if the deque throws one
    @DisplayName("offerLast(E, long, TimeUnit) on a full deque returns false after the timeout")
    @Test
    default void offerLastWithTimeoutWhenFullReturnsFalseAfterTimeout() throws Exception {
        if (supportsMethod(BlockingDequeMethods.OFFER_LAST_TIMEOUT)) {
            Optional<D> full = fullInstance();
            if (full.isEmpty()) {
                return;
            }
            D deque = full.get();
            int size = deque.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(deque));
            long waited = bounded(() -> {
                long start = System.nanoTime();
                assertFalse(deque.offerLast(extra, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS),
                        "offerLast(E, long, TimeUnit) returned true for a full deque");
                return System.nanoTime() - start;
            });
            assertTrue(waited >= TimeUnit.MILLISECONDS.toNanos(TIMED_WAIT_MILLIS),
                    "offerLast(E, long, TimeUnit) returned false after " + waited + " ns, before its timeout of "
                            + TIMED_WAIT_MILLIS + " ms");
            assertFalse(deque.contains(extra), "offerLast(E, long, TimeUnit) added the element to a full deque");
            assertEquals(size, deque.size());
        }
    }

    /// Tests that the timed `offerLast` on a full deque, by a thread whose interrupt status is set, throws
    /// [InterruptedException] and does not add the element.
    ///
    /// @throws Exception if the deque throws one other than the expected
    @DisplayName("offerLast(E, long, TimeUnit) on a full deque throws InterruptedException when interrupted")
    @Test
    default void offerLastWithTimeoutWhenFullAndInterruptedThrowsInterruptedException() throws Exception {
        if (supportsMethod(BlockingDequeMethods.OFFER_LAST_TIMEOUT)) {
            Optional<D> full = fullInstance();
            if (full.isEmpty()) {
                return;
            }
            D deque = full.get();
            int size = deque.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(deque));
            assertThrows(InterruptedException.class, () -> boundedInterrupted(
                    () -> deque.offerLast(extra, TIMED_WAIT_MILLIS, TimeUnit.MILLISECONDS)));
            assertFalse(deque.contains(extra), "an interrupted offerLast(E, long, TimeUnit) added the element");
            assertEquals(size, deque.size());
        }
    }
}
