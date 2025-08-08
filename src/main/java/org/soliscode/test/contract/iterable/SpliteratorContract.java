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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.Spliterator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.Assertions.assertLessThan;
import static org.soliscode.test.assertions.collection.CollectionAssertions.*;
import static org.soliscode.test.util.IterableTestOps.contains;
import static org.soliscode.test.util.IterableTestOps.size;

/// This interface tests if a collection class has implemented the [spliterator()][Iterable#spliterator] method
/// correctly and that the spliterator that is returned satisfies the [Spliterator] contract.
///
/// @param <E> The element type being tested.
/// @param <I> The type for the iterator being tested.
/// @author evanbergstrom
/// @see Collection#spliterator
/// @see Spliterator
/// @since 1.0.0
public interface SpliteratorContract<E, I extends Iterable<E>> extends IteratorContract<E, I> {

    /// The full list of supported Spliterator characteristics.
    int [] CHARACTERISTIC_VALUES = { SIZED, SUBSIZED, SORTED, CONCURRENT,  DISTINCT, IMMUTABLE, NONNULL };

    /// Tests that the [Spliterator#hasCharacteristics(int)] method works.
    @Test
    @DisplayName("The hasCharacteristics method works")
    default void testHasCharacteristic() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();

        for (int mask : CHARACTERISTIC_VALUES) {
            boolean hasCharacteristic = (iterator.characteristics() & mask) != 0;
            assertEquals(hasCharacteristic, iterator.hasCharacteristics(mask));
        }
    }

    /// Tests that the [Spliterator#getExactSizeIfKnown] method works on an empty collection.
    @Test
    @DisplayName("The getExactSizeIfKnown method can be called on an empty container")
    default void testSpliteratorGetExactSizeIfKnownOnAnEmptyCollection() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();
        long size = iterator.getExactSizeIfKnown();

        if (iterator.hasCharacteristics(Spliterator.SIZED)) {
            assertEquals(0, size);
        } else {
            assertEquals(-1, size);
        }
    }

    /// Tests that the [Spliterator#getExactSizeIfKnown] method works on a collection with elements.
    @Test
    @DisplayName("The getExactSizeIfKnown method can be called on a container with elements")
    default void testSpliteratorGetExactSizeIfKnownOnACollectionWithElements() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        Spliterator<E> iterator = iterable.spliterator();
        long size = iterator.getExactSizeIfKnown();

        if (iterator.hasCharacteristics(Spliterator.SIZED)) {
            assertEquals(size(iterable), size);
        } else {
            assertEquals(-1, size);
        }
    }

    /// Tests that the [Spliterator#estimateSize] method works on an empty collection.
    @Test
    @DisplayName("The estimateSize method can be called on an empty container")
    default void testSpliteratorEstimateSizeOnAnEmptyCollection() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();
        long size = iterator.estimateSize();

        if (iterator.hasCharacteristics(Spliterator.SIZED) || iterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            assertEquals(0, size);
        }
    }

    /// Tests that the [Spliterator#estimateSize] method works on a collection with elements.
    @Test
    @DisplayName("The Spliterator.estimateSize method can be called on a container with elements")
    default void testSpliteratorEstimateSizeOnACollectionWithElements() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        Spliterator<E> iterator = iterable.spliterator();
        long size = iterator.estimateSize();

        if (iterator.hasCharacteristics(Spliterator.SIZED) || iterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            assertEquals(size(iterable), size);
        } else {
            Spliterator<E> split = iterator.trySplit();
            if (split != null) {
                long splitSize = iterator.estimateSize();
                assertLessThan(size, splitSize);
            }
        }
    }

    /// Tests that the [Spliterator#forEachRemaining] method works on an empty collection.
    @Test
    @DisplayName("The Spliterator.forEachRemaining method can be called on an empty container")
    default void testSpliteratorForEachRemainingOnAnEmptyCollection() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();

        Collection<E> elements = new ArrayList<>();
        iterator.forEachRemaining(elements::add);
        assertIsEmpty(elements);
    }

    /// Tests that the [Spliterator#forEachRemaining] method works on a collection with elements.
    @Test
    @DisplayName("The Spliterator.forEachRemaining method can be called on a container with elements")
    default void testSpliteratorForEachRemainingOnACollectionWithElements() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        Spliterator<E> iterator = iterable.spliterator();

        List<E> elements = new ArrayList<>();
        iterator.forEachRemaining(elements::add);
            assertSameSize(iterable, elements);

        if (iterator.hasCharacteristics(Spliterator.ORDERED)) {
            assertIterableEquals(iterable, elements);
        } else {
            assertEqualsByIdentity(iterable, elements);
        }
    }

    /// Tests that the [Spliterator#tryAdvance] method works on an empty collection.
    @Test
    @DisplayName("The Spliterator.tryAdvance method can be called on an empty container")
    default void testSpliteratorTryAdvanceOnAnEmptyCollection() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();

        Collection<E> elements = new ArrayList<>();
        assertFalse(iterator.tryAdvance(elements::add));
        assertIsEmpty(elements);
    }

    /// Tests that the [Spliterator#tryAdvance] method works on a collection with elements.
    @Test
    @DisplayName("The Spliterator.tryAdvance method can be called on a container with elements")
    default void testSpliteratorTryAdvanceOnACollectionWithElements() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        Spliterator<E> iterator = iterable.spliterator();

        List<E> elements = new ArrayList<>();
        while (iterator.tryAdvance(elements::add)) {
            assertTrue(contains(iterable, elements.getLast()));
        }
        assertSameSize(iterable, elements);

        if (iterator.hasCharacteristics(Spliterator.ORDERED)) {
            assertIterableEquals(iterable, elements);
        } else {
            assertEqualsByIdentity(iterable, elements);
        }
    }

    /// Tests that the [Spliterator#trySplit] method works on an empty collection.
    @Test
    @DisplayName("The Spliterator.trySplit method can be called on an empty container")
    default void testSpliteratorTrySplitOnAnEmptyCollection() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();

        Spliterator<E> split = iterator.trySplit();
        assertNull(split);
    }

    /// Tests that the [Spliterator#trySplit] method works on a collection with elements.
    @Test
    @DisplayName("The Spliterator.trySplit method can be called on a container with elements")
    default void testSpliteratorTrySplitOnACollectionWithElements() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        Spliterator<E> iterator = iterable.spliterator();

        Spliterator<E> split = iterator.trySplit();
        assertNotNull(split);

        List<E> elements = new ArrayList<>();
        while (iterator.tryAdvance(elements::add)) {
            assertTrue(contains(iterable, elements.getLast()));
        }
        while (split.tryAdvance(elements::add)) {
            assertTrue(contains(iterable, elements.getLast()));
        }
        assertSameSize(iterable, elements);

        if (iterator.hasCharacteristics(Spliterator.ORDERED)) {
            assertContainsSameByIdentity(iterable, elements);
        } else {
            assertEqualsByIdentity(iterable, elements);
        }
    }
}
