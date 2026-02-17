package org.soliscode.test.contract.queue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `element` method of a `Queue`**
///
/// This interface defines tests for the [element()][Queue#element] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Queue] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a queue's `element` implementation correctly:
/// - Retrieves, but does not remove, the head of this queue.
/// - Throws [NoSuchElementException] if this queue is empty.
/// - Throws [UnsupportedOperationException] if the method is not supported.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyQueueElementTest implements ElementContract<String, MyQueue<String>> {
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
/// @see Queue#element
/// @since 1.0.0
public interface ElementContract<E, Q extends Queue<E>> extends CollectionContractSupport<E, Q> {

    /// Tests that the [element][Queue#element] method correctly retrieves the head of the queue.
    ///
    /// This test verifies that:
    /// 1. `element()` returns the correct head element.
    /// 2. `element()` does not remove the element from the queue.
    /// 3. If `element` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Queue#element
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("element() retrieves the head of the queue")
    @Test
    default void element_whenNotEmpty_returnsHead() {
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

    /// Tests that the [element][Queue#element] method throws [NoSuchElementException] when the queue is empty.
    ///
    /// @see Queue#element
    /// @throws NoSuchElementException if the queue is empty
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("element() throws NoSuchElementException when the queue is empty")
    @Test
    default void element_whenEmpty_throwsNoSuchElementException() {
        if (supportsMethod(QueueMethods.ELEMENT)) {
            Q queue = provider().emptyInstance();
            assertThrows(NoSuchElementException.class, queue::element);
        } else {
            Q queue = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, queue::element);
        }
    }
}
