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

import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.iterable.IterableMethods;
import com.soliscode.test.contract.listiterator.ListIteratorMethods;

/// A list iterator that can be broken in well-defined ways, to show that
/// [com.soliscode.test.contract.listiterator.ListIteratorContract] can fail.
///
/// It delegates to a list iterator, except where a break is set. It also has [BreakableIterator]'s breaks. `set` and
/// `add` may be made unsupported ([ListIteratorMethods]), and `remove` too
/// ([IterableMethods#ITERATOR_REMOVE]).
///
/// ## Thread Safety
/// It is not thread safe, as the list iterators of the JDK's lists are not.
///
/// @param <E> the element type
/// @since 0.1.3
/// @see java.util.ListIterator
public class BreakableListIterator<E> extends BreakableIterator<E> implements ListIterator<E> {

    /// **Purpose:** `hasPrevious` is always false. **Effect:** it returns `false` wherever the cursor is.
    /// **Affected Methods:** [hasPrevious()][#hasPrevious()]
    public static final Break LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_FALSE =
            new Break("list iterator hasPrevious always returns false");

    /// **Purpose:** `hasPrevious` is always true. **Effect:** it returns `true` wherever the cursor is, the start
    /// included. **Affected Methods:** [hasPrevious()][#hasPrevious()]
    public static final Break LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_TRUE =
            new Break("list iterator hasPrevious always returns true");

    /// **Purpose:** `previous` returns the wrong element. **Effect:** it moves the cursor back, and returns `null`.
    /// **Affected Methods:** [previous()][#previous()]
    public static final Break LIST_ITERATOR_PREVIOUS_ALWAYS_RETURNS_NULL =
            new Break("list iterator previous always returns null");

    /// **Purpose:** `previous` at the start throws the wrong exception. **Effect:** it throws a [RuntimeException],
    /// not [NoSuchElementException]. **Affected Methods:** [previous()][#previous()]
    public static final Break LIST_ITERATOR_PREVIOUS_THROWS_WRONG_EXCEPTION =
            new Break("list iterator previous throws the wrong exception at the start");

    /// **Purpose:** `nextIndex` is wrong. **Effect:** it returns one more than the index of the element `next` would
    /// return. **Affected Methods:** [nextIndex()][#nextIndex()]
    public static final Break LIST_ITERATOR_NEXT_INDEX_IS_ONE_TOO_HIGH =
            new Break("list iterator nextIndex is one too high");

    /// **Purpose:** `previousIndex` is wrong. **Effect:** it returns what `nextIndex` does.
    /// **Affected Methods:** [previousIndex()][#previousIndex()]
    public static final Break LIST_ITERATOR_PREVIOUS_INDEX_RETURNS_NEXT_INDEX =
            new Break("list iterator previousIndex returns nextIndex");

    /// **Purpose:** `set` does not replace the element. **Effect:** called in a legal state (after `next` or
    /// `previous`, with no `remove` or `add` since), it returns without changing the list; in an illegal state it
    /// throws as it should. **Affected Methods:** [set(Object)][#set(Object)]
    public static final Break LIST_ITERATOR_SET_DOES_NOT_SET =
            new Break("list iterator set does not replace the element");

    /// **Purpose:** `set` in an illegal state does nothing. **Effect:** where the list iterator would throw
    /// [IllegalStateException], it returns instead. **Affected Methods:** [set(Object)][#set(Object)]
    public static final Break LIST_ITERATOR_SET_IGNORES_ILLEGAL_STATE =
            new Break("list iterator set ignores an illegal state");

    /// **Purpose:** an unsupported `set` throws the wrong exception. **Effect:** where `set` is not supported, it
    /// throws a [RuntimeException], not [UnsupportedOperationException].
    /// **Affected Methods:** [set(Object)][#set(Object)]
    public static final Break LIST_ITERATOR_SET_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED =
            new Break("list iterator set throws the wrong exception if not supported");

    /// **Purpose:** `add` does not insert the element. **Effect:** it returns without changing the list; as after a
    /// correct `add`, `set` and `remove` then throw [IllegalStateException] until the next `next` or `previous`.
    /// **Affected Methods:** [add(Object)][#add(Object)]
    public static final Break LIST_ITERATOR_ADD_DOES_NOT_ADD =
            new Break("list iterator add does not insert the element");

