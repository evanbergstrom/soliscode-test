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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SpliteratorContractSupport;
import com.soliscode.test.provider.SpliteratorSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [tryAdvance(Consumer)][Spliterator#tryAdvance(java.util.function.Consumer)] method of a
/// [Spliterator]: it performs the action on one remaining element and returns `true`, or returns `false` where none
/// remains; in encounter order where the spliterator is `ORDERED`; it relays an exception the action throws.
///
/// Order is checked where the source documents `ORDERED`. It does not test what the spliterator does after the
/// action throws, which [Spliterator] leaves unspecified.
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Spliterator#tryAdvance(java.util.function.Consumer)
public interface TryAdvanceContract<S extends Spliterator<E>, E> extends SpliteratorContractSupport<S, E> {

    /// Tests that each call passes one element and returns `true`, until every element has been passed once; and
    /// that the next returns `false` without calling the action.
    @Test
    @DisplayName("tryAdvance() passes each element once, one per call, then returns false")
    default void tryAdvancePassesEachElementOnceThenReturnsFalse() {
        SpliteratorSource<S, E> source = defaultSource();
        Spliterator<E> spliterator = source.spliterator();
        List<E> passed = new ArrayList<>();
        for (int calls = 1; calls <= TRAVERSAL_LIMIT; calls++) {
            List<E> thisCall = new ArrayList<>();
            boolean advanced = spliterator.tryAdvance(thisCall::add);
            if (!advanced) {
                assertEquals(List.of(), thisCall, "the elements passed by the tryAdvance() that returned false");
                break;
            }
            assertEquals(1, thisCall.size(), "the elements passed by one tryAdvance() that returned true");
            passed.addAll(thisCall);
            assertTrue(calls < TRAVERSAL_LIMIT, "tryAdvance(Consumer) did not end");
        }
        assertSameElements(source.elements(), passed, documentsOrder(source), "the elements passed");
    }

    /// Tests that `tryAdvance` on an empty source returns `false` without calling the action.
    @Test
    @DisplayName("tryAdvance() on an empty source returns false")
    default void tryAdvanceOnEmptySourceReturnsFalse() {
        Spliterator<E> spliterator = sourceOf(List.of()).spliterator();
        List<E> passed = new ArrayList<>();
        assertFalse(spliterator.tryAdvance(passed::add), "tryAdvance() on an empty source");
        assertEquals(List.of(), passed);
    }

    /// Tests that `tryAdvance(null)` throws [NullPointerException]: with elements left, over an empty source, and at
    /// the end of a traversal.
    @Test
    @DisplayName("tryAdvance(null) throws NullPointerException, with elements left, when empty and at the end")
    default void tryAdvanceOfNullThrowsNullPointerException() {
        Spliterator<E> withElements = defaultSource().spliterator();
        assertThrows(NullPointerException.class, () -> withElements.tryAdvance(null), "with elements left");
        Spliterator<E> empty = sourceOf(List.of()).spliterator();
        assertThrows(NullPointerException.class, () -> empty.tryAdvance(null), "over an empty source");
        Spliterator<E> atEnd = defaultSource().spliterator();
        traverse(atEnd);
        assertThrows(NullPointerException.class, () -> atEnd.tryAdvance(null), "at the end of a traversal");
    }

    /// Tests that an exception the action throws is relayed to the caller.
    @Test
    @DisplayName("tryAdvance() relays an exception the action throws")
    default void tryAdvanceWithThrowingActionRelaysTheException() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        RuntimeException thrown = new IllegalStateException("thrown by the action");
        RuntimeException caught = assertThrows(RuntimeException.class, () -> spliterator.tryAdvance(e -> {
            throw thrown;
        }));
        assertSame(thrown, caught);
    }
}
