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
import java.util.function.DoubleConsumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.provider.SpliteratorSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the [Spliterator.OfDouble] interface**: [SpliteratorContract], and the `double` traversal methods,
/// [tryAdvance(DoubleConsumer)][Spliterator.OfDouble#tryAdvance(DoubleConsumer)] and
/// [forEachRemaining(DoubleConsumer)][Spliterator.OfDouble#forEachRemaining(DoubleConsumer)], with the parts of
/// [trySplit()][Spliterator.OfDouble#trySplit()] traversed through them.
///
/// It reads them as [SpliteratorContract] reads their boxed forms. It does not test the `@implSpec` of the boxed
/// `tryAdvance(Consumer)` and `forEachRemaining(Consumer)` (whether an action that is also a `DoubleConsumer` is passed
/// through unboxed): that is the default methods' specification, not the interface's.
///
/// ## Usage Examples
/// ```java
/// class DoubleArraySpliteratorTest extends AbstractTest
///         implements SpliteratorOfDoubleContract<Spliterator.OfDouble> {
///     @Override
///     public ObjectProvider<Double> elementProvider() {
///         return new DoubleProvider();
///     }
///
///     @Override
///     public SpliteratorProvider<Spliterator.OfDouble, Double> spliteratorProvider() {
///         return SpliteratorProviders.doubleArray();
///     }
/// }
/// ```
///
/// @param <S> the type of the spliterator under test
/// @since 0.1.3
/// @see Spliterator.OfDouble
public interface SpliteratorOfDoubleContract<S extends Spliterator.OfDouble> extends SpliteratorContract<S, Double> {

    /// Tests that each `tryAdvance(DoubleConsumer)` passes one element and returns `true`, until every element has been
    /// passed once; and that the next returns `false` without calling the action.
    @Test
    @DisplayName("tryAdvance(DoubleConsumer) passes each element once, one per call, then returns false")
    default void doubleTryAdvancePassesEachElementOnceThenReturnsFalse() {
        SpliteratorSource<S, Double> source = defaultSource();
        Spliterator.OfDouble spliterator = source.spliterator();
        List<Double> passed = new ArrayList<>();
        for (int calls = 1; calls <= TRAVERSAL_LIMIT; calls++) {
            List<Double> thisCall = new ArrayList<>();
            boolean advanced = spliterator.tryAdvance((DoubleConsumer) thisCall::add);
            if (!advanced) {
                assertEquals(List.of(), thisCall, "the elements passed by the tryAdvance() that returned false");
                break;
            }
            assertEquals(1, thisCall.size(), "the elements passed by one tryAdvance() that returned true");
            passed.addAll(thisCall);
            assertTrue(calls < TRAVERSAL_LIMIT, "tryAdvance(DoubleConsumer) did not end");
        }
        assertSameElements(source.elements(), passed, documentsOrder(source), "the elements passed");
    }

    /// Tests that `tryAdvance((DoubleConsumer) null)` throws [NullPointerException]: with elements left, over an empty
    /// source, and at the end of a traversal.
    @Test
    @DisplayName("tryAdvance((DoubleConsumer) null) throws NullPointerException")
    default void doubleTryAdvanceOfNullThrowsNullPointerException() {
        Spliterator.OfDouble withElements = defaultSource().spliterator();
        assertThrows(NullPointerException.class, () -> withElements.tryAdvance((DoubleConsumer) null),
                "with elements left");
        Spliterator.OfDouble empty = sourceOf(List.of()).spliterator();
        assertThrows(NullPointerException.class, () -> empty.tryAdvance((DoubleConsumer) null), "over an empty source");
        Spliterator.OfDouble atEnd = defaultSource().spliterator();
        atEnd.forEachRemaining((DoubleConsumer) e -> { });
        assertThrows(NullPointerException.class, () -> atEnd.tryAdvance((DoubleConsumer) null),
                "at the end of a traversal");
    }

