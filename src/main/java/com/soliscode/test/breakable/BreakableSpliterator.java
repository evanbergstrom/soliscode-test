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

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
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
    /// This is not a fault under [Spliterator#trySplit()], which may return `null` for any reason, so no contract
    /// fails for it; it is for testing code that assumes a spliterator splits.
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

    /// The spliterator ignores a `null` action given to [tryAdvance(Consumer)][#tryAdvance(Consumer)]: it returns
    /// `false` instead of throwing [NullPointerException] ([Spliterator#tryAdvance(Consumer)]).
    ///
    /// **Affected Methods:** [tryAdvance(Consumer)][#tryAdvance(Consumer)], with a `null` action only
    public static final Break SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION =
            new Break("spliterator tryAdvance ignores a null action");

    /// The spliterator swallows an exception the action throws in [tryAdvance(Consumer)][#tryAdvance(Consumer)]:
    /// it catches a [RuntimeException] and returns `true` (there was an element), instead of relaying it.
    ///
    /// **Affected Methods:** [tryAdvance(Consumer)][#tryAdvance(Consumer)], where the action throws
    public static final Break SPLITERATOR_TRY_ADVANCE_SWALLOWS_ACTION_EXCEPTION =
            new Break("spliterator tryAdvance swallows an exception the action throws");

    /// The spliterator ignores a `null` action given to
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]: it returns instead of throwing
    /// [NullPointerException].
    ///
    /// **Affected Methods:** [forEachRemaining(Consumer)][#forEachRemaining(Consumer)], with a `null` action only
    public static final Break SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION =
            new Break("spliterator forEachRemaining ignores a null action");

    /// The spliterator swallows an exception the action throws in
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]: it catches a [RuntimeException] and returns,
    /// instead of relaying it.
    ///
    /// **Affected Methods:** [forEachRemaining(Consumer)][#forEachRemaining(Consumer)], where the action throws
    public static final Break SPLITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION =
            new Break("spliterator forEachRemaining swallows an exception the action throws");

    /// The spliterator passes its first element twice. The traversal step after the one that passed it passes it
    /// again before going on: the next [tryAdvance(Consumer)][#tryAdvance(Consumer)] passes it instead of
    /// advancing, or [forEachRemaining(Consumer)][#forEachRemaining(Consumer)] passes it before the rest. A
    /// `forEachRemaining` at the start passes it twice, then the rest. No one `tryAdvance` passes two elements. A
    /// `DISTINCT` spliterator then encounters equal elements.
    ///
    /// **Affected Methods:** [tryAdvance(Consumer)][#tryAdvance(Consumer)],
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    public static final Break SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE =
            new Break("spliterator returns its first element twice");

    /// The spliterator passes `null` in place of its first element: the first traversal step, a
    /// [tryAdvance(Consumer)][#tryAdvance(Consumer)] or a [forEachRemaining(Consumer)][#forEachRemaining(Consumer)],
    /// passes `null` to the action where it would pass the first element. The rest are unchanged. A `NONNULL`
    /// spliterator then encounters `null`.
    ///
    /// **Affected Methods:** [tryAdvance(Consumer)][#tryAdvance(Consumer)],
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)]
    public static final Break SPLITERATOR_FIRST_ELEMENT_IS_NULL =
            new Break("spliterator passes null in place of its first element");

    /// The spliterator's [trySplit()][#trySplit()] returns the suffix, not the prefix: where its delegate splits off
    /// a part, the spliterator keeps that part and returns the rest. An `ORDERED` spliterator's part must be a strict
    /// prefix ([Spliterator#trySplit()]).
    ///
    /// **Affected Methods:** [trySplit()][#trySplit()]
    public static final Break SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX =
            new Break("spliterator trySplit returns the suffix and keeps the prefix");

    /// The spliterator's [trySplit()][#trySplit()] loses the part it splits off: its delegate splits, and it returns
    /// `null`, so the part's elements are covered by neither.
    ///
    /// **Affected Methods:** [trySplit()][#trySplit()]
    public static final Break SPLITERATOR_TRY_SPLIT_LOSES_PART =
            new Break("spliterator trySplit loses the part it splits off");

    /// The spliterator's [trySplit()][#trySplit()] never returns `null`: where its delegate returns `null`, it
    /// returns an empty spliterator, so repeated calls never end ([Spliterator#trySplit()]: they "must eventually
    /// return `null`").
    ///
    /// **Affected Methods:** [trySplit()][#trySplit()]
    public static final Break SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL =
            new Break("spliterator trySplit never returns null");

    /// The part [trySplit()][#trySplit()] returns does not report `SIZED` or `SUBSIZED`: it is wrapped in a
    /// spliterator with no breaks whose characteristics are the part's without them. A `SUBSIZED` spliterator's
    /// parts must be `SIZED` and `SUBSIZED`.
    ///
    /// **Affected Methods:** [trySplit()][#trySplit()] (the part it returns)
    public static final Break SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED =
            new Break("spliterator trySplit returns a part that is not SIZED");

    /// After a [trySplit()][#trySplit()] that returns a part, [estimateSize()][#estimateSize()] returns the
    /// estimate from before the split, plus one (or `Long.MAX_VALUE`, where that was the estimate), from then on.
    /// After a split, neither part may estimate more than the spliterator did before ([Spliterator#trySplit()]).
    ///
    /// **Affected Methods:** [estimateSize()][#estimateSize()], after a split
    public static final Break SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT =
            new Break("spliterator estimateSize grows after a split");

    /// Once traversal has begun (a [tryAdvance(Consumer)][#tryAdvance(Consumer)] or a
    /// [forEachRemaining(Consumer)][#forEachRemaining(Consumer)] has been called),
    /// [characteristics()][#characteristics()] returns zero. Repeated calls before or between splits should return
    /// the same result.
    ///
    /// **Affected Methods:** [characteristics()][#characteristics()], and
    /// [hasCharacteristics(int)][#hasCharacteristics(int)], which reads it
    public static final Break SPLITERATOR_CHARACTERISTICS_CHANGE_AFTER_TRAVERSAL =
            new Break("spliterator characteristics change after traversal begins");

    /// [characteristics()][#characteristics()] adds `IMMUTABLE` to the characteristics, though a change to the
    /// source may reach the spliterator.
    ///
    /// **Affected Methods:** [characteristics()][#characteristics()], and
    /// [hasCharacteristics(int)][#hasCharacteristics(int)], which reads it
    public static final Break SPLITERATOR_REPORTS_IMMUTABLE =
            new Break("spliterator reports IMMUTABLE");

    /// [characteristics()][#characteristics()] adds `CONCURRENT` to the characteristics, though the source may not
    /// be safely changed during traversal.
    ///
    /// **Affected Methods:** [characteristics()][#characteristics()], and
    /// [hasCharacteristics(int)][#hasCharacteristics(int)], which reads it
    public static final Break SPLITERATOR_REPORTS_CONCURRENT =
            new Break("spliterator reports CONCURRENT");

    /// [getComparator()][#getComparator()] returns [Comparator#naturalOrder()] where its delegate returns `null`: a
    /// spliterator `SORTED` in natural order must return `null` ([Spliterator#getComparator()]).
    ///
    /// **Affected Methods:** [getComparator()][#getComparator()], where the delegate returns `null`
    public static final Break SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER =
            new Break("spliterator getComparator returns natural order instead of null");

    /// [characteristics()][#characteristics()] leaves out `ORDERED`: a spliterator that reports `SORTED` must also
    /// report `ORDERED`. [getExactSizeIfKnown()][#getExactSizeIfKnown()] is not changed.
    ///
    /// **Affected Methods:** [characteristics()][#characteristics()], and
    /// [hasCharacteristics(int)][#hasCharacteristics(int)], which reads it
    public static final Break SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_ORDERED =
            new Break("spliterator characteristics leave out ORDERED");

    /// [characteristics()][#characteristics()] leaves out `SIZED`: a spliterator that reports `SUBSIZED` must also
    /// report `SIZED`. [getExactSizeIfKnown()][#getExactSizeIfKnown()] is not changed.
    ///
    /// **Affected Methods:** [characteristics()][#characteristics()], and
    /// [hasCharacteristics(int)][#hasCharacteristics(int)], which reads it
    public static final Break SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_SIZED =
            new Break("spliterator characteristics leave out SIZED");

    /// [getComparator()][#getComparator()] always throws [IllegalStateException], as it must only where the
    /// spliterator is not `SORTED`.
    ///
    /// **Affected Methods:** [getComparator()][#getComparator()]
    public static final Break SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS =
            new Break("spliterator getComparator always throws IllegalStateException");

    /// The underlying spliterator.
    ///
    /// Not final: [#SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX] keeps the part its delegate splits off.
    private Spliterator<E> iterator;

    /// The characteristics this spliterator reports that its delegate did not, when it was made.
    private final int added;

    /// The characteristics its delegate reported, when this spliterator was made, that this one does not.
    private final int removed;

    /// Whether traversal has started, so that the first-element breaks reach one element only.
    private boolean started;

    /// Under SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE, the first element, once a tryAdvance has passed it and before
    /// the next traversal step passes it again.
    private @Nullable E repeat;

    /// Whether [#repeat] is waiting to be passed again.
    private boolean repeatPending;

    /// Under SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT, the estimate after a split, or -1 before one.
    private long grownEstimate = -1;

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
        int own = iteratorArg.characteristics();
        this.added = characteristicsArg & ~own;
        this.removed = own & ~characteristicsArg;
    }

    /// Implements the [forEachRemaining][Spliterator#forEachRemaining] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION][#SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION]
    /// - [SPLITERATOR_SKIPS_FIRST_ELEMENT][#SPLITERATOR_SKIPS_FIRST_ELEMENT]
    /// - [SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION][#SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION]
    /// - [#SPLITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION]
    /// - [SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE][#SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE]
    /// - [SPLITERATOR_FIRST_ELEMENT_IS_NULL][#SPLITERATOR_FIRST_ELEMENT_IS_NULL]
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
        if (action == null && hasBreak(SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION)) {
            return;
        }
        boolean first = !started;
        skipFirstIfBroken();
        if (hasBreak(SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)) {
            return;
        }
        Consumer<? super E> act = action;
        if (action != null && hasBreak(SPLITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION)) {
            act = e -> {
                try {
                    action.accept(e);
                } catch (final RuntimeException thrown) {
                    throw new SwallowedException(thrown);
                }
            };
        }
        try {
            remaining(act, first);
        } catch (final SwallowedException ignored) {
            // SPLITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION: the action's exception ends the traversal,
            // and is not relayed.
        }
    }

    /// The traversal of `forEachRemaining`, with the breaks that change which elements it passes.
    ///
    /// @param action the action
    /// @param first whether this is the first traversal step
    private void remaining(final Consumer<? super E> action, final boolean first) {
        if (repeatPending) {
            repeatPending = false;
            acceptRepeat(action);
        } else if (first && hasBreak(SPLITERATOR_FIRST_ELEMENT_IS_NULL)) {
            iterator.tryAdvance(e -> acceptNull(action));
        } else if (first && hasBreak(SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE)) {
            iterator.tryAdvance(e -> {
                action.accept(e);
                action.accept(e);
            });
        }
        iterator.forEachRemaining(action);
    }

    /// Carries an exception the action threw out of a traversal, to be swallowed by a break.
    private static final class SwallowedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        SwallowedException(final RuntimeException cause) {
            super(cause);
        }
    }

    /// Passes `null` to the action, for SPLITERATOR_FIRST_ELEMENT_IS_NULL.
    ///
    /// @param action the action
    // NullAway: SPLITERATOR_FIRST_ELEMENT_IS_NULL passes null in place of an element, on purpose.
    @SuppressWarnings("NullAway")
    private void acceptNull(final Consumer<? super E> action) {
        action.accept(null);
    }

    /// Passes the first element again, for SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE.
    ///
    /// @param action the action
    private void acceptRepeat(final Consumer<? super E> action) {
        E e = repeat;
        if (e != null) {
            action.accept(e);
        } else {
            acceptNull(action);
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
    /// - [SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX][#SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX]
    /// - [SPLITERATOR_TRY_SPLIT_LOSES_PART][#SPLITERATOR_TRY_SPLIT_LOSES_PART]
    /// - [SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL][#SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL]
    /// - [SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED][#SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED]
    /// - [SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT][#SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT] (records the
    ///   estimate)
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
        }
        long before = hasBreak(SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT) ? estimateSize() : -1;
        Spliterator<E> part = iterator.trySplit();
        if (part == null) {
            return hasBreak(SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL) ? emptySpliterator() : null;
        } else if (hasBreak(SPLITERATOR_TRY_SPLIT_LOSES_PART)) {
            return null;
        }
        if (hasBreak(SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT)) {
            grownEstimate = before == Long.MAX_VALUE ? before : before + 1;
        }
        if (hasBreak(SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX)) {
            Spliterator<E> rest = iterator;
            iterator = part;
            part = rest;
        }
        return hasBreak(SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED) ? unsized(part) : part;
    }

    /// Returns an empty spliterator of this kind, for SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL. A primitive
    /// breakable returns a primitive one, so that its `trySplit` keeps its type.
    ///
    /// @return an empty spliterator
    Spliterator<E> emptySpliterator() {
        return Spliterators.emptySpliterator();
    }

    /// Returns a part wrapped so that it reports its characteristics without `SIZED` and `SUBSIZED`, with no breaks,
    /// for SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED. A primitive breakable returns a primitive one.
    ///
    /// @param part the part
    /// @return the wrapped part
    Spliterator<E> unsized(final Spliterator<E> part) {
        return new BreakableSpliterator<>(part, Set.of(), Map.of(), part.characteristics() & ~(SIZED | SUBSIZED),
                false);
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
    /// - [SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION][#SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION]
    /// - [SPLITERATOR_TRY_ADVANCE_SWALLOWS_ACTION_EXCEPTION][#SPLITERATOR_TRY_ADVANCE_SWALLOWS_ACTION_EXCEPTION]
    /// - [SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE][#SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE]
    /// - [SPLITERATOR_FIRST_ELEMENT_IS_NULL][#SPLITERATOR_FIRST_ELEMENT_IS_NULL]
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
        if (action == null && hasBreak(SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION)) {
            return false;
        } else if (hasBreak(SPLITERATOR_IS_ALWAYS_EMPTY)) {
            return false;
        }
        Consumer<? super E> act = action;
        if (action != null && hasBreak(SPLITERATOR_TRY_ADVANCE_SWALLOWS_ACTION_EXCEPTION)) {
            // The element is passed, and the spliterator advances, so tryAdvance returns true.
            act = e -> {
                try {
                    action.accept(e);
                } catch (final RuntimeException ignored) {
                    // SPLITERATOR_TRY_ADVANCE_SWALLOWS_ACTION_EXCEPTION: not relayed.
                }
            };
        }
        return advance(act);
    }

    /// One `tryAdvance`, with the breaks that change which element it passes and what it returns.
    ///
    /// @param action the action
    /// @return whether there was an element
    private boolean advance(final Consumer<? super E> action) {
        if (repeatPending) {
            repeatPending = false;
            acceptRepeat(action);
            return true;
        }
        boolean first = !started;
        skipFirstIfBroken();
        Consumer<? super E> target = action;
        if (first && hasBreak(SPLITERATOR_FIRST_ELEMENT_IS_NULL)) {
            target = e -> acceptNull(action);
        } else if (first && hasBreak(SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE)) {
            target = e -> {
                repeat = e;
                repeatPending = true;
                action.accept(e);
            };
        }
        if (hasBreak(SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION)) {
            return iterator.tryAdvance((e) -> { });
        } else if (hasBreak(SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE)) {
            iterator.tryAdvance(target);
            return true;
        } else if (hasBreak(SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE)) {
            iterator.tryAdvance(target);
            return false;
        } else {
            return iterator.tryAdvance(target);
        }
    }

    /// Implements the [estimateSize][Spliterator#estimateSize] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_IS_ALWAYS_EMPTY][#SPLITERATOR_IS_ALWAYS_EMPTY]
    /// - [SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE][#SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE]
    /// - [SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO][#SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO]
    /// - [SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT][#SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT]
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
        } else if (grownEstimate >= 0) {
            return grownEstimate;
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
    /// With no break, it is the delegate's `estimateSize()` where this spliterator's characteristics, as they follow
    /// its delegate's, have `SIZED`, and -1 where not: the JDK default's formula, read without the characteristics
    /// breaks, which do not reach it.
    ///
    /// @return the exact size, if known, else -1, or another value if it is broken.
    @Override
    public long getExactSizeIfKnown() {
        if (hasBreak(SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE)) {
            return -1;
        } else if (hasBreak(SPLITERATOR_IS_ALWAYS_EMPTY)) {
            return 0;
        } else if ((tracked() & SIZED) == SIZED) {
            return iterator.estimateSize();
        } else {
            return -1;
        }
    }

    /// Implements the [characteristics][Spliterator#characteristics] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO][#SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO]
    /// - [#SPLITERATOR_CHARACTERISTICS_CHANGE_AFTER_TRAVERSAL]
    /// - [SPLITERATOR_REPORTS_IMMUTABLE][#SPLITERATOR_REPORTS_IMMUTABLE]
    /// - [SPLITERATOR_REPORTS_CONCURRENT][#SPLITERATOR_REPORTS_CONCURRENT]
    /// - [SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_ORDERED][#SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_ORDERED]
    /// - [SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_SIZED][#SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_SIZED]
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
        } else if (started && hasBreak(SPLITERATOR_CHARACTERISTICS_CHANGE_AFTER_TRAVERSAL)) {
            return 0;
        }
        int reported = tracked();
        if (hasBreak(SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_ORDERED)) {
            reported &= ~ORDERED;
        }
        if (hasBreak(SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_SIZED)) {
            reported &= ~SIZED;
        }
        if (hasBreak(SPLITERATOR_REPORTS_IMMUTABLE)) {
            reported |= IMMUTABLE;
        }
        if (hasBreak(SPLITERATOR_REPORTS_CONCURRENT)) {
            reported |= CONCURRENT;
        }
        return reported;
    }

    /// The characteristics this spliterator reports with no break: its delegate's, now, with those it was made to add
    /// and take away. They follow the delegate's: a `TreeMap`'s key spliterator stops reporting `SIZED` once it has
    /// split, and so does this one.
    ///
    /// @return the characteristics
    private int tracked() {
        return (iterator.characteristics() & ~removed) | added;
    }

    /// Implements the [hasCharacteristics][Spliterator#hasCharacteristics] method from the [Spliterator] interface.
    ///
    /// # Breaks
    /// This method can be broken using the following breaks:
    /// - [SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE][#SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE]
    /// - [SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE][#SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE]
    /// - [SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO][#SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO]
    /// - [#SPLITERATOR_CHARACTERISTICS_CHANGE_AFTER_TRAVERSAL], [#SPLITERATOR_REPORTS_IMMUTABLE],
    ///   [#SPLITERATOR_REPORTS_CONCURRENT], [#SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_ORDERED] and
    ///   [#SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_SIZED], through [characteristics()][#characteristics()]
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
    /// - [SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER][#SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER]
    /// - [SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS][#SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS]
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
        } else if (hasBreak(SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS)) {
            throw new IllegalStateException("broken: SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS");
        }
        Comparator<? super E> comparator = iterator.getComparator();
        if (comparator == null && hasBreak(SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER)) {
            return naturalOrder();
        }
        return comparator;
    }

    /// Natural order, for SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER: where the delegate's comparator is `null`,
    /// its elements are sorted in natural order, so they are [Comparable].
    ///
    /// @return a comparator in natural order: the reverse of reverse natural order, which `Collections` gives as
    ///         natural order, with no cast here
    private Comparator<? super E> naturalOrder() {
        return Collections.reverseOrder(Collections.<E>reverseOrder());
    }
}
