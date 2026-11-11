package com.soliscode.test.breakable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.iterable.IterableMethods;

import static com.soliscode.test.assertions.Assertions.assertThrowsDifferent;
import static com.soliscode.test.assertions.collection.CollectionAssertions.assertContainsSame;
import static com.soliscode.test.assertions.collection.CollectionAssertions.assertEquals;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_HAS_NEXT_ADVANCES;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_IS_ALWAYS_EMPTY;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_NEXT_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_NEXT_REQUIRES_HAS_NEXT;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_NEXT_THROWS_WRONG_EXCEPTION;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_REMOVE_IGNORES_ILLEGAL_STATE;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED;
import static com.soliscode.test.breakable.BreakableIterator.ITERATOR_SKIPS_FIRST_ELEMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for the `BreakableIterator` class. These tests determine if the breaks supported by this class result in the
/// behavior expected.
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableIterator
@DisplayName("Tests for BreakableIterator")
public class BreakableIteratorTest {

    /// Default constructor.
    public BreakableIteratorTest() {
        super();
    }

    /// Test that the `ITERATOR_SKIPS_FIRST_ELEMENT` break causes the iterator to skip over the first element.
    ///
    /// @see BreakableIterable#iterator()
    @Test
    @DisplayName("Test the iterator with the ITERATOR_SKIPS_FIRST_ELEMENT break")
    public void testIteratorWithSkipsFirstElementBreak() {
        BreakableIterable<Integer> empty = Breakables.buildIterable(Integer.class)
                .addBreak(ITERATOR_SKIPS_FIRST_ELEMENT)
                .build();

        List<Integer> emptyActual = new ArrayList<>();
        for (final Integer e : empty) {
            emptyActual.add(e);
        }
        assertContainsSame(List.of(), emptyActual);

        BreakableIterable<Integer> nonEmpty = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_SKIPS_FIRST_ELEMENT)
                .build();
        List<Integer> nonEmptyActual = new ArrayList<>();
        for (final Integer e : nonEmpty) {
            nonEmptyActual.add(e);
        }
        assertContainsSame(List.of(2, 3), nonEmptyActual);
    }

    // ========== hasNext Tests ==========

    /// Test that the `ITERATOR_IS_ALWAYS_EMPTY` break causes the iterator not iterate over any elements.
    ///
    /// @see BreakableIterable#iterator()
    @Test
    @DisplayName("Test the hasNext method with the ITERATOR_IS_ALWAYS_EMPTY break")
    public void testIteratorWithAlwaysEmptyBreak() {
        BreakableIterable<Integer> working = Breakables.buildIterable(1, 2, 3)
                .build();
        assertTrue(working.iterator().hasNext());

        BreakableIterable<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_IS_ALWAYS_EMPTY)
                .build();

        assertFalse(broken.iterator().hasNext());

        BreakableIterable<Integer> empty = Breakables.buildIterable(Integer.class)
                .addBreak(ITERATOR_IS_ALWAYS_EMPTY)
                .build();

        // Empty iterators should still be empty
        assertFalse(empty.iterator().hasNext());

    }

    /// Test that the `ITERATOR_HAS_NEXT_ALWAYS_RETURNS_TRUE` break causes the `hasNext` method to always return `true`.
    ///
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

    /// Test that the `ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE` break causes the `hasNext` method to always return
    /// `false`.
    ///
    /// @see BreakableIterator#hasNext()
    @DisplayName("Test the `hasNext` method with the ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE break")
    @Test
    public void testIteratorWithHasNextAlwaysReturnsFalseBreak() {
        BreakableIterable<Integer> empty = Breakables.buildIterable(Integer.class)
                .addBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE)
                .build();
        assertFalse(empty.iterator().hasNext());

        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_HAS_NEXT_ALWAYS_RETURNS_FALSE)
                .build();

        Iterator<Integer> iterator = iterable.iterator();
        assertFalse(iterator.hasNext());
    }

    /// Test that the `ITERATOR_HAS_NEXT_RETURNS_OPPOSITE_VALUE` break causes the `hasNext` method to return the
    /// opposite value..
    ///
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

    // ========== next Tests ==========

    /// Test that the `ITERATOR_NEXT_ALWAYS_RETURNS_NULL` break causes the `next` method to always return `null`
    ///
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
    ///
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

    // ========== remove Tests ==========

    /// Test that the `ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT` break causes the `remove` method to not remove the
    /// element at the iterators current position.
    ///
    /// @see BreakableIterator#remove()
    @Test
    @DisplayName("Test the `remove` method with the ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT break")
    public void testIteratorWithRemoveDoesNotRemoveElementBreak() {
        BreakableIterable<Integer> working = Breakables.buildIterable(1, 2, 3)
                .build();

        Iterator<Integer> workingIterator = working.iterator();
        workingIterator.next();
        workingIterator.remove();
        assertEquals(List.of(2, 3), working);

        BreakableIterable<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_REMOVE_DOES_NOT_REMOVE_ELEMENT)
                .build();

        Iterator<Integer> brokenIterator = broken.iterator();
        brokenIterator.next();
        brokenIterator.remove();
        assertEquals(List.of(1, 2, 3), broken);
    }

    ///  Test 'remove' throws [UnsupportedOperationException] when it is not supported
    /// @see BreakableIterator#remove
    @Test
    @DisplayName("Test remove method fails when not supported")
    public void testRemoveFailsWhenNotSupported() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .doesNotSupport(IterableMethods.ITERATOR_REMOVE)
                .build();

        Iterator<Integer> iterator = iterable.iterator();
        iterator.next();

        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    /// Test that the `ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE` break causes the `remove` method to
    /// throw the wrong exception when it is in an illegal state.
    ///
    /// @see BreakableIterator#remove
    @Test
    @DisplayName("Test remove method with the ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE break")
    public void testRemoveWithThrowsWrongExceptionForIllegalStateBreak() {
        BreakableIterable<Integer> working = Breakables.buildIterable(1, 2, 3)
                .build();

        Iterator<Integer> workingIterator = working.iterator();
        assertThrows(IllegalStateException.class, workingIterator::remove);

        workingIterator.next();
        workingIterator.remove();
        assertThrows(IllegalStateException.class, workingIterator::remove);

        BreakableIterable<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_FOR_ILLEGAL_STATE)
                .build();

        Iterator<Integer> brokenIterator = broken.iterator();
        assertThrowsDifferent(IllegalStateException.class, brokenIterator::remove);

        brokenIterator.next();
        brokenIterator.remove();
        assertThrowsDifferent(IllegalStateException.class, brokenIterator::remove);
    }

    /// Test that the `ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED` break causes the `remove` method to
    /// throw the wrong exception when it is not supported.
    ///
    /// @see BreakableIterator#remove
    @Test
    @DisplayName("Test remove method with the ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED break")
    public void testRemoveWithThrowsWrongExceptionIfNotSupportedBreak() {
        // Working case covered by testRemoveFailsWhenNotSupported test above.

        BreakableIterable<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_REMOVE_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED)
                .doesNotSupport(IterableMethods.ITERATOR_REMOVE)
                .build();

        Iterator<Integer> brokenIterator = broken.iterator();
        brokenIterator.next();
        assertThrowsDifferent(UnsupportedOperationException.class, brokenIterator::remove);
    }


    // ========== forEachRemaining Tests ==========

    /// Test that the `ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION` break causes the `forEachRemaining` method to
    /// not call the action on any elements.
    ///
    /// @see BreakableIterator#forEachRemaining(Consumer)
    @Test
    @DisplayName("Test the `forEachRemaining` method with the ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION break")
    public void testIteratorForEachRemainingWithDoesNotCallActionBreak() {
        BreakableIterable<Integer> working = Breakables.buildIterable(1, 2, 3)
                .build();

        Collection<Integer> workingElements = new ArrayList<>();
        working.iterator().forEachRemaining(workingElements::add);
        assertEquals(List.of(1, 2, 3), workingElements);

        BreakableIterable<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION)
                .build();

        Collection<Integer> brokenElements = new ArrayList<>();
        broken.iterator().forEachRemaining(brokenElements::add);
        assertEquals(List.of(), brokenElements);
    }

    /// Test that the `ITERATOR_IS_ALWAYS_EMPTY` break causes the `forEachRemaining` method to
    /// not call the action on any elements.
    ///
    /// @see BreakableIterator#forEachRemaining(Consumer)
    @Test
    @DisplayName("Test the `forEachRemaining` method with the ITERATOR_IS_ALWAYS_EMPTY break")
    public void testIteratorForEachRemainingWithIteratorIsAlwaysEmptyBreak() {
        BreakableIterable<Integer> working = Breakables.buildIterable(1, 2, 3)
                .build();

        final AtomicInteger workingCount = new AtomicInteger();
        working.iterator().forEachRemaining((e) -> workingCount.incrementAndGet());
        assertEquals(3, workingCount.get());

        BreakableIterable<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_IS_ALWAYS_EMPTY)
                .build();

        final AtomicInteger brokenCount = new AtomicInteger();
        broken.iterator().forEachRemaining((e) -> brokenCount.incrementAndGet());
        assertEquals(0, brokenCount.get());
    }

    /// Test that the `ITERATOR_SKIPS_FIRST_ELEMENT` break causes the `forEachRemaining` method to
    /// not call the action on the first element
    ///
    /// @see BreakableIterator#forEachRemaining(Consumer)
    @Test
    @DisplayName("Test the `forEachRemaining` method with the ITERATOR_SKIPS_FIRST_ELEMENT break")
    public void testIteratorForEachRemainingWithIteratorSkipsFirstElementBreak() {
        BreakableIterable<Integer> working = Breakables.buildIterable(1, 2, 3)
                .build();

        Collection<Integer> elements = new ArrayList<>();
        working.iterator().forEachRemaining(elements::add);
        assertEquals(List.of(1, 2, 3), elements);

        BreakableIterable<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_SKIPS_FIRST_ELEMENT)
                .build();

        Collection<Integer> missingFirstElement = new ArrayList<>();
        broken.iterator().forEachRemaining(missingFirstElement::add);
        assertEquals(List.of(2, 3), missingFirstElement);
    }

    /// Test that the `ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT` break causes the
    /// `forEachRemaining` method to throw the wrong exception on a null argument.
    ///
    /// @see BreakableIterator#forEachRemaining(Consumer)
    @Test
    @DisplayName("Test the `forEachRemaining` method with the "
            + "ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT break")
    public void testIteratorForEachRemainingWithThrowsWrongExceptionForNullArgumentBreak() {
        BreakableIterable<Integer> working = Breakables.buildIterable(1, 2, 3)
                .build();

        assertThrows(NullPointerException.class, () -> working.iterator().forEachRemaining(null));

        BreakableIterable<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT)
                .build();

        assertThrowsDifferent(NullPointerException.class, () -> broken.iterator().forEachRemaining(null));
    }

    ///  Test 'forEachRemaining' throws [UnsupportedOperationException] when it is not supported
    /// @see BreakableIterator#forEachRemaining(Consumer)
    @Test
    @DisplayName("Test forEachRemaining method fails when not supported")
    public void testForEachRemainingFailsWhenNotSupported() {
        BreakableIterable<Integer> iterable = Breakables.buildIterable(1, 2, 3)
                .doesNotSupport(IterableMethods.ITERATOR_FOR_EACH_REMAINING)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> iterable.iterator().forEachRemaining((c) -> {}));
    }

    /// Test that the `ITERATOR_HAS_NEXT_ADVANCES` break makes `hasNext` take and discard the next element.
    ///
    /// @see BreakableIterator#hasNext()
    @Test
    @DisplayName("hasNext() with ITERATOR_HAS_NEXT_ADVANCES discards the next element")
    public void hasNextWithAdvancesBreakDiscardsTheNextElement() {
        Iterator<Integer> working = Breakables.buildIterable(1, 2, 3).build().iterator();
        assertTrue(working.hasNext());
        assertTrue(working.hasNext());
        assertEquals(1, working.next());

        Iterator<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_HAS_NEXT_ADVANCES)
                .build().iterator();
        assertTrue(broken.hasNext());
        assertEquals(2, broken.next());
        assertTrue(broken.hasNext());
        assertThrows(NoSuchElementException.class, broken::next);
        assertFalse(broken.hasNext());
    }

    /// Test that the `ITERATOR_REMOVE_IGNORES_ILLEGAL_STATE` break makes `remove` return in an illegal state.
    ///
    /// @see BreakableIterator#remove()
    @Test
    @DisplayName("remove() with ITERATOR_REMOVE_IGNORES_ILLEGAL_STATE returns instead of throwing")
    public void removeWithIgnoresIllegalStateBreakReturns() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        Iterator<Integer> broken = new BreakableIterator<>(list.iterator(),
                java.util.Set.of(ITERATOR_REMOVE_IGNORES_ILLEGAL_STATE), java.util.Map.of(), 0, false);
        broken.remove();
        broken.next();
        broken.remove();
        broken.remove();
        assertEquals(List.of(2, 3), list);

        Iterator<Integer> working = new ArrayList<>(List.of(1, 2, 3)).iterator();
        assertThrows(IllegalStateException.class, working::remove);
    }

    /// Test that the `ITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION` break swallows the action's exception and
    /// carries on.
    ///
    /// @see BreakableIterator#forEachRemaining(Consumer)
    @Test
    @DisplayName("forEachRemaining() with ITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION does not relay it")
    public void forEachRemainingWithSwallowsBreakDoesNotRelay() {
        Iterator<Integer> working = Breakables.buildIterable(1, 2, 3).build().iterator();
        assertThrows(IllegalStateException.class, () -> working.forEachRemaining(e -> {
            throw new IllegalStateException();
        }));

        Iterator<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION)
                .build().iterator();
        AtomicInteger calls = new AtomicInteger();
        broken.forEachRemaining(e -> {
            calls.incrementAndGet();
            throw new IllegalStateException();
        });
        assertEquals(3, calls.get());
        assertFalse(broken.hasNext());
    }

    /// Test that the `ITERATOR_IS_ALWAYS_EMPTY` break keeps a list iterator a list iterator.
    @Test
    @DisplayName("ITERATOR_IS_ALWAYS_EMPTY on a list iterator gives an empty list iterator")
    public void alwaysEmptyOnListIteratorGivesEmptyListIterator() {
        BreakableListIterator<Integer> broken = new BreakableListIterator<>(
                new ArrayList<>(List.of(1, 2)).listIterator(),
                java.util.Set.of(ITERATOR_IS_ALWAYS_EMPTY), java.util.Map.of());
        assertFalse(broken.hasNext());
        assertFalse(broken.hasPrevious());
        assertEquals(0, broken.nextIndex());
    }

    /// Test that the `ITERATOR_NEXT_REQUIRES_HAS_NEXT` break makes `next` throw unless `hasNext` came first.
    ///
    /// @see BreakableIterator#next()
    @Test
    @DisplayName("next() with ITERATOR_NEXT_REQUIRES_HAS_NEXT throws unless hasNext() was called first")
    public void nextWithRequiresHasNextBreakThrowsWithoutHasNext() {
        Iterator<Integer> working = Breakables.buildIterable(1, 2, 3).build().iterator();
        assertEquals(1, working.next());
        assertEquals(2, working.next());

        Iterator<Integer> broken = Breakables.buildIterable(1, 2, 3)
                .addBreak(ITERATOR_NEXT_REQUIRES_HAS_NEXT)
                .build().iterator();
        assertThrows(NoSuchElementException.class, broken::next);
        assertTrue(broken.hasNext());
        assertEquals(1, broken.next());
        assertThrows(NoSuchElementException.class, broken::next);
    }
}
