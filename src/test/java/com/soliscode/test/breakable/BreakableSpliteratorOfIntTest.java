/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.IntConsumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for [BreakableSpliteratorOfInt] (#38).
@DisplayName("Tests for BreakableSpliteratorOfInt")
public class BreakableSpliteratorOfIntTest {

    /// Default constructor.
    public BreakableSpliteratorOfIntTest() {
    }

    @Test
    @DisplayName("It copies its breaks: addBreak works on one made with Set.of(), and the caller's set is not shared")
    public void constructorCopiesBreaks() {
        BreakableSpliteratorOfInt fixed = over();
        fixed.addBreak(BreakableSpliterator.SPLITERATOR_SKIPS_FIRST_ELEMENT);
        assertTrue(fixed.hasBreak(BreakableSpliterator.SPLITERATOR_SKIPS_FIRST_ELEMENT));
        Set<Break> mine = new HashSet<>();
        BreakableSpliteratorOfInt copied =
                new BreakableSpliteratorOfInt(Arrays.spliterator(new int[] {1}), mine, Map.of(), 0, false);
        mine.add(BreakableSpliterator.SPLITERATOR_SKIPS_FIRST_ELEMENT);
        assertFalse(copied.hasBreak(BreakableSpliterator.SPLITERATOR_SKIPS_FIRST_ELEMENT));
    }

    private static BreakableSpliteratorOfInt over(final Break... breaks) {
        Spliterator.OfInt delegate = Arrays.spliterator(new int[] {1, 2, 3, 4});
        return new BreakableSpliteratorOfInt(delegate, Set.of(breaks), Map.of(), delegate.characteristics(), false);
    }

    private static List<Integer> drain(final Spliterator.OfInt spliterator) {
        List<Integer> elements = new ArrayList<>();
        spliterator.forEachRemaining((IntConsumer) elements::add);
        return elements;
    }

    @Test
    @DisplayName("tryAdvance(IntConsumer) and forEachRemaining(IntConsumer) pass every element, with no break")
    public void primitiveTraversalPassesEveryElement() {
        BreakableSpliteratorOfInt spliterator = over();
        List<Integer> passed = new ArrayList<>();
        spliterator.tryAdvance((IntConsumer) passed::add);
        passed.addAll(drain(spliterator));
        assertEquals(List.of(1, 2, 3, 4), passed);
        assertFalse(spliterator.tryAdvance((IntConsumer) passed::add));
    }

    @Test
    @DisplayName("an inherited break, SPLITERATOR_SKIPS_FIRST_ELEMENT, reaches the int methods")
    public void inheritedBreakReachesPrimitiveMethods() {
        assertEquals(List.of(2, 3, 4), drain(over(BreakableSpliterator.SPLITERATOR_SKIPS_FIRST_ELEMENT)));
    }

    @Test
    @DisplayName("a null IntConsumer throws NullPointerException, or is ignored with the IGNORES_NULL_ACTION breaks")
    public void nullPrimitiveActionThrowsUnlessIgnored() {
        assertThrows(NullPointerException.class, () -> over().tryAdvance((IntConsumer) null));
        assertThrows(NullPointerException.class, () -> over().forEachRemaining((IntConsumer) null));
        BreakableSpliteratorOfInt ignoresInTryAdvance =
                over(BreakableSpliterator.SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION);
        assertFalse(ignoresInTryAdvance.tryAdvance((IntConsumer) null));
        BreakableSpliteratorOfInt ignoresInForEach =
                over(BreakableSpliterator.SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION);
        ignoresInForEach.forEachRemaining((IntConsumer) null);
    }

    @Test
    @DisplayName("trySplit() returns a Spliterator.OfInt, under the breaks that change its part too")
    public void trySplitReturnsPrimitiveParts() {
        Spliterator.OfInt part = over().trySplit();
        assertNotNull(part);
        assertEquals(List.of(1, 2), drain(part));
        Spliterator.OfInt unsized = over(BreakableSpliterator.SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED).trySplit();
        assertInstanceOf(Spliterator.OfInt.class, unsized);
        assertFalse(unsized.hasCharacteristics(Spliterator.SIZED));
        Spliterator.OfInt suffix = over(BreakableSpliterator.SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX).trySplit();
        assertNotNull(suffix);
        assertEquals(List.of(3, 4), drain(suffix));
        BreakableSpliteratorOfInt never = over(BreakableSpliterator.SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL);
        for (int i = 0; i < 10; i++) {
            Spliterator.OfInt empty = never.trySplit();
            assertNotNull(empty);
        }
    }
}
