package org.soliscode.test.contract.queue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `poll` method of a `Queue`**
///
/// This interface defines tests for the [poll()][Queue#poll] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Queue] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a queue's `poll` implementation correctly:
/// - Retrieves and removes the head of this queue.
/// - Returns `null` if this queue is empty.
/// - Throws [UnsupportedOperationException] if the method is not supported.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyQueuePollTest implements PollContract<String, MyQueue<String>> {
///     @Override
///     public CollectionProvider<String, MyQueue<String>> provider() {
///         return MyQueue::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Queue] and [org.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <Q> The queue type being tested.
/// @author evanbergstrom
/// @see Queue#poll
/// @since 1.0.0
public interface PollContract<E, Q extends Queue<E>> extends CollectionContractSupport<E, Q> {

    /// Tests that the [poll][Queue#poll] method correctly retrieves and removes the head of the queue.
    ///
    /// This test verifies that:
    /// 1. `poll()` returns the correct head element.
    /// 2. `poll()` removes the element from the queue.
    /// 3. The size of the queue decreases by 1.
    /// 4. If `poll` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Queue#poll
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("poll() retrieves and removes the head of the queue")
    @Test
    default void poll_whenNotEmpty_returnsAndRemovesHead() {
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

    /// Tests that the [poll][Queue#poll] method returns `null` when the queue is empty.
    ///
    /// @see Queue#poll
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("poll() returns null when the queue is empty")
    @Test
    default void poll_whenEmpty_returnsNull() {
        if (supportsMethod(QueueMethods.POLL)) {
            Q queue = provider().emptyInstance();
            assertNull(queue.poll());
        } else {
            Q queue = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, queue::poll);
        }
    }
}
