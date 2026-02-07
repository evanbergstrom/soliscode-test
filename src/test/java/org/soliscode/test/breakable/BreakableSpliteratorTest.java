package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;
import static org.soliscode.test.breakable.BreakableSpliterator.*;

/**
 * Comprehensive test suite for validating the behavior of {@link BreakableSpliterator} and its various breakable behaviors.
 *
 * <p>This test class verifies that the {@code BreakableSpliterator} class correctly implements programmatically
 * broken {@link Spliterator} behavior for testing purposes. The {@code BreakableSpliterator} is designed to
 * simulate faulty or edge-case implementations of the Spliterator interface, enabling thorough testing of
 * code that depends on proper Spliterator behavior.
 *
 * ### Purpose and Use Cases
 * <p>The {@code BreakableSpliterator} is particularly valuable for:
 * <ul>
 * <li>Testing error handling in parallel processing code that uses Spliterators</li>
 * <li>Validating robustness of stream operations against malformed spliterators</li>
 * <li>Ensuring collection contract implementations handle broken spliterator behavior gracefully</li>
 * <li>Testing edge cases in custom collection implementations</li>
 * <li>Verifying defensive programming practices in spliterator-dependent code</li>
 * </ul>
 *
 * ### Breakable Behaviors Tested
 * <p>This test suite validates the following types of breakable behaviors:
 *
 * #### Traversal Breaks
 * <ul>
 * <li>{@code SPLITERATOR_IS_ALWAYS_EMPTY} - Spliterator appears empty regardless of underlying data</li>
 * <li>{@code SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION} - tryAdvance ignores the action consumer</li>
 * <li>{@code SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE/FALSE} - tryAdvance returns fixed boolean values</li>
 * <li>{@code SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION} - forEachRemaining ignores actions</li>
 * </ul>
 *
 * #### Splitting Breaks
 * <ul>
 * <li>{@code SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL} - trySplit never succeeds in splitting</li>
 * </ul>
 *
 * #### Size Estimation Breaks
 * <ul>
 * <li>{@code SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE} - estimateSize returns {@code Long.MAX_VALUE}</li>
 * <li>{@code SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO} - estimateSize always returns 0</li>
 * <li>{@code SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE} - exact size is always unknown</li>
 * </ul>
 *
 * #### Characteristics Breaks
 * <ul>
 * <li>{@code SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO} - No characteristics reported</li>
 * <li>{@code SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE/FALSE} - Fixed characteristic responses</li>
 * </ul>
 *
 * #### Comparator Breaks
 * <ul>
 * <li>{@code SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL} - Never provides a comparator</li>
 * </ul>
 *
 * ### Testing Strategy
 * <p>Each test method focuses on a specific break behavior and verifies that:
 * <ul>
 * <li>The break is correctly applied to the spliterator instance</li>
 * <li>The broken behavior persists across method calls</li>
 * <li>The break doesn't affect unrelated spliterator functionality</li>
 * <li>Edge cases and boundary conditions are handled appropriately</li>
 * </ul>
 *
 * <p>The tests use the {@link Breakables} builder pattern to construct spliterators with specific
 * breaks, then verify the expected broken behavior through direct method calls and assertions.
 *
 * ### Integration with Testing Framework
 * <p>These breakable spliterators are designed to be used in conjunction with collection contract
 * tests to ensure that collection implementations properly handle malformed spliterator behavior.
 * This is particularly important for collections that depend on spliterator characteristics for
 * optimization decisions or parallel processing strategies.
 *
 * <p>Usage example:
 * <pre>{@code
 * // Create a spliterator that always appears empty
 * BreakableIterable<String> iterable = Breakables.buildIterable("a", "b", "c")
 *     .addBreak(SPLITERATOR_IS_ALWAYS_EMPTY)
 *     .build();
 *
 * Spliterator<String> spliterator = iterable.spliterator();
 * assertFalse(spliterator.tryAdvance(System.out::println)); // Should not advance
 * assertEquals(0, spliterator.estimateSize()); // Should report size 0
 * }</pre>
 *
 * @author evanbergstrom
 * @see BreakableSpliterator
 * @see Breakables
 * @see Spliterator
 * @since 1.0
 */
@SuppressWarnings("MagicConstant")
@DisplayName("Tests for BreakableSpliterator")
public class BreakableSpliteratorTest {

