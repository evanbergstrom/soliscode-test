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
import java.util.function.DoubleConsumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests for [BreakableSpliteratorOfDouble] (#38).
@DisplayName("Tests for BreakableSpliteratorOfDouble")
public class BreakableSpliteratorOfDoubleTest {

    /// Default constructor.
    public BreakableSpliteratorOfDoubleTest() {
    }

    private static BreakableSpliteratorOfDouble over(final Break... breaks) {
        Spliterator.OfDouble delegate = Arrays.spliterator(new double[] {1, 2, 3, 4});
        return new BreakableSpliteratorOfDouble(delegate, Set.of(breaks), Map.of(), delegate.characteristics(), false);
    }

    private static List<Double> drain(final Spliterator.OfDouble spliterator) {
        List<Double> elements = new ArrayList<>();
        spliterator.forEachRemaining((DoubleConsumer) elements::add);
        return elements;
    }

    @Test
    @DisplayName("tryAdvance(DoubleConsumer) and forEachRemaining(DoubleConsumer) pass every element, with no break")
    public void primitiveTraversalPassesEveryElement() {
        BreakableSpliteratorOfDouble spliterator = over();
        List<Double> passed = new ArrayList<>();
        spliterator.tryAdvance((DoubleConsumer) passed::add);
        passed.addAll(drain(spliterator));
        assertEquals(List.of(1.0, 2.0, 3.0, 4.0), passed);
        assertFalse(spliterator.tryAdvance((DoubleConsumer) passed::add));
    }

    @Test
    @DisplayName("an inherited break, SPLITERATOR_SKIPS_FIRST_ELEMENT, reaches the double methods")
    public void inheritedBreakReachesPrimitiveMethods() {
        assertEquals(List.of(2.0, 3.0, 4.0), drain(over(BreakableSpliterator.SPLITERATOR_SKIPS_FIRST_ELEMENT)));
    }

    @Test
    @DisplayName("a null DoubleConsumer throws NullPointerException, or is ignored with the IGNORES_NULL_ACTION breaks")
    public void nullPrimitiveActionThrowsUnlessIgnored() {
        assertThrows(NullPointerException.class, () -> over().tryAdvance((DoubleConsumer) null));
        assertThrows(NullPointerException.class, () -> over().forEachRemaining((DoubleConsumer) null));
        BreakableSpliteratorOfDouble ignoresInTryAdvance =
                over(BreakableSpliterator.SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION);
        assertFalse(ignoresInTryAdvance.tryAdvance((DoubleConsumer) null));
        BreakableSpliteratorOfDouble ignoresInForEach =
                over(BreakableSpliterator.SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION);
        ignoresInForEach.forEachRemaining((DoubleConsumer) null);
    }

    @Test
    @DisplayName("trySplit() returns a Spliterator.OfDouble, under the breaks that change its part too")
    public void trySplitReturnsPrimitiveParts() {
        Spliterator.OfDouble part = over().trySplit();
        assertNotNull(part);
        assertEquals(List.of(1.0, 2.0), drain(part));
        Spliterator.OfDouble unsized = over(BreakableSpliterator.SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED).trySplit();
        assertInstanceOf(Spliterator.OfDouble.class, unsized);
        assertFalse(unsized.hasCharacteristics(Spliterator.SIZED));
        Spliterator.OfDouble suffix = over(BreakableSpliterator.SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX).trySplit();
        assertNotNull(suffix);
        assertEquals(List.of(3.0, 4.0), drain(suffix));
        BreakableSpliteratorOfDouble never = over(BreakableSpliterator.SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL);
        for (int i = 0; i < 10; i++) {
            Spliterator.OfDouble empty = never.trySplit();
            assertNotNull(empty);
        }
    }
}
