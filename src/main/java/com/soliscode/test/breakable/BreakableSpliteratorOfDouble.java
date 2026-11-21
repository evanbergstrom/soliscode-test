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
import java.util.function.DoubleConsumer;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;

/// A [Spliterator.OfDouble] that can be broken in well-defined ways: a [BreakableSpliterator] of `Double` that is
/// also a spliterator of `double` values.
///
/// # Breaks
/// It has no breaks of its own. Its `double` traversal methods go through the boxed ones it inherits, so each
/// [BreakableSpliterator] break reaches them as it reaches the boxed ones; a `null` action is handled first, by
/// the `..._IGNORES_NULL_ACTION` breaks, as the boxed methods handle it. One reaches them otherwise: under
/// `SPLITERATOR_FIRST_ELEMENT_IS_NULL`, the primitive methods throw [NullPointerException] at the first element,
/// since a primitive value cannot be `null`, and do not call the action. [trySplit()][#trySplit()] returns a
/// `Spliterator.OfDouble`.
///
/// Like [BreakableSpliterator], it is not thread safe.
///
/// @since 0.1.3
/// @see Spliterator.OfDouble
public class BreakableSpliteratorOfDouble extends BreakableSpliterator<Double> implements Spliterator.OfDouble {

    /// Constructs a breakable `double` spliterator from a spliterator that will provide the implementation.
    ///
    /// @param spliterator the spliterator that provides the implementation
    /// @param breaks the breaks that define how it is broken, copied
    /// @param methodStatuses the status of each method in the interface
    /// @param characteristics the characteristics it reports
    /// @param isSafe whether it is safe for concurrent access
    public BreakableSpliteratorOfDouble(final Spliterator.OfDouble spliterator, final Set<Break> breaks,
                                    final Map<InterfaceMethod, MethodStatus> methodStatuses,
                                    final int characteristics, final boolean isSafe) {
        super(spliterator, breaks, methodStatuses, characteristics, isSafe);
    }

    /// Implements [tryAdvance(DoubleConsumer)][Spliterator.OfDouble#tryAdvance(DoubleConsumer)] through the boxed
    /// [tryAdvance(Consumer)][#tryAdvance(Consumer)], with its breaks.
    ///
    /// @param action the action
    /// @return `false` if no remaining element existed, else `true`
    @Override
    public boolean tryAdvance(final DoubleConsumer action) {
        if (action == null && hasBreak(SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION)) {
            return false;
        }
        Objects.requireNonNull(action);
        return tryAdvance((Consumer<Double>) action::accept);
    }

    /// Implements [forEachRemaining(DoubleConsumer)][Spliterator.OfDouble#forEachRemaining(DoubleConsumer)] through
    /// the boxed [forEachRemaining(Consumer)][#forEachRemaining(Consumer)], with its breaks.
    ///
    /// @param action the action
    @Override
    public void forEachRemaining(final DoubleConsumer action) {
        if (action == null && hasBreak(SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION)) {
            return;
        }
        Objects.requireNonNull(action);
        forEachRemaining((Consumer<Double>) action::accept);
    }

    /// Implements [trySplit()][Spliterator.OfDouble#trySplit()] as [BreakableSpliterator#trySplit()] does, returning a
    /// `Spliterator.OfDouble`.
    ///
    /// @return a part, or `null`
    @Override
    public Spliterator.@Nullable OfDouble trySplit() {
        return (Spliterator.OfDouble) super.trySplit();
    }

    @Override
    Spliterator<Double> emptySpliterator() {
        return Spliterators.emptyDoubleSpliterator();
    }

    @Override
    Spliterator<Double> unsized(final Spliterator<Double> part) {
        return new BreakableSpliteratorOfDouble((Spliterator.OfDouble) part, Set.of(), Map.of(),
                part.characteristics() & ~(SIZED | SUBSIZED), false);
    }
}
