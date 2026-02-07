package org.soliscode.test.contract.queue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/// This interface tests if a queue class has implemented the [peek][Queue#peek] method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <Q> The queue type being tested.
/// @author evanbergstrom
/// @see Queue#peek
/// @since 1.0
public interface PeekContract<E, Q extends Queue<E>> extends CollectionContractSupport<E, Q> {

    /// Tests that the [peek][Queue#peek] method works.
    @DisplayName("Test that the peek method works")
    @Test
    default void peek() {
        if (supportsMethod(QueueMethods.PEEK)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Q queue = provider().createInstance(values);
            for (int i = 0; i < values.size(); i++) {
                E expected = values.get(i);
                assertEquals(expected, queue.peek());
                assertEquals(expected, queue.poll());
            }
        } else {
            Q queue = provider().createInstanceWithUniqueElements();
            assertThrows(UnsupportedOperationException.class, queue::peek);
        }
    }

    /// Tests that the [peek][Queue#peek] method returns null when the queue is empty.
    @DisplayName("Test that the peek method returns null when the queue is empty")
    @Test
    default void peekWhenEmpty() {
        if (supportsMethod(QueueMethods.PEEK)) {
            Q queue = provider().emptyInstance();
            assertNull(queue.peek());
        } else {
            Q queue = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, queue::peek);
        }
    }
}
