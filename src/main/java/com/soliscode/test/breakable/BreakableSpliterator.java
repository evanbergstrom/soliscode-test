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
package com.soliscode.test.breakable;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;

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

    /// The spliterator reports no elements to [tryAdvance(Consumer)][#tryAdvance(Consumer)] and no size, and does
    /// not split. [forEachRemaining(Consumer)][#forEachRemaining(Consumer)] and
    /// [characteristics()][#characteristics()] are not changed: `forEachRemaining` still passes every element to
    /// the action.
    ///
    /// **Affected Methods:** [tryAdvance(Consumer)][#tryAdvance(Consumer)] (returns `false`),
    /// [trySplit()][#trySplit()] (returns `null`), [estimateSize()][#estimateSize()] (returns zero),
    /// [getExactSizeIfKnown()][#getExactSizeIfKnown()] (returns zero)
    public static final Break SPLITERATOR_IS_ALWAYS_EMPTY =
            new Break("spliterator always has no elements");

    /// The spliterator skips the first element: the first call to [tryAdvance(Consumer)][#tryAdvance(Consumer)] or
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)] drops it without passing it to the action.
    ///
    /// **Affected Methods:** [tryAdvance(Consumer)][#tryAdvance(Consumer)],
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    public static final Break SPLITERATOR_SKIPS_FIRST_ELEMENT =
            new Break("spliterator skips the first element");

    /// The 'forEachRemaining' method on the spliterator does not call the action.
    ///
    /// **Affected Methods:** [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    /// @see BreakableSpliterator#forEachRemaining(Consumer)
    public static final Break SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION =
            new Break("spliterator forEachRemaining does not call action");

    /// The 'trySplit' method on the spliterator always returns `null`.
    ///
    /// **Affected Methods:** [trySplit()][#trySplit()]
    /// @see BreakableSpliterator#trySplit()
    public static final Break SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL =
            new Break("spliterator trySplit always returns null");

    /// The 'tryAdvance' method on the spliterator does not call the action; it still advances past the element and
    /// returns whether there was one.
    ///
    /// **Affected Methods:** [tryAdvance(Consumer)][#tryAdvance(Consumer)]
    /// @see BreakableSpliterator#tryAdvance(Consumer)
    public static final Break SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION =
            new Break("spliterator tryAdvance does not call action");

    /// The 'tryAdvance' method always returns true.
    ///
    /// **Affected Methods:** [tryAdvance(Consumer)][#tryAdvance(Consumer)]
    /// @see BreakableSpliterator#tryAdvance(Consumer)
    public static final Break SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE =
            new Break("spliterator tryAdvance always returns true");

    /// The 'tryAdvance' method always returns false.
    ///
    /// **Affected Methods:** [tryAdvance(Consumer)][#tryAdvance(Consumer)]
    /// @see BreakableSpliterator#tryAdvance(Consumer)
    public static final Break SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE =
            new Break("spliterator tryAdvance always returns false");

    /// The `estimateSize` method always returns the max value.
    ///
    /// **Affected Methods:** [estimateSize()][#estimateSize()]
    /// @see BreakableSpliterator#estimateSize()
    public static final Break SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE =
            new Break("spliterator estimateSize always returns the max value");

    /// The `estimateSize` method always returns zero.
    ///
    /// **Affected Methods:** [estimateSize()][#estimateSize()]
    /// @see BreakableSpliterator#estimateSize()
    public static final Break SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO =
            new Break("spliterator estimateSize always returns zero");

    /// The `getExactSizeIfKnown` method always returns negative one (-1).
    ///
    /// **Affected Methods:** [getExactSizeIfKnown()][#getExactSizeIfKnown()]
    /// @see BreakableSpliterator#getExactSizeIfKnown()
    public static final Break SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE =
            new Break("spliterator getExactSizeIfKnown always returns -1");

    /// The `characteristics` method always returns zero (0). [hasCharacteristics(int)][#hasCharacteristics(int)],
    /// which reads `characteristics()`, then returns `true` only for an argument of zero.
    ///
    /// **Affected Methods:** [characteristics()][#characteristics()],
    /// [hasCharacteristics(int)][#hasCharacteristics(int)]
    /// @see BreakableSpliterator#characteristics()
    public static final Break SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO =
            new Break("spliterator characteristics always returns zero");

    /// The `hasCharacteristics` method always returns `true`.
    ///
    /// **Affected Methods:** [hasCharacteristics(int)][#hasCharacteristics(int)]
    /// @see BreakableSpliterator#hasCharacteristics(int)
    public static final Break SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE =
            new Break("spliterator hasCharacteristics always returns true");

    /// The `hasCharacteristics` method always returns `false`.
    ///
    /// **Affected Methods:** [hasCharacteristics(int)][#hasCharacteristics(int)]
    /// @see BreakableSpliterator#hasCharacteristics(int)
    public static final Break SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE =
            new Break("spliterator hasCharacteristics always returns false");

    /// The `getComparator` method always return `null`.
    ///
    /// **Affected Methods:** [getComparator()][#getComparator()]
    /// @see BreakableSpliterator#getComparator()
    public static final Break SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL = new
            Break("spliterator getComparator always returns null");

    /// The underlying spliterator.
    private final Spliterator<E> iterator;

    /// The characteristics of the spliterator.
    private final int characteristics;

    /// Whether traversal has started, so that SPLITERATOR_SKIPS_FIRST_ELEMENT skips one element only.
    private boolean started;

    /// Constructs a breakable spliterator from a spliterator that will provide the implementation.
    /// @param iteratorArg The spliterator that will provide the implementation
    /// @param breaks The breaks that define how the iterator is broken.
    /// @param methodStatuses the status of each method in the interface.
    /// @param characteristicsArg A mask that indicates the characteristics of the iterator.
    /// @param isSafe Whether the spliterator is safe for concurrent access.
    public BreakableSpliterator(final Spliterator<E> iteratorArg, final Set<Break> breaks,
                                final Map<InterfaceMethod, MethodStatus> methodStatuses,
                                final int characteristicsArg, final boolean isSafe) {
        super(breaks, methodStatuses, isSafe);
        this.iterator = iteratorArg;
        this.characteristics = characteristicsArg;
    }

    /// Implements the [forEachRemaining][Spliterator#forEachRemaining] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION][#SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION]
    /// - [SPLITERATOR_SKIPS_FIRST_ELEMENT][#SPLITERATOR_SKIPS_FIRST_ELEMENT]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be obtained from a breakable iterable built with it:
    /// ```java
    ///     Spliterator<Integer> spliterator = Breakables.buildIterable(1, 2, 3, 4, 5)
    ///         .addBreak(SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)
    ///         .build().spliterator();
    /// ```
    /// @param action The action to run on the remaining elements.
    @Override
    public void forEachRemaining(final Consumer<? super E> action) {
        skipFirstIfBroken();
        if (!hasBreak(SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)) {
            iterator.forEachRemaining(action);
        }
    }

    /// Drops the first element, once, when the spliterator has the SPLITERATOR_SKIPS_FIRST_ELEMENT break.
    private void skipFirstIfBroken() {
        if (!started) {
            started = true;
            if (hasBreak(SPLITERATOR_SKIPS_FIRST_ELEMENT)) {
                iterator.tryAdvance((e) -> { });
            }
        }
    }

    /// Implements the [trySplit][Spliterator#trySplit] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL][#SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL]
    /// - [SPLITERATOR_IS_ALWAYS_EMPTY][#SPLITERATOR_IS_ALWAYS_EMPTY]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be obtained from a breakable iterable built with it:
    /// ```java
    ///     Spliterator<Integer> spliterator = Breakables.buildIterable(1, 2, 3, 4, 5)
    ///         .addBreak(SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL)
    ///         .build().spliterator();
    /// ```
    /// @return a spliterator over part of the elements, or `null` if this spliterator cannot be split or is broken.
    @Override
    public @Nullable Spliterator<E> trySplit() {
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
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_IS_ALWAYS_EMPTY][#SPLITERATOR_IS_ALWAYS_EMPTY]
    /// - [SPLITERATOR_SKIPS_FIRST_ELEMENT][#SPLITERATOR_SKIPS_FIRST_ELEMENT]
    /// - [SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION][#SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION]
    /// - [SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE][#SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE]
    /// - [SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE][#SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be obtained from a breakable iterable built with it:
    /// ```java
    ///     Spliterator<Integer> spliterator = Breakables.buildIterable(1, 2, 3, 4, 5)
    ///         .addBreak(SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION)
    ///         .build().spliterator();
    /// ```
    /// @param action The action to perform on the element
    /// @return `false` if no remaining elements existed upon entry to this method, else `true`.
    @Override
    public boolean tryAdvance(final Consumer<? super E> action) {
        if (hasBreak(SPLITERATOR_IS_ALWAYS_EMPTY)) {
            return false;
        }
        skipFirstIfBroken();
        if (hasBreak(SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION)) {
            return iterator.tryAdvance((e) -> { });
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
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_IS_ALWAYS_EMPTY][#SPLITERATOR_IS_ALWAYS_EMPTY]
    /// - [SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE][#SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE]
    /// - [SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO][#SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be obtained from a breakable iterable built with it:
    /// ```java
    ///     Spliterator<Integer> spliterator = Breakables.buildIterable(1, 2, 3, 4, 5)
    ///         .addBreak(SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE)
    ///         .build().spliterator();
    /// ```
    /// @return the estimated size, or zero if it is broken, or `Long.MAX_VALUE` if infinite, unknown, too expensive
    ///         to compute or broken.
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
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_IS_ALWAYS_EMPTY][#SPLITERATOR_IS_ALWAYS_EMPTY]
    /// - [#SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be obtained from a breakable iterable built with it:
    /// ```java
    ///     Spliterator<Integer> spliterator = Breakables.buildIterable(1, 2, 3, 4, 5)
    ///         .addBreak(SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE)
    ///         .build().spliterator();
    /// ```
    /// @return the exact size, if known, else -1, or another value if it is broken.
    @Override
    public long getExactSizeIfKnown() {
        if (hasBreak(SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE)) {
            return -1;
        } else if (hasBreak(SPLITERATOR_IS_ALWAYS_EMPTY)) {
            return 0;
        } else if ((characteristics & SIZED) == SIZED) {
            return iterator.getExactSizeIfKnown();
        } else {
            return -1;
        }
    }

    /// Implements the [characteristics][Spliterator#characteristics] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO][#SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be obtained from a breakable iterable built with it:
    /// ```java
    ///     Spliterator<Integer> spliterator = Breakables.buildIterable(1, 2, 3, 4, 5)
    ///         .addBreak(SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO)
    ///         .build().spliterator();
    /// ```
    /// @return a representation of characteristics, or possibly zero if the spliterator is broken.
    @SuppressWarnings("MagicConstant")
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
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE][#SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE]
    /// - [SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE][#SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE]
    /// - [SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO][#SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be obtained from a breakable iterable built with it:
    /// ```java
    ///     Spliterator<Integer> spliterator = Breakables.buildIterable(1, 2, 3, 4, 5)
    ///         .addBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE)
    ///         .build().spliterator();
    /// ```
    /// @param characteristicsToCheck the characteristics to check for.
    /// @return `true` if this spliterator reports all of the given characteristics, or a fixed value if it is
    ///         broken.
    @Override
    public boolean hasCharacteristics(final int characteristicsToCheck) {
        if (hasBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE)) {
            return true;
        } else if (hasBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE)) {
            return false;
        } else {
            return (characteristics() & characteristicsToCheck) == characteristicsToCheck;
        }
    }

    /// Implements the [getComparator][Spliterator#getComparator] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL][#SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL]
    ///
    /// # Example
    /// A spliterator that has any of these breaks can be obtained from a breakable iterable built with it:
    /// ```java
    ///     Spliterator<Integer> spliterator = Breakables.buildIterable(1, 2, 3, 4, 5)
    ///         .addBreak(SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL)
    ///         .build().spliterator();
    /// ```
    /// @return the comparator of the underlying spliterator, or `null` if it is broken.
    @Override
    public @Nullable Comparator<? super E> getComparator() {
        if (hasBreak(SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL)) {
            return null;
        } else {
            return iterator.getComparator();
        }
    }
}
