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

import java.util.Spliterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SpliteratorContractSupport;
import com.soliscode.test.provider.SpliteratorSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [characteristics()][Spliterator#characteristics()] method of a [Spliterator]: it reports the
/// characteristics the source's class documents, and the same ones on each call before a split.
///
/// Two readings: `Spliterator` requires no characteristic, so the first test checks the source's class's own
/// documentation, read through [SpliteratorSource#characteristics()], on a non-empty source (an empty
/// `IntStream.range` is `IntStream.empty()`, which reports only `SIZED` and `SUBSIZED`). And repeated calls before
/// or between splits "should always return the same result": the contract reads the "should" as a requirement, more
/// strictly than the text, and checks it across a traversal with no split. What each characteristic promises is
/// tested by [ReportedCharacteristicsContract].
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Spliterator#characteristics()
public interface CharacteristicsContract<S extends Spliterator<E>, E> extends SpliteratorContractSupport<S, E> {

    /// Tests that a spliterator over a non-empty source reports each characteristic its source's class documents.
    @Test
    @DisplayName("characteristics() includes each characteristic the source's class documents")
    default void characteristicsIncludesTheDocumentedOnes() {
        SpliteratorSource<S, E> source = defaultSource();
        int documented = source.characteristics();
        int reported = source.spliterator().characteristics();
        assertEquals(documented, reported & documented, () -> "characteristics() is 0x"
                + Integer.toHexString(reported) + ", but the source documents 0x" + Integer.toHexString(documented));
    }

    /// Tests that `characteristics()` returns the same value on repeated calls, before and during a traversal, with
    /// no split between them.
    @Test
    @DisplayName("characteristics() is the same on repeated calls, and through a traversal")
    default void characteristicsIsStableThroughATraversal() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        int first = spliterator.characteristics();
        assertEquals(first, spliterator.characteristics(), "characteristics() called again");
        spliterator.tryAdvance(e -> { });
        assertEquals(first, spliterator.characteristics(), "characteristics() after tryAdvance()");
        traverse(spliterator);
        assertEquals(first, spliterator.characteristics(), "characteristics() at the end of the traversal");
    }
}
