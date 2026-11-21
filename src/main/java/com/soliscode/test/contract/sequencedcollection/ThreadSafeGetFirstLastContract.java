/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.sequencedcollection;

import java.util.NoSuchElementException;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.methods.SequencedCollectionMethods;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for `getFirst` and `getLast` of a thread-safe `SequencedCollection` under contention**
///
/// The collection starts with one element. Two writers, one at each end, loop: remove at their end, and, only if the
/// remove returned an element, add a fresh unique element at that end. So at most one writer is between its remove
/// and its add, and the collection holds no element or one: each remove takes the only element, which is the first
/// and the last. Readers call `getFirst` and `getLast`; each call must return an element that was added, never a
/// `null`, or throw [NoSuchElementException]. A read that is not thread safe can see the collection's size before a
/// remove and its element after it: a `null`.
///
/// The run is on [#contentionCoordinator()], in phases, which bounds it in time. Unlike
/// [com.soliscode.test.contract.collection.ThreadSafeReadContract]'s `contains` test, phases do not lower the rate
/// here: under load, an unlocked `getLast` was caught in 20 of 20 single runs in phases and in 16 of 20 without them
/// (measured on #22).
///
/// @param <C> the type of collection
/// @param <E> the type of elements
/// @see SequencedCollection#getFirst
/// @see SequencedCollection#getLast
/// @since 0.1.6
public interface ThreadSafeGetFirstLastContract<C extends SequencedCollection<E>, E>
        extends ThreadSafeSequencedCollectionContractSupport<C, E> {

    /// Tests that `getFirst` and `getLast` return an element that was added, or throw [NoSuchElementException], while
    /// writers at both ends remove the only element and add another.
    @DisplayName("getFirst() and getLast() are thread safe under contention with removes and adds at both ends")
    @VerySlow
    @ConcurrencyTest
    default void getFirstAndGetLastWhenCalledUnderContentionAreThreadSafe() {
        if (!supportsMethods(SequencedCollectionMethods.GET_FIRST, SequencedCollectionMethods.GET_LAST,
                SequencedCollectionMethods.ADD_FIRST, SequencedCollectionMethods.ADD_LAST,
                SequencedCollectionMethods.REMOVE_FIRST, SequencedCollectionMethods.REMOVE_LAST)) {
            return;
        }
        Supplier<E> supplier = elementProvider().uniqueInstanceSupplier();
        Set<E> added = ConcurrentHashMap.newKeySet();
        C collection = provider().emptyInstance();
        E only = supplier.get();
        added.add(only);
        collection.addLast(only);

        ContentionCoordinator<C> coordinator = contentionCoordinator();
        coordinator.add(1, ITERATIONS, c -> {
            try {
                c.removeFirst();
            } catch (final NoSuchElementException empty) {
                return;
            }
            E e = supplier.get();
            added.add(e);
            c.addFirst(e);
        });
        coordinator.add(1, ITERATIONS, c -> {
            try {
                c.removeLast();
            } catch (final NoSuchElementException empty) {
                return;
            }
            E e = supplier.get();
            added.add(e);
            c.addLast(e);
        });
        coordinator.add(THREADS, ITERATIONS, c -> checkRead(SequencedCollection::getFirst, c, added, "getFirst()"));
        coordinator.add(THREADS, ITERATIONS, c -> checkRead(SequencedCollection::getLast, c, added, "getLast()"));
        coordinator.execute(collection);
        assertConsistent(collection);
    }

    private static <C extends SequencedCollection<E>, E> void checkRead(final Function<C, E> read, final C collection,
                                                                        final Set<E> added, final String name) {
        E e;
        try {
            e = read.apply(collection);
        } catch (final NoSuchElementException empty) {
            return;
        }
        assertNotNull(e, name + " returned a null that was never added");
        assertTrue(added.contains(e), name + " returned an element that was never added");
    }
}
