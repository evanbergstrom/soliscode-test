package org.soliscode.test.contract.deque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// **Contract for the `peekLast` method of a `Deque`**
/// This interface defines tests for the [peekLast()][Deque#peekLast] method.
/// It is designed to be used as a mix-in interface by test classes that verify [Deque] implementations.
///
/// @param <E> The element type being tested.
/// @param <D> The deque type being tested.
/// @author evanbergstrom
/// @see Deque#peekLast
/// @since 1.0.0
public interface PeekLastContract<E, D extends Deque<E>> extends CollectionContractSupport<E, D> {

    /// Tests that the [peekLast][Deque#peekLast] method correctly retrieves the last element without removing it.
    @DisplayName("peekLast() retrieves the last element without removing it")
    @Test
    default void peekLast_whenNotEmpty_returnsLast() {
        if (supportsMethod(DequeMethods.PEEK_LAST)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            D deque = provider().createInstance(values);
            assertEquals(values.getLast(), deque.peekLast());
            assertEquals(values.size(), deque.size());
        } else {
            D deque = provider().createInstanceWithUniqueElements();
            assertThrows(UnsupportedOperationException.class, deque::peekLast);
        }
    }

    /// Tests that the [peekLast][Deque#peekLast] method returns null when the deque is empty.
    @DisplayName("peekLast() returns null when the deque is empty")
    @Test
    default void peekLast_whenEmpty_returnsNull() {
        if (supportsMethod(DequeMethods.PEEK_LAST)) {
            D deque = provider().emptyInstance();
            assertNull(deque.peekLast());
        } else {
            D deque = provider().emptyInstance();
            assertThrows(UnsupportedOperationException.class, deque::peekLast);
        }
    }
}
