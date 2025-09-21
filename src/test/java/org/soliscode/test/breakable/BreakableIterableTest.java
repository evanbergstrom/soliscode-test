package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.soliscode.test.assertions.Assertions.assertNotInstanceOf;
import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSame;
import static org.soliscode.test.breakable.BreakableIterable.*;
import static org.soliscode.test.breakable.BreakableIterator.*;
import static org.soliscode.test.breakable.BreakableSpliterator.*;

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
    public @NonNull CollectionProvider<Integer, BreakableIterable<Integer>> provider() {
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
        iterable.forEach(AssertActions.consumeNone());
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

    // Tests for Iterator Break Delegation

    /// Test that iterator breaks are properly delegated to the BreakableIterator.
    @Test
    @DisplayName("Test iterator break delegation - ITERATOR_IS_ALWAYS_EMPTY")
    public void testIteratorBreakDelegation() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_IS_ALWAYS_EMPTY)
                .build();

        Iterator<Integer> iterator = iterable.iterator();
        Collection<Integer> found = new ArrayList<>();
        while (iterator.hasNext()) {
            found.add(iterator.next());
        }
        
        assertContainsSame(Collections.emptyList(), found);
    }

    /// Test that iterator next throws exception break is properly delegated when iterator is empty.
    @Test
    @DisplayName("Test iterator break delegation - ITERATOR_NEXT_THROWS_WRONG_EXCEPTION")
    public void testIteratorNextThrowsExceptionBreakDelegation() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(Integer.class)
                .addBreak(ITERATOR_NEXT_THROWS_WRONG_EXCEPTION)
                .build();

        Iterator<Integer> iterator = iterable.iterator();
        assertThrows(RuntimeException.class, iterator::next);
    }

    // Tests for Spliterator Break Delegation

    /// Test that spliterator breaks are properly delegated to the BreakableSpliterator.
    @Test
    @DisplayName("Test spliterator break delegation - SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION")
    public void testSpliteratorBreakDelegation() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)
                .build();

        Spliterator<Integer> spliterator = iterable.spliterator();
        Collection<Integer> found = new ArrayList<>();
        spliterator.forEachRemaining(found::add);
        
        assertContainsSame(Collections.emptyList(), found);
    }

    // Tests for Edge Cases

    /// Test forEach breaks with empty iterable.
    @Test
    @DisplayName("Test forEach breaks with empty iterable")
    public void testForEachBreaksWithEmptyIterable() {
        BreakableIterable<Integer> emptyIterable = Breakables.buildIterable(Integer.class)
                .addBreak(FOR_EACH_SKIPS_FIRST_ELEMENT)
                .build();

        Collection<Integer> found = new ArrayList<>();
        emptyIterable.forEach(found::add);
        assertContainsSame(Collections.emptyList(), found);
    }

    /// Test forEach with single element and skip first break.
    @Test
    @DisplayName("Test forEach with single element and skip first break")
    public void testForEachSkipFirstWithSingleElement() {
        BreakableIterable<Integer> singleElement = Breakables.buildIterable(1)
                .addBreak(FOR_EACH_SKIPS_FIRST_ELEMENT)
                .build();

        Collection<Integer> found = new ArrayList<>();
        singleElement.forEach(found::add);
        assertContainsSame(Collections.emptyList(), found);
    }

    /// Test forEach with single element and skip last break.
    @Test
    @DisplayName("Test forEach with single element and skip last break")
    public void testForEachSkipLastWithSingleElement() {
        BreakableIterable<Integer> singleElement = Breakables.buildIterable(1)
                .addBreak(FOR_EACH_SKIPS_LAST_ELEMENT)
                .build();

        Collection<Integer> found = new ArrayList<>();
        singleElement.forEach(found::add);
        assertContainsSame(Collections.emptyList(), found);
    }

    // Tests for Combined Breaks

    /// Test multiple forEach breaks together (should respect priority).
    @Test
    @DisplayName("Test multiple forEach breaks - does not call action takes priority")
    public void testMultipleForEachBreaks() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(FOR_EACH_DOES_NOT_CALL_ACTION)
                .addBreak(FOR_EACH_SKIPS_FIRST_ELEMENT)
                .build();

        iterable.forEach(AssertActions.consumeNone());
    }

    // Tests for Provider Methods

    /// Test the static iterableProvider method.
    @Test
    @DisplayName("Test static iterableProvider method")
    public void testIterableProvider() {
        CollectionProvider<Integer, BreakableIterable<Integer>> provider = 
                BreakableIterable.iterableProvider(elementProvider());
        
        BreakableIterable<Integer> empty = provider.emptyInstance();
        BreakableIterable<Integer> withElements = provider.createInstance(new Integer[]{1, 2, 3});
        
        assertContainsSame(Collections.emptyList(), empty);
        assertContainsSame(List.of(1, 2, 3), withElements);
    }

    /// Test the static iterableProvider method with breaks.
    @Test
    @DisplayName("Test static iterableProvider method with breaks")
    public void testIterableProviderWithBreaks() {
        Set<Break> breaks = Set.of(FOR_EACH_DOES_NOT_CALL_ACTION);
        CollectionProvider<Integer, BreakableIterable<Integer>> provider = 
                BreakableIterable.iterableProvider(elementProvider(), breaks);
        
        BreakableIterable<Integer> iterable = provider.createInstance(new Integer[]{1, 2, 3});
        
        assertContainsSame(breaks, iterable.breaks());
        iterable.forEach(AssertActions.consumeNone());
    }

    // Tests for Object Contract

    /// Test equals method with same content but different characteristics.
    @Test
    @DisplayName("Test equals method")
    public void testEquals() {
        BreakableIterable<Integer> iterable1 = Breakables.buildIterable(1, 2, 3)
                .setCharacteristics(ORDERED)
                .build();
        
        BreakableIterable<Integer> iterable2 = Breakables.buildIterable(1, 2, 3)
                .setCharacteristics(ORDERED)
                .build();
        
        BreakableIterable<Integer> iterable3 = Breakables.buildIterable(1, 2, 3)
                .setCharacteristics(SIZED)
                .build();

        assertEquals(iterable1, iterable2);
        assertNotEquals(iterable1, iterable3);
    }

    /// Test unbroken method returns original iterable.
    @Test
    @DisplayName("Test unbroken method")
    public void testUnbroken() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(FOR_EACH_DOES_NOT_CALL_ACTION)
                .build();

        Iterable<Integer> unbroken = iterable.unbroken();
        Collection<Integer> found = new ArrayList<>();
        unbroken.forEach(found::add);
        
        assertContainsSame(List.of(1, 2, 3), found);
    }
}
