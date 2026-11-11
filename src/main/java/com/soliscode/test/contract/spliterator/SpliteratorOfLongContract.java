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
import java.util.function.LongConsumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.provider.SpliteratorSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the [Spliterator.OfLong] interface**: [SpliteratorContract], and the `long` traversal methods,
/// [tryAdvance(LongConsumer)][Spliterator.OfLong#tryAdvance(LongConsumer)] and
/// [forEachRemaining(LongConsumer)][Spliterator.OfLong#forEachRemaining(LongConsumer)], with the parts of
/// [trySplit()][Spliterator.OfLong#trySplit()] traversed through them.
///
/// It reads them as [SpliteratorContract] reads their boxed forms. It does not test the `@implSpec` of the boxed
/// `tryAdvance(Consumer)` and `forEachRemaining(Consumer)` (whether an action that is also a `LongConsumer` is passed
/// through unboxed): that is the default methods' specification, not the interface's.
///
/// ## Usage Examples
/// ```java
/// class LongRangeSpliteratorTest extends AbstractTest
///         implements SpliteratorOfLongContract<Spliterator.OfLong> {
///     @Override
///     public ObjectProvider<Long> elementProvider() {
///         return new LongProvider();
///     }
///
///     @Override
///     public SpliteratorProvider<Spliterator.OfLong, Long> spliteratorProvider() {
///         return SpliteratorProviders.longRange();
///     }
/// }
/// ```
///
/// @param <S> the type of the spliterator under test
/// @since 0.1.3
/// @see Spliterator.OfLong
public interface SpliteratorOfLongContract<S extends Spliterator.OfLong> extends SpliteratorContract<S, Long> {

    /// Tests that each `tryAdvance(LongConsumer)` passes one element and returns `true`, until every element has been
    /// passed once; and that the next returns `false` without calling the action.
    @Test
    @DisplayName("tryAdvance(LongConsumer) passes each element once, one per call, then returns false")
    default void longTryAdvancePassesEachElementOnceThenReturnsFalse() {
        SpliteratorSource<S, Long> source = defaultSource();
        Spliterator.OfLong spliterator = source.spliterator();
        List<Long> passed = new ArrayList<>();
        for (int calls = 1; calls <= TRAVERSAL_LIMIT; calls++) {
            List<Long> thisCall = new ArrayList<>();
            boolean advanced = spliterator.tryAdvance((LongConsumer) thisCall::add);
            if (!advanced) {
                assertEquals(List.of(), thisCall, "the elements passed by the tryAdvance() that returned false");
                break;
            }
            assertEquals(1, thisCall.size(), "the elements passed by one tryAdvance() that returned true");
            passed.addAll(thisCall);
            assertTrue(calls < TRAVERSAL_LIMIT, "tryAdvance(LongConsumer) did not end");
        }
        assertSameElements(source.elements(), passed, documentsOrder(source), "the elements passed");
    }

    /// Tests that `tryAdvance((LongConsumer) null)` throws [NullPointerException]: with elements left, over an empty
    /// source, and at the end of a traversal.
    @Test
    @DisplayName("tryAdvance((LongConsumer) null) throws NullPointerException")
    default void longTryAdvanceOfNullThrowsNullPointerException() {
        Spliterator.OfLong withElements = defaultSource().spliterator();
        assertThrows(NullPointerException.class, () -> withElements.tryAdvance((LongConsumer) null),
                "with elements left");
        Spliterator.OfLong empty = sourceOf(List.of()).spliterator();
        assertThrows(NullPointerException.class, () -> empty.tryAdvance((LongConsumer) null), "over an empty source");
        Spliterator.OfLong atEnd = defaultSource().spliterator();
        atEnd.forEachRemaining((LongConsumer) e -> { });
        assertThrows(NullPointerException.class, () -> atEnd.tryAdvance((LongConsumer) null),
                "at the end of a traversal");
    }

