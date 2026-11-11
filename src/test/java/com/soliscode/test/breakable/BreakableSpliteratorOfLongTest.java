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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.LongConsumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests for [BreakableSpliteratorOfLong] (#38).
@DisplayName("Tests for BreakableSpliteratorOfLong")
public class BreakableSpliteratorOfLongTest {

    /// Default constructor.
    public BreakableSpliteratorOfLongTest() {
    }

    private static BreakableSpliteratorOfLong over(final Break... breaks) {
        Spliterator.OfLong delegate = Arrays.spliterator(new long[] {1, 2, 3, 4});
        return new BreakableSpliteratorOfLong(delegate, Set.of(breaks), Map.of(), delegate.characteristics(), false);
    }

    private static List<Long> drain(final Spliterator.OfLong spliterator) {
        List<Long> elements = new ArrayList<>();
        spliterator.forEachRemaining((LongConsumer) elements::add);
        return elements;
    }

    @Test
    @DisplayName("tryAdvance(LongConsumer) and forEachRemaining(LongConsumer) pass every element, with no break")
    public void primitiveTraversalPassesEveryElement() {
        BreakableSpliteratorOfLong spliterator = over();
        List<Long> passed = new ArrayList<>();
        spliterator.tryAdvance((LongConsumer) passed::add);
        passed.addAll(drain(spliterator));
        assertEquals(List.of(1L, 2L, 3L, 4L), passed);
        assertFalse(spliterator.tryAdvance((LongConsumer) passed::add));
    }

    @Test
    @DisplayName("an inherited break, SPLITERATOR_SKIPS_FIRST_ELEMENT, reaches the long methods")
    public void inheritedBreakReachesPrimitiveMethods() {
        assertEquals(List.of(2L, 3L, 4L), drain(over(BreakableSpliterator.SPLITERATOR_SKIPS_FIRST_ELEMENT)));
    }

    @Test
    @DisplayName("a null LongConsumer throws NullPointerException, or is ignored with the IGNORES_NULL_ACTION breaks")
    public void nullPrimitiveActionThrowsUnlessIgnored() {
        assertThrows(NullPointerException.class, () -> over().tryAdvance((LongConsumer) null));
        assertThrows(NullPointerException.class, () -> over().forEachRemaining((LongConsumer) null));
        BreakableSpliteratorOfLong ignoresInTryAdvance =
                over(BreakableSpliterator.SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION);
        assertFalse(ignoresInTryAdvance.tryAdvance((LongConsumer) null));
        BreakableSpliteratorOfLong ignoresInForEach =
                over(BreakableSpliterator.SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION);
        ignoresInForEach.forEachRemaining((LongConsumer) null);
    }

    @Test
    @DisplayName("trySplit() returns a Spliterator.OfLong, under the breaks that change its part too")
    public void trySplitReturnsPrimitiveParts() {
        Spliterator.OfLong part = over().trySplit();
        assertNotNull(part);
        assertEquals(List.of(1L, 2L), drain(part));
        Spliterator.OfLong unsized = over(BreakableSpliterator.SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED).trySplit();
        assertInstanceOf(Spliterator.OfLong.class, unsized);
        assertFalse(unsized.hasCharacteristics(Spliterator.SIZED));
        Spliterator.OfLong suffix = over(BreakableSpliterator.SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX).trySplit();
        assertNotNull(suffix);
        assertEquals(List.of(3L, 4L), drain(suffix));
        BreakableSpliteratorOfLong never = over(BreakableSpliterator.SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL);
        for (int i = 0; i < 10; i++) {
            Spliterator.OfLong empty = never.trySplit();
            assertNotNull(empty);
        }
    }
}
