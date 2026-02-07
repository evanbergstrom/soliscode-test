package org.soliscode.test.contract.queue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/// This interface tests if a queue class has implemented the [offer][Queue#offer] method
/// correctly.
///
/// @param <E> The element type being tested.
/// @param <Q> The queue type being tested.
/// @author evanbergstrom
/// @see Queue#offer
/// @since 1.0
public interface OfferContract<E, Q extends Queue<E>> extends CollectionContractSupport<E, Q> {

    /// Tests that the [offer][Queue#offer] method works.
    @DisplayName("Test that the offer method works")
    @Test
    default void offer() {
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

    /// Tests that the [offer][Queue#offer] method handles null values correctly.
    @DisplayName("Test that the offer method works with null element values")
    @Test
    default void offerWithNullValue() {
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
    @DisplayName("Test that the offer method works with duplicate element values")
    @Test
    default void offerWithDuplicateValue() {
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
