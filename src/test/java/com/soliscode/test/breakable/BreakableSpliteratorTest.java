package com.soliscode.test.breakable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.soliscode.test.assertions.collection.CollectionAssertions.assertIsEmpty;
import static com.soliscode.test.breakable.BreakableSpliterator.CONCURRENT;
import static com.soliscode.test.breakable.BreakableSpliterator.DISTINCT;
import static com.soliscode.test.breakable.BreakableSpliterator.IMMUTABLE;
import static com.soliscode.test.breakable.BreakableSpliterator.NONNULL;
import static com.soliscode.test.breakable.BreakableSpliterator.ORDERED;
import static com.soliscode.test.breakable.BreakableSpliterator.SIZED;
import static com.soliscode.test.breakable.BreakableSpliterator.SORTED;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_IS_ALWAYS_EMPTY;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION;
import static com.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableSpliterator.SUBSIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Comprehensive test suite for validating the behavior of [BreakableSpliterator] and its various breakable behaviors.
///
/// This test class verifies that the `BreakableSpliterator` class correctly implements programmatically
/// broken [Spliterator] behavior for testing purposes. The `BreakableSpliterator` is designed to
/// simulate faulty or edge-case implementations of the Spliterator interface, enabling thorough testing of
/// code that depends on proper Spliterator behavior.
/// ### Purpose and Use Cases
///
/// The `BreakableSpliterator` is particularly valuable for:
///
///   - Testing error handling in parallel processing code that uses Spliterators
///   - Validating robustness of stream operations against malformed spliterators
///   - Ensuring collection contract implementations handle broken spliterator behavior gracefully
///   - Testing edge cases in custom collection implementations
///   - Verifying defensive programming practices in spliterator-dependent code
///
/// ### Breakable Behaviors Tested
///
/// This test suite validates the following types of breakable behaviors:
/// #### Traversal Breaks
///
///   - `SPLITERATOR_IS_ALWAYS_EMPTY` - Spliterator appears empty regardless of underlying data
///   - `SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION` - tryAdvance ignores the action consumer
///   - `SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE/FALSE` - tryAdvance returns fixed boolean values
///   - `SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION` - forEachRemaining ignores actions
///
/// #### Splitting Breaks
///
///   - `SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL` - trySplit never succeeds in splitting
///
/// #### Size Estimation Breaks
///
///   - `SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE` - estimateSize returns `Long.MAX_VALUE`
///   - `SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO` - estimateSize always returns 0
///   - `SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE` - exact size is always unknown
///
/// #### Characteristics Breaks
///
///   - `SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO` - No characteristics reported
///   - `SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE/FALSE` - Fixed characteristic responses
///
/// #### Comparator Breaks
///
///   - `SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL` - Never provides a comparator
///
/// ### Testing Strategy
///
/// Each test method focuses on a specific break behavior and verifies that:
///
///   - The break is correctly applied to the spliterator instance
///   - The broken behavior persists across method calls
///   - The break doesn't affect unrelated spliterator functionality
///   - Edge cases and boundary conditions are handled appropriately
///
///
/// The tests use the [Breakables] builder pattern to construct spliterators with specific
/// breaks, then verify the expected broken behavior through direct method calls and assertions.
/// ### Integration with Testing Framework
///
/// These breakable spliterators are designed to be used in conjunction with collection contract
/// tests to ensure that collection implementations properly handle malformed spliterator behavior.
/// This is particularly important for collections that depend on spliterator characteristics for
/// optimization decisions or parallel processing strategies.
///
/// Usage example:
/// ```java
/// // Create a spliterator that always appears empty
/// BreakableIterable<String> iterable = Breakables.buildIterable("a", "b", "c")
///         .addBreak(SPLITERATOR_IS_ALWAYS_EMPTY)
///         .build();
/// Spliterator<String> spliterator = iterable.spliterator();
/// assertFalse(spliterator.tryAdvance(element -> { })); // Should not advance
/// assertEquals(0, spliterator.estimateSize()); // Should report size 0
/// ```
///
/// @author evanbergstrom
/// @see BreakableSpliterator
/// @see Breakables
/// @see Spliterator
/// @since 1.0
@SuppressWarnings("MagicConstant")
@DisplayName("Tests for BreakableSpliterator")
public class BreakableSpliteratorTest {

