/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.transferqueue;

import java.util.concurrent.TransferQueue;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.contract.support.TransferQueueContractSupport;
import com.soliscode.test.methods.BlockingQueueMethods;
import com.soliscode.test.methods.TransferQueueMethods;
import com.soliscode.test.util.RecordingSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for `tryTransfer(E)` of a `TransferQueue` under contention**
///
/// A `TransferQueue` is a [java.util.concurrent.BlockingQueue], whose "queuing methods achieve their effects
/// atomically". Producers `tryTransfer` unique elements, and `transfer` each one `tryTransfer` refused; consumers
/// `take` as many. After either call returns, its element must have been received, so it is no longer in the queue:
/// `tryTransfer` hands over only to a consumer "already waiting", and otherwise does not enqueue. Every element must
/// be taken exactly once, and the queue left empty.
///
/// A producer that finds its element still enqueued counts it and carries on; the count must be 0. The producers
/// start no hand-off after [#HAND_OFF_BUDGET_MILLIS], so that a run under load ends in time however slowly the
/// hand-offs go; once they have finished, one sentinel element per consumer is put, and each consumer takes until it
/// has taken one, so that the consumers take whatever is enqueued, in any split. The test runs on a
/// [com.soliscode.test.safety.ContentionCoordinator], which bounds it in time.
///
/// @param <Q> the transfer queue type being tested
/// @param <E> the element type being tested
/// @see TransferQueue#tryTransfer(Object)
/// @see TransferQueue#take()
/// @since 0.1.2
public interface ThreadSafeTryTransferContract<Q extends TransferQueue<E>, E>
        extends TransferQueueContractSupport<Q, E> {

    /// The number of producers ({@value}), twice the number of consumers: the more producers race for one waiting
    /// consumer, the likelier a check-then-act hand-off shows (measured on #34).
    int PRODUCERS = 4;

    /// The number of consumers ({@value}); together they take every element the producers hand off, and a sentinel
    /// each.
    int CONSUMERS = 2;

    /// The most times ({@value}) each producer calls the queue, within [#HAND_OFF_BUDGET_MILLIS]. Each hand-off
    /// waits for a consumer to take its element, so the calls run one hand-off at a time (measured on #34).
    int ITERATIONS = 5_000;

    /// How long ({@value} ms) the producers hand off, from the first hand-off. Idle, a run makes all its
    /// [#ITERATIONS] well within it; under load the hand-offs, each waiting on the scheduler, slow down, and the
    /// producers start no hand-off once it has passed, leaving the rest of [#SHUTDOWN_TIMEOUT_SECONDS] for the last
    /// hand-offs and the sentinels (measured on #54).
    long HAND_OFF_BUDGET_MILLIS = 6_000;

    /// How long ({@value} s) the coordinator waits for the producers and consumers to finish, longer than its default
    /// of 5 s, since the calls run one hand-off at a time, and below the 10 s
    /// [com.soliscode.test.annotations.TestTags#THREAD_SAFETY_TIMEOUT] that [ConcurrencyTest] gives the test under
    /// JUnit, so that the coordinator, not JUnit, reports a run that does not finish.
    int SHUTDOWN_TIMEOUT_SECONDS = 9;

    /// How many calls ({@value}) a producer or consumer makes between its yields to the scheduler. A check-then-act
    /// hand-off shows only when producers race over a consumer already waiting in `take()`, and frequent yields let
    /// the consumers get there: at the coordinator's default of 64 the test missed `tryTransfer`'s check-then-act in
    /// 3 of 20 coverage runs, and at 4 caught it in 20 of 20 single runs. A yield before every call caught it as
    /// well, but under load (18 busy processes on 18 cores) the fair store's runs then timed out in 10 of 120, against
    /// none at 4 (measured on #12).
    int YIELD_INTERVAL = 4;

    /// Tests that elements handed over by producers and taken by consumers, all at once, are each received before
    /// the producer's call returns, and taken exactly once.
    @DisplayName("tryTransfer(E) and take() are thread safe under contention with each other")
    @VerySlow
    @ConcurrencyTest
    default void tryTransferAndTakeWhenCalledUnderContentionAreThreadSafe() {
        if (!supportsMethods(TransferQueueMethods.TRY_TRANSFER, TransferQueueMethods.TRANSFER,
                BlockingQueueMethods.TAKE)) {
            return;
        }
        Q queue = provider().emptyInstance();
        RecordingSupplier<E> supplier = elementProvider().uniqueInstanceSupplier();
        HandOffRun<Q, E> run = new HandOffRun<Q, E>(PRODUCERS, CONSUMERS, ITERATIONS, YIELD_INTERVAL,
                SHUTDOWN_TIMEOUT_SECONDS, HAND_OFF_BUDGET_MILLIS).run(queue, supplier, (q, e) -> {
                    if (!q.tryTransfer(e)) {
                        q.transfer(e);
                    }
                });
        assertTrue(run.handedOff() > 0, "no element was handed off");
        assertEquals(0, run.leftEnqueued(),
                "tryTransfer(E) or transfer(E) returned while its element was still enqueued, not received");
        assertEquals(0, queue.size(), "elements left in the queue after the consumers took their sentinels");
        assertSameElements(supplier.recorded(), run.taken(), "the elements taken");
    }
}
