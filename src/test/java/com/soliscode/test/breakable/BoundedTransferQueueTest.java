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
package com.soliscode.test.breakable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.contract.transferqueue.ThreadSafeTransferQueueContract;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests of [BoundedTransferQueue], the store of a bounded or fair [BreakableTransferQueue]: it is the correct
/// implementation the full-queue tests of `TransferQueueContract` run on, since the JDK has no bounded
/// `TransferQueue`. Each configuration runs `ThreadSafeTransferQueueContract`, which extends `TransferQueueContract`.
@DisplayName("BoundedTransferQueue")
public class BoundedTransferQueueTest {

    /// The capacity of the queues under test.
    private static final int CAPACITY = 100;

    /// Default constructor.
    public BoundedTransferQueueTest() {
        super();
    }

    private static CollectionProvider<BoundedTransferQueue<Integer>, Integer> provider(final boolean fair) {
        return CollectionProviders.from(() -> new BoundedTransferQueue<>(CAPACITY, fair, List.of()),
                BoundedTransferQueue::copy, c -> new BoundedTransferQueue<>(CAPACITY, fair, c),
                WithIntegerElement.PROVIDER);
    }

    /// Tests that the constructor refuses a capacity below 1, and more elements than the capacity.
    @Test
    @DisplayName("The constructor refuses a capacity below 1, and more elements than the capacity")
    public void constructorWithBadArgumentsThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new BoundedTransferQueue<>(0, false, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new BoundedTransferQueue<>(1, false, List.of(1, 2)));
    }

    /// Tests that a queue survives serialization with its capacity and elements, in order.
    ///
    /// @throws Exception if serialization fails
    @Test
    @DisplayName("Serialization keeps the capacity and the elements")
    public void serializationWhenRoundTrippedKeepsCapacityAndElements() throws Exception {
        BoundedTransferQueue<Integer> queue = new BoundedTransferQueue<>(5, true, List.of(3, 1, 2));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(queue);
        }
        Object read;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            read = in.readObject();
        }
        BoundedTransferQueue<?> copy = (BoundedTransferQueue<?>) read;
        assertEquals(List.of(3, 1, 2), new ArrayList<>(copy));
        assertEquals(2, copy.remainingCapacity());
    }

    /// Tests that a `drainTo` whose target refuses an element partway still wakes a producer waiting for room: the
    /// elements moved before the refusal have left the queue.
    ///
    /// @throws Exception if the waiting producer does not finish
    @Test
    @DisplayName("drainTo(Collection) wakes a waiting producer even when the target refuses an element")
    public void drainToWhenTargetThrowsWakesWaitingProducer() throws Exception {
        BoundedTransferQueue<Integer> queue = new BoundedTransferQueue<>(2, false, List.of(1, 2));
        Thread producer = new Thread(() -> {
            try {
                queue.put(3);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "bounded-transfer-queue-test");
        producer.setDaemon(true);
        producer.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (producer.getState() != Thread.State.WAITING && System.nanoTime() - deadline < 0) {
            Thread.onSpinWait();
        }
        assertThrows(IllegalStateException.class, () -> queue.drainTo(new ArrayBlockingQueue<>(1)));
        producer.join(TimeUnit.SECONDS.toMillis(5));
        assertEquals(Thread.State.TERMINATED, producer.getState(), "the producer waiting for room was not woken");
        assertEquals(List.of(2, 3), new ArrayList<>(queue));
    }

    /// The contracts on a bounded queue whose lock is not fair.
    @Nested
    @DisplayName("of capacity 100, not fair")
    public class NotFair extends AbstractTest
            implements ThreadSafeTransferQueueContract<BoundedTransferQueue<Integer>, Integer>, WithIntegerElement {

        /// Default constructor.
        public NotFair() {
            super();
        }

        @Override
        public CollectionProvider<BoundedTransferQueue<Integer>, Integer> provider() {
            return BoundedTransferQueueTest.provider(false);
        }
    }

    /// The contracts on a bounded queue whose lock is fair.
    @Nested
    @DisplayName("of capacity 100, fair")
    public class Fair extends AbstractTest
            implements ThreadSafeTransferQueueContract<BoundedTransferQueue<Integer>, Integer>, WithIntegerElement {

        /// Default constructor.
        public Fair() {
            super();
        }

        @Override
        public CollectionProvider<BoundedTransferQueue<Integer>, Integer> provider() {
            return BoundedTransferQueueTest.provider(true);
        }
    }
}
