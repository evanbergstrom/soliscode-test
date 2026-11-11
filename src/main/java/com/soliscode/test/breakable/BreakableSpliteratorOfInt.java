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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;

/// A [Spliterator.OfInt] that can be broken in well-defined ways: a [BreakableSpliterator] of `Integer` that is
/// also a spliterator of `int` values.
///
/// # Breaks
/// It has no breaks of its own. Its `int` traversal methods go through the boxed ones it inherits, so each
/// [BreakableSpliterator] break reaches them as it reaches the boxed ones; a `null` action is handled first, by
/// the `..._IGNORES_NULL_ACTION` breaks, as the boxed methods handle it. One reaches them otherwise: under
/// `SPLITERATOR_FIRST_ELEMENT_IS_NULL`, the primitive methods throw [NullPointerException] at the first element,
/// since a primitive value cannot be `null`, and do not call the action. [trySplit()][#trySplit()] returns a
/// `Spliterator.OfInt`.
///
/// Like [BreakableSpliterator], it is not thread safe.
///
/// @since 0.1.3
/// @see Spliterator.OfInt
public class BreakableSpliteratorOfInt extends BreakableSpliterator<Integer> implements Spliterator.OfInt {

    /// Constructs a breakable `int` spliterator from a spliterator that will provide the implementation.
    ///
    /// @param spliterator the spliterator that provides the implementation
    /// @param breaks the breaks that define how it is broken
    /// @param methodStatuses the status of each method in the interface
    /// @param characteristics the characteristics it reports
    /// @param isSafe whether it is safe for concurrent access
    public BreakableSpliteratorOfInt(final Spliterator.OfInt spliterator, final Set<Break> breaks,
                                    final Map<InterfaceMethod, MethodStatus> methodStatuses,
                                    final int characteristics, final boolean isSafe) {
        super(spliterator, breaks, methodStatuses, characteristics, isSafe);
    }

    /// Implements [tryAdvance(IntConsumer)][Spliterator.OfInt#tryAdvance(IntConsumer)] through the boxed
    /// [tryAdvance(Consumer)][#tryAdvance(Consumer)], with its breaks.
    ///
    /// @param action the action
    /// @return `false` if no remaining element existed, else `true`
    @Override
    public boolean tryAdvance(final IntConsumer action) {
        if (action == null && hasBreak(SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION)) {
            return false;
        }
        Objects.requireNonNull(action);
        return tryAdvance((Consumer<Integer>) action::accept);
    }

    /// Implements [forEachRemaining(IntConsumer)][Spliterator.OfInt#forEachRemaining(IntConsumer)] through
    /// the boxed [forEachRemaining(Consumer)][#forEachRemaining(Consumer)], with its breaks.
    ///
    /// @param action the action
    @Override
    public void forEachRemaining(final IntConsumer action) {
        if (action == null && hasBreak(SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION)) {
            return;
        }
        Objects.requireNonNull(action);
        forEachRemaining((Consumer<Integer>) action::accept);
    }

    /// Implements [trySplit()][Spliterator.OfInt#trySplit()] as [BreakableSpliterator#trySplit()] does, returning a
    /// `Spliterator.OfInt`.
    ///
    /// @return a part, or `null`
    @Override
    public Spliterator.@Nullable OfInt trySplit() {
        return (Spliterator.OfInt) super.trySplit();
    }

    @Override
    Spliterator<Integer> emptySpliterator() {
        return Spliterators.emptyIntSpliterator();
    }

    @Override
    Spliterator<Integer> unsized(final Spliterator<Integer> part) {
        return new BreakableSpliteratorOfInt((Spliterator.OfInt) part, Set.of(), Map.of(),
                part.characteristics() & ~(SIZED | SUBSIZED), false);
    }
}
