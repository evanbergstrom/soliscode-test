package org.soliscode.test.contract.queue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `peek` method of a `Queue`**
///
/// This interface defines tests for the [peek()][Queue#peek] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Queue] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a queue's `peek` implementation correctly:
/// - Retrieves, but does not remove, the head of this queue.
/// - Returns `null` if this queue is empty.
/// - Throws [UnsupportedOperationException] if the method is not supported.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyQueuePeekTest implements PeekContract<String, MyQueue<String>> {
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
/// @see Queue#peek
/// @since 1.0.0
public interface PeekContract<E, Q extends Queue<E>> extends CollectionContractSupport<E, Q> {

    /// Tests that the [peek][Queue#peek] method correctly retrieves the head of the queue.
    ///
    /// This test verifies that:
    /// 1. `peek()` returns the correct head element.
    /// 2. `peek()` does not remove the element from the queue.
    /// 3. If `peek` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Queue#peek
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("peek() retrieves the head of the queue")
    @Test
    default void peek_whenNotEmpty_returnsHead() {
        if (supportsMethod(QueueMethods.PEEK)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Q queue = provider().createInstance(values);
            for (E expected : values) {
                assertEquals(expected, queue.peek());
                assertEquals(expected, queue.poll());
            }
        } else {
            Q queue = provider().createInstanceWithUniqueElements();
            assertThrows(UnsupportedOperationException.class, queue::peek);
        }
    }

    /// Tests that the [peek][Queue#peek] method returns `null` when the queue is empty.
    ///
    /// @see Queue#peek
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("peek() returns null when the queue is empty")
    @Test
    default void peek_whenEmpty_returnsNull() {
        if (supportsMethod(QueueMethods.PEEK)) {
            Q queue = provider().emptyInstance();
            assertNull(queue.peek());
        } else {
            Q queue = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, queue::peek);
        }
    }
}
