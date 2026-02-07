package org.soliscode.test.contract.queue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/// This interface tests if a queue class has implemented the [remove][Queue#remove] method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <Q> The queue type being tested.
/// @author evanbergstrom
/// @see Queue#remove
/// @since 1.0
public interface RemoveContract<E, Q extends Queue<E>> extends CollectionContractSupport<E, Q> {

    /// Tests that the [remove][Queue#remove] method works.
    @DisplayName("Test that the remove method works")
    @Test
    default void remove() {
        if (supportsMethod(QueueMethods.REMOVE)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Q queue = provider().createInstance(values);
            for (int i = 0; i < values.size(); i++) {
                E expected = values.get(i);
                assertEquals(expected, queue.remove());
                assertFalse(queue.contains(expected));
                assertEquals(values.size() - i - 1, queue.size());
            }
        } else {
            Q queue = provider().createInstanceWithUniqueElements();
            assertThrows(UnsupportedOperationException.class, queue::remove);
        }
    }

    /// Tests that the [remove][Queue#remove] method throws an exception when the queue is empty.
    @DisplayName("Test that the remove method throws an exception when the queue is empty")
    @Test
    default void removeWhenEmpty() {
        if (supportsMethod(QueueMethods.REMOVE)) {
            Q queue = provider().emptyInstance();
            assertThrows(NoSuchElementException.class, queue::remove);
        } else {
            Q queue = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, queue::remove);
        }
    }
}
