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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.deque.DequeMethods;
import com.soliscode.test.contract.queue.QueueMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `putLast` method of a `BlockingDeque`**
///
/// [BlockingDeque#putLast(Object)] "Inserts the specified element at the end of this deque, waiting if necessary for
/// space to become available". The contract checks that it adds each element at the end of a deque with room, refuses
/// `null`, waits on a full deque until another thread makes room, and throws [InterruptedException] when interrupted
/// on a full deque.
///
/// *Stricter reading:* `putLast` throws `InterruptedException` "if interrupted while waiting"; the contract sets the
/// interrupt status before the call, and expects the exception, as
/// [com.soliscode.test.contract.blockingqueue.PutContract] does.
///
/// The tests of a full deque run only on a deque with at most [BlockingQueueContractSupport#FILL_LIMIT] places
/// ([BlockingQueueContractSupport#fullInstance]).
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @see BlockingDeque#putLast(Object)
/// @since 0.1.2
public interface PutLastContract<D extends BlockingDeque<E>, E> extends BlockingQueueContractSupport<D, E> {

    /// Tests that `putLast` adds each element at the end of a deque with room, or, where it is not supported, throws
    /// [UnsupportedOperationException].
    ///
    /// @throws Exception if the deque throws one
    @DisplayName("putLast(E) adds each element at the end of a deque with room")
    @Test
    default void putLastWithRoomAddsElementAtEnd() throws Exception {
        D deque = provider().emptyInstance();
        List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
        if (supportsMethod(BlockingDequeMethods.PUT_LAST)) {
            boundedRun(() -> {
                for (int i = 0; i < values.size(); i++) {
                    deque.putLast(values.get(i));
                    assertTrue(deque.contains(values.get(i)), "putLast(E) did not add the element");
                    assertEquals(i + 1, deque.size());
                    if (supportsMethod(DequeMethods.PEEK_LAST)) {
                        assertEquals(values.get(i), deque.peekLast(), "putLast(E) did not add the element at the end");
                    }
                }
            });
        } else {
            assertThrows(UnsupportedOperationException.class, () -> boundedRun(() -> deque.putLast(values.get(0))));
        }
    }

    /// Tests that `putLast(null)` throws [NullPointerException]: a `BlockingDeque` does "not permit null elements".
    ///
    /// @throws Exception if the deque throws one other than the expected
    @DisplayName("putLast(null) throws NullPointerException")
    @Test
    // NullAway: the test passes null on purpose, to check that putLast refuses it.
    @SuppressWarnings("NullAway")
    default void putLastWithNullThrowsNullPointerException() throws Exception {
        if (supportsMethod(BlockingDequeMethods.PUT_LAST)) {
            D deque = provider().emptyInstance();
            assertThrows(NullPointerException.class, () -> boundedRun(() -> deque.putLast(null)));
            assertEquals(0, deque.size(), "putLast(null) changed the deque");
        }
    }

    /// Tests that `putLast` on a full deque waits until another thread makes room (with `poll()`), then adds the
    /// element.
    @DisplayName("putLast(E) on a full deque waits until another thread makes room")
    @Test
    default void putLastWhenFullWaitsForRoom() {
        if (supportsMethod(BlockingDequeMethods.PUT_LAST) && supportsMethod(QueueMethods.POLL)) {
            Optional<D> full = fullInstance();
            if (full.isEmpty() || full.get().size() == 0) {
                return;
            }
            D deque = full.get();
            int size = deque.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(deque));
            assertWaitsFor(deque, d -> d.putLast(extra), d -> d.poll());
            assertTrue(deque.contains(extra), "putLast(E) returned without adding the element");
            assertEquals(size, deque.size());
        }
    }

    /// Tests that `putLast` on a full deque, by a thread whose interrupt status is set, throws [InterruptedException]
    /// and does not add the element.
    ///
    /// @throws Exception if the deque throws one other than the expected
    @DisplayName("putLast(E) on a full deque throws InterruptedException when interrupted")
    @Test
    default void putLastWhenFullAndInterruptedThrowsInterruptedException() throws Exception {
        if (supportsMethod(BlockingDequeMethods.PUT_LAST)) {
            Optional<D> full = fullInstance();
            if (full.isEmpty()) {
                return;
            }
            D deque = full.get();
            int size = deque.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(deque));
            assertThrows(InterruptedException.class, () -> boundedInterrupted(() -> deque.putLast(extra)));
            assertFalse(deque.contains(extra), "an interrupted putLast(E) added the element");
            assertEquals(size, deque.size());
        }
    }
}