    static final int [] CHARACTERISTIC_VALUES = {
        0, SIZED, SUBSIZED, SORTED, CONCURRENT,  DISTINCT, IMMUTABLE, NONNULL
    };

    ///  Default constructor
    public BreakableSpliteratorTest() {
    }

    /// Tests the `SPLITERATOR_IS_ALWAYS_EMPTY` break behavior.
    ///
    /// Verifies that when this break is applied, the spliterator:
    ///
    ///   - Returns `false` from `tryAdvance()`, indicating no elements
    ///   - Returns `null` from `trySplit()`, indicating no splitting possible
    ///   - Returns `0` from `estimateSize()`, indicating empty collection
    ///   - Returns `0` from `getExactSizeIfKnown()`, indicating known empty size
    ///
    ///
    /// This break simulates a spliterator that appears completely empty regardless of
    /// the underlying data, useful for testing edge case handling in parallel processing.
    @Test
    @DisplayName("spliterator() with SPLITERATOR_IS_ALWAYS_EMPTY does not advance or split and reports size zero")
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

    /// Tests the `SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION` break behavior.
    ///
    /// Verifies that when this break is applied, the `forEachRemaining()` method
    /// does not invoke the provided action consumer, even though elements exist in the
    /// underlying data structure. This simulates a broken implementation that ignores
    /// the action parameter.
    ///
    /// The test confirms that after calling `forEachRemaining()`, the collection
    /// used to capture elements remains empty, proving the action was never called.
    @Test
    @DisplayName("forEachRemaining() with SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION does not call the action")
    public void testSpliteratorWithForEachRemainingDoesNotCallActionBreak() {
        Iterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)
                .build();
        Spliterator<Integer> iterator = iterable.spliterator();
        Collection<Integer> elements = new ArrayList<>();
        iterator.forEachRemaining(elements::add);
        assertIsEmpty(elements);
    }

    /// Tests the `SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL` break behavior.
    ///
    /// Verifies that when this break is applied, the `trySplit()` method always
    /// returns `null`, indicating that the spliterator cannot be split for parallel
    /// processing, even when the underlying data would normally support splitting.
    ///
    /// This break is useful for testing scenarios where parallel processing should
    /// gracefully degrade to sequential processing when splitting is not available.
    @Test
    @DisplayName("trySplit() with SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL returns null")
    public void testSpliteratorWithTrySplitAlwaysReturnsNullBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3, 4, 5)
                .addBreak(SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertNull(iterator.trySplit());
    }

    /// Tests the `SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION` break behavior.
    ///
    /// Verifies that when this break is applied, the `tryAdvance()` method
    /// does not invoke the provided action consumer, even though the method may still
    /// return appropriate boolean values. This simulates a broken implementation that
    /// advances through elements without actually processing them.
    ///
    /// The test confirms that after calling `tryAdvance()`, the collection
    /// used to capture elements remains empty, proving the action was never called.
    @Test
    @DisplayName("tryAdvance() with SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION does not call the action")
    public void testSpliteratorWithTryAdvanceDoesNotCallActionBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        Collection<Integer> elements = new ArrayList<>();
        iterator.tryAdvance(elements::add);
        assertIsEmpty(elements);
    }

    /// Tests the `SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE` break behavior.
    ///
    /// Verifies that when this break is applied, the `tryAdvance()` method always
    /// returns `true`, even when called on an empty spliterator. This simulates
    /// a broken implementation that incorrectly reports successful advancement.
    ///
    /// This break is particularly useful for testing infinite loop detection and
    /// proper termination conditions in code that relies on `tryAdvance()`
    /// return values to determine when iteration should stop.
    @Test
    @DisplayName("tryAdvance() with SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE returns true when empty")
    public void testSpliteratorWithTryAdvanceAlwaysReturnsTrueBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(Integer.class)
                .addBreak(SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertTrue(iterator.tryAdvance((e) -> {}));
    }

    /// Tests the `SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE` break behavior.
    ///
    /// Verifies that when this break is applied, the `tryAdvance()` method always
    /// returns `false`, even when elements exist in the underlying data structure.
    /// This simulates a broken implementation that incorrectly reports no available elements.
    ///
    /// This break is useful for testing how code handles spliterators that appear
    /// empty due to faulty advancement logic, ensuring robust handling of such edge cases.
    @Test
    @DisplayName("tryAdvance() with SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE returns false when not empty")
    public void testSpliteratorWithTryAdvanceAlwaysReturnsFalseBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_FALSE)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertFalse(iterator.tryAdvance((e) -> {}));
    }

    /// Tests the `SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE` break behavior.
    ///
    /// Verifies that when this break is applied, the `estimateSize()` method always
    /// returns `Long.MAX_VALUE`, regardless of the actual size of the underlying data.
    /// This simulates a broken size estimation that could cause memory allocation issues
    /// or infinite loop conditions in size-dependent algorithms.
    ///
    /// This break is useful for testing robustness against extremely large size estimates
    /// and ensuring proper bounds checking in parallel processing code.
    @Test
    @DisplayName("estimateSize() with SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE returns Long.MAX_VALUE")
    public void testSpliteratorWithEstimateSizeAlwaysReturnsMaxValueBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(Long.MAX_VALUE, iterator.estimateSize());
    }

    /// Tests the `SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO` break behavior.
    ///
    /// Verifies that when this break is applied, the `estimateSize()` method always
    /// returns `0`, even when elements exist in the underlying data structure.
    /// This simulates incorrect size estimation that could cause suboptimal parallel
    /// processing decisions or pre-allocation strategies.
    ///
    /// This break tests how code handles spliterators that underestimate their size,
    /// which can affect performance optimizations and memory management.
    @Test
    @DisplayName("estimateSize() with SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO returns zero when not empty")
    public void testSpliteratorWithEstimateSizeAlwaysReturnsZeoBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_ZERO)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(0, iterator.estimateSize());
    }

    /// Tests the normal behavior of `getExactSizeIfKnown()` method without breaks.
    ///
    /// This test validates the baseline behavior by verifying that:
    ///
    ///   - Unsized spliterators return `-1` from `getExactSizeIfKnown()`
    ///   - Sized spliterators return the exact element count
    ///
    ///
    /// This serves as a control test to ensure that the break behaviors deviate
    /// from the expected normal operation of size reporting methods.
    @Test
    @DisplayName("getExactSizeIfKnown() returns the size when sized and -1 when not, without a break")
    public void testSpliteratorWithGetExactSizeIfKnown() {
        // The spliterator reports what the backing list's does, so whether it is sized decides the answer.
        Spliterator<Integer> spliterator = Breakables.buildIterable(1, 2, 3).build().spliterator();
        assertEquals(spliterator.hasCharacteristics(SIZED) ? 3 : -1, spliterator.getExactSizeIfKnown());

        BreakableIterable<Integer> sizedIterable = Breakables.buildIterable(1, 2, 3)
                .setCharacteristics(SIZED)
                .build();
        assertEquals(3, sizedIterable.spliterator().getExactSizeIfKnown());
    }

    /// Tests the `SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE` break behavior.
    ///
    /// Verifies that when this break is applied, the `getExactSizeIfKnown()` method
    /// always returns `-1`, even when the spliterator has the `SIZED` characteristic
    /// and should know its exact size. This simulates a broken size reporting mechanism.
    ///
    /// This break is useful for testing code that depends on exact size information
    /// for optimization decisions, ensuring it handles unknown sizes gracefully.
    @Test
    @DisplayName("getExactSizeIfKnown() with SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE "
            + "returns -1")
    public void testSpliteratorWithGetExactSizeIfKnownAlwaysReturnsNegativeOneBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(-1, iterator.getExactSizeIfKnown());
    }

    /// Tests the `SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO` break behavior.
    ///
    /// Verifies that when this break is applied, the `characteristics()` method
    /// always returns `0`, indicating no special characteristics, even when
    /// characteristics were explicitly set during construction. This simulates a broken
    /// characteristic reporting mechanism.
    ///
    /// This break tests how code handles spliterators that lose their characteristic
    /// information, potentially affecting optimization strategies and behavioral assumptions.
    @Test
    @DisplayName("characteristics() with SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO returns zero")
    public void testSpliteratorWithCharacteristicsAlwaysReturnsZeroBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertEquals(0, iterator.characteristics());
    }

    /// Tests the `SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE` break behavior.
    ///
    /// Verifies that when this break is applied, the `hasCharacteristics()` method
    /// always returns `true` for any characteristic query, regardless of the actual
    /// characteristics of the spliterator. This simulates overly permissive characteristic
    /// reporting that could lead to incorrect optimization assumptions.
    ///
    /// This break tests robustness against spliterators that claim to have characteristics
    /// they don't actually possess, such as claiming to be concurrent when they're not thread-safe.
    @Test
    @DisplayName("hasCharacteristics() with SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE returns true for any "
            + "characteristic")
    public void testSpliteratorWithHasCharacteristicAlwaysReturnsTrueBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_TRUE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertTrue(iterator.hasCharacteristics(CONCURRENT));
    }

    /// Tests the `SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE` break behavior across all characteristics.
    ///
    /// Verifies that when this break is applied, the `hasCharacteristics()` method
    /// always returns `false` for any characteristic query, regardless of the characteristics
    /// that were explicitly set during construction. This tests the break behavior against
    /// all standard Spliterator characteristics.
    ///
    /// The test iterates through all standard characteristic values (`SIZED`, `SUBSIZED`,
    /// `SORTED`, `CONCURRENT`, `DISTINCT`, `IMMUTABLE`, `NONNULL`)
    /// to ensure comprehensive coverage of the break behavior.
    @Test
    @DisplayName("hasCharacteristics() with SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE returns false for "
            + "every characteristic")
    public void testSpliteratorWithHasCharacteristicWorks() {

        for (final int value : CHARACTERISTIC_VALUES){
            BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                    .addBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE)
                    .setCharacteristics(value)
                    .build();

            Spliterator<Integer> iterator = iterable.spliterator();
            assertFalse(iterator.hasCharacteristics(value));
        }
    }

    /// Tests the `SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE` break behavior.
    ///
    /// Verifies that when this break is applied, the `hasCharacteristics()` method
    /// always returns `false` for any characteristic query, even for characteristics
    /// that were explicitly set during construction. This simulates a spliterator that
    /// incorrectly denies having characteristics it actually possesses.
    ///
    /// This break tests how code handles spliterators that underreport their capabilities,
    /// potentially causing suboptimal processing strategies or missed optimization opportunities.
    @Test
    @DisplayName("hasCharacteristics() with SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE returns false for a "
            + "set characteristic")
    public void testSpliteratorWithHasCharacteristicAlwaysReturnsFalseBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_HAS_CHARACTERISTIC_ALWAYS_RETURNS_FALSE)
                .setCharacteristics(SIZED)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertFalse(iterator.hasCharacteristics(SIZED));
    }

    /// Tests the `SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL` break behavior.
    ///
    /// Verifies that when this break is applied, the `getComparator()` method
    /// always returns `null`, even for spliterators that would normally have
    /// a natural ordering. This simulates a broken comparator reporting mechanism.
    ///
    /// This break is useful for testing code that depends on spliterator ordering
    /// information, ensuring it handles cases where comparator information is unavailable
    /// or incorrectly reported.
    @Test
    @DisplayName("getComparator() with SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL returns null")
    public void testSpliteratorGetComparatorAlwaysReturnsNull() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(Integer.class)
                .addBreak(SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL)
                .build();

        Spliterator<Integer> iterator = iterable.spliterator();
        assertNull(iterator.getComparator());
    }

    /// Tests the normal behavior of `getComparator()` method without breaks.
    ///
    /// This test validates that calling `getComparator()` on a spliterator without
    /// the `SORTED` characteristic throws [IllegalStateException], as specified
    /// by the Spliterator contract. This serves as a control test to ensure that the
    /// break behavior deviates from normal operation.
    ///
    /// The test constructs a spliterator without explicit sorting characteristics
    /// and verifies that accessing the comparator results in the expected exception.
    @Test
    @DisplayName("getComparator() throws IllegalStateException for a spliterator that is not SORTED")
    public void testGetComparator() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(Integer.class)
                .build();

        assertThrows(IllegalStateException.class, () -> {
            Spliterator<Integer> iterator = iterable.spliterator();
            assertNotNull(iterator.getComparator());
        });
    }


    // ========== The breaks added by #38 ==========

    private static BreakableSpliterator<Integer> over(final List<Integer> elements, final Break b) {
        Set<Break> breaks = b == null ? Set.of() : Set.of(b);
        Spliterator<Integer> delegate = new ArrayList<>(elements).spliterator();
        return new BreakableSpliterator<>(delegate, breaks, Map.of(), delegate.characteristics(), false);
    }

    private static List<Integer> drain(final Spliterator<Integer> spliterator) {
        List<Integer> elements = new ArrayList<>();
        spliterator.forEachRemaining(elements::add);
        return elements;
    }

    @Test
    @DisplayName("tryAdvance(null) with SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION returns false; without, throws")
    public void tryAdvanceIgnoresNullActionBreak() {
        assertFalse(over(List.of(1, 2), BreakableSpliterator.SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION)
                .tryAdvance(null));
        assertThrows(NullPointerException.class, () -> over(List.of(1, 2), null).tryAdvance(null));
    }

    @Test
    @DisplayName("tryAdvance() with SPLITERATOR_TRY_ADVANCE_SWALLOWS_ACTION_EXCEPTION returns true and relays nothing")
    public void tryAdvanceSwallowsActionExceptionBreak() {
        Spliterator<Integer> spliterator = over(List.of(1, 2),
                BreakableSpliterator.SPLITERATOR_TRY_ADVANCE_SWALLOWS_ACTION_EXCEPTION);
        assertTrue(spliterator.tryAdvance(e -> {
            throw new IllegalStateException("thrown by the action");
        }));
        assertEquals(List.of(2), drain(spliterator));
        assertThrows(NullPointerException.class, () -> over(List.of(1),
                BreakableSpliterator.SPLITERATOR_TRY_ADVANCE_SWALLOWS_ACTION_EXCEPTION).tryAdvance(null));
    }

    @Test
    @DisplayName("forEachRemaining(null) with SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION returns")
    public void forEachRemainingIgnoresNullActionBreak() {
        over(List.of(1, 2), BreakableSpliterator.SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION)
                .forEachRemaining(null);
        assertThrows(NullPointerException.class, () -> over(List.of(1, 2), null).forEachRemaining(null));
    }

    @Test
    @DisplayName("forEachRemaining() with SPLITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION stops and relays "
            + "nothing")
    public void forEachRemainingSwallowsActionExceptionBreak() {
        Break b = BreakableSpliterator.SPLITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION;
        List<Integer> visited = new ArrayList<>();
        over(List.of(1, 2, 3), b).forEachRemaining(e -> {
            visited.add(e);
            throw new IllegalStateException("thrown by the action");
        });
        assertEquals(List.of(1), visited);
        assertThrows(NullPointerException.class, () -> over(List.of(1), b).forEachRemaining(null));
    }

    @Test
    @DisplayName("SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE passes the first element again at the next step")
    public void returnsFirstElementTwiceBreak() {
        Break b = BreakableSpliterator.SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE;
        assertEquals(List.of(1, 1, 2, 3), drain(over(List.of(1, 2, 3), b)));
        Spliterator<Integer> stepping = over(List.of(1, 2, 3), b);
        List<Integer> passed = new ArrayList<>();
        while (stepping.tryAdvance(passed::add)) {
            assertTrue(passed.size() <= 4);
        }
        assertEquals(List.of(1, 1, 2, 3), passed);
        Spliterator<Integer> mixed = over(List.of(1, 2, 3), b);
        List<Integer> both = new ArrayList<>();
        mixed.tryAdvance(both::add);
        mixed.forEachRemaining(both::add);
        assertEquals(List.of(1, 1, 2, 3), both);
    }

    @Test
    @DisplayName("SPLITERATOR_FIRST_ELEMENT_IS_NULL passes null in place of the first element only")
    public void firstElementIsNullBreak() {
        Break b = BreakableSpliterator.SPLITERATOR_FIRST_ELEMENT_IS_NULL;
        assertEquals(Arrays.asList(null, 2, 3), drain(over(List.of(1, 2, 3), b)));
        Spliterator<Integer> stepping = over(List.of(1, 2, 3), b);
        List<Integer> passed = new ArrayList<>();
        stepping.tryAdvance(passed::add);
        stepping.forEachRemaining(passed::add);
        assertEquals(Arrays.asList(null, 2, 3), passed);
    }

    @Test
    @DisplayName("trySplit() with SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX returns the rest and keeps the prefix")
    public void trySplitReturnsSuffixBreak() {
        Spliterator<Integer> spliterator = over(List.of(1, 2, 3, 4),
                BreakableSpliterator.SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX);
        Spliterator<Integer> part = spliterator.trySplit();
        assertNotNull(part);
        assertEquals(List.of(3, 4), drain(part));
        assertEquals(List.of(1, 2), drain(spliterator));
    }

    @Test
    @DisplayName("trySplit() with SPLITERATOR_TRY_SPLIT_LOSES_PART returns null and loses the part")
    public void trySplitLosesPartBreak() {
        Spliterator<Integer> spliterator = over(List.of(1, 2, 3, 4),
                BreakableSpliterator.SPLITERATOR_TRY_SPLIT_LOSES_PART);
        assertNull(spliterator.trySplit());
        assertEquals(List.of(3, 4), drain(spliterator));
    }

    @Test
    @DisplayName("trySplit() with SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL returns empty parts where it would be null")
    public void trySplitNeverReturnsNullBreak() {
        Spliterator<Integer> spliterator = over(List.of(1),
                BreakableSpliterator.SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL);
        for (int i = 0; i < 3; i++) {
            Spliterator<Integer> part = spliterator.trySplit();
            assertNotNull(part);
            assertEquals(List.of(), drain(part));
        }
        assertEquals(List.of(1), drain(spliterator));
    }

    @Test
    @DisplayName("trySplit() with SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED returns a part that is not SIZED")
    public void trySplitPartIsNotSizedBreak() {
        Spliterator<Integer> spliterator = over(List.of(1, 2, 3, 4),
                BreakableSpliterator.SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED);
        Spliterator<Integer> part = spliterator.trySplit();
        assertNotNull(part);
        assertFalse(part.hasCharacteristics(SIZED));
        assertFalse(part.hasCharacteristics(SUBSIZED));
        assertEquals(-1, part.getExactSizeIfKnown());
        assertEquals(List.of(1, 2), drain(part));
        assertTrue(spliterator.hasCharacteristics(SIZED | SUBSIZED));
    }

    @Test
    @DisplayName("estimateSize() with SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT is one more than before the split")
    public void estimateSizeGrowsAfterSplitBreak() {
        Spliterator<Integer> spliterator = over(List.of(1, 2, 3, 4),
                BreakableSpliterator.SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT);
        assertEquals(4, spliterator.estimateSize());
        assertNotNull(spliterator.trySplit());
        assertEquals(5, spliterator.estimateSize());
        assertEquals(4, over(List.of(1, 2, 3, 4), null).estimateSize());
    }

    @Test
    @DisplayName("characteristics() with SPLITERATOR_CHARACTERISTICS_CHANGE_AFTER_TRAVERSAL is 0 once traversal begins")
    public void characteristicsChangeAfterTraversalBreak() {
        Spliterator<Integer> spliterator = over(List.of(1, 2),
                BreakableSpliterator.SPLITERATOR_CHARACTERISTICS_CHANGE_AFTER_TRAVERSAL);
        int before = spliterator.characteristics();
        assertTrue(before != 0);
        spliterator.tryAdvance(e -> { });
        assertEquals(0, spliterator.characteristics());
        // getExactSizeIfKnown() reads the characteristics as they follow the delegate's, which the break leaves alone.
        assertEquals(1, spliterator.getExactSizeIfKnown());
    }

    @Test
    @DisplayName("characteristics() with SPLITERATOR_REPORTS_IMMUTABLE or _CONCURRENT adds that characteristic")
    public void reportsImmutableAndConcurrentBreaks() {
        int plain = over(List.of(1), null).characteristics();
        assertEquals(plain | IMMUTABLE,
                over(List.of(1), BreakableSpliterator.SPLITERATOR_REPORTS_IMMUTABLE).characteristics());
        assertEquals(plain | CONCURRENT,
                over(List.of(1), BreakableSpliterator.SPLITERATOR_REPORTS_CONCURRENT).characteristics());
    }

    @Test
    @DisplayName("characteristics() follow the delegate's: over a TreeSet, SIZED goes after a split")
    public void characteristicsFollowTheDelegateAfterSplit() {
        TreeSet<Integer> set = new TreeSet<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));
        Spliterator<Integer> delegate = set.spliterator();
        Spliterator<Integer> spliterator = new BreakableSpliterator<>(delegate, Set.of(), Map.of(),
                delegate.characteristics() | NONNULL, false);
        assertTrue(spliterator.hasCharacteristics(SIZED | NONNULL));
        assertEquals(8, spliterator.getExactSizeIfKnown());
        assertNotNull(spliterator.trySplit());
        assertFalse(delegate.hasCharacteristics(SIZED));
        assertFalse(spliterator.hasCharacteristics(SIZED), "SIZED is dropped with the delegate's");
        assertTrue(spliterator.hasCharacteristics(NONNULL), "an added characteristic stays");
        assertEquals(-1, spliterator.getExactSizeIfKnown());
    }

    @Test
    @DisplayName("characteristics() keep a characteristic taken away from the delegate's away")
    public void characteristicsKeepARemovedOneAway() {
        Spliterator<Integer> delegate = new ArrayList<>(List.of(1, 2, 3, 4)).spliterator();
        Spliterator<Integer> spliterator = new BreakableSpliterator<>(delegate, Set.of(), Map.of(),
                delegate.characteristics() & ~SIZED, false);
        assertFalse(spliterator.hasCharacteristics(SIZED));
        assertEquals(-1, spliterator.getExactSizeIfKnown());
        assertNotNull(spliterator.trySplit());
        assertFalse(spliterator.hasCharacteristics(SIZED));
    }

    @Test
    @DisplayName("getComparator() with SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER returns natural order for null")
    public void getComparatorReturnsNaturalOrderBreak() {
        Spliterator<Integer> delegate = new TreeSet<>(List.of(1, 2)).spliterator();
        Spliterator<Integer> broken = new BreakableSpliterator<>(delegate,
                Set.of(BreakableSpliterator.SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER), Map.of(),
                delegate.characteristics(), false);
        Comparator<? super Integer> comparator = broken.getComparator();
        assertNotNull(comparator);
        assertTrue(comparator.compare(1, 2) < 0);
        Comparator<Integer> reverse = Comparator.reverseOrder();
        TreeSet<Integer> reversed = new TreeSet<>(reverse);
        reversed.addAll(List.of(1, 2));
        Spliterator<Integer> reversedDelegate = reversed.spliterator();
        assertEquals(reverse, new BreakableSpliterator<>(reversedDelegate,
                Set.of(BreakableSpliterator.SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER), Map.of(),
                reversedDelegate.characteristics(), false).getComparator());
    }

    @Test
    @DisplayName("characteristics() with the LEAVE_OUT breaks leaves out ORDERED or SIZED, and nothing else")
    public void leaveOutBreaks() {
        int plain = over(List.of(1, 2), null).characteristics();
        Spliterator<Integer> noOrder = over(List.of(1, 2),
                BreakableSpliterator.SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_ORDERED);
        assertEquals(plain & ~ORDERED, noOrder.characteristics());
        Spliterator<Integer> noSize = over(List.of(1, 2),
                BreakableSpliterator.SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_SIZED);
        assertEquals(plain & ~SIZED, noSize.characteristics());
        assertEquals(2, noSize.getExactSizeIfKnown(), "getExactSizeIfKnown() is not changed");
    }

    @Test
    @DisplayName("getComparator() with SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS throws, even where SORTED")
    public void getComparatorAlwaysThrowsBreak() {
        Spliterator<Integer> delegate = new TreeSet<>(List.of(1, 2)).spliterator();
        Spliterator<Integer> broken = new BreakableSpliterator<>(delegate,
                Set.of(BreakableSpliterator.SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS), Map.of(),
                delegate.characteristics(), false);
        assertTrue(broken.hasCharacteristics(SORTED));
        assertThrows(IllegalStateException.class, broken::getComparator);
    }
}
