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
package com.soliscode.test.contract.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.safety.ContentionCoordinator;

/// Support for the contracts of [TransferQueue]: consumers known to be waiting, in the timed `poll`, while another
/// thread checks what the queue does with them, and a check repeated until it holds.
///
/// A consumer is known to be waiting once its thread is waiting ([#isWaiting]). A thread can be waiting before the
/// queue has registered it, for example on a lock, so a check that depends on the consumer repeats until it gives the
/// expected answer ([#spinUntil]). A correct queue fails only if a consumer is not registered within
/// [#CONSUMER_WAIT_MILLIS] of starting.
///
/// @param <Q> the transfer queue type being tested
/// @param <E> the element type being tested
/// @see com.soliscode.test.contract.transferqueue.TransferQueueContract
/// @since 0.1.2
public interface TransferQueueContractSupport<Q extends TransferQueue<E>, E>
        extends BlockingQueueContractSupport<Q, E> {

    /// How long ({@value} ms) a consumer of [#withWaitingConsumers] waits in the timed `poll`, and the longest a
    /// [#spinUntil] repeats. It is below the coordinator's shutdown timeout of [#BLOCKING_TIMEOUT_SECONDS], so that
    /// the consumers of a broken queue end by themselves.
    long CONSUMER_WAIT_MILLIS = 3_000;

    /// A check made while consumers wait.
    ///
    /// @param <Q> the queue type
    @FunctionalInterface
    interface WaitingCheck<Q> {
        /// Makes the check.
        ///
        /// @param queue the queue
        /// @param anyReturned whether any consumer has returned by itself, so that the check can stop repeating
        /// @throws Exception what the check throws
        void check(Q queue, BooleanSupplier anyReturned) throws Exception;
    }

    /// Runs `count` consumers, each in one timed `poll` of [#CONSUMER_WAIT_MILLIS], and one thread that waits until
    /// they are all waiting (at most [#SPIN_LIMIT_MILLIS]) and then makes the check. Afterwards it interrupts each
    /// consumer still waiting; a consumer interrupted receives nothing. Returns what each consumer received, `null`
    /// for nothing.
    ///
    /// The consumers are released by interrupt, not by an element, so that the queue holds nothing the test did not
    /// put in it. No interrupt outlives a consumer's action: the consumer marks itself done and clears its interrupt
    /// status, and the checking thread interrupts it only if it is not done, both holding the consumer's monitor.
    ///
    /// @param queue the queue
    /// @param count the number of consumers
    /// @param check the check
    /// @return what each consumer received, `null` for nothing
    default List<@Nullable E> withWaitingConsumers(final Q queue, final int count, final WaitingCheck<Q> check) {
        List<WaitingConsumer<E>> consumers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            consumers.add(new WaitingConsumer<>());
        }
        AtomicInteger next = new AtomicInteger();
        ContentionCoordinator<Q> coordinator = new ContentionCoordinator<>(BLOCKING_TIMEOUT_SECONDS,
                BLOCKING_TIMEOUT_SECONDS, ContentionCoordinator.DEFAULT_YIELD_INTERVAL);
        coordinator.add(count, 1, q -> {
            WaitingConsumer<E> consumer = consumers.get(next.getAndIncrement());
            consumer.start();
            @Nullable E received = null;
            try {
                received = q.poll(CONSUMER_WAIT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                // Released: it received nothing.
            } finally {
                consumer.finish(received);
            }
        });
        coordinator.add(1, 1, unchecked(q -> {
            try {
                long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SPIN_LIMIT_MILLIS);
                while (!consumers.stream().allMatch(c -> c.isDone() || isWaiting(c.thread()))
                        && System.nanoTime() - deadline < 0) {
                    Thread.onSpinWait();
                }
                check.check(q, () -> consumers.stream().anyMatch(WaitingConsumer::isDone));
            } finally {
                consumers.forEach(WaitingConsumer::release);
            }
        }));
        coordinator.execute(queue);
        List<@Nullable E> received = new ArrayList<>();
        consumers.forEach(c -> received.add(c.received()));
        return Collections.unmodifiableList(received);
    }

    /// Repeats a check until it holds, or `giveUp` holds, or [#CONSUMER_WAIT_MILLIS] passes, and returns its last
    /// result.
    ///
    /// @param condition the check
    /// @param giveUp when to stop early
    /// @return whether the check held
    default boolean spinUntil(final BooleanSupplier condition, final BooleanSupplier giveUp) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(CONSUMER_WAIT_MILLIS);
        while (true) {
            if (condition.getAsBoolean()) {
                return true;
            }
            if (giveUp.getAsBoolean() || System.nanoTime() - deadline >= 0) {
                return condition.getAsBoolean();
            }
            Thread.onSpinWait();
        }
    }
}
