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
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.contract.deque.DequeMethods;
import com.soliscode.test.contract.sequencedcollection.SequencedCollectionMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the non-blocking inserts of a capacity-bounded `BlockingDeque`**
///
/// [BlockingDeque] restates five inserts with a meaning for a full deque: [BlockingDeque#addFirst(Object)],
/// [BlockingDeque#addLast(Object)] and [BlockingDeque#push(Object)] throw [IllegalStateException] "if the element
/// cannot be added at this time due to capacity restrictions", and [BlockingDeque#offerFirst(Object)] and
/// [BlockingDeque#offerLast(Object)] return `false` then. The contract checks each on a full deque, and that the deque
/// is left unchanged. The deque contracts ([com.soliscode.test.contract.deque.DequeContract]) test the rest of them.
///
/// It is one contract for the five, not one for each, because they share one property, capacity, and because
/// `BlockingDeque`'s contract already holds a `PushContract` ([com.soliscode.test.contract.deque.PushContract]).
///
/// It runs only on a deque with at most [BlockingQueueContractSupport#FILL_LIMIT] places
/// ([BlockingQueueContractSupport#fullInstance]).
///
/// @param <D> the blocking deque type being tested
/// @param <E> the element type being tested
/// @see BlockingDeque#addFirst(Object)
/// @see BlockingDeque#offerFirst(Object)
/// @since 0.1.2
public interface FullDequeContract<D extends BlockingDeque<E>, E> extends BlockingQueueContractSupport<D, E> {

    /// Tests that `addFirst` on a full deque throws [IllegalStateException] and does not add the element.
    @DisplayName("addFirst(E) on a full deque throws IllegalStateException")
    @Test
    default void addFirstWhenFullThrowsIllegalStateException() {
        assertThrowsWhenFull(SequencedCollectionMethods.ADD_FIRST, "addFirst(E)", (d, e) -> d.addFirst(e));
    }

    /// Tests that `addLast` on a full deque throws [IllegalStateException] and does not add the element.
    @DisplayName("addLast(E) on a full deque throws IllegalStateException")
    @Test
    default void addLastWhenFullThrowsIllegalStateException() {
        assertThrowsWhenFull(SequencedCollectionMethods.ADD_LAST, "addLast(E)", (d, e) -> d.addLast(e));
    }

    /// Tests that `push` on a full deque throws [IllegalStateException] and does not add the element.
    @DisplayName("push(E) on a full deque throws IllegalStateException")
    @Test
    default void pushWhenFullThrowsIllegalStateException() {
        assertThrowsWhenFull(DequeMethods.PUSH, "push(E)", (d, e) -> d.push(e));
    }

    /// Tests that `offerFirst` on a full deque returns `false` and does not add the element.
    @DisplayName("offerFirst(E) on a full deque returns false")
    @Test
    default void offerFirstWhenFullReturnsFalse() {
        assertFalseWhenFull(DequeMethods.OFFER_FIRST, "offerFirst(E)", (d, e) -> d.offerFirst(e));
    }

    /// Tests that `offerLast` on a full deque returns `false` and does not add the element.
    @DisplayName("offerLast(E) on a full deque returns false")
    @Test
    default void offerLastWhenFullReturnsFalse() {
        assertFalseWhenFull(DequeMethods.OFFER_LAST, "offerLast(E)", (d, e) -> d.offerLast(e));
    }

    /// Asserts that an insert on a full deque, of an element it does not hold, throws [IllegalStateException] and
    /// leaves the deque unchanged. Returns without testing when the method is not supported or there is no full
    /// deque.
    ///
    /// @param method the insert
    /// @param name the insert, for the failure message
    /// @param insert calls the insert
    private void assertThrowsWhenFull(final InterfaceMethod method, final String name, final BiConsumer<D, E> insert) {
        whenFull(method, name, (d, e) -> assertThrows(IllegalStateException.class, () -> insert.accept(d, e),
                name + " on a full deque"));
    }

    /// Asserts that an insert on a full deque, of an element it does not hold, returns `false` and leaves the deque
    /// unchanged. Returns without testing when the method is not supported or there is no full deque.
    ///
    /// @param method the insert
    /// @param name the insert, for the failure message
    /// @param insert calls the insert
    private void assertFalseWhenFull(final InterfaceMethod method, final String name,
                                     final BiPredicate<D, E> insert) {
        whenFull(method, name, (d, e) -> assertFalse(insert.test(d, e), name + " returned true for a full deque"));
    }

    /// Calls `check` with a full deque and an element it does not hold, then asserts that the deque is unchanged.
    ///
    /// @param method the insert
    /// @param name the insert, for the failure message
    /// @param check calls the insert and checks its result
    private void whenFull(final InterfaceMethod method, final String name, final BiConsumer<D, E> check) {
        if (!supportsMethod(method)) {
            return;
        }
        Optional<D> full = fullInstance();
        if (full.isEmpty()) {
            return;
        }
        D deque = full.get();
        int size = deque.size();
        E extra = elementProvider().createInstanceNotIn(new ArrayList<>(deque));
        check.accept(deque, extra);
        assertFalse(deque.contains(extra), name + " added the element to a full deque");
        assertEquals(size, deque.size());
    }
}