    /// **Purpose:** `add` inserts after the cursor, not before it. **Effect:** it inserts the element and moves the
    /// cursor back over it, so that `next` returns it and `previous` does not, and `nextIndex` and `previousIndex`
    /// do not change; as after a correct `add`, `set` and `remove` then throw [IllegalStateException] until the next
    /// `next` or `previous`. **Affected Methods:** [add(Object)][#add(Object)]
    public static final Break LIST_ITERATOR_ADD_INSERTS_AFTER_CURSOR =
            new Break("list iterator add inserts after the cursor");

    /// **Purpose:** an unsupported `add` throws the wrong exception. **Effect:** where `add` is not supported, it
    /// throws a [RuntimeException], not [UnsupportedOperationException].
    /// **Affected Methods:** [add(Object)][#add(Object)]
    public static final Break LIST_ITERATOR_ADD_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED =
            new Break("list iterator add throws the wrong exception if not supported");

    /// **Purpose:** `remove` after `previous` removes the wrong element. **Effect:** the first `remove` after a
    /// `previous` removes the element before the one `previous` returned; after a `previous` that returned the first
    /// element there is none before it, and it removes correctly. A second `remove` throws [IllegalStateException]
    /// as it should. **Affected Methods:** [remove()][#remove()]
    public static final Break LIST_ITERATOR_REMOVE_AFTER_PREVIOUS_REMOVES_THE_ONE_BEFORE =
            new Break("list iterator remove after previous removes the element before");

    /// The underlying list iterator: the one the constructor was given, or the empty one
    /// [BreakableIterator#ITERATOR_IS_ALWAYS_EMPTY] puts in its place.
    private final ListIterator<E> listIterator;

    /// The last call that moved or changed the list iterator, for the breaks that depend on it.
    private LastCall lastCall = LastCall.NONE;

    /// Creates a `BreakableListIterator` on a list iterator, with breaks and the methods it does not support.
    ///
    /// @param listIteratorArg the underlying list iterator
    /// @param breaks the breaks to apply, [BreakableIterator]'s included
    /// @param methodStatuses whether each optional method is supported
    public BreakableListIterator(final ListIterator<E> listIteratorArg, final Set<Break> breaks,
                                 final Map<InterfaceMethod, MethodStatus> methodStatuses) {
        super(listIteratorArg, breaks, methodStatuses, 0, false);
        this.listIterator = (ListIterator<E>) delegate();
    }

    /// Implements [ListIterator#next()]: [BreakableIterator#next()], recorded as the last move.
    ///
    /// @return the next element
    /// @throws NoSuchElementException if there is none
    @Override
    public @Nullable E next() {
        E e = super.next();
        lastCall = LastCall.NEXT;
        return e;
    }

