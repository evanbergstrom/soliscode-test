package org.soliscode.test.breakable;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.assertions.actions.AssertActions;
import org.soliscode.test.contract.CollectionMethods;
import org.soliscode.test.contract.iterable.IterableContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Spliterator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.Assertions.assertNotInstanceOf;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSame;
import static org.soliscode.test.breakable.BreakableIterable.*;
import static org.soliscode.test.breakable.BreakableSpliterator.SPLITERATOR_IS_ALWAYS_EMPTY;

/// Tests for the `BreakableIterable` class. These tests determine if the breaks supported by this class result in the
/// behavior expected.
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableIterable
@DisplayName("Tests for BreakableIterable")
public class BreakableIterableTest extends AbstractTest
        implements IterableContract<Integer, BreakableIterable<Integer>>, WithIntegerElement {

    @Override
    public @NotNull CollectionProvider<Integer, BreakableIterable<Integer>> provider() {
        return CollectionProviders.from(
                BreakableIterable::new,
                BreakableIterable::new,
                BreakableIterable::new,
                elementProvider()
        );
    }

    /// Test that the copy constructor copies the elements, breaks, and characteristics.
    @Test
    @DisplayName("The copy constructor creates a correct copy.")
    void testCopyConstructor() {
        BreakableIterable<Integer> original = Breakables.buildIterable(1, 2, 3)
                .setCharacteristics(ORDERED)
                .addBreak(SPLITERATOR_IS_ALWAYS_EMPTY)
                .build();

        BreakableIterable<Integer> copy = new BreakableIterable<>(original);

        assertContainsSame(original, copy);
        assertContainsSame(original.breaks(), copy.breaks());
        assertEquals(original.spliterator().characteristics(), copy.spliterator().characteristics());
    }

    // Tests for Breaks

    /// Test that the `FOR_EACH_DOES_NOT_CALL_ACTION` break causes the iterable to never call the action.
    /// @see BreakableIterable#forEach(Consumer)
    @Test
    @DisplayName("Test the `forEach` method with the FOR_EACH_DOES_NOT_CALL_ACTION break")
    public void testForEachWithDoesNotCallActionBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(FOR_EACH_DOES_NOT_CALL_ACTION)
                .build();
        iterable.forEach(AssertActions.assertConsumeNone());
    }

    /// Test that the `FOR_EACH_SKIPS_FIRST_ELEMENT` break causes the `forEach` method to skip the first element.
    /// @see BreakableIterable#forEach(Consumer)
    @Test
    @DisplayName("Test the `forEach` method with the FOR_EACH_SKIPS_FIRST_ELEMENT break")
    public void testForEachWithSkipsFirstElementBreak() {
        BreakableIterable<Integer> nonEmpty = Breakables.buildIterable(1, 2, 3)
                .addBreak(FOR_EACH_SKIPS_FIRST_ELEMENT)
                .build();

        Collection<Integer> found = new ArrayList<>();
        nonEmpty.forEach(found::add);
        assertContainsSame(List.of(2, 3), found);
    }

    /// Test that the `FOR_EACH_SKIPS_LAST_ELEMENT` break causes the `forEach` method to skip the last element.
    /// @see BreakableIterable#forEach(Consumer)
    @Test
    @DisplayName("Test the `forEach` method with the FOR_EACH_SKIPS_LAST_ELEMENT break")
    public void testForEachWithSkipsLastElementBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(FOR_EACH_SKIPS_LAST_ELEMENT)
                .build();

        Collection<Integer> found = new ArrayList<>();
        iterable.forEach(found::add);
        assertContainsSame(List.of(1, 2), found);
    }

    /// Test that the `FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT` break causes the `forEach` method to throw the
    /// wrong exception when the argument is `null`.
    /// @see BreakableIterable#forEach(Consumer)
    @Test
    @DisplayName("Test the `forEach` method with the FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT break")
    public void testForEachWithThrowsWrongExceptionBreak() {
        try {
            BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                    .addBreak(FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT)
                    .build();

            iterable.forEach(null);
        } catch (Exception e) {
            assertNotInstanceOf(NullPointerException.class, e);
        }
    }

    @Test
    public void testBuilderAddElements() {
        BreakableIterable<Integer> actual1 = Breakables.buildIterable(Integer.class)
                .addElements(List.of(1,2,3))
                .build();

        assertContainsSame(List.of(1,2,3), actual1);

        BreakableIterable<Integer> actual2 = Breakables.buildIterable(Integer.class)
                .addElements(1,2,3)
                .build();

        assertContainsSame(List.of(1,2,3), actual2);
    }

    @ParameterizedTest
    @ValueSource(ints = {ORDERED, DISTINCT, SORTED, SIZED, SUBSIZED, NONNULL, IMMUTABLE, CONCURRENT})
    public void testBuilderSetCharacteristics(final int characteristics) {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .setCharacteristics(characteristics)
                .build();

        assertEquals(characteristics, iterable.spliterator().characteristics());
    }

    @Test
    public void testBuilderCopy() {
        BreakableIterable.Builder<Integer> builder = Breakables.buildIterable(1, 2, 3)
                .setCharacteristics(ORDERED)
                .addBreak(SPLITERATOR_IS_ALWAYS_EMPTY);

        BreakableIterable.Builder<Integer> builderCopy = builder.copy();

        BreakableIterable<Integer> original = builder.build();
        BreakableIterable<Integer> copy = builderCopy.build();

        assertContainsSame(original, copy);
        assertContainsSame(original.breaks(), copy.breaks());
        assertEquals(original.spliterator().characteristics(), copy.spliterator().characteristics());
    }

    // Tests for Method Support

    @Test
    public void testIteratorRemoveWithoutSupport() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .doesNotSupport(CollectionMethods.IteratorRemove)
                .build();

        final Iterator<?> iterator = iterable.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, () -> {
            iterator.remove();
        });
    }

    @Test
    public void testIteratorForEachRemainingWithoutSupport() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .doesNotSupport(CollectionMethods.IteratorForEachRemaining)
                .build();

        final Iterator<?> iterator = iterable.iterator();
        assertThrows(UnsupportedOperationException.class, () -> {
            iterator.forEachRemaining((e) -> {});
        });
    }
}
