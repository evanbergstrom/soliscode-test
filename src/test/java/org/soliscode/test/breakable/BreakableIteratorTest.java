package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.assertions.Assertions.assertThrowsDifferent;
import static org.soliscode.test.assertions.collection.CollectionAssertions.*;
import static org.soliscode.test.breakable.BreakableIterator.*;

/// Tests for the `BreakableIterator` class. These tests determine if the breaks supported by this class result in the
/// behavior expected.
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableIterator
@DisplayName("Tests for BreakableIterator")
public class BreakableIteratorTest {

    /// Test that the `ITERATOR_IS_ALWAYS_EMPTY` break causes the iterator not iterate over any elements.
    /// @see BreakableIterable#iterator()
    @Test
    @DisplayName("Test the iterator with the ITERATOR_IS_ALWAYS_EMPTY break")
    public void testIteratorWithAlwaysEmptyBreak() {
        BreakableIterable<Integer> empty = Breakables.buildIterable(Integer.class)
                .addBreak(ITERATOR_IS_ALWAYS_EMPTY)
                .build();

        BreakableIterable<Integer> nonEmpty = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_IS_ALWAYS_EMPTY)
                .build();
        assertFalse(nonEmpty.iterator().hasNext());
    }

    /// Test that the `ITERATOR_SKIPS_FIRST_ELEMENT` break causes the iterator to skip over the first element.
    /// @see BreakableIterable#iterator()
    @Test
    @DisplayName("Test the iterator with the ITERATOR_SKIPS_FIRST_ELEMENT break")
    public void testIteratorWithSkipsFirstElementBreak() {
        BreakableIterable<Integer> empty = Breakables.buildIterable(Integer.class)
                .addBreak(ITERATOR_SKIPS_FIRST_ELEMENT)
                .build();

        List<Integer> emptyActual = new ArrayList<>();
        for(Integer e : empty) {
            emptyActual.add(e);
        }
        assertContainsSame(List.of(), emptyActual);

        BreakableIterable<Integer> nonEmpty = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_SKIPS_FIRST_ELEMENT)
                .build();
        List<Integer> nonEmptyActual = new ArrayList<>();
        for(Integer e : nonEmpty) {
            nonEmptyActual.add(e);
        }
        assertContainsSame(List.of(2,3), nonEmptyActual);
    }

    /// Test that the `ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE` break causes the `hasNext` method to always return `true`.
    /// @see BreakableIterator#hasNext()
    @Test
    @DisplayName("Test the `hasNext` method with the ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE break")
    public void testIteratorWithHasNextAlwaysReturnsTrue() {
        BreakableIterable<Integer> empty = Breakables.buildIterable(Integer.class)
                .addBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE)
                .build();
        assertTrue(empty.iterator().hasNext());

        BreakableIterable<Integer> nonEmpty = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE)
                .build();
        Iterator<Integer> iterator = nonEmpty.iterator();
        iterator.next();
        iterator.next();
        iterator.next();
        assertTrue(iterator.hasNext());
    }

    /// Test that the `ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE` break causes the `hasNext` method to always return `false`.
    /// @see BreakableIterator#hasNext()
    @DisplayName("Test the `hasNext` method with the ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE break")
    @Test
    public void testIteratorWithHasNextAlwaysReturnsFalseBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE)
                .build();
        Iterator<Integer> iterator = iterable.iterator();
        assertFalse(iterator.hasNext());
    }

    /// Test that the `ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE` break causes the `hasNext` method to return the
    /// opposite value..
    /// @see BreakableIterator#hasNext()
    @Test
    @DisplayName("Test the `hasNext` method with the ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE break")
    public void testIteratorWithHasNextReturnsOppositeValueBreak() {
        BreakableIterable<Integer> empty = Breakables.buildIterable(Integer.class)
                .addBreak(ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE)
                .build();
        assertTrue(empty.iterator().hasNext());

        BreakableIterable<Integer> nonEmpty = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE)
                .build();
        Iterator<Integer> iterator = nonEmpty.iterator();
        assertFalse(iterator.hasNext());
        iterator.next();
        assertFalse(iterator.hasNext());
        iterator.next();
        assertFalse(iterator.hasNext());
        iterator.next();
        assertTrue(iterator.hasNext());
    }

    /// Test that the `ITERATOR_NEXT_ALWAYS_RETURNS_NULL` break causes the `next` method to always return `null`
    /// @see BreakableIterator#next()
    @DisplayName("Test the `next` method with the ITERATOR_NEXT_ALWAYS_RETURNS_NULL break")
    @Test
    public void testIteratorWithNextAlwaysReturnsNullBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_NEXT_ALWAYS_RETURNS_NULL)
                .build();
        Iterator<Integer> iterator = iterable.iterator();
        assertNull(iterator.next());
    }

    /// Test that the `ITERATOR_NEXT_THROWS_WRONG_EXCEPTION` break causes the `next` method to always return `null`
    /// @see BreakableIterator#next()
    @Test
    @DisplayName("Test the `next` method with the ITERATOR_NEXT_THROWS_WRONG_EXCEPTION break")
    public void testIteratorWithNextThrowsWrongExceptionBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(Integer.class)
                .addBreak(ITERATOR_NEXT_THROWS_WRONG_EXCEPTION)
                .build();
        Iterator<Integer> iterator = iterable.iterator();
        assertThrowsDifferent(NoSuchElementException.class, iterator::next);
    }

    /// Test that the `ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT` break causes the `remove` method to not remove the
    /// element at the iterators current position.
    /// @see BreakableIterator#remove()
    @Test
    @DisplayName("Test the `remove` method with the ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT break")
    public void testIteratorWithRemoveDoesNotRemoveElementBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT)
                .build();
        Iterator<Integer> iterator = iterable.iterator();
        iterator.next();
        iterator.remove();
        assertContains(2, iterable);
    }


    /// Test that the `ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION` break causes the `forEachRemaining` method to
    /// not call the action on any elements.
    /// @see BreakableIterator#forEachRemaining(Consumer)
    @Test
    @DisplayName("Test the `forEachRemaining` method with the ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION break")
    public void testIteratorForEachRemainingWithDoesNotCallActionBreak() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)
                .build();
        Iterator<Integer> iterator = iterable.iterator();
        Collection<Integer> elements = new ArrayList<>();
        iterator.forEachRemaining(elements::add);
        assertIsEmpty(elements);
    }
}
