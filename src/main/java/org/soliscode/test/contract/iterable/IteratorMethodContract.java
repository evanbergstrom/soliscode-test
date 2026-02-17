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
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.util.IterableTestUtils.contains;
import static org.soliscode.test.util.IterableTestUtils.size;

/// **Contract for the `iterator` method of an `Iterable`**
///
/// This interface defines tests for the [iterator()][Iterable#iterator] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Iterable] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that an iterable's `iterator` implementation correctly:
/// - Returns an [Iterator] that traverses all elements.
/// - Handles empty collections.
/// - Correctly implements [Iterator#hasNext], [Iterator#next], [Iterator#remove], and [Iterator#forEachRemaining].
/// - Throws appropriate exceptions (e.g., [NoSuchElementException], [IllegalStateException]).
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyIterableIteratorTest implements IteratorMethodContract<String, MyIterable<String>> {
///     @Override
///     public CollectionProvider<String, MyIterable<String>> provider() {
///         return MyIterable::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Iterable] and [org.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <I> The type of the iterable being tested.
/// @author evanbergstrom
/// @see Iterable#iterator
/// @see Iterator
/// @since 1.0.0
public interface IteratorMethodContract<E, I extends Iterable<E>> extends CollectionContractSupport<E, I> {


    /// Tests that the [Iterator#hasNext] and [Iterator#next] methods work for an empty collection.
    ///
    /// This test verifies that calling `next()` on an iterator for an empty collection throws
    /// [NoSuchElementException].
    ///
    /// @since 1.0.0
    @Test
    default void iterator_whenEmpty_returnsEmptyIterator() {
        Iterator<E> iterator = provider().emptyInstance().iterator();
        assertFalse(iterator.hasNext());

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    /// Tests that the [Iterator#hasNext] and [Iterator#next] methods work for a collection with elements.
    ///
    /// This test verifies that the iterator traverses all elements in the collection and then
    /// throws [NoSuchElementException].
    ///
    /// @since 1.0.0
    @Test
    default void iterator_whenNotEmpty_traversesAllElements() {
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
    ///
    /// This test verifies that elements removed via the iterator are no longer present in
    /// the iterable.
    ///
    /// @since 1.0.0
    @Test
    default void iteratorRemove_whenSupported_removesElements() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        final Iterator<E> iterator = iterable.iterator();
        if (supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
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


    /// Tests that the [Iterator#remove] method throws an [IllegalStateException] if it is called
    /// without first calling `next`, or it is called twice in a row without a call to `next` in between.
    ///
    /// @since 1.0.0
    @Test
    default void iteratorRemove_whenCalledInvalidly_throwsIllegalStateException() {
        if (supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
            final Iterator<E> iterator = provider().createInstanceWithUniqueElements().iterator();
            assertThrows(IllegalStateException.class, iterator::remove);

            iterator.next();
            iterator.remove();

            assertThrows(IllegalStateException.class, iterator::remove);
        }
    }


    /// Tests that the [Iterator#forEachRemaining] method works over an entire collection.
    ///
    /// @since 1.0.0
    @Test
    default void forEachRemaining_whenCalledAtStart_traversesEntireCollection() {
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
    ///
    /// @since 1.0.0
    @Test
    default void forEachRemaining_whenCalledInMiddle_traversesRemainingElements() {
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

    /// Tests that the [Iterator#forEachRemaining] method throws for null action.
    ///
    /// @since 1.0.0
    @Test
    default void forEachRemaining_withNullAction_throwsNullPointerException() {
        final Iterator<E> iterator = provider().createInstanceWithUniqueElements().iterator();

        assertThrows(NullPointerException.class, () -> iterator.forEachRemaining(null));
    }
}