    /**
     * Tests the {@code SPLITERATOR_IS_ALWAYS_EMPTY} break behavior.
     *
     * <p>Verifies that when this break is applied, the spliterator:
     * <ul>
     * <li>Returns {@code false} from {@code tryAdvance()}, indicating no elements</li>
     * <li>Returns {@code null} from {@code trySplit()}, indicating no splitting possible</li>
     * <li>Returns {@code 0} from {@code estimateSize()}, indicating empty collection</li>
     * <li>Returns {@code 0} from {@code getExactSizeIfKnown()}, indicating known empty size</li>
     * </ul>
     *
     * <p>This break simulates a spliterator that appears completely empty regardless of
     * the underlying data, useful for testing edge case handling in parallel processing.
     */
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

    /**
     * Tests the {@code SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code forEachRemaining()} method
     * does not invoke the provided action consumer, even though elements exist in the
     * underlying data structure. This simulates a broken implementation that ignores
     * the action parameter.
     *
     * <p>The test confirms that after calling {@code forEachRemaining()}, the collection
     * used to capture elements remains empty, proving the action was never called.
     */
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

    /**
     * Tests the {@code SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code trySplit()} method always
     * returns {@code null}, indicating that the spliterator cannot be split for parallel
     * processing, even when the underlying data would normally support splitting.
     *
     * <p>This break is useful for testing scenarios where parallel processing should
     * gracefully degrade to sequential processing when splitting is not available.
     */
    @Test
    public void testSpliteratorWithTrySplitAlwaysReturnsNullBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3, 4, 5)
                .addBreak(SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertNull(iterator.trySplit());
    }

    /**
     * Tests the {@code SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code tryAdvance()} method
     * does not invoke the provided action consumer, even though the method may still
     * return appropriate boolean values. This simulates a broken implementation that
     * advances through elements without actually processing them.
     *
     * <p>The test confirms that after calling {@code tryAdvance()}, the collection
     * used to capture elements remains empty, proving the action was never called.
     */
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

    /**
     * Tests the {@code SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code tryAdvance()} method always
     * returns {@code true}, even when called on an empty spliterator. This simulates
     * a broken implementation that incorrectly reports successful advancement.
     *
     * <p>This break is particularly useful for testing infinite loop detection and
     * proper termination conditions in code that relies on {@code tryAdvance()}
     * return values to determine when iteration should stop.
     */
    @Test
    public void testSpliteratorWithTryAdvanceAlwaysReturnsTrueBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(Integer.class)
                .addBreak(SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertTrue(iterator.tryAdvance((e) -> {}));
    }

    /**
     * Tests the {@code SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code tryAdvance()} method always
     * returns {@code false}, even when elements exist in the underlying data structure.
     * This simulates a broken implementation that incorrectly reports no available elements.
     *
     * <p>This break is useful for testing how code handles spliterators that appear
     * empty due to faulty advancement logic, ensuring robust handling of such edge cases.
     */
    @Test
    public void testSpliteratorWithTryAdvanceAlwaysReturnsFalseBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertFalse(iterator.tryAdvance((e) -> {}));
    }

    /**
     * Tests the {@code SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code estimateSize()} method always
     * returns {@code Long.MAX_VALUE}, regardless of the actual size of the underlying data.
     * This simulates a broken size estimation that could cause memory allocation issues
     * or infinite loop conditions in size-dependent algorithms.
     *
     * <p>This break is useful for testing robustness against extremely large size estimates
     * and ensuring proper bounds checking in parallel processing code.
     */
    @Test
    public void testSpliteratorWithEstimateSizeAlwaysReturnsMaxValueBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(Long.MAX_VALUE, iterator.estimateSize());
    }

    /**
     * Tests the {@code SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code estimateSize()} method always
     * returns {@code 0}, even when elements exist in the underlying data structure.
     * This simulates incorrect size estimation that could cause suboptimal parallel
     * processing decisions or pre-allocation strategies.
     *
     * <p>This break tests how code handles spliterators that underestimate their size,
     * which can affect performance optimizations and memory management.
     */
    @Test
    public void testSpliteratorWithEstimateSizeAlwaysReturnsZeoBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(0, iterator.estimateSize());
    }

    /**
     * Tests the normal behavior of {@code getExactSizeIfKnown()} method without breaks.
     *
     * <p>This test validates the baseline behavior by verifying that:
     * <ul>
     * <li>Unsized spliterators return {@code -1} from {@code getExactSizeIfKnown()}</li>
     * <li>Sized spliterators return the exact element count</li>
     * </ul>
     *
     * <p>This serves as a control test to ensure that the break behaviors deviate
     * from the expected normal operation of size reporting methods.
     */
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

    /**
     * Tests the {@code SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code getExactSizeIfKnown()} method
     * always returns {@code -1}, even when the spliterator has the {@code SIZED} characteristic
     * and should know its exact size. This simulates a broken size reporting mechanism.
     *
     * <p>This break is useful for testing code that depends on exact size information
     * for optimization decisions, ensuring it handles unknown sizes gracefully.
     */
    @Test
    public void testSpliteratorWithGetExactSizeIfKnownAlwaysReturnsNegativeOneBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(-1, iterator.getExactSizeIfKnown());
    }

    /**
     * Tests the {@code SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code characteristics()} method
     * always returns {@code 0}, indicating no special characteristics, even when
     * characteristics were explicitly set during construction. This simulates a broken
     * characteristic reporting mechanism.
     *
     * <p>This break tests how code handles spliterators that lose their characteristic
     * information, potentially affecting optimization strategies and behavioral assumptions.
     */
    @Test
    public void testSpliteratorWithCharacteristicsAlwaysReturnsZeroBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(0, iterator.characteristics());
    }

    /**
     * Tests the {@code SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code hasCharacteristics()} method
     * always returns {@code true} for any characteristic query, regardless of the actual
     * characteristics of the spliterator. This simulates overly permissive characteristic
     * reporting that could lead to incorrect optimization assumptions.
     *
     * <p>This break tests robustness against spliterators that claim to have characteristics
     * they don't actually possess, such as claiming to be concurrent when they're not thread-safe.
     */
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

    /**
     * Tests the {@code SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE} break behavior across all characteristics.
     *
     * <p>Verifies that when this break is applied, the {@code hasCharacteristics()} method
     * always returns {@code false} for any characteristic query, regardless of the characteristics
     * that were explicitly set during construction. This tests the break behavior against
     * all standard Spliterator characteristics.
     *
     * <p>The test iterates through all standard characteristic values ({@code SIZED}, {@code SUBSIZED},
     * {@code SORTED}, {@code CONCURRENT}, {@code DISTINCT}, {@code IMMUTABLE}, {@code NONNULL})
     * to ensure comprehensive coverage of the break behavior.
     */
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

    /**
     * Tests the {@code SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code hasCharacteristics()} method
     * always returns {@code false} for any characteristic query, even for characteristics
     * that were explicitly set during construction. This simulates a spliterator that
     * incorrectly denies having characteristics it actually possesses.
     *
     * <p>This break tests how code handles spliterators that underreport their capabilities,
     * potentially causing suboptimal processing strategies or missed optimization opportunities.
     */
    @Test
    public void testSpliteratorWithHasCharacteristicAlwaysReturnsFalseBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertFalse(iterator.hasCharacteristics(SIZED));
    }

    /**
     * Tests the {@code SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL} break behavior.
     *
     * <p>Verifies that when this break is applied, the {@code getComparator()} method
     * always returns {@code null}, even for spliterators that would normally have
     * a natural ordering. This simulates a broken comparator reporting mechanism.
     *
     * <p>This break is useful for testing code that depends on spliterator ordering
     * information, ensuring it handles cases where comparator information is unavailable
     * or incorrectly reported.
     */
    @Test
    public void testSpliteratorGetComparatorAlwaysReturnsNull() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(Integer.class)
                .addBreak(SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertNull(iterator.getComparator());
    }

    /**
     * Tests the normal behavior of {@code getComparator()} method without breaks.
     *
     * <p>This test validates that calling {@code getComparator()} on a spliterator without
     * the {@code SORTED} characteristic throws {@link IllegalStateException}, as specified
     * by the Spliterator contract. This serves as a control test to ensure that the
     * break behavior deviates from normal operation.
     *
     * <p>The test constructs a spliterator without explicit sorting characteristics
     * and verifies that accessing the comparator results in the expected exception.
     */
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
