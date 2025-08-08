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
package org.soliscode.test.breakable;

import org.jetbrains.annotations.Contract;
import org.soliscode.test.contract.CollectionMethods;

import java.util.*;
import java.util.function.Consumer;

/// An iterator that can be broken in well-defined ways in order to test collection utilities or testing classes.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// Any breaks that are not listed in this class can be added to an instance of `BreakableIterator`, but will
/// not have any impact on how it functions.
///
/// @param <E> the element type.
/// @author evanbergstrom
/// @since 1.0
/// @see Iterator
public class BreakableIterator<E> extends AbstractBreakable implements Iterator<E> {
    private final Iterator<E> iterator;

    /// The iterator will always have no elements
    public static final Break ITERATOR_IS_ALWAYS_EMPTY = new Break("iterator has no elements");

    /// The iterator will skip the first element.
    public static final Break ITERATOR_SKIPS_FIRST_ELEMENT = new Break("iterator skips the first element.");

    /// The iterator `hasNext` method will always return a true.
    /// @see BreakableIterator#hasNext();
    public static final Break ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE = new Break("iterator hasNext always returns true.");

    /// The iterator `hasNext` method will always return a false.
    /// @see BreakableIterator#hasNext();
    public static final Break ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE = new Break("iterator hasNext always returns false.");

    /// The iterator `hasNext` method will always return the opposite value
    /// @see BreakableIterator#hasNext();
    public static final Break ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE = new Break("iterator hasNext always returns opposite value");

    /// The iterator `next` method will always return `null`
    /// @see BreakableIterator#next();
    public static final Break ITERATOR_NEXT_ALWAYS_RETURNS_NULL = new Break("iterator next method returns null");

    /// The iterator `next` method will always return `null`
    /// @see BreakableIterator#next();
    public static final Break ITERATOR_NEXT_THROWS_WRONG_EXCEPTION = new Break("iterator next method always returns null");

    /// The iterator 'remove' method does not remove the element at the current iterator position.
    /// @see BreakableIterator#remove();
    public static final Break ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT = new Break("iterator remove does not remove elements");

    /// The iterator 'remove' method throws the wrong exception for an illegal state
    /// @see BreakableIterator#remove();
    public static final Break ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE = new Break("iterator remove throws wrong exception for illegal state");

    /// The iterator 'remove' method throws the wrong exception nif it is not supported.
    /// @see BreakableIterator#remove();
    public static final Break ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED = new Break("iterator remove method throws wrong exception if not supported.");

    /// The iterator 'forEachRemaining' does not call the action
    /// @see BreakableIterator#forEachRemaining(Consumer);
    public static final Break ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION = new Break("iterator forEachRemaining does not call action");

    /// The iterator 'forEachRemaining' throws the wrong exception for a `null` argument.
    /// @see BreakableIterator#forEachRemaining(Consumer);
    public static final Break ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT = new Break("iterator forEachRemaining throws wrong exception for null argument.");

    /// Constructs a breakable iterator from a iterator that will provide the implementation.
    ///
    /// @param iterator The iterator that will provide the implementation
    /// @param breaks The breaks that define how the iterator is broken.
    /// @param characteristics A mask that indicates the characteristics of the iterator.
    public BreakableIterator(final Iterator<E> iterator, final Collection<Break> breaks, final int characteristics) {
        super(breaks);
        if (hasBreak(ITERATOR_IS_ALWAYS_EMPTY)) {
            this.iterator = Collections.emptyIterator();
        } else {
            this.iterator = iterator;
        }
        if (hasBreak(ITERATOR_SKIPS_FIRST_ELEMENT) && iterator.hasNext()) {
            iterator.next();
        }
    }

