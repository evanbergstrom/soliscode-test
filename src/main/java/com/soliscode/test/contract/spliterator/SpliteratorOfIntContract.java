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
package com.soliscode.test.contract.spliterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.IntConsumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.provider.SpliteratorSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the [Spliterator.OfInt] interface**: [SpliteratorContract], and the `int` traversal methods,
/// [tryAdvance(IntConsumer)][Spliterator.OfInt#tryAdvance(IntConsumer)] and
/// [forEachRemaining(IntConsumer)][Spliterator.OfInt#forEachRemaining(IntConsumer)], with the parts of
/// [trySplit()][Spliterator.OfInt#trySplit()] traversed through them.
///
/// It reads them as [SpliteratorContract] reads their boxed forms. It does not test the `@implSpec` of the boxed
/// `tryAdvance(Consumer)` and `forEachRemaining(Consumer)` (whether an action that is also an `IntConsumer` is passed
/// through unboxed): that is the default methods' specification, not the interface's.
///
/// ## Usage Examples
/// ```java
/// class IntRangeSpliteratorTest extends AbstractTest
///         implements SpliteratorOfIntContract<Spliterator.OfInt> {
///     @Override
///     public ObjectProvider<Integer> elementProvider() {
///         return new IntegerProvider();
///     }
///
///     @Override
///     public SpliteratorProvider<Spliterator.OfInt, Integer> spliteratorProvider() {
///         return SpliteratorProviders.intRange();
///     }
/// }
/// ```
///
/// @param <S> the type of the spliterator under test
/// @since 0.1.3
/// @see Spliterator.OfInt
public interface SpliteratorOfIntContract<S extends Spliterator.OfInt> extends SpliteratorContract<S, Integer> {

    /// Tests that each `tryAdvance(IntConsumer)` passes one element and returns `true`, until every element has been
    /// passed once; and that the next returns `false` without calling the action.
    @Test
    @DisplayName("tryAdvance(IntConsumer) passes each element once, one per call, then returns false")
    default void intTryAdvancePassesEachElementOnceThenReturnsFalse() {
        SpliteratorSource<S, Integer> source = defaultSource();
        Spliterator.OfInt spliterator = source.spliterator();
        List<Integer> passed = new ArrayList<>();
        for (int calls = 1; calls <= TRAVERSAL_LIMIT; calls++) {
            List<Integer> thisCall = new ArrayList<>();
            boolean advanced = spliterator.tryAdvance((IntConsumer) thisCall::add);
            if (!advanced) {
                assertEquals(List.of(), thisCall, "the elements passed by the tryAdvance() that returned false");
                break;
            }
            assertEquals(1, thisCall.size(), "the elements passed by one tryAdvance() that returned true");
            passed.addAll(thisCall);
            assertTrue(calls < TRAVERSAL_LIMIT, "tryAdvance(IntConsumer) did not end");
        }
        assertSameElements(source.elements(), passed, documentsOrder(source), "the elements passed");
    }

    /// Tests that `tryAdvance((IntConsumer) null)` throws [NullPointerException]: with elements left, over an empty
    /// source, and at the end of a traversal.
    @Test
    @DisplayName("tryAdvance((IntConsumer) null) throws NullPointerException")
    default void intTryAdvanceOfNullThrowsNullPointerException() {
        Spliterator.OfInt withElements = defaultSource().spliterator();
        assertThrows(NullPointerException.class, () -> withElements.tryAdvance((IntConsumer) null),
                "with elements left");
        Spliterator.OfInt empty = sourceOf(List.of()).spliterator();
        assertThrows(NullPointerException.class, () -> empty.tryAdvance((IntConsumer) null), "over an empty source");
        Spliterator.OfInt atEnd = defaultSource().spliterator();
        atEnd.forEachRemaining((IntConsumer) e -> { });
        assertThrows(NullPointerException.class, () -> atEnd.tryAdvance((IntConsumer) null),
                "at the end of a traversal");
    }

