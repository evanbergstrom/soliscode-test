package org.soliscode.test.contract.queue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/// This interface tests if a queue class has implemented the [poll][Queue#poll] method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <Q> The queue type being tested.
/// @author evanbergstrom
/// @see Queue#poll
/// @since 1.0
public interface PollContract<E, Q extends Queue<E>> extends CollectionContractSupport<E, Q> {

    /// Tests that the [poll][Queue#poll] method works.
    @DisplayName("Test that the poll method works")
    @Test
    default void poll() {
        if (supportsMethod(QueueMethods.POLL)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Q queue = provider().createInstance(values);
            for (int i = 0; i < values.size(); i++) {
                E expected = values.get(i);
                assertEquals(expected, queue.poll());
                assertFalse(queue.contains(expected));
                assertEquals(values.size() - i - 1, queue.size());
            }
        } else {
            Q queue = provider().createInstanceWithUniqueElements();
            assertThrows(UnsupportedOperationException.class, queue::poll);
        }
    }

    /// Tests that the [poll][Queue#poll] method returns null when the queue is empty.
    @DisplayName("Test that the poll method returns null when the queue is empty")
    @Test
    default void pollWhenEmpty() {
        if (supportsMethod(QueueMethods.POLL)) {
            Q queue = provider().emptyInstance();
            assertNull(queue.poll());
        } else {
            Q queue = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, queue::poll);
        }
    }
}