    /// Tests that an exception the `LongConsumer` throws is relayed to the caller of `tryAdvance`.
    @Test
    @DisplayName("tryAdvance(LongConsumer) relays an exception the action throws")
    default void longTryAdvanceWithThrowingActionRelaysTheException() {
        Spliterator.OfLong spliterator = defaultSource().spliterator();
        RuntimeException thrown = new IllegalStateException("thrown by the action");
        RuntimeException caught = assertThrows(RuntimeException.class,
                () -> spliterator.tryAdvance((LongConsumer) e -> {
                    throw thrown;
                }));
        assertSame(thrown, caught);
    }

    /// Tests that `forEachRemaining(LongConsumer)` at the start visits every element, and after one `tryAdvance` the
    /// rest; and that `tryAdvance` then returns `false`.
    @Test
    @DisplayName("forEachRemaining(LongConsumer) visits the remaining elements, at the start and after tryAdvance()")
    default void longForEachRemainingVisitsTheRemainingElements() {
        SpliteratorSource<S, Long> source = defaultSource();
        Spliterator.OfLong atStart = source.spliterator();
        List<Long> visited = new ArrayList<>();
        atStart.forEachRemaining((LongConsumer) visited::add);
        assertSameElements(source.elements(), visited, documentsOrder(source), "the elements visited at the start");
        assertFalse(atStart.tryAdvance((LongConsumer) e -> { }), "tryAdvance() after forEachRemaining()");
        Spliterator.OfLong afterOne = source.spliterator();
        List<Long> all = new ArrayList<>();
        afterOne.tryAdvance((LongConsumer) all::add);
        afterOne.forEachRemaining((LongConsumer) all::add);
        assertSameElements(source.elements(), all, documentsOrder(source), "the element passed, then those visited");
    }

    /// Tests that `forEachRemaining((LongConsumer) null)` throws [NullPointerException]: with elements left, over an
    /// empty source, and at the end of a traversal.
    @Test
    @DisplayName("forEachRemaining((LongConsumer) null) throws NullPointerException")
    default void longForEachRemainingOfNullThrowsNullPointerException() {
        Spliterator.OfLong withElements = defaultSource().spliterator();
        assertThrows(NullPointerException.class, () -> withElements.forEachRemaining((LongConsumer) null),
                "with elements left");
        Spliterator.OfLong empty = sourceOf(List.of()).spliterator();
        assertThrows(NullPointerException.class, () -> empty.forEachRemaining((LongConsumer) null),
                "over an empty source");
        Spliterator.OfLong atEnd = defaultSource().spliterator();
        atEnd.forEachRemaining((LongConsumer) e -> { });
        assertThrows(NullPointerException.class, () -> atEnd.forEachRemaining((LongConsumer) null),
                "at the end of a traversal");
    }

    /// Tests that an exception the `LongConsumer` throws is relayed to the caller of `forEachRemaining`.
    @Test
    @DisplayName("forEachRemaining(LongConsumer) relays an exception the action throws")
    default void longForEachRemainingWithThrowingActionRelaysTheException() {
        Spliterator.OfLong spliterator = defaultSource().spliterator();
        RuntimeException thrown = new IllegalStateException("thrown by the action");
        RuntimeException caught = assertThrows(RuntimeException.class,
                () -> spliterator.forEachRemaining((LongConsumer) e -> {
                    throw thrown;
                }));
        assertSame(thrown, caught);
    }

    /// Tests that the parts of `trySplit()`, each a `Spliterator.OfLong`, split until no part splits and traversed
    /// with a `LongConsumer`, cover each element once: prefix first, in order, where the source documents `ORDERED`.
    @Test
    @DisplayName("trySplit()'s parts, traversed with LongConsumer, cover each element once")
    default void longTrySplitLeavesPartsThatCoverEachElementOnce() {
        SpliteratorSource<S, Long> source = defaultSource();
        List<Long> covered = new ArrayList<>();
        for (final Spliterator.OfLong part : leaves(source.spliterator(), Spliterator.OfLong::trySplit)) {
            int calls = 0;
            while (part.tryAdvance((LongConsumer) covered::add)) {
                calls++;
                assertTrue(calls < TRAVERSAL_LIMIT, "tryAdvance(LongConsumer) did not end");
            }
        }
        assertSameElements(source.elements(), covered, documentsOrder(source), "the elements of the parts");
    }
}
