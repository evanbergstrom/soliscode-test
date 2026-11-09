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
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.queue.QueueMethods;
import com.soliscode.test.contract.support.BlockingQueueContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/// **Contract for the `offer` method of a capacity-bounded `BlockingQueue`**
///
/// [BlockingQueue#offer(Object)] returns "`true` if the element was added to this queue, else `false`", inserting
/// it only "if it is possible to do so immediately without violating capacity restrictions". The contract checks that
/// on a full queue it returns `false` and leaves the queue unchanged. [com.soliscode.test.contract.queue.OfferContract]
/// tests the rest of `offer`.
///
/// It runs only on a queue with at most [BlockingQueueContractSupport#FILL_LIMIT] places
/// ([BlockingQueueContractSupport#fullInstance]).
///
/// @param <Q> the blocking queue type being tested
/// @param <E> the element type being tested
/// @see BlockingQueue#offer(Object)
/// @since 0.1.2
public interface OfferContract<Q extends BlockingQueue<E>, E> extends BlockingQueueContractSupport<Q, E> {

    /// Tests that `offer` on a full queue returns `false` and does not add the element.
    @DisplayName("offer(E) on a full queue returns false")
    @Test
    default void offerWhenFullReturnsFalse() {
        if (supportsMethod(QueueMethods.OFFER)) {
            Optional<Q> full = fullInstance();
            if (full.isEmpty()) {
                return;
            }
            Q queue = full.get();
            int size = queue.size();
            E extra = elementProvider().createInstanceNotIn(new ArrayList<>(queue));
            assertFalse(queue.offer(extra), "offer(E) returned true for a full queue");
            assertFalse(queue.contains(extra), "offer(E) added the element to a full queue");
            assertEquals(size, queue.size());
        }
    }
}
