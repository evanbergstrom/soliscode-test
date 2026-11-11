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
package com.soliscode.test.breakable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.listiterator.ListIteratorMethods;

import static com.soliscode.test.assertions.Assertions.assertThrowsDifferent;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_ADD_DOES_NOT_ADD;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_ADD_INSERTS_AFTER_CURSOR;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_ADD_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_TRUE;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_NEXT_INDEX_IS_ONE_TOO_HIGH;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_PREVIOUS_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_PREVIOUS_INDEX_RETURNS_NEXT_INDEX;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_PREVIOUS_THROWS_WRONG_EXCEPTION;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_REMOVE_AFTER_PREVIOUS_REMOVES_THE_ONE_BEFORE;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_SET_DOES_NOT_SET;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_SET_IGNORES_ILLEGAL_STATE;
import static com.soliscode.test.breakable.BreakableListIterator.LIST_ITERATOR_SET_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests that each break of `BreakableListIterator` changes what its description says, and that without one it
/// behaves as the list iterator it wraps (#37).
@DisplayName("Tests for BreakableListIterator")
public class BreakableListIteratorTest {

    private final List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4));

    /// Default constructor.
    public BreakableListIteratorTest() {
        super();
    }

    private BreakableListIterator<Integer> iterator(final Break... breaks) {
        return new BreakableListIterator<>(list.listIterator(), Set.of(breaks), Map.of());
    }

    private BreakableListIterator<Integer> unsupported(final InterfaceMethod method, final Break... breaks) {
        Map<InterfaceMethod, MethodStatus> statuses = new HashMap<>();
        statuses.put(method, new MethodStatus(false));
        return new BreakableListIterator<>(list.listIterator(), Set.of(breaks), statuses);
    }

    @Test
    @DisplayName("without a break it behaves as the list iterator it wraps")
    void unbrokenBehavesAsTheListIterator() {
        BreakableListIterator<Integer> it = iterator();
        assertFalse(it.hasPrevious());
        assertEquals(-1, it.previousIndex());
        assertEquals(1, it.next());
        assertEquals(1, it.previous());
        assertEquals(0, it.nextIndex());
        it.next();
        it.set(10);
        it.add(11);
        assertThrows(IllegalStateException.class, it::remove);
        assertEquals(11, it.previous());
        it.remove();
        assertEquals(List.of(10, 2, 3, 4), list);
    }

    @Test
    @DisplayName("LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_FALSE and _TRUE fix hasPrevious()")
    void hasPreviousBreaks() {
        BreakableListIterator<Integer> never = iterator(LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_FALSE);
        never.next();
        assertFalse(never.hasPrevious());
        assertTrue(iterator(LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_TRUE).hasPrevious());
    }

    @Test
    @DisplayName("LIST_ITERATOR_PREVIOUS_ALWAYS_RETURNS_NULL moves back and returns null")
    void previousReturnsNullBreak() {
        BreakableListIterator<Integer> it = iterator(LIST_ITERATOR_PREVIOUS_ALWAYS_RETURNS_NULL);
        it.next();
        assertNull(it.previous());
        assertEquals(0, it.nextIndex());
    }

    @Test
    @DisplayName("LIST_ITERATOR_PREVIOUS_THROWS_WRONG_EXCEPTION throws another exception at the start")
    void previousWrongExceptionBreak() {
        assertThrows(NoSuchElementException.class, iterator()::previous);
        assertThrowsDifferent(NoSuchElementException.class,
                iterator(LIST_ITERATOR_PREVIOUS_THROWS_WRONG_EXCEPTION)::previous);
    }

    @Test
    @DisplayName("LIST_ITERATOR_NEXT_INDEX_IS_ONE_TOO_HIGH and PREVIOUS_INDEX_RETURNS_NEXT_INDEX change the indexes")
    void indexBreaks() {
        assertEquals(1, iterator(LIST_ITERATOR_NEXT_INDEX_IS_ONE_TOO_HIGH).nextIndex());
        assertEquals(0, iterator(LIST_ITERATOR_PREVIOUS_INDEX_RETURNS_NEXT_INDEX).previousIndex());
    }

    @Test
    @DisplayName("LIST_ITERATOR_SET_DOES_NOT_SET leaves the list, and still throws in an illegal state")
    void setDoesNotSetBreak() {
        BreakableListIterator<Integer> it = iterator(LIST_ITERATOR_SET_DOES_NOT_SET);
        assertThrows(IllegalStateException.class, () -> it.set(10));
        it.next();
        it.set(10);
        assertEquals(List.of(1, 2, 3, 4), list);
    }

    @Test
    @DisplayName("LIST_ITERATOR_SET_IGNORES_ILLEGAL_STATE returns before next()")
    void setIgnoresIllegalStateBreak() {
        iterator(LIST_ITERATOR_SET_IGNORES_ILLEGAL_STATE).set(10);
        assertEquals(List.of(1, 2, 3, 4), list);
    }

    @Test
    @DisplayName("the wrong-exception-if-not-supported breaks of set() and add() throw another exception")
    void unsupportedBreaks() {
        BreakableListIterator<Integer> set = unsupported(ListIteratorMethods.LIST_ITERATOR_SET);
        set.next();
        assertThrows(UnsupportedOperationException.class, () -> set.set(10));
        BreakableListIterator<Integer> wrongSet = unsupported(ListIteratorMethods.LIST_ITERATOR_SET,
                LIST_ITERATOR_SET_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED);
        wrongSet.next();
        assertThrowsDifferent(UnsupportedOperationException.class, () -> wrongSet.set(10));
        assertThrows(UnsupportedOperationException.class,
                () -> unsupported(ListIteratorMethods.LIST_ITERATOR_ADD).add(10));
        assertThrowsDifferent(UnsupportedOperationException.class,
                () -> unsupported(ListIteratorMethods.LIST_ITERATOR_ADD,
                        LIST_ITERATOR_ADD_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED).add(10));
    }

    @Test
    @DisplayName("LIST_ITERATOR_ADD_DOES_NOT_ADD leaves the list, and set() and remove() then throw")
    void addDoesNotAddBreak() {
        BreakableListIterator<Integer> it = iterator(LIST_ITERATOR_ADD_DOES_NOT_ADD);
        it.next();
        it.add(10);
        assertThrows(IllegalStateException.class, it::remove);
        assertThrows(IllegalStateException.class, () -> it.set(11));
        assertEquals(List.of(1, 2, 3, 4), list);
    }

    @Test
    @DisplayName("LIST_ITERATOR_ADD_INSERTS_AFTER_CURSOR puts the element after the cursor; set() and remove() throw")
    void addInsertsAfterCursorBreak() {
        BreakableListIterator<Integer> it = iterator(LIST_ITERATOR_ADD_INSERTS_AFTER_CURSOR);
        it.next();
        it.add(10);
        assertEquals(1, it.nextIndex());
        assertThrows(IllegalStateException.class, it::remove);
        assertThrows(IllegalStateException.class, () -> it.set(11));
        assertEquals(10, it.next());
        assertEquals(List.of(1, 10, 2, 3, 4), list);
    }

    @Test
    @DisplayName("LIST_ITERATOR_REMOVE_AFTER_PREVIOUS_REMOVES_THE_ONE_BEFORE removes the element before, once")
    void removeAfterPreviousBreak() {
        BreakableListIterator<Integer> it = iterator(LIST_ITERATOR_REMOVE_AFTER_PREVIOUS_REMOVES_THE_ONE_BEFORE);
        it.next();
        it.next();
        it.next();
        assertEquals(3, it.previous());
        it.remove();
        assertThrows(IllegalStateException.class, it::remove);
        assertEquals(List.of(1, 3, 4), list);

        BreakableListIterator<Integer> first = iterator(LIST_ITERATOR_REMOVE_AFTER_PREVIOUS_REMOVES_THE_ONE_BEFORE);
        first.next();
        first.previous();
        first.remove();
        assertEquals(List.of(3, 4), list);
    }
}
