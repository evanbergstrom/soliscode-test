/*
 * Copyright 2024 Evan Bergstrom
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

package org.soliscode.test.contract.iterable;

import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.util.IterableTestOps.contains;
import static org.soliscode.test.util.IterableTestOps.size;

/// This interface tests if a iterable class has implemented the `iterator()` method
/// correctly and that the iterator that is returned satisfies the [Iterator] contract.
///
/// @param <E> The element type being tested.
/// @param <I> The type of the iterator being tested.</I>
///
/// @author evanbergstrom
/// @see Collection#iterator
/// @see Iterator
/// @since 1.0.0
public interface IteratorContract<E, I extends Iterable<E>> extends CollectionContractSupport<E, I> {


    /// Tests that the [Iterator#hasNext] and [Iterator#next] methods work for an empty collection.
    @Test
    default void testIteratorOverEmptyCollection() {
        Iterator<E> iterator = provider().emptyInstance().iterator();
        assertFalse(iterator.hasNext());

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    /// Tests that the [Iterator#hasNext] and [Iterator#next] methods work for a collection with elements.
    @Test
    default void testIteratorOverCollectionWithElements() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        Iterator<E> iterator = iterable.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            E e = iterator.next();
            count = count + 1;
            assertTrue(contains(iterable, e));
        }
        assertEquals(size(iterable), count);

        assertThrows(NoSuchElementException.class, iterator::next);
    }


    /// Tests that the [Iterator#remove] method works.
    @Test
    default void testIteratorRemove() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        final Iterator<E> iterator = iterable.iterator();
        if (supportsMethod(CollectionMethods.IteratorRemove)) {
            while (iterator.hasNext()) {
                E e = iterator.next();
                assertTrue(contains(iterable, e));
                iterator.remove();
                assertFalse(contains(iterable, e));
            }
            assertEquals(0, size(iterable));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> {
                iterator.next();
                iterator.remove();
            });
        }
    }


    /// Tests that the [Iterator#remove] method throws and [IllegalStateException] if it is called
    /// without first calling `next`, or it is called twice in a row without a call to `next` in between.
    @Test
    default void testIteratorRemoveThrowsOnIllegalState() {
        if (supportsMethod(CollectionMethods.IteratorRemove)) {
            final Iterator<E> iterator = provider().createInstanceWithUniqueElements().iterator();
            assertThrows(IllegalStateException.class, iterator::remove);

            iterator.next();
            iterator.remove();

            assertThrows(IllegalStateException.class, iterator::remove);
        }
    }


    ///Tests that the [Iterator#forEachRemaining] method works over an entire collection.
    @Test
    default void testForEachRemainingOverEntireCollection() {
        final Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        final Iterator<E> iterator = iterable.iterator();
        final AtomicInteger count = new AtomicInteger();
        iterator.forEachRemaining((e) -> {
            count.incrementAndGet();
            assertTrue(contains(iterable, e));
        });
        assertEquals(size(iterable), count.get());
    }

    /// Tests that the [Iterator#forEachRemaining] method works over the remaining collection.
    @Test
    default void testForEachRemainingOverPartialCollection() {
        final Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        final Iterator<E> iterator = iterable.iterator();

        Collection<E> notRemaining = new HashSet<>();
        // Advance iterator to mid point
        int middle = size(iterable) / 2;
        for (int i = 0; i < middle; i++) {
            notRemaining.add(iterator.next());
        }

        final AtomicInteger count = new AtomicInteger();
        iterator.forEachRemaining((e) -> {
            count.incrementAndGet();
            assertTrue(contains(iterable, e));
            assertFalse(notRemaining.contains(e));
        });
        assertEquals(size(iterable) - middle, count.get());
    }

    /// Tests that the [Iterator#forEachRemaining[] method throws for null action.
    @Test
    default void testForEachRemainingForNullAction() {
        final Iterator<E> iterator = provider().createInstanceWithUniqueElements().iterator();

        assertThrows(NullPointerException.class, () -> iterator.forEachRemaining(null));
    }
}
