/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.transferqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.safety.ContentionCoordinator;
import com.soliscode.test.util.RecordingSupplier;

/// One run of producers handing elements off to consumers through a transfer queue, all at once, within a time
/// budget, which [ThreadSafeTransferContract] and [ThreadSafeTryTransferContract] share.
///
/// Each producer and each consumer is one call of a [ContentionCoordinator] that loops by itself, yielding before its
/// first call to the queue and then once every `yieldInterval` calls. A producer makes up to `iterations` hand-offs,
/// and stops early once the budget has run out; the budget starts at the first producer's first call, so that
/// starting the threads does not use it, and that call always makes a hand-off. After each hand-off the producer
/// counts its element if it is still enqueued. The last producer to finish, whether it ended or threw, puts one
/// sentinel per consumer, made before the run by the same supplier. A consumer `take`s until it has taken a
/// sentinel, so the consumers take whatever was enqueued, in any split, and each stops at one.
///
/// On a correct queue, the run after the budget is at most the hand-off each producer is in, and the sentinels. The
/// coordinator's finish timeout still bounds a run that does not end.
///
/// @param <Q> the transfer queue type
/// @param <E> the element type
final class HandOffRun<Q extends TransferQueue<E>, E> {

    /// A producer's hand-off of one element.
    ///
    /// @param <Q> the queue type
    /// @param <E> the element type
    @FunctionalInterface
    interface HandOff<Q, E> {
        /// Hands an element off to a consumer.
        ///
        /// @param queue   the queue
        /// @param element the element
        /// @throws InterruptedException if interrupted while waiting
        void handOff(Q queue, E element) throws InterruptedException;
    }

    private final int producers;
    private final int consumers;
    private final int iterations;
    private final int yieldInterval;
    private final int shutdownTimeoutSeconds;
    private final long budgetNanos;
    private final Queue<E> taken = new ConcurrentLinkedQueue<>();
    private final AtomicInteger handedOff = new AtomicInteger();
    private final AtomicInteger leftEnqueued = new AtomicInteger();
    private final AtomicReference<@Nullable Long> deadline = new AtomicReference<>();

    /// Creates a run.
    ///
    /// @param producersArg              the number of producers
    /// @param consumersArg              the number of consumers
    /// @param iterationsArg             the most hand-offs each producer makes
    /// @param yieldIntervalArg          the number of calls between a thread's yields
    /// @param shutdownTimeoutSecondsArg how long the coordinator waits for the threads to finish
    /// @param budgetMillis              how long the producers hand off, from the first hand-off
    HandOffRun(final int producersArg, final int consumersArg, final int iterationsArg, final int yieldIntervalArg,
               final int shutdownTimeoutSecondsArg, final long budgetMillis) {
        this.producers = producersArg;
        this.consumers = consumersArg;
        this.iterations = iterationsArg;
        this.yieldInterval = yieldIntervalArg;
        this.shutdownTimeoutSeconds = shutdownTimeoutSecondsArg;
        this.budgetNanos = TimeUnit.MILLISECONDS.toNanos(budgetMillis);
    }

    /// Runs the producers and consumers on the queue, once.
    ///
    /// @param queue    the queue, empty
    /// @param supplier the supplier of unique elements, sentinels included
    /// @param handOff  a producer's hand-off of one element
    /// @return this run, with its results
    /// @throws AssertionError if the threads do not start or finish in time, or one of them throws
    HandOffRun<Q, E> run(final Q queue, final RecordingSupplier<E> supplier, final HandOff<Q, E> handOff) {
        List<E> made = new ArrayList<>();
        for (int i = 0; i < consumers; i++) {
            made.add(supplier.get());
        }
        Set<E> sentinels = Set.copyOf(made);
        AtomicInteger producersLeft = new AtomicInteger(producers);
        ContentionCoordinator<Q> coordinator = new ContentionCoordinator<>(
                ContentionCoordinator.DEFAULT_START_TIMEOUT_SECONDS, shutdownTimeoutSeconds, yieldInterval);
        coordinator.add(producers, 1, q -> {
            @Nullable Throwable failure = null;
            try {
                produce(q, supplier, handOff);
            } catch (final RuntimeException | Error e) {
                failure = e;
                throw e;
            } finally {
                if (producersLeft.decrementAndGet() == 0) {
                    putAll(q, made, failure);
                }
            }
        });
        coordinator.add(consumers, 1, q -> consume(q, sentinels));
        coordinator.execute(queue);
        return this;
    }

    /// The elements the consumers took, sentinels included.
    ///
    /// @return the elements taken
    Queue<E> taken() {
        return taken;
    }

    /// The number of hand-offs the producers made.
    ///
    /// @return the number of hand-offs
    int handedOff() {
        return handedOff.get();
    }

    /// The number of hand-offs after which the element was still enqueued.
    ///
    /// @return the number of elements found enqueued
    int leftEnqueued() {
        return leftEnqueued.get();
    }

    private void produce(final Q queue, final RecordingSupplier<E> supplier, final HandOff<Q, E> handOff) {
        try {
            for (int i = 0; i < iterations; i++) {
                if (i % yieldInterval == 0) {
                    yieldToScheduler();
                }
                // Strictly after: the call that sets the deadline reads the time first, so it always hands off.
                if (System.nanoTime() - deadline() > 0) {
                    return;
                }
                E e = supplier.get();
                handOff.handOff(queue, e);
                handedOff.incrementAndGet();
                if (queue.contains(e)) {
                    leftEnqueued.incrementAndGet();
                }
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("interrupted", e);
        }
    }

    private void consume(final Q queue, final Set<E> sentinels) {
        try {
            for (int i = 0;; i++) {
                if (i % yieldInterval == 0) {
                    yieldToScheduler();
                }
                E e = queue.take();
                taken.add(e);
                if (sentinels.contains(e)) {
                    return;
                }
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("interrupted", e);
        }
    }

    /// Returns the deadline, setting it on the first call: the budget starts at the first hand-off.
    private long deadline() {
        Long d = deadline.get();
        if (d == null) {
            deadline.compareAndSet(null, System.nanoTime() + budgetNanos);
            d = deadline.get();
        }
        return Objects.requireNonNull(d);
    }

    /// Puts the sentinels. If `put` throws, the exception is added to the producer's own, if it threw, so that
    /// neither is lost.
    private static <Q extends TransferQueue<E>, E> void putAll(final Q queue, final List<E> elements,
                                                               final @Nullable Throwable failure) {
        try {
            for (final E e : elements) {
                queue.put(e);
            }
        } catch (final InterruptedException e) {
            // The coordinator interrupted the workers: it is already reporting the timeout.
            Thread.currentThread().interrupt();
        } catch (final RuntimeException e) {
            if (failure == null) {
                throw e;
            }
            failure.addSuppressed(e);
        }
    }

    // ThreadPriorityCheck: yielding to the scheduler lets the consumers reach take(), so that producers race over one.
    @SuppressWarnings("ThreadPriorityCheck")
    private static void yieldToScheduler() {
        Thread.yield();
    }
}
