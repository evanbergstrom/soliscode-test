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
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Contract for the [trySplit()][Spliterator#trySplit()] method of a [Spliterator]: a part it returns covers
/// elements the spliterator no longer covers, a prefix where it is `ORDERED`; and repeated calls eventually return
/// `null`.
///
/// "A strict prefix" is read as a prefix, more loosely than the text: the part must come before what is left, but may
/// take every element, as `LinkedList`'s does for a small list (it moves every element into the part, and is left
/// empty).
///
/// `trySplit` may return `null` "for any reason", so each test accepts `null` and checks what is left: it reads
/// [Spliterator] as it stands, where `SpliteratorMethodContract` (`Iterable`'s) asserts `null` on an empty source.
/// Order is checked where the source documents `ORDERED`; the `ORDERED` test of [ReportedCharacteristicsContract]
/// checks the prefix where the spliterator reports it without the source documenting it. The source is finite.
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Spliterator#trySplit()
public interface TrySplitContract<S extends Spliterator<E>, E> extends SpliteratorContractSupport<S, E> {

    /// Tests that, split until no part splits, the parts cover each element once: prefix first, in order, where the
    /// source documents `ORDERED`.
    @Test
    @DisplayName("trySplit(), repeated on every part, leaves parts that cover each element once")
    default void trySplitRepeatedLeavesPartsThatCoverEachElementOnce() {
        SpliteratorSource<S, E> source = defaultSource();
        List<E> covered = new ArrayList<>();
        for (final Spliterator<E> part : this.<Spliterator<E>>leaves(source.spliterator(), Spliterator::trySplit)) {
            covered.addAll(traverse(part));
        }
        assertSameElements(source.elements(), covered, documentsOrder(source), "the elements of the parts");
    }

    /// Tests that a split after one `tryAdvance` leaves parts that cover the rest once.
    @Test
    @DisplayName("trySplit() after tryAdvance() leaves parts that cover the remaining elements once")
    default void trySplitAfterTryAdvanceCoversTheRest() {
        SpliteratorSource<S, E> source = defaultSource();
        Spliterator<E> spliterator = source.spliterator();
        List<E> covered = new ArrayList<>();
        spliterator.tryAdvance(covered::add);
        for (final Spliterator<E> part : this.<Spliterator<E>>leaves(spliterator, Spliterator::trySplit)) {
            covered.addAll(traverse(part));
        }
        assertSameElements(source.elements(), covered, documentsOrder(source),
                "the element passed, then the elements of the parts");
    }

    /// Tests that the parts of a spliterator over an empty source cover nothing.
    @Test
    @DisplayName("trySplit() on an empty source leaves parts that cover nothing")
    default void trySplitOnEmptySourceCoversNothing() {
        List<E> covered = new ArrayList<>();
        Spliterator<E> spliterator = sourceOf(List.of()).spliterator();
        for (final Spliterator<E> part : this.<Spliterator<E>>leaves(spliterator, Spliterator::trySplit)) {
            covered.addAll(traverse(part));
        }
        assertEquals(List.of(), covered, "the elements of the parts");
    }

    /// Tests that `trySplit` called again and again on one spliterator returns `null` in the end: "repeated calls to
    /// `trySplit()` must eventually return `null`".
    @Test
    @DisplayName("trySplit() called repeatedly on one spliterator eventually returns null")
    default void trySplitRepeatedEventuallyReturnsNull() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        int splits = 0;
        while (spliterator.trySplit() != null) {
            splits++;
            assertTrue(splits < TRAVERSAL_LIMIT, "trySplit() did not return null after " + splits + " splits");
        }
    }
}
