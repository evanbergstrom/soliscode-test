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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `IteratorProviders` and the sources it makes (#37).
@DisplayName("Tests for IteratorProviders")
public class IteratorProvidersTest {

    private static final List<Integer> ELEMENTS = List.of(3, 1, 2);

    /// Default constructor.
    public IteratorProvidersTest() {
        super();
    }

    @Test
    @DisplayName("a list's source is ordered, holds the elements, and its iterator returns them in order")
    void listSourceIsOrdered() {
        for (final IteratorProvider<Iterator<Integer>, Integer> provider
                : List.of(IteratorProviders.<Integer>arrayList(), IteratorProviders.<Integer>linkedList())) {
            IteratorSource<Iterator<Integer>, Integer> source = provider.source(ELEMENTS);
            assertTrue(source.ordered());
            assertEquals(ELEMENTS, source.elements());
            List<Integer> returned = new ArrayList<>();
            source.iterator().forEachRemaining(returned::add);
            assertEquals(ELEMENTS, returned);
        }
    }

    @Test
    @DisplayName("a list's source gives list iterators over one list, and elements() is a snapshot")
    void listIteratorSourceSharesOneList() {
        IteratorSource<ListIterator<Integer>, Integer> source =
                IteratorProviders.<Integer>listIteratorsOf(LinkedList::new).source(ELEMENTS);
        List<Integer> before = source.elements();
        ListIterator<Integer> iterator = source.iterator();
        iterator.next();
        iterator.set(7);
        iterator.add(8);
        assertEquals(List.of(7, 8, 1, 2), source.elements());
        assertEquals(ELEMENTS, before);
        assertEquals(7, source.iterator().next());
        assertEquals(List.of(3, 1, 2),
                IteratorProviders.<Integer>arrayListListIterator().source(ELEMENTS).elements());
        assertEquals(List.of(3, 1, 2),
                IteratorProviders.<Integer>linkedListListIterator().source(ELEMENTS).elements());
    }

    @Test
    @DisplayName("the provider does not change or keep the list it is given")
    void providerCopiesTheGivenList() {
        List<Integer> given = new ArrayList<>(ELEMENTS);
        IteratorSource<Iterator<Integer>, Integer> source = IteratorProviders.<Integer>arrayList().source(given);
        Iterator<Integer> iterator = source.iterator();
        iterator.next();
        iterator.remove();
        assertEquals(ELEMENTS, given, "the given list after a remove through the source");
        given.clear();
        assertEquals(List.of(1, 2), source.elements(), "the source after the given list is cleared");
    }

    @Test
    @DisplayName("a collection's source is unordered, and elements() shows what remove() did")
    void collectionSourceIsUnordered() {
        IteratorSource<Iterator<Integer>, Integer> source =
                IteratorProviders.<Integer>fromCollection(HashSet::new).source(ELEMENTS);
        assertFalse(source.ordered());
        assertEquals(ELEMENTS, source.elements());
        Iterator<Integer> iterator = source.iterator();
        Integer removed = iterator.next();
        iterator.remove();
        List<Integer> expected = new ArrayList<>(ELEMENTS);
        expected.remove(removed);
        assertEquals(expected, source.elements());
    }

    @Test
    @DisplayName("a collection's elements() fails if the collection holds elements it was not made from")
    void collectionSourceWithOtherElementsFails() {
        IteratorSource<Iterator<Integer>, Integer> source = IteratorProviders.<Integer>fromCollection(l -> {
            Set<Integer> set = new HashSet<>(l);
            set.add(99);
            return set;
        }).source(ELEMENTS);
        AssertionError error = assertThrows(AssertionError.class, source::elements);
        assertTrue(error.getMessage().contains("4"), error.getMessage());
    }

    @Test
    @DisplayName("the factories refuse a null factory")
    void factoriesRefuseNull() {
        assertThrows(NullPointerException.class, () -> IteratorProviders.fromList(null));
        assertThrows(NullPointerException.class, () -> IteratorProviders.listIteratorsOf(null));
        assertThrows(NullPointerException.class, () -> IteratorProviders.fromCollection(null));
    }

    @Test
    @DisplayName("a list's and a collection's sources change their source with remove and add")
    void sourcesChangeTheirSource() {
        IteratorSource<Iterator<Integer>, Integer> list =
                IteratorProviders.<Integer>arrayList().source(List.of(1, 2, 3));
        assertTrue(list.removeFromSource(2));
        assertTrue(list.addToSource(4));
        assertEquals(List.of(1, 3, 4), list.elements());

        IteratorSource<Iterator<Integer>, Integer> collection =
                IteratorProviders.<Integer>fromCollection(HashSet::new).source(List.of(1, 2, 3));
        assertTrue(collection.removeFromSource(2));
        assertTrue(collection.addToSource(4));
        assertFalse(collection.addToSource(4), "a set refuses a second copy");
        assertEquals(Set.of(1, 3, 4), new HashSet<>(collection.elements()));
    }

    @Test
    @DisplayName("a source that cannot be changed returns false from removeFromSource and addToSource")
    void unchangeableSourcesReturnFalse() {
        IteratorSource<Iterator<Integer>, Integer> fixed =
                IteratorProviders.<Integer>fromList(List::copyOf).source(List.of(1, 2, 3));
        assertFalse(fixed.removeFromSource(2));
        assertFalse(fixed.addToSource(4));
        assertEquals(List.of(1, 2, 3), fixed.elements());

        IteratorSource<Iterator<Integer>, Integer> unmodifiable = IteratorProviders.<Integer>fromCollection(
                l -> Collections.unmodifiableCollection(new ArrayList<>(l))).source(List.of(1, 2, 3));
        assertFalse(unmodifiable.removeFromSource(2));
        assertFalse(unmodifiable.addToSource(4));
    }
}
