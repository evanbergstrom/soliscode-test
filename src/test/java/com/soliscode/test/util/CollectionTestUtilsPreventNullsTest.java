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
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests that the null-preventing wrappers' bulk methods check their argument for `null` (T-0008): they throw
/// `NullPointerException` for an argument with a `null` element, and otherwise act as the wrapped collection does,
/// whatever it holds and whether or not the argument permits `null`.
@DisplayName("CollectionTestUtils.preventNulls: the bulk methods check their argument for null")
public class CollectionTestUtilsPreventNullsTest {

    /// Default constructor.
    public CollectionTestUtilsPreventNullsTest() {
        super();
    }

    private static ArrayList<Integer> backing(final Integer... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    private static Collection<Integer> wrap(final Collection<Integer> c) {
        return CollectionTestUtils.preventNulls(c);
    }

    private static List<Integer> withNull() {
        return Arrays.asList(2, null);
    }

    // The Collection wrapper

    @Test
    @DisplayName("Collection containsAll throws for an argument with a null")
    void collectionContainsAllWhenArgumentHasNullThrows() {
        Collection<Integer> c = wrap(backing(1, 2));
        assertThrows(NullPointerException.class, () -> c.containsAll(withNull()));
    }

    @Test
    @DisplayName("Collection addAll throws for an argument with a null, and adds nothing")
    void collectionAddAllWhenArgumentHasNullThrows() {
        ArrayList<Integer> b = backing(1);
        Collection<Integer> c = wrap(b);
        assertThrows(NullPointerException.class, () -> c.addAll(withNull()));
        assertEquals(List.of(1), b);
    }

    @Test
    @DisplayName("Collection removeAll throws for an argument with a null, and removes nothing")
    void collectionRemoveAllWhenArgumentHasNullThrows() {
        ArrayList<Integer> b = backing(1, 2);
        Collection<Integer> c = wrap(b);
        assertThrows(NullPointerException.class, () -> c.removeAll(withNull()));
        assertEquals(List.of(1, 2), b);
    }

    @Test
    @DisplayName("Collection retainAll throws for an argument with a null, and removes nothing")
    void collectionRetainAllWhenArgumentHasNullThrows() {
        ArrayList<Integer> b = backing(1, 2);
        Collection<Integer> c = wrap(b);
        assertThrows(NullPointerException.class, () -> c.retainAll(withNull()));
        assertEquals(List.of(1, 2), b);
    }

    @Test
    @DisplayName("Collection bulk methods take an argument that refuses null, as the wrapped collection does")
    void collectionBulkMethodsWhenArgumentRefusesNullDelegate() {
        ArrayList<Integer> b = backing(1, 2);
        Collection<Integer> c = wrap(b);
        assertTrue(c.containsAll(List.of(2)));
        assertTrue(c.addAll(List.of(3)));
        assertTrue(c.removeAll(List.of(1)));
        assertTrue(c.retainAll(List.of(3)));
        assertEquals(List.of(3), b);
    }

    @Test
    @DisplayName("Collection bulk methods act as the wrapped collection does when it already holds a null")
    void collectionBulkMethodsWhenWrappedHoldsNullDelegate() {
        ArrayList<Integer> b = backing(1, null);
        Collection<Integer> c = wrap(b);
        assertTrue(c.containsAll(List.of(1)));
        assertTrue(c.addAll(List.of(2)));
        assertEquals(Arrays.asList(1, null, 2), b);
        // ArrayList's removeAll and retainAll ask the argument whether it contains each element, null included, so
        // the argument here permits null: List.of(1).contains(null) would throw, from the backing list.
        assertTrue(c.removeAll(backing(1)));
        assertEquals(Arrays.asList(null, 2), b);
        assertTrue(c.retainAll(backing(2)));
        assertEquals(List.of(2), b);
    }

    // The List wrapper

    @Test
    @DisplayName("List bulk methods throw for an argument with a null, and change nothing")
    void listBulkMethodsWhenArgumentHasNullThrow() {
        ArrayList<Integer> b = backing(1, 2);
        List<Integer> l = CollectionTestUtils.preventNulls(b);
        assertThrows(NullPointerException.class, () -> l.containsAll(withNull()));
        assertThrows(NullPointerException.class, () -> l.addAll(withNull()));
        assertThrows(NullPointerException.class, () -> l.addAll(0, withNull()));
        assertThrows(NullPointerException.class, () -> l.removeAll(withNull()));
        assertThrows(NullPointerException.class, () -> l.retainAll(withNull()));
        assertEquals(List.of(1, 2), b);
    }

    @Test
    @DisplayName("List containsAll takes an argument that refuses null")
    void listContainsAllWhenArgumentRefusesNullDelegates() {
        List<Integer> l = CollectionTestUtils.preventNulls(backing(1, 2));
        assertTrue(l.containsAll(List.of(2)));
        assertFalse(l.containsAll(List.of(3)));
    }

    @Test
    @DisplayName("List addAll takes an argument that refuses null")
    void listAddAllWhenArgumentRefusesNullDelegates() {
        ArrayList<Integer> b = backing(1);
        List<Integer> l = CollectionTestUtils.preventNulls(b);
        assertTrue(l.addAll(List.of(2)));
        assertEquals(List.of(1, 2), b);
    }

    @Test
    @DisplayName("List addAll at an index takes an argument that refuses null")
    void listAddAllAtIndexWhenArgumentRefusesNullDelegates() {
        ArrayList<Integer> b = backing(1);
        List<Integer> l = CollectionTestUtils.preventNulls(b);
        assertTrue(l.addAll(0, List.of(2)));
        assertEquals(List.of(2, 1), b);
    }

    @Test
    @DisplayName("List removeAll takes an argument that refuses null")
    void listRemoveAllWhenArgumentRefusesNullDelegates() {
        ArrayList<Integer> b = backing(1, 2);
        List<Integer> l = CollectionTestUtils.preventNulls(b);
        assertTrue(l.removeAll(List.of(1)));
        assertEquals(List.of(2), b);
    }

    @Test
    @DisplayName("List retainAll takes an argument that refuses null")
    void listRetainAllWhenArgumentRefusesNullDelegates() {
        ArrayList<Integer> b = backing(1, 2);
        List<Integer> l = CollectionTestUtils.preventNulls(b);
        assertTrue(l.retainAll(List.of(1)));
        assertEquals(List.of(1), b);
    }
}
