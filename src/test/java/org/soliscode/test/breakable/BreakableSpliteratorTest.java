package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.Spliterator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;
import static org.soliscode.test.breakable.BreakableSpliterator.*;

/// Tests for the `BreakableSpliterator` class. These tests determine if the breaks supported by this class result in the
/// behavior expected.
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableSpliterator
@DisplayName("Tests for BreakableSpliterator")
public class BreakableSpliteratorTest {

    @Test
    public void testSpliteratorWithSpliteratorIsAlwaysEmptyBeak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_IS_ALWAYS_EMPTY)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertFalse(iterator.tryAdvance((e) -> {}));
        assertNull(iterator.trySplit());
        assertEquals(0, iterator.estimateSize());
        assertEquals(0, iterator.getExactSizeIfKnown());
    }

    @Test
    public void testSpliteratorWithForEachRemainingDoesNotCallActionBreak() {
        Iterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)
                .build();
        Spliterator<Integer> iterator = iterable.spliterator();
        Collection<Integer> elements = new ArrayList<>();
        iterator.forEachRemaining(elements::add);
        assertIsEmpty(elements);
    }

    @Test
    public void testSpliteratorWithTrySplitAlwaysReturnsNullBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3, 4, 5)
                .addBreak(SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertNull(iterator.trySplit());
    }

    @Test
    public void testSpliteratorWithTryAdvanceDoesNotCallActionBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        Collection<Integer> elements = new ArrayList<>();
        iterator.tryAdvance(elements::add);
        assertIsEmpty(elements);
    }

    @Test
    public void testSpliteratorWithTryAdvanceAlwaysReturnsTrueBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(Integer.class)
                .addBreak(SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertTrue(iterator.tryAdvance((e) -> {}));
    }

    @Test
    public void testSpliteratorWithTryAdvanceAlwaysReturnsFalseBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertFalse(iterator.tryAdvance((e) -> {}));
    }

    @Test
    public void testSpliteratorWithEstimateSizeAlwaysReturnsMaxValueBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(Long.MAX_VALUE, iterator.estimateSize());
    }

    @Test
    public void testSpliteratorWithEstimateSizeAlwaysReturnsZeoBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(0, iterator.estimateSize());
    }

    @Test
    public void testSpliteratorWithGetExactSizeIfKnown() {
        BreakableIterable<Integer> unsizedIterable = Breakables.buildIterable(1, 2, 3)
                .build();
        assertEquals(-1, unsizedIterable.spliterator().getExactSizeIfKnown());

        BreakableIterable<Integer> sizedIterable = Breakables.buildIterable(1, 2, 3)
                .setCharacteristics(SIZED)
                .build();
        assertEquals(3, sizedIterable.spliterator().getExactSizeIfKnown());
    }

    @Test
    public void testSpliteratorWithGetExactSizeIfKnownAlwaysReturnsNegativeOneBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(-1, iterator.getExactSizeIfKnown());
    }

    @Test
    public void testSpliteratorWithCharacteristicsAlwaysReturnsZeroBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(0, iterator.characteristics());
    }

    @Test
    public void testSpliteratorWithHasCharacteristicAlwaysReturnsTrueBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertTrue(iterator.hasCharacteristics(CONCURRENT));
    }

    static final int [] CHARACTERISTIC_VALUES = { 0, SIZED, SUBSIZED, SORTED, CONCURRENT,  DISTINCT, IMMUTABLE, NONNULL };

    @Test
    public void testSpliteratorWithHasCharacteristicWorks() {

        for (int value : CHARACTERISTIC_VALUES){
            BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                    .addBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE)
                    .setCharacteristics(value)
                    .build();

            Spliterator<Integer> iterator = iterable.spliterator();
            assertFalse(iterator.hasCharacteristics(value));
        }
    }

    @Test
    public void testSpliteratorWithHasCharacteristicAlwaysReturnsFalseBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertFalse(iterator.hasCharacteristics(SIZED));
    }

    @Test
    public void testSpliteratorGetComparatorAlwaysReturnsNull() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(Integer.class)
                .addBreak(SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertNull(iterator.getComparator());
    }

    @Test
    public void testGetComparator() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(Integer.class)
                .build();

        assertThrows(IllegalStateException.class, () -> {
            Spliterator<Integer> iterator = iterable.spliterator();
            assertNotNull(iterator.getComparator());
        });
    }

}
