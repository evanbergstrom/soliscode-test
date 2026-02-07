package org.soliscode.test.contract.queue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// This interface tests if a queue class has implemented the [element][Queue#element] method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <Q> The queue type being tested.
/// @author evanbergstrom
/// @see Queue#element
/// @since 1.0
public interface ElementContract<E, Q extends Queue<E>> extends CollectionContractSupport<E, Q> {

    /// Tests that the [element][Queue#element] method works.
    @DisplayName("Test that the element method works")
    @Test
    default void element() {
        if (supportsMethod(QueueMethods.ELEMENT)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Q queue = provider().createInstance(values);
            for (int i = 0; i < values.size(); i++) {
                E expected = values.get(i);
                assertEquals(expected, queue.element());
                // element() should not remove the element
                assertEquals(values.size() - i, queue.size());
                // Now remove it to check the next one
                assertEquals(expected, queue.remove());
            }
        } else {
            Q queue = provider().createInstanceWithUniqueElements();
            assertThrows(UnsupportedOperationException.class, queue::element);
        }
    }

    /// Tests that the [element][Queue#element] method throws an exception when the queue is empty.
    @DisplayName("Test that the element method throws an exception when the queue is empty")
    @Test
    default void elementWhenEmpty() {
        if (supportsMethod(QueueMethods.ELEMENT)) {
            Q queue = provider().emptyInstance();
            assertThrows(NoSuchElementException.class, queue::element);
        } else {
            Q queue = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, queue::element);
        }
    }
}
