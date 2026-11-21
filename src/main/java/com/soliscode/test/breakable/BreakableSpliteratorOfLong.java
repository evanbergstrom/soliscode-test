/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;

/// A [Spliterator.OfLong] that can be broken in well-defined ways: a [BreakableSpliterator] of `Long` that is
/// also a spliterator of `long` values.
///
/// # Breaks
/// It has no breaks of its own. Its `long` traversal methods go through the boxed ones it inherits, so each
/// [BreakableSpliterator] break reaches them as it reaches the boxed ones; a `null` action is handled first, by
/// the `..._IGNORES_NULL_ACTION` breaks, as the boxed methods handle it. One reaches them otherwise: under
/// `SPLITERATOR_FIRST_ELEMENT_IS_NULL`, the primitive methods throw [NullPointerException] at the first element,
/// since a primitive value cannot be `null`, and do not call the action. [trySplit()][#trySplit()] returns a
/// `Spliterator.OfLong`.
///
/// Like [BreakableSpliterator], it is not thread safe.
///
/// @since 0.1.3
/// @see Spliterator.OfLong
public class BreakableSpliteratorOfLong extends BreakableSpliterator<Long> implements Spliterator.OfLong {

    /// Constructs a breakable `long` spliterator from a spliterator that will provide the implementation.
    ///
    /// @param spliterator the spliterator that provides the implementation
    /// @param breaks the breaks that define how it is broken, copied
    /// @param methodStatuses the status of each method in the interface
    /// @param characteristics the characteristics it reports
    /// @param isSafe whether it is safe for concurrent access
    public BreakableSpliteratorOfLong(final Spliterator.OfLong spliterator, final Set<Break> breaks,
                                    final Map<InterfaceMethod, MethodStatus> methodStatuses,
                                    final int characteristics, final boolean isSafe) {
        super(spliterator, breaks, methodStatuses, characteristics, isSafe);
    }

    /// Implements [tryAdvance(LongConsumer)][Spliterator.OfLong#tryAdvance(LongConsumer)] through the boxed
    /// [tryAdvance(Consumer)][#tryAdvance(Consumer)], with its breaks.
    ///
    /// @param action the action
    /// @return `false` if no remaining element existed, else `true`
    @Override
    public boolean tryAdvance(final LongConsumer action) {
        if (action == null && hasBreak(SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION)) {
            return false;
        }
        Objects.requireNonNull(action);
        return tryAdvance((Consumer<Long>) action::accept);
    }

    /// Implements [forEachRemaining(LongConsumer)][Spliterator.OfLong#forEachRemaining(LongConsumer)] through
    /// the boxed [forEachRemaining(Consumer)][#forEachRemaining(Consumer)], with its breaks.
    ///
    /// @param action the action
    @Override
    public void forEachRemaining(final LongConsumer action) {
        if (action == null && hasBreak(SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION)) {
            return;
        }
        Objects.requireNonNull(action);
        forEachRemaining((Consumer<Long>) action::accept);
    }

    /// Implements [trySplit()][Spliterator.OfLong#trySplit()] as [BreakableSpliterator#trySplit()] does, returning a
    /// `Spliterator.OfLong`.
    ///
    /// @return a part, or `null`
    @Override
    public Spliterator.@Nullable OfLong trySplit() {
        return (Spliterator.OfLong) super.trySplit();
    }

    @Override
    Spliterator<Long> emptySpliterator() {
        return Spliterators.emptyLongSpliterator();
    }

    @Override
    Spliterator<Long> unsized(final Spliterator<Long> part) {
        return new BreakableSpliteratorOfLong((Spliterator.OfLong) part, Set.of(), Map.of(),
                part.characteristics() & ~(SIZED | SUBSIZED), false);
    }
}
