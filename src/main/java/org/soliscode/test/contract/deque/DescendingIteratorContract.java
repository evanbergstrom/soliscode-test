package org.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `descendingIterator` method of a `Deque`**
/// This interface defines tests for the [descendingIterator()][Deque#descendingIterator] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#descendingIterator
/// @since 1.0.0
public interface DescendingIteratorContract<E, D extends Deque<E>> extends CollectionContractSupport<E, D> {

    /// Tests that the [descendingIterator][Deque#descendingIterator] method returns an iterator that traverses the elements in reverse order.
    @DisplayName("descendingIterator() returns an iterator that traverses the elements in reverse order")
    @Test
    default void descendingIterator_whenCalled_traversesInReverseOrder() {
        if (supportsMethod(DequeMethods.DESCENDING_ITERATOR)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            D deque = provider().createInstance(values);

            List<E> actual = new ArrayList<>();
            Iterator<E> it = deque.descendingIterator();
            while (it.hasNext()) {
                actual.add(it.next());
            }

            List<E> expected = new ArrayList<>(values);
            Collections.reverse(expected);

            assertEquals(expected, actual);
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, deque::descendingIterator);
        }
    }

    /// Tests that the [descendingIterator][Deque#descendingIterator] method returns an iterator that can remove elements.
    @DisplayName("descendingIterator() returns an iterator that supports removal")
    @Test
    default void descendingIterator_iterator_supportsRemoval() {
        if (supportsMethod(DequeMethods.DESCENDING_ITERATOR)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            D deque = provider().createInstance(values);

            Iterator<E> it = deque.descendingIterator();
            if (it.hasNext()) {
                E element = it.next();
                try {
                    it.remove();
                    assertFalse(deque.contains(element));
                    assertEquals(values.size() - 1, deque.size());
                } catch (UnsupportedOperationException e) {
                    // It's optional, but many Deques support it if they are modifiable
                }
            }
        }
    }
}
