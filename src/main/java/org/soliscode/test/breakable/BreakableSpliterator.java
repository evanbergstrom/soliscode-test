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

import java.util.*;
import java.util.function.Consumer;

/// An Spliterator that can be broken in well-defined ways in order to test collection utilities or testing classes.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
///
/// Any breaks that are not listed in this class can be added to an instance of `BreakableSpliterator`, but will
/// not have any impact on how it functions.
///
/// @param <E> the element type.
/// @author evanbergstrom
/// @since 1.0
/// @see Spliterator
public class BreakableSpliterator<E> extends AbstractBreakable implements Spliterator<E> {
    private final Spliterator<E> iterator;
    private final int characteristics;

    /// The spliterator always has no elements.
    public static final Break SPLITERATOR_IS_ALWAYS_EMPTY = new Break("spliterator always has no elements");

    /// The spliterator that skips the first element.
    public static final Break SPLITERATOR_SKIPS_FIRST_ELEMENT = new Break("spliterator skips the first element");

    /// The 'forEachRemaining' method on the spliterator does not call the action.
    /// @see BreakableSpliterator#forEachRemaining(Consumer)
    public static final Break SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION = new Break("spliterator forEachRemaining does not call action");

    /// The 'trySplit' method on the spliterator always returns `null`.
    /// @see BreakableSpliterator#trySplit()
    public static final Break SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL = new Break("spliterator trySplit always returns null");

    /// The 'tryAdvance' method on the spliterator does not call the action.
    /// @see BreakableSpliterator#tryAdvance(Consumer)
    public static final Break SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION = new Break("spliterator tryAdvance does not call action");

    /// The 'tryAdvance' method always returns true.
    /// @see BreakableSpliterator#tryAdvance(Consumer)
    /// SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE,
    public static final Break SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE = new Break("spliterator tryAdvance always returns true");

    /// The 'tryAdvance' method always returns false.
    /// @see BreakableSpliterator#tryAdvance(Consumer)
    public static final Break SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE = new Break("spliterator tryAdvance always returns false");

    /// The `estimateSize` method always returns the max value.
    /// @see BreakableSpliterator#estimateSize()
    public static final Break SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE = new Break("spliterator estimateSize always returns the max value.");

    /// The `estimateSize` method always returns zero.
    /// @see BreakableSpliterator#estimateSize()
    public static final Break SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO = new Break("spliterator estimateSize` always returns zero.");

    /// The `getExactSizeIfKnown` method always returns negative one (-1).
    /// @see BreakableSpliterator#getExactSizeIfKnown()
    public static final Break SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE = new Break("");

    /// The `characteristics` method always returns zero (0).
    /// @see BreakableSpliterator#characteristics()
    public static final Break SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO = new Break("spliterator getExactSizeIfKnown always returns -1");

    /// The `hasCharacteristics` method always returns `true`.
    /// @see BreakableSpliterator#hasCharacteristics(int)
    public static final Break SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE = new Break("spliterator hasCharacteristics always returns true");

    /// The `hasCharacteristics` method always returns `false`.
    /// @see BreakableSpliterator#hasCharacteristics(int)
    public static final Break SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE = new Break("spliterator hasCharacteristics always returns false");

    /// The `getComparator` method always return `null`.
    /// @see BreakableSpliterator#getComparator()
    public static final Break SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL = new Break("spliterator getComparator always returns null");


    /// Constructs a breakable spliterator from a spliterator that will provide the implementation.
    ///
    /// @param iterator The spliterator that will provide the implementation
    /// @param breaks The breaks that define how the iterator is broken.
    /// @param characteristics A mask that indicates the characteristics of the iterator.
    public BreakableSpliterator(final Spliterator<E> iterator, final Collection<Break> breaks, final int characteristics) {
        super(breaks);
        this.iterator = iterator;
        this.characteristics = characteristics;
    }

