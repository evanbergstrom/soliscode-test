package org.soliscode.test.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests for the skipping iterators of [IterableTestUtils], which the breakables' skip breaks use
/// (soliscode-quant T-0002).
@DisplayName("IterableTestUtils skipping iterators")
public class IterableTestUtilsTest {

    /// Default constructor.
    public IterableTestUtilsTest() {
        super();
    }

    private static <E> List<E> drain(final Iterator<E> iterator) {
        List<E> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    @Test
    @DisplayName("skipFirstIterator returns every element but the first")
    void skipFirstIterator_whenDrained_skipsFirst() {
        assertEquals(List.of(2, 3, 4), drain(IterableTestUtils.skipFirstIterator(List.of(1, 2, 3, 4))));
        assertEquals(List.of(), drain(IterableTestUtils.skipFirstIterator(List.of(1))));
        assertEquals(List.of(), drain(IterableTestUtils.skipFirstIterator(List.of())));
    }

    @Test
    @DisplayName("skipLastIterator returns every element but the last")
    void skipLastIterator_whenDrained_skipsLast() {
        assertEquals(List.of(1, 2, 3), drain(IterableTestUtils.skipLastIterator(List.of(1, 2, 3, 4))));
        assertEquals(List.of(), drain(IterableTestUtils.skipLastIterator(List.of(1))));
        assertEquals(List.of(), drain(IterableTestUtils.skipLastIterator(List.of())));
    }

    @Test
    @DisplayName("skipLastIterator throws when next is called past the end")
    void skipLastIterator_pastTheEnd_throws() {
        Iterator<Integer> iterator = IterableTestUtils.skipLastIterator(List.of(1, 2));
        assertEquals(1, iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    @DisplayName("skipLastIterator removes every element but the last")
    void skipLastIterator_remove_keepsLast() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4));
        Iterator<Integer> iterator = IterableTestUtils.skipLastIterator(list);
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        assertEquals(List.of(4), list);
    }

    @Test
    @DisplayName("skipLastIterator.forEachRemaining stops before the last element")
    void skipLastIterator_forEachRemaining_stopsBeforeLast() {
        List<Integer> list = new ArrayList<>();
        Iterator<Integer> iterator = IterableTestUtils.skipLastIterator(List.of(1, 2, 3));
        iterator.next();
        iterator.forEachRemaining(list::add);
        assertEquals(List.of(2), list);
    }
}
