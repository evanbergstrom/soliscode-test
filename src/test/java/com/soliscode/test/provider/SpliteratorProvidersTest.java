/*
 * Copyright 2026 Evan Bergstrom
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
package com.soliscode.test.provider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for [SpliteratorProviders] (#38).
@DisplayName("Tests for SpliteratorProviders")
public class SpliteratorProvidersTest {

    /// Default constructor.
    public SpliteratorProvidersTest() {
    }

    private static <E> List<E> traverse(final Spliterator<E> spliterator) {
        List<E> elements = new ArrayList<>();
        spliterator.forEachRemaining(elements::add);
        return elements;
    }

    @Test
    @DisplayName("arrayList() makes sources of an ArrayList, ordered, sized and modifiable")
    public void arrayListSource() {
        SpliteratorSource<Spliterator<Integer>, Integer> source = SpliteratorProviders.<Integer>arrayList()
                .source(List.of(3, 1, 2));
        assertEquals(List.of(3, 1, 2), source.elements());
        assertEquals(List.of(3, 1, 2), traverse(source.spliterator()));
        assertEquals(Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED, source.characteristics());
        assertNull(source.comparator());
        assertTrue(source.removeOne());
        assertEquals(List.of(3, 1), source.elements());
    }

    @Test
    @DisplayName("treeSet() keeps the set's order, and comparator() is the set's: null in natural order")
    public void treeSetSources() {
        SpliteratorSource<Spliterator<Integer>, Integer> natural = SpliteratorProviders.<Integer>treeSet()
                .source(List.of(3, 1, 2));
        assertEquals(List.of(1, 2, 3), natural.elements());
        assertNull(natural.comparator());
        assertTrue((natural.characteristics() & Spliterator.SORTED) != 0);
        Comparator<Integer> reverse = Comparator.reverseOrder();
        SpliteratorSource<Spliterator<Integer>, Integer> reversed = SpliteratorProviders.treeSet(reverse)
                .source(List.of(3, 1, 2));
        assertEquals(List.of(3, 2, 1), reversed.elements());
        assertSame(reverse, reversed.comparator());
        assertSame(reverse, reversed.spliterator().getComparator());
    }

    @Test
    @DisplayName("fromCollection() of an unmodifiable collection does not remove")
    public void unmodifiableCollectionSourceDoesNotRemove() {
        SpliteratorSource<Spliterator<Integer>, Integer> source = SpliteratorProviders.<Integer>fromCollection(
                List::copyOf, 0, false).source(List.of(1, 2));
        assertFalse(source.removeOne());
        assertEquals(List.of(1, 2), source.elements());
    }

    @Test
    @DisplayName("intRange() and longRange() hold consecutive ascending values, and refuse others")
    public void rangeSources() {
        SpliteratorSource<Spliterator.OfInt, Integer> ints = SpliteratorProviders.intRange().source(List.of(4, 5, 6));
        assertEquals(List.of(4, 5, 6), traverse(ints.spliterator()));
        assertEquals(Spliterator.ORDERED, ints.characteristics());
        assertFalse(ints.removeOne());
        assertEquals(List.of(), traverse(SpliteratorProviders.intRange().source(List.of()).spliterator()));
        assertThrows(IllegalArgumentException.class, () -> SpliteratorProviders.intRange().source(List.of(1, 3)));
        assertEquals(List.of(Integer.MAX_VALUE - 1, Integer.MAX_VALUE), traverse(SpliteratorProviders.intRange()
                .source(List.of(Integer.MAX_VALUE - 1, Integer.MAX_VALUE)).spliterator()));
        assertEquals(List.of(7L, 8L), traverse(SpliteratorProviders.longRange().source(List.of(7L, 8L))
                .spliterator()));
        assertThrows(IllegalArgumentException.class, () -> SpliteratorProviders.longRange().source(List.of(2L, 1L)));
    }

    @Test
    @DisplayName("the array providers hold any values, and document what Arrays.spliterator does")
    public void arraySources() {
        int documented = Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED | Spliterator.IMMUTABLE;
        SpliteratorSource<Spliterator.OfInt, Integer> ints = SpliteratorProviders.intArray().source(List.of(5, 1));
        assertEquals(List.of(5, 1), traverse(ints.spliterator()));
        assertEquals(documented, ints.characteristics());
        assertEquals(List.of(9L, 2L), traverse(SpliteratorProviders.longArray().source(List.of(9L, 2L))
                .spliterator()));
        SpliteratorSource<Spliterator.OfDouble, Double> doubles = SpliteratorProviders.doubleArray()
                .source(List.of(0.5, -1.0));
        assertEquals(List.of(0.5, -1.0), traverse(doubles.spliterator()));
        assertFalse(doubles.removeOne());
    }
}
