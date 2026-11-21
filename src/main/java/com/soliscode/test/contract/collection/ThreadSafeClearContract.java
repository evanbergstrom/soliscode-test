/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.collection;

import java.util.Collection;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.Slow;
import com.soliscode.test.methods.CollectionMethods;
import com.soliscode.test.methods.IterableMethods;
import com.soliscode.test.safety.ContentionCoordinator;

import static com.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;

/// **Contract for thread-safe `clear` operations on collections**
///
/// This interface defines tests for the [clear][Collection#clear] method under concurrent access.
/// It is designed to be used as a mix-in interface by test classes that verify thread-safe
/// [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection's `clear` implementation correctly
/// handles concurrent access from multiple threads, specifically:
/// - Concurrent `clear` operations.
/// - `clear` operations concurrent with `add`.
/// - `clear` concurrent with `forEach`.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyThreadSafeCollectionClearTest implements ThreadSafeClearContract<Integer, MyThreadSafeCollection<Integer>> {
///     @Override
///     public CollectionProvider<Integer, MyThreadSafeCollection<Integer>> objectProvider() {
///         return MyThreadSafeCollection::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface focuses on testing the thread safety of the [Collection] implementation.
/// The tests use [ContentionCoordinator] to simulate concurrent access.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection#clear
/// @since 1.0.0
public interface ThreadSafeClearContract<C extends Collection<E>, E>
        extends ThreadSafeCollectionContractSupport<C, E> {

    /// How many elements ({@value}) the clear-under-contention test puts back before each clear.
    int CLEAR_BATCH = 1_000;

    /// How many times ({@value}) each thread adds the batch, in the clear-under-contention test: few, since each adds
    /// [#CLEAR_BATCH] elements.
    int CLEAR_ADD_ALL_ITERATIONS = 300;

    /// How many elements ({@value}) the clear-during-forEach test puts in the collection.
    int CLEAR_FOR_EACH_SIZE = 100;

    /// How long ({@value} ms), in the clear-during-forEach test, the reader waits for the other thread to call
    /// `clear()`, and that thread for the reader to reach its first element. The waits of a run, at most twice this
    /// and [#CLEAR_RETURN_WAIT_MILLIS] one after another, end inside the coordinator's 5 s finish timeout.
    long CLEAR_CALL_WAIT_MILLIS = 2_000;

    /// How long ({@value} ms), in the clear-during-forEach test, the reader holds `forEach` open once `clear()` has
    /// been called, if `clear()` neither returns nor waits: a `clear` that spins on a lock `forEach` holds.
    long CLEAR_RETURN_WAIT_MILLIS = 1_000;

    /// Tests that the [clear][Collection#clear] method is thread-safe when called under contention.
    ///
    /// This test verifies that if multiple threads call `clear` simultaneously, the collection
    /// is correctly emptied.
    ///
    /// @since 1.0.0
    @DisplayName("clear() is thread safe on collection with elements")
    @Slow
    @ConcurrencyTest  // repeat to shake out flaky races
    default void clearWhenCalledUnderContentionIsThreadSafe() {
        C collection = provider().createSingleton(elementProvider().createInstance());
        if (supportsMethod(CollectionMethods.CLEAR)) {
            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add(Collection::clear);
            coordinator.execute(collection);

            assertIsEmpty(collection);
            assertCollectionStillWorks(collection);
        }
    }

    /// Tests that the [clear][Collection#clear] method is thread-safe when called under contention with `add`.
    ///
    /// This test verifies that concurrent `clear` and `add` operations do not leave the collection
    /// in an inconsistent state.
    ///
    /// @since 1.0.0
    @DisplayName("clear() is thread safe under contention with add(Object)")
    @Slow
    @ConcurrencyTest  // repeat to shake out flaky races
    default void clearWhenCalledUnderContentionWithAddIsThreadSafe() {
        C collection = provider().createSingleton(elementProvider().createInstance());
        if (supportsMethod(CollectionMethods.CLEAR)) {
            final Supplier<E> uniqueSupplier = elementProvider().uniqueInstanceSupplier();

            // Other threads add elements, one at a time and in batches. A clear that is not thread safe can run in the
            // middle of an addAll, which then counts the slots the clear emptied: nulls in the collection.
            java.util.List<E> batch = elementProvider().createUniqueInstances(CLEAR_BATCH);
            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add(Collection::clear);
            coordinator.add((c) -> c.add(uniqueSupplier.get()));
            coordinator.add(0, CLEAR_ADD_ALL_ITERATIONS, (c) -> c.addAll(batch));
            coordinator.execute(collection);

            // A clear that races with add can leave the size and the elements out of step (nulls, lost counts).
            assertConsistent(collection);
            collection.clear();
            assertIsEmpty(collection);
            assertCollectionStillWorks(collection);
        }
    }

    /// Tests that [clear][Collection#clear], called while [forEach][Iterable#forEach] is traversing the collection,
    /// does not disturb the traversal.
    ///
    /// One thread calls `forEach` on a collection of [#CLEAR_FOR_EACH_SIZE] elements. On its first element the action
    /// waits until another thread has called `clear()` and that call has either returned or is waiting (for a monitor
    /// or a lock), and then lets the traversal go on. The test fails if `forEach` throws (a
    /// [java.util.ConcurrentModificationException], from a `clear` that ran inside it), or gives its action a `null`,
    /// an element the collection never held, or an element twice; if `forEach` never calls its action, or either
    /// thread waits for the other longer than [#CLEAR_CALL_WAIT_MILLIS]; and, afterwards, if the collection is not
    /// empty.
    ///
    /// [Collection] says nothing about concurrency, so this is the contract's own reading of a thread-safe
    /// collection, stricter than `Collection`'s documentation. The synchronized wrappers meet it: the
    /// [java.util.Collections#synchronizedCollection(Collection)] documentation asks callers to synchronize by hand
    /// only to traverse by Iterator, Spliterator or Stream, and its `forEach` and `clear` hold the same mutex, so
    /// `clear` waits for `forEach`. So do the weakly consistent concurrent collections
    /// ([java.util.concurrent.ConcurrentLinkedQueue], [java.util.concurrent.CopyOnWriteArrayList]), whose `clear`
    /// returns while `forEach` goes on and whose `forEach` never throws for it. A collection that inherits
    /// [Iterable#forEach] over a fail-fast iterator its callers must lock by hand fails it.
    ///
    /// The reader takes the other thread's [Thread#getState()] only as a hint for when to let go (`BLOCKED`: waiting
    /// for a monitor; `WAITING` or `TIMED_WAITING`: parked, for a lock): a wrong hint lets the reader go early, so
    /// that `clear` runs after `forEach`, which can let a faulty `clear` pass a run but never fails a correct one. A
    /// `clear` that neither returns nor waits is given [#CLEAR_RETURN_WAIT_MILLIS] and then waits for `forEach`.
    ///
    /// @since 0.1.6
    @DisplayName("clear() called during forEach(Consumer) does not disturb it")
    @Slow
    @ConcurrencyTest  // repeat to shake out flaky races
    default void clearWhenCalledDuringForEachDoesNotDisturbIt() {
        if (supportsMethod(CollectionMethods.CLEAR) && supportsMethod(IterableMethods.FOR_EACH)
                && supportsMethod(CollectionMethods.ADD_ALL)) {
            java.util.List<E> elements = elementProvider().createUniqueInstances(CLEAR_FOR_EACH_SIZE);
            C collection = provider().createInstance(elements);
            collection.clear();  // Load and compile what clear() runs before the race
            collection.addAll(elements);

            ClearDuringForEach<E> run = new ClearDuringForEach<>(elements, CLEAR_CALL_WAIT_MILLIS,
                    CLEAR_RETURN_WAIT_MILLIS);
            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add(1, 1, run::read);
            coordinator.add(1, 1, run::clear);
            coordinator.execute(collection);

            run.assertUndisturbed();
            assertIsEmpty(collection);
            assertConsistent(collection);
            assertCollectionStillWorks(collection);
        }
    }
}
