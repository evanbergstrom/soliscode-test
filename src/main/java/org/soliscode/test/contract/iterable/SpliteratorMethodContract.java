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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;

import static java.util.Spliterator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.soliscode.test.assertions.Assertions.assertLessThan;
import static org.soliscode.test.assertions.collection.CollectionAssertions.*;
import static org.soliscode.test.util.IterableTestUtils.contains;
import static org.soliscode.test.util.IterableTestUtils.size;

/// **Contract for the `spliterator` method of an `Iterable`**
///
/// This interface defines tests for the [spliterator()][Iterable#spliterator] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Iterable] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that an iterable's `spliterator` implementation correctly:
/// - Returns a [Spliterator] that satisfies the [Spliterator] contract.
/// - Correctly reports characteristics.
/// - Handles empty collections and non-empty collections.
/// - Correctly implements `estimateSize`, `getExactSizeIfKnown`, `tryAdvance`, `forEachRemaining`, and `trySplit`.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyIterableSpliteratorTest implements SpliteratorMethodContract<String, MyIterable<String>> {
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
/// @see Iterable#spliterator
/// @see Spliterator
/// @since 1.0.0
@SuppressWarnings("MagicConstant")
public interface SpliteratorMethodContract<E, I extends Iterable<E>> extends IteratorMethodContract<E, I> {

    /// The full list of supported Spliterator characteristics.
    int[] CHARACTERISTIC_VALUES = {SIZED, SUBSIZED, SORTED, CONCURRENT,  DISTINCT, IMMUTABLE, NONNULL};

    /// Tests that the [Spliterator#hasCharacteristics(int)] method works.
    ///
    /// This test verifies that `hasCharacteristics` returns results consistent with `characteristics()`.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("hasCharacteristics(int) returns results consistent with characteristics()")
    default void hasCharacteristics_whenCalled_isConsistentWithCharacteristics() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();

        for (int mask : CHARACTERISTIC_VALUES) {
            boolean hasCharacteristic = (iterator.characteristics() & mask) != 0;
            assertEquals(hasCharacteristic, iterator.hasCharacteristics(mask));
        }
    }

    /// Tests that the [Spliterator#getExactSizeIfKnown] method works on an empty collection.
    ///
    /// This test verifies that `getExactSizeIfKnown()` returns 0 if SIZED, else -1.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("getExactSizeIfKnown() returns 0 for empty sized spliterator")
    default void getExactSizeIfKnown_whenEmpty_returnsExpectedValue() {
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
    ///
    /// This test verifies that `getExactSizeIfKnown()` returns the correct size if SIZED, else -1.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("getExactSizeIfKnown() returns correct size for non-empty sized spliterator")
    default void getExactSizeIfKnown_whenNotEmpty_returnsExpectedValue() {
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
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("estimateSize() returns 0 for empty sized spliterator")
    default void estimateSize_whenEmpty_returnsExpectedValue() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();
        long size = iterator.estimateSize();

        if (iterator.hasCharacteristics(Spliterator.SIZED) || iterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            assertEquals(0, size);
        }
    }

    /// Tests that the [Spliterator#estimateSize] method works on a collection with elements.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("estimateSize() returns correct size for non-empty sized spliterator")
    default void estimateSize_whenNotEmpty_returnsExpectedValue() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        Spliterator<E> iterator = iterable.spliterator();
        long size = iterator.estimateSize();

        if (iterator.hasCharacteristics(Spliterator.SIZED) || iterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            assertEquals(size(iterable), size);
        } else {
            Spliterator<E> split = iterator.trySplit();
            if (split != null) {
                long splitSize = iterator.estimateSize();
                assertLessThan(splitSize, size);
            }
        }
    }

    /// Tests that the [Spliterator#forEachRemaining] method works on an empty collection.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("forEachRemaining(Consumer) does not call action for empty spliterator")
    default void forEachRemaining_whenEmpty_doesNotCallAction() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();

        Collection<E> elements = new ArrayList<>();
        iterator.forEachRemaining(elements::add);
        assertIsEmpty(elements);
    }

    /// Tests that the [Spliterator#forEachRemaining] method works on a collection with elements.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("forEachRemaining(Consumer) traverses all elements for non-empty spliterator")
    default void forEachRemaining_whenNotEmpty_traversesAllElements() {
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
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("tryAdvance(Consumer) returns false for empty spliterator")
    default void tryAdvance_whenEmpty_returnsFalse() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();

        Collection<E> elements = new ArrayList<>();
        assertFalse(iterator.tryAdvance(elements::add));
        assertIsEmpty(elements);
    }

    /// Tests that the [Spliterator#tryAdvance] method works on a collection with elements.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("tryAdvance(Consumer) traverses all elements for non-empty spliterator")
    default void tryAdvance_whenNotEmpty_traversesAllElements() {
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
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("trySplit() returns null for empty spliterator")
    default void trySplit_whenEmpty_returnsNull() {
        Iterable<E> iterable = provider().emptyInstance();
        Spliterator<E> iterator = iterable.spliterator();

        Spliterator<E> split = iterator.trySplit();
        assertNull(split);
    }

    /// Tests that the [Spliterator#trySplit] method works on a collection with elements.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("trySplit() returns non-null spliterator for non-empty spliterator")
    default void trySplit_whenNotEmpty_returnsNonNullSpliterator() {
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
        assertContainsSameByIdentity(iterable, elements);
    }
}