    /// Tests that an exception the `IntConsumer` throws is relayed to the caller of `tryAdvance`.
    @Test
    @DisplayName("tryAdvance(IntConsumer) relays an exception the action throws")
    default void intTryAdvanceWithThrowingActionRelaysTheException() {
        Spliterator.OfInt spliterator = defaultSource().spliterator();
        RuntimeException thrown = new IllegalStateException("thrown by the action");
        RuntimeException caught = assertThrows(RuntimeException.class,
                () -> spliterator.tryAdvance((IntConsumer) e -> {
                    throw thrown;
                }));
        assertSame(thrown, caught);
    }

    /// Tests that `forEachRemaining(IntConsumer)` at the start visits every element, and after one `tryAdvance` the
    /// rest; and that `tryAdvance` then returns `false`.
    @Test
    @DisplayName("forEachRemaining(IntConsumer) visits the remaining elements, at the start and after tryAdvance()")
    default void intForEachRemainingVisitsTheRemainingElements() {
        SpliteratorSource<S, Integer> source = defaultSource();
        Spliterator.OfInt atStart = source.spliterator();
        List<Integer> visited = new ArrayList<>();
        atStart.forEachRemaining((IntConsumer) visited::add);
        assertSameElements(source.elements(), visited, documentsOrder(source), "the elements visited at the start");
        assertFalse(atStart.tryAdvance((IntConsumer) e -> { }), "tryAdvance() after forEachRemaining()");
        Spliterator.OfInt afterOne = source.spliterator();
        List<Integer> all = new ArrayList<>();
        afterOne.tryAdvance((IntConsumer) all::add);
        afterOne.forEachRemaining((IntConsumer) all::add);
        assertSameElements(source.elements(), all, documentsOrder(source), "the element passed, then those visited");
    }

    /// Tests that `forEachRemaining((IntConsumer) null)` throws [NullPointerException]: with elements left, over an
    /// empty source, and at the end of a traversal.
    @Test
    @DisplayName("forEachRemaining((IntConsumer) null) throws NullPointerException")
    default void intForEachRemainingOfNullThrowsNullPointerException() {
        Spliterator.OfInt withElements = defaultSource().spliterator();
        assertThrows(NullPointerException.class, () -> withElements.forEachRemaining((IntConsumer) null),
                "with elements left");
        Spliterator.OfInt empty = sourceOf(List.of()).spliterator();
        assertThrows(NullPointerException.class, () -> empty.forEachRemaining((IntConsumer) null),
                "over an empty source");
        Spliterator.OfInt atEnd = defaultSource().spliterator();
        atEnd.forEachRemaining((IntConsumer) e -> { });
        assertThrows(NullPointerException.class, () -> atEnd.forEachRemaining((IntConsumer) null),
                "at the end of a traversal");
    }

    /// Tests that an exception the `IntConsumer` throws is relayed to the caller of `forEachRemaining`.
    @Test
    @DisplayName("forEachRemaining(IntConsumer) relays an exception the action throws")
    default void intForEachRemainingWithThrowingActionRelaysTheException() {
        Spliterator.OfInt spliterator = defaultSource().spliterator();
        RuntimeException thrown = new IllegalStateException("thrown by the action");
        RuntimeException caught = assertThrows(RuntimeException.class,
                () -> spliterator.forEachRemaining((IntConsumer) e -> {
                    throw thrown;
                }));
        assertSame(thrown, caught);
    }

    /// Tests that the parts of `trySplit()`, each a `Spliterator.OfInt`, split until no part splits and traversed
    /// with an `IntConsumer`, cover each element once: prefix first, in order, where the source documents `ORDERED`.
    @Test
    @DisplayName("trySplit()'s parts, traversed with IntConsumer, cover each element once")
    default void intTrySplitLeavesPartsThatCoverEachElementOnce() {
        SpliteratorSource<S, Integer> source = defaultSource();
        List<Integer> covered = new ArrayList<>();
        for (final Spliterator.OfInt part : leaves(source.spliterator(), Spliterator.OfInt::trySplit)) {
            int calls = 0;
            while (part.tryAdvance((IntConsumer) covered::add)) {
                calls++;
                assertTrue(calls < TRAVERSAL_LIMIT, "tryAdvance(IntConsumer) did not end");
            }
        }
        assertSameElements(source.elements(), covered, documentsOrder(source), "the elements of the parts");
    }
}
