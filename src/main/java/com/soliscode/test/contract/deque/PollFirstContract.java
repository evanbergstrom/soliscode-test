package com.soliscode.test.contract.deque;

import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CollectionContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `pollFirst` method of a `Deque`**
/// This interface defines tests for the [pollFirst()][Deque#pollFirst] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#pollFirst
/// @since 1.0.0
public interface PollFirstContract<D extends Deque<E>, E> extends CollectionContractSupport<D, E> {

    /// Tests that the [pollFirst][Deque#pollFirst] method correctly retrieves and removes the first element.
    @DisplayName("pollFirst() retrieves and removes the first element")
    @Test
    default void pollFirstWhenNotEmptyReturnsAndRemovesFirst() {
        if (supportsMethod(DequeMethods.POLL_FIRST)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            D deque = provider().createInstance(values);
            for (int i = 0; i < values.size(); i++) {
                E expected = values.get(i);
                assertEquals(expected, deque.pollFirst());
                assertFalse(deque.contains(expected));
                assertEquals(values.size() - i - 1, deque.size());
            }
        } else {
            D deque = provider().createInstanceWithUniqueElements();
            assertThrows(UnsupportedOperationException.class, deque::pollFirst);
        }
    }

    /// Tests that the [pollFirst][Deque#pollFirst] method returns null when the deque is empty.
    @DisplayName("pollFirst() returns null when the deque is empty")
    @Test
    default void pollFirstWhenEmptyReturnsNull() {
        if (supportsMethod(DequeMethods.POLL_FIRST)) {
            D deque = provider().emptyInstance();
            assertNull(deque.pollFirst());
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, deque::pollFirst);
        }
    }
}
