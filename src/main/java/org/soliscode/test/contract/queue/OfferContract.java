package org.soliscode.test.contract.queue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `offer` method of a `Queue`**
///
/// This interface defines tests for the [offer(E)][Queue#offer] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Queue] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a queue's `offer` implementation correctly:
/// - Inserts the specified element into this queue if it is possible to do so immediately
///   without violating capacity restrictions.
/// - Returns `true` if the element was added, else `false`.
/// - Handles `null` values according to the queue's configuration.
/// - Handles duplicate values according to the queue's configuration.
/// - Throws [UnsupportedOperationException] if the method is not supported.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyQueueOfferTest implements OfferContract<String, MyQueue<String>> {
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
/// @see Queue#offer
/// @since 1.0.0
public interface OfferContract<E, Q extends Queue<E>> extends CollectionContractSupport<E, Q> {

    /// Tests that the [offer][Queue#offer] method successfully adds an element to the queue.
    ///
    /// This test verifies that:
    /// 1. A single element is added to the queue.
    /// 2. The method returns `true` if the element was added.
    /// 3. The queue contains the added element after the call.
    /// 4. The size of the queue increases appropriately.
    /// 5. If `offer` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Queue#offer
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("offer(E) adds a single element and updates size")
    @Test
    default void offer_singleElement_returnsTrueAndUpdatesSize() {
        if (supportsMethod(QueueMethods.OFFER)) {
            Q queue = provider().emptyInstance();
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            for (int i = 0; i < values.size(); i++) {
                assertTrue(queue.offer(values.get(i)));
                assertTrue(queue.contains(values.get(i)));
                assertEquals(i + 1, queue.size());
            }
        } else {
            Q queue = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> queue.offer(elementProvider().createInstance()));
        }
    }

    /// Tests that the [offer][Queue#offer] method handles `null` values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitNulls()]:
    /// - If `null` is permitted: Adding `null` should succeed, and the queue should contain `null`.
    /// - If `null` is not permitted: Adding `null` should throw [NullPointerException].
    /// - If `offer` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Queue#offer
    /// @throws NullPointerException if null is not permitted and the argument is null
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("offer(E) handles null values based on permission")
    @Test
    default void offer_withNullValue_handlesCorrectly() {
        if (supportsMethod(QueueMethods.OFFER)) {
            Q queue = provider().emptyInstance();
            if (permitNulls()) {
                assertTrue(queue.offer(null));
                assertTrue(queue.contains(null));
                assertEquals(1, queue.size());
            } else {
                assertThrows(NullPointerException.class, () -> queue.offer(null));
            }
        } else {
            Q queue = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> queue.offer(null));
        }
    }


    /// Tests that the [offer][Queue#offer] method handles duplicate values correctly.
    ///
    /// This test verifies the behavior based on [CollectionContractSupport#permitDuplicates()]:
    /// - If duplicates are permitted: Adding an existing element should succeed and increase the queue size.
    /// - If duplicates are not permitted: Adding an existing element should return `false` and not increase size.
    /// - If `offer` is not supported, it verifies that [UnsupportedOperationException] is thrown.
    ///
    /// @see Queue#offer
    /// @throws UnsupportedOperationException if the method is not supported
    /// @throws org.opentest4j.AssertionFailedError if any assertions failed
    /// @since 1.0.0
    @DisplayName("offer(E) handles duplicate values based on permission")
    @Test
    default void offer_withDuplicateValue_handlesCorrectly() {
        if (supportsMethod(QueueMethods.OFFER)) {
            if (permitDuplicates()) {
                List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
                Q queue = provider().createInstance(values);
                for (int i = 0; i < values.size(); i++) {
                    assertTrue(queue.offer(values.get(i)));
                    assertTrue(queue.contains(values.get(i)));
                    assertEquals(values.size() + i + 1, queue.size());
                }
            } else {
                Q queue = provider().emptyInstance();
                E value = elementProvider().createInstance();
                assertTrue(queue.offer(value));
                E otherValue = elementProvider().copyInstance(value);
                assertFalse(queue.offer(otherValue));
                assertEquals(1, queue.size());
            }
        } else {
            Q queue = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, () -> queue.offer(elementProvider().createInstance()));
        }
    }
}