    /// Implements the [forEachRemaining][Spliterator#forEachRemaining] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following collection breaks:
    /// - [SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION][BreakableSpliterator#SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be constructed using the iterable builder:
    /// ```java
    ///     Spliterator<Integer> iterator = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)
    ///         .build().spliterator();
    /// ```
    /// @param action The action to run on the remaining elements.
    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        if (!hasBreak(SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)) {
            iterator.forEachRemaining(action);
        }
    }

    /// Implements the [forEachRemaining][Spliterator#forEachRemaining] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following collection breaks:
    /// - [SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL][BreakableSpliterator#SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL]
    /// - [SPLITERATOR_IS_ALWAYS_EMPTY][BreakableSpliterator#SPLITERATOR_IS_ALWAYS_EMPTY]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be constructed using the iterable builder:
    /// ```java
    ///     Spliterator<Integer> iterator = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL)
    ///         .build().spliterator();
    /// ```
    @Override
    public Spliterator<E> trySplit() {
        if (hasBreak(SPLITERATOR_IS_ALWAYS_EMPTY)) {
            return null;
        } else if (hasBreak(SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL)) {
            return null;
        } else {
            return iterator.trySplit();
        }
    }

    /// Implements the [tryAdvance][Spliterator#tryAdvance] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following collection breaks:
    /// - [SPLITERATOR_IS_ALWAYS_EMPTY][BreakableSpliterator#SPLITERATOR_IS_ALWAYS_EMPTY]
    /// - [SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION][BreakableSpliterator#SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION]
    /// - [SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE][BreakableSpliterator#SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE]
    /// - [SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE][BreakableSpliterator#SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be constructed using the iterable builder:
    /// ```java
    ///     Spliterator<Integer> iterator = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION)
    ///         .build().spliterator();
    /// ```
    /// @param action The action to perform on the element
    /// @return `false` if no remaining elements existed upon entry to this method, else `true`.
    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        if (hasBreak(SPLITERATOR_IS_ALWAYS_EMPTY)) {
            return false;
        } else if (hasBreak(SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION)) {
            return iterator.tryAdvance((e) -> {});
        } else if (hasBreak(SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE)) {
            iterator.tryAdvance(action);
            return true;
        } else if (hasBreak(SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE)) {
            iterator.tryAdvance(action);
            return false;
        } else {
            return iterator.tryAdvance(action);
        }
    }

    /// Implements the [estimateSize][Spliterator#estimateSize] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following collection breaks:
    /// - [SPLITERATOR_IS_ALWAYS_EMPTY][BreakableSpliterator#SPLITERATOR_IS_ALWAYS_EMPTY]
    /// - [SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE][BreakableSpliterator#SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE]
    /// - [SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO][BreakableSpliterator#SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be constructed using the iterable builder:
    /// ```java
    ///     Spliterator<Integer> iterator = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE)
    ///         .build().spliterator();
    /// ```
    /// @return the estimated size, or zero if iut is broken, or Long. MAX_VALUE if infinite, unknown, too expensive to
    ///         compute or broken.
    @Override
    public long estimateSize() {
        if (hasBreak(SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE)) {
            return Long.MAX_VALUE;
        } else if (hasBreak(SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO)) {
            return 0;
        } else if (hasBreak(SPLITERATOR_IS_ALWAYS_EMPTY)) {
            return 0;
        } else {
            return iterator.estimateSize();
        }
    }

    /// Implements the [getExactSizeIfKnown][Spliterator#getExactSizeIfKnown] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following collection breaks:
    /// - [SPLITERATOR_IS_ALWAYS_EMPTY][BreakableSpliterator#SPLITERATOR_IS_ALWAYS_EMPTY]
    /// - [SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE][BreakableSpliterator#SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be constructed using the iterable builder:
    /// ```java
    ///     Spliterator<Integer> iterator = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE)
    ///         .build().spliterator();
    /// ```
    /// @return the exact size, if known, else -1, or another value if it is broken.
    @Override
    public long getExactSizeIfKnown() {
        if (hasBreak(SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE)) {
            return -1;
        } else if ((characteristics & SIZED) == SIZED) {
            return iterator.getExactSizeIfKnown();
        } else if (hasBreak(SPLITERATOR_IS_ALWAYS_EMPTY)) {
             return 0;
         } else {
            return -1;
        }
    }

    /// Implements the [characteristics][Spliterator#characteristics] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO][BreakableSpliterator#SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be constructed using the iterable builder:
    /// ```java
    ///     Spliterator<Integer> iterator = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO)
    ///         .build().spliterator();
    /// ```
    /// @return a representation of characteristics, or possibly zero if the spliterator is broken.
    @Override
    public int characteristics() {
        if (hasBreak(SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO)) {
                return 0;
        } else {
            return characteristics;
        }
    }

    /// Implements the [hasCharacteristics][Spliterator#hasCharacteristics] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following collection breaks:
    /// - [SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE][BreakableSpliterator#SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be constructed using the iterable builder:
    /// ```java
    ///     Spliterator<Integer> iterator = Breakable.iterableOf(1,2,3,4,5)
    ///         .withBreak(SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO)
    ///         .build().spliterator();
    /// ```
    /// @return a representation of characteristics, or possibly zero if the spliterator is broken.
    @Override
    public boolean hasCharacteristics(int characteristics) {
        if (hasBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE)) {
            return true;
        } else if (hasBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE)) {
            return false;
        } else {
            return (characteristics() & characteristics) == characteristics;
        }
    }

    @Override
    public Comparator<? super E> getComparator() {
        if (hasBreak(SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL)) {
            return null;
        } else {
            return iterator.getComparator();
        }
    }
}