    /// Implements the [hasNext][Iterator#hasNext] method from the [Iterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following collection breaks:
    /// - [ITERATOR_IS_ALWAYS_EMPTY][BreakableIterator#ITERATOR_IS_ALWAYS_EMPTY]
    /// - [ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE][BreakableIterator#ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE]
    /// - [ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE][BreakableIterator#ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE]
    /// - [ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE][BreakableIterator#ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE]
    ///
    /// # Example
    /// A iterator that has any of these breaks can be constructed using the `BreakableIterator` builder:
    /// ```java
    ///     Iterator<Integer> iterator = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE)
    ///         .build().iterator();
    /// ```
    /// @return 'true' or 'false' depending on the state of the iterator and the breaks.
    /// @see Iterator#hasNext
    @Override
    @Contract(pure=true)
    public boolean hasNext() {
        if (hasBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE)) {
            return true;
        } else if (hasBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE)) {
            return false;
        } else if (hasBreak(ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE)) {
            return !iterator.hasNext();
        } else {
            return iterator.hasNext();
        }
    }

    /// Implements the [next][Iterator#next] method from the [Iterator] interface.
    ///
    /// #Breaks
    /// This method can be broken using the following collection breaks:
    /// - [ITERATOR_NEXT_ALWAYS_RETURNS_NULL][BreakableIterator#ITERATOR_NEXT_ALWAYS_RETURNS_NULL]
    /// - [ITERATOR_NEXT_ALWAYS_RETURNS_NULL][BreakableIterator#ITERATOR_NEXT_ALWAYS_RETURNS_NULL]
    ///
    /// A iterator that has any of these breaks can be constructed using the `BreakableIterator` builder:
    /// ```java
    ///     Iterator<Integer> iterator = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(ITERATOR_NEXT_ALWAYS_RETURNS_NULL)
    ///         .build().iterator();
    /// ```
    /// @return The next element or `null` if there sare no more elements or if the iterator is broken.
    /// @throws NoSuchElementException if the iteration has no more elements
    /// @see Iterator#next
    @Override
    @Contract(mutates="this")
    public E next() {
        try {
            E element = iterator.next();
            if (hasBreak(ITERATOR_NEXT_ALWAYS_RETURNS_NULL)) {
                return null;
            } else {
                return element;
            }
        } catch (NoSuchElementException e) {
            if (hasBreak(ITERATOR_NEXT_THROWS_WRONG_EXCEPTION)) {
                throw new RuntimeException();
            } else {
                throw e;
            }
        }
    }

    /// Implements the [remove][Iterator#remove] method from the [Iterator] interface.
    ///
    /// #Breaks
    /// This method can be broken using the following collection breaks:
    /// - [ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT][BreakableIterator#ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT]
    /// - [ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED][BreakableIterator#ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED]
    /// - [ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE][BreakableIterator#ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE]
    ///
    /// A iterator that has any of these breaks can be constructed using the `BreakableIterator` builder:
    /// ```java
    ///     Iterator<Integer> iterator = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT)
    ///         .build().iterator();
    /// ```
    /// @throws UnsupportedOperationException if the `remove` operation is not supported by this iterator
    /// @throws IllegalStateException if the `next` method has not yet been called, or the `remove` method has already
    ///                               been called after the last call to the `next` method.
    /// @see Iterator#remove
    @Override
    public void remove() {
        if (supportsMethod(CollectionMethods.IteratorRemove)) {
            if (!hasBreak(ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT)) {
                try {
                    iterator.remove();
                } catch (UnsupportedOperationException e) {
                    if (hasBreak(ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE)) {
                        throw new RuntimeException();
                    } else {
                        throw e;
                    }
                }
            }
        } else {
            if (hasBreak(ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED)) {
                throw new RuntimeException();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    /// Implements the [forEachRemaining][Iterator#forEachRemaining] method from the [Iterator] interface.
    ///
    /// #Breaks
    /// This method can be broken using the following iterable breaks:
    /// - [ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION][BreakableIterator#ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION]
    /// - [ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT][BreakableIterator#ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT]
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Iterable<Integer> iterable = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)
    ///         .build();
    /// ```
    /// @param action The action to run on the remaining elements.
    /// @throws NullPointerException if the specified action is null
    @Override
    public void forEachRemaining(final Consumer<? super E> action) {
        if (supportsMethod(CollectionMethods.IteratorForEachRemaining)) {
            if (!hasBreak(ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)) {
                iterator.forEachRemaining(action);
            }
        } else {
            if (hasBreak(ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT)) {
                throw new RuntimeException();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
