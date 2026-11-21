/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.collection;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/// One run of [ThreadSafeClearContract#clearWhenCalledDuringForEachDoesNotDisturbIt]: a reader whose `forEach`
/// holds the traversal open, on its first element, until a clearer's `clear()` has returned or waits for it.
///
/// The reader and the clearer are the two operations of a
/// [com.soliscode.test.safety.ContentionCoordinator], one thread and one call each. The state they share is atomic:
/// the clearer sets [#clearer] before [#calling], so that a reader that sees `calling` also sees the thread, and the
/// reader's read of `returned` happens after the clearer's `clear()`, so that what `clear()` wrote (an `ArrayList`'s
/// `modCount`, say) is visible to the `forEach` that goes on after it.
///
/// [Thread#getState()] is only a hint here, for when the reader may let go: a clearer that shows
/// [Thread.State#BLOCKED] waits for a monitor, one that shows [Thread.State#WAITING] or
/// [Thread.State#TIMED_WAITING] is parked, for a lock in a thread-safe collection. A wrong hint can cost a catch
/// (the reader lets go early, and `clear()` runs after `forEach`), never a false failure.
///
/// @param <E> the element type
final class ClearDuringForEach<E> {

    private final Set<E> expected;
    private final Set<E> seen = ConcurrentHashMap.newKeySet();
    private final long callWaitNanos;
    private final long returnWaitNanos;
    private final CountDownLatch inForEach = new CountDownLatch(1);
    private final AtomicBoolean actionCalled = new AtomicBoolean();
    private final AtomicReference<@Nullable Thread> clearer = new AtomicReference<>();
    private final AtomicBoolean calling = new AtomicBoolean();
    private final AtomicBoolean returned = new AtomicBoolean();
    private final AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();

    /// Creates a run over a collection that holds `elements`.
    ///
    /// @param elements the elements the collection holds when the run starts
    /// @param callWaitMillis how long the reader waits for the clearer to call `clear()`, and the clearer for the
    ///                       reader to reach its first element
    /// @param returnWaitMillis how long the reader holds `forEach` open once `clear()` has been called, if the
    ///                         clearer neither returns nor waits
    ClearDuringForEach(final Collection<E> elements, final long callWaitMillis, final long returnWaitMillis) {
        this.expected = new HashSet<>(elements);
        this.callWaitNanos = TimeUnit.MILLISECONDS.toNanos(callWaitMillis);
        this.returnWaitNanos = TimeUnit.MILLISECONDS.toNanos(returnWaitMillis);
    }

    /// The reader: traverses the collection with `forEach`, and records what goes wrong.
    ///
    /// @param collection the collection under test
    void read(final Collection<E> collection) {
        try {
            collection.forEach(this::accept);
        } catch (final ConcurrentModificationException e) {
            failure.compareAndSet(null,
                    new AssertionError("clear() ran inside forEach(Consumer) without waiting for it to finish", e));
        } catch (final RuntimeException | AssertionError e) {
            failure.compareAndSet(null, e);
        } finally {
            inForEach.countDown();  // A forEach that never called its action must not leave the clearer waiting
        }
    }

    /// The clearer: waits until the reader is inside `forEach`, then calls `clear()`.
    ///
    /// @param collection the collection under test
    void clear(final Collection<E> collection) {
        try {
            if (!inForEach.await(callWaitNanos, TimeUnit.NANOSECONDS)) {
                failure.compareAndSet(null, new AssertionError("forEach(Consumer) did not reach its first element "
                        + "within " + TimeUnit.NANOSECONDS.toMillis(callWaitNanos) + " ms"));
                return;
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            failure.compareAndSet(null, new AssertionError("interrupted waiting for forEach(Consumer)", e));
            return;
        }
        clearer.set(Thread.currentThread());
        calling.set(true);
        collection.clear();
        returned.set(true);
    }

    /// Asserts, once both threads have finished, that the run found nothing wrong and that `forEach` called its
    /// action.
    void assertUndisturbed() {
        Throwable t = failure.get();
        if (t instanceof AssertionError e) {
            throw e;
        }
        if (t != null) {
            throw new AssertionError("forEach(Consumer) threw while clear() ran", t);
        }
        assertTrue(actionCalled.get(), "forEach(Consumer) never called its action");
    }

    private void accept(final @Nullable E element) {
        if (actionCalled.compareAndSet(false, true)) {
            inForEach.countDown();
            awaitCall();
            awaitClear();
        }
        assertNotNull(element, "forEach(Consumer) gave its action a null while clear() ran");
        assertTrue(expected.contains(element), () -> "forEach(Consumer) gave its action " + element
                + ", which the collection never held");
        assertTrue(seen.add(element), () -> "forEach(Consumer) gave its action " + element + " twice");
    }

    private void awaitCall() {
        long start = System.nanoTime();
        while (!calling.get()) {
            if (System.nanoTime() - start > callWaitNanos) {
                fail("the other thread did not call clear() within "
                        + TimeUnit.NANOSECONDS.toMillis(callWaitNanos) + " ms");
            }
            Thread.onSpinWait();
        }
    }

    private void awaitClear() {
        Thread thread = Objects.requireNonNull(clearer.get(), "clearer");  // Set before calling
        long start = System.nanoTime();
        while (!returned.get() && !isWaiting(thread) && System.nanoTime() - start < returnWaitNanos) {
            Thread.onSpinWait();
        }
    }

    private static boolean isWaiting(final Thread thread) {
        Thread.State state = thread.getState();
        return state == Thread.State.BLOCKED || state == Thread.State.WAITING
                || state == Thread.State.TIMED_WAITING;
    }
}