    /// Implements [ListIterator#hasPrevious()].
    ///
    /// # Breaks
    /// - [#LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_FALSE]
    /// - [#LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_TRUE]
    ///
    /// @return whether there is an element before the cursor
    @Override
    public boolean hasPrevious() {
        if (hasBreak(LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_FALSE)) {
            return false;
        } else if (hasBreak(LIST_ITERATOR_HAS_PREVIOUS_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        return listIterator.hasPrevious();
    }

    /// Implements [ListIterator#previous()].
    ///
    /// # Breaks
    /// - [#LIST_ITERATOR_PREVIOUS_ALWAYS_RETURNS_NULL]
    /// - [#LIST_ITERATOR_PREVIOUS_THROWS_WRONG_EXCEPTION]
    ///
    /// @return the previous element
    /// @throws NoSuchElementException if there is none
    @Override
    public @Nullable E previous() {
        E e;
        try {
            e = listIterator.previous();
        } catch (final NoSuchElementException ex) {
            if (hasBreak(LIST_ITERATOR_PREVIOUS_THROWS_WRONG_EXCEPTION)) {
                throw wrongException(ex);
            }
            throw ex;
        }
        lastCall = LastCall.PREVIOUS;
        return hasBreak(LIST_ITERATOR_PREVIOUS_ALWAYS_RETURNS_NULL) ? null : e;
    }

    /// Implements [ListIterator#nextIndex()].
    ///
    /// # Breaks
    /// - [#LIST_ITERATOR_NEXT_INDEX_IS_ONE_TOO_HIGH]
    ///
    /// @return the index of the element `next` would return
    @Override
    public int nextIndex() {
        int index = listIterator.nextIndex();
        return hasBreak(LIST_ITERATOR_NEXT_INDEX_IS_ONE_TOO_HIGH) ? index + 1 : index;
    }

    /// Implements [ListIterator#previousIndex()].
    ///
    /// # Breaks
    /// - [#LIST_ITERATOR_PREVIOUS_INDEX_RETURNS_NEXT_INDEX]
    ///
    /// @return the index of the element `previous` would return
    @Override
    public int previousIndex() {
        if (hasBreak(LIST_ITERATOR_PREVIOUS_INDEX_RETURNS_NEXT_INDEX)) {
            return listIterator.nextIndex();
        }
        return listIterator.previousIndex();
    }

    /// Implements the optional [ListIterator#remove()]: [BreakableIterator#remove()], and the breaks of its own.
    ///
    /// # Breaks
    /// - [#LIST_ITERATOR_REMOVE_AFTER_PREVIOUS_REMOVES_THE_ONE_BEFORE]
    /// - after [#LIST_ITERATOR_ADD_DOES_NOT_ADD] or [#LIST_ITERATOR_ADD_INSERTS_AFTER_CURSOR], it throws
    ///   [IllegalStateException], as after a correct `add`
    ///
    /// @throws UnsupportedOperationException if `remove` is not supported
    /// @throws IllegalStateException if neither `next` nor `previous` has been called since the last `remove` or
    ///     `add`
    @Override
    public void remove() {
        if (supportsMethod(IterableMethods.ITERATOR_REMOVE)) {
            checkNotAfterBrokenAdd();
            if (hasBreak(LIST_ITERATOR_REMOVE_AFTER_PREVIOUS_REMOVES_THE_ONE_BEFORE)
                    && lastCall == LastCall.PREVIOUS && listIterator.hasPrevious()) {
                listIterator.previous();
            }
        }
        super.remove();
        lastCall = LastCall.REMOVE;
    }

    /// Implements the optional [ListIterator#set(Object)].
    ///
    /// # Breaks
    /// - [#LIST_ITERATOR_SET_DOES_NOT_SET]
    /// - [#LIST_ITERATOR_SET_IGNORES_ILLEGAL_STATE]
    /// - [#LIST_ITERATOR_SET_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED]
    /// - after [#LIST_ITERATOR_ADD_DOES_NOT_ADD] or [#LIST_ITERATOR_ADD_INSERTS_AFTER_CURSOR], it throws
    ///   [IllegalStateException], as after a correct `add`
    ///
    /// @param e the element to put in place of the last one `next` or `previous` returned
    /// @throws UnsupportedOperationException if `set` is not supported
    /// @throws IllegalStateException if neither `next` nor `previous` has been called since the last `remove` or
    ///     `add`
    @Override
    public void set(final E e) {
        checkOptionalMethodSupport(ListIteratorMethods.LIST_ITERATOR_SET,
                LIST_ITERATOR_SET_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED);
        checkNotAfterBrokenAdd();
        boolean legal = lastCall == LastCall.NEXT || lastCall == LastCall.PREVIOUS;
        if (hasBreak(LIST_ITERATOR_SET_DOES_NOT_SET) && legal) {
            return;
        }
        try {
            listIterator.set(e);
        } catch (final IllegalStateException ex) {
            if (!hasBreak(LIST_ITERATOR_SET_IGNORES_ILLEGAL_STATE)) {
                throw ex;
            }
        }
    }

    /// Implements the optional [ListIterator#add(Object)].
    ///
    /// # Breaks
    /// - [#LIST_ITERATOR_ADD_DOES_NOT_ADD]
    /// - [#LIST_ITERATOR_ADD_INSERTS_AFTER_CURSOR]
    /// - [#LIST_ITERATOR_ADD_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED]
    ///
    /// @param e the element to insert
    /// @throws UnsupportedOperationException if `add` is not supported
    @Override
    public void add(final E e) {
        checkOptionalMethodSupport(ListIteratorMethods.LIST_ITERATOR_ADD,
                LIST_ITERATOR_ADD_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED);
        if (!hasBreak(LIST_ITERATOR_ADD_DOES_NOT_ADD)) {
            listIterator.add(e);
            if (hasBreak(LIST_ITERATOR_ADD_INSERTS_AFTER_CURSOR)) {
                listIterator.previous();
            }
        }
        lastCall = LastCall.ADD;
    }

    /// After an `add` that a break changed, the list iterator underneath may allow `set` and `remove`; a correct one
    /// would not, so this throws as it would.
    private void checkNotAfterBrokenAdd() {
        if (lastCall == LastCall.ADD
                && (hasBreak(LIST_ITERATOR_ADD_DOES_NOT_ADD) || hasBreak(LIST_ITERATOR_ADD_INSERTS_AFTER_CURSOR))) {
            throw new IllegalStateException("neither next nor previous has been called since add");
        }
    }

    /// The calls that move or change a list iterator.
    private enum LastCall { NONE, NEXT, PREVIOUS, ADD, REMOVE }
}
