/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.transferqueue;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.provider.IntegerProvider;
import com.soliscode.test.util.RecordingSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of [HandOffRun]'s own paths: a whole run, a budget that runs out, and a producer that throws (#54).
@DisplayName("HandOffRun Tests")
class HandOffRunTest extends AbstractTest {

    private static final int PRODUCERS = 2;
    private static final int CONSUMERS = 2;
    private static final int ITERATIONS = 200;
    private static final int YIELD_INTERVAL = 4;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 9;

    /// Default constructor.
    HandOffRunTest() {
        super();
    }

    @Test
    @DisplayName("a run within its budget makes every hand-off, and the consumers take every element once")
    void runWithinItsBudgetMakesEveryHandOff() {
        LinkedTransferQueue<Integer> queue = new LinkedTransferQueue<>();
        RecordingSupplier<Integer> supplier = new IntegerProvider().uniqueInstanceSupplier();
        HandOffRun<LinkedTransferQueue<Integer>, Integer> run = new HandOffRun<LinkedTransferQueue<Integer>, Integer>(
                PRODUCERS, CONSUMERS, ITERATIONS, YIELD_INTERVAL, SHUTDOWN_TIMEOUT_SECONDS, 60_000)
                .run(queue, supplier, LinkedTransferQueue::transfer);
        assertEquals(PRODUCERS * ITERATIONS, run.handedOff(), "the hand-offs");
        assertEquals(0, run.leftEnqueued(), "the elements left enqueued after a hand-off");
        assertEquals(0, queue.size(), "the elements left in the queue");
        assertEquals(PRODUCERS * ITERATIONS + CONSUMERS, run.taken().size(), "the elements taken, sentinels included");
        assertEquals(List.copyOf(supplier.recorded()).stream().sorted().toList(),
                run.taken().stream().sorted().toList(), "the elements taken");
    }

    @Test
    @DisplayName("a budget that has run out stops the producers, and the consumers still take every element")
    void budgetThatHasRunOutStopsTheProducers() {
        LinkedTransferQueue<Integer> queue = new LinkedTransferQueue<>();
        RecordingSupplier<Integer> supplier = new IntegerProvider().uniqueInstanceSupplier();
        HandOffRun<LinkedTransferQueue<Integer>, Integer> run = new HandOffRun<LinkedTransferQueue<Integer>, Integer>(
                PRODUCERS, CONSUMERS, ITERATIONS, YIELD_INTERVAL, SHUTDOWN_TIMEOUT_SECONDS, 0)
                .run(queue, supplier, LinkedTransferQueue::transfer);
        // With no budget, the producer whose call sets the deadline makes its hand-off, and so may one that read the
        // time before the deadline was set; none makes a second.
        assertTrue(run.handedOff() >= 1 && run.handedOff() <= PRODUCERS, () -> "the hand-offs: " + run.handedOff());
        assertEquals(0, queue.size(), "the elements left in the queue");
        assertEquals(List.copyOf(supplier.recorded()).stream().sorted().toList(),
                run.taken().stream().sorted().toList(), "the elements taken");
    }

    @Test
    @DisplayName("a producer that throws ends the run, which reports its exception rather than timing out")
    void producerThatThrowsEndsTheRun() {
        IllegalStateException thrown = new IllegalStateException("the 10th transfer");
        AtomicInteger transfers = new AtomicInteger();
        LinkedTransferQueue<Integer> queue = new LinkedTransferQueue<>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void transfer(final Integer e) throws InterruptedException {
                if (transfers.incrementAndGet() == 10) {
                    throw thrown;
                }
                super.transfer(e);
            }
        };
        RecordingSupplier<Integer> supplier = new IntegerProvider().uniqueInstanceSupplier();
        HandOffRun<LinkedTransferQueue<Integer>, Integer> run = new HandOffRun<LinkedTransferQueue<Integer>, Integer>(
                PRODUCERS, CONSUMERS, ITERATIONS, YIELD_INTERVAL, SHUTDOWN_TIMEOUT_SECONDS, 60_000);
        AssertionError error = assertThrows(AssertionError.class,
                () -> run.run(queue, supplier, LinkedTransferQueue::transfer));
        assertTrue(error.getMessage().startsWith("Operations failed"),
                () -> "the run timed out, or failed otherwise, rather than reporting the exception: " + error);
        assertEquals(1, error.getSuppressed().length, "the exceptions reported");
        assertInstanceOf(IllegalStateException.class, error.getSuppressed()[0], "the exception reported");
        assertEquals(thrown, error.getSuppressed()[0], "the exception reported");
    }
}
