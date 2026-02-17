package org.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `pollLast` method of a `Deque`**
/// This interface defines tests for the [pollLast()][Deque#pollLast] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#pollLast
/// @since 1.0.0
public interface PollLastContract<E, D extends Deque<E>> extends CollectionContractSupport<E, D> {

    /// Tests that the [pollLast][Deque#pollLast] method correctly retrieves and removes the last element.
    @DisplayName("pollLast() retrieves and removes the last element")
    @Test
    default void pollLast_whenNotEmpty_returnsAndRemovesLast() {
        if (supportsMethod(DequeMethods.POLL_LAST)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            D deque = provider().createInstance(values);
            for (int i = 0; i < values.size(); i++) {
                E expected = values.get(values.size() - 1 - i);
                assertEquals(expected, deque.pollLast());
                assertFalse(deque.contains(expected));
                assertEquals(values.size() - i - 1, deque.size());
            }
        } else {
            D deque = provider().createInstanceWithUniqueElements();
            assertThrows(UnsupportedOperationException.class, deque::pollLast);
        }
    }

    /// Tests that the [pollLast][Deque#pollLast] method returns null when the deque is empty.
    @DisplayName("pollLast() returns null when the deque is empty")
    @Test
    default void pollLast_whenEmpty_returnsNull() {
        if (supportsMethod(DequeMethods.POLL_LAST)) {
            D deque = provider().emptyInstance();
            assertNull(deque.pollLast());
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, deque::pollLast);
        }
    }
}
