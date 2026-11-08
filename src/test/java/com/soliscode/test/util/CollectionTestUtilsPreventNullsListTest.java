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

package com.soliscode.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests that the `preventNulls(List)` wrapper refuses `null` through every method that takes an element, its list
/// iterators, its sub-lists and `replaceAll` included, and otherwise acts as the backing list does (T-0051).
@DisplayName("CollectionTestUtils.preventNulls(List): refuses null through its queries, iterators and views")
public class CollectionTestUtilsPreventNullsListTest {

    /// Default constructor.
    public CollectionTestUtilsPreventNullsListTest() {
        super();
    }

    private static ArrayList<Integer> backing(final Integer... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    // remove(Object), indexOf and lastIndexOf (the user's decision on T-0051: they throw for null)

    @Test
    @DisplayName("remove(Object) throws for null")
    void removeObjectWhenNullThrows() {
        List<Integer> l = CollectionTestUtils.preventNulls(backing(1, 2));
        assertThrows(NullPointerException.class, () -> l.remove((Object) null));
        assertTrue(l.remove((Object) 1));
        assertEquals(List.of(2), l);
    }

    @Test
    @DisplayName("indexOf throws for null")
    void indexOfWhenNullThrows() {
        List<Integer> l = CollectionTestUtils.preventNulls(backing(1, 2, 1));
        assertThrows(NullPointerException.class, () -> l.indexOf(null));
        assertEquals(0, l.indexOf(1));
        assertEquals(-1, l.indexOf(3));
    }

    @Test
    @DisplayName("lastIndexOf throws for null")
    void lastIndexOfWhenNullThrows() {
        List<Integer> l = CollectionTestUtils.preventNulls(backing(1, 2, 1));
        assertThrows(NullPointerException.class, () -> l.lastIndexOf(null));
        assertEquals(2, l.lastIndexOf(1));
        assertEquals(-1, l.lastIndexOf(3));
    }

    // The list iterators

    @Test
    @DisplayName("listIterator() add throws for null, and adds nothing")
    void listIteratorAddWhenNullThrows() {
        ArrayList<Integer> b = backing(1, 2);
        ListIterator<Integer> it = CollectionTestUtils.preventNulls(b).listIterator();
        assertThrows(NullPointerException.class, () -> it.add(null));
        assertEquals(List.of(1, 2), b);
    }

    @Test
    @DisplayName("listIterator() set throws for null, and changes nothing")
    void listIteratorSetWhenNullThrows() {
        ArrayList<Integer> b = backing(1, 2);
        ListIterator<Integer> it = CollectionTestUtils.preventNulls(b).listIterator();
        it.next();
        assertThrows(NullPointerException.class, () -> it.set(null));
        assertEquals(List.of(1, 2), b);
    }

    @Test
    @DisplayName("listIterator(int) add and set throw for null, and change nothing")
    void listIteratorAtIndexAddAndSetWhenNullThrow() {
        ArrayList<Integer> b = backing(1, 2);
        ListIterator<Integer> it = CollectionTestUtils.preventNulls(b).listIterator(1);
        assertThrows(NullPointerException.class, () -> it.add(null));
        it.next();
        assertThrows(NullPointerException.class, () -> it.set(null));
        assertEquals(List.of(1, 2), b);
    }

    @Test
    @DisplayName("listIterator acts as the backing list's does for non-null elements")
    void listIteratorWhenNotNullActsAsBacking() {
        ArrayList<Integer> b = backing(1, 2, 3);
        ListIterator<Integer> it = CollectionTestUtils.preventNulls(b).listIterator(1);
        assertTrue(it.hasPrevious());
        assertEquals(0, it.previousIndex());
        assertEquals(1, it.nextIndex());
        assertEquals(2, it.next());
        it.set(20);
        it.add(25);
        assertEquals(3, it.next());
        it.remove();
        assertFalse(it.hasNext());
        assertEquals(25, it.previous());
        assertEquals(List.of(1, 20, 25), b);
    }

    // subList

    @Test
    @DisplayName("subList refuses null through add, set, addAll, its list iterator and its own sub-lists")
    void subListWhenNullThrows() {
        ArrayList<Integer> b = backing(1, 2, 3);
        List<Integer> s = CollectionTestUtils.preventNulls(b).subList(0, 2);
        assertThrows(NullPointerException.class, () -> s.add(null));
        assertThrows(NullPointerException.class, () -> s.add(0, null));
        assertThrows(NullPointerException.class, () -> s.set(0, null));
        assertThrows(NullPointerException.class, () -> s.addAll(Arrays.asList(4, null)));
        assertThrows(NullPointerException.class, () -> s.listIterator().add(null));
        assertThrows(NullPointerException.class, () -> s.subList(0, 1).add(null));
        assertThrows(NullPointerException.class, () -> s.indexOf(null));
        assertEquals(List.of(1, 2, 3), b);
    }

    @Test
    @DisplayName("subList acts as the backing list's view does for non-null elements")
    void subListWhenNotNullActsAsBackingView() {
        ArrayList<Integer> b = backing(1, 2, 3);
        List<Integer> s = CollectionTestUtils.preventNulls(b).subList(1, 3);
        assertEquals(List.of(2, 3), s);
        assertEquals(1, s.indexOf(3));
        s.add(4);
        s.set(0, 20);
        assertEquals(List.of(1, 20, 3, 4), b);
        s.clear();
        assertEquals(List.of(1), b);
    }

    // replaceAll and sort

    @Test
    @DisplayName("replaceAll throws when the operator returns null")
    void replaceAllWhenOperatorReturnsNullThrows() {
        ArrayList<Integer> b = backing(1, 2);
        List<Integer> l = CollectionTestUtils.preventNulls(b);
        assertThrows(NullPointerException.class, () -> l.replaceAll(x -> null));
        assertFalse(b.contains(null));
    }

    @Test
    @DisplayName("replaceAll replaces each element when the operator returns non-null")
    void replaceAllWhenOperatorReturnsNotNullReplaces() {
        ArrayList<Integer> b = backing(1, 2);
        CollectionTestUtils.preventNulls(b).replaceAll(x -> x * 10);
        assertEquals(List.of(10, 20), b);
    }

    @Test
    @DisplayName("sort acts as the backing list's does, even when it already holds a null")
    void sortWhenBackingHoldsNullActsAsBacking() {
        ArrayList<Integer> b = backing(2, null, 1);
        CollectionTestUtils.preventNulls(b).sort(Comparator.nullsFirst(Comparator.naturalOrder()));
        assertEquals(Arrays.asList(null, 1, 2), b);
    }
}