    /// Tests that an exception the `DoubleConsumer` throws is relayed to the caller of `tryAdvance`.
    @Test
    @DisplayName("tryAdvance(DoubleConsumer) relays an exception the action throws")
    default void doubleTryAdvanceWithThrowingActionRelaysTheException() {
        Spliterator.OfDouble spliterator = defaultSource().spliterator();
        RuntimeException thrown = new IllegalStateException("thrown by the action");
        RuntimeException caught = assertThrows(RuntimeException.class,
                () -> spliterator.tryAdvance((DoubleConsumer) e -> {
                    throw thrown;
                }));
        assertSame(thrown, caught);
    }

    /// Tests that `forEachRemaining(DoubleConsumer)` at the start visits every element, and after one `tryAdvance` the
    /// rest; and that `tryAdvance` then returns `false`.
    @Test
    @DisplayName("forEachRemaining(DoubleConsumer) visits the remaining elements, at the start and after tryAdvance()")
    default void doubleForEachRemainingVisitsTheRemainingElements() {
        SpliteratorSource<S, Double> source = defaultSource();
        Spliterator.OfDouble atStart = source.spliterator();
        List<Double> visited = new ArrayList<>();
        atStart.forEachRemaining((DoubleConsumer) visited::add);
        assertSameElements(source.elements(), visited, documentsOrder(source), "the elements visited at the start");
        assertFalse(atStart.tryAdvance((DoubleConsumer) e -> { }), "tryAdvance() after forEachRemaining()");
        Spliterator.OfDouble afterOne = source.spliterator();
        List<Double> all = new ArrayList<>();
        afterOne.tryAdvance((DoubleConsumer) all::add);
        afterOne.forEachRemaining((DoubleConsumer) all::add);
        assertSameElements(source.elements(), all, documentsOrder(source), "the element passed, then those visited");
    }

    /// Tests that `forEachRemaining((DoubleConsumer) null)` throws [NullPointerException]: with elements left, over an
    /// empty source, and at the end of a traversal.
    @Test
    @DisplayName("forEachRemaining((DoubleConsumer) null) throws NullPointerException")
    default void doubleForEachRemainingOfNullThrowsNullPointerException() {
        Spliterator.OfDouble withElements = defaultSource().spliterator();
        assertThrows(NullPointerException.class, () -> withElements.forEachRemaining((DoubleConsumer) null),
                "with elements left");
        Spliterator.OfDouble empty = sourceOf(List.of()).spliterator();
        assertThrows(NullPointerException.class, () -> empty.forEachRemaining((DoubleConsumer) null),
                "over an empty source");
        Spliterator.OfDouble atEnd = defaultSource().spliterator();
        atEnd.forEachRemaining((DoubleConsumer) e -> { });
        assertThrows(NullPointerException.class, () -> atEnd.forEachRemaining((DoubleConsumer) null),
                "at the end of a traversal");
    }

    /// Tests that an exception the `DoubleConsumer` throws is relayed to the caller of `forEachRemaining`.
    @Test
    @DisplayName("forEachRemaining(DoubleConsumer) relays an exception the action throws")
    default void doubleForEachRemainingWithThrowingActionRelaysTheException() {
        Spliterator.OfDouble spliterator = defaultSource().spliterator();
        RuntimeException thrown = new IllegalStateException("thrown by the action");
        RuntimeException caught = assertThrows(RuntimeException.class,
                () -> spliterator.forEachRemaining((DoubleConsumer) e -> {
                    throw thrown;
                }));
        assertSame(thrown, caught);
    }

    /// Tests that the parts of `trySplit()`, each a `Spliterator.OfDouble`, split until no part splits and traversed
    /// with a `DoubleConsumer`, cover each element once: prefix first, in order, where the source documents `ORDERED`.
    @Test
    @DisplayName("trySplit()'s parts, traversed with DoubleConsumer, cover each element once")
    default void doubleTrySplitLeavesPartsThatCoverEachElementOnce() {
        SpliteratorSource<S, Double> source = defaultSource();
        List<Double> covered = new ArrayList<>();
        for (final Spliterator.OfDouble part : leaves(source.spliterator(), Spliterator.OfDouble::trySplit)) {
            int calls = 0;
            while (part.tryAdvance((DoubleConsumer) covered::add)) {
                calls++;
                assertTrue(calls < TRAVERSAL_LIMIT, "tryAdvance(DoubleConsumer) did not end");
            }
        }
        assertSameElements(source.elements(), covered, documentsOrder(source), "the elements of the parts");
    }
}
